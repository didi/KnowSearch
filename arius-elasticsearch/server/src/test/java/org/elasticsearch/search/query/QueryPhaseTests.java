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

package org.elasticsearch.search.query;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LatLonDocValuesField;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NoMergePolicy;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.MinDocQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.DocValuesFieldExistsQuery;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldDoc;
import org.apache.lucene.search.FilterCollector;
import org.apache.lucene.search.FilterLeafCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MatchNoDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.join.BitSetProducer;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.FixedBitSet;
import org.elasticsearch.action.search.SearchShardTask;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.mapper.DateFieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.NumberFieldMapper;
import org.elasticsearch.index.query.ParsedQuery;
import org.elasticsearch.index.search.ESToParentBlockJoinQuery;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.IndexShardTestCase;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.internal.ContextIndexSearcher;
import org.elasticsearch.search.internal.ScrollContext;
import org.elasticsearch.search.internal.SearchContext;
import org.elasticsearch.search.sort.SortAndFormats;
import org.elasticsearch.test.TestSearchContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.elasticsearch.search.query.QueryPhase.indexFieldHasDuplicateData;
import static org.elasticsearch.search.query.TopDocsCollectorContext.hasInfMaxScore;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

public class QueryPhaseTests extends IndexShardTestCase {

    private IndexShard indexShard;

    @Override
    public Settings threadPoolSettings() {
        return Settings.builder().put(super.threadPoolSettings()).put("thread_pool.search.min_queue_size", 10).build();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        indexShard = newShard(true);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        closeShards(indexShard);
    }

    private void countTestCase(Query query, IndexReader reader, boolean shouldCollectSearch, boolean shouldCollectCount) throws Exception {
        ContextIndexSearcher searcher = shouldCollectSearch ? newContextSearcher(reader) :
            newEarlyTerminationContextSearcher(reader, 0);
        TestSearchContext context = new TestSearchContext(null, indexShard, searcher);
        context.parsedQuery(new ParsedQuery(query));
        context.setSize(0);
        context.setTask(new SearchShardTask(123L, "", "", "", null, Collections.emptyMap()));
        final boolean rescore = QueryPhase.executeInternal(context);
        assertFalse(rescore);

        ContextIndexSearcher countSearcher = shouldCollectCount ? newContextSearcher(reader) :
            newEarlyTerminationContextSearcher(reader, 0);
        assertEquals(countSearcher.count(query), context.queryResult().topDocs().topDocs.totalHits.value);
    }

    private void countTestCase(boolean withDeletions) throws Exception {
        Directory dir = newDirectory();
        IndexWriterConfig iwc = newIndexWriterConfig().setMergePolicy(NoMergePolicy.INSTANCE);
        RandomIndexWriter w = new RandomIndexWriter(random(), dir, iwc);
        final int numDocs = scaledRandomIntBetween(100, 200);
        for (int i = 0; i < numDocs; ++i) {
            Document doc = new Document();
            if (randomBoolean()) {
                doc.add(new StringField("foo", "bar", Store.NO));
                doc.add(new SortedSetDocValuesField("foo", new BytesRef("bar")));
                doc.add(new SortedSetDocValuesField("docValuesOnlyField", new BytesRef("bar")));
                doc.add(new LatLonDocValuesField("latLonDVField", 1.0, 1.0));
                doc.add(new LatLonPoint("latLonDVField", 1.0, 1.0));
            }
            if (randomBoolean()) {
                doc.add(new StringField("foo", "baz", Store.NO));
                doc.add(new SortedSetDocValuesField("foo", new BytesRef("baz")));
            }
            if (withDeletions && (rarely() || i == 0)) {
                doc.add(new StringField("delete", "yes", Store.NO));
            }
            w.addDocument(doc);
        }
        if (withDeletions) {
            w.deleteDocuments(new Term("delete", "yes"));
        }
        final IndexReader reader = w.getReader();
        Query matchAll = new MatchAllDocsQuery();
        Query matchAllCsq = new ConstantScoreQuery(matchAll);
        Query tq = new TermQuery(new Term("foo", "bar"));
        Query tCsq = new ConstantScoreQuery(tq);
        Query dvfeq = new DocValuesFieldExistsQuery("foo");
        Query dvfeq_points = new DocValuesFieldExistsQuery("latLonDVField");
        Query dvfeqCsq = new ConstantScoreQuery(dvfeq);
        // field with doc-values but not indexed will need to collect
        Query dvOnlyfeq = new DocValuesFieldExistsQuery("docValuesOnlyField");
        BooleanQuery bq = new BooleanQuery.Builder()
            .add(matchAll, Occur.SHOULD)
            .add(tq, Occur.MUST)
            .build();

        countTestCase(matchAll, reader, false, false);
        countTestCase(matchAllCsq, reader, false, false);
        countTestCase(tq, reader, withDeletions, withDeletions);
        countTestCase(tCsq, reader, withDeletions, withDeletions);
        countTestCase(dvfeq, reader, withDeletions, true);
        countTestCase(dvfeq_points, reader, withDeletions, true);
        countTestCase(dvfeqCsq, reader, withDeletions, true);
        countTestCase(dvOnlyfeq, reader, true, true);
        countTestCase(bq, reader, true, true);
        reader.close();
        w.close();
        dir.close();
    }

    public void testCountWithoutDeletions() throws Exception {
        countTestCase(false);
    }

    public void testCountWithDeletions() throws Exception {
        countTestCase(true);
    }

    public void testPostFilterDisablesCountOptimization() throws Exception {
        Directory dir = newDirectory();
        final Sort sort = new Sort(new SortField("rank", SortField.Type.INT));
        IndexWriterConfig iwc = newIndexWriterConfig()
            .setIndexSort(sort);
        RandomIndexWriter w = new RandomIndexWriter(random(), dir, iwc);
        Document doc = new Document();
        w.addDocument(doc);
        w.close();

        IndexReader reader = DirectoryReader.open(dir);

        TestSearchContext context =
            new TestSearchContext(null, indexShard, newEarlyTerminationContextSearcher(reader, 0));
        context.setTask(new SearchShardTask(123L, "", "", "", null, Collections.emptyMap()));
        context.parsedQuery(new ParsedQuery(new MatchAllDocsQuery()));

        QueryPhase.executeInternal(context);
        assertEquals(1, context.queryResult().topDocs().topDocs.totalHits.value);

        context.setSearcher(newContextSearcher(reader));
        context.parsedPostFilter(new ParsedQuery(new MatchNoDocsQuery()));
        QueryPhase.executeInternal(context);
        assertEquals(0, context.queryResult().topDocs().topDocs.totalHits.value);
        reader.close();
        dir.close();
    }

    public void testTerminateAfterWithFilter() throws Exception {
        Directory dir = newDirectory();
        final Sort sort = new Sort(new SortField("rank", SortField.Type.INT));
        IndexWriterConfig iwc = newIndexWriterConfig()
            .setIndexSort(sort);
        RandomIndexWriter w = new RandomIndexWriter(random(), dir, iwc);
        Document doc = new Document();
        for (int i = 0; i < 10; i++) {
            doc.add(new StringField("foo", Integer.toString(i), Store.NO));
        }
        w.addDocument(doc);
        w.close();

        IndexReader reader = DirectoryReader.open(dir);

        TestSearchContext context = new TestSearchContext(null, indexShard, newContextSearcher(reader));
        context.setTask(new SearchShardTask(123L, "", "", "", null, Collections.emptyMap()));

        context.parsedQuery(new ParsedQuery(new MatchAllDocsQuery()));
        context.terminateAfter(1);
        context.setSize(10);
        for (int i = 0; i < 10; i++) {
            context.parsedPostFilter(new ParsedQuery(new TermQuery(new Term("foo", Integer.toString(i)))));
            QueryPhase.executeInternal(context);
            assertEquals(1, context.queryResult().topDocs().topDocs.totalHits.value);
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(1));
        }
        reader.close();
        dir.close();
    }

    public void testMinScoreDisablesCountOptimization() throws Exception {
        Directory dir = newDirectory();
        final Sort sort = new Sort(new SortField("rank", SortField.Type.INT));
        IndexWriterConfig iwc = newIndexWriterConfig()
            .setIndexSort(sort);
        RandomIndexWriter w = new RandomIndexWriter(random(), dir, iwc);
        Document doc = new Document();
        w.addDocument(doc);
        w.close();

        IndexReader reader = DirectoryReader.open(dir);
        TestSearchContext context =
            new TestSearchContext(null, indexShard, newEarlyTerminationContextSearcher(reader, 0));
        context.parsedQuery(new ParsedQuery(new MatchAllDocsQuery()));
        context.setSize(0);
        context.setTask(new SearchShardTask(123L, "", "", "", null, Collections.emptyMap()));
        QueryPhase.executeInternal(context);
        assertEquals(1, context.queryResult().topDocs().topDocs.totalHits.value);

        context.minimumScore(100);
        QueryPhase.executeInternal(context);
        assertEquals(0, context.queryResult().topDocs().topDocs.totalHits.value);
        reader.close();
        dir.close();
    }

    public void testQueryCapturesThreadPoolStats() throws Exception {
        Directory dir = newDirectory();
        IndexWriterConfig iwc = newIndexWriterConfig();
        RandomIndexWriter w = new RandomIndexWriter(random(), dir, iwc);
        final int numDocs = scaledRandomIntBetween(100, 200);
        for (int i = 0; i < numDocs; ++i) {
            w.addDocument(new Document());
        }
        w.close();
        IndexReader reader = DirectoryReader.open(dir);
        TestSearchContext context = new TestSearchContext(null, indexShard, newContextSearcher(reader));
        context.setTask(new SearchShardTask(123L, "", "", "", null, Collections.emptyMap()));
        context.parsedQuery(new ParsedQuery(new MatchAllDocsQuery()));

        QueryPhase.executeInternal(context);
        QuerySearchResult results = context.queryResult();
        assertThat(results.serviceTimeEWMA(), greaterThanOrEqualTo(0L));
        assertThat(results.nodeQueueSize(), greaterThanOrEqualTo(0));
        reader.close();
        dir.close();
    }

    public void testInOrderScrollOptimization() throws Exception {
        Directory dir = newDirectory();
        final Sort sort = new Sort(new SortField("rank", SortField.Type.INT));
        IndexWriterConfig iwc = newIndexWriterConfig()
            .setIndexSort(sort);
        RandomIndexWriter w = new RandomIndexWriter(random(), dir, iwc);
        final int numDocs = scaledRandomIntBetween(100, 200);
        for (int i = 0; i < numDocs; ++i) {
            w.addDocument(new Document());
        }
        w.close();
        IndexReader reader = DirectoryReader.open(dir);
        TestSearchContext context = new TestSearchContext(null, indexShard, newContextSearcher(reader));
        context.parsedQuery(new ParsedQuery(new MatchAllDocsQuery()));
        ScrollContext scrollContext = new ScrollContext();
        scrollContext.lastEmittedDoc = null;
        scrollContext.maxScore = Float.NaN;
        scrollContext.totalHits = null;
        context.scrollContext(scrollContext);
        context.setTask(new SearchShardTask(123L, "", "", "", null, Collections.emptyMap()));
        int size = randomIntBetween(2, 5);
        context.setSize(size);

        QueryPhase.executeInternal(context);
        assertThat(context.queryResult().topDocs().topDocs.totalHits.value, equalTo((long) numDocs));
        assertNull(context.queryResult().terminatedEarly());
        assertThat(context.terminateAfter(), equalTo(0));
        assertThat(context.queryResult().getTotalHits().value, equalTo((long) numDocs));

        context.setSearcher(newEarlyTerminationContextSearcher(reader, size));
        QueryPhase.executeInternal(context);
        assertThat(context.queryResult().topDocs().topDocs.totalHits.value, equalTo((long) numDocs));
        assertThat(context.terminateAfter(), equalTo(size));
        assertThat(context.queryResult().getTotalHits().value, equalTo((long) numDocs));
        assertThat(context.queryResult().topDocs().topDocs.scoreDocs[0].doc, greaterThanOrEqualTo(size));
        reader.close();
        dir.close();
    }

    public void testTerminateAfterEarlyTermination() throws Exception {
        Directory dir = newDirectory();
        IndexWriterConfig iwc = newIndexWriterConfig();
        RandomIndexWriter w = new RandomIndexWriter(random(), dir, iwc);
        final int numDocs = scaledRandomIntBetween(100, 200);
        for (int i = 0; i < numDocs; ++i) {
            Document doc = new Document();
            if (randomBoolean()) {
                doc.add(new StringField("foo", "bar", Store.NO));
            }
            if (randomBoolean()) {
                doc.add(new StringField("foo", "baz", Store.NO));
            }
            doc.add(new NumericDocValuesField("rank", numDocs - i));
            w.addDocument(doc);
        }
        w.close();
        final IndexReader reader = DirectoryReader.open(dir);
        TestSearchContext context = new TestSearchContext(null, indexShard, newContextSearcher(reader));
        context.setTask(new SearchShardTask(123L, "", "", "", null, Collections.emptyMap()));
        context.parsedQuery(new ParsedQuery(new MatchAllDocsQuery()));

        context.terminateAfter(numDocs);
        {
            context.setSize(10);
            TotalHitCountCollector collector = new TotalHitCountCollector();
            context.queryCollectors().put(TotalHitCountCollector.class, collector);
            QueryPhase.executeInternal(context);
            assertFalse(context.queryResult().terminatedEarly());
            assertThat(context.queryResult().topDocs().topDocs.totalHits.value, equalTo((long) numDocs));
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(10));
            assertThat(collector.getTotalHits(), equalTo(numDocs));
        }

        context.terminateAfter(1);
        {
            context.setSize(1);
            QueryPhase.executeInternal(context);
            assertTrue(context.queryResult().terminatedEarly());
            assertThat(context.queryResult().topDocs().topDocs.totalHits.value, equalTo(1L));
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(1));

            context.setSize(0);
            QueryPhase.executeInternal(context);
            assertTrue(context.queryResult().terminatedEarly());
            assertThat(context.queryResult().topDocs().topDocs.totalHits.value, equalTo(1L));
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(0));
        }

        {
            context.setSize(1);
            QueryPhase.executeInternal(context);
            assertTrue(context.queryResult().terminatedEarly());
            assertThat(context.queryResult().topDocs().topDocs.totalHits.value, equalTo(1L));
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(1));
        }
        {
            context.setSize(1);
            BooleanQuery bq = new BooleanQuery.Builder()
                .add(new TermQuery(new Term("foo", "bar")), Occur.SHOULD)
                .add(new TermQuery(new Term("foo", "baz")), Occur.SHOULD)
                .build();
            context.parsedQuery(new ParsedQuery(bq));
            QueryPhase.executeInternal(context);
            assertTrue(context.queryResult().terminatedEarly());
            assertThat(context.queryResult().topDocs().topDocs.totalHits.value, equalTo(1L));
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(1));

            context.setSize(0);
            context.parsedQuery(new ParsedQuery(bq));
            QueryPhase.executeInternal(context);
            assertTrue(context.queryResult().terminatedEarly());
            assertThat(context.queryResult().topDocs().topDocs.totalHits.value, equalTo(1L));
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(0));
        }
        {
            context.setSize(1);
            TotalHitCountCollector collector = new TotalHitCountCollector();
            context.queryCollectors().put(TotalHitCountCollector.class, collector);
            QueryPhase.executeInternal(context);
            assertTrue(context.queryResult().terminatedEarly());
            assertThat(context.queryResult().topDocs().topDocs.totalHits.value, equalTo(1L));
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(1));
            assertThat(collector.getTotalHits(), equalTo(1));
            context.queryCollectors().clear();
        }
        {
            context.setSize(0);
            TotalHitCountCollector collector = new TotalHitCountCollector();
            context.queryCollectors().put(TotalHitCountCollector.class, collector);
            QueryPhase.executeInternal(context);
            assertTrue(context.queryResult().terminatedEarly());
            assertThat(context.queryResult().topDocs().topDocs.totalHits.value, equalTo(1L));
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(0));
            assertThat(collector.getTotalHits(), equalTo(1));
        }
        reader.close();
        dir.close();
    }

    public void testIndexSortingEarlyTermination() throws Exception {
        Directory dir = newDirectory();
        final Sort sort = new Sort(new SortField("rank", SortField.Type.INT));
        IndexWriterConfig iwc = newIndexWriterConfig()
            .setIndexSort(sort);
        RandomIndexWriter w = new RandomIndexWriter(random(), dir, iwc);
        final int numDocs = scaledRandomIntBetween(100, 200);
        for (int i = 0; i < numDocs; ++i) {
            Document doc = new Document();
            if (randomBoolean()) {
                doc.add(new StringField("foo", "bar", Store.NO));
            }
            if (randomBoolean()) {
                doc.add(new StringField("foo", "baz", Store.NO));
            }
            doc.add(new NumericDocValuesField("rank", numDocs - i));
            w.addDocument(doc);
        }
        w.close();

        final IndexReader reader = DirectoryReader.open(dir);
        TestSearchContext context = new TestSearchContext(null, indexShard, newContextSearcher(reader));
        context.parsedQuery(new ParsedQuery(new MatchAllDocsQuery()));
        context.setSize(1);
        context.setTask(new SearchShardTask(123L, "", "", "", null, Collections.emptyMap()));
        context.sort(new SortAndFormats(sort, new DocValueFormat[] {DocValueFormat.RAW}));


        QueryPhase.executeInternal(context);
        assertThat(context.queryResult().topDocs().topDocs.totalHits.value, equalTo((long) numDocs));
        assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(1));
        assertThat(context.queryResult().topDocs().topDocs.scoreDocs[0], instanceOf(FieldDoc.class));
        FieldDoc fieldDoc = (FieldDoc) context.queryResult().topDocs().topDocs.scoreDocs[0];
        assertThat(fieldDoc.fields[0], equalTo(1));

        {
            context.parsedPostFilter(new ParsedQuery(new MinDocQuery(1)));
            QueryPhase.executeInternal(context);
            assertNull(context.queryResult().terminatedEarly());
            assertThat(context.queryResult().topDocs().topDocs.totalHits.value, equalTo(numDocs - 1L));
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(1));
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs[0], instanceOf(FieldDoc.class));
            assertThat(fieldDoc.fields[0], anyOf(equalTo(1), equalTo(2)));
            context.parsedPostFilter(null);

            final TotalHitCountCollector totalHitCountCollector = new TotalHitCountCollector();
            context.queryCollectors().put(TotalHitCountCollector.class, totalHitCountCollector);
            QueryPhase.executeInternal(context);
            assertNull(context.queryResult().terminatedEarly());
            assertThat(context.queryResult().topDocs().topDocs.totalHits.value, equalTo((long) numDocs));
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(1));
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs[0], instanceOf(FieldDoc.class));
            assertThat(fieldDoc.fields[0], anyOf(equalTo(1), equalTo(2)));
            assertThat(totalHitCountCollector.getTotalHits(), equalTo(numDocs));
            context.queryCollectors().clear();
        }

        {
            context.setSearcher(newEarlyTerminationContextSearcher(reader, 1));
            context.trackTotalHitsUpTo(SearchContext.TRACK_TOTAL_HITS_DISABLED);
            QueryPhase.executeInternal(context);
            assertNull(context.queryResult().terminatedEarly());
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(1));
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs[0], instanceOf(FieldDoc.class));
            assertThat(fieldDoc.fields[0], anyOf(equalTo(1), equalTo(2)));

            QueryPhase.executeInternal(context);
            assertNull(context.queryResult().terminatedEarly());
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(1));
            assertThat(context.queryResult().topDocs().topDocs.scoreDocs[0], instanceOf(FieldDoc.class));
            assertThat(fieldDoc.fields[0], anyOf(equalTo(1), equalTo(2)));
        }
        reader.close();
        dir.close();
    }

    public void testIndexSortScrollOptimization() throws Exception {
        Directory dir = newDirectory();
        final Sort indexSort = new Sort(
            new SortField("rank", SortField.Type.INT),
            new SortField("tiebreaker", SortField.Type.INT)
        );
        IndexWriterConfig iwc = newIndexWriterConfig().setIndexSort(indexSort);
        RandomIndexWriter w = new RandomIndexWriter(random(), dir, iwc);
        final int numDocs = scaledRandomIntBetween(100, 200);
        for (int i = 0; i < numDocs; ++i) {
            Document doc = new Document();
            doc.add(new NumericDocValuesField("rank", random().nextInt()));
            doc.add(new NumericDocValuesField("tiebreaker", i));
            w.addDocument(doc);
        }
        if (randomBoolean()) {
            w.forceMerge(randomIntBetween(1, 10));
        }
        w.close();

        final IndexReader reader = DirectoryReader.open(dir);
        List<SortAndFormats> searchSortAndFormats = new ArrayList<>();
        searchSortAndFormats.add(new SortAndFormats(indexSort, new DocValueFormat[]{DocValueFormat.RAW, DocValueFormat.RAW}));
        // search sort is a prefix of the index sort
        searchSortAndFormats.add(new SortAndFormats(new Sort(indexSort.getSort()[0]), new DocValueFormat[]{DocValueFormat.RAW}));
        for (SortAndFormats searchSortAndFormat : searchSortAndFormats) {
            TestSearchContext context = new TestSearchContext(null, indexShard, newContextSearcher(reader));
            context.parsedQuery(new ParsedQuery(new MatchAllDocsQuery()));
            ScrollContext scrollContext = new ScrollContext();
            scrollContext.lastEmittedDoc = null;
            scrollContext.maxScore = Float.NaN;
            scrollContext.totalHits = null;
            context.scrollContext(scrollContext);
            context.setTask(new SearchShardTask(123L, "", "", "", null, Collections.emptyMap()));
            context.setSize(10);
            context.sort(searchSortAndFormat);

            QueryPhase.executeInternal(context);
            assertThat(context.queryResult().topDocs().topDocs.totalHits.value, equalTo((long) numDocs));
            assertNull(context.queryResult().terminatedEarly());
            assertThat(context.terminateAfter(), equalTo(0));
            assertThat(context.queryResult().getTotalHits().value, equalTo((long) numDocs));
            int sizeMinus1 = context.queryResult().topDocs().topDocs.scoreDocs.length - 1;
            FieldDoc lastDoc = (FieldDoc) context.queryResult().topDocs().topDocs.scoreDocs[sizeMinus1];

            context.setSearcher(newEarlyTerminationContextSearcher(reader, 10));
            QueryPhase.executeInternal(context);
            assertNull(context.queryResult().terminatedEarly());
            assertThat(context.queryResult().topDocs().topDocs.totalHits.value, equalTo((long) numDocs));
            assertThat(context.terminateAfter(), equalTo(0));
            assertThat(context.queryResult().getTotalHits().value, equalTo((long) numDocs));
            FieldDoc firstDoc = (FieldDoc) context.queryResult().topDocs().topDocs.scoreDocs[0];
            for (int i = 0; i < searchSortAndFormat.sort.getSort().length; i++) {
                @SuppressWarnings("unchecked")
                FieldComparator<Object> comparator = (FieldComparator<Object>) searchSortAndFormat.sort.getSort()[i].getComparator(1, i);
                int cmp = comparator.compareValues(firstDoc.fields[i], lastDoc.fields[i]);
                if (cmp == 0) {
                    continue;
                }
                assertThat(cmp, equalTo(1));
                break;
            }
        }
        reader.close();
        dir.close();
    }

    public void testDisableTopScoreCollection() throws Exception {
        Directory dir = newDirectory();
        IndexWriterConfig iwc = newIndexWriterConfig(new StandardAnalyzer());
        RandomIndexWriter w = new RandomIndexWriter(random(), dir, iwc);
        Document doc = new Document();
        for (int i = 0; i < 10; i++) {
            doc.clear();
            if (i % 2 == 0) {
                doc.add(new TextField("title", "foo bar", Store.NO));
            } else {
                doc.add(new TextField("title", "foo", Store.NO));
            }
            w.addDocument(doc);
        }
        w.close();

        IndexReader reader = DirectoryReader.open(dir);
        TestSearchContext context = new TestSearchContext(null, indexShard, newContextSearcher(reader));
        context.setTask(new SearchShardTask(123L, "", "", "", null, Collections.emptyMap()));
        Query q = new SpanNearQuery.Builder("title", true)
            .addClause(new SpanTermQuery(new Term("title", "foo")))
            .addClause(new SpanTermQuery(new Term("title", "bar")))
            .build();

        context.parsedQuery(new ParsedQuery(q));
        context.setSize(3);
        context.trackTotalHitsUpTo(3);
        TopDocsCollectorContext topDocsContext = TopDocsCollectorContext.createTopDocsCollectorContext(context, false);
        assertEquals(topDocsContext.create(null).scoreMode(), org.apache.lucene.search.ScoreMode.COMPLETE);
        QueryPhase.executeInternal(context);
        assertEquals(5, context.queryResult().topDocs().topDocs.totalHits.value);
        assertEquals(context.queryResult().topDocs().topDocs.totalHits.relation, TotalHits.Relation.EQUAL_TO);
        assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(3));


        context.sort(new SortAndFormats(new Sort(new SortField("other", SortField.Type.INT)),
            new DocValueFormat[] { DocValueFormat.RAW }));
        topDocsContext = TopDocsCollectorContext.createTopDocsCollectorContext(context, false);
        assertEquals(topDocsContext.create(null).scoreMode(), org.apache.lucene.search.ScoreMode.COMPLETE_NO_SCORES);
        QueryPhase.executeInternal(context);
        assertEquals(5, context.queryResult().topDocs().topDocs.totalHits.value);
        assertThat(context.queryResult().topDocs().topDocs.scoreDocs.length, equalTo(3));
        assertEquals(context.queryResult().topDocs().topDocs.totalHits.relation, TotalHits.Relation.EQUAL_TO);

        reader.close();
        dir.close();
    }

    public void testNumericLongOrDateSortOptimization() throws Exception {
        final String fieldNameLong = "long-field";
        final String fieldNameDate = "date-field";
        MappedFieldType fieldTypeLong = new NumberFieldMapper.NumberFieldType(NumberFieldMapper.NumberType.LONG);
        MappedFieldType fieldTypeDate = new DateFieldMapper.Builder(fieldNameDate).fieldType();
        MapperService mapperService = mock(MapperService.class);
        when(mapperService.fullName(fieldNameLong)).thenReturn(fieldTypeLong);
        when(mapperService.fullName(fieldNameDate)).thenReturn(fieldTypeDate);

        final int numDocs = 7000;
        Directory dir = newDirectory();
        IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(null));
        for (int i = 1; i <= numDocs; ++i) {
            Document doc = new Document();
            long longValue = randomLongBetween(-10000000L, 10000000L);
            doc.add(new LongPoint(fieldNameLong, longValue));
            doc.add(new NumericDocValuesField(fieldNameLong, longValue));
            longValue = randomLongBetween(0, 3000000000000L);
            doc.add(new LongPoint(fieldNameDate, longValue));
            doc.add(new NumericDocValuesField(fieldNameDate, longValue));
            writer.addDocument(doc);
            if (i % 3500 == 0) writer.commit();
        }
        writer.close();
        final IndexReader reader = DirectoryReader.open(dir);

        TestSearchContext searchContext =
            spy(new TestSearchContext(null, indexShard, newOptimizedContextSearcher(reader, 0)));
        when(searchContext.mapperService()).thenReturn(mapperService);

        // 1. Test a sort on long field
        final SortField sortFieldLong = new SortField(fieldNameLong, SortField.Type.LONG);
        sortFieldLong.setMissingValue(Long.MAX_VALUE);
        final Sort longSort = new Sort(sortFieldLong);
        SortAndFormats sortAndFormats = new SortAndFormats(longSort, new DocValueFormat[]{DocValueFormat.RAW});
        searchContext.sort(sortAndFormats);
        searchContext.parsedQuery(new ParsedQuery(new MatchAllDocsQuery()));
        searchContext.setTask(new SearchShardTask(123L, "", "", "", null, Collections.emptyMap()));
        searchContext.setSize(10);
        QueryPhase.executeInternal(searchContext);
        assertSortResults(searchContext.queryResult().topDocs().topDocs, (long) numDocs, false);

        // 2. Test a sort on long field + date field
        final SortField sortFieldDate = new SortField(fieldNameDate, SortField.Type.LONG);
        DocValueFormat dateFormat = fieldTypeDate.docValueFormat(null, null);
        final Sort longDateSort = new Sort(sortFieldLong, sortFieldDate);
        sortAndFormats = new SortAndFormats(longDateSort, new DocValueFormat[]{DocValueFormat.RAW, dateFormat});
        searchContext.sort(sortAndFormats);
        QueryPhase.executeInternal(searchContext);
        assertSortResults(searchContext.queryResult().topDocs().topDocs, (long) numDocs, true);

        // 3. Test a sort on date field
        sortFieldDate.setMissingValue(Long.MAX_VALUE);
        final Sort dateSort = new Sort(sortFieldDate);
        sortAndFormats = new SortAndFormats(dateSort, new DocValueFormat[]{dateFormat});
        searchContext.sort(sortAndFormats);
        QueryPhase.executeInternal(searchContext);
        assertSortResults(searchContext.queryResult().topDocs().topDocs, (long) numDocs, false);

        // 4. Test a sort on date field + long field
        final Sort dateLongSort = new Sort(sortFieldDate, sortFieldLong);
        sortAndFormats = new SortAndFormats(dateLongSort, new DocValueFormat[]{dateFormat, DocValueFormat.RAW});
        searchContext.sort(sortAndFormats);
        QueryPhase.executeInternal(searchContext);
        assertSortResults(searchContext.queryResult().topDocs().topDocs, (long) numDocs, true);
        reader.close();
        dir.close();
    }

    @AwaitsFix(bugUrl = "https://github.com/elastic/elasticsearch/issues/49703")
    public void testIndexHasDuplicateData() throws IOException {
        int docsCount = 7000;
        int duplIndex = docsCount * 7 / 10;
        int duplIndex2 = docsCount * 3 / 10;
        long duplicateValue = randomLongBetween(-10000000L, 10000000L);
        Directory dir = newDirectory();
        IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(null));
        for (int docId = 0; docId < docsCount; docId++) {
            Document doc = new Document();
            long rndValue = randomLongBetween(-10000000L, 10000000L);
            long value = (docId < duplIndex) ? duplicateValue : rndValue;
            long value2 = (docId < duplIndex2) ? duplicateValue : rndValue;
            doc.add(new LongPoint("duplicateField", value));
            doc.add(new LongPoint("notDuplicateField", value2));
            writer.addDocument(doc);
        }
        writer.close();
        final IndexReader reader = DirectoryReader.open(dir);
        boolean hasDuplicateData = indexFieldHasDuplicateData(reader, "duplicateField");
        boolean hasDuplicateData2 = indexFieldHasDuplicateData(reader, "notDuplicateField");
        reader.close();
        dir.close();
        assertTrue(hasDuplicateData);
        assertFalse(hasDuplicateData2);
    }

    public void testMaxScoreQueryVisitor() {
        BitSetProducer producer = context -> new FixedBitSet(1);
        Query query = new ESToParentBlockJoinQuery(new MatchAllDocsQuery(), producer, ScoreMode.Avg, "nested");
        assertTrue(hasInfMaxScore(query));

        query = new ESToParentBlockJoinQuery(new MatchAllDocsQuery(), producer, ScoreMode.None, "nested");
        assertFalse(hasInfMaxScore(query));


        for (Occur occur : Occur.values()) {
            query = new BooleanQuery.Builder()
                .add(new ESToParentBlockJoinQuery(new MatchAllDocsQuery(), producer, ScoreMode.Avg, "nested"), occur)
                .build();
            if (occur == Occur.MUST) {
                assertTrue(hasInfMaxScore(query));
            } else {
                assertFalse(hasInfMaxScore(query));
            }

            query = new BooleanQuery.Builder()
                .add(new BooleanQuery.Builder()
                    .add(new ESToParentBlockJoinQuery(new MatchAllDocsQuery(), producer, ScoreMode.Avg, "nested"), occur)
                    .build(), occur)
                .build();
            if (occur == Occur.MUST) {
                assertTrue(hasInfMaxScore(query));
            } else {
                assertFalse(hasInfMaxScore(query));
            }

            query = new BooleanQuery.Builder()
                .add(new BooleanQuery.Builder()
                    .add(new ESToParentBlockJoinQuery(new MatchAllDocsQuery(), producer, ScoreMode.Avg, "nested"), occur)
                    .build(), Occur.FILTER)
                .build();
            assertFalse(hasInfMaxScore(query));

            query = new BooleanQuery.Builder()
                .add(new BooleanQuery.Builder()
                    .add(new SpanTermQuery(new Term("field", "foo")), occur)
                    .add(new ESToParentBlockJoinQuery(new MatchAllDocsQuery(), producer, ScoreMode.Avg, "nested"), occur)
                    .build(), occur)
                .build();
            if (occur == Occur.MUST) {
                assertTrue(hasInfMaxScore(query));
            } else {
                assertFalse(hasInfMaxScore(query));
            }
        }
    }

    // assert score docs are in order and their number is as expected
    private void assertSortResults(TopDocs topDocs, long expectedNumDocs, boolean isDoubleSort) {
        assertEquals(topDocs.totalHits.value, expectedNumDocs);
        long cur1, cur2;
        long prev1 = Long.MIN_VALUE;
        long prev2 = Long.MIN_VALUE;
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            cur1 = (long) ((FieldDoc) scoreDoc).fields[0];
            assertThat(cur1, greaterThanOrEqualTo(prev1)); // test that docs are properly sorted on the first sort
            if (isDoubleSort) {
                cur2 = (long) ((FieldDoc) scoreDoc).fields[1];
                if (cur1 == prev1) {
                    assertThat(cur2, greaterThanOrEqualTo(prev2)); // test that docs are properly sorted on the secondary sort
                }
                prev2 = cur2;
            }
            prev1 = cur1;
        }
    }

    public void testMinScore() throws Exception {
        Directory dir = newDirectory();
        IndexWriterConfig iwc = newIndexWriterConfig();
        RandomIndexWriter w = new RandomIndexWriter(random(), dir, iwc);
        for (int i = 0; i < 10; i++) {
            Document doc = new Document();
            doc.add(new StringField("foo", "bar", Store.NO));
            doc.add(new StringField("filter", "f1", Store.NO));
            w.addDocument(doc);
        }
        w.close();

        IndexReader reader = DirectoryReader.open(dir);
        TestSearchContext context = new TestSearchContext(null, indexShard, newContextSearcher(reader));
        context.parsedQuery(new ParsedQuery(
            new BooleanQuery.Builder()
                .add(new TermQuery(new Term("foo", "bar")), Occur.MUST)
                .add(new TermQuery(new Term("filter", "f1")), Occur.SHOULD)
                .build()
        ));
        context.minimumScore(0.01f);
        context.setTask(new SearchShardTask(123L, "", "", "", null, Collections.emptyMap()));
        context.setSize(1);
        context.trackTotalHitsUpTo(5);

        QueryPhase.executeInternal(context);
        assertEquals(10, context.queryResult().topDocs().topDocs.totalHits.value);

        reader.close();
        dir.close();

    }

    private static ContextIndexSearcher newContextSearcher(IndexReader reader) {
        return new ContextIndexSearcher(reader, IndexSearcher.getDefaultSimilarity(),
            IndexSearcher.getDefaultQueryCache(), IndexSearcher.getDefaultQueryCachingPolicy());
    }

    private static ContextIndexSearcher newEarlyTerminationContextSearcher(IndexReader reader, int size) {
        return new ContextIndexSearcher(reader, IndexSearcher.getDefaultSimilarity(),
            IndexSearcher.getDefaultQueryCache(), IndexSearcher.getDefaultQueryCachingPolicy()) {

            @Override
            public void search(List<LeafReaderContext> leaves, Weight weight, Collector collector) throws IOException {
                final Collector in = new AssertingEarlyTerminationFilterCollector(collector, size);
                super.search(leaves, weight, in);
            }
        };
    }

    // used to check that numeric long or date sort optimization was run
    private static ContextIndexSearcher newOptimizedContextSearcher(IndexReader reader, int queryType) {
        return new ContextIndexSearcher(reader, IndexSearcher.getDefaultSimilarity(),
            IndexSearcher.getDefaultQueryCache(), IndexSearcher.getDefaultQueryCachingPolicy()) {

            @Override
            public void search(List<LeafReaderContext> leaves, Weight weight, CollectorManager manager,
                    QuerySearchResult result, DocValueFormat[] formats, TotalHits totalHits) throws IOException {
                final Query query = weight.getQuery();
                assertTrue(query instanceof BooleanQuery);
                List<BooleanClause> clauses = ((BooleanQuery) query).clauses();
                assertTrue(clauses.size() == 2);
                assertTrue(clauses.get(0).getOccur() == Occur.FILTER);
                assertTrue(clauses.get(1).getOccur() == Occur.SHOULD);
                if (queryType == 0) {
                    assertTrue (clauses.get(1).getQuery().getClass() ==
                        LongPoint.newDistanceFeatureQuery("random_field", 1, 1, 1).getClass()
                    );
                }
                if (queryType == 1) assertTrue(clauses.get(1).getQuery() instanceof DocValuesFieldExistsQuery);
                super.search(leaves, weight, manager, result, formats, totalHits);
            }

            @Override
            public void search(List<LeafReaderContext> leaves, Weight weight, Collector collector) {
                assert(false);  // should not be there, expected to search with CollectorManager
            }
        };
    }

    private static class AssertingEarlyTerminationFilterCollector extends FilterCollector {
        private final int size;

        AssertingEarlyTerminationFilterCollector(Collector in, int size) {
            super(in);
            this.size = size;
        }

        @Override
        public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
            final LeafCollector in = super.getLeafCollector(context);
            return new FilterLeafCollector(in) {
                int collected;

                @Override
                public void collect(int doc) throws IOException {
                    assert collected <= size : "should not collect more than " + size + " doc per segment, got " + collected;
                    ++ collected;
                    super.collect(doc);
                }
            };
        }
    }
}
