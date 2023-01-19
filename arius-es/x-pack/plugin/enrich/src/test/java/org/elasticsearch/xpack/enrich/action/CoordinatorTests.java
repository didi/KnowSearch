/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.enrich.action;

import org.apache.logging.log4j.util.BiConsumer;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.action.support.single.shard.SingleShardRequest;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.InternalAggregations;
import org.elasticsearch.search.internal.InternalSearchResponse;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.threadpool.ThreadPool;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.elasticsearch.xpack.enrich.action.EnrichCoordinatorProxyAction.Coordinator;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public class CoordinatorTests extends ESTestCase {

    public void testCoordinateLookups() {
        MockLookupFunction lookupFunction = new MockLookupFunction();
        Coordinator coordinator = new Coordinator(lookupFunction, 5, 1, 100);

        List<ActionListener<SearchResponse>> searchActionListeners = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            SearchRequest searchRequest = new SearchRequest("my-index");
            searchRequest.source().query(new MatchQueryBuilder("my_field", String.valueOf(i)));
            @SuppressWarnings("unchecked")
            ActionListener<SearchResponse> actionListener = Mockito.mock(ActionListener.class);
            searchActionListeners.add(actionListener);
            coordinator.queue.add(new Coordinator.Slot(searchRequest, actionListener));
        }

        SearchRequest searchRequest = new SearchRequest("my-index");
        searchRequest.source().query(new MatchQueryBuilder("my_field", String.valueOf(10)));
        @SuppressWarnings("unchecked")
        ActionListener<SearchResponse> actionListener = Mockito.mock(ActionListener.class);
        searchActionListeners.add(actionListener);
        coordinator.schedule(searchRequest, actionListener);

        // First batch of search requests have been sent off:
        // (However still 5 should remain in the queue)
        assertThat(coordinator.queue.size(), equalTo(5));
        assertThat(coordinator.remoteRequestsCurrent.get(), equalTo(1));
        assertThat(lookupFunction.capturedRequests.size(), equalTo(1));
        assertThat(lookupFunction.capturedRequests.get(0).requests().size(), equalTo(5));

        // Nothing should happen now, because there is an outstanding request and max number of requests has been set to 1:
        coordinator.coordinateLookups();
        assertThat(coordinator.queue.size(), equalTo(5));
        assertThat(coordinator.remoteRequestsCurrent.get(), equalTo(1));
        assertThat(lookupFunction.capturedRequests.size(), equalTo(1));

        SearchResponse emptyResponse = emptySearchResponse();
        // Replying a response and that should trigger another coordination round
        MultiSearchResponse.Item[] responseItems = new MultiSearchResponse.Item[5];
        for (int i = 0; i < 5; i++) {
            responseItems[i] = new MultiSearchResponse.Item(emptyResponse, null);
        }
        lookupFunction.capturedConsumers.get(0).accept(new MultiSearchResponse(responseItems, 1L), null);
        assertThat(coordinator.queue.size(), equalTo(0));
        assertThat(coordinator.remoteRequestsCurrent.get(), equalTo(1));
        assertThat(lookupFunction.capturedRequests.size(), equalTo(2));

        // Replying last response, resulting in an empty queue and no outstanding requests.
        responseItems = new MultiSearchResponse.Item[5];
        for (int i = 0; i < 5; i++) {
            responseItems[i] = new MultiSearchResponse.Item(emptyResponse, null);
        }
        lookupFunction.capturedConsumers.get(1).accept(new MultiSearchResponse(responseItems, 1L), null);
        assertThat(coordinator.queue.size(), equalTo(0));
        assertThat(coordinator.remoteRequestsCurrent.get(), equalTo(0));
        assertThat(lookupFunction.capturedRequests.size(), equalTo(2));

        // All individual action listeners for the search requests should have been invoked:
        for (ActionListener<SearchResponse> searchActionListener : searchActionListeners) {
            Mockito.verify(searchActionListener).onResponse(Mockito.eq(emptyResponse));
        }
    }

    public void testCoordinateLookupsMultiSearchError() {
        MockLookupFunction lookupFunction = new MockLookupFunction();
        Coordinator coordinator = new Coordinator(lookupFunction, 5, 1, 100);

        List<ActionListener<SearchResponse>> searchActionListeners = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            SearchRequest searchRequest = new SearchRequest("my-index");
            searchRequest.source().query(new MatchQueryBuilder("my_field", String.valueOf(i)));
            @SuppressWarnings("unchecked")
            ActionListener<SearchResponse> actionListener = Mockito.mock(ActionListener.class);
            searchActionListeners.add(actionListener);
            coordinator.queue.add(new Coordinator.Slot(searchRequest, actionListener));
        }

        SearchRequest searchRequest = new SearchRequest("my-index");
        searchRequest.source().query(new MatchQueryBuilder("my_field", String.valueOf(5)));
        @SuppressWarnings("unchecked")
        ActionListener<SearchResponse> actionListener = Mockito.mock(ActionListener.class);
        searchActionListeners.add(actionListener);
        coordinator.schedule(searchRequest, actionListener);

        // First batch of search requests have been sent off:
        // (However still 5 should remain in the queue)
        assertThat(coordinator.queue.size(), equalTo(0));
        assertThat(coordinator.remoteRequestsCurrent.get(), equalTo(1));
        assertThat(lookupFunction.capturedRequests.size(), equalTo(1));
        assertThat(lookupFunction.capturedRequests.get(0).requests().size(), equalTo(5));

        RuntimeException e = new RuntimeException();
        lookupFunction.capturedConsumers.get(0).accept(null, e);
        assertThat(coordinator.queue.size(), equalTo(0));
        assertThat(coordinator.remoteRequestsCurrent.get(), equalTo(0));
        assertThat(lookupFunction.capturedRequests.size(), equalTo(1));

        // All individual action listeners for the search requests should have been invoked:
        for (ActionListener<SearchResponse> searchActionListener : searchActionListeners) {
            Mockito.verify(searchActionListener).onFailure(Mockito.eq(e));
        }
    }

    public void testCoordinateLookupsMultiSearchItemError() {
        MockLookupFunction lookupFunction = new MockLookupFunction();
        Coordinator coordinator = new Coordinator(lookupFunction, 5, 1, 100);

        List<ActionListener<SearchResponse>> searchActionListeners = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            SearchRequest searchRequest = new SearchRequest("my-index");
            searchRequest.source().query(new MatchQueryBuilder("my_field", String.valueOf(i)));
            @SuppressWarnings("unchecked")
            ActionListener<SearchResponse> actionListener = Mockito.mock(ActionListener.class);
            searchActionListeners.add(actionListener);
            coordinator.queue.add(new Coordinator.Slot(searchRequest, actionListener));
        }

        SearchRequest searchRequest = new SearchRequest("my-index");
        searchRequest.source().query(new MatchQueryBuilder("my_field", String.valueOf(5)));
        @SuppressWarnings("unchecked")
        ActionListener<SearchResponse> actionListener = Mockito.mock(ActionListener.class);
        searchActionListeners.add(actionListener);
        coordinator.schedule(searchRequest, actionListener);

        // First batch of search requests have been sent off:
        // (However still 5 should remain in the queue)
        assertThat(coordinator.queue.size(), equalTo(0));
        assertThat(coordinator.remoteRequestsCurrent.get(), equalTo(1));
        assertThat(lookupFunction.capturedRequests.size(), equalTo(1));
        assertThat(lookupFunction.capturedRequests.get(0).requests().size(), equalTo(5));

        RuntimeException e = new RuntimeException();
        // Replying a response and that should trigger another coordination round
        MultiSearchResponse.Item[] responseItems = new MultiSearchResponse.Item[5];
        for (int i = 0; i < 5; i++) {
            responseItems[i] = new MultiSearchResponse.Item(null, e);
        }
        lookupFunction.capturedConsumers.get(0).accept(new MultiSearchResponse(responseItems, 1L), null);
        assertThat(coordinator.queue.size(), equalTo(0));
        assertThat(coordinator.remoteRequestsCurrent.get(), equalTo(0));
        assertThat(lookupFunction.capturedRequests.size(), equalTo(1));

        // All individual action listeners for the search requests should have been invoked:
        for (ActionListener<SearchResponse> searchActionListener : searchActionListeners) {
            Mockito.verify(searchActionListener).onFailure(Mockito.eq(e));
        }
    }

    public void testQueueing() throws Exception {
        MockLookupFunction lookupFunction = new MockLookupFunction();
        Coordinator coordinator = new Coordinator(lookupFunction, 1, 1, 1);
        coordinator.queue.add(new Coordinator.Slot(new SearchRequest(), ActionListener.wrap(() -> {})));

        AtomicBoolean completed = new AtomicBoolean(false);
        SearchRequest searchRequest = new SearchRequest();
        Thread t = new Thread(() -> {
            coordinator.schedule(searchRequest, ActionListener.wrap(() -> {}));
            completed.set(true);
        });
        t.start();
        assertBusy(() -> {
            assertThat(t.getState(), equalTo(Thread.State.WAITING));
            assertThat(completed.get(), is(false));
        });

        coordinator.coordinateLookups();
        assertBusy(() -> { assertThat(completed.get(), is(true)); });

        lookupFunction.capturedConsumers.get(0)
            .accept(
                new MultiSearchResponse(new MultiSearchResponse.Item[] { new MultiSearchResponse.Item(emptySearchResponse(), null) }, 1L),
                null
            );
        assertThat(coordinator.queue.size(), equalTo(0));
        assertThat(lookupFunction.capturedRequests.size(), equalTo(2));
        assertThat(lookupFunction.capturedRequests.get(1).requests().get(0), sameInstance(searchRequest));
    }

    public void testLookupFunction() {
        MultiSearchRequest multiSearchRequest = new MultiSearchRequest();
        List<String> indices = Arrays.asList("index1", "index2", "index3");
        for (String index : indices) {
            multiSearchRequest.add(new SearchRequest(index));
            multiSearchRequest.add(new SearchRequest(index));
        }

        List<EnrichShardMultiSearchAction.Request> requests = new ArrayList<>();
        ElasticsearchClient client = new ElasticsearchClient() {

            @Override
            public <Request extends ActionRequest, Response extends ActionResponse> ActionFuture<Response> execute(
                ActionType<Response> action,
                Request request
            ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <Request extends ActionRequest, Response extends ActionResponse> void execute(
                ActionType<Response> action,
                Request request,
                ActionListener<Response> listener
            ) {
                requests.add((EnrichShardMultiSearchAction.Request) request);
            }

            @Override
            public ThreadPool threadPool() {
                throw new UnsupportedOperationException();
            }
        };
        BiConsumer<MultiSearchRequest, BiConsumer<MultiSearchResponse, Exception>> consumer = Coordinator.lookupFunction(client);
        consumer.accept(multiSearchRequest, null);

        assertThat(requests.size(), equalTo(indices.size()));
        requests.sort(Comparator.comparing(SingleShardRequest::index));
        for (int i = 0; i < indices.size(); i++) {
            String index = indices.get(i);
            assertThat(requests.get(i).index(), equalTo(index));
            assertThat(requests.get(i).getMultiSearchRequest().requests().size(), equalTo(2));
            assertThat(requests.get(i).getMultiSearchRequest().requests().get(0).indices().length, equalTo(1));
            assertThat(requests.get(i).getMultiSearchRequest().requests().get(0).indices()[0], equalTo(index));
        }
    }

    public void testReduce() {
        Map<String, List<Tuple<Integer, SearchRequest>>> itemsPerIndex = new HashMap<>();
        Map<String, Tuple<MultiSearchResponse, Exception>> shardResponses = new HashMap<>();

        MultiSearchResponse.Item item1 = new MultiSearchResponse.Item(emptySearchResponse(), null);
        itemsPerIndex.put("index1", Arrays.asList(new Tuple<>(0, null), new Tuple<>(1, null), new Tuple<>(2, null)));
        shardResponses.put("index1", new Tuple<>(new MultiSearchResponse(new MultiSearchResponse.Item[] { item1, item1, item1 }, 1), null));

        Exception failure = new RuntimeException();
        itemsPerIndex.put("index2", Arrays.asList(new Tuple<>(3, null), new Tuple<>(4, null), new Tuple<>(5, null)));
        shardResponses.put("index2", new Tuple<>(null, failure));

        MultiSearchResponse.Item item2 = new MultiSearchResponse.Item(emptySearchResponse(), null);
        itemsPerIndex.put("index3", Arrays.asList(new Tuple<>(6, null), new Tuple<>(7, null), new Tuple<>(8, null)));
        shardResponses.put("index3", new Tuple<>(new MultiSearchResponse(new MultiSearchResponse.Item[] { item2, item2, item2 }, 1), null));

        MultiSearchResponse result = Coordinator.reduce(9, itemsPerIndex, shardResponses);
        assertThat(result.getResponses().length, equalTo(9));
        assertThat(result.getResponses()[0], sameInstance(item1));
        assertThat(result.getResponses()[1], sameInstance(item1));
        assertThat(result.getResponses()[2], sameInstance(item1));
        assertThat(result.getResponses()[3].getFailure(), sameInstance(failure));
        assertThat(result.getResponses()[4].getFailure(), sameInstance(failure));
        assertThat(result.getResponses()[5].getFailure(), sameInstance(failure));
        assertThat(result.getResponses()[6], sameInstance(item2));
        assertThat(result.getResponses()[7], sameInstance(item2));
        assertThat(result.getResponses()[8], sameInstance(item2));
    }

    private static SearchResponse emptySearchResponse() {
        InternalSearchResponse response = new InternalSearchResponse(
            new SearchHits(new SearchHit[0], new TotalHits(0, TotalHits.Relation.EQUAL_TO), Float.NaN),
            InternalAggregations.EMPTY,
            null,
            null,
            false,
            null,
            1
        );
        return new SearchResponse(response, null, 1, 1, 0, 100, ShardSearchFailure.EMPTY_ARRAY, SearchResponse.Clusters.EMPTY);
    }

    private class MockLookupFunction implements BiConsumer<MultiSearchRequest, BiConsumer<MultiSearchResponse, Exception>> {

        private final List<MultiSearchRequest> capturedRequests = new ArrayList<>();
        private final List<BiConsumer<MultiSearchResponse, Exception>> capturedConsumers = new ArrayList<>();

        @Override
        public void accept(MultiSearchRequest multiSearchRequest, BiConsumer<MultiSearchResponse, Exception> consumer) {
            capturedRequests.add(multiSearchRequest);
            capturedConsumers.add(consumer);
        }
    }

}
