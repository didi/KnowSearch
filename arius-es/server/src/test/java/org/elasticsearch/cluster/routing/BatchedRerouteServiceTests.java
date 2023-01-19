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
package org.elasticsearch.cluster.routing;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.coordination.FailedToCommitClusterStateException;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.Randomness;
import org.elasticsearch.test.ClusterServiceUtils;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.threadpool.TestThreadPool;
import org.elasticsearch.threadpool.ThreadPool;
import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.hamcrest.Matchers.lessThan;

public class BatchedRerouteServiceTests extends ESTestCase {

    private ThreadPool threadPool;
    private ClusterService clusterService;

    @Before
    public void beforeTest() {
        threadPool = new TestThreadPool("test");
        clusterService = ClusterServiceUtils.createClusterService(threadPool);
    }

    @After
    public void afterTest() {
        clusterService.stop();
        threadPool.shutdown();
    }

    public void testReroutesWhenRequested() throws InterruptedException {
        final AtomicLong rerouteCount = new AtomicLong();
        final BatchedRerouteService batchedRerouteService = new BatchedRerouteService(clusterService, (s, r) -> {
            rerouteCount.incrementAndGet();
            return s;
        });

        long rerouteCountBeforeReroute = 0L;
        final int iterations = between(1, 100);
        final CountDownLatch countDownLatch = new CountDownLatch(iterations);
        for (int i = 0; i < iterations; i++) {
            rerouteCountBeforeReroute = Math.max(rerouteCountBeforeReroute, rerouteCount.get());
            batchedRerouteService.reroute("iteration " + i, randomFrom(EnumSet.allOf(Priority.class)),
                ActionListener.wrap(countDownLatch::countDown));
        }
        countDownLatch.await(10, TimeUnit.SECONDS);
        assertThat(rerouteCountBeforeReroute, lessThan(rerouteCount.get()));
    }

    public void testNotifiesOnFailure() throws InterruptedException {

        final BatchedRerouteService batchedRerouteService = new BatchedRerouteService(clusterService, (s, r) -> {
            if (rarely()) {
                throw new ElasticsearchException("simulated");
            }
            return randomBoolean() ? s : ClusterState.builder(s).build();
        });

        final int iterations = between(1, 100);
        final CountDownLatch countDownLatch = new CountDownLatch(iterations);
        for (int i = 0; i < iterations; i++) {
            batchedRerouteService.reroute("iteration " + i,
                randomFrom(EnumSet.allOf(Priority.class)), ActionListener.wrap(
                    r -> {
                        countDownLatch.countDown();
                        if (rarely()) {
                            throw new ElasticsearchException("failure during notification");
                        }
                    }, e -> {
                        countDownLatch.countDown();
                        if (randomBoolean()) {
                            throw new ElasticsearchException("failure during failure notification", e);
                        }
                    }));
            if (rarely()) {
                clusterService.getMasterService().setClusterStatePublisher(
                    randomBoolean()
                        ? ClusterServiceUtils.createClusterStatePublisher(clusterService.getClusterApplierService())
                        : (event, publishListener, ackListener)
                        -> publishListener.onFailure(new FailedToCommitClusterStateException("simulated")));
            }

            if (rarely()) {
                clusterService.getClusterApplierService().onNewClusterState("simulated", () -> {
                    ClusterState state = clusterService.state();
                    return ClusterState.builder(state).nodes(DiscoveryNodes.builder(state.nodes())
                        .masterNodeId(randomBoolean() ? null : state.nodes().getLocalNodeId())).build();
                }, (source, e) -> { });
            }
        }

        assertTrue(countDownLatch.await(10, TimeUnit.SECONDS)); // i.e. it doesn't leak any listeners
    }
}
