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

package org.elasticsearch.client;

import com.carrotsearch.randomizedtesting.generators.RandomPicks;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.rest.action.document.RestBulkAction;
import org.elasticsearch.search.SearchHit;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.fieldFromSource;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.hasId;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.hasIndex;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.hasProperty;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.hasType;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

public class BulkProcessorIT extends ESRestHighLevelClientTestCase {

    private static BulkProcessor.Builder initBulkProcessorBuilder(BulkProcessor.Listener listener) {
        return BulkProcessor.builder(
                (request, bulkListener) -> highLevelClient().bulkAsync(request, RequestOptions.DEFAULT,
                       bulkListener), listener);
    }

    private static BulkProcessor.Builder initBulkProcessorBuilderUsingTypes(BulkProcessor.Listener listener) {
        return BulkProcessor.builder(
                (request, bulkListener) -> highLevelClient().bulkAsync(request, expectWarnings(RestBulkAction.TYPES_DEPRECATION_MESSAGE),
                       bulkListener), listener);
    }

    public void testThatBulkProcessorCountIsCorrect() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        BulkProcessorTestListener listener = new BulkProcessorTestListener(latch);

        int numDocs = randomIntBetween(10, 100);
        try (BulkProcessor processor = initBulkProcessorBuilder(listener)
                //let's make sure that the bulk action limit trips, one single execution will index all the documents
                .setConcurrentRequests(randomIntBetween(0, 1)).setBulkActions(numDocs)
                .setFlushInterval(TimeValue.timeValueHours(24)).setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
                .build()) {

            MultiGetRequest multiGetRequest = indexDocs(processor, numDocs);

            latch.await();

            assertThat(listener.beforeCounts.get(), equalTo(1));
            assertThat(listener.afterCounts.get(), equalTo(1));
            assertThat(listener.bulkFailures.size(), equalTo(0));
            assertResponseItems(listener.bulkItems, numDocs);
            assertMultiGetResponse(highLevelClient().mget(multiGetRequest, RequestOptions.DEFAULT), numDocs);
        }
    }

    public void testBulkProcessorFlush() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        BulkProcessorTestListener listener = new BulkProcessorTestListener(latch);

        int numDocs = randomIntBetween(10, 100);

        try (BulkProcessor processor = initBulkProcessorBuilder(listener)
                //let's make sure that this bulk won't be automatically flushed
                .setConcurrentRequests(randomIntBetween(0, 10)).setBulkActions(numDocs + randomIntBetween(1, 100))
                .setFlushInterval(TimeValue.timeValueHours(24)).setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB)).build()) {

            MultiGetRequest multiGetRequest = indexDocs(processor, numDocs);

            assertThat(latch.await(randomInt(500), TimeUnit.MILLISECONDS), equalTo(false));
            //we really need an explicit flush as none of the bulk thresholds was reached
            processor.flush();
            latch.await();

            assertThat(listener.beforeCounts.get(), equalTo(1));
            assertThat(listener.afterCounts.get(), equalTo(1));
            assertThat(listener.bulkFailures.size(), equalTo(0));
            assertResponseItems(listener.bulkItems, numDocs);
            assertMultiGetResponse(highLevelClient().mget(multiGetRequest, RequestOptions.DEFAULT), numDocs);
        }
    }

    public void testBulkProcessorConcurrentRequests() throws Exception {
        int bulkActions = randomIntBetween(10, 100);
        int numDocs = randomIntBetween(bulkActions, bulkActions + 100);
        int concurrentRequests = randomIntBetween(0, 7);

        int expectedBulkActions = numDocs / bulkActions;

        final CountDownLatch latch = new CountDownLatch(expectedBulkActions);
        int totalExpectedBulkActions = numDocs % bulkActions == 0 ? expectedBulkActions : expectedBulkActions + 1;
        final CountDownLatch closeLatch = new CountDownLatch(totalExpectedBulkActions);

        BulkProcessorTestListener listener = new BulkProcessorTestListener(latch, closeLatch);

        MultiGetRequest multiGetRequest;

        try (BulkProcessor processor = initBulkProcessorBuilder(listener)
                .setConcurrentRequests(concurrentRequests).setBulkActions(bulkActions)
                //set interval and size to high values
                .setFlushInterval(TimeValue.timeValueHours(24)).setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB)).build()) {

            multiGetRequest = indexDocs(processor, numDocs);

            latch.await();

            assertThat(listener.beforeCounts.get(), equalTo(expectedBulkActions));
            assertThat(listener.afterCounts.get(), equalTo(expectedBulkActions));
            assertThat(listener.bulkFailures.size(), equalTo(0));
            assertThat(listener.bulkItems.size(), equalTo(numDocs - numDocs % bulkActions));
        }

        closeLatch.await();

        assertThat(listener.beforeCounts.get(), equalTo(totalExpectedBulkActions));
        assertThat(listener.afterCounts.get(), equalTo(totalExpectedBulkActions));
        assertThat(listener.bulkFailures.size(), equalTo(0));
        assertThat(listener.bulkItems.size(), equalTo(numDocs));

        Set<String> ids = new HashSet<>();
        for (BulkItemResponse bulkItemResponse : listener.bulkItems) {
            assertThat(bulkItemResponse.getFailureMessage(), bulkItemResponse.isFailed(), equalTo(false));
            assertThat(bulkItemResponse.getIndex(), equalTo("test"));
            assertThat(bulkItemResponse.getType(), equalTo("_doc"));
            //with concurrent requests > 1 we can't rely on the order of the bulk requests
            assertThat(Integer.valueOf(bulkItemResponse.getId()), both(greaterThan(0)).and(lessThanOrEqualTo(numDocs)));
            //we do want to check that we don't get duplicate ids back
            assertThat(ids.add(bulkItemResponse.getId()), equalTo(true));
        }

        assertMultiGetResponse(highLevelClient().mget(multiGetRequest, RequestOptions.DEFAULT), numDocs);
    }

    public void testBulkProcessorWaitOnClose() throws Exception {
        BulkProcessorTestListener listener = new BulkProcessorTestListener();

        int numDocs = randomIntBetween(10, 100);
        BulkProcessor processor = initBulkProcessorBuilder(listener)
                //let's make sure that the bulk action limit trips, one single execution will index all the documents
                .setConcurrentRequests(randomIntBetween(0, 1)).setBulkActions(numDocs)
                .setFlushInterval(TimeValue.timeValueHours(24)).setBulkSize(new ByteSizeValue(randomIntBetween(1, 10),
                        RandomPicks.randomFrom(random(), ByteSizeUnit.values())))
                .build();

        MultiGetRequest multiGetRequest = indexDocs(processor, numDocs);
        assertThat(processor.awaitClose(1, TimeUnit.MINUTES), is(true));
        if (randomBoolean()) { // check if we can call it multiple times
            if (randomBoolean()) {
                assertThat(processor.awaitClose(1, TimeUnit.MINUTES), is(true));
            } else {
                processor.close();
            }
        }

        assertThat(listener.beforeCounts.get(), greaterThanOrEqualTo(1));
        assertThat(listener.afterCounts.get(), greaterThanOrEqualTo(1));
        for (Throwable bulkFailure : listener.bulkFailures) {
            logger.error("bulk failure", bulkFailure);
        }
        assertThat(listener.bulkFailures.size(), equalTo(0));
        assertResponseItems(listener.bulkItems, numDocs);
        assertMultiGetResponse(highLevelClient().mget(multiGetRequest, RequestOptions.DEFAULT), numDocs);
    }

    public void testBulkProcessorConcurrentRequestsReadOnlyIndex() throws Exception {
        Request request = new Request("PUT", "/test-ro");
        request.setJsonEntity("{\n" +
                "    \"settings\" : {\n" +
                "        \"index\" : {\n" +
                "            \"blocks.write\" : true\n" +
                "        }\n" +
                "    }\n" +
                "    \n" +
                "}");
        Response response = client().performRequest(request);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));

        int bulkActions = randomIntBetween(10, 100);
        int numDocs = randomIntBetween(bulkActions, bulkActions + 100);
        int concurrentRequests = randomIntBetween(0, 10);

        int expectedBulkActions = numDocs / bulkActions;

        final CountDownLatch latch = new CountDownLatch(expectedBulkActions);
        int totalExpectedBulkActions = numDocs % bulkActions == 0 ? expectedBulkActions : expectedBulkActions + 1;
        final CountDownLatch closeLatch = new CountDownLatch(totalExpectedBulkActions);

        int testDocs = 0;
        int testReadOnlyDocs = 0;
        MultiGetRequest multiGetRequest = new MultiGetRequest();
        BulkProcessorTestListener listener = new BulkProcessorTestListener(latch, closeLatch);

        try (BulkProcessor processor = initBulkProcessorBuilder(listener)
                .setConcurrentRequests(concurrentRequests).setBulkActions(bulkActions)
                //set interval and size to high values
                .setFlushInterval(TimeValue.timeValueHours(24)).setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB)).build()) {

            for (int i = 1; i <= numDocs; i++) {
                if (randomBoolean()) {
                    testDocs++;
                    processor.add(new IndexRequest("test").id(Integer.toString(testDocs))
                            .source(XContentType.JSON, "field", "value"));
                    multiGetRequest.add("test", Integer.toString(testDocs));
                } else {
                    testReadOnlyDocs++;
                    processor.add(new IndexRequest("test-ro").id(Integer.toString(testReadOnlyDocs))
                            .source(XContentType.JSON, "field", "value"));
                }
            }
        }

        closeLatch.await();

        assertThat(listener.beforeCounts.get(), equalTo(totalExpectedBulkActions));
        assertThat(listener.afterCounts.get(), equalTo(totalExpectedBulkActions));
        assertThat(listener.bulkFailures.size(), equalTo(0));
        assertThat(listener.bulkItems.size(), equalTo(testDocs + testReadOnlyDocs));

        Set<String> ids = new HashSet<>();
        Set<String> readOnlyIds = new HashSet<>();
        for (BulkItemResponse bulkItemResponse : listener.bulkItems) {
            assertThat(bulkItemResponse.getIndex(), either(equalTo("test")).or(equalTo("test-ro")));
            assertThat(bulkItemResponse.getType(), equalTo("_doc"));
            if (bulkItemResponse.getIndex().equals("test")) {
                assertThat(bulkItemResponse.isFailed(), equalTo(false));
                //with concurrent requests > 1 we can't rely on the order of the bulk requests
                assertThat(Integer.valueOf(bulkItemResponse.getId()), both(greaterThan(0)).and(lessThanOrEqualTo(testDocs)));
                //we do want to check that we don't get duplicate ids back
                assertThat(ids.add(bulkItemResponse.getId()), equalTo(true));
            } else {
                assertThat(bulkItemResponse.isFailed(), equalTo(true));
                //with concurrent requests > 1 we can't rely on the order of the bulk requests
                assertThat(Integer.valueOf(bulkItemResponse.getId()), both(greaterThan(0)).and(lessThanOrEqualTo(testReadOnlyDocs)));
                //we do want to check that we don't get duplicate ids back
                assertThat(readOnlyIds.add(bulkItemResponse.getId()), equalTo(true));
            }
        }

        assertMultiGetResponse(highLevelClient().mget(multiGetRequest, RequestOptions.DEFAULT), testDocs);
    }

    public void testGlobalParametersAndSingleRequest() throws Exception {
        createIndexWithMultipleShards("test");

        final CountDownLatch latch = new CountDownLatch(1);
        BulkProcessorTestListener listener = new BulkProcessorTestListener(latch);
        createFieldAddingPipleine("pipeline_id", "fieldNameXYZ", "valueXYZ");

        // tag::bulk-processor-mix-parameters
        try (BulkProcessor processor = initBulkProcessorBuilder(listener)
                .setGlobalIndex("tweets")
                .setGlobalType("_doc")
                .setGlobalRouting("routing")
                .setGlobalPipeline("pipeline_id")
                .build()) {


            processor.add(new IndexRequest() // <1>
                .source(XContentType.JSON, "user", "some user"));
            processor.add(new IndexRequest("blogs").id("1") // <2>
                .source(XContentType.JSON, "title", "some title"));
        }
        // end::bulk-processor-mix-parameters
        latch.await();

        Iterable<SearchHit> hits = searchAll(new SearchRequest("tweets").routing("routing"));
        assertThat(hits, everyItem(hasProperty(fieldFromSource("user"), equalTo("some user"))));
        assertThat(hits, everyItem(hasProperty(fieldFromSource("fieldNameXYZ"), equalTo("valueXYZ"))));


        Iterable<SearchHit> blogs = searchAll(new SearchRequest("blogs").routing("routing"));
        assertThat(blogs, everyItem(hasProperty(fieldFromSource("title"), equalTo("some title"))));
        assertThat(blogs, everyItem(hasProperty(fieldFromSource("fieldNameXYZ"), equalTo("valueXYZ"))));
    }

    public void testGlobalParametersAndBulkProcessor() throws Exception {
        createIndexWithMultipleShards("test");

        createFieldAddingPipleine("pipeline_id", "fieldNameXYZ", "valueXYZ");
        final String customType = "testType";
        final String ignoredType = "ignoredType";

        int numDocs = randomIntBetween(10, 10);
        {
            final CountDownLatch latch = new CountDownLatch(1);
            BulkProcessorTestListener listener = new BulkProcessorTestListener(latch);
            //Check that untyped document additions inherit the global type
            String globalType = customType;
            String localType = null;
            try (BulkProcessor processor = initBulkProcessorBuilderUsingTypes(listener)
                    //let's make sure that the bulk action limit trips, one single execution will index all the documents
                    .setConcurrentRequests(randomIntBetween(0, 1)).setBulkActions(numDocs)
                    .setFlushInterval(TimeValue.timeValueHours(24)).setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
                    .setGlobalIndex("test")
                    .setGlobalType(globalType)
                    .setGlobalRouting("routing")
                    .setGlobalPipeline("pipeline_id")
                    .build()) {

                indexDocs(processor, numDocs, null, localType, "test", globalType, "pipeline_id");
                latch.await();

                assertThat(listener.beforeCounts.get(), equalTo(1));
                assertThat(listener.afterCounts.get(), equalTo(1));
                assertThat(listener.bulkFailures.size(), equalTo(0));
                assertResponseItems(listener.bulkItems, numDocs, globalType);

                Iterable<SearchHit> hits = searchAll(new SearchRequest("test").routing("routing"));

                assertThat(hits, everyItem(hasProperty(fieldFromSource("fieldNameXYZ"), equalTo("valueXYZ"))));
                assertThat(hits, everyItem(Matchers.allOf(hasIndex("test"), hasType(globalType))));
                assertThat(hits, containsInAnyOrder(expectedIds(numDocs)));
            }

        }
        {
            //Check that typed document additions don't inherit the global type
            String globalType = ignoredType;
            String localType = customType;
            final CountDownLatch latch = new CountDownLatch(1);
            BulkProcessorTestListener listener = new BulkProcessorTestListener(latch);
            try (BulkProcessor processor = initBulkProcessorBuilderUsingTypes(listener)
                    //let's make sure that the bulk action limit trips, one single execution will index all the documents
                    .setConcurrentRequests(randomIntBetween(0, 1)).setBulkActions(numDocs)
                    .setFlushInterval(TimeValue.timeValueHours(24)).setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
                    .setGlobalIndex("test")
                    .setGlobalType(globalType)
                    .setGlobalRouting("routing")
                    .setGlobalPipeline("pipeline_id")
                    .build()) {
                indexDocs(processor, numDocs, null, localType, "test", globalType, "pipeline_id");
                latch.await();

                assertThat(listener.beforeCounts.get(), equalTo(1));
                assertThat(listener.afterCounts.get(), equalTo(1));
                assertThat(listener.bulkFailures.size(), equalTo(0));
                assertResponseItems(listener.bulkItems, numDocs, localType);

                Iterable<SearchHit> hits = searchAll(new SearchRequest("test").routing("routing"));

                assertThat(hits, everyItem(hasProperty(fieldFromSource("fieldNameXYZ"), equalTo("valueXYZ"))));
                assertThat(hits, everyItem(Matchers.allOf(hasIndex("test"), hasType(localType))));
                assertThat(hits, containsInAnyOrder(expectedIds(numDocs)));
            }
        }
        {
            //Check that untyped document additions and untyped global inherit the established custom type
            // (the custom document type introduced to the mapping by the earlier code in this test)
            String globalType = null;
            String localType = null;
            final CountDownLatch latch = new CountDownLatch(1);
            BulkProcessorTestListener listener = new BulkProcessorTestListener(latch);
            try (BulkProcessor processor = initBulkProcessorBuilder(listener)
                    //let's make sure that the bulk action limit trips, one single execution will index all the documents
                    .setConcurrentRequests(randomIntBetween(0, 1)).setBulkActions(numDocs)
                    .setFlushInterval(TimeValue.timeValueHours(24)).setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
                    .setGlobalIndex("test")
                    .setGlobalType(globalType)
                    .setGlobalRouting("routing")
                    .setGlobalPipeline("pipeline_id")
                    .build()) {
                indexDocs(processor, numDocs, null, localType, "test", globalType, "pipeline_id");
                latch.await();

                assertThat(listener.beforeCounts.get(), equalTo(1));
                assertThat(listener.afterCounts.get(), equalTo(1));
                assertThat(listener.bulkFailures.size(), equalTo(0));
                assertResponseItems(listener.bulkItems, numDocs, MapperService.SINGLE_MAPPING_NAME);

                Iterable<SearchHit> hits = searchAll(new SearchRequest("test").routing("routing"));

                assertThat(hits, everyItem(hasProperty(fieldFromSource("fieldNameXYZ"), equalTo("valueXYZ"))));
                assertThat(hits, everyItem(Matchers.allOf(hasIndex("test"), hasType(customType))));
                assertThat(hits, containsInAnyOrder(expectedIds(numDocs)));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Matcher<SearchHit>[] expectedIds(int numDocs) {
        return IntStream.rangeClosed(1, numDocs)
            .boxed()
            .map(n -> hasId(n.toString()))
            .<Matcher<SearchHit>>toArray(Matcher[]::new);
    }

    private MultiGetRequest indexDocs(BulkProcessor processor, int numDocs, String localIndex, String localType,
                                      String globalIndex, String globalType, String globalPipeline) throws Exception {
        MultiGetRequest multiGetRequest = new MultiGetRequest();
        for (int i = 1; i <= numDocs; i++) {
            if (randomBoolean()) {
                processor.add(new IndexRequest(localIndex, localType, Integer.toString(i))
                    .source(XContentType.JSON, "field", randomRealisticUnicodeOfLengthBetween(1, 30)));
            } else {
                BytesArray data = bytesBulkRequest(localIndex, localType, i);
                processor.add(data, globalIndex, globalType, globalPipeline, XContentType.JSON);

                if (localType != null) {
                    // If the payload contains types, parsing it into a bulk request results in a warning.
                    assertWarnings(RestBulkAction.TYPES_DEPRECATION_MESSAGE);
                }
            }
            multiGetRequest.add(localIndex, Integer.toString(i));
        }
        return multiGetRequest;
    }

    private static BytesArray bytesBulkRequest(String localIndex, String localType, int id) throws IOException {
        XContentBuilder action = jsonBuilder().startObject().startObject("index");

        if (localIndex != null) {
            action.field("_index", localIndex);
        }

        if (localType != null) {
            action.field("_type", localType);
        }

        action.field("_id", Integer.toString(id));
        action.endObject().endObject();

        XContentBuilder source = jsonBuilder()
            .startObject()
                .field("field", randomRealisticUnicodeOfLengthBetween(1, 30))
            .endObject();

        String request = Strings.toString(action) + "\n" + Strings.toString(source) + "\n";
        return new BytesArray(request);
    }

    private MultiGetRequest indexDocs(BulkProcessor processor, int numDocs) throws Exception {
        return indexDocs(processor, numDocs, "test", null, null, null, null);
    }
    
    private static void assertResponseItems(List<BulkItemResponse> bulkItemResponses, int numDocs) {
        assertResponseItems(bulkItemResponses, numDocs, MapperService.SINGLE_MAPPING_NAME);
    }
    
    private static void assertResponseItems(List<BulkItemResponse> bulkItemResponses, int numDocs, String expectedType) {
        assertThat(bulkItemResponses.size(), is(numDocs));
        int i = 1;
        for (BulkItemResponse bulkItemResponse : bulkItemResponses) {
            assertThat(bulkItemResponse.getIndex(), equalTo("test"));
            assertThat(bulkItemResponse.getType(), equalTo(expectedType));
            assertThat(bulkItemResponse.getId(), equalTo(Integer.toString(i++)));
            assertThat("item " + i + " failed with cause: " + bulkItemResponse.getFailureMessage(),
                    bulkItemResponse.isFailed(), equalTo(false));
        }
    }

    private static void assertMultiGetResponse(MultiGetResponse multiGetResponse, int numDocs) {
        assertThat(multiGetResponse.getResponses().length, equalTo(numDocs));
        int i = 1;
        for (MultiGetItemResponse multiGetItemResponse : multiGetResponse) {
            assertThat(multiGetItemResponse.getIndex(), equalTo("test"));
            assertThat(multiGetItemResponse.getType(), equalTo("_doc"));
            assertThat(multiGetItemResponse.getId(), equalTo(Integer.toString(i++)));
        }
    }

    private static class BulkProcessorTestListener implements BulkProcessor.Listener {

        private final CountDownLatch[] latches;
        private final AtomicInteger beforeCounts = new AtomicInteger();
        private final AtomicInteger afterCounts = new AtomicInteger();
        private final List<BulkItemResponse> bulkItems = new CopyOnWriteArrayList<>();
        private final List<Throwable> bulkFailures = new CopyOnWriteArrayList<>();

        private BulkProcessorTestListener(CountDownLatch... latches) {
            this.latches = latches;
        }

        @Override
        public void beforeBulk(long executionId, BulkRequest request) {
            beforeCounts.incrementAndGet();
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
            bulkItems.addAll(Arrays.asList(response.getItems()));
            afterCounts.incrementAndGet();
            for (CountDownLatch latch : latches) {
                latch.countDown();
            }
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
            bulkFailures.add(failure);
            afterCounts.incrementAndGet();
            for (CountDownLatch latch : latches) {
                latch.countDown();
            }
        }
    }


}
