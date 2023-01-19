/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.watcher.test.integration;

import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.protocol.xpack.watcher.PutWatchResponse;
import org.elasticsearch.test.ESIntegTestCase.ClusterScope;
import org.elasticsearch.xpack.core.watcher.watch.Watch;
import org.elasticsearch.xpack.watcher.test.AbstractWatcherIntegrationTestCase;
import org.elasticsearch.xpack.watcher.trigger.schedule.IntervalSchedule;
import org.elasticsearch.xpack.watcher.watch.WatchStoreUtils;

import java.util.concurrent.TimeUnit;

import static org.elasticsearch.test.ESIntegTestCase.Scope.SUITE;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.elasticsearch.xpack.watcher.actions.ActionBuilders.loggingAction;
import static org.elasticsearch.xpack.watcher.client.WatchSourceBuilders.watchBuilder;
import static org.elasticsearch.xpack.watcher.input.InputBuilders.simpleInput;
import static org.elasticsearch.xpack.watcher.trigger.TriggerBuilders.schedule;
import static org.elasticsearch.xpack.watcher.trigger.schedule.Schedules.interval;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

@LuceneTestCase.AwaitsFix(bugUrl = "https://github.com/elastic/elasticsearch/issues/36782")
@ClusterScope(scope = SUITE, numClientNodes = 0, transportClientRatio = 0, maxNumDataNodes = 1, supportsDedicatedMasters = false)
public class SingleNodeTests extends AbstractWatcherIntegrationTestCase {

    @Override
    protected boolean timeWarped() {
        return false;
    }

    // this is the standard setup when starting watcher in a regular cluster
    // the index does not exist, a watch gets added
    // the watch should be executed properly, despite the index being created and the cluster state listener being reloaded
    public void testThatLoadingWithNonExistingIndexWorks() throws Exception {
        stopWatcher();
        ClusterStateResponse clusterStateResponse = client().admin().cluster().prepareState().get();
        IndexMetaData metaData = WatchStoreUtils.getConcreteIndex(Watch.INDEX, clusterStateResponse.getState().metaData());
        String watchIndexName = metaData.getIndex().getName();
        assertAcked(client().admin().indices().prepareDelete(watchIndexName));
        startWatcher();

        String watchId = randomAlphaOfLength(20);
        // now we start with an empty set up, store a watch and expected it to be executed
        PutWatchResponse putWatchResponse = watcherClient().preparePutWatch(watchId)
            .setSource(watchBuilder()
                .trigger(schedule(interval(1, IntervalSchedule.Interval.Unit.SECONDS)))
                .input(simpleInput())
                .addAction("_logger", loggingAction("logging of watch _name")))
            .get();
        assertThat(putWatchResponse.isCreated(), is(true));

        assertBusy(() -> {
            client().admin().indices().prepareRefresh(".watcher-history*");
            SearchResponse searchResponse = client().prepareSearch(".watcher-history*").setSize(0).get();
            assertThat(searchResponse.getHits().getTotalHits().value, is(greaterThanOrEqualTo(1L)));
        }, 5, TimeUnit.SECONDS);
    }

}
