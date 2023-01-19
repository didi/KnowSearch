/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.security.transport.nio;

import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.MockPageCacheRecycler;
import org.elasticsearch.indices.breaker.NoneCircuitBreakerService;
import org.elasticsearch.transport.ConnectionProfile;
import org.elasticsearch.transport.TcpChannel;
import org.elasticsearch.transport.Transport;
import org.elasticsearch.transport.nio.NioGroupFactory;
import org.elasticsearch.xpack.security.transport.AbstractSimpleSecurityTransportTestCase;

import java.util.Collections;

public class SimpleSecurityNioTransportTests extends AbstractSimpleSecurityTransportTestCase {

    @Override
    protected Transport build(Settings settings, final Version version, ClusterSettings clusterSettings, boolean doHandshake) {
        NamedWriteableRegistry namedWriteableRegistry = new NamedWriteableRegistry(Collections.emptyList());
        NetworkService networkService = new NetworkService(Collections.emptyList());
        Settings settings1 = Settings.builder()
                .put(settings)
                .put("xpack.security.transport.ssl.enabled", true).build();
        return new SecurityNioTransport(settings1, version, threadPool, networkService, new MockPageCacheRecycler(settings),
            namedWriteableRegistry, new NoneCircuitBreakerService(), null, createSSLService(settings1),
            new NioGroupFactory(settings, logger)) {

            @Override
            public void executeHandshake(DiscoveryNode node, TcpChannel channel, ConnectionProfile profile,
                                         ActionListener<Version> listener) {
                if (doHandshake) {
                    super.executeHandshake(node, channel, profile, listener);
                } else {
                    listener.onResponse(version.minimumCompatibilityVersion());
                }
            }
        };
    }
}
