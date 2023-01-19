package com.didi.cloud.fastdump.core.action.movetask;

import static com.didi.cloud.fastdump.common.utils.BaseHttpUtil.buildHeader;
import static com.didi.cloud.fastdump.common.utils.RetryUtil.DEFAULT_INTERVAL_MILLS;
import static com.didi.cloud.fastdump.common.utils.RetryUtil.DEFAULT_TIME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didi.cloud.fastdump.common.bean.common.Result;
import com.didi.cloud.fastdump.common.bean.reader.es.LuceneReader;
import com.didi.cloud.fastdump.common.bean.sinker.es.ESIndexDataSinker;
import com.didi.cloud.fastdump.common.bean.source.es.ESIndexSource;
import com.didi.cloud.fastdump.common.bean.stats.IndexMoveTaskStats;
import com.didi.cloud.fastdump.common.bean.taskcontext.BaseTaskActionContext;
import com.didi.cloud.fastdump.common.bean.taskcontext.es.ESIndexMoveTaskActionContext;
import com.didi.cloud.fastdump.common.client.es.ESRestClient;
import com.didi.cloud.fastdump.common.component.SpringTool;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.enums.HealthEnum;
import com.didi.cloud.fastdump.common.enums.IndexModeEnum;
import com.didi.cloud.fastdump.common.enums.TaskStatusEnum;
import com.didi.cloud.fastdump.common.event.es.IndexMoveTaskDistributedEvent;
import com.didi.cloud.fastdump.common.event.es.IndexNodeMoveStatsEvent;
import com.didi.cloud.fastdump.common.exception.BaseException;
import com.didi.cloud.fastdump.common.exception.FastDumpTransportException;
import com.didi.cloud.fastdump.common.exception.NotExistFastDumpException;
import com.didi.cloud.fastdump.common.exception.NotSupportESVersionException;
import com.didi.cloud.fastdump.common.threadpool.FutureUtil;
import com.didi.cloud.fastdump.common.threadpool.TaskThreadPool;
import com.didi.cloud.fastdump.common.utils.BaseHttpUtil;
import com.didi.cloud.fastdump.common.utils.ConvertUtil;
import com.didi.cloud.fastdump.common.utils.ESVersionUtil;
import com.didi.cloud.fastdump.common.utils.ListUtils;
import com.didi.cloud.fastdump.common.utils.RetryUtil;
import com.didi.cloud.fastdump.core.action.metadata.GetAllIndexMoveStatsAction;
import com.didi.cloud.fastdump.core.service.metadata.IndexMoveTaskMetadata;
import com.didi.cloud.fastdump.core.service.source.ESFileSourceService;

/**
 * Created by linyunan on 2022/8/24
 */
@Component
public class ESIndexMoveTaskSubmitAction extends BaseMoveTaskAction<ESIndexMoveTaskActionContext> {
    @Value("${fastdump.httpTransport.port:8300}")
    private int                        httpPort;
    @Autowired
    private ESFileSourceService        esFileSourceService;
    @Autowired
    private IndexMoveTaskMetadata      indexMoveTaskMetadata;
    @Autowired
    private GetAllIndexMoveStatsAction getAllIndexMoveStatsAction;

    private static final FutureUtil<Void> TRANSFORM_FUTURE_UTIL = FutureUtil.init("transform-future-util",
            10, 10, 1000);

    @Override
    protected void initMoveTaskThreadPool() {
        moveTaskThreadPool = new TaskThreadPool();
        moveTaskThreadPool.init(5, 5, "es-index-move-submit-threadPool", 100);
    }

    @Override
    public String doAction(BaseTaskActionContext baseTaskActionContext) throws Exception {
        return submit((ESIndexMoveTaskActionContext) baseTaskActionContext);
    }

    @Override
    protected void check(ESIndexMoveTaskActionContext taskActionContext) throws Exception {
        // 检查source参数
        ESIndexSource source = taskActionContext.getSource();
        if (taskActionContext.getSource() == null) {
            throw new BaseException("faled to initTaskContext, esIndexSource is null", ResultType.ILLEGAL_PARAMS);
        }

        if (taskActionContext.getSinker() == null) {
            throw new BaseException("faled to initTaskContext, ESIndexSinker is null", ResultType.ILLEGAL_PARAMS);
        }

        String sourceClusterAddress  = source.getSourceClusterAddress();
        String sourceClusterUserName = source.getSourceClusterUserName();
        String sourceClusterPassword = source.getSourceClusterPassword();
        String sourceCluster;
        try (ESRestClient esRestClient = new ESRestClient(sourceClusterAddress, sourceClusterUserName,
            sourceClusterPassword)) {
            // 检查es版本是否支持
            String esVersion = esRestClient.syncRetryGetClusterVersion(DEFAULT_TIME, DEFAULT_INTERVAL_MILLS);
            if (!ESVersionUtil.checkSupport(esVersion)) {
                throw new NotSupportESVersionException(String.format("source cluster[version:%s] are not supported", esVersion));
            }

            // 检查索引是否存在
            if (!esRestClient.syncRetryCheckIndexExist(source.getSourceIndex(), DEFAULT_TIME, DEFAULT_INTERVAL_MILLS)) {
                throw new BaseException(String.format("index[%s] not exist", source.getSourceIndex()),
                    ResultType.ILLEGAL_PARAMS);
            }

            // 检查索引是否green or yellow, 若是red状态可能导致数据缺失
            if (!source.getIgnoreHealth()) {
                String indexHealth = esRestClient.syncRetryGetIndicesHealthMap(
                        source.getSourceIndex(),
                        DEFAULT_TIME,
                        DEFAULT_INTERVAL_MILLS)
                        .get(source.getSourceIndex());
                if (StringUtils.isNotBlank(indexHealth) && HealthEnum.RED.getDesc().equals(indexHealth)) {
                    throw new BaseException(
                        String.format("index[%s] is red, please reply index health to  yellow or red "
                                      + "or set source.ignoreHealth:true to ignore the red health",
                            source.getSourceIndex()),
                        ResultType.ILLEGAL_PARAMS);
                }
            }
            sourceCluster = esRestClient.syncRetryGetClusterName(DEFAULT_TIME, DEFAULT_INTERVAL_MILLS);

            // 多type检查
            if (StringUtils.isNotBlank(source.getSourceIndexType())) {
                List<String> indexTypeList = esRestClient.syncRetryGetIndexTypeList(
                        source.getSourceIndex(),
                        DEFAULT_TIME,
                        DEFAULT_INTERVAL_MILLS);

                if (!indexTypeList.contains(source.getSourceIndexType())) {
                    throw new BaseException(
                            String.format("index type:%s is non-existent", source.getSourceIndexType()),
                            ResultType.ILLEGAL_PARAMS);
                }
            }
        }

        // 检查reader参数
        LuceneReader reader             = taskActionContext.getReader();
        Integer      singleReadBulkSize = reader.getSingleReadBulkSize();
        if (null != singleReadBulkSize && (singleReadBulkSize < 100 || singleReadBulkSize > 5000)) {
            throw new BaseException(String.format("invalid param, reader.singleReadBulkSize is: %s, please adjust" +
                            "suggest set 100 ~ 5000",
                    singleReadBulkSize), ResultType.ILLEGAL_PARAMS);
        }
        Long readFileRateLimit = reader.getReadFileRateLimit();
        if (null != readFileRateLimit && readFileRateLimit < 1000) {
            throw new BaseException(String.format("invalid param, reader.readFileRateLimit is: %s, please adjust" +
                            "suggest set greater than 1000",
                    readFileRateLimit), ResultType.ILLEGAL_PARAMS);
        }


        // 检查sinker参数
        ESIndexDataSinker sinker          = taskActionContext.getSinker();

        String targetClusterAddress  = sinker.getTargetClusterAddress();
        String targetClusterUserName = sinker.getTargetClusterUserName();
        String targetClusterPassword = sinker.getTargetClusterPassword();
        String targetCluster;
        try (ESRestClient esRestClient = new ESRestClient(targetClusterAddress, targetClusterUserName,
            targetClusterPassword)) {
            targetCluster    = esRestClient.syncRetryGetClusterName(DEFAULT_TIME, DEFAULT_INTERVAL_MILLS);
            String esVersion = esRestClient.syncRetryGetClusterVersion(DEFAULT_TIME, DEFAULT_INTERVAL_MILLS);

            if (!ESVersionUtil.checkSupport(esVersion)) {
                throw new NotSupportESVersionException(String.format("target cluster[version:%s] are not supported", esVersion));
            }

            // 多 type 校验
            if (StringUtils.isNotBlank(sinker.getTargetIndexType()) && !ESVersionUtil.supportMultipleType(esVersion)) {
                throw new BaseException(
                    String.format("multiple types of target cluster[version:%s] are not supported", esVersion),
                    ResultType.ILLEGAL_PARAMS);
            }
        }

        // 检查迁移索引任务是否在运行
        String key = buildIndexMoveTaskKey(source.getSourceIndex(), sourceCluster, sinker.getTargetIndex(),
            targetCluster);
        // 模板级别迁移任务可能会影响这里的校验
        // 如先提交模板级别arius_stats_index_info任务、然后提交索引级别arius_stats_index_info_2022-09-10
        List<IndexMoveTaskStats> indexMoveTaskStatsList = getAllIndexMoveStatsAction.doAction(null);
        if (CollectionUtils.isNotEmpty(indexMoveTaskStatsList)) {
            for (IndexMoveTaskStats taskStats : indexMoveTaskStatsList) {
                if (null == taskStats) { continue;}
                if (key.equals(taskStats.getKey()) && TaskStatusEnum.RUNNING.getValue().equals(taskStats.getStatus())) {
                    throw new BaseException(
                        String.format(
                            "the index move task of sourceIndex[%s] to targetIndex[%s] is running, do not resubmit",
                                source.getSourceIndex(),
                                sinker.getTargetIndex()), ResultType.FAIL);
                }
            }
        }
    }

    @Override
    protected void init(ESIndexMoveTaskActionContext taskActionContext) throws Exception {
        // 初始化source
        ESIndexSource source = taskActionContext.getSource();

        if (taskActionContext.getReader() == null) {
            LuceneReader luceneReader = new LuceneReader();
            luceneReader.setSourceIndex(source.getSourceIndex());
            taskActionContext.setReader(luceneReader);
        }

        // 设置任务唯一标识
        taskActionContext.setTaskId(UUID.randomUUID().toString());
        taskActionContext.setStatus(TaskStatusEnum.WAIT.getValue());

        // 透传taskId到各个shard所在节点
        LuceneReader reader = taskActionContext.getReader();
        reader.setTaskId(taskActionContext.getTaskId());
        Boolean ignoreVersion = reader.getIgnoreVersion();
        Boolean ignoreId      = reader.getIgnoreId();

        // 获取源集群名称
        String sourceClusterAddress  = source.getSourceClusterAddress();
        String sourceClusterUserName = source.getSourceClusterUserName();
        String sourceClusterPassword = source.getSourceClusterPassword();
        try (ESRestClient esRestClient = new ESRestClient(sourceClusterAddress, sourceClusterUserName,
            sourceClusterPassword)) {
            source.setSourceCluster(esRestClient.syncRetryGetClusterName(DEFAULT_TIME, DEFAULT_INTERVAL_MILLS));
        }

        // 初始化sinker
        ESIndexDataSinker sinker = taskActionContext.getSinker();

        // 写入模型更新
        if      (!ignoreVersion && !ignoreId) { sinker.setIndexMode(IndexModeEnum.UPDATE.getMode());}
        else if (ignoreVersion && !ignoreId)  { sinker.setIndexMode(IndexModeEnum.INSERT.getMode());}

        // index type
        if (StringUtils.isNotBlank(source.getSourceIndexType())) {
            reader.setType(source.getSourceIndexType());
        }

        // 获取目标集群名称
        String targetClusterAddress  = sinker.getTargetClusterAddress();
        String targetClusterUserName = sinker.getTargetClusterUserName();
        String targetClusterPassword = sinker.getTargetClusterPassword();
        try (ESRestClient esRestClient = new ESRestClient(targetClusterAddress, targetClusterUserName,
            targetClusterPassword)) {
            sinker.setTargetCluster(esRestClient.syncRetryGetClusterName(DEFAULT_TIME, DEFAULT_INTERVAL_MILLS));
            sinker.setTargetClusterVersion(esRestClient.syncRetryGetClusterVersion(DEFAULT_TIME, DEFAULT_INTERVAL_MILLS));
        }

        // 发布事件, 记录状态信息
        SpringTool.publish(new IndexNodeMoveStatsEvent(this, taskActionContext));
    }

    @Override
    protected void source(ESIndexMoveTaskActionContext taskActionContext) throws Exception {
        esFileSourceService.parseSource(taskActionContext.getSource());
    }

    @Override
    protected void transform(ESIndexMoveTaskActionContext taskActionContext) throws NotExistFastDumpException {
        ESIndexSource esIndexSource = taskActionContext.getSource();
        try {
            Map<String/*ip*/, List<String>/*lucene文件目录*/> ip2LocalLuceneFileDirectoryListMap =
                    getIp2LocalLuceneFileDirectoryListMap(esIndexSource);

            // 根据全局计算局部节点限流量
            computeNodeReadFileRateLimit(taskActionContext, ip2LocalLuceneFileDirectoryListMap);

            // 发布事件, 更新任务所在的执行节点信息
            SpringTool.publish(new IndexMoveTaskDistributedEvent(this, taskActionContext.getTaskId(),
                new ArrayList<>(ip2LocalLuceneFileDirectoryListMap.keySet())));

            // 转发请求前检验流程
            checkNodeFastDumpCheckHealth(ip2LocalLuceneFileDirectoryListMap.keySet());

            // 并发运行任务到各个节点
            doTransform(taskActionContext, ip2LocalLuceneFileDirectoryListMap);
        } catch (Exception e) {
            if (e instanceof NotExistFastDumpException) {
                throw new NotExistFastDumpException(e.getMessage());
            }
            LOGGER.error("class=ESIndexMoveTaskSubmitAction||method=transform||taskId={}||params={}||error={}",
                    taskActionContext.getTaskId(),taskActionContext, e.getMessage(), e);
        }
    }

    /***********************************************private****************************************************/
    private void checkNodeFastDumpCheckHealth(Set<String> ipSet)
            throws NotExistFastDumpException {
        StringBuffer errStringBuffer = new StringBuffer();
        for (String ip : ipSet) {
            TRANSFORM_FUTURE_UTIL.runnableTask(() -> {
                try {
                    String fastDumpCheckHealthURL = buildFastDumpCheckHealthURL(ip);
                    String checkHealth = BaseHttpUtil.get(fastDumpCheckHealthURL, null, buildHeader());
                    Result result = ConvertUtil.str2ObjByJson(checkHealth, Result.class);
                    try {
                        if (result.getCode() != ResultType.FAST_DUMP_EXIST.getCode()) {
                            throw new NotExistFastDumpException(
                                    String.format("node ip[%s] fast-dump action is not exist, please install first it", ip));
                        }
                    } catch (NumberFormatException | BaseException ex) {
                        throw new NotExistFastDumpException(String.format("node ip[%s] fast-dump action is not exist, please install first it", ip));
                    }
                } catch (FastDumpTransportException | NotExistFastDumpException ex) {
                    errStringBuffer.append(ex.getMessage()).append(",");
                }
            });
        }

        TRANSFORM_FUTURE_UTIL.waitExecute(10);
        if (errStringBuffer.length() > 0) {
            throw new NotExistFastDumpException(
                    String.format("the fast-dump app in some node is invalid, errMsg:%s", errStringBuffer));
        }
    }

    private void doTransform(ESIndexMoveTaskActionContext taskActionContext,
                             Map<String, List<String>> ip2LocalLuceneFileDirectoryListMap) {

        for (Map.Entry<String, List<String>> e : ip2LocalLuceneFileDirectoryListMap.entrySet()) {
            TRANSFORM_FUTURE_UTIL.runnableTask(() -> {
                String ip    = e.getKey();
                List<String> luceneFileDirectoryList = e.getValue();
                LuceneReader reader = taskActionContext.getReader();
                LuceneReader luceneReader = ConvertUtil.obj2Obj(reader, LuceneReader.class);
                luceneReader.setIp(ip);
                luceneReader.setShardDataPathList(luceneFileDirectoryList);
                ESIndexMoveTaskActionContext nodeIndexMoveTaskActionContext = ConvertUtil.obj2Obj(taskActionContext, ESIndexMoveTaskActionContext.class);
                nodeIndexMoveTaskActionContext.setReader(luceneReader);

                Result result = Result.buildFail();
                int count = DEFAULT_TIME;
                while (count-- > 0 && result != null && result.failed()) {
                    String resp = RetryUtil.retryWhenNullOrExceptionAndFailedThrowRuntimeException(
                            "doTransform",
                            DEFAULT_TIME,
                            DEFAULT_INTERVAL_MILLS,
                            () -> BaseHttpUtil.postForString(buildIndexMoveStartURL(ip),
                                    JSON.toJSONString(nodeIndexMoveTaskActionContext), buildHeader())
                    );
                     result = ConvertUtil.str2ObjByJson(resp, Result.class);
                }

                LOGGER.info("class=ESIndexMoveTaskSubmitAction||method=transform|||taskId={}|ip={}||shardDataPath={}",
                        nodeIndexMoveTaskActionContext.getTaskId(), ip,
                        ListUtils.strList2String(nodeIndexMoveTaskActionContext.getReader().getShardDataPathList()));
            });
        }
        TRANSFORM_FUTURE_UTIL.waitExecute();
    }

    private void computeNodeReadFileRateLimit(ESIndexMoveTaskActionContext taskActionContext,
                                              Map<String, List<String>> ip2LocalLuceneFileDirectoryListMap) {
        if (null == taskActionContext.getReader().getReadFileRateLimit()) { return;}

        Set<String>  ipSet = ip2LocalLuceneFileDirectoryListMap.keySet();
        LuceneReader reader = taskActionContext.getReader();

        long indexReadFileRateLimit     = reader.getReadFileRateLimit();
        long indexNodeReadFileRateLimit = indexReadFileRateLimit / ipSet.size();
        reader.setReadFileRateLimit(indexNodeReadFileRateLimit);
    }

    private Map<String, List<String>> getIp2LocalLuceneFileDirectoryListMap(ESIndexSource esIndexSource) {
        Map<String/*shardNum*/, String/*ip@dataPath*/> shardNum2DataPathMap = esIndexSource.getShardNum2DataPathMap();
        // 分组：ip->shardPaths
        List<String> ip2DataPathCollection = new ArrayList<>(shardNum2DataPathMap.values());

        // 过滤无用数据 触发gc
        esIndexSource.setShardNum2DataPathMap(null);
        Map<String/*ip*/, List<String>/*dataPaths*/> ip2dataPathsMap = new HashMap<>();
        for (String ip2DataPath : ip2DataPathCollection) {
            String[] ip2DataPathStr = ip2DataPath.split("@");
            String shardIp  = ip2DataPathStr[0];
            String dataPath = ip2DataPathStr[1];
            ip2dataPathsMap.computeIfAbsent(shardIp, a -> new ArrayList<>()).add(dataPath);
        }
        return ip2dataPathsMap;
    }

    private String buildIndexMoveStartURL(String shardIp) {
        return "http://" + shardIp + ":" + httpPort + "/index-move/start";
    }

    private String buildIndexMoveTaskKey(String sourceIndex,
                                         String sourceCluster,
                                         String targetIndex,
                                         String targetCluster) {
        return sourceIndex + "@" + sourceCluster + "@" + targetIndex + "@" + targetCluster;
    }

    private String buildFastDumpCheckHealthURL(String shardIp) {
        return "http://" + shardIp + ":" + httpPort + "/check-health";
    }
}
