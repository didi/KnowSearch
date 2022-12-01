package com.didi.cloud.fastdump.core.action.movetask;

import javax.annotation.PostConstruct;

import com.didi.cloud.fastdump.common.bean.metrics.IndexMoveMetrics;
import com.didi.cloud.fastdump.common.component.SpringTool;
import com.didi.cloud.fastdump.common.enums.MetricsLevelEnum;
import com.didi.cloud.fastdump.common.event.es.IndexShardBulkMoveStatsEvent;
import com.didi.cloud.fastdump.common.event.es.metrics.ESIndexMoveMetricsEvent;
import com.didi.cloud.fastdump.common.utils.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.reader.es.LuceneReader;
import com.didi.cloud.fastdump.common.bean.reader.es.LuceneReaderWithESIndexSinker;
import com.didi.cloud.fastdump.common.bean.sinker.es.ESIndexDataSinker;
import com.didi.cloud.fastdump.common.bean.source.es.ESIndexSource;
import com.didi.cloud.fastdump.common.bean.taskcontext.es.ESIndexMoveTaskActionContext;
import com.didi.cloud.fastdump.common.threadpool.TaskThreadPool;
import com.didi.cloud.fastdump.common.utils.ConvertUtil;
import com.didi.cloud.fastdump.common.utils.RetryUtil;
import com.didi.cloud.fastdump.core.action.Action;
import com.didi.cloud.fastdump.core.service.reader.LuceneFileReaderService;

/**
 * Created by linyunan on 2022/9/22
 */
@Component
public class ESIndexMoveTaskStartAction implements Action<ESIndexMoveTaskActionContext, Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ESIndexMoveTaskStartAction.class);
    @Autowired
    private LuceneFileReaderService luceneFileReaderService;

    protected TaskThreadPool        indexMoveTaskThreadPool;

    @PostConstruct
    private void init(){
        indexMoveTaskThreadPool = new TaskThreadPool();
        // 同一时刻只能运行一个索引任务，否则可能撑爆内存
        indexMoveTaskThreadPool.init(1, 1, "es-index-move-start-threadPool", 1000);
    }
    
    @Override
    public Boolean doAction(ESIndexMoveTaskActionContext taskActionContext) throws Exception {
            RetryUtil.retryWhenExceptionAndFailedThrowRuntimeException(
                    "ESIndexMoveTaskStartAction#doAction",
                    50,
                    10000,
                    () -> {
                        indexMoveTaskThreadPool.run(() -> {
                            try {
                                ESIndexSource     source  = taskActionContext.getSource();
                                LuceneReader      reader  = taskActionContext.getReader();
                                ESIndexDataSinker sinker  = taskActionContext.getSinker();

                                reader.setEsVersion(source.getSourceClusterVersion());
                                reader.setSourceIndex(source.getSourceIndex());
                                reader.setSourceCluster(source.getSourceCluster());
                                sinker.setSourceIndex(source.getSourceIndex());

                                LuceneReaderWithESIndexSinker luceneReaderWithESIndexSinker = ConvertUtil.obj2Obj(reader,
                                        LuceneReaderWithESIndexSinker.class);
                                luceneReaderWithESIndexSinker.setEsIndexDataSinker(sinker);

                                luceneFileReaderService.parseReader(luceneReaderWithESIndexSinker);
                            } catch (Exception e) {
                                IndexMoveMetrics indexMoveMetrics = IndexMoveMetrics.builder()
                                        .taskId(taskActionContext.getTaskId())
                                        .ip(taskActionContext.getReader().getIp())
                                        .level(MetricsLevelEnum.ERROR.getLevel())
                                        .sourceIndex(taskActionContext.getSource().getSourceIndex())
                                        .sourceClusterName(taskActionContext.getSource().getSourceCluster())
                                        .targetIndex(taskActionContext.getSinker().getTargetIndex())
                                        .targetClusterName(taskActionContext.getSinker().getTargetCluster())
                                        .failedLuceneDataPath(ListUtils.strList2String(taskActionContext.getReader().getShardDataPathList()))
                                        .message(StringUtils.substring(e.getMessage(), 0, 2000))
                                        .build();
                                SpringTool.publish(new ESIndexMoveMetricsEvent(this, indexMoveMetrics));
                            LOGGER.error(
                                "class=ESIndexMoveTaskStartAction||method=doAction||errMsg=failed to parseReader", e);
                            }
                        });
                        return null;
            });
        return true;
    }
}
