package com.didi.cloud.fastdump.core.action.movetask;

import static com.didi.cloud.fastdump.common.utils.RetryUtil.DEFAULT_INTERVAL_MILLS;
import static com.didi.cloud.fastdump.common.utils.RetryUtil.DEFAULT_TIME;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.reader.es.ESTemplateReader;
import com.didi.cloud.fastdump.common.bean.reader.es.LuceneReader;
import com.didi.cloud.fastdump.common.bean.sinker.es.ESIndexDataSinker;
import com.didi.cloud.fastdump.common.bean.sinker.es.ESTemplateDataSinker;
import com.didi.cloud.fastdump.common.bean.source.es.ESIndexSource;
import com.didi.cloud.fastdump.common.bean.source.es.ESTemplateSource;
import com.didi.cloud.fastdump.common.bean.stats.IndexMoveTaskStats;
import com.didi.cloud.fastdump.common.bean.stats.TemplateMoveTaskStats;
import com.didi.cloud.fastdump.common.bean.taskcontext.BaseTaskActionContext;
import com.didi.cloud.fastdump.common.bean.taskcontext.es.ESIndexMoveTaskActionContext;
import com.didi.cloud.fastdump.common.bean.taskcontext.es.ESTemplateMoveTaskActionContext;
import com.didi.cloud.fastdump.common.client.es.ESRestClient;
import com.didi.cloud.fastdump.common.component.SpringTool;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.enums.HealthEnum;
import com.didi.cloud.fastdump.common.enums.TaskStatusEnum;
import com.didi.cloud.fastdump.common.event.es.TemplateMoveStatsEvent;
import com.didi.cloud.fastdump.common.exception.BaseException;
import com.didi.cloud.fastdump.common.exception.NotSupportESVersionException;
import com.didi.cloud.fastdump.common.threadpool.FutureUtil;
import com.didi.cloud.fastdump.common.threadpool.TaskThreadPool;
import com.didi.cloud.fastdump.common.utils.ESVersionUtil;
import com.didi.cloud.fastdump.common.utils.RetryUtil;
import com.didi.cloud.fastdump.core.action.metadata.GetAllTemplateMoveStatsAction;
import com.didi.cloud.fastdump.core.action.metadata.GetIndexMoveStatsAction;
import com.didi.cloud.fastdump.core.service.metadata.TemplateMoveTaskMetadata;

/**
 * Created by linyunan on 2022/9/13
 */
@Component
public class ESTemplateMoveTaskSubmitAction extends BaseMoveTaskAction<ESTemplateMoveTaskActionContext> {
    @Autowired
    private GetIndexMoveStatsAction       getIndexMoveStatsAction;
    @Autowired
    private ESIndexMoveTaskSubmitAction   esIndexMoveTaskSubmitAction;
    @Autowired
    private TemplateMoveTaskMetadata      templateMoveTaskMetadata;

    @Autowired
    private GetAllTemplateMoveStatsAction getAllTemplateMoveStatsAction;

    private static final FutureUtil<Void>  FUTURE_UTIL = FutureUtil.init("es-template-index-move-future-util",
            10, 10, 100);

    @Override
    public String doAction(BaseTaskActionContext baseTaskActionContext) throws Exception {
        return submit((ESTemplateMoveTaskActionContext) baseTaskActionContext);
    }

    @Override
    protected void initMoveTaskThreadPool() {
        moveTaskThreadPool = new TaskThreadPool();
        // 模板迁移单线程，否则撑爆内存
        moveTaskThreadPool.init(1, 1, "es-template-move-threadPool", 1000);
    }

    @Override
    protected String submit(ESTemplateMoveTaskActionContext taskActionContext) throws Exception {
        // 1、参数检验
        check(taskActionContext);
        // 2、初始化
        init(taskActionContext);
        // 3、构建索引子任务
        List<ESIndexMoveTaskActionContext> esIndexMoveTaskActionContextList = buildESIndexMoveTaskActionContext(
            taskActionContext);
        if (CollectionUtils.isEmpty(esIndexMoveTaskActionContextList)) {
            return taskActionContext.getTaskId();
        }

        // 4、提交索引任务
        RetryUtil.retryWhenNullOrExceptionAndFailedThrowRuntimeException(
                "ESTemplateMoveTaskSubmitAction#submit",
                50,
                10000,
                () -> {
                    moveTaskThreadPool.run(() -> {
                        try {
                            innerSubmit(esIndexMoveTaskActionContextList, taskActionContext);
                        } catch (Exception e) {
                            LOGGER.warn("class=ESTemplateMoveTaskSubmitAction||method=submit||errMsg={}", e.getMessage(), e);
                        }
                    });
                    return null;
                }
        );

        return taskActionContext.getTaskId();
    }

    @Override
    protected void check(ESTemplateMoveTaskActionContext taskActionContext) throws Exception {
        // 检验模板是否存在
        ESTemplateSource source = taskActionContext.getSource();
        if (source == null) {
            throw new BaseException("faled to initTaskContext, esIndexSource is null", ResultType.ILLEGAL_PARAMS);
        }

        if (taskActionContext.getSinker() == null) {
            throw new BaseException("faled to initTaskContext, ESIndexSinker is null", ResultType.ILLEGAL_PARAMS);
        }

        String sourceClusterAddress  = source.getSourceClusterAddress();
        String sourceClusterUserName = source.getSourceClusterUserName();
        String sourceClusterPassword = source.getSourceClusterPassword();
        String sourceCluster;
        int sourceIndicesNum = 0;
        try (ESRestClient sourceESRestClient = new ESRestClient(sourceClusterAddress, sourceClusterUserName,
            sourceClusterPassword)) {
            // 检查es版本是否支持
            String esVersion = sourceESRestClient.syncRetryGetClusterVersion(DEFAULT_TIME, DEFAULT_INTERVAL_MILLS);
            if (!ESVersionUtil.checkSupport(esVersion)) {
                throw new NotSupportESVersionException(esVersion);
            }

            // 检查源模板是否存在
            if (!sourceESRestClient.syncRetryCheckTemplateExist(
                    source.getSourceTemplate(),
                    DEFAULT_TIME,
                    DEFAULT_INTERVAL_MILLS)) {
                throw new BaseException(String.format("source-template[%s] not exist", source.getSourceTemplate()),
                    ResultType.ILLEGAL_PARAMS);
            }

            // 检查源模板下的索引是否 green or yellow
            if (!source.getIgnoreHealth()) {
                String sourceTemplate = source.getSourceTemplate();
                Map<String/*indexName*/, String/*health*/> indicesHealthMap =
                        sourceESRestClient.syncRetryGetIndicesHealthMap(
                                sourceTemplate + "*",
                                DEFAULT_TIME,
                                DEFAULT_INTERVAL_MILLS);

                sourceIndicesNum = indicesHealthMap.keySet().size();


                for (Map.Entry<String, String> e : indicesHealthMap.entrySet()) {
                    String index = e.getKey();
                    String health = e.getValue();
                    if (StringUtils.isNotBlank(health) && HealthEnum.RED.getDesc().equals(health)) {
                        throw new BaseException(
                            String.format("index[%s] is red, please reply index health to  yellow or red "
                                          + "or set source.ignoreHealth:true to ignore the red health",
                                index),
                            ResultType.ILLEGAL_PARAMS);
                    }
                }
            }
            sourceCluster = sourceESRestClient.syncRetryGetClusterName(DEFAULT_TIME, DEFAULT_INTERVAL_MILLS);
        }

        ESTemplateReader reader = taskActionContext.getReader();

        // 检查reader参数
        Integer      singleReadBulkSize = reader.getSingleReadBulkSize();
        if (singleReadBulkSize < 100 || singleReadBulkSize > 20000) {
            throw new BaseException(String.format("invalid param, reader.singleReadBulkSize is: %s, please adjust" +
                            "suggest set 1000 ~ 5000",
                    singleReadBulkSize), ResultType.ILLEGAL_PARAMS);
        }

        ESTemplateDataSinker sinker = taskActionContext.getSinker();
        // 检查限流值
        long globalReadFileRateLimit = sinker.getGlobalReadFileRateLimit();
        long singleIndexGlobalReadRate = globalReadFileRateLimit / sourceIndicesNum;
        if (singleIndexGlobalReadRate < 5000) {
            throw new BaseException(
                    String.format("sourceTemplate[%s] has %d indices, " +
                                    "singleIndexGlobalReadRate is %d"
                                    + " please adjust globalReadFileRateLimit between %d ~ %d",
                            source.getSourceTemplate(), sourceIndicesNum, singleIndexGlobalReadRate,
                            5000 * sourceIndicesNum, 500000 * sourceIndicesNum),
                    ResultType.ILLEGAL_PARAMS);
        }

        // 检查目标模板是否存在
        String targetTemplate = sinker.getTargetTemplate();
        String targetClusterAddress = sinker.getTargetClusterAddress();
        String targetClusterUserName = sinker.getTargetClusterUserName();
        String targetClusterPassword = sinker.getTargetClusterPassword();
        String targetCluster;
        try (ESRestClient targetESRestClient = new ESRestClient(targetClusterAddress, targetClusterUserName,
            targetClusterPassword)) {
            targetCluster = targetESRestClient.syncRetryGetClusterName(DEFAULT_TIME, DEFAULT_INTERVAL_MILLS);
            if (!targetESRestClient.syncRetryCheckTemplateExist(targetTemplate, DEFAULT_TIME, DEFAULT_INTERVAL_MILLS)) {
                throw new BaseException(String
                    .format("target-template[%s] is not exist, please create a new template in target cluster[%s],"
                            + "must make sure the target template information(mapping setting) is consistent with the source template information!!!",
                        targetTemplate, targetClusterAddress),
                    ResultType.ILLEGAL_PARAMS);
            }
        }

        // 检查迁移模板任务是否在运行
        String key = buildTemplateMoveTaskKey(source.getSourceTemplate(), sourceCluster, sinker.getTargetTemplate(),
            targetCluster);
        List<TemplateMoveTaskStats> templateMoveTaskStats = getAllTemplateMoveStatsAction.doAction(null);
        if (CollectionUtils.isNotEmpty(templateMoveTaskStats)) {
            for (TemplateMoveTaskStats taskStats : templateMoveTaskStats) {
                if (key.equals(taskStats.getKey()) && TaskStatusEnum.RUNNING.getValue().equals(taskStats.getStatus())) {
                    throw new BaseException(String.format(
                        "the template move task[%s] is running, do not resubmit", key), ResultType.FAIL);
                }
            }
        }
    }

    @Override
    protected void init(ESTemplateMoveTaskActionContext taskActionContext) throws Exception {
        ESTemplateSource source = taskActionContext.getSource();

        if (taskActionContext.getReader() == null) {
            ESTemplateReader esTemplateReader = new ESTemplateReader();
            esTemplateReader.setSourceTemplate(taskActionContext.getSource().getSourceTemplate());
            taskActionContext.setReader(esTemplateReader);
        }

        // 设置任务唯一标识
        taskActionContext.setTaskId(UUID.randomUUID().toString());
        taskActionContext.setStatus(TaskStatusEnum.WAIT.getValue());

        // 获取源集群名称
        String sourceClusterAddress = source.getSourceClusterAddress();
        String sourceClusterUserName = source.getSourceClusterUserName();
        String sourceClusterPassword = source.getSourceClusterPassword();
        try (ESRestClient esRestClient = new ESRestClient(sourceClusterAddress, sourceClusterUserName, sourceClusterPassword)) {
            source.setSourceCluster(esRestClient.syncRetryGetClusterName(DEFAULT_TIME, DEFAULT_INTERVAL_MILLS));
        }

        // 初始化sinker
        ESTemplateDataSinker sinker = taskActionContext.getSinker();
        // 获取目标集群名称
        String targetClusterAddress = sinker.getTargetClusterAddress();
        String targetClusterUserName = sinker.getTargetClusterUserName();
        String targetClusterPassword = sinker.getTargetClusterPassword();
        try (ESRestClient esRestClient = new ESRestClient(targetClusterAddress, targetClusterUserName,
            targetClusterPassword)) {
            sinker.setTargetCluster(esRestClient.syncRetryGetClusterName(DEFAULT_TIME, DEFAULT_INTERVAL_MILLS));
        }
    }

    @Override
    protected void source(ESTemplateMoveTaskActionContext taskActionContext) throws Exception {
        // do nothing
    }

    @Override
    protected void transform(ESTemplateMoveTaskActionContext taskActionContext) throws Exception {
        // do nothing
    }

    /***********************************************private****************************************************/
    private String buildTemplateMoveTaskKey(String sourceTemplate, String sourceCluster, String targetTemplate,
                                            String targetCluster) {
        return sourceTemplate + "@" + sourceCluster + "@" + targetTemplate + "@" + targetCluster;
    }

    private List<ESIndexMoveTaskActionContext> buildESIndexMoveTaskActionContext(ESTemplateMoveTaskActionContext templateMoveTaskActionContext)
            throws Exception {
        ESTemplateSource     templateSource  = templateMoveTaskActionContext.getSource();
        ESTemplateReader     templateReader  = templateMoveTaskActionContext.getReader();
        ESTemplateDataSinker templateSinker  = templateMoveTaskActionContext.getSinker();

        String sourceClusterAddress  = templateSource.getSourceClusterAddress();
        String sourceClusterUserName = templateSource.getSourceClusterUserName();
        String sourceClusterPassword = templateSource.getSourceClusterPassword();

        Integer singleReadBulkSize = templateReader.getSingleReadBulkSize();

        String targetClusterAddress  = templateSinker.getTargetClusterAddress();
        String targetClusterUserName = templateSinker.getTargetClusterUserName();
        String targetClusterPassword = templateSinker.getTargetClusterPassword();

        List<ESIndexMoveTaskActionContext> esIndexMoveTaskActionContexts = new ArrayList<>();
        List<String> indicesByTemplate;
        // 根据模板获取索引列表
        try (ESRestClient esRestClient = new ESRestClient(sourceClusterAddress, sourceClusterUserName,
            sourceClusterPassword)) {
            indicesByTemplate = new ArrayList<>(esRestClient.syncRetryGetIndicesByTemplate(
                        templateSource.getSourceTemplate(),
                        DEFAULT_TIME,
                        DEFAULT_INTERVAL_MILLS));
        }

        if (CollectionUtils.isEmpty(indicesByTemplate)) {
            return esIndexMoveTaskActionContexts;
        }

        // 计算索引级别限流值
        long globalReadFileRateLimit  = templateSinker.getGlobalReadFileRateLimit();
        long indexReadFileRateLimit   = globalReadFileRateLimit / indicesByTemplate.size();

        for (String index : indicesByTemplate) {
            ESIndexMoveTaskActionContext indexMoveTaskActionContext = new ESIndexMoveTaskActionContext();
            // 构建index source
            ESIndexSource indexSource = new ESIndexSource();
            indexMoveTaskActionContext.setSource(indexSource);
            indexSource.setSourceIndex(index);
            indexSource.setSourceClusterAddress(sourceClusterAddress);
            indexSource.setSourceClusterUserName(sourceClusterUserName);
            indexSource.setSourceClusterPassword(sourceClusterPassword);

            // 构建index reader
            LuceneReader indexReader = new LuceneReader();
            indexMoveTaskActionContext.setReader(indexReader);
            indexReader.setSourceIndex(index);
            indexReader.setSingleReadBulkSize(singleReadBulkSize);
            indexReader.setIgnoreId(templateReader.getIgnoreId());
            indexReader.setIgnoreVersion(templateReader.getIgnoreVersion());
            indexReader.setReadFileRateLimit(indexReadFileRateLimit);

            // 构建index sinker
            ESIndexDataSinker indexSinker = new ESIndexDataSinker();
            indexMoveTaskActionContext.setSinker(indexSinker);
            indexSinker.setSourceIndex(index);
            indexSinker.setTargetIndex(index);
            indexSinker.setTargetClusterAddress(targetClusterAddress);
            indexSinker.setTargetClusterUserName(targetClusterUserName);
            indexSinker.setTargetClusterPassword(targetClusterPassword);

            esIndexMoveTaskActionContexts.add(indexMoveTaskActionContext);
        }

        return esIndexMoveTaskActionContexts;
    }

    private void innerSubmit(List<ESIndexMoveTaskActionContext> esIndexMoveTaskActionContextsList,
                             ESTemplateMoveTaskActionContext    taskActionContext) throws Exception {
        int totalIndexNum = esIndexMoveTaskActionContextsList.size();

        // 串行运行
        for (ESIndexMoveTaskActionContext esIndexMoveTaskActionContext : esIndexMoveTaskActionContextsList) {
            ESIndexSource     source = esIndexMoveTaskActionContext.getSource();

            // 模板任务是否需要中断
            TemplateMoveTaskStats moveTaskStats = templateMoveTaskMetadata.getMoveTaskStats(taskActionContext.getTaskId());
            if (null != moveTaskStats && moveTaskStats.isInterruptMark()) {
                LOGGER.warn(
                        "class=ESTemplateMoveTaskAction||method=batchInnerSubmit||pause move task source-index:{} to target-index:{}",
                        source.getSourceIndex(), source.getSourceIndex());
                return;
            }

            String submitIndexMoveTaskId = esIndexMoveTaskSubmitAction.submit(esIndexMoveTaskActionContext);

            // 发布事件, 记录状态信息
            SpringTool.publish(new TemplateMoveStatsEvent(this, taskActionContext, submitIndexMoveTaskId, totalIndexNum));

            // 等待提交的索引任务完成
            waitIndexMoveTaskSucc(submitIndexMoveTaskId);
        }
    }

    private void waitIndexMoveTaskSucc(String indexMoveTaskId) throws Exception {
        // 首次执行等待2s, 等待状态同步
        Thread.sleep(2000);

        while (true) {
            IndexMoveTaskStats indexMoveTaskStats = getIndexMoveStatsAction.doAction(indexMoveTaskId);
            if (null != indexMoveTaskStats
                && TaskStatusEnum.RUNNING.getValue().equals(indexMoveTaskStats.getStatus())) {

                Thread.sleep(8000);
            } else {
                break;
            }
        }
    }
}
