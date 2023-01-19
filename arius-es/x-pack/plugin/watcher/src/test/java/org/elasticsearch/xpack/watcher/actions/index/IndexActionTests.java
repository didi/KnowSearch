/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.watcher.actions.index;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.time.DateUtils;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.xpack.core.watcher.actions.Action;
import org.elasticsearch.xpack.core.watcher.actions.Action.Result.Status;
import org.elasticsearch.xpack.core.watcher.execution.WatchExecutionContext;
import org.elasticsearch.xpack.core.watcher.support.WatcherDateTimeUtils;
import org.elasticsearch.xpack.core.watcher.support.xcontent.XContentSource;
import org.elasticsearch.xpack.core.watcher.watch.Payload;
import org.elasticsearch.xpack.watcher.test.WatcherTestUtils;
import org.junit.Before;
import org.mockito.ArgumentCaptor;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableSet;
import static org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import static org.elasticsearch.common.util.set.Sets.newHashSet;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IndexActionTests extends ESTestCase {

    private RefreshPolicy refreshPolicy = randomBoolean() ? null : randomFrom(RefreshPolicy.values());

    private final Client client = mock(Client.class);

    @Before
    public void setupClient() {
        ThreadPool threadPool = mock(ThreadPool.class);
        ThreadContext threadContext = new ThreadContext(Settings.EMPTY);
        when(threadPool.getThreadContext()).thenReturn(threadContext);
        when(client.threadPool()).thenReturn(threadPool);
    }

    public void testParser() throws Exception {
        String timestampField = randomBoolean() ? "@timestamp" : null;
        XContentBuilder builder = jsonBuilder();
        builder.startObject();
        boolean includeIndex = randomBoolean();
        if (includeIndex) {
            builder.field(IndexAction.Field.INDEX.getPreferredName(), "test-index");
        }
        if (timestampField != null) {
            builder.field(IndexAction.Field.EXECUTION_TIME_FIELD.getPreferredName(), timestampField);
        }
        TimeValue writeTimeout = randomBoolean() ? TimeValue.timeValueSeconds(randomInt(10)) : null;
        if (writeTimeout != null) {
            builder.field(IndexAction.Field.TIMEOUT.getPreferredName(), writeTimeout.millis());
        }
        builder.endObject();
        IndexActionFactory actionParser = new IndexActionFactory(Settings.EMPTY, client);
        XContentParser parser = createParser(builder);
        parser.nextToken();

        ExecutableIndexAction executable = actionParser.parseExecutable(randomAlphaOfLength(5), randomAlphaOfLength(3), parser);

        if (includeIndex) {
            assertThat(executable.action().index, equalTo("test-index"));
        }
        if (timestampField != null) {
            assertThat(executable.action().executionTimeField, equalTo(timestampField));
        }
        assertThat(executable.action().timeout, equalTo(writeTimeout));
    }

    public void testDeprecationTypes() throws Exception {
        XContentBuilder builder = jsonBuilder();
        builder.startObject();
        builder.field(IndexAction.Field.DOC_TYPE.getPreferredName(), "test-type");
        builder.endObject();
        IndexActionFactory actionParser = new IndexActionFactory(Settings.EMPTY, client);
        XContentParser parser = createParser(builder);
        parser.nextToken();
        ExecutableIndexAction executable = actionParser.parseExecutable(randomAlphaOfLength(5), randomAlphaOfLength(3), parser);
        assertThat(executable.action().docType, equalTo("test-type"));
        assertWarnings(IndexAction.TYPES_DEPRECATION_MESSAGE);
    }

    public void testParserFailure() throws Exception {
        // wrong type for field
        expectParseFailure(jsonBuilder()
                .startObject()
                .field(IndexAction.Field.DOC_TYPE.getPreferredName(), 1234)
                .endObject());

        expectParseFailure(jsonBuilder()
                .startObject()
                .field(IndexAction.Field.TIMEOUT.getPreferredName(), "1234")
                .endObject());

        // unknown field
        expectParseFailure(jsonBuilder()
                .startObject()
                .field("unknown", "whatever")
                .endObject());

        expectParseFailure(jsonBuilder()
                .startObject()
                .field("unknown", 1234)
                .endObject());

        // unknown refresh policy
        expectFailure(IllegalArgumentException.class, jsonBuilder()
                .startObject()
                .field(IndexAction.Field.REFRESH.getPreferredName(), "unknown")
                .endObject());
    }

    private void expectParseFailure(XContentBuilder builder) throws Exception {
        expectFailure(ElasticsearchParseException.class, builder);
    }

    private void expectFailure(Class clazz, XContentBuilder builder) throws Exception {
        IndexActionFactory actionParser = new IndexActionFactory(Settings.EMPTY, client);
        XContentParser parser = createParser(builder);
        parser.nextToken();
        expectThrows(clazz, () ->
                actionParser.parseExecutable(randomAlphaOfLength(4), randomAlphaOfLength(5), parser));
    }

    public void testUsingParameterIdWithBulkOrIdFieldThrowsIllegalState() {
        final IndexAction action = new IndexAction("test-index", "test-type", "123", null, null, null, refreshPolicy);
        final ExecutableIndexAction executable = new ExecutableIndexAction(action, logger, client,
                TimeValue.timeValueSeconds(30), TimeValue.timeValueSeconds(30));
        final Map<String, Object> docWithId = MapBuilder.<String, Object>newMapBuilder().put("foo", "bar").put("_id", "0").immutableMap();
        final ZonedDateTime executionTime = ZonedDateTime.now(ZoneOffset.UTC);

        // using doc_id with bulk fails regardless of using ID
        expectThrows(IllegalStateException.class, () -> {
            final List<Map> idList = Arrays.asList(docWithId, MapBuilder.newMapBuilder().put("foo", "bar1").put("_id", "1").map());

            final Object list = randomFrom(
                    new Map[] { singletonMap("foo", "bar"), singletonMap("foo", "bar1") },
                    Arrays.asList(singletonMap("foo", "bar"), singletonMap("foo", "bar1")),
                    unmodifiableSet(newHashSet(singletonMap("foo", "bar"), singletonMap("foo", "bar1"))),
                    idList
            );

            final WatchExecutionContext ctx = WatcherTestUtils.mockExecutionContext("_id", executionTime, new Payload.Simple("_doc", list));

            executable.execute("_id", ctx, ctx.payload());
        });

        // using doc_id with _id
        expectThrows(IllegalStateException.class, () -> {
            final Payload payload = randomBoolean() ? new Payload.Simple("_doc", docWithId) : new Payload.Simple(docWithId);
            final WatchExecutionContext ctx = WatcherTestUtils.mockExecutionContext("_id", executionTime, payload);

            executable.execute("_id", ctx, ctx.payload());
        });
    }

    public void testThatIndexTypeIdDynamically() throws Exception {
        boolean configureIndexDynamically = randomBoolean();
        boolean configureTypeDynamically = randomBoolean();
        boolean configureIdDynamically = (configureTypeDynamically == false && configureIndexDynamically == false) || randomBoolean();

        MapBuilder<String, Object> builder = MapBuilder.<String, Object>newMapBuilder().put("foo", "bar");
        if (configureIdDynamically) {
            builder.put("_id", "my_dynamic_id");
        }
        if (configureTypeDynamically) {
            builder.put("_type", "my_dynamic_type");
        }
        if (configureIndexDynamically) {
            builder.put("_index", "my_dynamic_index");
        }

        final IndexAction action = new IndexAction(configureIndexDynamically ? null : "my_index",
                configureTypeDynamically ? null : "my_type",
                configureIdDynamically ? null : "my_id",
                null, null, null, refreshPolicy);
        final ExecutableIndexAction executable = new ExecutableIndexAction(action, logger, client,
                TimeValue.timeValueSeconds(30), TimeValue.timeValueSeconds(30));

        final WatchExecutionContext ctx = WatcherTestUtils.mockExecutionContext("_id", new Payload.Simple(builder.immutableMap()));

        ArgumentCaptor<IndexRequest> captor = ArgumentCaptor.forClass(IndexRequest.class);
        PlainActionFuture<IndexResponse> listener = PlainActionFuture.newFuture();
        listener.onResponse(new IndexResponse(new ShardId(new Index("foo", "bar"), 0), "whatever", "whatever", 1, 1, 1, true));
        when(client.index(captor.capture())).thenReturn(listener);
        Action.Result result = executable.execute("_id", ctx, ctx.payload());

        assertThat(result.status(), is(Status.SUCCESS));
        assertThat(captor.getAllValues(), hasSize(1));

        assertThat(captor.getValue().index(), is(configureIndexDynamically ? "my_dynamic_index" : "my_index"));
        assertThat(captor.getValue().type(), is(configureTypeDynamically ? "my_dynamic_type" : "my_type"));
        assertThat(captor.getValue().id(), is(configureIdDynamically ? "my_dynamic_id" : "my_id"));
    }

    public void testThatIndexActionCanBeConfiguredWithDynamicIndexNameAndBulk() throws Exception {
        final IndexAction action = new IndexAction(null, "my-type", null, null, null, null, refreshPolicy);
        final ExecutableIndexAction executable = new ExecutableIndexAction(action, logger, client,
                TimeValue.timeValueSeconds(30), TimeValue.timeValueSeconds(30));

        final Map<String, Object> docWithIndex = MapBuilder.<String, Object>newMapBuilder().put("foo", "bar")
                .put("_index", "my-index").immutableMap();
        final Map<String, Object> docWithOtherIndex = MapBuilder.<String, Object>newMapBuilder().put("foo", "bar")
                .put("_index", "my-other-index").immutableMap();
        final WatchExecutionContext ctx = WatcherTestUtils.mockExecutionContext("_id",
                new Payload.Simple("_doc", Arrays.asList(docWithIndex, docWithOtherIndex)));

        ArgumentCaptor<BulkRequest> captor = ArgumentCaptor.forClass(BulkRequest.class);
        PlainActionFuture<BulkResponse> listener = PlainActionFuture.newFuture();
        IndexResponse indexResponse = new IndexResponse(new ShardId(new Index("foo", "bar"), 0), "whatever", "whatever", 1, 1, 1, true);
        BulkItemResponse response = new BulkItemResponse(0, DocWriteRequest.OpType.INDEX, indexResponse);
        BulkResponse bulkResponse = new BulkResponse(new BulkItemResponse[]{response}, 1);
        listener.onResponse(bulkResponse);
        when(client.bulk(captor.capture())).thenReturn(listener);
        Action.Result result = executable.execute("_id", ctx, ctx.payload());

        assertThat(result.status(), is(Status.SUCCESS));
        assertThat(captor.getAllValues(), hasSize(1));
        assertThat(captor.getValue().requests(), hasSize(2));
        assertThat(captor.getValue().requests().get(0).type(), is("my-type"));
        assertThat(captor.getValue().requests().get(0).index(), is("my-index"));
        assertThat(captor.getValue().requests().get(1).type(), is("my-type"));
        assertThat(captor.getValue().requests().get(1).index(), is("my-other-index"));
    }

    public void testConfigureIndexInMapAndAction() {
        String fieldName = randomFrom("_index", "_type");
        final IndexAction action = new IndexAction(fieldName.equals("_index") ? "my_index" : null,
                fieldName.equals("_type") ? "my_type" : null,
                null,null, null, null, refreshPolicy);
        final ExecutableIndexAction executable = new ExecutableIndexAction(action, logger, client,
                TimeValue.timeValueSeconds(30), TimeValue.timeValueSeconds(30));

        final Map<String, Object> docWithIndex = MapBuilder.<String, Object>newMapBuilder().put("foo", "bar")
                .put(fieldName, "my-value").immutableMap();
        final WatchExecutionContext ctx = WatcherTestUtils.mockExecutionContext("_id",
                new Payload.Simple("_doc", Collections.singletonList(docWithIndex)));

        IllegalStateException e = expectThrows(IllegalStateException.class, () -> executable.execute("_id", ctx, ctx.payload()));
        assertThat(e.getMessage(), startsWith("could not execute action [_id] of watch [_id]. [ctx.payload." +
                fieldName + "] or [ctx.payload._doc." + fieldName + "]"));
    }

    public void testIndexActionExecuteSingleDoc() throws Exception {
        boolean customId = randomBoolean();
        boolean docIdAsParam = customId && randomBoolean();
        String docId = randomAlphaOfLength(5);
        String timestampField = randomFrom("@timestamp", null);

        IndexAction action = new IndexAction("test-index", "test-type", docIdAsParam ? docId : null, timestampField, null, null,
                refreshPolicy);
        ExecutableIndexAction executable = new ExecutableIndexAction(action, logger, client, TimeValue.timeValueSeconds(30),
                TimeValue.timeValueSeconds(30));
        ZonedDateTime executionTime = DateUtils.nowWithMillisResolution();
        Payload payload;

        if (customId && docIdAsParam == false) {
            // intentionally immutable because the other side needs to cut out _id
            payload = new Payload.Simple("_doc", MapBuilder.newMapBuilder().put("foo", "bar").put("_id", docId).immutableMap());
        } else {
            payload = randomBoolean() ? new Payload.Simple("foo", "bar") : new Payload.Simple("_doc", singletonMap("foo", "bar"));
        }

        WatchExecutionContext ctx = WatcherTestUtils.mockExecutionContext("_id", executionTime, payload);

        ArgumentCaptor<IndexRequest> captor = ArgumentCaptor.forClass(IndexRequest.class);
        PlainActionFuture<IndexResponse> listener = PlainActionFuture.newFuture();
        listener.onResponse(new IndexResponse(new ShardId(new Index("test-index", "uuid"), 0), "test-type", docId, 1, 1, 1, true));
        when(client.index(captor.capture())).thenReturn(listener);

        Action.Result result = executable.execute("_id", ctx, ctx.payload());

        assertThat(result.status(), equalTo(Status.SUCCESS));
        assertThat(result, instanceOf(IndexAction.Result.class));
        IndexAction.Result successResult = (IndexAction.Result) result;
        XContentSource response = successResult.response();
        assertThat(response.getValue("created"), equalTo((Object)Boolean.TRUE));
        assertThat(response.getValue("version"), equalTo((Object) 1));
        assertThat(response.getValue("type").toString(), equalTo("test-type"));
        assertThat(response.getValue("index").toString(), equalTo("test-index"));

        assertThat(captor.getAllValues(), hasSize(1));
        IndexRequest indexRequest = captor.getValue();
        assertThat(indexRequest.sourceAsMap(), is(hasEntry("foo", "bar")));
        if (customId) {
            assertThat(indexRequest.id(), is(docId));
        }

        RefreshPolicy expectedRefreshPolicy = refreshPolicy == null ? RefreshPolicy.NONE: refreshPolicy;
        assertThat(indexRequest.getRefreshPolicy(), is(expectedRefreshPolicy));

        if (timestampField != null) {
            assertThat(indexRequest.sourceAsMap().keySet(), is(hasSize(2)));
            assertThat(indexRequest.sourceAsMap(), hasEntry(timestampField, WatcherDateTimeUtils.formatDate(executionTime)));
        } else {
            assertThat(indexRequest.sourceAsMap().keySet(), is(hasSize(1)));
        }
    }

    public void testFailureResult() throws Exception {
        IndexAction action = new IndexAction("test-index", "test-type", null, "@timestamp", null, null, refreshPolicy);
        ExecutableIndexAction executable = new ExecutableIndexAction(action, logger, client,
                TimeValue.timeValueSeconds(30), TimeValue.timeValueSeconds(30));

        // should the result resemble a failure or a partial failure
        boolean isPartialFailure = randomBoolean();

        List<Map<String, Object>> docs = new ArrayList<>();
        docs.add(Collections.singletonMap("foo", Collections.singletonMap("foo", "bar")));
        docs.add(Collections.singletonMap("foo", Collections.singletonMap("foo", "bar")));
        Payload payload = new Payload.Simple(Collections.singletonMap("_doc", docs));

        WatchExecutionContext ctx = WatcherTestUtils.mockExecutionContext("_id", ZonedDateTime.now(ZoneOffset.UTC), payload);

        ArgumentCaptor<BulkRequest> captor = ArgumentCaptor.forClass(BulkRequest.class);
        PlainActionFuture<BulkResponse> listener = PlainActionFuture.newFuture();
        BulkItemResponse.Failure failure = new BulkItemResponse.Failure("test-index", "test-type", "anything",
                new ElasticsearchException("anything"));
        BulkItemResponse firstResponse = new BulkItemResponse(0, DocWriteRequest.OpType.INDEX, failure);
        BulkItemResponse secondResponse;
        if (isPartialFailure) {
            ShardId shardId = new ShardId(new Index("foo", "bar"), 0);
            IndexResponse indexResponse = new IndexResponse(shardId, "whatever", "whatever", 1, 1, 1, true);
            secondResponse = new BulkItemResponse(1, DocWriteRequest.OpType.INDEX, indexResponse);
        } else {
            secondResponse = new BulkItemResponse(1, DocWriteRequest.OpType.INDEX, failure);
        }
        BulkResponse bulkResponse = new BulkResponse(new BulkItemResponse[]{firstResponse, secondResponse}, 1);
        listener.onResponse(bulkResponse);
        when(client.bulk(captor.capture())).thenReturn(listener);
        Action.Result result = executable.execute("_id", ctx, payload);
        RefreshPolicy expectedRefreshPolicy = refreshPolicy == null ? RefreshPolicy.NONE: refreshPolicy;
        assertThat(captor.getValue().getRefreshPolicy(), is(expectedRefreshPolicy));

        if (isPartialFailure) {
            assertThat(result.status(), is(Status.PARTIAL_FAILURE));
        } else {
            assertThat(result.status(), is(Status.FAILURE));
        }
    }
}
