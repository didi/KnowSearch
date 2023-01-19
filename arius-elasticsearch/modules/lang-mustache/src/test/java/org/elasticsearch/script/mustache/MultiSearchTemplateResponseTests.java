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
package org.elasticsearch.script.mustache;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.search.internal.InternalSearchResponse;
import org.elasticsearch.test.AbstractXContentTestCase;

import java.io.IOException;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class MultiSearchTemplateResponseTests extends AbstractXContentTestCase<MultiSearchTemplateResponse> {

    @Override
    protected MultiSearchTemplateResponse createTestInstance() {
        int numItems = randomIntBetween(0, 128);
        long overallTookInMillis = randomNonNegativeLong();
        MultiSearchTemplateResponse.Item[] items = new MultiSearchTemplateResponse.Item[numItems];
        for (int i = 0; i < numItems; i++) {
            // Creating a minimal response is OK, because SearchResponse self
            // is tested elsewhere.
            long tookInMillis = randomNonNegativeLong();
            int totalShards = randomIntBetween(1, Integer.MAX_VALUE);
            int successfulShards = randomIntBetween(0, totalShards);
            int skippedShards = totalShards - successfulShards;
            InternalSearchResponse internalSearchResponse = InternalSearchResponse.empty();
            SearchResponse.Clusters clusters = randomClusters();
            SearchTemplateResponse searchTemplateResponse = new SearchTemplateResponse();
            SearchResponse searchResponse = new SearchResponse(internalSearchResponse, null, totalShards,
                    successfulShards, skippedShards, tookInMillis, ShardSearchFailure.EMPTY_ARRAY, clusters);
            searchTemplateResponse.setResponse(searchResponse);
            items[i] = new MultiSearchTemplateResponse.Item(searchTemplateResponse, null);
        }
        return new MultiSearchTemplateResponse(items, overallTookInMillis);
    }

    private static SearchResponse.Clusters randomClusters() {
        int totalClusters = randomIntBetween(0, 10);
        int successfulClusters = randomIntBetween(0, totalClusters);
        int skippedClusters = totalClusters - successfulClusters;
        return new SearchResponse.Clusters(totalClusters, successfulClusters, skippedClusters);
    }

    private static  MultiSearchTemplateResponse createTestInstanceWithFailures() {
        int numItems = randomIntBetween(0, 128);
        long overallTookInMillis = randomNonNegativeLong();
        MultiSearchTemplateResponse.Item[] items = new MultiSearchTemplateResponse.Item[numItems];
        for (int i = 0; i < numItems; i++) {
            if (randomBoolean()) {
                // Creating a minimal response is OK, because SearchResponse is tested elsewhere.
                long tookInMillis = randomNonNegativeLong();
                int totalShards = randomIntBetween(1, Integer.MAX_VALUE);
                int successfulShards = randomIntBetween(0, totalShards);
                int skippedShards = totalShards - successfulShards;
                InternalSearchResponse internalSearchResponse = InternalSearchResponse.empty();
                SearchResponse.Clusters clusters = randomClusters();
                SearchTemplateResponse searchTemplateResponse = new SearchTemplateResponse();
                SearchResponse searchResponse = new SearchResponse(internalSearchResponse, null, totalShards,
                        successfulShards, skippedShards, tookInMillis, ShardSearchFailure.EMPTY_ARRAY, clusters);
                searchTemplateResponse.setResponse(searchResponse);
                items[i] = new MultiSearchTemplateResponse.Item(searchTemplateResponse, null);
            } else {
                items[i] = new MultiSearchTemplateResponse.Item(null, new ElasticsearchException("an error"));
            }
        }
        return new MultiSearchTemplateResponse(items, overallTookInMillis);
    }

    @Override
    protected MultiSearchTemplateResponse doParseInstance(XContentParser parser) throws IOException {
        return MultiSearchTemplateResponse.fromXContext(parser);
    }

    @Override
    protected boolean supportsUnknownFields() {
        return true;
    }

    protected Predicate<String> getRandomFieldsExcludeFilterWhenResultHasErrors() {
        return field -> field.startsWith("responses");
    }    

    @Override
    protected void assertEqualInstances(MultiSearchTemplateResponse expectedInstance, MultiSearchTemplateResponse newInstance) {
        assertThat(newInstance.getTook(), equalTo(expectedInstance.getTook()));
        assertThat(newInstance.getResponses().length, equalTo(expectedInstance.getResponses().length));
        for (int i = 0; i < expectedInstance.getResponses().length; i++) {
            MultiSearchTemplateResponse.Item expectedItem = expectedInstance.getResponses()[i];
            MultiSearchTemplateResponse.Item actualItem = newInstance.getResponses()[i];
            if (expectedItem.isFailure()) {
                assertThat(actualItem.getResponse(), nullValue());
                assertThat(actualItem.getFailureMessage(), containsString(expectedItem.getFailureMessage()));
            } else {
                assertThat(actualItem.getResponse().toString(), equalTo(expectedItem.getResponse().toString()));
                assertThat(actualItem.getFailure(), nullValue());
            }
        }
    }
    
    /**
     * Test parsing {@link MultiSearchTemplateResponse} with inner failures as they don't support asserting on xcontent equivalence, given
     * exceptions are not parsed back as the same original class. We run the usual {@link AbstractXContentTestCase#testFromXContent()}
     * without failures, and this other test with failures where we disable asserting on xcontent equivalence at the end.
     */
    public void testFromXContentWithFailures() throws IOException {
        Supplier<MultiSearchTemplateResponse> instanceSupplier = MultiSearchTemplateResponseTests::createTestInstanceWithFailures;
        //with random fields insertion in the inner exceptions, some random stuff may be parsed back as metadata,
        //but that does not bother our assertions, as we only want to test that we don't break.
        boolean supportsUnknownFields = true;
        //exceptions are not of the same type whenever parsed back
        boolean assertToXContentEquivalence = false;
        AbstractXContentTestCase.testFromXContent(NUMBER_OF_TEST_RUNS, instanceSupplier, supportsUnknownFields, Strings.EMPTY_ARRAY,
                getRandomFieldsExcludeFilterWhenResultHasErrors(), this::createParser, this::doParseInstance,
                this::assertEqualInstances, assertToXContentEquivalence, ToXContent.EMPTY_PARAMS);
    }
}
