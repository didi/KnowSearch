/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.watcher.actions;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ObjectPath;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.protocol.xpack.watcher.PutWatchResponse;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xpack.core.watcher.history.HistoryStoreField;
import org.elasticsearch.xpack.watcher.test.AbstractWatcherIntegrationTestCase;

import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertHitCount;
import static org.elasticsearch.xpack.watcher.actions.ActionBuilders.indexAction;
import static org.elasticsearch.xpack.watcher.actions.ActionBuilders.loggingAction;
import static org.elasticsearch.xpack.watcher.client.WatchSourceBuilders.watchBuilder;
import static org.elasticsearch.xpack.watcher.input.InputBuilders.simpleInput;
import static org.elasticsearch.xpack.watcher.trigger.TriggerBuilders.schedule;
import static org.elasticsearch.xpack.watcher.trigger.schedule.Schedules.interval;
import static org.hamcrest.Matchers.is;

public class TimeThrottleIntegrationTests extends AbstractWatcherIntegrationTestCase {

    public void testTimeThrottle(){
        String id = randomAlphaOfLength(20);
        PutWatchResponse putWatchResponse = watcherClient().preparePutWatch()
                .setId(id)
                .setSource(watchBuilder()
                        .trigger(schedule(interval("5s")))
                        .input(simpleInput())
                        .addAction("my-logging-action", loggingAction("foo"))
                        .defaultThrottlePeriod(TimeValue.timeValueSeconds(30)))
                .get();
        assertThat(putWatchResponse.isCreated(), is(true));

        timeWarp().trigger(id);
        assertHistoryEntryExecuted(id);

        timeWarp().clock().fastForward(TimeValue.timeValueMillis(4000));
        timeWarp().trigger(id);
        assertHistoryEntryThrottled(id);

        timeWarp().clock().fastForwardSeconds(30);
        timeWarp().trigger(id);
        assertHistoryEntryExecuted(id);

        assertTotalHistoryEntries(id, 3);
    }

    public void testTimeThrottleDefaults() {
        String id = randomAlphaOfLength(30);
        PutWatchResponse putWatchResponse = watcherClient().preparePutWatch()
                .setId(id)
                .setSource(watchBuilder()
                        .trigger(schedule(interval("1s")))
                        .input(simpleInput())
                        .addAction("my-logging-action", indexAction("my_watcher_index")))
                .get();
        assertThat(putWatchResponse.isCreated(), is(true));

        timeWarp().trigger(id);
        assertHistoryEntryExecuted(id);

        timeWarp().clock().fastForwardSeconds(2);
        timeWarp().trigger(id);
        assertHistoryEntryThrottled(id);

        timeWarp().clock().fastForwardSeconds(10);
        timeWarp().trigger(id);
        assertHistoryEntryExecuted(id);

        assertTotalHistoryEntries(id, 3);
    }

    private void assertHistoryEntryExecuted(String id) {
        Map<String, Object> map = assertLatestHistoryEntry(id);
        String actionStatus = ObjectPath.eval("result.actions.0.status", map);
        assertThat(actionStatus, is("success"));
    }

    private void assertHistoryEntryThrottled(String id) {
        Map<String, Object> map = assertLatestHistoryEntry(id);
        String actionStatus = ObjectPath.eval("result.actions.0.status", map);
        assertThat(actionStatus, is("throttled"));
    }

    private Map<String, Object> assertLatestHistoryEntry(String id) {
        refresh(HistoryStoreField.INDEX_PREFIX_WITH_TEMPLATE + "*");

        SearchResponse searchResponse = client().prepareSearch(HistoryStoreField.INDEX_PREFIX_WITH_TEMPLATE + "*")
                .setSize(1)
                .setSource(new SearchSourceBuilder().query(QueryBuilders.boolQuery()
                        .must(termQuery("watch_id", id))))
                .addSort(SortBuilders.fieldSort("result.execution_time").order(SortOrder.DESC))
                .get();

        Map<String, Object> map = searchResponse.getHits().getHits()[0].getSourceAsMap();
        String actionId = ObjectPath.eval("result.actions.0.id", map);
        assertThat(actionId, is("my-logging-action"));
        return map;
    }

    private void assertTotalHistoryEntries(String id, long expectedCount) {
        SearchResponse searchResponse = client().prepareSearch(HistoryStoreField.INDEX_PREFIX_WITH_TEMPLATE + "*")
                .setSize(0)
                .setSource(new SearchSourceBuilder().query(QueryBuilders.boolQuery().must(termQuery("watch_id", id))))
                .get();

        assertHitCount(searchResponse, expectedCount);
    }
}
