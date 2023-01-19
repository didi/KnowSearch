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

package org.elasticsearch.env;

import org.elasticsearch.Version;
import org.elasticsearch.common.CheckedConsumer;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.gateway.PersistedClusterStateService;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.node.Node;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.InternalTestCluster;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;

@ESIntegTestCase.ClusterScope(scope = ESIntegTestCase.Scope.TEST, numDataNodes = 0)
public class NodeEnvironmentIT extends ESIntegTestCase {
    public void testStartFailureOnDataForNonDataNode() throws Exception {
        final String indexName = "test-fail-on-data";

        logger.info("--> starting one node");
        final boolean writeDanglingIndices = randomBoolean();
        String node = internalCluster().startNode(Settings.builder()
            .put(IndicesService.WRITE_DANGLING_INDICES_INFO_SETTING.getKey(), writeDanglingIndices).build());
        Settings dataPathSettings = internalCluster().dataPathSettings(node);

        logger.info("--> creating index");
        prepareCreate(indexName, Settings.builder()
            .put("index.number_of_shards", 1)
            .put("index.number_of_replicas", 0)
        ).get();
        final String indexUUID = resolveIndex(indexName).getUUID();
        if (writeDanglingIndices) {
            assertBusy(() -> internalCluster().getInstances(IndicesService.class).forEach(
                indicesService -> assertTrue(indicesService.allPendingDanglingIndicesWritten())));
        }

        logger.info("--> restarting the node with node.data=false and node.master=false");
        IllegalStateException ex = expectThrows(IllegalStateException.class,
            "Node started with node.data=false and node.master=false while having existing index metadata must fail",
            () ->
                internalCluster().restartRandomDataNode(new InternalTestCluster.RestartCallback() {
                    @Override
                    public Settings onNodeStopped(String nodeName) {
                        return Settings.builder()
                            .put(Node.NODE_DATA_SETTING.getKey(), false)
                            .put(Node.NODE_MASTER_SETTING.getKey(), false)
                            .build();
                    }
                }));
        if (writeDanglingIndices) {
            assertThat(ex.getMessage(),
                startsWith("Node is started with "
                    + Node.NODE_DATA_SETTING.getKey()
                    + "=false and "
                    + Node.NODE_MASTER_SETTING.getKey()
                    + "=false, but has index metadata"));
        } else {
            assertThat(ex.getMessage(),
                startsWith("Node is started with "
                    + Node.NODE_DATA_SETTING.getKey()
                    + "=false, but has shard data"));
        }

        logger.info("--> start the node again with node.data=true and node.master=true");
        internalCluster().startNode(dataPathSettings);

        logger.info("--> indexing a simple document");
        client().prepareIndex(indexName, "type1", "1").setSource("field1", "value1").get();

        logger.info("--> restarting the node with node.data=false");
        ex = expectThrows(IllegalStateException.class,
            "Node started with node.data=false while having existing shard data must fail",
            () ->
                internalCluster().restartRandomDataNode(new InternalTestCluster.RestartCallback() {
                    @Override
                    public Settings onNodeStopped(String nodeName) {
                        return Settings.builder().put(Node.NODE_DATA_SETTING.getKey(), false).build();
                    }
                }));
        assertThat(ex.getMessage(), containsString(indexUUID));
        assertThat(ex.getMessage(),
            startsWith("Node is started with "
                + Node.NODE_DATA_SETTING.getKey()
                + "=false, but has shard data"));
    }

    private IllegalStateException expectThrowsOnRestart(CheckedConsumer<Path[], Exception> onNodeStopped) {
        internalCluster().startNode();
        final Path[] dataPaths = internalCluster().getInstance(NodeEnvironment.class).nodeDataPaths();
        return expectThrows(IllegalStateException.class,
            () -> internalCluster().restartRandomDataNode(new InternalTestCluster.RestartCallback() {
                @Override
                public Settings onNodeStopped(String nodeName) {
                    try {
                        onNodeStopped.accept(dataPaths);
                    } catch (Exception e) {
                        throw new AssertionError(e);
                    }
                    return Settings.EMPTY;
                }
            }));
    }

    public void testFailsToStartIfDowngraded() {
        final IllegalStateException illegalStateException = expectThrowsOnRestart(dataPaths ->
            PersistedClusterStateService.overrideVersion(NodeMetaDataTests.tooNewVersion(), dataPaths));
        assertThat(illegalStateException.getMessage(),
            allOf(startsWith("cannot downgrade a node from version ["), endsWith("] to version [" + Version.CURRENT + "]")));
    }

    public void testFailsToStartIfUpgradedTooFar() {
        final IllegalStateException illegalStateException = expectThrowsOnRestart(dataPaths ->
            PersistedClusterStateService.overrideVersion(NodeMetaDataTests.tooOldVersion(), dataPaths));
        assertThat(illegalStateException.getMessage(),
            allOf(startsWith("cannot upgrade a node from version ["), endsWith("] directly to version [" + Version.CURRENT + "]")));
    }

    public void testFailsToStartOnDataPathsFromMultipleNodes() throws IOException {
        final List<String> nodes = internalCluster().startNodes(2);
        ensureStableCluster(2);

        final List<String> node0DataPaths = Environment.PATH_DATA_SETTING.get(internalCluster().dataPathSettings(nodes.get(0)));
        final List<String> node1DataPaths = Environment.PATH_DATA_SETTING.get(internalCluster().dataPathSettings(nodes.get(1)));

        final List<String> allDataPaths = new ArrayList<>(node0DataPaths);
        allDataPaths.addAll(node1DataPaths);

        internalCluster().stopRandomNode(InternalTestCluster.nameFilter(nodes.get(1)));
        internalCluster().stopRandomNode(InternalTestCluster.nameFilter(nodes.get(0)));

        IllegalStateException illegalStateException = expectThrows(IllegalStateException.class,
            () -> PersistedClusterStateService.nodeMetaData(allDataPaths.stream().map(PathUtils::get)
                .map(path -> NodeEnvironment.resolveNodePath(path, 0)).toArray(Path[]::new)));

        assertThat(illegalStateException.getMessage(), containsString("unexpected node ID in metadata"));

        illegalStateException = expectThrows(IllegalStateException.class,
            () -> internalCluster().startNode(Settings.builder().putList(Environment.PATH_DATA_SETTING.getKey(), allDataPaths)));

        assertThat(illegalStateException.getMessage(), containsString("unexpected node ID in metadata"));

        final List<String> node0DataPathsPlusOne = new ArrayList<>(node0DataPaths);
        node0DataPathsPlusOne.add(createTempDir().toString());
        internalCluster().startNode(Settings.builder().putList(Environment.PATH_DATA_SETTING.getKey(), node0DataPathsPlusOne));

        final List<String> node1DataPathsPlusOne = new ArrayList<>(node1DataPaths);
        node1DataPathsPlusOne.add(createTempDir().toString());
        internalCluster().startNode(Settings.builder().putList(Environment.PATH_DATA_SETTING.getKey(), node1DataPathsPlusOne));

        ensureStableCluster(2);
    }
}
