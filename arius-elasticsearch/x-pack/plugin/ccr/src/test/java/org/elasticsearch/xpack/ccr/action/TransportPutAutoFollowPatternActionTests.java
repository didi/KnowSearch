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
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.xpack.core.ccr.AutoFollowMetadata;
import org.elasticsearch.xpack.core.ccr.AutoFollowMetadata.AutoFollowPattern;
import org.elasticsearch.xpack.core.ccr.action.PutAutoFollowPatternAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class TransportPutAutoFollowPatternActionTests extends ESTestCase {

    public void testInnerPut() {
        PutAutoFollowPatternAction.Request request = new PutAutoFollowPatternAction.Request();
        request.setName("name1");
        request.setRemoteCluster("eu_cluster");
        request.setLeaderIndexPatterns(Collections.singletonList("logs-*"));

        ClusterState localState = ClusterState.builder(new ClusterName("us_cluster"))
            .metaData(MetaData.builder())
            .build();

        ClusterState remoteState = ClusterState.builder(new ClusterName("eu_cluster"))
            .metaData(MetaData.builder())
            .build();

        ClusterState result = TransportPutAutoFollowPatternAction.innerPut(request, null, localState, remoteState);
        AutoFollowMetadata autoFollowMetadata = result.metaData().custom(AutoFollowMetadata.TYPE);
        assertThat(autoFollowMetadata, notNullValue());
        assertThat(autoFollowMetadata.getPatterns().size(), equalTo(1));
        assertThat(autoFollowMetadata.getPatterns().get("name1").getRemoteCluster(), equalTo("eu_cluster"));
        assertThat(autoFollowMetadata.getPatterns().get("name1").getLeaderIndexPatterns().size(), equalTo(1));
        assertThat(autoFollowMetadata.getPatterns().get("name1").getLeaderIndexPatterns().get(0), equalTo("logs-*"));
        assertThat(autoFollowMetadata.getFollowedLeaderIndexUUIDs().size(), equalTo(1));
        assertThat(autoFollowMetadata.getFollowedLeaderIndexUUIDs().get("name1").size(), equalTo(0));
    }

    public void testInnerPut_existingLeaderIndices() {
        PutAutoFollowPatternAction.Request request = new PutAutoFollowPatternAction.Request();
        request.setName("name1");
        request.setRemoteCluster("eu_cluster");
        request.setLeaderIndexPatterns(Collections.singletonList("logs-*"));

        ClusterState localState = ClusterState.builder(new ClusterName("us_cluster"))
            .metaData(MetaData.builder())
            .build();

        int numLeaderIndices = randomIntBetween(1, 8);
        int numMatchingLeaderIndices = randomIntBetween(1, 8);
        MetaData.Builder mdBuilder = MetaData.builder();
        for (int i = 0; i < numLeaderIndices; i++) {
            mdBuilder.put(IndexMetaData.builder("transactions-" + i)
                .settings(settings(Version.CURRENT))
                .numberOfShards(1)
                .numberOfReplicas(0));
        }
        for (int i = 0; i < numMatchingLeaderIndices; i++) {
            mdBuilder.put(IndexMetaData.builder("logs-" + i)
                .settings(settings(Version.CURRENT))
                .numberOfShards(1)
                .numberOfReplicas(0));
        }

        ClusterState remoteState = ClusterState.builder(new ClusterName("eu_cluster"))
            .metaData(mdBuilder)
            .build();

        ClusterState result = TransportPutAutoFollowPatternAction.innerPut(request, null, localState, remoteState);
        AutoFollowMetadata autoFollowMetadata = result.metaData().custom(AutoFollowMetadata.TYPE);
        assertThat(autoFollowMetadata, notNullValue());
        assertThat(autoFollowMetadata.getPatterns().size(), equalTo(1));
        assertThat(autoFollowMetadata.getPatterns().get("name1").getRemoteCluster(), equalTo("eu_cluster"));
        assertThat(autoFollowMetadata.getPatterns().get("name1").getLeaderIndexPatterns().size(), equalTo(1));
        assertThat(autoFollowMetadata.getPatterns().get("name1").getLeaderIndexPatterns().get(0), equalTo("logs-*"));
        assertThat(autoFollowMetadata.getFollowedLeaderIndexUUIDs().size(), equalTo(1));
        assertThat(autoFollowMetadata.getFollowedLeaderIndexUUIDs().get("name1").size(), equalTo(numMatchingLeaderIndices));
    }

    public void testInnerPut_existingLeaderIndicesAndAutoFollowMetadata() {
        PutAutoFollowPatternAction.Request request = new PutAutoFollowPatternAction.Request();
        request.setName("name1");
        request.setRemoteCluster("eu_cluster");
        request.setLeaderIndexPatterns(Arrays.asList("logs-*", "transactions-*"));

        Map<String, AutoFollowPattern> existingAutoFollowPatterns = new HashMap<>();
        List<String> existingPatterns = new ArrayList<>();
        existingPatterns.add("transactions-*");
        existingAutoFollowPatterns.put("name1",
            new AutoFollowPattern("eu_cluster", existingPatterns, null, true, null, null, null, null, null, null, null, null, null, null));
        Map<String, List<String>> existingAlreadyFollowedIndexUUIDS = new HashMap<>();
        List<String> existingUUIDS = new ArrayList<>();
        existingUUIDS.add("_val");
        existingAlreadyFollowedIndexUUIDS.put("name1", existingUUIDS);
        Map<String, Map<String, String>> existingHeaders = new HashMap<>();
        existingHeaders.put("name1", Collections.singletonMap("key", "val"));

        ClusterState localState = ClusterState.builder(new ClusterName("us_cluster"))
            .metaData(MetaData.builder().putCustom(AutoFollowMetadata.TYPE,
                new AutoFollowMetadata(existingAutoFollowPatterns, existingAlreadyFollowedIndexUUIDS, existingHeaders)))
            .build();

        int numLeaderIndices = randomIntBetween(1, 8);
        MetaData.Builder mdBuilder = MetaData.builder();
        for (int i = 0; i < numLeaderIndices; i++) {
            mdBuilder.put(IndexMetaData.builder("logs-" + i)
                .settings(settings(Version.CURRENT))
                .numberOfShards(1)
                .numberOfReplicas(0));
        }

        ClusterState remoteState = ClusterState.builder(new ClusterName("eu_cluster"))
            .metaData(mdBuilder)
            .build();

        ClusterState result = TransportPutAutoFollowPatternAction.innerPut(request, null, localState, remoteState);
        AutoFollowMetadata autoFollowMetadata = result.metaData().custom(AutoFollowMetadata.TYPE);
        assertThat(autoFollowMetadata, notNullValue());
        assertThat(autoFollowMetadata.getPatterns().size(), equalTo(1));
        assertThat(autoFollowMetadata.getPatterns().get("name1").getRemoteCluster(), equalTo("eu_cluster"));
        assertThat(autoFollowMetadata.getPatterns().get("name1").getLeaderIndexPatterns().size(), equalTo(2));
        assertThat(autoFollowMetadata.getPatterns().get("name1").getLeaderIndexPatterns().get(0), equalTo("logs-*"));
        assertThat(autoFollowMetadata.getPatterns().get("name1").getLeaderIndexPatterns().get(1), equalTo("transactions-*"));
        assertThat(autoFollowMetadata.getFollowedLeaderIndexUUIDs().size(), equalTo(1));
        assertThat(autoFollowMetadata.getFollowedLeaderIndexUUIDs().get("name1").size(), equalTo(numLeaderIndices + 1));
        assertThat(autoFollowMetadata.getHeaders().size(), equalTo(1));
        assertThat(autoFollowMetadata.getHeaders().get("name1"), notNullValue());
    }

}
