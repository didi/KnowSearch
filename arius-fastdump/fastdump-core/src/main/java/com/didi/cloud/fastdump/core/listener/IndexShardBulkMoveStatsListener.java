package com.didi.cloud.fastdump.core.listener;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.stats.IndexNodeMoveTaskStats;
import com.didi.cloud.fastdump.common.event.es.IndexShardBulkMoveStatsEvent;

/**
 * Created by linyunan on 2022/9/6
 */
@Component
public class IndexShardBulkMoveStatsListener implements ApplicationListener<IndexShardBulkMoveStatsEvent> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(IndexShardBulkMoveStatsListener.class);

    @Override
    public synchronized void onApplicationEvent(IndexShardBulkMoveStatsEvent event) {
        IndexNodeMoveTaskStats  indexNodeMoveTaskStats = event.getIndexNodeMoveTaskStats();
        Map<String, AtomicLong> shard2SuccSinkDocMap   = event.getShard2SuccSinkDocMap();
        Map<String, Integer>    shard2ValidDocMap      = event.getShard2ValidDocMap();

        int successShardNum = 0;
        long succDocSum = 0;
        for (Map.Entry<String, Integer> e : shard2ValidDocMap.entrySet()) {
            String  shardDataPath = e.getKey();
            Integer shardDocSum   = e.getValue();
            AtomicLong shardSuccSinkDoc = shard2SuccSinkDocMap.get(shardDataPath);
            if (null == shardSuccSinkDoc) { continue;}

            succDocSum += shardSuccSinkDoc.get();
            if (shardDocSum == shardSuccSinkDoc.get()) { successShardNum++;}
        }

        indexNodeMoveTaskStats.setSuccShardNum(successShardNum);
        indexNodeMoveTaskStats.setSuccDocumentNum(new AtomicLong(succDocSum));
    }
}
