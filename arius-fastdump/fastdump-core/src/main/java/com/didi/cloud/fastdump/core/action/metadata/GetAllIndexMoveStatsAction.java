package com.didi.cloud.fastdump.core.action.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.stats.IndexMoveTaskStats;
import com.didi.cloud.fastdump.common.threadpool.FutureUtil;
import com.didi.cloud.fastdump.core.action.Action;
import com.didi.cloud.fastdump.core.service.metadata.IndexMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/8
 */
@Component
public class GetAllIndexMoveStatsAction implements Action<Void, List<IndexMoveTaskStats>> {
    protected static final Logger         LOGGER     = LoggerFactory.getLogger(GetAllIndexMoveStatsAction.class);
    @Autowired
    private IndexMoveTaskMetadata         indexMoveTaskMetadata;
    @Autowired
    private GetIndexMoveStatsAction       getIndexMoveStatsAction;
    private static final FutureUtil<Void> FUTURE_UTIL = FutureUtil.init("DeleteIndexMoveStatsAction-FutureUtil", 5, 5, 1000);

    @Override
    public List<IndexMoveTaskStats> doAction(Void unused) throws Exception {
        List<IndexMoveTaskStats> indexMoveTaskStatsList = new CopyOnWriteArrayList<>();
        List<String> taskIds = indexMoveTaskMetadata.listAllTaskIds();
        if (CollectionUtils.isEmpty(taskIds)) {
            return new ArrayList<>();
        }

        for (String taskId : taskIds) {
            FUTURE_UTIL.runnableTask(() -> {
                try {
                    IndexMoveTaskStats indexMoveTaskStats = getIndexMoveStatsAction.doAction(taskId);
                    if (null == indexMoveTaskStats) { return;}

                    indexMoveTaskStatsList.add(indexMoveTaskStats);
                } catch (Exception e) {
                    LOGGER.error("class=GetAllIndexMoveStatsAction||taskId={}||method=doAction||errMsg={}", taskId,
                        e.getMessage(), e);
                }
            });
        }
        FUTURE_UTIL.waitExecute();
        return indexMoveTaskStatsList;
    }
}
