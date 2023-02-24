package com.didi.cloud.fastdump.common.event.es;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.didi.cloud.fastdump.common.bean.stats.IndexNodeMoveTaskStats;

/**
 * Created by linyunan on 2022/9/6
 */
public class IndexShardBulkMoveStatsEvent extends BaseMoveStatsEvent {
    private final IndexNodeMoveTaskStats  indexNodeMoveTaskStats;
    private final Map<String, AtomicLong> shard2SuccSinkDocMap;
    private final Map<String, Integer>    shard2ValidDocMap;

    public IndexNodeMoveTaskStats getIndexNodeMoveTaskStats() {
        return indexNodeMoveTaskStats;
    }

    public Map<String, AtomicLong> getShard2SuccSinkDocMap() {
        return shard2SuccSinkDocMap;
    }

    public Map<String, Integer> getShard2ValidDocMap() {
        return shard2ValidDocMap;
    }

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public IndexShardBulkMoveStatsEvent(Object source, IndexNodeMoveTaskStats indexNodeMoveTaskStats,
                                        Map<String, AtomicLong> shard2SuccSinkDocMap,
                                        Map<String, Integer> shard2ValidDocMap) {
        super(source);
        this.indexNodeMoveTaskStats = indexNodeMoveTaskStats;
        this.shard2SuccSinkDocMap = shard2SuccSinkDocMap;
        this.shard2ValidDocMap = shard2ValidDocMap;
    }
}
