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

package org.elasticsearch.persistent;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodeRole;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.UUIDs;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.persistent.PersistentTasksCustomMetaData.Assignment;
import org.elasticsearch.persistent.PersistentTasksCustomMetaData.PersistentTask;
import org.elasticsearch.persistent.TestPersistentTasksPlugin.TestParams;
import org.elasticsearch.persistent.TestPersistentTasksPlugin.TestPersistentTasksExecutor;
import org.elasticsearch.persistent.decider.EnableAssignmentDecider;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.VersionUtils;
import org.elasticsearch.threadpool.TestThreadPool;
import org.elasticsearch.threadpool.ThreadPool;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static org.elasticsearch.persistent.PersistentTasksClusterService.needsReassignment;
import static org.elasticsearch.persistent.PersistentTasksClusterService.persistentTasksChanged;
import static org.elasticsearch.persistent.PersistentTasksExecutor.NO_NODE_FOUND;
import static org.elasticsearch.test.ClusterServiceUtils.createClusterService;
import static org.elasticsearch.test.ClusterServiceUtils.setState;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersistentTasksClusterServiceTests extends ESTestCase {

    /** Needed by {@link ClusterService} **/
    private static ThreadPool threadPool;
    /** Needed by {@link PersistentTasksClusterService} **/
    private ClusterService clusterService;

    private volatile boolean nonClusterStateCondition;

    @BeforeClass
    public static void setUpThreadPool() {
        threadPool = new TestThreadPool(PersistentTasksClusterServiceTests.class.getSimpleName());
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        clusterService = createClusterService(threadPool);
    }

    @AfterClass
    public static void tearDownThreadPool() {
        terminate(threadPool);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        clusterService.close();
    }

    public void testReassignmentRequired() {
        final PersistentTasksClusterService service = createService((params, clusterState) ->
            "never_assign".equals(((TestParams) params).getTestParam()) ? NO_NODE_FOUND : randomNodeAssignment(clusterState.nodes())
        );

        int numberOfIterations = randomIntBetween(1, 30);
        ClusterState clusterState = initialState();
        for (int i = 0; i < numberOfIterations; i++) {
            boolean significant = randomBoolean();
            ClusterState previousState = clusterState;
            logger.info("inter {} significant: {}", i, significant);
            if (significant) {
                clusterState = significantChange(clusterState);
            } else {
                clusterState = insignificantChange(clusterState);
            }
            ClusterChangedEvent event = new ClusterChangedEvent("test", clusterState, previousState);
            assertThat(dumpEvent(event), service.shouldReassignPersistentTasks(event), equalTo(significant));
        }
    }

    public void testReassignmentRequiredOnMetadataChanges() {
        EnableAssignmentDecider.Allocation allocation = randomFrom(EnableAssignmentDecider.Allocation.values());

        DiscoveryNodes nodes = DiscoveryNodes.builder()
            .add(new DiscoveryNode("_node", buildNewFakeTransportAddress(), Version.CURRENT))
            .localNodeId("_node")
            .masterNodeId("_node")
            .build();

        boolean unassigned = randomBoolean();
        PersistentTasksCustomMetaData tasks = PersistentTasksCustomMetaData.builder()
            .addTask("_task_1", TestPersistentTasksExecutor.NAME, null, new Assignment(unassigned ? null : "_node", "_reason"))
            .build();

        MetaData metaData = MetaData.builder()
            .putCustom(PersistentTasksCustomMetaData.TYPE, tasks)
            .persistentSettings(Settings.builder()
                    .put(EnableAssignmentDecider.CLUSTER_TASKS_ALLOCATION_ENABLE_SETTING.getKey(), allocation.toString())
                    .build())
            .build();

        ClusterState previous = ClusterState.builder(new ClusterName("_name"))
            .nodes(nodes)
            .metaData(metaData)
            .build();

        ClusterState current;

        final boolean changed = randomBoolean();
        if (changed) {
            allocation = randomValueOtherThan(allocation, () -> randomFrom(EnableAssignmentDecider.Allocation.values()));

            current = ClusterState.builder(previous)
                .metaData(MetaData.builder(previous.metaData())
                    .persistentSettings(Settings.builder()
                        .put(EnableAssignmentDecider.CLUSTER_TASKS_ALLOCATION_ENABLE_SETTING.getKey(), allocation.toString())
                        .build())
                    .build())
                .build();
        } else {
            current = ClusterState.builder(previous).build();
        }

        final ClusterChangedEvent event = new ClusterChangedEvent("test", current, previous);

        final PersistentTasksClusterService service = createService((params, clusterState) -> randomNodeAssignment(clusterState.nodes()));
        assertThat(dumpEvent(event), service.shouldReassignPersistentTasks(event), equalTo(changed && unassigned));
    }

    public void testReassignTasksWithNoTasks() {
        ClusterState clusterState = initialState();
        assertThat(reassign(clusterState).metaData().custom(PersistentTasksCustomMetaData.TYPE), nullValue());
    }

    public void testReassignConsidersClusterStateUpdates() {
        ClusterState clusterState = initialState();
        ClusterState.Builder builder = ClusterState.builder(clusterState);
        PersistentTasksCustomMetaData.Builder tasks = PersistentTasksCustomMetaData.builder(
                clusterState.metaData().custom(PersistentTasksCustomMetaData.TYPE));
        DiscoveryNodes.Builder nodes = DiscoveryNodes.builder(clusterState.nodes());
        addTestNodes(nodes, randomIntBetween(1, 10));
        int numberOfTasks = randomIntBetween(2, 40);
        for (int i = 0; i < numberOfTasks; i++) {
            addTask(tasks, "assign_one", randomBoolean() ? null : "no_longer_exists");
        }

        MetaData.Builder metaData = MetaData.builder(clusterState.metaData()).putCustom(PersistentTasksCustomMetaData.TYPE, tasks.build());
        clusterState = builder.metaData(metaData).nodes(nodes).build();
        ClusterState newClusterState = reassign(clusterState);

        PersistentTasksCustomMetaData tasksInProgress = newClusterState.getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
        assertThat(tasksInProgress, notNullValue());
    }

    public void testNonClusterStateConditionAssignment() {
        ClusterState clusterState = initialState();
        ClusterState.Builder builder = ClusterState.builder(clusterState);
        PersistentTasksCustomMetaData.Builder tasks = PersistentTasksCustomMetaData.builder(
            clusterState.metaData().custom(PersistentTasksCustomMetaData.TYPE));
        DiscoveryNodes.Builder nodes = DiscoveryNodes.builder(clusterState.nodes());
        addTestNodes(nodes, randomIntBetween(1, 3));
        addTask(tasks, "assign_based_on_non_cluster_state_condition", null);
        MetaData.Builder metaData = MetaData.builder(clusterState.metaData()).putCustom(PersistentTasksCustomMetaData.TYPE, tasks.build());
        clusterState = builder.metaData(metaData).nodes(nodes).build();

        nonClusterStateCondition = false;
        ClusterState newClusterState = reassign(clusterState);

        PersistentTasksCustomMetaData tasksInProgress = newClusterState.getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
        assertThat(tasksInProgress, notNullValue());
        for (PersistentTask<?> task : tasksInProgress.tasks()) {
            assertThat(task.getExecutorNode(), nullValue());
            assertThat(task.isAssigned(), equalTo(false));
            assertThat(task.getAssignment().getExplanation(), equalTo("non-cluster state condition prevents assignment"));
        }
        assertThat(tasksInProgress.tasks().size(), equalTo(1));

        nonClusterStateCondition = true;
        ClusterState finalClusterState = reassign(newClusterState);

        tasksInProgress = finalClusterState.getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
        assertThat(tasksInProgress, notNullValue());
        for (PersistentTask<?> task : tasksInProgress.tasks()) {
            assertThat(task.getExecutorNode(), notNullValue());
            assertThat(task.isAssigned(), equalTo(true));
            assertThat(task.getAssignment().getExplanation(), equalTo("test assignment"));
        }
        assertThat(tasksInProgress.tasks().size(), equalTo(1));
    }

    public void testReassignTasks() {
        ClusterState clusterState = initialState();
        ClusterState.Builder builder = ClusterState.builder(clusterState);
        PersistentTasksCustomMetaData.Builder tasks = PersistentTasksCustomMetaData.builder(
                clusterState.metaData().custom(PersistentTasksCustomMetaData.TYPE));
        DiscoveryNodes.Builder nodes = DiscoveryNodes.builder(clusterState.nodes());
        addTestNodes(nodes, randomIntBetween(1, 10));
        int numberOfTasks = randomIntBetween(0, 40);
        for (int i = 0; i < numberOfTasks; i++) {
            switch (randomInt(2)) {
                case 0:
                    // add an unassigned task that should get assigned because it's assigned to a non-existing node or unassigned
                    addTask(tasks, "assign_me", randomBoolean() ? null : "no_longer_exists");
                    break;
                case 1:
                    // add a task assigned to non-existing node that should not get assigned
                    addTask(tasks, "dont_assign_me", randomBoolean() ? null : "no_longer_exists");
                    break;
                case 2:
                    addTask(tasks, "assign_one", randomBoolean() ? null : "no_longer_exists");
                    break;

            }
        }
        MetaData.Builder metaData = MetaData.builder(clusterState.metaData()).putCustom(PersistentTasksCustomMetaData.TYPE, tasks.build());
        clusterState = builder.metaData(metaData).nodes(nodes).build();
        ClusterState newClusterState = reassign(clusterState);

        PersistentTasksCustomMetaData tasksInProgress = newClusterState.getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
        assertThat(tasksInProgress, notNullValue());

        assertThat("number of tasks shouldn't change as a result or reassignment",
                numberOfTasks, equalTo(tasksInProgress.tasks().size()));

        int assignOneCount = 0;

        for (PersistentTask<?> task : tasksInProgress.tasks()) {
            // explanation should correspond to the action name
            switch (((TestParams) task.getParams()).getTestParam()) {
                case "assign_me":
                    assertThat(task.getExecutorNode(), notNullValue());
                    assertThat(task.isAssigned(), equalTo(true));
                    if (clusterState.nodes().nodeExists(task.getExecutorNode()) == false) {
                        logger.info(clusterState.metaData().custom(PersistentTasksCustomMetaData.TYPE).toString());
                    }
                    assertThat("task should be assigned to a node that is in the cluster, was assigned to " + task.getExecutorNode(),
                            clusterState.nodes().nodeExists(task.getExecutorNode()), equalTo(true));
                    assertThat(task.getAssignment().getExplanation(), equalTo("test assignment"));
                    break;
                case "dont_assign_me":
                    assertThat(task.getExecutorNode(), nullValue());
                    assertThat(task.isAssigned(), equalTo(false));
                    assertThat(task.getAssignment().getExplanation(), equalTo("no appropriate nodes found for the assignment"));
                    break;
                case "assign_one":
                    if (task.isAssigned()) {
                        assignOneCount++;
                        assertThat("more than one assign_one tasks are assigned", assignOneCount, lessThanOrEqualTo(1));
                        assertThat(task.getAssignment().getExplanation(), equalTo("test assignment"));
                    } else {
                        assertThat(task.getAssignment().getExplanation(), equalTo("only one task can be assigned at a time"));
                    }
                    break;
                default:
                    fail("Unknown action " + task.getTaskName());
            }
        }
    }

    public void testPersistentTasksChangedNoTasks() {
        DiscoveryNodes nodes = DiscoveryNodes.builder()
            .add(new DiscoveryNode("_node_1", buildNewFakeTransportAddress(), Version.CURRENT))
            .build();

        ClusterState previous = ClusterState.builder(new ClusterName("_name"))
            .nodes(nodes)
            .build();
        ClusterState current = ClusterState.builder(new ClusterName("_name"))
            .nodes(nodes)
            .build();

        assertFalse("persistent tasks unchanged (no tasks)",
            persistentTasksChanged(new ClusterChangedEvent("test", current, previous)));
    }

    public void testPersistentTasksChangedTaskAdded() {
        DiscoveryNodes nodes = DiscoveryNodes.builder()
            .add(new DiscoveryNode("_node_1", buildNewFakeTransportAddress(), Version.CURRENT))
            .build();

        ClusterState previous = ClusterState.builder(new ClusterName("_name"))
            .nodes(nodes)
            .build();

        PersistentTasksCustomMetaData tasks = PersistentTasksCustomMetaData.builder()
            .addTask("_task_1", "test", null, new Assignment(null, "_reason"))
            .build();

        ClusterState current = ClusterState.builder(new ClusterName("_name"))
            .nodes(nodes)
            .metaData(MetaData.builder().putCustom(PersistentTasksCustomMetaData.TYPE, tasks))
            .build();

        assertTrue("persistent tasks changed (task added)",
            persistentTasksChanged(new ClusterChangedEvent("test", current, previous)));
    }

    public void testPersistentTasksChangedTaskRemoved() {
        DiscoveryNodes nodes = DiscoveryNodes.builder()
            .add(new DiscoveryNode("_node_1", buildNewFakeTransportAddress(), Version.CURRENT))
            .add(new DiscoveryNode("_node_2", buildNewFakeTransportAddress(), Version.CURRENT))
            .build();

        PersistentTasksCustomMetaData previousTasks = PersistentTasksCustomMetaData.builder()
            .addTask("_task_1", "test", null, new Assignment("_node_1", "_reason"))
            .addTask("_task_2", "test", null, new Assignment("_node_1", "_reason"))
            .addTask("_task_3", "test", null, new Assignment("_node_2", "_reason"))
            .build();

        ClusterState previous = ClusterState.builder(new ClusterName("_name"))
            .nodes(nodes)
            .metaData(MetaData.builder().putCustom(PersistentTasksCustomMetaData.TYPE, previousTasks))
            .build();

        PersistentTasksCustomMetaData currentTasks = PersistentTasksCustomMetaData.builder()
            .addTask("_task_1", "test", null, new Assignment("_node_1", "_reason"))
            .addTask("_task_3", "test", null, new Assignment("_node_2", "_reason"))
            .build();

        ClusterState current = ClusterState.builder(new ClusterName("_name"))
            .nodes(nodes)
            .metaData(MetaData.builder().putCustom(PersistentTasksCustomMetaData.TYPE, currentTasks))
            .build();

        assertTrue("persistent tasks changed (task removed)",
            persistentTasksChanged(new ClusterChangedEvent("test", current, previous)));
    }

    public void testPersistentTasksAssigned() {
        DiscoveryNodes nodes = DiscoveryNodes.builder()
            .add(new DiscoveryNode("_node_1", buildNewFakeTransportAddress(), Version.CURRENT))
            .add(new DiscoveryNode("_node_2", buildNewFakeTransportAddress(), Version.CURRENT))
            .build();

        PersistentTasksCustomMetaData previousTasks = PersistentTasksCustomMetaData.builder()
            .addTask("_task_1", "test", null, new Assignment("_node_1", ""))
            .addTask("_task_2", "test", null, new Assignment(null, "unassigned"))
            .build();

        ClusterState previous = ClusterState.builder(new ClusterName("_name"))
            .nodes(nodes)
            .metaData(MetaData.builder().putCustom(PersistentTasksCustomMetaData.TYPE, previousTasks))
            .build();

        PersistentTasksCustomMetaData currentTasks = PersistentTasksCustomMetaData.builder()
            .addTask("_task_1", "test", null, new Assignment("_node_1", ""))
            .addTask("_task_2", "test", null, new Assignment("_node_2", ""))
            .build();

        ClusterState current = ClusterState.builder(new ClusterName("_name"))
            .nodes(nodes)
            .metaData(MetaData.builder().putCustom(PersistentTasksCustomMetaData.TYPE, currentTasks))
            .build();

        assertTrue("persistent tasks changed (task assigned)",
            persistentTasksChanged(new ClusterChangedEvent("test", current, previous)));
    }

    public void testNeedsReassignment() {
        DiscoveryNodes nodes = DiscoveryNodes.builder()
            .add(new DiscoveryNode("_node_1", buildNewFakeTransportAddress(), Version.CURRENT))
            .add(new DiscoveryNode("_node_2", buildNewFakeTransportAddress(), Version.CURRENT))
            .build();

        assertTrue(needsReassignment(new Assignment(null, "unassigned"), nodes));
        assertTrue(needsReassignment(new Assignment("_node_left", "assigned to a node that left"), nodes));
        assertFalse(needsReassignment(new Assignment("_node_1", "assigned"), nodes));
    }

    public void testPeriodicRecheck() throws Exception {
        ClusterState initialState = initialState();
        ClusterState.Builder builder = ClusterState.builder(initialState);
        PersistentTasksCustomMetaData.Builder tasks = PersistentTasksCustomMetaData.builder(
            initialState.metaData().custom(PersistentTasksCustomMetaData.TYPE));
        DiscoveryNodes.Builder nodes = DiscoveryNodes.builder(initialState.nodes());
        addTestNodes(nodes, randomIntBetween(1, 3));
        addTask(tasks, "assign_based_on_non_cluster_state_condition", null);
        MetaData.Builder metaData = MetaData.builder(initialState.metaData()).putCustom(PersistentTasksCustomMetaData.TYPE, tasks.build());
        ClusterState clusterState = builder.metaData(metaData).nodes(nodes).build();

        nonClusterStateCondition = false;

        boolean shouldSimulateFailure = randomBoolean();
        ClusterService recheckTestClusterService = createRecheckTestClusterService(clusterState, shouldSimulateFailure);
        PersistentTasksClusterService service = createService(recheckTestClusterService,
            (params, currentState) -> assignBasedOnNonClusterStateCondition(currentState.nodes()));

        ClusterChangedEvent event = new ClusterChangedEvent("test", clusterState, initialState);
        service.clusterChanged(event);
        ClusterState newClusterState = recheckTestClusterService.state();

        {
            PersistentTasksCustomMetaData tasksInProgress = newClusterState.getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
            assertThat(tasksInProgress, notNullValue());
            for (PersistentTask<?> task : tasksInProgress.tasks()) {
                assertThat(task.getExecutorNode(), nullValue());
                assertThat(task.isAssigned(), equalTo(false));
                assertThat(task.getAssignment().getExplanation(), equalTo(shouldSimulateFailure ?
                    "explanation: assign_based_on_non_cluster_state_condition" : "non-cluster state condition prevents assignment"));
            }
            assertThat(tasksInProgress.tasks().size(), equalTo(1));
        }

        nonClusterStateCondition = true;
        service.setRecheckInterval(TimeValue.timeValueMillis(1));

        assertBusy(() -> {
            PersistentTasksCustomMetaData tasksInProgress =
                recheckTestClusterService.state().getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
            assertThat(tasksInProgress, notNullValue());
            for (PersistentTask<?> task : tasksInProgress.tasks()) {
                assertThat(task.getExecutorNode(), notNullValue());
                assertThat(task.isAssigned(), equalTo(true));
                assertThat(task.getAssignment().getExplanation(), equalTo("test assignment"));
            }
            assertThat(tasksInProgress.tasks().size(), equalTo(1));
        });
    }

    public void testUnassignTask() {
        ClusterState clusterState = initialState();
        ClusterState.Builder builder = ClusterState.builder(clusterState);
        PersistentTasksCustomMetaData.Builder tasks = PersistentTasksCustomMetaData.builder(
            clusterState.metaData().custom(PersistentTasksCustomMetaData.TYPE));
        DiscoveryNodes.Builder nodes = DiscoveryNodes.builder()
            .add(new DiscoveryNode("_node_1", buildNewFakeTransportAddress(), Version.CURRENT))
            .localNodeId("_node_1")
            .masterNodeId("_node_1")
            .add(new DiscoveryNode("_node_2", buildNewFakeTransportAddress(), Version.CURRENT));

        String unassignedId = addTask(tasks, "unassign", "_node_2");

        MetaData.Builder metaData = MetaData.builder(clusterState.metaData()).putCustom(PersistentTasksCustomMetaData.TYPE, tasks.build());
        clusterState = builder.metaData(metaData).nodes(nodes).build();
        setState(clusterService, clusterState);
        PersistentTasksClusterService service = createService((params, currentState) ->
            new Assignment("_node_2", "test"));
        service.unassignPersistentTask(unassignedId, tasks.getLastAllocationId(), "unassignment test", ActionListener.wrap(
            task -> {
                assertThat(task.getAssignment().getExecutorNode(), is(nullValue()));
                assertThat(task.getId(), equalTo(unassignedId));
                assertThat(task.getAssignment().getExplanation(), equalTo("unassignment test"));
            },
            e -> fail()
        ));
    }

    public void testUnassignNonExistentTask() {
        ClusterState clusterState = initialState();
        ClusterState.Builder builder = ClusterState.builder(clusterState);
        PersistentTasksCustomMetaData.Builder tasks = PersistentTasksCustomMetaData.builder(
            clusterState.metaData().custom(PersistentTasksCustomMetaData.TYPE));
        DiscoveryNodes.Builder nodes = DiscoveryNodes.builder()
            .add(new DiscoveryNode("_node_1", buildNewFakeTransportAddress(), Version.CURRENT))
            .localNodeId("_node_1")
            .masterNodeId("_node_1")
            .add(new DiscoveryNode("_node_2", buildNewFakeTransportAddress(), Version.CURRENT));

        MetaData.Builder metaData = MetaData.builder(clusterState.metaData()).putCustom(PersistentTasksCustomMetaData.TYPE, tasks.build());
        clusterState = builder.metaData(metaData).nodes(nodes).build();
        setState(clusterService, clusterState);
        PersistentTasksClusterService service = createService((params, currentState) ->
            new Assignment("_node_2", "test"));
        service.unassignPersistentTask("missing-task", tasks.getLastAllocationId(), "unassignment test", ActionListener.wrap(
            task -> fail(),
            e -> assertThat(e, instanceOf(ResourceNotFoundException.class))
        ));
    }

    private ClusterService createRecheckTestClusterService(ClusterState initialState, boolean shouldSimulateFailure) {
        AtomicBoolean testFailureNextTime = new AtomicBoolean(shouldSimulateFailure);
        AtomicReference<ClusterState> state = new AtomicReference<>(initialState);
        ClusterService recheckTestClusterService = mock(ClusterService.class);
        when(recheckTestClusterService.getClusterSettings()).thenReturn(clusterService.getClusterSettings());
        doAnswer(invocationOnMock -> state.get().getNodes().getLocalNode()).when(recheckTestClusterService).localNode();
        doAnswer(invocationOnMock -> state.get()).when(recheckTestClusterService).state();
        doAnswer(invocationOnMock -> {
            @SuppressWarnings("unchecked")
            ClusterStateUpdateTask task = (ClusterStateUpdateTask) invocationOnMock.getArguments()[1];
            ClusterState before = state.get();
            ClusterState after = task.execute(before);
            if (testFailureNextTime.compareAndSet(true, false)) {
                task.onFailure("testing failure", new RuntimeException("foo"));
            } else {
                state.set(after);
                task.clusterStateProcessed("test", before, after);
            }
            return null;
        }).when(recheckTestClusterService).submitStateUpdateTask(anyString(), any(ClusterStateUpdateTask.class));

        return recheckTestClusterService;
    }

    private void addTestNodes(DiscoveryNodes.Builder nodes, int nonLocalNodesCount) {
        for (int i = 0; i < nonLocalNodesCount; i++) {
            nodes.add(new DiscoveryNode("other_node_" + i, buildNewFakeTransportAddress(), Version.CURRENT));
        }
    }

    private ClusterState reassign(ClusterState clusterState) {
        PersistentTasksClusterService service = createService((params, currentState) -> {
            TestParams testParams = (TestParams) params;
            switch (testParams.getTestParam()) {
                case "assign_me":
                    return randomNodeAssignment(currentState.nodes());
                case "dont_assign_me":
                    return NO_NODE_FOUND;
                case "fail_me_if_called":
                    fail("the decision decider shouldn't be called on this task");
                    return null;
                case "assign_one":
                    return assignOnlyOneTaskAtATime(currentState);
                case "assign_based_on_non_cluster_state_condition":
                    return assignBasedOnNonClusterStateCondition(currentState.nodes());
                default:
                    fail("unknown param " + testParams.getTestParam());
            }
            return NO_NODE_FOUND;
        });

        return service.reassignTasks(clusterState);
    }

    private Assignment assignOnlyOneTaskAtATime(ClusterState clusterState) {
        DiscoveryNodes nodes = clusterState.nodes();
        PersistentTasksCustomMetaData tasksInProgress = clusterState.getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
        if (tasksInProgress.findTasks(TestPersistentTasksExecutor.NAME, task ->
                "assign_one".equals(((TestParams) task.getParams()).getTestParam()) &&
                        nodes.nodeExists(task.getExecutorNode())).isEmpty()) {
            return randomNodeAssignment(clusterState.nodes());
        } else {
            return new Assignment(null, "only one task can be assigned at a time");
        }
    }

    private Assignment assignBasedOnNonClusterStateCondition(DiscoveryNodes nodes) {
        if (nonClusterStateCondition) {
            return randomNodeAssignment(nodes);
        } else {
            return new Assignment(null, "non-cluster state condition prevents assignment");
        }
    }

    private Assignment randomNodeAssignment(DiscoveryNodes nodes) {
        if (nodes.getNodes().isEmpty()) {
            return NO_NODE_FOUND;
        }
        List<String> nodeList = new ArrayList<>();
        for (ObjectCursor<String> node : nodes.getNodes().keys()) {
            nodeList.add(node.value);
        }
        String node = randomFrom(nodeList);
        if (node != null) {
            return new Assignment(node, "test assignment");
        } else {
            return NO_NODE_FOUND;
        }
    }

    private String dumpEvent(ClusterChangedEvent event) {
        return "nodes_changed: " + event.nodesChanged() +
                " nodes_removed:" + event.nodesRemoved() +
                " routing_table_changed:" + event.routingTableChanged() +
                " tasks: " + event.state().metaData().custom(PersistentTasksCustomMetaData.TYPE);
    }

    private ClusterState significantChange(ClusterState clusterState) {
        ClusterState.Builder builder = ClusterState.builder(clusterState);
        PersistentTasksCustomMetaData tasks = clusterState.getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
        if (tasks != null) {
            if (randomBoolean()) {
                for (PersistentTask<?> task : tasks.tasks()) {
                    if (task.isAssigned() && clusterState.nodes().nodeExists(task.getExecutorNode())) {
                        logger.info("removed node {}", task.getExecutorNode());
                        builder.nodes(DiscoveryNodes.builder(clusterState.nodes()).remove(task.getExecutorNode()));
                        return builder.build();
                    }
                }
            }
        }
        boolean tasksOrNodesChanged = false;
        // add a new unassigned task
        if (hasAssignableTasks(tasks, clusterState.nodes()) == false) {
            // we don't have any unassigned tasks - add some
            if (randomBoolean()) {
                logger.info("added random task");
                addRandomTask(builder, MetaData.builder(clusterState.metaData()), PersistentTasksCustomMetaData.builder(tasks), null);
                tasksOrNodesChanged = true;
            } else {
                logger.info("added unassignable task with custom assignment message");
                addRandomTask(builder, MetaData.builder(clusterState.metaData()), PersistentTasksCustomMetaData.builder(tasks),
                        new Assignment(null, "change me"), "never_assign");
                tasksOrNodesChanged = true;
            }
        }
        // add a node if there are unassigned tasks
        if (clusterState.nodes().getNodes().isEmpty()) {
            logger.info("added random node");
            builder.nodes(DiscoveryNodes.builder(clusterState.nodes()).add(newNode(randomAlphaOfLength(10))));
            tasksOrNodesChanged = true;
        }

        if (tasksOrNodesChanged == false) {
            // change routing table to simulate a change
            logger.info("changed routing table");
            MetaData.Builder metaData = MetaData.builder(clusterState.metaData());
            RoutingTable.Builder routingTable = RoutingTable.builder(clusterState.routingTable());
            changeRoutingTable(metaData, routingTable);
            builder.metaData(metaData).routingTable(routingTable.build());
        }
        return builder.build();
    }

    private PersistentTasksCustomMetaData removeTasksWithChangingAssignment(PersistentTasksCustomMetaData tasks) {
        if (tasks != null) {
            boolean changed = false;
            PersistentTasksCustomMetaData.Builder tasksBuilder = PersistentTasksCustomMetaData.builder(tasks);
            for (PersistentTask<?> task : tasks.tasks()) {
                // Remove all unassigned tasks that cause changing assignments they might trigger a significant change
                if ("never_assign".equals(((TestParams) task.getParams()).getTestParam()) &&
                        "change me".equals(task.getAssignment().getExplanation())) {
                    logger.info("removed task with changing assignment {}", task.getId());
                    tasksBuilder.removeTask(task.getId());
                    changed = true;
                }
            }
            if (changed) {
                return tasksBuilder.build();
            }
        }
        return tasks;
    }

    private ClusterState insignificantChange(ClusterState clusterState) {
        ClusterState.Builder builder = ClusterState.builder(clusterState);
        PersistentTasksCustomMetaData tasks = clusterState.getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
        tasks = removeTasksWithChangingAssignment(tasks);
        PersistentTasksCustomMetaData.Builder tasksBuilder = PersistentTasksCustomMetaData.builder(tasks);

        if (randomBoolean()) {
            if (hasAssignableTasks(tasks, clusterState.nodes()) == false) {
                // we don't have any unassigned tasks - adding a node or changing a routing table shouldn't affect anything
                if (randomBoolean()) {
                    logger.info("added random node");
                    builder.nodes(DiscoveryNodes.builder(clusterState.nodes()).add(newNode(randomAlphaOfLength(10))));
                }
                if (randomBoolean()) {
                    logger.info("added random unassignable task");
                    addRandomTask(builder, MetaData.builder(clusterState.metaData()), tasksBuilder, NO_NODE_FOUND, "never_assign");
                    return builder.build();
                }
                logger.info("changed routing table");
                MetaData.Builder metaData = MetaData.builder(clusterState.metaData());
                metaData.putCustom(PersistentTasksCustomMetaData.TYPE, tasksBuilder.build());
                RoutingTable.Builder routingTable = RoutingTable.builder(clusterState.routingTable());
                changeRoutingTable(metaData, routingTable);
                builder.metaData(metaData).routingTable(routingTable.build());
                return builder.build();
            }
        }
        if (randomBoolean()) {
            // remove a node that doesn't have any tasks assigned to it and it's not the master node
            for (DiscoveryNode node : clusterState.nodes()) {
                if (hasTasksAssignedTo(tasks, node.getId()) == false && "this_node".equals(node.getId()) == false) {
                    logger.info("removed unassigned node {}", node.getId());
                    return builder.nodes(DiscoveryNodes.builder(clusterState.nodes()).remove(node.getId())).build();
                }
            }
        }

        if (randomBoolean()) {
            // clear the task
            if (randomBoolean()) {
                logger.info("removed all tasks");
                MetaData.Builder metaData = MetaData.builder(clusterState.metaData()).putCustom(PersistentTasksCustomMetaData.TYPE,
                        PersistentTasksCustomMetaData.builder().build());
                return builder.metaData(metaData).build();
            } else {
                logger.info("set task custom to null");
                MetaData.Builder metaData = MetaData.builder(clusterState.metaData()).removeCustom(PersistentTasksCustomMetaData.TYPE);
                return builder.metaData(metaData).build();
            }
        }
        logger.info("removed all unassigned tasks and changed routing table");
        if (tasks != null) {
            for (PersistentTask<?> task : tasks.tasks()) {
                if (task.getExecutorNode() == null || "never_assign".equals(((TestParams) task.getParams()).getTestParam())) {
                    tasksBuilder.removeTask(task.getId());
                }
            }
        }
        // Just add a random index - that shouldn't change anything
        IndexMetaData indexMetaData = IndexMetaData.builder(randomAlphaOfLength(10))
                .settings(Settings.builder().put("index.version.created", VersionUtils.randomVersion(random())))
                .numberOfShards(1)
                .numberOfReplicas(1)
                .build();
        MetaData.Builder metaData = MetaData.builder(clusterState.metaData()).put(indexMetaData, false)
                .putCustom(PersistentTasksCustomMetaData.TYPE, tasksBuilder.build());
        return builder.metaData(metaData).build();
    }

    private boolean hasAssignableTasks(PersistentTasksCustomMetaData tasks, DiscoveryNodes discoveryNodes) {
        if (tasks == null || tasks.tasks().isEmpty()) {
            return false;
        }
        return tasks.tasks().stream().anyMatch(task -> {
            if (task.getExecutorNode() == null || discoveryNodes.nodeExists(task.getExecutorNode())) {
                return "never_assign".equals(((TestParams) task.getParams()).getTestParam()) == false;
            }
            return false;
        });
    }

    private boolean hasTasksAssignedTo(PersistentTasksCustomMetaData tasks, String nodeId) {
        return tasks != null && tasks.tasks().stream().anyMatch(
                task -> nodeId.equals(task.getExecutorNode())) == false;
    }

    private ClusterState.Builder addRandomTask(ClusterState.Builder clusterStateBuilder,
                                               MetaData.Builder metaData, PersistentTasksCustomMetaData.Builder tasks,
                                               String node) {
        return addRandomTask(clusterStateBuilder, metaData, tasks, new Assignment(node, randomAlphaOfLength(10)),
                randomAlphaOfLength(10));
    }

    private ClusterState.Builder addRandomTask(ClusterState.Builder clusterStateBuilder,
                                               MetaData.Builder metaData, PersistentTasksCustomMetaData.Builder tasks,
                                               Assignment assignment, String param) {
        return clusterStateBuilder.metaData(metaData.putCustom(PersistentTasksCustomMetaData.TYPE,
                tasks.addTask(UUIDs.base64UUID(), TestPersistentTasksExecutor.NAME, new TestParams(param), assignment).build()));
    }

    private String addTask(PersistentTasksCustomMetaData.Builder tasks, String param, String node) {
        String id = UUIDs.base64UUID();
        tasks.addTask(id, TestPersistentTasksExecutor.NAME, new TestParams(param),
                new Assignment(node, "explanation: " + param));
        return id;
    }

    private DiscoveryNode newNode(String nodeId) {
        final Set<DiscoveryNodeRole> roles =
                Collections.unmodifiableSet(new HashSet<>(Arrays.asList(DiscoveryNodeRole.MASTER_ROLE, DiscoveryNodeRole.DATA_ROLE)));
        return new DiscoveryNode(nodeId, buildNewFakeTransportAddress(), emptyMap(), roles, Version.CURRENT);
    }

    private ClusterState initialState() {
        MetaData.Builder metaData = MetaData.builder();
        RoutingTable.Builder routingTable = RoutingTable.builder();
        int randomIndices = randomIntBetween(0, 5);
        for (int i = 0; i < randomIndices; i++) {
            changeRoutingTable(metaData, routingTable);
        }

        DiscoveryNodes.Builder nodes = DiscoveryNodes.builder();
        nodes.add(DiscoveryNode.createLocal(Settings.EMPTY, buildNewFakeTransportAddress(), "this_node"));
        nodes.localNodeId("this_node");
        nodes.masterNodeId("this_node");

        return ClusterState.builder(ClusterName.DEFAULT)
                .nodes(nodes)
                .metaData(metaData)
                .routingTable(routingTable.build())
                .build();
    }

    private void changeRoutingTable(MetaData.Builder metaData, RoutingTable.Builder routingTable) {
        IndexMetaData indexMetaData = IndexMetaData.builder(randomAlphaOfLength(10))
                .settings(Settings.builder().put("index.version.created", VersionUtils.randomVersion(random())))
                .numberOfShards(1)
                .numberOfReplicas(1)
                .build();
        metaData.put(indexMetaData, false);
        routingTable.addAsNew(indexMetaData);
    }

    /** Creates a PersistentTasksClusterService with a single PersistentTasksExecutor implemented by a BiFunction **/
    private <P extends PersistentTaskParams> PersistentTasksClusterService createService(final BiFunction<P, ClusterState, Assignment> fn) {
        return createService(clusterService, fn);
    }

    private <P extends PersistentTaskParams> PersistentTasksClusterService createService(ClusterService clusterService,
                                                                                         final BiFunction<P, ClusterState, Assignment> fn) {
        PersistentTasksExecutorRegistry registry = new PersistentTasksExecutorRegistry(
            singleton(new PersistentTasksExecutor<P>(TestPersistentTasksExecutor.NAME, null) {
                @Override
                public Assignment getAssignment(P params, ClusterState clusterState) {
                    return fn.apply(params, clusterState);
                }

                @Override
                protected void nodeOperation(AllocatedPersistentTask task, P params, PersistentTaskState state) {
                    throw new UnsupportedOperationException();
                }
            }));
        return new PersistentTasksClusterService(Settings.EMPTY, registry, clusterService, threadPool);
    }
}
