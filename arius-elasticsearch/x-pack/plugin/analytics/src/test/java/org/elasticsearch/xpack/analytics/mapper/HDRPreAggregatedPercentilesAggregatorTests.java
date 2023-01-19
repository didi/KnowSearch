/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.analytics.mapper;

import org.HdrHistogram.DoubleHistogram;
import org.HdrHistogram.DoubleHistogramIterationValue;
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.search.DocValuesFieldExistsQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.elasticsearch.common.CheckedConsumer;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorTestCase;
import org.elasticsearch.search.aggregations.metrics.InternalHDRPercentiles;
import org.elasticsearch.search.aggregations.metrics.PercentilesAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.PercentilesMethod;
import org.elasticsearch.search.aggregations.support.AggregationInspectionHelper;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;

import static java.util.Collections.singleton;

public class HDRPreAggregatedPercentilesAggregatorTests extends AggregatorTestCase {

   private BinaryDocValuesField getDocValue(String fieldName, double[] values) throws IOException {
       DoubleHistogram histogram = new DoubleHistogram(3);//default
       for (double value : values) {
           histogram.recordValue(value);
       }
       BytesStreamOutput streamOutput = new BytesStreamOutput();
       DoubleHistogram.RecordedValues recordedValues = histogram.recordedValues();
       Iterator<DoubleHistogramIterationValue> iterator = recordedValues.iterator();
       while (iterator.hasNext()) {

           DoubleHistogramIterationValue value = iterator.next();
           long count = value.getCountAtValueIteratedTo();
           if (count != 0) {
               streamOutput.writeVInt(Math.toIntExact(count));
               double d = value.getValueIteratedTo();
               streamOutput.writeDouble(d);
           }

       }
       return new BinaryDocValuesField(fieldName, streamOutput.bytes().toBytesRef());
   }

    public void testNoMatchingField() throws IOException {
        testCase(new MatchAllDocsQuery(), iw -> {
            iw.addDocument(singleton(getDocValue("wrong_number", new double[]{7, 1})));
        }, hdr -> {
            //assertEquals(0L, hdr.state.getTotalCount());
            assertFalse(AggregationInspectionHelper.hasValue(hdr));
        });
    }

    public void testEmptyField() throws IOException {
        testCase(new MatchAllDocsQuery(), iw -> {
            iw.addDocument(singleton(getDocValue("number", new double[0])));
        }, hdr -> {
            assertFalse(AggregationInspectionHelper.hasValue(hdr));
        });
    }

    public void testSomeMatchesBinaryDocValues() throws IOException {
        testCase(new DocValuesFieldExistsQuery("number"), iw -> {
            iw.addDocument(singleton(getDocValue("number", new double[]{60, 40, 20, 10})));
        }, hdr -> {
            //assertEquals(4L, hdr.state.getTotalCount());
            double approximation = 0.05d;
            assertEquals(10.0d, hdr.percentile(25), approximation);
            assertEquals(20.0d, hdr.percentile(50), approximation);
            assertEquals(40.0d, hdr.percentile(75), approximation);
            assertEquals(60.0d, hdr.percentile(99), approximation);
            assertTrue(AggregationInspectionHelper.hasValue(hdr));
        });
    }

    public void testSomeMatchesMultiBinaryDocValues() throws IOException {
        testCase(new DocValuesFieldExistsQuery("number"), iw -> {
            iw.addDocument(singleton(getDocValue("number", new double[]{60, 40, 20, 10})));
            iw.addDocument(singleton(getDocValue("number", new double[]{60, 40, 20, 10})));
            iw.addDocument(singleton(getDocValue("number", new double[]{60, 40, 20, 10})));
            iw.addDocument(singleton(getDocValue("number", new double[]{60, 40, 20, 10})));
        }, hdr -> {
            //assertEquals(16L, hdr.state.getTotalCount());
            double approximation = 0.05d;
            assertEquals(10.0d, hdr.percentile(25), approximation);
            assertEquals(20.0d, hdr.percentile(50), approximation);
            assertEquals(40.0d, hdr.percentile(75), approximation);
            assertEquals(60.0d, hdr.percentile(99), approximation);
            assertTrue(AggregationInspectionHelper.hasValue(hdr));
        });
    }

    private void testCase(Query query, CheckedConsumer<RandomIndexWriter, IOException> buildIndex,
                          Consumer<InternalHDRPercentiles> verify) throws IOException {
        try (Directory directory = newDirectory()) {
            try (RandomIndexWriter indexWriter = new RandomIndexWriter(random(), directory)) {
                buildIndex.accept(indexWriter);
            }

            try (IndexReader indexReader = DirectoryReader.open(directory)) {
                IndexSearcher indexSearcher = newSearcher(indexReader, true, true);

                PercentilesAggregationBuilder builder =
                        new PercentilesAggregationBuilder("test").field("number").method(PercentilesMethod.HDR);

                MappedFieldType fieldType = new HistogramFieldMapper.Builder("number").fieldType();
                fieldType.setName("number");
                Aggregator aggregator = createAggregator(builder, indexSearcher, fieldType);
                aggregator.preCollection();
                indexSearcher.search(query, aggregator);
                aggregator.postCollection();
                verify.accept((InternalHDRPercentiles) aggregator.buildAggregation(0L));

            }
        }
    }
}
