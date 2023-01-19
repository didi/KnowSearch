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
package org.elasticsearch.search.aggregations.bucket.filter;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.store.Directory;
import org.elasticsearch.index.mapper.KeywordFieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregatorTestCase;
import org.elasticsearch.search.aggregations.support.AggregationInspectionHelper;
import org.junit.Before;

public class FilterAggregatorTests extends AggregatorTestCase {
    private MappedFieldType fieldType;

    @Before
    public void setUpTest() throws Exception {
        super.setUp();
        fieldType = new KeywordFieldMapper.KeywordFieldType();
        fieldType.setHasDocValues(true);
        fieldType.setIndexOptions(IndexOptions.DOCS);
        fieldType.setName("field");
    }

    public void testEmpty() throws Exception {
        Directory directory = newDirectory();
        RandomIndexWriter indexWriter = new RandomIndexWriter(random(), directory);
        indexWriter.close();
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = newSearcher(indexReader, true, true);
        QueryBuilder filter = QueryBuilders.termQuery("field", randomAlphaOfLength(5));
        FilterAggregationBuilder builder = new FilterAggregationBuilder("test", filter);
        InternalFilter response = search(indexSearcher, new MatchAllDocsQuery(), builder,
                fieldType);
        assertEquals(response.getDocCount(), 0);
        assertFalse(AggregationInspectionHelper.hasValue(response));
        indexReader.close();
        directory.close();
    }

    public void testRandom() throws Exception {
        Directory directory = newDirectory();
        RandomIndexWriter indexWriter = new RandomIndexWriter(random(), directory);
        int numDocs = randomIntBetween(100, 200);
        int maxTerm = randomIntBetween(10, 50);
        int[] expectedBucketCount = new int[maxTerm];
        Document document = new Document();
        for (int i = 0; i < numDocs; i++) {
            if (frequently()) {
                // make sure we have more than one segment to test the merge
                indexWriter.getReader().close();
            }
            int value = randomInt(maxTerm-1);
            expectedBucketCount[value] += 1;
            document.add(new Field("field", Integer.toString(value), fieldType));
            indexWriter.addDocument(document);
            document.clear();
        }
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = newSearcher(indexReader, true, true);
        try {

            int value = randomInt(maxTerm - 1);
            QueryBuilder filter = QueryBuilders.termQuery("field", Integer.toString(value));
            FilterAggregationBuilder builder = new FilterAggregationBuilder("test", filter);

            for (boolean doReduce : new boolean[]{true, false}) {
                final InternalFilter response;
                if (doReduce) {
                    response = searchAndReduce(indexSearcher, new MatchAllDocsQuery(), builder,
                        fieldType);
                } else {
                    response = search(indexSearcher, new MatchAllDocsQuery(), builder, fieldType);
                }
                assertEquals(response.getDocCount(), (long) expectedBucketCount[value]);
                if (expectedBucketCount[value] > 0) {
                    assertTrue(AggregationInspectionHelper.hasValue(response));
                } else {
                    assertFalse(AggregationInspectionHelper.hasValue(response));
                }
            }
        } finally {
            indexReader.close();
            directory.close();
        }

    }
}
