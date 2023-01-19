package org.elasticsearch.cluster.routing.allocation.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.routing.allocation.AllocationService;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.threadpool.ThreadPool;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoExpandReplicasService extends AbstractLifecycleComponent implements ClusterStateListener {
    private static final Logger logger = LogManager.getLogger(AutoExpandReplicasService.class);

    public static final Setting<TimeValue> CLUSTER_ROUTING_ALLOCATION_AUTO_EXPAND_REPLICAS_REROUTE_INTERVAL_SETTING =
        Setting.positiveTimeSetting("cluster.routing.allocation.auto_expand_replicas.reroute_interval", TimeValue.timeValueHours(2),
            Setting.Property.Dynamic, Setting.Property.NodeScope);
    public static final Setting<TimeValue> CLUSTER_ROUTING_ALLOCATION_AUTO_EXPAND_REPLICAS_SCHEDULE_INTERVAL_SETTING =
        Setting.positiveTimeSetting("cluster.routing.allocation.auto_expand_replicas.schedule_interval", TimeValue.timeValueMinutes(1),
            Setting.Property.Dynamic, Setting.Property.NodeScope);

    private final ThreadPool threadPool;
    private final ClusterService clusterService;
    private final AllocationService allocationService;
    private final AutoExpandReplicasClusterStateUpdateTask clusterStateUpdateTask;
    private final AtomicBoolean state = new AtomicBoolean(false);

    private long lastRunNS;
    private TimeValue rerouteInterval;
    private TimeValue scheduleInterval;
    private ThreadPool.Cancellable cancellable;

    public AutoExpandReplicasService(Settings settings, ThreadPool threadPool, ClusterService clusterService, AllocationService allocationService) {
        this.clusterService = clusterService;
        this.allocationService = allocationService;
        this.clusterStateUpdateTask = new AutoExpandReplicasClusterStateUpdateTask();
        this.threadPool = threadPool;
        this.lastRunNS = System.nanoTime();
        this.rerouteInterval = CLUSTER_ROUTING_ALLOCATION_AUTO_EXPAND_REPLICAS_REROUTE_INTERVAL_SETTING.get(settings);
        this.scheduleInterval = CLUSTER_ROUTING_ALLOCATION_AUTO_EXPAND_REPLICAS_SCHEDULE_INTERVAL_SETTING.get(settings);
        this.clusterService.addListener(this);
        clusterService.getClusterSettings().addSettingsUpdateConsumer(CLUSTER_ROUTING_ALLOCATION_AUTO_EXPAND_REPLICAS_REROUTE_INTERVAL_SETTING, this::setRerouteInterval);
        clusterService.getClusterSettings().addSettingsUpdateConsumer(CLUSTER_ROUTING_ALLOCATION_AUTO_EXPAND_REPLICAS_SCHEDULE_INTERVAL_SETTING, this::setScheduleInterval);
    }

    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        if (event.state().getNodes().isLocalNodeElectedMaster() && state.get()
            && event.previousState().getNodes().isLocalNodeElectedMaster() == false) {
            state.set(false);
        }
    }

    private ThreadPool.Cancellable createCancellable(ThreadPool threadPool, ClusterService clusterService, TimeValue timeValue) {
        return threadPool.scheduleWithFixedDelay(() -> {
            if ((System.nanoTime() - lastRunNS) > rerouteInterval.nanos() && state.compareAndSet(false, true)) {
                clusterService.submitStateUpdateTask("auto_expand_replicas", clusterStateUpdateTask);
            }
        }, timeValue, ThreadPool.Names.MANAGEMENT);
    }

    private void setRerouteInterval(TimeValue rerouteInterval) {
        this.rerouteInterval = rerouteInterval;
    }

    private void setScheduleInterval(TimeValue scheduleInterval) {
        this.cancellable.cancel();
        this.scheduleInterval = scheduleInterval;
        this.cancellable = createCancellable(threadPool, clusterService, scheduleInterval);
    }

    @Override
    protected void doStart() {
        this.cancellable = createCancellable(threadPool, clusterService, scheduleInterval);
    }

    @Override
    protected void doStop() {
        this.cancellable.cancel();
    }

    @Override
    protected void doClose() throws IOException {
        this.cancellable.cancel();
    }

    class AutoExpandReplicasClusterStateUpdateTask extends ClusterStateUpdateTask {

        @Override
        public ClusterState execute(ClusterState currentState) throws Exception {
            ClusterState clusterState = allocationService.adaptAutoExpandReplicas(currentState);
            lastRunNS = System.nanoTime();
            state.compareAndSet(true, false);
            return clusterState;
        }

        @Override
        public void onFailure(String source, Exception e) {
            logger.error("[source: {}] auto expand replicas throws exception: {}", source, e);
        }
    }
}
