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

package org.elasticsearch.cluster.action.index;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.master.MasterNodeRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Setting.Property;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.UncategorizedExecutionException;
import org.elasticsearch.common.util.concurrent.RunOnce;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Mapping;

import java.util.concurrent.Semaphore;

/**
 * Called by shards in the cluster when their mapping was dynamically updated and it needs to be updated
 * in the cluster state meta data (and broadcast to all members).
 */
public class MappingUpdatedAction {

    public static final Setting<TimeValue> INDICES_MAPPING_DYNAMIC_TIMEOUT_SETTING =
        Setting.positiveTimeSetting("indices.mapping.dynamic_timeout", TimeValue.timeValueSeconds(30),
            Property.Dynamic, Property.NodeScope);

    public static final Setting<Integer> INDICES_MAX_IN_FLIGHT_UPDATES_SETTING =
        Setting.intSetting("indices.mapping.max_in_flight_updates", 10, 1, 1000,
            Property.Dynamic, Property.NodeScope);

    private IndicesAdminClient client;
    private volatile TimeValue dynamicMappingUpdateTimeout;
    private final AdjustableSemaphore semaphore;

    @Inject
    public MappingUpdatedAction(Settings settings, ClusterSettings clusterSettings) {
        this.dynamicMappingUpdateTimeout = INDICES_MAPPING_DYNAMIC_TIMEOUT_SETTING.get(settings);
        this.semaphore = new AdjustableSemaphore(INDICES_MAX_IN_FLIGHT_UPDATES_SETTING.get(settings), true);
        clusterSettings.addSettingsUpdateConsumer(INDICES_MAPPING_DYNAMIC_TIMEOUT_SETTING, this::setDynamicMappingUpdateTimeout);
        clusterSettings.addSettingsUpdateConsumer(INDICES_MAX_IN_FLIGHT_UPDATES_SETTING, this::setMaxInFlightUpdates);
    }

    private void setDynamicMappingUpdateTimeout(TimeValue dynamicMappingUpdateTimeout) {
        this.dynamicMappingUpdateTimeout = dynamicMappingUpdateTimeout;
    }

    private void setMaxInFlightUpdates(int maxInFlightUpdates) {
        semaphore.setMaxPermits(maxInFlightUpdates);
    }

    public void setClient(Client client) {
        this.client = client.admin().indices();
    }

    /**
     * Update mappings on the master node, waiting for the change to be committed,
     * but not for the mapping update to be applied on all nodes. The timeout specified by
     * {@code timeout} is the master node timeout ({@link MasterNodeRequest#masterNodeTimeout()}),
     * potentially waiting for a master node to be available.
     */
    public void updateMappingOnMaster(Index index, String type, Mapping mappingUpdate, ActionListener<Void> listener) {
        if (type.equals(MapperService.DEFAULT_MAPPING)) {
            throw new IllegalArgumentException("_default_ mapping should not be updated");
        }

        final RunOnce release = new RunOnce(() -> semaphore.release());
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            listener.onFailure(e);
            return;
        }
        boolean successFullySent = false;
        try {
            sendUpdateMapping(index, type, mappingUpdate, ActionListener.runBefore(listener, release::run));
            successFullySent = true;
        } finally {
            if (successFullySent == false) {
                release.run();
            }
        }
    }

    // used by tests
    int blockedThreads() {
        return semaphore.getQueueLength();
    }

    // can be overridden by tests
    protected void sendUpdateMapping(Index index, String type, Mapping mappingUpdate, ActionListener<Void> listener) {
        client.preparePutMapping().setConcreteIndex(index).setType(type).setSource(mappingUpdate.toString(), XContentType.JSON)
            .setMasterNodeTimeout(dynamicMappingUpdateTimeout).setTimeout(TimeValue.ZERO)
            .execute(new ActionListener<AcknowledgedResponse>() {
                @Override
                public void onResponse(AcknowledgedResponse acknowledgedResponse) {
                    listener.onResponse(null);
                }

                @Override
                public void onFailure(Exception e) {
                    listener.onFailure(unwrapException(e));
                }
            });
    }

    // todo: this explicit unwrap should not be necessary, but is until guessRootCause is fixed to allow wrapped non-es exception.
    private static Exception unwrapException(Exception cause) {
        return cause instanceof ElasticsearchException ? unwrapEsException((ElasticsearchException) cause) : cause;
    }

    private static RuntimeException unwrapEsException(ElasticsearchException esEx) {
        Throwable root = esEx.unwrapCause();
        if (root instanceof RuntimeException) {
            return (RuntimeException) root;
        }
        return new UncategorizedExecutionException("Failed execution", root);
    }

    static class AdjustableSemaphore extends Semaphore {

        private final Object maxPermitsMutex = new Object();
        private int maxPermits;

        AdjustableSemaphore(int maxPermits, boolean fair) {
            super(maxPermits, fair);
            this.maxPermits = maxPermits;
        }

        void setMaxPermits(int permits) {
            synchronized (maxPermitsMutex) {
                final int diff = Math.subtractExact(permits, maxPermits);
                if (diff > 0) {
                    // add permits
                    release(diff);
                } else if (diff < 0) {
                    // remove permits
                    reducePermits(Math.negateExact(diff));
                }

                maxPermits = permits;
            }
        }
    }
}
