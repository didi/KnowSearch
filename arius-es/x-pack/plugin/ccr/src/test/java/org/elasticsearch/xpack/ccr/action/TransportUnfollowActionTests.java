/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.ccr.action;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.persistent.PersistentTasksCustomMetaData;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.xpack.ccr.Ccr;
import org.elasticsearch.xpack.ccr.CcrSettings;

import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class TransportUnfollowActionTests extends ESTestCase {

    public void testUnfollow() {
        final long settingsVersion = randomNonNegativeLong();
        IndexMetaData.Builder followerIndex = IndexMetaData.builder("follow_index")
            .settings(settings(Version.CURRENT).put(CcrSettings.CCR_FOLLOWING_INDEX_SETTING.getKey(), true))
            .settingsVersion(settingsVersion)
            .numberOfShards(1)
            .numberOfReplicas(0)
            .state(IndexMetaData.State.CLOSE)
            .putCustom(Ccr.CCR_CUSTOM_METADATA_KEY, new HashMap<>());

        ClusterState current = ClusterState.builder(new ClusterName("cluster_name"))
            .metaData(MetaData.builder()
                .put(followerIndex)
                .build())
            .build();
        ClusterState result = TransportUnfollowAction.unfollow("follow_index", current);

        IndexMetaData resultIMD = result.metaData().index("follow_index");
        assertThat(resultIMD.getSettings().get(CcrSettings.CCR_FOLLOWING_INDEX_SETTING.getKey()), nullValue());
        assertThat(resultIMD.getCustomData(Ccr.CCR_CUSTOM_METADATA_KEY), nullValue());
        assertThat(resultIMD.getSettingsVersion(), equalTo(settingsVersion + 1));
    }

    public void testUnfollowIndexOpen() {
        IndexMetaData.Builder followerIndex = IndexMetaData.builder("follow_index")
            .settings(settings(Version.CURRENT).put(CcrSettings.CCR_FOLLOWING_INDEX_SETTING.getKey(), true))
            .numberOfShards(1)
            .numberOfReplicas(0)
            .putCustom(Ccr.CCR_CUSTOM_METADATA_KEY, new HashMap<>());

        ClusterState current = ClusterState.builder(new ClusterName("cluster_name"))
            .metaData(MetaData.builder()
                .put(followerIndex)
                .build())
            .build();
        Exception e = expectThrows(IllegalArgumentException.class, () -> TransportUnfollowAction.unfollow("follow_index", current));
        assertThat(e.getMessage(),
            equalTo("cannot convert the follower index [follow_index] to a non-follower, because it has not been closed"));
    }

    public void testUnfollowRunningShardFollowTasks() {
        IndexMetaData.Builder followerIndex = IndexMetaData.builder("follow_index")
            .settings(settings(Version.CURRENT).put(CcrSettings.CCR_FOLLOWING_INDEX_SETTING.getKey(), true))
            .numberOfShards(1)
            .numberOfReplicas(0)
            .state(IndexMetaData.State.CLOSE)
            .putCustom(Ccr.CCR_CUSTOM_METADATA_KEY, new HashMap<>());


        ShardFollowTask params = new ShardFollowTask(
            null,
            new ShardId("follow_index", "", 0),
            new ShardId("leader_index", "", 0),
            1024,
            1024,
            1,
            1,
            TransportResumeFollowAction.DEFAULT_MAX_READ_REQUEST_SIZE,
            TransportResumeFollowAction.DEFAULT_MAX_READ_REQUEST_SIZE,
            10240,
            new ByteSizeValue(512, ByteSizeUnit.MB),
            TimeValue.timeValueMillis(10),
            TimeValue.timeValueMillis(10),
            Collections.emptyMap()
        );
        PersistentTasksCustomMetaData.PersistentTask<?> task =
            new PersistentTasksCustomMetaData.PersistentTask<>("id", ShardFollowTask.NAME, params, 0, null);

        ClusterState current = ClusterState.builder(new ClusterName("cluster_name"))
            .metaData(MetaData.builder()
                .put(followerIndex)
                .putCustom(PersistentTasksCustomMetaData.TYPE, new PersistentTasksCustomMetaData(0, Collections.singletonMap("id", task)))
                .build())
            .build();
        Exception e = expectThrows(IllegalArgumentException.class, () -> TransportUnfollowAction.unfollow("follow_index", current));
        assertThat(e.getMessage(),
            equalTo("cannot convert the follower index [follow_index] to a non-follower, because it has not been paused"));
    }

    public void testUnfollowMissingIndex() {
        IndexMetaData.Builder followerIndex = IndexMetaData.builder("follow_index")
            .settings(settings(Version.CURRENT).put(CcrSettings.CCR_FOLLOWING_INDEX_SETTING.getKey(), true))
            .numberOfShards(1)
            .numberOfReplicas(0)
            .state(IndexMetaData.State.CLOSE)
            .putCustom(Ccr.CCR_CUSTOM_METADATA_KEY, new HashMap<>());

        ClusterState current = ClusterState.builder(new ClusterName("cluster_name"))
            .metaData(MetaData.builder()
                .put(followerIndex)
                .build())
            .build();
        expectThrows(IndexNotFoundException.class, () -> TransportUnfollowAction.unfollow("another_index", current));
    }

    public void testUnfollowNoneFollowIndex() {
        IndexMetaData.Builder followerIndex = IndexMetaData.builder("follow_index")
            .settings(settings(Version.CURRENT).put(CcrSettings.CCR_FOLLOWING_INDEX_SETTING.getKey(), true))
            .numberOfShards(1)
            .numberOfReplicas(0)
            .state(IndexMetaData.State.CLOSE);

        ClusterState current = ClusterState.builder(new ClusterName("cluster_name"))
            .metaData(MetaData.builder()
                .put(followerIndex)
                .build())
            .build();
        expectThrows(IllegalArgumentException.class, () -> TransportUnfollowAction.unfollow("follow_index", current));
    }

}
