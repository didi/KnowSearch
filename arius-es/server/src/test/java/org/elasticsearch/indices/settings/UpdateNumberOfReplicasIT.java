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

package org.elasticsearch.indices.settings;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.test.ESIntegTestCase;

import java.util.EnumSet;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertHitCount;
import static org.hamcrest.Matchers.equalTo;

@ESIntegTestCase.ClusterScope(minNumDataNodes = 2)
public class UpdateNumberOfReplicasIT extends ESIntegTestCase {
    @Override
    protected int maximumNumberOfReplicas() {
        return 1;
    }

    public void testSimpleUpdateNumberOfReplicas() throws Exception {
        logger.info("Creating index test");
        assertAcked(prepareCreate("test", 2));
        logger.info("Running Cluster Health");
        ClusterHealthResponse clusterHealth = client().admin().cluster().prepareHealth()
            .setWaitForEvents(Priority.LANGUID).setWaitForGreenStatus().execute().actionGet();
        logger.info("Done Cluster Health, status {}", clusterHealth.getStatus());

        NumShards numShards = getNumShards("test");

        assertThat(clusterHealth.isTimedOut(), equalTo(false));
        assertThat(clusterHealth.getStatus(), equalTo(ClusterHealthStatus.GREEN));
        assertThat(clusterHealth.getIndices().get("test").getActivePrimaryShards(), equalTo(numShards.numPrimaries));
        assertThat(clusterHealth.getIndices().get("test").getNumberOfReplicas(), equalTo(numShards.numReplicas));
        assertThat(clusterHealth.getIndices().get("test").getActiveShards(), equalTo(numShards.totalNumShards));

        for (int i = 0; i < 10; i++) {
            client().prepareIndex("test", "type1", Integer.toString(i)).setSource(jsonBuilder().startObject()
                    .field("value", "test" + i)
                    .endObject()).get();
        }

        refresh();

        for (int i = 0; i < 10; i++) {
            SearchResponse countResponse = client().prepareSearch().setSize(0).setQuery(matchAllQuery()).get();
            assertHitCount(countResponse, 10L);
        }

        final long settingsVersion =
                client().admin().cluster().prepareState().get().getState().metaData().index("test").getSettingsVersion();
        logger.info("Increasing the number of replicas from 1 to 2");
        assertAcked(client().admin().indices().prepareUpdateSettings("test")
            .setSettings(Settings.builder().put("index.number_of_replicas", 2)).execute().actionGet());
        logger.info("Running Cluster Health");
        clusterHealth = client().admin().cluster().prepareHealth().setWaitForEvents(Priority.LANGUID).setWaitForYellowStatus()
            .setWaitForActiveShards(numShards.numPrimaries * 2).execute().actionGet();
        logger.info("Done Cluster Health, status {}", clusterHealth.getStatus());
        assertThat(clusterHealth.isTimedOut(), equalTo(false));
        assertThat(clusterHealth.getStatus(), equalTo(ClusterHealthStatus.YELLOW));
        assertThat(clusterHealth.getIndices().get("test").getActivePrimaryShards(), equalTo(numShards.numPrimaries));
        assertThat(clusterHealth.getIndices().get("test").getNumberOfReplicas(), equalTo(2));
        //only 2 copies allocated (1 replica) across 2 nodes
        assertThat(clusterHealth.getIndices().get("test").getActiveShards(), equalTo(numShards.numPrimaries * 2));

        final long afterReplicaIncreaseSettingsVersion =
                client().admin().cluster().prepareState().get().getState().metaData().index("test").getSettingsVersion();
        assertThat(afterReplicaIncreaseSettingsVersion, equalTo(1 + settingsVersion));

        logger.info("starting another node to new replicas will be allocated to it");
        allowNodes("test", 3);

        final long afterStartingAnotherNodeVersion =
                client().admin().cluster().prepareState().get().getState().metaData().index("test").getSettingsVersion();

        logger.info("Running Cluster Health");
        clusterHealth = client().admin().cluster().prepareHealth().setWaitForEvents(Priority.LANGUID).setWaitForGreenStatus()
            .setWaitForNoRelocatingShards(true).setWaitForNodes(">=3").execute().actionGet();
        logger.info("Done Cluster Health, status {}", clusterHealth.getStatus());
        assertThat(clusterHealth.isTimedOut(), equalTo(false));
        assertThat(clusterHealth.getStatus(), equalTo(ClusterHealthStatus.GREEN));
        assertThat(clusterHealth.getIndices().get("test").getActivePrimaryShards(), equalTo(numShards.numPrimaries));
        assertThat(clusterHealth.getIndices().get("test").getNumberOfReplicas(), equalTo(2));
        //all 3 copies allocated across 3 nodes
        assertThat(clusterHealth.getIndices().get("test").getActiveShards(), equalTo(numShards.numPrimaries * 3));

        for (int i = 0; i < 10; i++) {
            SearchResponse countResponse = client().prepareSearch().setSize(0).setQuery(matchAllQuery()).get();
            assertHitCount(countResponse, 10L);
        }

        logger.info("Decreasing number of replicas from 2 to 0");
        assertAcked(client().admin().indices().prepareUpdateSettings("test").
            setSettings(Settings.builder().put("index.number_of_replicas", 0)).get());

        logger.info("Running Cluster Health");
        clusterHealth = client().admin().cluster().prepareHealth().setWaitForEvents(Priority.LANGUID)
            .setWaitForGreenStatus().setWaitForNoRelocatingShards(true).setWaitForNodes(">=3").execute().actionGet();
        logger.info("Done Cluster Health, status {}", clusterHealth.getStatus());
        assertThat(clusterHealth.isTimedOut(), equalTo(false));
        assertThat(clusterHealth.getStatus(), equalTo(ClusterHealthStatus.GREEN));
        assertThat(clusterHealth.getIndices().get("test").getActivePrimaryShards(), equalTo(numShards.numPrimaries));
        assertThat(clusterHealth.getIndices().get("test").getNumberOfReplicas(), equalTo(0));
        //a single copy is allocated (replica set to 0)
        assertThat(clusterHealth.getIndices().get("test").getActiveShards(), equalTo(numShards.numPrimaries));

        for (int i = 0; i < 10; i++) {
            assertHitCount(client().prepareSearch().setQuery(matchAllQuery()).get(), 10);
        }

        final long afterReplicaDecreaseSettingsVersion =
                client().admin().cluster().prepareState().get().getState().metaData().index("test").getSettingsVersion();
        assertThat(afterReplicaDecreaseSettingsVersion, equalTo(1 + afterStartingAnotherNodeVersion));
    }

    public void testUpdateWithInvalidNumberOfReplicas() {
        createIndex("test");
        final long settingsVersion =
                client().admin().cluster().prepareState().get().getState().metaData().index("test").getSettingsVersion();
        final int value = randomIntBetween(-10, -1);
        try {
            client().admin().indices().prepareUpdateSettings("test")
                .setSettings(Settings.builder()
                        .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, value)
                )
                .execute().actionGet();
            fail("should have thrown an exception about the replica shard count");
        } catch (IllegalArgumentException e) {
            assertEquals("Failed to parse value [" + value + "] for setting [index.number_of_replicas] must be >= 0", e.getMessage());
            assertThat(
                    client().admin().cluster().prepareState().get().getState().metaData().index("test").getSettingsVersion(),
                    equalTo(settingsVersion));
        }
    }

    public void testUpdateNumberOfReplicasAllowNoIndices() {
        createIndex("test-index", Settings.builder().put("index.number_of_replicas", 0).build());
        final IndicesOptions options =
                new IndicesOptions(EnumSet.of(IndicesOptions.Option.ALLOW_NO_INDICES), EnumSet.of(IndicesOptions.WildcardStates.OPEN));
        assertAcked(client()
                .admin()
                .indices()
                .prepareUpdateSettings("non-existent-*")
                .setSettings(Settings.builder().put("index.number_of_replicas", 1))
                .setIndicesOptions(options)
                .get());
        final int numberOfReplicas = Integer.parseInt(
                client().admin().indices().prepareGetSettings("test-index").get().getSetting("test-index", "index.number_of_replicas"));
        assertThat(numberOfReplicas, equalTo(0));
    }

}
