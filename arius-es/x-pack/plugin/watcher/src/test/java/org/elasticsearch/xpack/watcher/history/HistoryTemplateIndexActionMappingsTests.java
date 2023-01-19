/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.watcher.history;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.protocol.xpack.watcher.PutWatchResponse;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.xpack.core.watcher.execution.ExecutionState;
import org.elasticsearch.xpack.core.watcher.history.HistoryStoreField;
import org.elasticsearch.xpack.watcher.test.AbstractWatcherIntegrationTestCase;

import static org.elasticsearch.index.mapper.MapperService.SINGLE_MAPPING_NAME;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;
import static org.elasticsearch.search.builder.SearchSourceBuilder.searchSource;
import static org.elasticsearch.xpack.watcher.actions.ActionBuilders.indexAction;
import static org.elasticsearch.xpack.watcher.client.WatchSourceBuilders.watchBuilder;
import static org.elasticsearch.xpack.watcher.trigger.TriggerBuilders.schedule;
import static org.elasticsearch.xpack.watcher.trigger.schedule.Schedules.interval;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * This test makes sure that the index action response `index` and `type` fields in the watch_record action result are
 * not analyzed so they can be used in aggregations
 */
public class HistoryTemplateIndexActionMappingsTests extends AbstractWatcherIntegrationTestCase {

    public void testIndexActionFields() throws Exception {
        String index = "the-index";

        PutWatchResponse putWatchResponse = watcherClient().preparePutWatch("_id").setSource(watchBuilder()
                .trigger(schedule(interval("5m")))
                .addAction("index", indexAction(index)))
                .get();

        assertThat(putWatchResponse.isCreated(), is(true));
        timeWarp().trigger("_id");
        flush();
        refresh();

        // the action should fail as no email server is available
        assertWatchWithMinimumActionsCount("_id", ExecutionState.EXECUTED, 1);
        flush();
        refresh();

        SearchResponse response = client().prepareSearch(HistoryStoreField.INDEX_PREFIX_WITH_TEMPLATE + "*").setSource(searchSource()
                .aggregation(terms("index_action_indices").field("result.actions.index.response.index"))
                .aggregation(terms("index_action_types").field("result.actions.index.response.type")))
                .get();

        assertThat(response, notNullValue());
        assertThat(response.getHits().getTotalHits().value, is(1L));
        Aggregations aggs = response.getAggregations();
        assertThat(aggs, notNullValue());

        Terms terms = aggs.get("index_action_indices");
        assertThat(terms, notNullValue());
        assertThat(terms.getBuckets().size(), is(1));
        assertThat(terms.getBucketByKey(index), notNullValue());
        assertThat(terms.getBucketByKey(index).getDocCount(), is(1L));

        terms = aggs.get("index_action_types");
        assertThat(terms, notNullValue());
        assertThat(terms.getBuckets().size(), is(1));
        assertThat(terms.getBucketByKey(SINGLE_MAPPING_NAME), notNullValue());
        assertThat(terms.getBucketByKey(SINGLE_MAPPING_NAME).getDocCount(), is(1L));
    }
}
