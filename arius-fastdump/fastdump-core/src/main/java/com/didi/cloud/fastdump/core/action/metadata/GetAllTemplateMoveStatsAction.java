package com.didi.cloud.fastdump.core.action.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.stats.TemplateMoveTaskStats;
import com.didi.cloud.fastdump.common.threadpool.FutureUtil;
import com.didi.cloud.fastdump.core.action.Action;
import com.didi.cloud.fastdump.core.service.metadata.TemplateMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/8
 */
@Component
public class GetAllTemplateMoveStatsAction implements Action<Void, List<TemplateMoveTaskStats>> {
    protected static final Logger         LOGGER     = LoggerFactory.getLogger(GetAllTemplateMoveStatsAction.class);
    @Autowired
    private TemplateMoveTaskMetadata      templateMoveTaskMetadata;
    @Autowired
    private GetTemplateMoveStatsAction    getTemplateMoveStatsAction;
    private static final FutureUtil<Void> FUTURE_UTIL = FutureUtil.init("GetAllTemplateMoveStatsAction-FutureUtil", 5, 5, 1000);

    @Override
    public List<TemplateMoveTaskStats> doAction(Void unused) throws Exception {
        List<String> templateTaskIds = templateMoveTaskMetadata.listAllTaskIds();
        if (CollectionUtils.isEmpty(templateTaskIds)) {
            return new ArrayList<>();
        }

        List<TemplateMoveTaskStats> templateMoveTaskStatsList = new CopyOnWriteArrayList<>();
        for (String taskId : templateTaskIds) {
            FUTURE_UTIL.runnableTask(() -> {
                try {
                    templateMoveTaskStatsList.add(getTemplateMoveStatsAction.doAction(taskId));
                } catch (Exception e) {
                    LOGGER.error("class=GetAllTemplateMoveStatsAction||taskId={}||method=doAction||errMsg={}",
                        taskId, e.getMessage(), e);
                }

            });
        }
        FUTURE_UTIL.waitExecute();
        return templateMoveTaskStatsList;
    }
}
