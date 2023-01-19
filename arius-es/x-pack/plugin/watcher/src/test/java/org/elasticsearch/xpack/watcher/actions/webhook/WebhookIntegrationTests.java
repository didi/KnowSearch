/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.watcher.actions.webhook;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.http.HttpServerTransport;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.http.MockResponse;
import org.elasticsearch.test.http.MockWebServer;
import org.elasticsearch.transport.Netty4Plugin;
import org.elasticsearch.xpack.core.watcher.history.WatchRecord;
import org.elasticsearch.xpack.core.watcher.support.xcontent.XContentSource;
import org.elasticsearch.xpack.watcher.actions.ActionBuilders;
import org.elasticsearch.xpack.watcher.common.http.BasicAuth;
import org.elasticsearch.xpack.watcher.common.http.HttpMethod;
import org.elasticsearch.xpack.watcher.common.http.HttpRequestTemplate;
import org.elasticsearch.xpack.watcher.common.text.TextTemplate;
import org.elasticsearch.xpack.watcher.condition.InternalAlwaysCondition;
import org.elasticsearch.xpack.watcher.test.AbstractWatcherIntegrationTestCase;
import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Collection;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertExists;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertNoFailures;
import static org.elasticsearch.xpack.watcher.client.WatchSourceBuilders.watchBuilder;
import static org.elasticsearch.xpack.watcher.input.InputBuilders.simpleInput;
import static org.elasticsearch.xpack.watcher.test.WatcherTestUtils.xContentSource;
import static org.elasticsearch.xpack.watcher.trigger.TriggerBuilders.schedule;
import static org.elasticsearch.xpack.watcher.trigger.schedule.Schedules.interval;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class WebhookIntegrationTests extends AbstractWatcherIntegrationTestCase {

    private MockWebServer webServer = new MockWebServer();

    @Override
    protected boolean addMockHttpTransport() {
        return false; // enable http
    }

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        ArrayList<Class<? extends Plugin>> plugins = new ArrayList<>(super.nodePlugins());
        plugins.add(Netty4Plugin.class); // for http
        return plugins;
    }

    @Before
    public void startWebservice() throws Exception {
        webServer.start();
    }

    @After
    public void stopWebservice() throws Exception {
        webServer.close();
    }

    public void testWebhook() throws Exception {
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody("body"));
        HttpRequestTemplate.Builder builder = HttpRequestTemplate.builder("localhost", webServer.getPort())
                .path(new TextTemplate("/test/_id"))
                .putParam("param1", new TextTemplate("value1"))
                .putParam("watch_id", new TextTemplate("_id"))
                .body(new TextTemplate("_body"))
                .auth(new BasicAuth("user", "pass".toCharArray()))
                .method(HttpMethod.POST);

        watcherClient().preparePutWatch("_id")
                .setSource(watchBuilder()
                        .trigger(schedule(interval("5s")))
                        .input(simpleInput("key", "value"))
                        .condition(InternalAlwaysCondition.INSTANCE)
                        .addAction("_id", ActionBuilders.webhookAction(builder)))
                .get();

        timeWarp().trigger("_id");
        refresh();

        assertWatchWithMinimumPerformedActionsCount("_id", 1, false);
        assertThat(webServer.requests(), hasSize(1));
        assertThat(webServer.requests().get(0).getUri().getQuery(),
                anyOf(equalTo("watch_id=_id&param1=value1"), equalTo("param1=value1&watch_id=_id")));

        assertThat(webServer.requests().get(0).getBody(), is("_body"));

        SearchResponse response = searchWatchRecords(b -> QueryBuilders.termQuery(WatchRecord.STATE.getPreferredName(), "executed"));

        assertNoFailures(response);
        XContentSource source = xContentSource(response.getHits().getAt(0).getSourceRef());
        String body = source.getValue("result.actions.0.webhook.response.body");
        assertThat(body, notNullValue());
        assertThat(body, is("body"));
        Number status = source.getValue("result.actions.0.webhook.response.status");
        assertThat(status, notNullValue());
        assertThat(status.intValue(), is(200));
    }

    public void testWebhookWithBasicAuth() throws Exception {
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody("body"));
        HttpRequestTemplate.Builder builder = HttpRequestTemplate.builder("localhost", webServer.getPort())
                .auth(new BasicAuth("_username", "_password".toCharArray()))
                .path(new TextTemplate("/test/_id"))
                .putParam("param1", new TextTemplate("value1"))
                .putParam("watch_id", new TextTemplate("_id"))
                .body(new TextTemplate("_body"))
                .method(HttpMethod.POST);

        watcherClient().preparePutWatch("_id")
                .setSource(watchBuilder()
                        .trigger(schedule(interval("5s")))
                        .input(simpleInput("key", "value"))
                        .condition(InternalAlwaysCondition.INSTANCE)
                        .addAction("_id", ActionBuilders.webhookAction(builder)))
                .get();

        timeWarp().trigger("_id");
        refresh();

        assertWatchWithMinimumPerformedActionsCount("_id", 1, false);

        assertThat(webServer.requests(), hasSize(1));
        assertThat(webServer.requests().get(0).getUri().getQuery(),
                anyOf(equalTo("watch_id=_id&param1=value1"), equalTo("param1=value1&watch_id=_id")));
        assertThat(webServer.requests().get(0).getBody(), is("_body"));
        assertThat(webServer.requests().get(0).getHeader("Authorization"), is(("Basic X3VzZXJuYW1lOl9wYXNzd29yZA==")));
    }

    public void testWebhookWithTimebasedIndex() throws Exception {
        assertAcked(client().admin().indices().prepareCreate("<logstash-{now/d}>").get());

        HttpServerTransport serverTransport = internalCluster().getDataNodeInstance(HttpServerTransport.class);
        TransportAddress publishAddress = serverTransport.boundAddress().publishAddress();

        String host = publishAddress.address().getHostString();
        HttpRequestTemplate.Builder builder = HttpRequestTemplate.builder(host, publishAddress.getPort())
                .path(new TextTemplate("/%3Clogstash-%7Bnow%2Fd%7D%3E/_doc/1"))
                .body(new TextTemplate("{\"foo\":\"bar\"}"))
                .putHeader("Content-Type", new TextTemplate("application/json"))
                .method(HttpMethod.PUT);

        watcherClient().preparePutWatch("_id")
                .setSource(watchBuilder()
                        .trigger(schedule(interval("5s")))
                        .input(simpleInput("key", "value"))
                        .condition(InternalAlwaysCondition.INSTANCE)
                        .addAction("_id", ActionBuilders.webhookAction(builder)))
                .get();

        watcherClient().prepareExecuteWatch("_id").get();

        GetResponse response = client().prepareGet().setIndex("<logstash-{now/d}>").setId("1").get();
        assertExists(response);
    }
}
