/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthAction;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.search.internal.InternalSearchResponse;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.xpack.core.security.authc.AuthenticationField;
import org.elasticsearch.xpack.core.security.authc.AuthenticationServiceField;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static org.elasticsearch.xpack.core.ClientHelper.ACTION_ORIGIN_TRANSIENT_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientHelperTests extends ESTestCase {
    public void testExecuteAsyncWrapsListener() throws Exception {
        final ThreadContext threadContext = new ThreadContext(Settings.EMPTY);
        final String headerName = randomAlphaOfLengthBetween(4, 16);
        final String headerValue = randomAlphaOfLengthBetween(4, 16);
        final String origin = randomAlphaOfLengthBetween(4, 16);
        final CountDownLatch latch = new CountDownLatch(2);
        final ActionListener<ClusterHealthResponse> listener = ActionListener.wrap(v -> {
            assertNull(threadContext.getTransient(ClientHelper.ACTION_ORIGIN_TRANSIENT_NAME));
            assertEquals(headerValue, threadContext.getHeader(headerName));
            latch.countDown();
        }, e -> fail(e.getMessage()));

        final ClusterHealthRequest request = new ClusterHealthRequest();
        threadContext.putHeader(headerName, headerValue);

        ClientHelper.executeAsyncWithOrigin(threadContext, origin, request, listener, (req, listener1) -> {
            assertSame(request, req);
            assertEquals(origin, threadContext.getTransient(ClientHelper.ACTION_ORIGIN_TRANSIENT_NAME));
            assertNull(threadContext.getHeader(headerName));
            latch.countDown();
            listener1.onResponse(null);
        });

        latch.await();
    }

    public void testExecuteWithClient() throws Exception {
        final ThreadContext threadContext = new ThreadContext(Settings.EMPTY);
        final Client client = mock(Client.class);
        final ThreadPool threadPool = mock(ThreadPool.class);
        when(client.threadPool()).thenReturn(threadPool);
        when(threadPool.getThreadContext()).thenReturn(threadContext);

        final String headerName = randomAlphaOfLengthBetween(4, 16);
        final String headerValue = randomAlphaOfLengthBetween(4, 16);
        final String origin = randomAlphaOfLengthBetween(4, 16);
        final CountDownLatch latch = new CountDownLatch(2);
        final ActionListener<ClusterHealthResponse> listener = ActionListener.wrap(v -> {
            assertNull(threadContext.getTransient(ClientHelper.ACTION_ORIGIN_TRANSIENT_NAME));
            assertEquals(headerValue, threadContext.getHeader(headerName));
            latch.countDown();
        }, e -> fail(e.getMessage()));

        doAnswer(invocationOnMock -> {
            assertEquals(origin, threadContext.getTransient(ClientHelper.ACTION_ORIGIN_TRANSIENT_NAME));
            assertNull(threadContext.getHeader(headerName));
            latch.countDown();
            ((ActionListener<?>) invocationOnMock.getArguments()[2]).onResponse(null);
            return null;
        }).when(client).execute(anyObject(), anyObject(), anyObject());

        threadContext.putHeader(headerName, headerValue);
        ClientHelper.executeAsyncWithOrigin(client, origin, ClusterHealthAction.INSTANCE, new ClusterHealthRequest(), listener);

        latch.await();
    }

    public void testClientWithOrigin() throws Exception {
        final ThreadContext threadContext = new ThreadContext(Settings.EMPTY);
        final Client client = mock(Client.class);
        final ThreadPool threadPool = mock(ThreadPool.class);
        when(client.threadPool()).thenReturn(threadPool);
        when(threadPool.getThreadContext()).thenReturn(threadContext);
        when(client.settings()).thenReturn(Settings.EMPTY);

        final String headerName = randomAlphaOfLengthBetween(4, 16);
        final String headerValue = randomAlphaOfLengthBetween(4, 16);
        final String origin = randomAlphaOfLengthBetween(4, 16);
        final CountDownLatch latch = new CountDownLatch(2);
        final ActionListener<ClusterHealthResponse> listener = ActionListener.wrap(v -> {
            assertNull(threadContext.getTransient(ClientHelper.ACTION_ORIGIN_TRANSIENT_NAME));
            assertEquals(headerValue, threadContext.getHeader(headerName));
            latch.countDown();
        }, e -> fail(e.getMessage()));


        doAnswer(invocationOnMock -> {
            assertEquals(origin, threadContext.getTransient(ClientHelper.ACTION_ORIGIN_TRANSIENT_NAME));
            assertNull(threadContext.getHeader(headerName));
            latch.countDown();
            ((ActionListener<?>) invocationOnMock.getArguments()[2]).onResponse(null);
            return null;
        }).when(client).execute(anyObject(), anyObject(), anyObject());

        threadContext.putHeader(headerName, headerValue);
        Client clientWithOrigin = ClientHelper.clientWithOrigin(client, origin);
        clientWithOrigin.execute(null, null, listener);
        latch.await();
    }

    public void testExecuteWithHeadersAsyncNoHeaders() throws InterruptedException {
        final ThreadContext threadContext = new ThreadContext(Settings.EMPTY);
        final Client client = mock(Client.class);
        final ThreadPool threadPool = mock(ThreadPool.class);
        when(client.threadPool()).thenReturn(threadPool);
        when(threadPool.getThreadContext()).thenReturn(threadContext);

        final CountDownLatch latch = new CountDownLatch(2);
        final ActionListener<SearchResponse> listener = ActionListener.wrap(v -> {
            assertTrue(threadContext.getHeaders().isEmpty());
            latch.countDown();
        }, e -> fail(e.getMessage()));

        doAnswer(invocationOnMock -> {
            assertTrue(threadContext.getHeaders().isEmpty());
            latch.countDown();
            ((ActionListener<?>) invocationOnMock.getArguments()[2]).onResponse(null);
            return null;
        }).when(client).execute(anyObject(), anyObject(), anyObject());

        SearchRequest request = new SearchRequest("foo");

        String originName = randomFrom(ClientHelper.ML_ORIGIN, ClientHelper.WATCHER_ORIGIN, ClientHelper.ROLLUP_ORIGIN);
        ClientHelper.executeWithHeadersAsync(Collections.emptyMap(), originName, client, SearchAction.INSTANCE, request, listener);

        latch.await();
    }

    public void testExecuteWithHeadersAsyncWrongHeaders() throws InterruptedException {
        final ThreadContext threadContext = new ThreadContext(Settings.EMPTY);
        final Client client = mock(Client.class);
        final ThreadPool threadPool = mock(ThreadPool.class);
        when(client.threadPool()).thenReturn(threadPool);
        when(threadPool.getThreadContext()).thenReturn(threadContext);

        final CountDownLatch latch = new CountDownLatch(2);
        final ActionListener<SearchResponse> listener = ActionListener.wrap(v -> {
            assertTrue(threadContext.getHeaders().isEmpty());
            latch.countDown();
        }, e -> fail(e.getMessage()));

        doAnswer(invocationOnMock -> {
            assertTrue(threadContext.getHeaders().isEmpty());
            latch.countDown();
            ((ActionListener<?>) invocationOnMock.getArguments()[2]).onResponse(null);
            return null;
        }).when(client).execute(anyObject(), anyObject(), anyObject());

        SearchRequest request = new SearchRequest("foo");
        Map<String, String> headers = new HashMap<>(1);
        headers.put("foo", "foo");
        headers.put("bar", "bar");

        String originName = randomFrom(ClientHelper.ML_ORIGIN, ClientHelper.WATCHER_ORIGIN, ClientHelper.ROLLUP_ORIGIN);
        ClientHelper.executeWithHeadersAsync(headers, originName, client, SearchAction.INSTANCE, request, listener);

        latch.await();
    }

    public void testExecuteWithHeadersAsyncWithHeaders() throws Exception {
        final ThreadContext threadContext = new ThreadContext(Settings.EMPTY);
        final Client client = mock(Client.class);
        final ThreadPool threadPool = mock(ThreadPool.class);
        when(client.threadPool()).thenReturn(threadPool);
        when(threadPool.getThreadContext()).thenReturn(threadContext);

        final CountDownLatch latch = new CountDownLatch(2);
        final ActionListener<SearchResponse> listener = ActionListener.wrap(v -> {
            assertTrue(threadContext.getHeaders().isEmpty());
            latch.countDown();
        }, e -> fail(e.getMessage()));

        doAnswer(invocationOnMock -> {
            assertThat(threadContext.getHeaders().size(), equalTo(2));
            assertThat(threadContext.getHeaders().get("es-security-runas-user"), equalTo("foo"));
            assertThat(threadContext.getHeaders().get("_xpack_security_authentication"), equalTo("bar"));
            latch.countDown();
            ((ActionListener<?>) invocationOnMock.getArguments()[2]).onResponse(null);
            return null;
        }).when(client).execute(anyObject(), anyObject(), anyObject());

        SearchRequest request = new SearchRequest("foo");
        Map<String, String> headers = new HashMap<>(1);
        headers.put("es-security-runas-user", "foo");
        headers.put("_xpack_security_authentication", "bar");

        String originName = randomFrom(ClientHelper.ML_ORIGIN, ClientHelper.WATCHER_ORIGIN, ClientHelper.ROLLUP_ORIGIN);
        ClientHelper.executeWithHeadersAsync(headers, originName, client, SearchAction.INSTANCE, request, listener);

        latch.await();
    }

    public void testExecuteWithHeadersNoHeaders() {
        Client client = mock(Client.class);
        ThreadPool threadPool = mock(ThreadPool.class);
        ThreadContext threadContext = new ThreadContext(Settings.EMPTY);
        when(threadPool.getThreadContext()).thenReturn(threadContext);
        when(client.threadPool()).thenReturn(threadPool);

        PlainActionFuture<SearchResponse> searchFuture = PlainActionFuture.newFuture();
        searchFuture.onResponse(new SearchResponse(InternalSearchResponse.empty(), null, 0, 0, 0, 0L, ShardSearchFailure.EMPTY_ARRAY,
            SearchResponse.Clusters.EMPTY));
        when(client.search(any())).thenReturn(searchFuture);
        assertExecutionWithOrigin(Collections.emptyMap(), client);
    }

    public void testExecuteWithHeaders() {
        Client client = mock(Client.class);
        ThreadPool threadPool = mock(ThreadPool.class);
        ThreadContext threadContext = new ThreadContext(Settings.EMPTY);
        when(threadPool.getThreadContext()).thenReturn(threadContext);
        when(client.threadPool()).thenReturn(threadPool);

        PlainActionFuture<SearchResponse> searchFuture = PlainActionFuture.newFuture();
        searchFuture.onResponse(new SearchResponse(InternalSearchResponse.empty(), null, 0, 0, 0, 0L, ShardSearchFailure.EMPTY_ARRAY,
            SearchResponse.Clusters.EMPTY));
        when(client.search(any())).thenReturn(searchFuture);
        Map<String, String> headers = MapBuilder.<String, String> newMapBuilder().put(AuthenticationField.AUTHENTICATION_KEY, "anything")
                .put(AuthenticationServiceField.RUN_AS_USER_HEADER, "anything").map();

        assertRunAsExecution(headers, h -> {
            assertThat(h.keySet(), hasSize(2));
            assertThat(h, hasEntry(AuthenticationField.AUTHENTICATION_KEY, "anything"));
            assertThat(h, hasEntry(AuthenticationServiceField.RUN_AS_USER_HEADER, "anything"));
        }, client);
    }

    public void testExecuteWithHeadersNoSecurityHeaders() {
        Client client = mock(Client.class);
        ThreadPool threadPool = mock(ThreadPool.class);
        ThreadContext threadContext = new ThreadContext(Settings.EMPTY);
        when(threadPool.getThreadContext()).thenReturn(threadContext);
        when(client.threadPool()).thenReturn(threadPool);

        PlainActionFuture<SearchResponse> searchFuture = PlainActionFuture.newFuture();
        searchFuture.onResponse(new SearchResponse(InternalSearchResponse.empty(), null, 0, 0, 0, 0L, ShardSearchFailure.EMPTY_ARRAY,
            SearchResponse.Clusters.EMPTY));
        when(client.search(any())).thenReturn(searchFuture);
        Map<String, String> unrelatedHeaders = MapBuilder.<String, String> newMapBuilder().put(randomAlphaOfLength(10), "anything").map();

        assertExecutionWithOrigin(unrelatedHeaders, client);
    }

    /**
     * This method executes a search and checks if the thread context was
     * enriched with the ml origin
     */
    private void assertExecutionWithOrigin(Map<String, String> storedHeaders, Client client) {
        String originName = randomFrom(ClientHelper.ML_ORIGIN, ClientHelper.WATCHER_ORIGIN, ClientHelper.ROLLUP_ORIGIN);
        ClientHelper.executeWithHeaders(storedHeaders, originName, client, () -> {
            Object origin = client.threadPool().getThreadContext().getTransient(ACTION_ORIGIN_TRANSIENT_NAME);
            assertThat(origin, is(originName));

            // Check that headers are not set
            Map<String, String> headers = client.threadPool().getThreadContext().getHeaders();
            assertThat(headers, not(hasEntry(AuthenticationField.AUTHENTICATION_KEY, "anything")));
            assertThat(headers, not(hasEntry(AuthenticationServiceField.RUN_AS_USER_HEADER, "anything")));

            return client.search(new SearchRequest()).actionGet();
        });
    }

    /**
     * This method executes a search and ensures no stashed origin thread
     * context was created, so that the regular node client was used, to emulate
     * a run_as function
     */
    public void assertRunAsExecution(Map<String, String> storedHeaders, Consumer<Map<String, String>> consumer, Client client) {
        String originName = randomFrom(ClientHelper.ML_ORIGIN, ClientHelper.WATCHER_ORIGIN, ClientHelper.ROLLUP_ORIGIN);
        ClientHelper.executeWithHeaders(storedHeaders, originName, client, () -> {
            Object origin = client.threadPool().getThreadContext().getTransient(ACTION_ORIGIN_TRANSIENT_NAME);
            assertThat(origin, is(nullValue()));

            consumer.accept(client.threadPool().getThreadContext().getHeaders());
            return client.search(new SearchRequest()).actionGet();
        });
    }
}
