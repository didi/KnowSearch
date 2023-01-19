/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.common.lucene.search.function;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.FilterLeafCollector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.QueryVisitor;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.BulkScorer;
import org.apache.lucene.util.Bits;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.Version;
import org.elasticsearch.script.ScoreScript;
import org.elasticsearch.script.ScoreScript.ExplanationHolder;
import org.elasticsearch.script.Script;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * A query that uses a script to compute documents' scores.
 */
public class ScriptScoreQuery extends Query {
    private final Query subQuery;
    private final Script script;
    private final ScoreScript.LeafFactory scriptBuilder;
    private final Float minScore;
    private final String indexName;
    private final int shardId;
    private final Version indexVersion;

    public ScriptScoreQuery(Query subQuery, Script script, ScoreScript.LeafFactory scriptBuilder,
                            Float minScore, String indexName, int shardId, Version indexVersion) {
        this.subQuery = subQuery;
        this.script = script;
        this.scriptBuilder = scriptBuilder;
        this.minScore = minScore;
        this.indexName = indexName;
        this.shardId = shardId;
        this.indexVersion = indexVersion;
    }

    @Override
    public Query rewrite(IndexReader reader) throws IOException {
        Query newQ = subQuery.rewrite(reader);
        if (newQ != subQuery) {
            return new ScriptScoreQuery(newQ, script, scriptBuilder, minScore, indexName, shardId, indexVersion);
        }
        return super.rewrite(reader);
    }

    @Override
    public Weight createWeight(IndexSearcher searcher, ScoreMode scoreMode, float boost) throws IOException {
        if (scoreMode == ScoreMode.COMPLETE_NO_SCORES && minScore == null) {
            return subQuery.createWeight(searcher, scoreMode, boost);
        }
        boolean needsScore = scriptBuilder.needs_score();
        ScoreMode subQueryScoreMode = needsScore ? ScoreMode.COMPLETE : ScoreMode.COMPLETE_NO_SCORES;
        Weight subQueryWeight = subQuery.createWeight(searcher, subQueryScoreMode, boost);

        return new Weight(this){
            @Override
            public BulkScorer bulkScorer(LeafReaderContext context) throws IOException {
                if (minScore == null) {
                    final BulkScorer subQueryBulkScorer = subQueryWeight.bulkScorer(context);
                    if (subQueryBulkScorer == null) {
                        return null;
                    }
                    return new ScriptScoreBulkScorer(subQueryBulkScorer, subQueryScoreMode, makeScoreScript(context));
                } else {
                    return super.bulkScorer(context);
                }
            }

            @Override
            public void extractTerms(Set<Term> terms) {
                subQueryWeight.extractTerms(terms);
            }

            @Override
            public Scorer scorer(LeafReaderContext context) throws IOException {
                Scorer subQueryScorer = subQueryWeight.scorer(context);
                if (subQueryScorer == null) {
                    return null;
                }
                Scorer scriptScorer = new ScriptScorer(this, makeScoreScript(context), subQueryScorer, subQueryScoreMode, null);
                if (minScore != null) {
                    scriptScorer = new MinScoreScorer(this, scriptScorer, minScore);
                }
                return scriptScorer;
            }

            @Override
            public Explanation explain(LeafReaderContext context, int doc) throws IOException {
                Explanation subQueryExplanation = subQueryWeight.explain(context, doc);
                if (subQueryExplanation.isMatch() == false) {
                    return subQueryExplanation;
                }
                ExplanationHolder explanationHolder = new ExplanationHolder();
                Scorer scorer = new ScriptScorer(this, makeScoreScript(context),
                    subQueryWeight.scorer(context), subQueryScoreMode, explanationHolder);
                int newDoc = scorer.iterator().advance(doc);
                assert doc == newDoc; // subquery should have already matched above
                float score = scorer.score();
                
                Explanation explanation = explanationHolder.get(score, needsScore ? subQueryExplanation : null);
                if (explanation == null) {
                    // no explanation provided by user; give a simple one
                    String desc = "script score function, computed with script:\"" + script + "\"";
                    if (needsScore) {
                        Explanation scoreExp = Explanation.match(subQueryExplanation.getValue(), "_score: ", subQueryExplanation);
                        explanation = Explanation.match(score, desc, scoreExp);
                    } else {
                        explanation = Explanation.match(score, desc);
                    }
                }
                
                if (minScore != null && minScore > explanation.getValue().floatValue()) {
                    explanation = Explanation.noMatch("Score value is too low, expected at least " + minScore +
                        " but got " + explanation.getValue(), explanation);
                }
                return explanation;
            }

            private ScoreScript makeScoreScript(LeafReaderContext context) throws IOException {
                final ScoreScript scoreScript = scriptBuilder.newInstance(context);
                scoreScript._setIndexName(indexName);
                scoreScript._setShard(shardId);
                scoreScript._setIndexVersion(indexVersion);
                return scoreScript;
            }

            @Override
            public boolean isCacheable(LeafReaderContext ctx) {
                // If minScore is not null, then matches depend on statistics of the top-level reader.
                return minScore == null;
            }
        };
    }

    @Override
    public void visit(QueryVisitor visitor) {
        // Highlighters must visit the child query to extract terms
        subQuery.visit(visitor.getSubVisitor(BooleanClause.Occur.MUST, this));
    }

    @Override
    public String toString(String field) {
        StringBuilder sb = new StringBuilder();
        sb.append("script_score (").append(subQuery.toString(field)).append(", script: ");
        sb.append("{" + script.toString() + "}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScriptScoreQuery that = (ScriptScoreQuery) o;
        return shardId == that.shardId &&
            subQuery.equals(that.subQuery) &&
            script.equals(that.script) &&
            Objects.equals(minScore, that.minScore) &&
            indexName.equals(that.indexName) &&
            indexVersion.equals(that.indexVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subQuery, script, minScore, indexName, shardId, indexVersion);
    }


    private static class ScriptScorer extends Scorer {
        private final ScoreScript scoreScript;
        private final Scorer subQueryScorer;
        private final ExplanationHolder explanation;

        ScriptScorer(Weight weight, ScoreScript scoreScript, Scorer subQueryScorer,
                ScoreMode subQueryScoreMode, ExplanationHolder explanation) {
            super(weight);
            this.scoreScript = scoreScript;
            if (subQueryScoreMode == ScoreMode.COMPLETE) {
                scoreScript.setScorer(subQueryScorer);
            }
            this.subQueryScorer = subQueryScorer;
            this.explanation = explanation;
        }

        @Override
        public float score() throws IOException {
            int docId = docID();
            scoreScript.setDocument(docId);
            float score = (float) scoreScript.execute(explanation);
            if (score == Float.NEGATIVE_INFINITY || Float.isNaN(score)) {
                throw new ElasticsearchException(
                    "script_score query returned an invalid score [" + score + "] for doc [" + docId + "].");
            }
            return score;
        }
        @Override
        public int docID() {
            return subQueryScorer.docID();
        }

        @Override
        public DocIdSetIterator iterator() {
            return subQueryScorer.iterator();
        }

        @Override
        public float getMaxScore(int upTo) {
            return Float.MAX_VALUE; // TODO: what would be a good upper bound?
        }

    }

    private static class ScriptScorable extends Scorable {
        private final ScoreScript scoreScript;
        private final Scorable subQueryScorer;
        private final ExplanationHolder explanation;

        ScriptScorable(ScoreScript scoreScript, Scorable subQueryScorer,
                ScoreMode subQueryScoreMode, ExplanationHolder explanation) {
            this.scoreScript = scoreScript;
            if (subQueryScoreMode == ScoreMode.COMPLETE) {
                scoreScript.setScorer(subQueryScorer);
            }
            this.subQueryScorer = subQueryScorer;
            this.explanation = explanation;
        }

        @Override
        public float score() throws IOException {
            int docId = docID();
            scoreScript.setDocument(docId);
            float score = (float) scoreScript.execute(explanation);
            if (score == Float.NEGATIVE_INFINITY || Float.isNaN(score)) {
                throw new ElasticsearchException(
                    "script_score query returned an invalid score [" + score + "] for doc [" + docId + "].");
            }
            return score;
        }
        @Override
        public int docID() {
            return subQueryScorer.docID();
        }
    }

    /**
     * Use the {@link BulkScorer} of the sub-query,
     * as it may be significantly faster (e.g. BooleanScorer) than iterating over the scorer
     */
    private static class ScriptScoreBulkScorer extends BulkScorer {
        private final BulkScorer subQueryBulkScorer;
        private final ScoreMode subQueryScoreMode;
        private final ScoreScript scoreScript;

        ScriptScoreBulkScorer(BulkScorer subQueryBulkScorer, ScoreMode subQueryScoreMode, ScoreScript scoreScript) {
            this.subQueryBulkScorer = subQueryBulkScorer;
            this.subQueryScoreMode = subQueryScoreMode;
            this.scoreScript = scoreScript;
        }

        @Override
        public int score(LeafCollector collector, Bits acceptDocs, int min, int max) throws IOException {
            return subQueryBulkScorer.score(wrapCollector(collector), acceptDocs, min, max);
        }

        private LeafCollector wrapCollector(LeafCollector collector) {
            return new FilterLeafCollector(collector) {
                @Override
                public void setScorer(Scorable scorer) throws IOException {
                    in.setScorer(new ScriptScorable(scoreScript, scorer, subQueryScoreMode, null));
                }
            };
        }

        @Override
        public long cost() {
            return subQueryBulkScorer.cost();
        }

    }

}
