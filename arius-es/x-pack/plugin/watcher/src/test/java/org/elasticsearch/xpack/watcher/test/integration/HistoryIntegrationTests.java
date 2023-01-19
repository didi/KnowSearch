/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.watcher.test.integration;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.protocol.xpack.watcher.PutWatchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xpack.core.watcher.actions.ActionStatus;
import org.elasticsearch.xpack.core.watcher.client.WatchSourceBuilder;
import org.elasticsearch.xpack.core.watcher.input.Input;
import org.elasticsearch.xpack.core.watcher.support.xcontent.XContentSource;
import org.elasticsearch.xpack.core.watcher.watch.WatchStatus;
import org.elasticsearch.xpack.watcher.support.search.WatcherSearchTemplateRequest;
import org.elasticsearch.xpack.watcher.test.AbstractWatcherIntegrationTestCase;
import org.elasticsearch.xpack.watcher.test.WatcherTestUtils;
import org.elasticsearch.xpack.watcher.trigger.schedule.IntervalSchedule;

import java.util.Locale;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.mapper.MapperService.SINGLE_MAPPING_NAME;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.search.builder.SearchSourceBuilder.searchSource;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertHitCount;
import static org.elasticsearch.xpack.watcher.actions.ActionBuilders.loggingAction;
import static org.elasticsearch.xpack.watcher.client.WatchSourceBuilders.watchBuilder;
import static org.elasticsearch.xpack.watcher.input.InputBuilders.chainInput;
import static org.elasticsearch.xpack.watcher.input.InputBuilders.searchInput;
import static org.elasticsearch.xpack.watcher.input.InputBuilders.simpleInput;
import static org.elasticsearch.xpack.watcher.test.WatcherTestUtils.templateRequest;
import static org.elasticsearch.xpack.watcher.trigger.TriggerBuilders.schedule;
import static org.elasticsearch.xpack.watcher.trigger.schedule.Schedules.interval;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class HistoryIntegrationTests extends AbstractWatcherIntegrationTestCase {

    // issue: https://github.com/elastic/x-plugins/issues/2338
    public void testThatHistoryIsWrittenWithChainedInput() throws Exception {
        XContentBuilder xContentBuilder = jsonBuilder().startObject().startObject("inner").field("date", "2015-06-06").endObject()
                .endObject();
        index("foo", "bar", "1", xContentBuilder);
        refresh();

        WatchSourceBuilder builder = watchBuilder()
                .trigger(schedule(interval("10s")))
                .addAction("logging", loggingAction("foo"));

        builder.input(chainInput().add("first", searchInput(
                templateRequest(searchSource().sort(SortBuilders.fieldSort("inner.date").order(SortOrder.DESC)), "foo")))
        );

        PutWatchResponse response = watcherClient().preparePutWatch("test_watch").setSource(builder).get();
        assertThat(response.isCreated(), is(true));

        watcherClient().prepareExecuteWatch("test_watch").setRecordExecution(true).get();

        flushAndRefresh(".watcher-history-*");
        SearchResponse searchResponse = client().prepareSearch(".watcher-history-*").get();
        assertHitCount(searchResponse, 1);
    }

    // See https://github.com/elastic/x-plugins/issues/2913
    public void testFailedInputResultWithDotsInFieldNameGetsStored() throws Exception {
        WatcherSearchTemplateRequest request = templateRequest(searchSource()
                .query(matchAllQuery())
                .sort("trigger_event.triggered_time", SortOrder.DESC)
                .size(1), "non-existing-index");

        // The result of the search input will be a failure, because a missing index does not exist when
        // the query is executed
        Input.Builder input = searchInput(request);
        // wrapping this randomly into a chained input to test this as well
        boolean useChained = randomBoolean();
        if (useChained) {
            input = chainInput().add("chained", input);
        }

        watcherClient().preparePutWatch("test_watch")
                .setSource(watchBuilder()
                        .trigger(schedule(interval(5, IntervalSchedule.Interval.Unit.HOURS)))
                        .input(input)
                        .addAction("_logger", loggingAction("#### randomLogging")))
                .get();

        watcherClient().prepareExecuteWatch("test_watch").setRecordExecution(true).get();

        refresh(".watcher-history*");
        SearchResponse searchResponse = client().prepareSearch(".watcher-history*").setSize(0).get();
        assertHitCount(searchResponse, 1);

        // as fields with dots are allowed in 5.0 again, the mapping must be checked in addition
        GetMappingsResponse response = client().admin().indices().prepareGetMappings(".watcher-history*")
            .addTypes(SINGLE_MAPPING_NAME).get();
        byte[] bytes = response.getMappings().values().iterator().next().value.get(SINGLE_MAPPING_NAME).source().uncompressed();
        XContentSource source = new XContentSource(new BytesArray(bytes), XContentType.JSON);
        // lets make sure the body fields are disabled
        if (useChained) {
            String chainedPath = SINGLE_MAPPING_NAME +
                ".properties.result.properties.input.properties.chain.properties.chained.properties.search" +
                    ".properties.request.properties.body.enabled";
            assertThat(source.getValue(chainedPath), is(false));
        } else {
            String path = SINGLE_MAPPING_NAME +
                ".properties.result.properties.input.properties.search.properties.request.properties.body.enabled";
            assertThat(source.getValue(path), is(false));
        }
    }

    // See https://github.com/elastic/x-plugins/issues/2913
    public void testPayloadInputWithDotsInFieldNameWorks() throws Exception {
        Input.Builder input = simpleInput("foo.bar", "bar");

        // wrapping this randomly into a chained input to test this as well
        boolean useChained = randomBoolean();
        if (useChained) {
            input = chainInput().add("chained", input);
        }

        watcherClient().preparePutWatch("test_watch")
                .setSource(watchBuilder()
                        .trigger(schedule(interval(5, IntervalSchedule.Interval.Unit.HOURS)))
                        .input(input)
                        .addAction("_logger", loggingAction("#### randomLogging")))
                .get();

        watcherClient().prepareExecuteWatch("test_watch").setRecordExecution(true).get();

        refresh(".watcher-history*");
        SearchResponse searchResponse = client().prepareSearch(".watcher-history*").setSize(0).get();
        assertHitCount(searchResponse, 1);

        // as fields with dots are allowed in 5.0 again, the mapping must be checked in addition
        GetMappingsResponse response = client().admin().indices().prepareGetMappings(".watcher-history*")
            .addTypes(SINGLE_MAPPING_NAME).get();
        byte[] bytes = response.getMappings().values().iterator().next().value.get(SINGLE_MAPPING_NAME).source().uncompressed();
        XContentSource source = new XContentSource(new BytesArray(bytes), XContentType.JSON);

        // lets make sure the body fields are disabled
        if (useChained) {
            String path = SINGLE_MAPPING_NAME +
                ".properties.result.properties.input.properties.chain.properties.chained.properties.payload.enabled";
            assertThat(source.getValue(path), is(false));
        } else {
            String path = SINGLE_MAPPING_NAME + ".properties.result.properties.input.properties.payload.enabled";
            assertThat(source.getValue(path), is(false));
        }
    }

    public void testThatHistoryContainsStatus() throws Exception {
        watcherClient().preparePutWatch("test_watch")
                .setSource(watchBuilder()
                        .trigger(schedule(interval(5, IntervalSchedule.Interval.Unit.HOURS)))
                        .input(simpleInput("foo", "bar"))
                        .addAction("_logger", loggingAction("#### randomLogging")))
                .get();

        watcherClient().prepareExecuteWatch("test_watch").setRecordExecution(true).get();

        WatchStatus status = watcherClient().prepareGetWatch("test_watch").get().getStatus();

        refresh(".watcher-history*");
        SearchResponse searchResponse = client().prepareSearch(".watcher-history*").setSize(1).get();
        assertHitCount(searchResponse, 1);
        SearchHit hit = searchResponse.getHits().getAt(0);

        XContentSource source = new XContentSource(hit.getSourceRef(), XContentType.JSON);

        Boolean active = source.getValue("status.state.active");
        assertThat(active, is(status.state().isActive()));

        String timestamp = source.getValue("status.state.timestamp");
        assertThat(timestamp, WatcherTestUtils.isSameDate(status.state().getTimestamp()));

        String lastChecked = source.getValue("status.last_checked");
        assertThat(lastChecked, WatcherTestUtils.isSameDate(status.lastChecked()));
        String lastMetCondition = source.getValue("status.last_met_condition");
        assertThat(lastMetCondition, WatcherTestUtils.isSameDate(status.lastMetCondition()));

        Integer version = source.getValue("status.version");
        int expectedVersion = (int) (status.version() - 1);
        assertThat(version, is(expectedVersion));

        ActionStatus actionStatus = status.actionStatus("_logger");
        String ackStatusState = source.getValue("status.actions._logger.ack.state").toString().toUpperCase(Locale.ROOT);
        assertThat(ackStatusState, is(actionStatus.ackStatus().state().toString()));

        Boolean lastExecutionSuccesful = source.getValue("status.actions._logger.last_execution.successful");
        assertThat(lastExecutionSuccesful, is(actionStatus.lastExecution().successful()));

        // also ensure that the status field is disabled in the watch history
        GetMappingsResponse response = client().admin().indices().prepareGetMappings(".watcher-history*")
            .addTypes(SINGLE_MAPPING_NAME).get();
        byte[] bytes = response.getMappings().values().iterator().next().value.get(SINGLE_MAPPING_NAME).source().uncompressed();
        XContentSource mappingSource = new XContentSource(new BytesArray(bytes), XContentType.JSON);
        assertThat(mappingSource.getValue(SINGLE_MAPPING_NAME + ".properties.status.enabled"), is(false));
        assertThat(mappingSource.getValue(SINGLE_MAPPING_NAME + ".properties.status.properties.status"), is(nullValue()));
        assertThat(mappingSource.getValue(SINGLE_MAPPING_NAME + ".properties.status.properties.status.properties.active"), is(nullValue()));
    }


}
