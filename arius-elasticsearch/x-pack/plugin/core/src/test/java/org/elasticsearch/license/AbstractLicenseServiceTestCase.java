/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.license;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlocks;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.component.Lifecycle;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.watcher.ResourceWatcherService;
import org.elasticsearch.xpack.core.XPackPlugin;
import org.elasticsearch.xpack.core.watcher.watch.ClockMock;
import org.junit.After;
import org.junit.Before;

import java.nio.file.Path;
import java.util.Arrays;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractLicenseServiceTestCase extends ESTestCase {

    protected LicenseService licenseService;
    protected ClusterService clusterService;
    protected ResourceWatcherService resourceWatcherService;
    protected ClockMock clock;
    protected DiscoveryNodes discoveryNodes;
    protected Environment environment;
    protected String licenseType;

    @Before
    public void init() throws Exception {
        clusterService = mock(ClusterService.class);
        clock = ClockMock.frozen();
        discoveryNodes = mock(DiscoveryNodes.class);
        resourceWatcherService = mock(ResourceWatcherService.class);
        environment = mock(Environment.class);
    }

    protected void setInitialState(License license, XPackLicenseState licenseState, Settings settings) {
        setInitialState(license, licenseState, settings, randomBoolean() ? "trial" : "basic");
    }

    protected void setInitialState(License license, XPackLicenseState licenseState, Settings settings, String selfGeneratedType) {
        Path tempDir = createTempDir();
        when(environment.configFile()).thenReturn(tempDir);
        licenseType = selfGeneratedType;
        settings = Settings.builder().put(settings).put(LicenseService.SELF_GENERATED_LICENSE_TYPE.getKey(), licenseType).build();
        licenseService = new LicenseService(settings, clusterService, clock, environment, resourceWatcherService, licenseState);
        ClusterState state = mock(ClusterState.class);
        final ClusterBlocks noBlock = ClusterBlocks.builder().build();
        when(state.blocks()).thenReturn(noBlock);
        MetaData metaData = mock(MetaData.class);
        when(metaData.custom(LicensesMetaData.TYPE)).thenReturn(new LicensesMetaData(license, null));
        when(state.metaData()).thenReturn(metaData);
        final DiscoveryNode mockNode = getLocalNode();
        when(discoveryNodes.getMasterNode()).thenReturn(mockNode);
        when(discoveryNodes.spliterator()).thenReturn(Arrays.asList(mockNode).spliterator());
        when(discoveryNodes.isLocalNodeElectedMaster()).thenReturn(false);
        when(discoveryNodes.getMinNodeVersion()).thenReturn(mockNode.getVersion());
        when(state.nodes()).thenReturn(discoveryNodes);
        when(state.getNodes()).thenReturn(discoveryNodes); // it is really ridiculous we have nodes() and getNodes()...
        when(clusterService.state()).thenReturn(state);
        when(clusterService.lifecycleState()).thenReturn(Lifecycle.State.STARTED);
        when(clusterService.getClusterName()).thenReturn(new ClusterName("a"));
        when(clusterService.localNode()).thenReturn(mockNode);
    }

    protected DiscoveryNode getLocalNode() {
        return new DiscoveryNode("b", buildNewFakeTransportAddress(), singletonMap(XPackPlugin.XPACK_INSTALLED_NODE_ATTR, "true"),
            emptySet(), Version.CURRENT);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        licenseService.stop();
    }
}
