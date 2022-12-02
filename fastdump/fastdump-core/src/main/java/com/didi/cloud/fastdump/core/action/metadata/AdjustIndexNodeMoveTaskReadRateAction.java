package com.didi.cloud.fastdump.core.action.metadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.stats.IndexNodeMoveTaskStats;
import com.didi.cloud.fastdump.common.bean.readrate.ReadFileRateInfo;
import com.didi.cloud.fastdump.core.action.Action;
import com.didi.cloud.fastdump.core.service.metadata.IndexMoveTaskMetadata;
import com.google.common.util.concurrent.RateLimiter;

/**
 * Created by linyunan on 2022/9/22
 */
@Component
public class AdjustIndexNodeMoveTaskReadRateAction implements Action<ReadFileRateInfo, Boolean> {
    @Autowired
    private IndexMoveTaskMetadata indexMoveTaskMetadata;

    @Override
    public Boolean doAction(ReadFileRateInfo readFileRateInfo) throws Exception {
        String taskId = readFileRateInfo.getTaskId();
        IndexNodeMoveTaskStats IndexNodeMoveTaskStats = indexMoveTaskMetadata.getMoveTaskStats(taskId);

        RateLimiter readRateLimiter = IndexNodeMoveTaskStats.getReadRateLimiter();
        if (null != readRateLimiter) {
            double readFileRateLimit = readFileRateInfo.getReadFileRateLimit();
            readRateLimiter.setRate(readFileRateLimit);
            IndexNodeMoveTaskStats.getCustomReadFileRateLimitFlag().set(true);
            IndexNodeMoveTaskStats.getReadFileRateLimit().set((long) readFileRateLimit);
        }
        return true;
    }
}
