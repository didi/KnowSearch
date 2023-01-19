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

package org.elasticsearch.percolator;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.ReaderUtil;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.FetchSubPhase;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightPhase;
import org.elasticsearch.search.fetch.subphase.highlight.Highlighter;
import org.elasticsearch.search.fetch.subphase.highlight.SearchContextHighlight;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Highlighting in the case of the percolate query is a bit different, because the PercolateQuery itself doesn't get highlighted,
 * but the source of the PercolateQuery gets highlighted by each hit containing a query.
 */
final class PercolatorHighlightSubFetchPhase implements FetchSubPhase {
    private final HighlightPhase highlightPhase;

    PercolatorHighlightSubFetchPhase(Map<String, Highlighter> highlighters) {
        this.highlightPhase = new HighlightPhase(highlighters);
    }

    boolean hitsExecutionNeeded(SearchContext context) { // for testing
        return context.highlight() != null && locatePercolatorQuery(context.query()).isEmpty() == false;
    }

    @Override
    public void hitsExecute(SearchContext context, SearchHit[] hits) throws IOException {
        if (hitsExecutionNeeded(context) == false) {
            return;
        }
        List<PercolateQuery> percolateQueries = locatePercolatorQuery(context.query());
        if (percolateQueries.isEmpty()) {
            // shouldn't happen as we checked for the existence of a percolator query in hitsExecutionNeeded(...)
            throw new IllegalStateException("couldn't locate percolator query");
        }

        boolean singlePercolateQuery = percolateQueries.size() == 1;
        for (PercolateQuery percolateQuery : percolateQueries) {
            String fieldName = singlePercolateQuery ? PercolatorMatchedSlotSubFetchPhase.FIELD_NAME_PREFIX :
                PercolatorMatchedSlotSubFetchPhase.FIELD_NAME_PREFIX + "_" + percolateQuery.getName();
            List<LeafReaderContext> ctxs = context.searcher().getIndexReader().leaves();
            IndexSearcher percolatorIndexSearcher = percolateQuery.getPercolatorIndexSearcher();
            PercolateQuery.QueryStore queryStore = percolateQuery.getQueryStore();

            LeafReaderContext percolatorLeafReaderContext = percolatorIndexSearcher.getIndexReader().leaves().get(0);
            FetchSubPhase.HitContext hitContext = new FetchSubPhase.HitContext();

            for (SearchHit hit : hits) {
                LeafReaderContext ctx = ctxs.get(ReaderUtil.subIndex(hit.docId(), ctxs));
                int segmentDocId = hit.docId() - ctx.docBase;
                final Query query = queryStore.getQueries(ctx).apply(segmentDocId);
                if (query != null) {
                    DocumentField field = hit.field(fieldName);
                    if (field == null) {
                        // It possible that a hit did not match with a particular percolate query,
                        // so then continue highlighting with the next hit.
                        continue;
                    }

                    for (Object matchedSlot : field.getValues()) {
                        int slot = (int) matchedSlot;
                        BytesReference document = percolateQuery.getDocuments().get(slot);
                        SearchContextHighlight highlight = new SearchContextHighlight(context.highlight().fields());
                        // Enforce highlighting by source, because MemoryIndex doesn't support stored fields.
                        highlight.globalForceSource(true);
                        QueryShardContext shardContext = new QueryShardContext(context.getQueryShardContext());
                        shardContext.freezeContext();
                        shardContext.lookup().source().setSegmentAndDocument(percolatorLeafReaderContext, slot);
                        shardContext.lookup().source().setSource(document);
                        hitContext.reset(
                            new SearchHit(slot, "unknown", new Text(hit.getType()), Collections.emptyMap()),
                            percolatorLeafReaderContext, slot, percolatorIndexSearcher
                        );
                        hitContext.cache().clear();
                        highlightPhase.hitExecute(context.shardTarget(), shardContext, query, highlight, hitContext);
                        for (Map.Entry<String, HighlightField> entry : hitContext.hit().getHighlightFields().entrySet()) {
                            if (percolateQuery.getDocuments().size() == 1) {
                                String hlFieldName;
                                if (singlePercolateQuery) {
                                    hlFieldName = entry.getKey();
                                } else {
                                    hlFieldName = percolateQuery.getName() + "_" + entry.getKey();
                                }
                                hit.getHighlightFields().put(hlFieldName, new HighlightField(hlFieldName, entry.getValue().fragments()));
                            } else {
                                // In case multiple documents are being percolated we need to identify to which document
                                // a highlight belongs to.
                                String hlFieldName;
                                if (singlePercolateQuery) {
                                    hlFieldName = slot + "_" + entry.getKey();
                                } else {
                                    hlFieldName = percolateQuery.getName() + "_" + slot + "_" + entry.getKey();
                                }
                                hit.getHighlightFields().put(hlFieldName, new HighlightField(hlFieldName, entry.getValue().fragments()));
                            }
                        }
                    }
                }
            }
        }
    }

    static List<PercolateQuery> locatePercolatorQuery(Query query) {
        if (query instanceof PercolateQuery) {
            return Collections.singletonList((PercolateQuery) query);
        } else if (query instanceof BooleanQuery) {
            List<PercolateQuery> percolateQueries = new ArrayList<>();
            for (BooleanClause clause : ((BooleanQuery) query).clauses()) {
                List<PercolateQuery> result = locatePercolatorQuery(clause.getQuery());
                if (result.isEmpty() == false) {
                    percolateQueries.addAll(result);
                }
            }
            return percolateQueries;
        } else if (query instanceof DisjunctionMaxQuery) {
            List<PercolateQuery> percolateQueries = new ArrayList<>();
            for (Query disjunct : ((DisjunctionMaxQuery) query).getDisjuncts()) {
                List<PercolateQuery> result = locatePercolatorQuery(disjunct);
                if (result.isEmpty() == false) {
                    percolateQueries.addAll(result);
                }
            }
            return  percolateQueries;
        } else if (query instanceof ConstantScoreQuery) {
            return locatePercolatorQuery(((ConstantScoreQuery) query).getQuery());
        } else if (query instanceof BoostQuery) {
            return locatePercolatorQuery(((BoostQuery) query).getQuery());
        } else if (query instanceof FunctionScoreQuery) {
            return locatePercolatorQuery(((FunctionScoreQuery) query).getSubQuery());
        }
        return Collections.emptyList();
    }
}
