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

package org.elasticsearch.indices.recovery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Setting.Property;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

public class RecoverySettings {

    private static final Logger logger = LogManager.getLogger(RecoverySettings.class);

    public static final Setting<ByteSizeValue> INDICES_RECOVERY_MAX_BYTES_PER_SEC_SETTING =
        Setting.byteSizeSetting("indices.recovery.max_bytes_per_sec", new ByteSizeValue(40, ByteSizeUnit.MB),
            Property.Dynamic, Property.NodeScope);

    public static final Setting<ByteSizeValue> INDICES_RECOVERY_CEPH_MAX_BYTES_PER_SEC_SETTING =
        Setting.byteSizeSetting("indices.recovery.ceph_max_bytes_per_sec", new ByteSizeValue(10, ByteSizeUnit.MB),
            Property.Dynamic, Property.NodeScope);

    public static final Setting<ByteSizeValue> INDICES_RECOVERY_DISK_MAX_BYTES_PER_SEC_SETTING =
        Setting.byteSizeSetting("indices.recovery.disk.max_bytes_per_sec", new ByteSizeValue(300, ByteSizeUnit.MB),
            Property.Dynamic, Property.NodeScope);

    public static final Setting<ByteSizeValue> INDICES_RECOVERY_DISK_CEPH_MAX_BYTES_PER_SEC_SETTING =
        Setting.byteSizeSetting("indices.recovery.disk.ceph_max_bytes_per_sec", new ByteSizeValue(300, ByteSizeUnit.MB),
            Property.Dynamic, Property.NodeScope);


    /**
     * Controls the maximum number of file chunk requests that can be sent concurrently from the source node to the target node.
     */
    public static final Setting<Integer> INDICES_RECOVERY_MAX_CONCURRENT_FILE_CHUNKS_SETTING =
        Setting.intSetting("indices.recovery.max_concurrent_file_chunks", 2, 1, 5, Property.Dynamic, Property.NodeScope);

    /**
     * how long to wait before retrying after issues cause by cluster state syncing between nodes
     * i.e., local node is not yet known on remote node, remote shard not yet started etc.
     */
    public static final Setting<TimeValue> INDICES_RECOVERY_RETRY_DELAY_STATE_SYNC_SETTING =
        Setting.positiveTimeSetting("indices.recovery.retry_delay_state_sync", TimeValue.timeValueMillis(500),
            Property.Dynamic, Property.NodeScope);

    /** how long to wait before retrying after network related issues */
    public static final Setting<TimeValue> INDICES_RECOVERY_RETRY_DELAY_NETWORK_SETTING =
        Setting.positiveTimeSetting("indices.recovery.retry_delay_network", TimeValue.timeValueSeconds(5),
            Property.Dynamic, Property.NodeScope);

    /** timeout value to use for requests made as part of the recovery process */
    public static final Setting<TimeValue> INDICES_RECOVERY_INTERNAL_ACTION_TIMEOUT_SETTING =
        Setting.positiveTimeSetting("indices.recovery.internal_action_timeout", TimeValue.timeValueMinutes(15),
            Property.Dynamic, Property.NodeScope);

    /**
     * timeout value to use for requests made as part of the recovery process that are expected to take long time.
     * defaults to twice `indices.recovery.internal_action_timeout`.
     */
    public static final Setting<TimeValue> INDICES_RECOVERY_INTERNAL_LONG_ACTION_TIMEOUT_SETTING =
        Setting.timeSetting("indices.recovery.internal_action_long_timeout",
            (s) -> TimeValue.timeValueMillis(INDICES_RECOVERY_INTERNAL_ACTION_TIMEOUT_SETTING.get(s).millis() * 2),
            TimeValue.timeValueSeconds(0), Property.Dynamic,  Property.NodeScope);

    /**
     * recoveries that don't show any activity for more then this interval will be failed.
     * defaults to `indices.recovery.internal_action_long_timeout`
     */
    public static final Setting<TimeValue> INDICES_RECOVERY_ACTIVITY_TIMEOUT_SETTING =
        Setting.timeSetting("indices.recovery.recovery_activity_timeout",
            INDICES_RECOVERY_INTERNAL_LONG_ACTION_TIMEOUT_SETTING::get, TimeValue.timeValueSeconds(0),
            Property.Dynamic, Property.NodeScope);

    // choose 512KB-16B to ensure that the resulting byte[] is not a humongous allocation in G1.
    public static final ByteSizeValue DEFAULT_CHUNK_SIZE = new ByteSizeValue(512 * 1024 - 16, ByteSizeUnit.BYTES);

    private volatile int maxConcurrentFileChunks;
    private volatile ByteSizeValue maxGlobalBytesPerSec;
    private volatile ByteSizeValue maxDiskBytesPerSec;
//    private volatile SimpleRateLimiter rateLimiter;
    private volatile MulitDiskLImiter rateLimiter;
    private volatile TimeValue retryDelayStateSync;
    private volatile TimeValue retryDelayNetwork;
    private volatile TimeValue activityTimeout;
    private volatile TimeValue internalActionTimeout;
    private volatile TimeValue internalActionLongTimeout;
    private volatile Boolean isColdNode;

    private volatile ByteSizeValue chunkSize = DEFAULT_CHUNK_SIZE;

    public RecoverySettings(Settings settings, ClusterSettings clusterSettings) {
        this.retryDelayStateSync = INDICES_RECOVERY_RETRY_DELAY_STATE_SYNC_SETTING.get(settings);
        this.maxConcurrentFileChunks = INDICES_RECOVERY_MAX_CONCURRENT_FILE_CHUNKS_SETTING.get(settings);
        // doesn't have to be fast as nodes are reconnected every 10s by default (see InternalClusterService.ReconnectToNodes)
        // and we want to give the master time to remove a faulty node
        this.retryDelayNetwork = INDICES_RECOVERY_RETRY_DELAY_NETWORK_SETTING.get(settings);

        this.internalActionTimeout = INDICES_RECOVERY_INTERNAL_ACTION_TIMEOUT_SETTING.get(settings);
        this.internalActionLongTimeout = INDICES_RECOVERY_INTERNAL_LONG_ACTION_TIMEOUT_SETTING.get(settings);

        this.activityTimeout = INDICES_RECOVERY_ACTIVITY_TIMEOUT_SETTING.get(settings);

        isColdNode = DiscoveryNode.isColdNode(settings);
        if(!isColdNode) {
            this.maxGlobalBytesPerSec = INDICES_RECOVERY_MAX_BYTES_PER_SEC_SETTING.get(settings);
            this.maxDiskBytesPerSec = INDICES_RECOVERY_DISK_MAX_BYTES_PER_SEC_SETTING.get(settings);
        } else {
            this.maxGlobalBytesPerSec = INDICES_RECOVERY_CEPH_MAX_BYTES_PER_SEC_SETTING.get(settings);
            this.maxDiskBytesPerSec = INDICES_RECOVERY_DISK_CEPH_MAX_BYTES_PER_SEC_SETTING.get(settings);
        }

        if (maxGlobalBytesPerSec.getBytes()<=0 || maxDiskBytesPerSec.getBytes()<=0) {
            rateLimiter = null;
        } else {
            rateLimiter = new MulitDiskLImiter(maxGlobalBytesPerSec.getMbFrac(), maxDiskBytesPerSec.getMbFrac());
        }
        logger.debug("using maxGlobalBytesPerSec[{}], maxDiskBytesPerSec[{}]", maxGlobalBytesPerSec, maxDiskBytesPerSec);

        clusterSettings.addSettingsUpdateConsumer(INDICES_RECOVERY_MAX_BYTES_PER_SEC_SETTING, this::setSSDMaxBytesPerSec);
        clusterSettings.addSettingsUpdateConsumer(INDICES_RECOVERY_CEPH_MAX_BYTES_PER_SEC_SETTING, this::setCephMaxBytesPerSec);
        clusterSettings.addSettingsUpdateConsumer(INDICES_RECOVERY_MAX_CONCURRENT_FILE_CHUNKS_SETTING, this::setMaxConcurrentFileChunks);
        clusterSettings.addSettingsUpdateConsumer(INDICES_RECOVERY_DISK_MAX_BYTES_PER_SEC_SETTING, this::setDiskSSDMaxBytesPerSec);
        clusterSettings.addSettingsUpdateConsumer(INDICES_RECOVERY_DISK_CEPH_MAX_BYTES_PER_SEC_SETTING, this::setDiskCephMaxBytesPerSec);
        clusterSettings.addSettingsUpdateConsumer(INDICES_RECOVERY_RETRY_DELAY_STATE_SYNC_SETTING, this::setRetryDelayStateSync);
        clusterSettings.addSettingsUpdateConsumer(INDICES_RECOVERY_RETRY_DELAY_NETWORK_SETTING, this::setRetryDelayNetwork);
        clusterSettings.addSettingsUpdateConsumer(INDICES_RECOVERY_INTERNAL_ACTION_TIMEOUT_SETTING, this::setInternalActionTimeout);
        clusterSettings.addSettingsUpdateConsumer(INDICES_RECOVERY_INTERNAL_LONG_ACTION_TIMEOUT_SETTING,
            this::setInternalActionLongTimeout);
        clusterSettings.addSettingsUpdateConsumer(INDICES_RECOVERY_ACTIVITY_TIMEOUT_SETTING, this::setActivityTimeout);
    }

    public MulitDiskLImiter rateLimiter() {
        return rateLimiter;
    }

    public TimeValue retryDelayNetwork() {
        return retryDelayNetwork;
    }

    public TimeValue retryDelayStateSync() {
        return retryDelayStateSync;
    }

    public TimeValue activityTimeout() {
        return activityTimeout;
    }

    public TimeValue internalActionTimeout() {
        return internalActionTimeout;
    }

    public TimeValue internalActionLongTimeout() {
        return internalActionLongTimeout;
    }

    public ByteSizeValue getChunkSize() { return chunkSize; }

    public void setChunkSize(ByteSizeValue chunkSize) { // only settable for tests
        if (chunkSize.bytesAsInt() <= 0) {
            throw new IllegalArgumentException("chunkSize must be > 0");
        }
        this.chunkSize = chunkSize;
    }


    public void setRetryDelayStateSync(TimeValue retryDelayStateSync) {
        this.retryDelayStateSync = retryDelayStateSync;
    }

    public void setRetryDelayNetwork(TimeValue retryDelayNetwork) {
        this.retryDelayNetwork = retryDelayNetwork;
    }

    public void setActivityTimeout(TimeValue activityTimeout) {
        this.activityTimeout = activityTimeout;
    }

    public void setInternalActionTimeout(TimeValue internalActionTimeout) {
        this.internalActionTimeout = internalActionTimeout;
    }

    public void setInternalActionLongTimeout(TimeValue internalActionLongTimeout) {
        this.internalActionLongTimeout = internalActionLongTimeout;
    }

    private void setCephMaxBytesPerSec(ByteSizeValue maxBytesPerSec) {
        if(!isColdNode) {
            return;
        }

        setMaxBytesPerSec(maxBytesPerSec, maxDiskBytesPerSec);
    }

    private void setSSDMaxBytesPerSec(ByteSizeValue maxBytesPerSec) {
        if(isColdNode) {
            return;
        }

        setMaxBytesPerSec(maxBytesPerSec, maxDiskBytesPerSec);
    }


    private void setDiskCephMaxBytesPerSec(ByteSizeValue maxBytesPerSec) {
        if(!isColdNode) {
            return;
        }

        setMaxBytesPerSec(maxGlobalBytesPerSec, maxBytesPerSec);
    }

    private void setDiskSSDMaxBytesPerSec(ByteSizeValue maxBytesPerSec) {
        if(isColdNode) {
            return;
        }

        setMaxBytesPerSec(maxGlobalBytesPerSec, maxBytesPerSec);
    }

    private void setMaxBytesPerSec(ByteSizeValue maxGlobalBytesPerSec, ByteSizeValue maxDiskBytesPerSec) {
        this.maxGlobalBytesPerSec = maxGlobalBytesPerSec;
        this.maxDiskBytesPerSec = maxDiskBytesPerSec;

        if (maxGlobalBytesPerSec.getBytes() <= 0 || maxDiskBytesPerSec.getBytes()<=0) {
            rateLimiter = null;
        } else if (rateLimiter != null) {
            rateLimiter.setGlobalMBPerSec(maxGlobalBytesPerSec.getMbFrac());
            rateLimiter.setDiskMBPerSec(maxDiskBytesPerSec.getMbFrac());
        } else {
            rateLimiter = new MulitDiskLImiter(maxGlobalBytesPerSec.getMbFrac(), maxDiskBytesPerSec.getMbFrac());
        }
    }

    public int getMaxConcurrentFileChunks() {
        return maxConcurrentFileChunks;
    }

    private void setMaxConcurrentFileChunks(int maxConcurrentFileChunks) {
        this.maxConcurrentFileChunks = maxConcurrentFileChunks;
    }
}
