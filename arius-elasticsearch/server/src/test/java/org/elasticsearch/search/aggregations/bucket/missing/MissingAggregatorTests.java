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

package org.elasticsearch.search.aggregations.bucket.missing;

import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.elasticsearch.common.lucene.search.Queries;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.NumberFieldMapper;
import org.elasticsearch.index.mapper.RangeFieldMapper;
import org.elasticsearch.index.mapper.RangeType;
import org.elasticsearch.search.aggregations.AggregatorTestCase;
import org.elasticsearch.search.aggregations.support.AggregationInspectionHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


public class MissingAggregatorTests extends AggregatorTestCase {
    public void testMatchNoDocs() throws IOException {
        int numDocs = randomIntBetween(10, 200);
        testBothCases(numDocs,
            "field",
            Queries.newMatchAllQuery(),
            doc -> doc.add(new SortedNumericDocValuesField("field", randomLong())),
            internalMissing -> {
                assertEquals(internalMissing.getDocCount(), 0);
                assertFalse(AggregationInspectionHelper.hasValue(internalMissing));
            });
    }

    public void testMatchAllDocs() throws IOException {
        int numDocs = randomIntBetween(10, 200);
        testBothCases(numDocs,
            "field",
            Queries.newMatchAllQuery(),
            doc -> doc.add(new SortedNumericDocValuesField("another_field", randomLong())),
            internalMissing -> {
                assertEquals(internalMissing.getDocCount(), numDocs);
                assertTrue(AggregationInspectionHelper.hasValue(internalMissing));
            });
    }

    public void testMatchSparse() throws IOException {
        int numDocs = randomIntBetween(100, 200);
        final AtomicInteger count = new AtomicInteger();
        testBothCases(numDocs,
            "field",
            Queries.newMatchAllQuery(),
            doc -> {
                if (randomBoolean()) {
                    doc.add(new SortedNumericDocValuesField("another_field", randomLong()));
                    count.incrementAndGet();
                } else {
                    doc.add(new SortedNumericDocValuesField("field", randomLong()));
                }
            },
            internalMissing -> {
                assertEquals(internalMissing.getDocCount(), count.get());
                count.set(0);
                assertTrue(AggregationInspectionHelper.hasValue(internalMissing));
            });
    }

    public void testMatchSparseRangeField() throws IOException {
        int numDocs = randomIntBetween(100, 200);
        final AtomicInteger count = new AtomicInteger();
        final String fieldName = "field";
        RangeType rangeType = RangeType.DOUBLE;
        final BinaryDocValuesField field = new BinaryDocValuesField(fieldName, rangeType.encodeRanges(Collections.singleton(
            new RangeFieldMapper.Range(rangeType, 1.0D, 5.0D, true, true))));
        MappedFieldType fieldType = new RangeFieldMapper.Builder(fieldName, rangeType).fieldType();
        fieldType.setName(fieldName);
        testBothCases(numDocs,
            fieldName,
            Queries.newMatchAllQuery(),
            doc -> {
                if (randomBoolean()) {
                    doc.add(new SortedNumericDocValuesField("another_field", randomLong()));
                    count.incrementAndGet();
                } else {
                    doc.add(field);
                }
            },
            internalMissing -> {
                assertEquals(internalMissing.getDocCount(), count.get());
                count.set(0);
                assertTrue(AggregationInspectionHelper.hasValue(internalMissing));
            }, fieldType);
    }


    public void testMissingField() throws IOException {
        int numDocs = randomIntBetween(10, 20);
        testBothCases(numDocs,
            "unknown_field",
            Queries.newMatchAllQuery(),
            doc -> {
                doc.add(new SortedNumericDocValuesField("field", randomLong()));
            },
            internalMissing -> {
                assertEquals(internalMissing.getDocCount(), numDocs);
                assertTrue(AggregationInspectionHelper.hasValue(internalMissing));
            });
    }

    private void testBothCases(int numDocs,
                               String fieldName,
                               Query query,
                               Consumer<Document> consumer,
                               Consumer<InternalMissing> verify) throws IOException {
        NumberFieldMapper.Builder mapperBuilder = new NumberFieldMapper.Builder("_name",
            NumberFieldMapper.NumberType.LONG);
        final MappedFieldType fieldType = mapperBuilder.fieldType();
        fieldType.setHasDocValues(true);
        fieldType.setName(fieldName);
        testBothCases(numDocs, fieldName, query, consumer, verify, fieldType);
    }

    private void testBothCases(int numDocs,
                               String fieldName,
                               Query query,
                               Consumer<Document> consumer,
                               Consumer<InternalMissing> verify,
                               MappedFieldType fieldType) throws IOException {
        executeTestCase(numDocs, fieldName, query, consumer, verify, false, fieldType);
        executeTestCase(numDocs, fieldName, query, consumer, verify, true, fieldType);

    }

    private void executeTestCase(int numDocs,
                                 String fieldName,
                                 Query query,
                                 Consumer<Document> consumer,
                                 Consumer<InternalMissing> verify,
                                 boolean reduced,
                                 MappedFieldType fieldType) throws IOException {
        try (Directory directory = newDirectory()) {
            try (RandomIndexWriter indexWriter = new RandomIndexWriter(random(), directory)) {
                Document document = new Document();
                for (int i = 0; i < numDocs; i++) {
                    if (frequently()) {
                        indexWriter.commit();
                    }
                    consumer.accept(document);
                    indexWriter.addDocument(document);
                    document.clear();
                }
            }

            try (IndexReader indexReader = DirectoryReader.open(directory)) {
                IndexSearcher indexSearcher =
                    newSearcher(indexReader, true, true);
                MissingAggregationBuilder builder = new MissingAggregationBuilder("_name", null);
                builder.field(fieldName);

                InternalMissing missing;
                if (reduced) {
                    missing = searchAndReduce(indexSearcher, query, builder, fieldType);
                } else {
                    missing = search(indexSearcher, query, builder, fieldType);
                }
                verify.accept(missing);
            }
        }
    }
}
