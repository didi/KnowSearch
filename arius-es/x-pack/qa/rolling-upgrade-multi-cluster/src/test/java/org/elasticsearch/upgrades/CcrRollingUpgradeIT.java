/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.upgrades;

import org.elasticsearch.Version;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ObjectPath;
import org.elasticsearch.common.xcontent.support.XContentMapValues;

import java.io.IOException;
import java.util.Map;

import static org.elasticsearch.rest.action.search.RestSearchAction.TOTAL_HITS_AS_INT_PARAM;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class CcrRollingUpgradeIT extends AbstractMultiClusterUpgradeTestCase {

    public void testUniDirectionalIndexFollowing() throws Exception {
        logger.info("clusterName={}, upgradeState={}", clusterName, upgradeState);

        if (clusterName == ClusterName.LEADER) {
            switch (upgradeState) {
                case NONE:
                    createLeaderIndex(leaderClient(), "leader_index1");
                    index(leaderClient(), "leader_index1", 64);
                    createLeaderIndex(leaderClient(), "leader_index2");
                    index(leaderClient(), "leader_index2", 64);
                    break;
                case ONE_THIRD:
                    break;
                case TWO_THIRD:
                    break;
                case ALL:
                    createLeaderIndex(leaderClient(), "leader_index4");
                    followIndex(followerClient(), "leader", "leader_index4", "follower_index4");
                    index(leaderClient(), "leader_index4", 64);
                    assertTotalHitCount("follower_index4", 64, followerClient());
                    break;
                default:
                    throw new AssertionError("unexpected upgrade_state [" + upgradeState + "]");
            }
        } else if (clusterName == ClusterName.FOLLOWER) {
            switch (upgradeState) {
                case NONE:
                    followIndex(followerClient(), "leader", "leader_index1", "follower_index1");
                    assertTotalHitCount("follower_index1", 64, followerClient());
                    break;
                case ONE_THIRD:
                    index(leaderClient(), "leader_index1", 64);
                    assertTotalHitCount("follower_index1", 128, followerClient());

                    followIndex(followerClient(), "leader", "leader_index2", "follower_index2");
                    assertTotalHitCount("follower_index2", 64, followerClient());
                    break;
                case TWO_THIRD:
                    index(leaderClient(), "leader_index1", 64);
                    assertTotalHitCount("follower_index1", 192, followerClient());

                    index(leaderClient(), "leader_index2", 64);
                    assertTotalHitCount("follower_index2", 128, followerClient());

                    createLeaderIndex(leaderClient(), "leader_index3");
                    index(leaderClient(), "leader_index3", 64);
                    followIndex(followerClient(), "leader", "leader_index3", "follower_index3");
                    assertTotalHitCount("follower_index3", 64, followerClient());
                    break;
                case ALL:
                    index(leaderClient(), "leader_index1", 64);
                    assertTotalHitCount("follower_index1", 256, followerClient());

                    index(leaderClient(), "leader_index2", 64);
                    assertTotalHitCount("follower_index2", 192, followerClient());

                    index(leaderClient(), "leader_index3", 64);
                    assertTotalHitCount("follower_index3", 128, followerClient());
                    break;
                default:
                    throw new AssertionError("unexpected upgrade_state [" + upgradeState + "]");
            }
        } else {
            throw new AssertionError("unexpected cluster_name [" + clusterName + "]");
        }
    }

    public void testAutoFollowing() throws Exception {
        String leaderIndex1 = "logs-20200101";
        String leaderIndex2 = "logs-20200102";
        String leaderIndex3 = "logs-20200103";

        if (clusterName == ClusterName.LEADER) {
            switch (upgradeState) {
                case NONE:
                case ONE_THIRD:
                case TWO_THIRD:
                    break;
                case ALL:
                    index(leaderClient(), leaderIndex1, 64);
                    assertBusy(() -> {
                        String followerIndex = "copy-" + leaderIndex1;
                        assertTotalHitCount(followerIndex, 320, followerClient());
                    });
                    index(leaderClient(), leaderIndex2, 64);
                    assertBusy(() -> {
                        String followerIndex = "copy-" + leaderIndex2;
                        assertTotalHitCount(followerIndex, 256, followerClient());
                    });
                    index(leaderClient(), leaderIndex3, 64);
                    assertBusy(() -> {
                        String followerIndex = "copy-" + leaderIndex3;
                        assertTotalHitCount(followerIndex, 192, followerClient());
                    });

                    deleteAutoFollowPattern(followerClient(), "test_pattern");
                    stopIndexFollowing(followerClient(), "copy-" + leaderIndex1);
                    stopIndexFollowing(followerClient(), "copy-" + leaderIndex2);
                    stopIndexFollowing(followerClient(), "copy-" + leaderIndex3);
                    break;
                default:
                    throw new AssertionError("unexpected upgrade_state [" + upgradeState + "]");
            }
        } else if (clusterName == ClusterName.FOLLOWER) {
            switch (upgradeState) {
                case NONE:
                    putAutoFollowPattern(followerClient(), "test_pattern", "leader", "logs-*");
                    createLeaderIndex(leaderClient(), leaderIndex1);
                    index(leaderClient(), leaderIndex1, 64);
                    assertBusy(() -> {

                        String followerIndex = "copy-" + leaderIndex1;
                        assertThat(getNumberOfSuccessfulFollowedIndices(), equalTo(1));
                        assertTotalHitCount(followerIndex, 64, followerClient());
                    });
                    break;
                case ONE_THIRD:
                    index(leaderClient(), leaderIndex1, 64);
                    assertBusy(() -> {
                        String followerIndex = "copy-" + leaderIndex1;
                        assertTotalHitCount(followerIndex, 128, followerClient());
                    });
                    // Auto follow stats are kept in-memory on master elected node
                    // and if this node get updated then auto follow stats are reset
                {
                    int previousNumberOfSuccessfulFollowedIndices = getNumberOfSuccessfulFollowedIndices();
                    createLeaderIndex(leaderClient(), leaderIndex2);
                    index(leaderClient(), leaderIndex2, 64);
                    assertBusy(() -> {
                        String followerIndex = "copy-" + leaderIndex2;
                        assertThat(getNumberOfSuccessfulFollowedIndices(), equalTo(previousNumberOfSuccessfulFollowedIndices + 1));
                        assertTotalHitCount(followerIndex, 64, followerClient());
                    });
                }
                break;
                case TWO_THIRD:
                    index(leaderClient(), leaderIndex1, 64);
                    assertBusy(() -> {
                        String followerIndex = "copy-" + leaderIndex1;
                        assertTotalHitCount(followerIndex, 192, followerClient());
                    });
                    index(leaderClient(), leaderIndex2, 64);
                    assertBusy(() -> {
                        String followerIndex = "copy-" + leaderIndex2;
                        assertTotalHitCount(followerIndex, 128, followerClient());
                    });

                    // Auto follow stats are kept in-memory on master elected node
                    // and if this node get updated then auto follow stats are reset
                {
                    int previousNumberOfSuccessfulFollowedIndices = getNumberOfSuccessfulFollowedIndices();
                    createLeaderIndex(leaderClient(), leaderIndex3);
                    index(leaderClient(), leaderIndex3, 64);
                    assertBusy(() -> {
                        String followerIndex = "copy-" + leaderIndex3;
                        assertThat(getNumberOfSuccessfulFollowedIndices(), equalTo(previousNumberOfSuccessfulFollowedIndices + 1));
                        assertTotalHitCount(followerIndex, 64, followerClient());
                    });
                }
                break;
                case ALL:
                    index(leaderClient(), leaderIndex1, 64);
                    assertBusy(() -> {
                        String followerIndex = "copy-" + leaderIndex1;
                        assertTotalHitCount(followerIndex, 256, followerClient());
                    });
                    index(leaderClient(), leaderIndex2, 64);
                    assertBusy(() -> {
                        String followerIndex = "copy-" + leaderIndex2;
                        assertTotalHitCount(followerIndex, 192, followerClient());
                    });
                    index(leaderClient(), leaderIndex3, 64);
                    assertBusy(() -> {
                        String followerIndex = "copy-" + leaderIndex3;
                        assertTotalHitCount(followerIndex, 128, followerClient());
                    });
                    break;
                default:
                    throw new UnsupportedOperationException("unexpected upgrade state [" + upgradeState + "]");
            }
        } else {
            throw new AssertionError("unexpected cluster_name [" + clusterName + "]");
        }
    }

    public void testCannotFollowLeaderInUpgradedCluster() throws Exception {
        if (upgradeState != UpgradeState.ALL) {
            return;
        }

        if (clusterName == ClusterName.FOLLOWER) {
            // At this point the leader cluster has not been upgraded, but follower cluster has been upgrade.
            // Create a leader index in the follow cluster and try to follow it in the leader cluster.
            // This should fail, because the leader cluster at this point in time can't do file based recovery from follower.
            createLeaderIndex(followerClient(), "not_supported");
            index(followerClient(), "not_supported", 64);

            ResponseException e = expectThrows(ResponseException.class,
                () -> followIndex(leaderClient(), "follower", "not_supported", "not_supported"));
            assertThat(e.getMessage(), containsString("the snapshot was created with Elasticsearch version ["));
            assertThat(e.getMessage(), containsString("] which is higher than the version of this node ["));
        } else if (clusterName == ClusterName.LEADER) {
            // At this point all nodes in both clusters have been updated and
            // the leader cluster can now follow not_supported index in the follower cluster:
            ensureGreen(followerClient(), "not_supported");
            followIndex(leaderClient(), "follower", "not_supported", "not_supported");
            assertTotalHitCount("not_supported", 64, leaderClient());
        } else {
            throw new AssertionError("unexpected cluster_name [" + clusterName + "]");
        }
    }

    public void testBiDirectionalIndexFollowing() throws Exception {
        logger.info("clusterName={}, upgradeState={}", clusterName, upgradeState);

        if (clusterName == ClusterName.FOLLOWER) {
            switch (upgradeState) {
                case NONE:
                    createLeaderIndex(leaderClient(), "leader_index5");
                    index(leaderClient(), "leader_index5", 128);

                    followIndex(followerClient(), "leader", "leader_index5", "follower_index5");
                    followIndex(leaderClient(), "follower", "follower_index5", "follower_index6");
                    assertTotalHitCount("follower_index5", 128, followerClient());
                    assertTotalHitCount("follower_index6", 128, leaderClient());

                    index(leaderClient(), "leader_index5", 128);
                    pauseIndexFollowing(followerClient(), "follower_index5");
                    pauseIndexFollowing(leaderClient(), "follower_index6");
                    break;
                case ONE_THIRD:
                    index(leaderClient(), "leader_index5", 128);
                    break;
                case TWO_THIRD:
                    index(leaderClient(), "leader_index5", 128);
                    break;
                case ALL:
                    index(leaderClient(), "leader_index5", 128);
                    break;
                default:
                    throw new AssertionError("unexpected upgrade_state [" + upgradeState + "]");
            }
        } else if (clusterName == ClusterName.LEADER) {
            switch (upgradeState) {
                case NONE:
                    break;
                case ONE_THIRD:
                    index(leaderClient(), "leader_index5", 128);
                    break;
                case TWO_THIRD:
                    index(leaderClient(), "leader_index5", 128);
                    break;
                case ALL:
                    ensureGreen(followerClient(), "follower_index5");
                    resumeIndexFollowing(followerClient(), "follower_index5");
                    ensureGreen(leaderClient(), "follower_index6");
                    resumeIndexFollowing(leaderClient(), "follower_index6");

                    assertTotalHitCount("follower_index5", 896, followerClient());
                    assertTotalHitCount("follower_index6", 896, leaderClient());
                    break;
                default:
                    throw new AssertionError("unexpected upgrade_state [" + upgradeState + "]");
            }
        }  else {
            throw new AssertionError("unexpected cluster_name [" + clusterName + "]");
        }
    }

    private static void createLeaderIndex(RestClient client, String indexName) throws IOException {
        Settings.Builder indexSettings = Settings.builder()
            .put("index.number_of_shards", 1)
            .put("index.number_of_replicas", 0);
        if (UPGRADE_FROM_VERSION.before(Version.V_7_0_0) || randomBoolean()) {
            indexSettings.put("index.soft_deletes.enabled", true);
        }
        createIndex(client, indexName, indexSettings.build());
    }

    private static void createIndex(RestClient client, String name, Settings settings) throws IOException {
        Request request = new Request("PUT", "/" + name);
        request.setJsonEntity("{\n \"settings\": " + Strings.toString(settings) + "}");
        client.performRequest(request);
    }

    private static void followIndex(RestClient client, String leaderCluster, String leaderIndex, String followIndex) throws IOException {
        final Request request = new Request("PUT", "/" + followIndex + "/_ccr/follow?wait_for_active_shards=1");
        request.setJsonEntity("{\"remote_cluster\": \"" + leaderCluster + "\", \"leader_index\": \"" + leaderIndex +
            "\", \"read_poll_timeout\": \"10ms\"}");
        assertOK(client.performRequest(request));
    }

    private static void putAutoFollowPattern(RestClient client, String name, String remoteCluster, String pattern) throws IOException {
        Request request = new Request("PUT", "/_ccr/auto_follow/" + name);
        request.setJsonEntity("{\"leader_index_patterns\": [\"" + pattern + "\"], \"remote_cluster\": \"" +
            remoteCluster + "\"," +
            "\"follow_index_pattern\": \"copy-{{leader_index}}\", \"read_poll_timeout\": \"10ms\"}");
        assertOK(client.performRequest(request));
    }

    private static void deleteAutoFollowPattern(RestClient client, String patternName) throws IOException {
        Request request = new Request("DELETE", "/_ccr/auto_follow/" + patternName);
        assertOK(client.performRequest(request));
    }

    private int getNumberOfSuccessfulFollowedIndices() throws IOException {
        Request statsRequest = new Request("GET", "/_ccr/stats");
        Map<?, ?> response = toMap(followerClient().performRequest(statsRequest));
        Integer actualSuccessfulFollowedIndices = ObjectPath.eval("auto_follow_stats.number_of_successful_follow_indices", response);
        if (actualSuccessfulFollowedIndices != null) {
            return actualSuccessfulFollowedIndices;
        } else {
            return -1;
        }
    }

    private static void index(RestClient client, String index, int numDocs) throws IOException {
        for (int i = 0; i < numDocs; i++) {
            final Request request = new Request("POST", "/" + index + "/_doc/");
            request.setJsonEntity("{}");
            assertOK(client.performRequest(request));
            if (randomIntBetween(0, 5) == 3) {
                assertOK(client.performRequest(new Request("POST", "/" + index + "/_refresh")));
            }
        }
    }

    private static void assertTotalHitCount(final String index,
                                            final int expectedTotalHits,
                                            final RestClient client) throws Exception {
        assertOK(client.performRequest(new Request("POST", "/" + index + "/_refresh")));
        assertBusy(() -> verifyTotalHitCount(index, expectedTotalHits, client));
    }

    private static void verifyTotalHitCount(final String index,
                                            final int expectedTotalHits,
                                            final RestClient client) throws IOException {
        final Request request = new Request("GET", "/" + index + "/_search");
        request.addParameter(TOTAL_HITS_AS_INT_PARAM, "true");
        Map<?, ?> response = toMap(client.performRequest(request));
        final int totalHits = (int) XContentMapValues.extractValue("hits.total", response);
        assertThat(totalHits, equalTo(expectedTotalHits));
    }

    private static void stopIndexFollowing(RestClient client, String followerIndex) throws IOException {
        pauseIndexFollowing(client, followerIndex);
        assertOK(client.performRequest(new Request("POST", "/" + followerIndex + "/_close")));
        assertOK(client.performRequest(new Request("POST", "/" + followerIndex + "/_ccr/unfollow")));
    }

    private static void pauseIndexFollowing(RestClient client, String followerIndex) throws IOException {
        assertOK(client.performRequest(new Request("POST", "/" + followerIndex + "/_ccr/pause_follow")));
    }

    private static void resumeIndexFollowing(RestClient client, String followerIndex) throws IOException {
        assertOK(client.performRequest(new Request("POST", "/" + followerIndex + "/_ccr/resume_follow")));
    }

    private static void ensureGreen(RestClient client, String index) throws IOException {
        Request request = new Request("GET", "/_cluster/health/" + index);
        request.addParameter("wait_for_status", "green");
        request.addParameter("wait_for_no_relocating_shards", "true");
        request.addParameter("timeout", "70s");
        request.addParameter("level", "shards");
        client.performRequest(request);
    }

}
