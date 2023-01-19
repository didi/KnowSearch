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
package org.elasticsearch.search.aggregations.bucket;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.elasticsearch.search.aggregations.AggregatorTestCase;
import org.elasticsearch.search.aggregations.BucketCollector;
import org.elasticsearch.search.aggregations.LeafBucketCollector;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;

public class BestBucketsDeferringCollectorTests extends AggregatorTestCase {

    public void testReplay() throws Exception {
        Directory directory = newDirectory();
        RandomIndexWriter indexWriter = new RandomIndexWriter(random(), directory);
        int numDocs = randomIntBetween(1, 128);
        int maxNumValues = randomInt(16);
        for (int i = 0; i < numDocs; i++) {
            Document document = new Document();
            document.add(new StringField("field", String.valueOf(randomInt(maxNumValues)), Field.Store.NO));
            indexWriter.addDocument(document);
        }

        indexWriter.close();
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        TermQuery termQuery = new TermQuery(new Term("field", String.valueOf(randomInt(maxNumValues))));
        Query rewrittenQuery = indexSearcher.rewrite(termQuery);
        TopDocs topDocs = indexSearcher.search(termQuery, numDocs);

        SearchContext searchContext = createSearchContext(indexSearcher, createIndexSettings(), rewrittenQuery, null);
        when(searchContext.query()).thenReturn(rewrittenQuery);
        BestBucketsDeferringCollector collector = new BestBucketsDeferringCollector(searchContext, false) {
            @Override
            public ScoreMode scoreMode() {
                return ScoreMode.COMPLETE;
            }
        };
        Set<Integer> deferredCollectedDocIds = new HashSet<>();
        collector.setDeferredCollector(Collections.singleton(bla(deferredCollectedDocIds)));
        collector.preCollection();
        indexSearcher.search(termQuery, collector);
        collector.postCollection();
        collector.replay(0);

        assertEquals(topDocs.scoreDocs.length, deferredCollectedDocIds.size());
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            assertTrue("expected docid [" + scoreDoc.doc + "] is missing", deferredCollectedDocIds.contains(scoreDoc.doc));
        }

        topDocs = indexSearcher.search(new MatchAllDocsQuery(), numDocs);
        collector = new BestBucketsDeferringCollector(searchContext, true);
        deferredCollectedDocIds = new HashSet<>();
        collector.setDeferredCollector(Collections.singleton(bla(deferredCollectedDocIds)));
        collector.preCollection();
        indexSearcher.search(new MatchAllDocsQuery(), collector);
        collector.postCollection();
        collector.replay(0);

        assertEquals(topDocs.scoreDocs.length, deferredCollectedDocIds.size());
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            assertTrue("expected docid [" + scoreDoc.doc + "] is missing", deferredCollectedDocIds.contains(scoreDoc.doc));
        }
        indexReader.close();
        directory.close();
    }

    private BucketCollector bla(Set<Integer> docIds) {
        return new BucketCollector() {
            @Override
            public LeafBucketCollector getLeafCollector(LeafReaderContext ctx) throws IOException {
                return new LeafBucketCollector() {
                    @Override
                    public void collect(int doc, long bucket) throws IOException {
                        docIds.add(ctx.docBase + doc);
                    }
                };
            }

            @Override
            public void preCollection() throws IOException {

            }

            @Override
            public void postCollection() throws IOException {

            }

            @Override
            public ScoreMode scoreMode() {
                return ScoreMode.COMPLETE_NO_SCORES;
            }
        };
    }

}
