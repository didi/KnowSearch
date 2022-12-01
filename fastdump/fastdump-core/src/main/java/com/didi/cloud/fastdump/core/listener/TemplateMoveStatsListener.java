package com.didi.cloud.fastdump.core.listener;

import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.reader.es.ESTemplateReader;
import com.didi.cloud.fastdump.common.bean.sinker.es.ESTemplateDataSinker;
import com.didi.cloud.fastdump.common.bean.source.es.ESTemplateSource;
import com.didi.cloud.fastdump.common.bean.stats.TemplateMoveTaskStats;
import com.didi.cloud.fastdump.common.bean.taskcontext.es.ESTemplateMoveTaskActionContext;
import com.didi.cloud.fastdump.common.event.es.TemplateMoveStatsEvent;
import com.didi.cloud.fastdump.core.service.metadata.TemplateMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/6
 */
@Component
public class TemplateMoveStatsListener implements ApplicationListener<TemplateMoveStatsEvent> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(TemplateMoveStatsListener.class);
    private final TemplateMoveTaskMetadata templateMoveTaskMetadata;

    public TemplateMoveStatsListener(TemplateMoveTaskMetadata templateMoveTaskMetadata) {
        this.templateMoveTaskMetadata = templateMoveTaskMetadata;
    }

    @Override
    public synchronized void onApplicationEvent(TemplateMoveStatsEvent event) {
        ESTemplateMoveTaskActionContext moveTaskActionContext = event.getEsTemplateMoveTaskActionContext();

        String taskId = moveTaskActionContext.getTaskId();

        ESTemplateSource     source = moveTaskActionContext.getSource();
        ESTemplateReader     reader = moveTaskActionContext.getReader();
        ESTemplateDataSinker sinker = moveTaskActionContext.getSinker();

        TemplateMoveTaskStats templateMoveTaskStats = templateMoveTaskMetadata.getMoveTaskStats(taskId);
        if (null == templateMoveTaskStats) {
            templateMoveTaskStats = new TemplateMoveTaskStats();
            templateMoveTaskStats.setTaskId(taskId);
        }

        templateMoveTaskStats.setSourceTemplate(source.getSourceTemplate());
        templateMoveTaskStats.setSourceCluster(source.getSourceCluster());

        templateMoveTaskStats.setTargetTemplate(sinker.getTargetTemplate());
        templateMoveTaskStats.setTargetCluster(sinker.getTargetCluster());

        templateMoveTaskStats.setStatus(moveTaskActionContext.getStatus());

        templateMoveTaskStats.setGlobalReadFileRateLimit(sinker.getGlobalReadFileRateLimit());

        if (null == templateMoveTaskStats.getSubmitIndexMoveTaskIds()) {
            templateMoveTaskStats.setSubmitIndexMoveTaskIds(new CopyOnWriteArrayList<>());
        }

        boolean exitFlag = templateMoveTaskStats.getSubmitIndexMoveTaskIds().contains(event.getSubmitIndexMoveTaskId());
        if (!exitFlag) {
            templateMoveTaskStats.getSubmitIndexMoveTaskIds().add(event.getSubmitIndexMoveTaskId());
        }

        templateMoveTaskStats.setTotalIndexNum(event.getTotalIndexNum());
        templateMoveTaskMetadata.putTaskStats(taskId, templateMoveTaskStats);
    }
}
