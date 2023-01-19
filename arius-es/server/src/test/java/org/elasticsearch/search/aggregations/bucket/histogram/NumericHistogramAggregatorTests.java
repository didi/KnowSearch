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

package org.elasticsearch.search.aggregations.bucket.histogram;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.elasticsearch.index.mapper.KeywordFieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.NumberFieldMapper;
import org.elasticsearch.search.aggregations.AggregatorTestCase;
import org.elasticsearch.search.aggregations.support.AggregationInspectionHelper;
import static org.hamcrest.Matchers.containsString;

public class NumericHistogramAggregatorTests extends AggregatorTestCase {

    public void testLongs() throws Exception {
        try (Directory dir = newDirectory();
                RandomIndexWriter w = new RandomIndexWriter(random(), dir)) {
            for (long value : new long[] {7, 3, -10, -6, 5, 50}) {
                Document doc = new Document();
                doc.add(new SortedNumericDocValuesField("field", value));
                w.addDocument(doc);
            }

            HistogramAggregationBuilder aggBuilder = new HistogramAggregationBuilder("my_agg")
                    .field("field")
                    .interval(5);
            MappedFieldType fieldType = new NumberFieldMapper.NumberFieldType(NumberFieldMapper.NumberType.LONG);
            fieldType.setName("field");
            try (IndexReader reader = w.getReader()) {
                IndexSearcher searcher = new IndexSearcher(reader);
                InternalHistogram histogram = search(searcher, new MatchAllDocsQuery(), aggBuilder, fieldType);
                assertEquals(4, histogram.getBuckets().size());
                assertEquals(-10d, histogram.getBuckets().get(0).getKey());
                assertEquals(2, histogram.getBuckets().get(0).getDocCount());
                assertEquals(0d, histogram.getBuckets().get(1).getKey());
                assertEquals(1, histogram.getBuckets().get(1).getDocCount());
                assertEquals(5d, histogram.getBuckets().get(2).getKey());
                assertEquals(2, histogram.getBuckets().get(2).getDocCount());
                assertEquals(50d, histogram.getBuckets().get(3).getKey());
                assertEquals(1, histogram.getBuckets().get(3).getDocCount());
                assertTrue(AggregationInspectionHelper.hasValue(histogram));
            }
        }
    }

    public void testDoubles() throws Exception {
        try (Directory dir = newDirectory();
                RandomIndexWriter w = new RandomIndexWriter(random(), dir)) {
            for (double value : new double[] {9.3, 3.2, -10, -6.5, 5.3, 50.1}) {
                Document doc = new Document();
                doc.add(new SortedNumericDocValuesField("field", NumericUtils.doubleToSortableLong(value)));
                w.addDocument(doc);
            }

            HistogramAggregationBuilder aggBuilder = new HistogramAggregationBuilder("my_agg")
                    .field("field")
                    .interval(5);
            MappedFieldType fieldType = new NumberFieldMapper.NumberFieldType(NumberFieldMapper.NumberType.DOUBLE);
            fieldType.setName("field");
            try (IndexReader reader = w.getReader()) {
                IndexSearcher searcher = new IndexSearcher(reader);
                InternalHistogram histogram = search(searcher, new MatchAllDocsQuery(), aggBuilder, fieldType);
                assertEquals(4, histogram.getBuckets().size());
                assertEquals(-10d, histogram.getBuckets().get(0).getKey());
                assertEquals(2, histogram.getBuckets().get(0).getDocCount());
                assertEquals(0d, histogram.getBuckets().get(1).getKey());
                assertEquals(1, histogram.getBuckets().get(1).getDocCount());
                assertEquals(5d, histogram.getBuckets().get(2).getKey());
                assertEquals(2, histogram.getBuckets().get(2).getDocCount());
                assertEquals(50d, histogram.getBuckets().get(3).getKey());
                assertEquals(1, histogram.getBuckets().get(3).getDocCount());
                assertTrue(AggregationInspectionHelper.hasValue(histogram));
            }
        }
    }

    public void testIrrationalInterval() throws Exception {
        try (Directory dir = newDirectory();
                RandomIndexWriter w = new RandomIndexWriter(random(), dir)) {
            for (long value : new long[] {3, 2, -10, 5, -9}) {
                Document doc = new Document();
                doc.add(new SortedNumericDocValuesField("field", value));
                w.addDocument(doc);
            }

            HistogramAggregationBuilder aggBuilder = new HistogramAggregationBuilder("my_agg")
                    .field("field")
                    .interval(Math.PI);
            MappedFieldType fieldType = new NumberFieldMapper.NumberFieldType(NumberFieldMapper.NumberType.LONG);
            fieldType.setName("field");
            try (IndexReader reader = w.getReader()) {
                IndexSearcher searcher = new IndexSearcher(reader);
                InternalHistogram histogram = search(searcher, new MatchAllDocsQuery(), aggBuilder, fieldType);
                assertEquals(4, histogram.getBuckets().size());
                assertEquals(-4 * Math.PI, histogram.getBuckets().get(0).getKey());
                assertEquals(1, histogram.getBuckets().get(0).getDocCount());
                assertEquals(-3 * Math.PI, histogram.getBuckets().get(1).getKey());
                assertEquals(1, histogram.getBuckets().get(1).getDocCount());
                assertEquals(0d, histogram.getBuckets().get(2).getKey());
                assertEquals(2, histogram.getBuckets().get(2).getDocCount());
                assertEquals(Math.PI, histogram.getBuckets().get(3).getKey());
                assertEquals(1, histogram.getBuckets().get(3).getDocCount());
                assertTrue(AggregationInspectionHelper.hasValue(histogram));
            }
        }
    }

    public void testMinDocCount() throws Exception {
        try (Directory dir = newDirectory();
                RandomIndexWriter w = new RandomIndexWriter(random(), dir)) {
            for (long value : new long[] {7, 3, -10, -6, 5, 50}) {
                Document doc = new Document();
                doc.add(new SortedNumericDocValuesField("field", value));
                w.addDocument(doc);
            }

            HistogramAggregationBuilder aggBuilder = new HistogramAggregationBuilder("my_agg")
                    .field("field")
                    .interval(10)
                    .minDocCount(2);
            MappedFieldType fieldType = new NumberFieldMapper.NumberFieldType(NumberFieldMapper.NumberType.LONG);
            fieldType.setName("field");
            try (IndexReader reader = w.getReader()) {
                IndexSearcher searcher = new IndexSearcher(reader);
                InternalHistogram histogram = searchAndReduce(searcher, new MatchAllDocsQuery(), aggBuilder, fieldType);
                assertEquals(2, histogram.getBuckets().size());
                assertEquals(-10d, histogram.getBuckets().get(0).getKey());
                assertEquals(2, histogram.getBuckets().get(0).getDocCount());
                assertEquals(0d, histogram.getBuckets().get(1).getKey());
                assertEquals(3, histogram.getBuckets().get(1).getDocCount());
                assertTrue(AggregationInspectionHelper.hasValue(histogram));
            }
        }
    }

    public void testMissing() throws Exception {
        try (Directory dir = newDirectory();
                RandomIndexWriter w = new RandomIndexWriter(random(), dir)) {
            for (long value : new long[] {7, 3, -10, -6, 5, 50}) {
                Document doc = new Document();
                doc.add(new SortedNumericDocValuesField("field", value));
                w.addDocument(doc);
                w.addDocument(new Document());
            }

            HistogramAggregationBuilder aggBuilder = new HistogramAggregationBuilder("my_agg")
                    .field("field")
                    .interval(5)
                    .missing(2d);
            MappedFieldType fieldType = new NumberFieldMapper.NumberFieldType(NumberFieldMapper.NumberType.LONG);
            fieldType.setName("field");
            try (IndexReader reader = w.getReader()) {
                IndexSearcher searcher = new IndexSearcher(reader);
                InternalHistogram histogram = search(searcher, new MatchAllDocsQuery(), aggBuilder, fieldType);
                assertEquals(4, histogram.getBuckets().size());
                assertEquals(-10d, histogram.getBuckets().get(0).getKey());
                assertEquals(2, histogram.getBuckets().get(0).getDocCount());
                assertEquals(0d, histogram.getBuckets().get(1).getKey());
                assertEquals(7, histogram.getBuckets().get(1).getDocCount());
                assertEquals(5d, histogram.getBuckets().get(2).getKey());
                assertEquals(2, histogram.getBuckets().get(2).getDocCount());
                assertEquals(50d, histogram.getBuckets().get(3).getKey());
                assertEquals(1, histogram.getBuckets().get(3).getDocCount());
                assertTrue(AggregationInspectionHelper.hasValue(histogram));
            }
        }
    }

    public void testMissingUnmappedField() throws Exception {
        try (Directory dir = newDirectory();
             RandomIndexWriter w = new RandomIndexWriter(random(), dir)) {
            for (int i = 0; i < 7; i ++) {
                Document doc = new Document();
                w.addDocument(doc);
            }

            HistogramAggregationBuilder aggBuilder = new HistogramAggregationBuilder("my_agg")
                .field("field")
                .interval(5)
                .missing(2d);
            MappedFieldType type = null;
            try (IndexReader reader = w.getReader()) {
                IndexSearcher searcher = new IndexSearcher(reader);
                InternalHistogram histogram = search(searcher, new MatchAllDocsQuery(), aggBuilder, type);

                assertEquals(1, histogram.getBuckets().size());

                assertEquals(0d, histogram.getBuckets().get(0).getKey());
                assertEquals(7, histogram.getBuckets().get(0).getDocCount());

                assertTrue(AggregationInspectionHelper.hasValue(histogram));
            }
        }
    }

    public void testMissingUnmappedFieldBadType() throws Exception {
        try (Directory dir = newDirectory();
             RandomIndexWriter w = new RandomIndexWriter(random(), dir)) {
            for (int i = 0; i < 7; i ++) {
                w.addDocument(new Document());
            }

            String missingValue = "🍌🍌🍌";
            HistogramAggregationBuilder aggBuilder = new HistogramAggregationBuilder("my_agg")
                .field("field")
                .interval(5)
                .missing(missingValue);
            MappedFieldType type = null;
            try (IndexReader reader = w.getReader()) {
                IndexSearcher searcher = new IndexSearcher(reader);
                Throwable t = expectThrows(IllegalArgumentException.class, () -> {
                    search(searcher, new MatchAllDocsQuery(), aggBuilder, type);
                });
                // This throws a number format exception (which is a subclass of IllegalArgumentException) and might be ok?
                assertThat(t.getMessage(), containsString(missingValue));
            }
        }
    }

    public void testIncorrectFieldType() throws Exception {
        try (Directory dir = newDirectory();
             RandomIndexWriter w = new RandomIndexWriter(random(), dir)) {
            for (String value : new String[] {"foo", "bar", "baz", "quux"}) {
                Document doc = new Document();
                doc.add(new SortedSetDocValuesField("field", new BytesRef(value)));
                w.addDocument(doc);
            }

            HistogramAggregationBuilder aggBuilder = new HistogramAggregationBuilder("my_agg")
                .field("field")
                .interval(5);
            MappedFieldType fieldType = new KeywordFieldMapper.KeywordFieldType();
            fieldType.setName("field");
            fieldType.setHasDocValues(true);
            try (IndexReader reader = w.getReader()) {
                IndexSearcher searcher = new IndexSearcher(reader);

                expectThrows(IllegalArgumentException.class, () -> {
                    search(searcher, new MatchAllDocsQuery(), aggBuilder, fieldType);
                });
            }
        }

    }

    public void testOffset() throws Exception {
        try (Directory dir = newDirectory();
                RandomIndexWriter w = new RandomIndexWriter(random(), dir)) {
            for (double value : new double[] {9.3, 3.2, -5, -6.5, 5.3}) {
                Document doc = new Document();
                doc.add(new SortedNumericDocValuesField("field", NumericUtils.doubleToSortableLong(value)));
                w.addDocument(doc);
            }

            HistogramAggregationBuilder aggBuilder = new HistogramAggregationBuilder("my_agg")
                    .field("field")
                    .interval(5)
                    .offset(Math.PI);
            MappedFieldType fieldType = new NumberFieldMapper.NumberFieldType(NumberFieldMapper.NumberType.DOUBLE);
            fieldType.setName("field");
            try (IndexReader reader = w.getReader()) {
                IndexSearcher searcher = new IndexSearcher(reader);
                InternalHistogram histogram = search(searcher, new MatchAllDocsQuery(), aggBuilder, fieldType);
                assertEquals(3, histogram.getBuckets().size());
                assertEquals(-10 + Math.PI, histogram.getBuckets().get(0).getKey());
                assertEquals(2, histogram.getBuckets().get(0).getDocCount());
                assertEquals(Math.PI, histogram.getBuckets().get(1).getKey());
                assertEquals(2, histogram.getBuckets().get(1).getDocCount());
                assertEquals(5 + Math.PI, histogram.getBuckets().get(2).getKey());
                assertEquals(1, histogram.getBuckets().get(2).getDocCount());
                assertTrue(AggregationInspectionHelper.hasValue(histogram));
            }
        }
    }

    public void testRandomOffset() throws Exception {
        try (Directory dir = newDirectory();
             RandomIndexWriter w = new RandomIndexWriter(random(), dir)) {
            // Note, these values are carefully chosen to ensure that no matter what offset we pick, no two can end up in the same bucket
            for (double value : new double[] {9.3, 3.2, -5}) {
                Document doc = new Document();
                doc.add(new SortedNumericDocValuesField("field", NumericUtils.doubleToSortableLong(value)));
                w.addDocument(doc);
            }

            final double offset = randomDouble();
            final double interval = 5;
            final double expectedOffset = offset % interval;
            HistogramAggregationBuilder aggBuilder = new HistogramAggregationBuilder("my_agg")
                .field("field")
                .interval(interval)
                .offset(offset);
            MappedFieldType fieldType = new NumberFieldMapper.NumberFieldType(NumberFieldMapper.NumberType.DOUBLE);
            fieldType.setName("field");
            try (IndexReader reader = w.getReader()) {
                IndexSearcher searcher = new IndexSearcher(reader);
                InternalHistogram histogram = search(searcher, new MatchAllDocsQuery(), aggBuilder, fieldType);
                assertEquals(3, histogram.getBuckets().size());

                assertEquals(-10 + expectedOffset, histogram.getBuckets().get(0).getKey());
                assertEquals(1, histogram.getBuckets().get(0).getDocCount());

                assertEquals(expectedOffset, histogram.getBuckets().get(1).getKey());
                assertEquals(1, histogram.getBuckets().get(1).getDocCount());

                assertEquals(5 + expectedOffset, histogram.getBuckets().get(2).getKey());
                assertEquals(1, histogram.getBuckets().get(2).getDocCount());

                assertTrue(AggregationInspectionHelper.hasValue(histogram));
            }
        }
    }

    public void testExtendedBounds() throws Exception {
        try (Directory dir = newDirectory();
                RandomIndexWriter w = new RandomIndexWriter(random(), dir)) {
            for (double value : new double[] {3.2, -5, -4.5, 4.3}) {
                Document doc = new Document();
                doc.add(new SortedNumericDocValuesField("field", NumericUtils.doubleToSortableLong(value)));
                w.addDocument(doc);
            }

            HistogramAggregationBuilder aggBuilder = new HistogramAggregationBuilder("my_agg")
                    .field("field")
                    .interval(5)
                    .extendedBounds(-12, 13);
            MappedFieldType fieldType = new NumberFieldMapper.NumberFieldType(NumberFieldMapper.NumberType.DOUBLE);
            fieldType.setName("field");
            try (IndexReader reader = w.getReader()) {
                IndexSearcher searcher = new IndexSearcher(reader);
                InternalHistogram histogram = searchAndReduce(searcher, new MatchAllDocsQuery(), aggBuilder, fieldType);
                assertEquals(6, histogram.getBuckets().size());
                assertEquals(-15d, histogram.getBuckets().get(0).getKey());
                assertEquals(0, histogram.getBuckets().get(0).getDocCount());
                assertEquals(-10d, histogram.getBuckets().get(1).getKey());
                assertEquals(0, histogram.getBuckets().get(1).getDocCount());
                assertEquals(-5d, histogram.getBuckets().get(2).getKey());
                assertEquals(2, histogram.getBuckets().get(2).getDocCount());
                assertEquals(0d, histogram.getBuckets().get(3).getKey());
                assertEquals(2, histogram.getBuckets().get(3).getDocCount());
                assertEquals(5d, histogram.getBuckets().get(4).getKey());
                assertEquals(0, histogram.getBuckets().get(4).getDocCount());
                assertEquals(10d, histogram.getBuckets().get(5).getKey());
                assertEquals(0, histogram.getBuckets().get(5).getDocCount());
                assertTrue(AggregationInspectionHelper.hasValue(histogram));
            }
        }
    }
}
