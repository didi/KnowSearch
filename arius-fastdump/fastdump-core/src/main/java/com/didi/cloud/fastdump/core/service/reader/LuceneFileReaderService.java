package com.didi.cloud.fastdump.core.service.reader;

import static com.didi.cloud.fastdump.common.utils.RetryUtil.DEFAULT_INTERVAL_MILLS;
import static com.didi.cloud.fastdump.common.utils.RetryUtil.DEFAULT_TIME;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.VersionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.didi.cloud.fastdump.common.bean.adapter.FastDumpBulkInfo;
import com.didi.cloud.fastdump.common.bean.lucene.LuceneIndexInfo;
import com.didi.cloud.fastdump.common.bean.metrics.IndexMoveMetrics;
import com.didi.cloud.fastdump.common.bean.reader.es.LuceneReader;
import com.didi.cloud.fastdump.common.bean.reader.es.LuceneReaderWithESIndexSinker;
import com.didi.cloud.fastdump.common.bean.sinker.es.ESIndexDataSinker;
import com.didi.cloud.fastdump.common.bean.stats.IndexNodeMoveTaskStats;
import com.didi.cloud.fastdump.common.component.SpringTool;
import com.didi.cloud.fastdump.common.component.threadmodel.LuceneMoveTaskThreadPoolModel;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.content.Tuple;
import com.didi.cloud.fastdump.common.enums.MetricsLevelEnum;
import com.didi.cloud.fastdump.common.enums.TaskStatusEnum;
import com.didi.cloud.fastdump.common.event.es.IndexShardBulkMoveStatsEvent;
import com.didi.cloud.fastdump.common.event.es.metrics.ESIndexMoveMetricsEvent;
import com.didi.cloud.fastdump.common.exception.BaseException;
import com.didi.cloud.fastdump.common.exception.NotSupportESVersionException;
import com.didi.cloud.fastdump.common.threadpool.FutureUtil;
import com.didi.cloud.fastdump.common.utils.ListUtils;
import com.didi.cloud.fastdump.common.utils.RetryUtil;
import com.didi.cloud.fastdump.common.utils.lucene.LuceneReaderUtil;
import com.didi.cloud.fastdump.common.utils.lucene.RamUsageEstimator;
import com.didi.cloud.fastdump.core.factory.LuceneClientFactory;
import com.didi.cloud.fastdump.core.service.metadata.IndexMoveTaskMetadata;
import com.didi.cloud.fastdump.core.service.sinker.ESRestDataSinkerService;
import com.didi.fastdump.adapter.lucene.LuceneClientAdapter;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;

/**
 * Created by linyunan on 2022/8/10
 */
@Service
public class LuceneFileReaderService implements FileReaderService<LuceneReaderWithESIndexSinker> {
    private static final Logger           LOGGER         = LoggerFactory.getLogger(LuceneFileReaderService.class);

    private static final String           VERSION_TYPE   = VersionType.EXTERNAL_GTE.toString().toLowerCase();

    private static final MemoryMXBean     MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();

    private FutureUtil<Void>              shardReaderFutureUtil;

    private FutureUtil<Void>              shardValidDocReaderFutureUtil;
    @Value("${index.bulk.thread.pool.size:20}")
    public int                            singleBulkPoolSize;
    @Value("${node.concurrent.Handle.shard.num:5}")
    public int                            nodeConcurrentHandleShardNum;
    /**
     * jvm 堆内存最高可用水位线
     */
    @Value("${jvm.heap.mem.protect.percent.threshold:0.2}")
    private double                        jvmHeapMemPercentProtectThreshold;
    /**
     * 单bulk最大文档数
     */
    @Value("${single.bulk.max.doc.num:5000}")
    private int                           singleBulkMaxDocNum;
    @Autowired
    private LuceneClientFactory           luceneClientFactory;
    @Autowired
    private ESRestDataSinkerService       esRestDataSinkerService;
    @Autowired
    private LuceneMoveTaskThreadPoolModel luceneMoveTaskThreadPoolModel;
    @Autowired
    private IndexMoveTaskMetadata         indexMoveTaskMetadata;

    @PostConstruct
    private void init() {
        shardReaderFutureUtil = FutureUtil.init(
                "LuceneShardReader-FutureUtil", 
                nodeConcurrentHandleShardNum, 
                nodeConcurrentHandleShardNum, 
                100);

        shardValidDocReaderFutureUtil = FutureUtil.init(
                "LuceneShardReader-FutureUtil",
                nodeConcurrentHandleShardNum,
                nodeConcurrentHandleShardNum,
                100);
    }

    /**
     *
     * @param luceneReaderWithESIndexSinker         LuceneReaderWithESIndexSinker
     * @throws Exception
     */
    public void parseReader(LuceneReaderWithESIndexSinker luceneReaderWithESIndexSinker) throws Exception {
        try {
            ESIndexDataSinker esIndexDataSinker = luceneReaderWithESIndexSinker.getEsIndexDataSinker();
            List<String>      shardDataPathList = luceneReaderWithESIndexSinker.getShardDataPathList();
            if (CollectionUtils.isEmpty(shardDataPathList)) {
                throw new BaseException(String.format("shardDataPathList[%s] is null", ListUtils.strList2String(shardDataPathList)),
                        ResultType.FAIL);
            }
            executeShardsReader(luceneReaderWithESIndexSinker, esIndexDataSinker, shardDataPathList);
        } finally {
            // 休眠一段时间, 等待gc回收heap mem ???
            System.gc();
            sleep(4000);
        }
    }
    /******************************************private****************************************************/
    private void executeShardsReader(LuceneReader      luceneReader,
                                     ESIndexDataSinker esIndexDataSinker,
                                     List<String>      shardDataPathList)
            throws NotSupportESVersionException, IOException, ExecutionException {

        long startTime = System.currentTimeMillis();
        String taskId  = luceneReader.getTaskId();

        // 1. 根据ESVersion获取不同版本 LuceneClient
        LuceneClientAdapter luceneClientAdapter = luceneClientFactory.getClientByType(luceneReader.getEsVersion());

        // 2. 准备lucene读取需要的信息
        Map<String, Integer> shard2ValidDocMap = getShard2ValidDocMap(
                luceneReader,
                shardDataPathList,
                luceneClientAdapter);

        // 3. 构建任务状态, 透传状态到sinker端
        IndexNodeMoveTaskStats indexNodeMoveTaskStats = buildIndexNodeMoveTaskStats(
                        luceneReader,
                        esIndexDataSinker,
                        shard2ValidDocMap,
                        shardDataPathList);

        Map<String, AtomicLong> shard2SuccSinkDocMap = new ConcurrentHashMap<>();
        // 4. 单个节点上执行同一个index多个shard任务时，需要分组执行, 默认为5个一组
        List<List<String>> shardDataPathListGroup = Lists.partition(shardDataPathList, nodeConcurrentHandleShardNum);
        for (List<String> shardDataPaths : shardDataPathListGroup) {
            // 外部api触发停止任务
            if (indexNodeMoveTaskStats.isInterruptMark()) { break;}

            shard2SuccSinkDocMap.putAll(getShard2SuccSinkDocMap(shardDataPaths));

            // 4.1 根据单个文件大小估估算出每个bulk的文件数量, 避免并发加载大量lucene文件导致的heap oom
            Map<String/*shardDataPath*/, Integer/*singleReadBulkSize*/> shardDataPath2SingleReadBulkSizeMap =
                    estimateShardDataPath2SingleReadBulkSizeMap(
                            luceneReader,
                            esIndexDataSinker,
                            luceneClientAdapter,
                            shardDataPaths,
                            shard2ValidDocMap);

            // 4.2 构建当前节点的lucene indices 文件元信息
            List<LuceneIndexInfo> luceneIndexInfoList =
                    buildLuceneIndexInfos(
                            shardDataPath2SingleReadBulkSizeMap,
                            shardDataPaths,
                            shard2ValidDocMap,
                            taskId,
                            luceneReader.getType(),
                            luceneClientAdapter);

            // 4.3 更新读取文档限流值
            Map<String, Long> shardDataPath2ShardReadFileRateLimitMap =
                    updateAndGetReadFileRateLimitMap(
                            indexNodeMoveTaskStats,
                            luceneIndexInfoList,
                            shardDataPath2SingleReadBulkSizeMap,
                            luceneReader);

            // 4.4 执行一组shard任务(等待一组执行), 开始读取 lucene index 文档, 然后写入sinker端
            executeShardReaderGroup(
                    luceneReader,
                    esIndexDataSinker,
                    taskId,
                    luceneClientAdapter,
                    shard2ValidDocMap,
                    indexNodeMoveTaskStats,
                    shard2SuccSinkDocMap,
                    luceneIndexInfoList,
                    shardDataPath2ShardReadFileRateLimitMap);
        }

        // 全部shard级别任务执行完，记录最终统计信息
        postProcessing(
                luceneReader,
                esIndexDataSinker,
                shard2ValidDocMap,
                indexNodeMoveTaskStats,
                startTime,
                shard2SuccSinkDocMap);
    }

    private Map<String, Integer> getShard2ValidDocMap(LuceneReader        luceneReader,
                                                      List<String>        shardDataPathList,
                                                      LuceneClientAdapter luceneClientAdapter) {
        Map<String/*shardDataPath*/, Integer/*validDoc*/> shard2ValidDocMap = new ConcurrentHashMap<>();
        for (String shardDataPath : shardDataPathList) {
            shardValidDocReaderFutureUtil.runnableTask(() -> {
                try {
                    Integer docNum = luceneClientAdapter.getDocNum(shardDataPath, luceneReader.getType());
                    shard2ValidDocMap.put(shardDataPath, docNum);
                } catch (ExecutionException | IOException e) {
                    LOGGER.error("class=LuceneFileReaderService||method=getShard2ValidDocMap||errMsg=exception", e);
                }
            });
        }
        shardValidDocReaderFutureUtil.waitExecuteUnlimited();
        return shard2ValidDocMap;
    }

    private void executeShardReader(LuceneReader            luceneReader,
                                    ESIndexDataSinker       esIndexDataSinker,
                                    LuceneClientAdapter     luceneClientAdapter,
                                    Map<String, Integer>    shard2ValidDocMap,
                                    IndexNodeMoveTaskStats  indexNodeMoveTaskStats,
                                    Map<String, AtomicLong> shard2SuccSinkDocMap,
                                    LuceneIndexInfo         luceneIndexInfo,
                                    FutureUtil<Void>        segmentBulkFutureUtil) {

        // 多个segment-bulk任务放入shard级别池中运行
        for (LuceneIndexInfo.LuceneSegmentBulkInfo segmentBulkInfo : luceneIndexInfo.getLuceneSegmentBulkInfos()) {
            // 外部api触发停止任务， 退出剩余 segmentBulkInfos
            if (indexNodeMoveTaskStats.isInterruptMark()) {
                segmentBulkFutureUtil.shutdown();
                break;
            }

            RetryUtil.retryWhenExceptionAndFailedThrowRuntimeException(
                    "executeShardReader",
                    100,
                    5000,
                    () -> {
                        if (indexNodeMoveTaskStats.isInterruptMark()) { return null;}

                        int docNum = segmentBulkInfo.getEndPointer() - segmentBulkInfo.getStartPointer();
                        if (0 == docNum) { return null;}

                        indexNodeMoveTaskStats.getReadRateLimiter().acquire(docNum);

                        segmentBulkFutureUtil.runnableTask(() -> {
                            // 执行segment-bulk级别任务
                            executeSegmentBulkReader(luceneIndexInfo, segmentBulkInfo, luceneClientAdapter,
                                    luceneReader, esIndexDataSinker, indexNodeMoveTaskStats, shard2SuccSinkDocMap);

                            // 统计单个shard状态
                            SpringTool.publish(new IndexShardBulkMoveStatsEvent(this,
                                    indexNodeMoveTaskStats, shard2SuccSinkDocMap, shard2ValidDocMap));
                        });
                        return null;});
        }
    }

    private void executeShardReaderGroup(LuceneReader            luceneReader,
                                         ESIndexDataSinker       esIndexDataSinker,
                                         String                  taskId,
                                         LuceneClientAdapter     luceneClientAdapter,
                                         Map<String, Integer>    shard2ValidDocMap,
                                         IndexNodeMoveTaskStats  indexNodeMoveTaskStats,
                                         Map<String, AtomicLong> shard2SuccSinkDocMap,
                                         List<LuceneIndexInfo>   luceneIndexInfos,
                                         Map<String, Long> shardDataPath2ShardReadFileRateLimitMap) {
        for (LuceneIndexInfo luceneIndexInfo : luceneIndexInfos) {
            try {
                if ((shard2ValidDocMap.get(luceneIndexInfo.getLuceneIndexDataPath()) == 0)) { continue;}

                RetryUtil.retryWhenExceptionAndFailedThrowRuntimeException(
                        "executeShardReaderGroup",
                        30,
                        10000,
                        () -> {
                            shardReaderFutureUtil.runnableTask(() -> {
                                FutureUtil<Void> segmentBulkFutureUtil = null;
                                try {
                                    // 获取shard级别可执行线程池，无空闲线程池进行阻塞
                                    segmentBulkFutureUtil = getIdleShardSegmentBulksFutureUtil(
                                            luceneReader,
                                            esIndexDataSinker,
                                            taskId);

                                    // 获取执行线程池成功, shard reader 开始执行
                                    setStatusRunning(indexNodeMoveTaskStats);

                                    executeShardReader(
                                            luceneReader,
                                            esIndexDataSinker,
                                            luceneClientAdapter,
                                            shard2ValidDocMap,
                                            indexNodeMoveTaskStats,
                                            shard2SuccSinkDocMap,
                                            luceneIndexInfo,
                                            segmentBulkFutureUtil);

                                    // 等待池子中的任务全部执行完成
                                    segmentBulkFutureUtil.waitExecuteUnlimited();

                                    // 完成读写, 更新读文件限流值
                                    UpdateReadFileRateLimit(indexNodeMoveTaskStats, shardDataPath2ShardReadFileRateLimitMap, luceneIndexInfo);
                                } catch (Exception e) {
                                    IndexMoveMetrics indexMoveMetrics = IndexMoveMetrics.builder()
                                            .taskId(taskId)
                                            .ip(luceneReader.getIp())
                                            .level(MetricsLevelEnum.ERROR.getLevel())
                                            .sourceIndex(luceneReader.getSourceIndex())
                                            .sourceClusterName(luceneReader.getSourceCluster())
                                            .targetIndex(esIndexDataSinker.getTargetIndex())
                                            .targetClusterName(esIndexDataSinker.getTargetCluster())
                                            .failedLuceneDataPath(luceneIndexInfo.getShardDataPath())
                                            .message(StringUtils.substring(e.getMessage(), 0, 2000))
                                            .build();
                                    SpringTool.publish(new ESIndexMoveMetricsEvent(this, indexMoveMetrics));

                                    LOGGER.error("Class=LuceneFileReaderService||method=parseReader||taskId={}||shardData={}||msg=interruptedException",
                                            taskId,
                                            luceneIndexInfo.getShardDataPath(),
                                            e);
                                } finally {
                                    // 完成一个shard级别任务后，把该线程池置为空闲
                                    luceneMoveTaskThreadPoolModel.release(segmentBulkFutureUtil);
                                }
                            });
                            return null;
                        }
                );
            } catch (Error e) {
                // 触发 error, 如oom 数据写入丢失, 直接终止任务
                indexNodeMoveTaskStats.setInterruptMark(true);
                IndexMoveMetrics indexMoveMetrics = IndexMoveMetrics.builder()
                        .taskId(taskId)
                        .ip(luceneReader.getIp())
                        .level(MetricsLevelEnum.ERROR.getLevel())
                        .sourceIndex(luceneReader.getSourceIndex())
                        .sourceClusterName(luceneReader.getSourceCluster())
                        .targetIndex(esIndexDataSinker.getTargetIndex())
                        .targetClusterName(esIndexDataSinker.getTargetCluster())
                        .failedLuceneDataPath(luceneIndexInfo.getShardDataPath())
                        .message(StringUtils.substring(e.getMessage(), 0, 2000))
                        .build();
                SpringTool.publish(new ESIndexMoveMetricsEvent(this, indexMoveMetrics));

                LOGGER.error("class=LuceneFileReaderService||method=executeShardReaderGroup||errMsg={}",e.getMessage(), e);
            }
        }

        shardReaderFutureUtil.waitExecuteUnlimited();
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ignored) {
        }
    }

    private Map<String, Long> updateAndGetReadFileRateLimitMap(IndexNodeMoveTaskStats indexNodeMoveTaskStats,
                                                               List<LuceneIndexInfo>  luceneIndexInfos,
                                                               Map<String, Integer>   shardDataPath2SingleReadBulkSizeMap,
                                                               LuceneReader           luceneReader) {
        Map<String, Long> shardDataPath2ShardReadFileRateLimitMap = new HashMap<>();
        for (LuceneIndexInfo luceneIndexInfo : luceneIndexInfos) {
            Integer singleReadBulkSize  = shardDataPath2SingleReadBulkSizeMap.get(luceneIndexInfo.getShardDataPath());
            long shardReadFileRateLimit = (long) singleReadBulkSize * singleBulkPoolSize;

            shardDataPath2ShardReadFileRateLimitMap.put(luceneIndexInfo.getShardDataPath(), shardReadFileRateLimit);
        }

        long readFileRateLimit = shardDataPath2ShardReadFileRateLimitMap.values().stream().mapToLong(Long::longValue).sum();

        indexNodeMoveTaskStats.setKernelEstimationReadFileRateLimit(readFileRateLimit);

        if (null == indexNodeMoveTaskStats.getReadRateLimiter()) {
            if (null != luceneReader.getReadFileRateLimit()) {
                readFileRateLimit = luceneReader.getReadFileRateLimit();
                indexNodeMoveTaskStats.getCustomReadFileRateLimitFlag().set(true);
            }
            indexNodeMoveTaskStats.setReadFileRateLimit(new AtomicLong(readFileRateLimit == 0 ? 1 : readFileRateLimit));
            indexNodeMoveTaskStats.setReadRateLimiter(RateLimiter.create(readFileRateLimit == 0 ? 1 : readFileRateLimit));
        } else {
            if (!indexNodeMoveTaskStats.getCustomReadFileRateLimitFlag().get()) {
                indexNodeMoveTaskStats.getReadFileRateLimit().set(readFileRateLimit == 0 ? 1 : readFileRateLimit);
                indexNodeMoveTaskStats.getReadRateLimiter().setRate(readFileRateLimit == 0 ? 1 : readFileRateLimit);
            }
        }

        return shardDataPath2ShardReadFileRateLimitMap;
    }

    private void executeSegmentBulkReader(LuceneIndexInfo luceneIndexInfo,
                                          LuceneIndexInfo.LuceneSegmentBulkInfo segmentBulkInfo,
                                          LuceneClientAdapter    luceneClientAdapter,
                                          LuceneReader           luceneReader,
                                          ESIndexDataSinker      sinker,
                                          IndexNodeMoveTaskStats indexNodeMoveTaskStats,
                                          Map<String, AtomicLong> shard2SuccSinkDocMap) {
        if (indexNodeMoveTaskStats.isInterruptMark()) { return;}

        Boolean ignoreId             = luceneReader.getIgnoreId();
        Boolean ignoreVersion        = luceneReader.getIgnoreVersion();
        String  targetIndex          = sinker.getTargetIndex();
        String  indexMode            = sinker.getIndexMode();
        String  targetClusterVersion = sinker.getTargetClusterVersion();

        String  readerType           = luceneIndexInfo.getType();
        String  sinkerType           = sinker.getTargetIndexType();

        String shardDataPath    = luceneIndexInfo.getShardDataPath();
        int[]  segmentPreSumArr = luceneIndexInfo.getSegmentPreSumArr();

        Integer startPointer   = segmentBulkInfo.getStartPointer();
        Integer endPointer     = segmentBulkInfo.getEndPointer();
        try {
            // 读取 lucene segment
            FastDumpBulkInfo fastDumpBulkInfo = RetryUtil.retryWhenNullOrExceptionAndFailedThrowRuntimeException(
                    "executeSegmentBulkReader",
                    DEFAULT_TIME, 
                    DEFAULT_INTERVAL_MILLS,
                    () -> luceneClientAdapter.getFastDumpBulkInfo(
                                shardDataPath, startPointer, endPointer, segmentPreSumArr,
                                targetIndex, readerType, sinkerType, ignoreVersion, VERSION_TYPE, ignoreId, indexMode, targetClusterVersion));

            if (null == fastDumpBulkInfo) { return;}

            fastDumpBulkInfo.setStartPointer(startPointer);
            fastDumpBulkInfo.setEndPointer(endPointer);

            fastDumpBulkInfo.setReaderIndexType(readerType);
            fastDumpBulkInfo.setSinkerIndexType(sinkerType);

            if (fastDumpBulkInfo.getBulkDocNum() == 0 || indexNodeMoveTaskStats.isInterruptMark()) { return;}

            int succSinkSum = esRestDataSinkerService.doSink(sinker, indexNodeMoveTaskStats, fastDumpBulkInfo);

            shard2SuccSinkDocMap.get(shardDataPath).addAndGet(succSinkSum);
        } catch (Exception e) {
            indexNodeMoveTaskStats.getFailedDocumentNum().addAndGet(endPointer - startPointer);

            IndexMoveMetrics indexMoveMetrics = IndexMoveMetrics.builder()
                    .taskId(luceneIndexInfo.getTaskId())
                    .timestamp(System.currentTimeMillis())
                    .ip(luceneReader.getIp())
                    .level(MetricsLevelEnum.ERROR.getLevel())
                    .sourceIndex(luceneReader.getSourceIndex())
                    .sourceClusterName(luceneReader.getSourceCluster())
                    .targetIndex(sinker.getTargetIndex())
                    .targetClusterName(sinker.getTargetCluster())
                    .failedLuceneDataPath(luceneIndexInfo.getShardDataPath())
                    .message(StringUtils.substring(e.getMessage(), 0, 2000))
                    .build();
            SpringTool.publish(new ESIndexMoveMetricsEvent(this, indexMoveMetrics));

            LOGGER.error("class=LuceneFileReaderService||method=doSingleSegmentReader||shardDataPath={}||errMsg={}",
                    shardDataPath, e.getMessage(), e);
        } catch (Error e) {
            // 触发 error, 如oom 数据写入丢失, 直接终止任务
            indexNodeMoveTaskStats.setInterruptMark(true);

            IndexMoveMetrics indexMoveMetrics = IndexMoveMetrics.builder()
                    .taskId(luceneIndexInfo.getTaskId())
                    .ip(luceneReader.getIp())
                    .level(MetricsLevelEnum.ERROR.getLevel())
                    .sourceIndex(luceneReader.getSourceIndex())
                    .sourceClusterName(luceneReader.getSourceCluster())
                    .targetIndex(sinker.getTargetIndex())
                    .targetClusterName(sinker.getTargetCluster())
                    .failedLuceneDataPath(luceneIndexInfo.getShardDataPath())
                    .message(StringUtils.substring(e.getMessage(), 0, 2000))
                    .build();
            SpringTool.publish(new ESIndexMoveMetricsEvent(this, indexMoveMetrics));

            LOGGER.error("class=LuceneFileReaderService||method=executeShardReader||errMsg={}",e.getMessage(), e);
        }
    }

    private void setStatusRunning(IndexNodeMoveTaskStats indexNodeMoveTaskStats) {
        indexNodeMoveTaskStats.setStatus(TaskStatusEnum.RUNNING.getValue());
        indexNodeMoveTaskStats.setStatusCode(TaskStatusEnum.RUNNING.getCode());
    }

    private FutureUtil<Void> getIdleShardSegmentBulksFutureUtil(LuceneReader      luceneReader,
                                                                ESIndexDataSinker esIndexDataSinker,
                                                                String            taskId) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        FutureUtil<Void> shardSegmentBulksFutureUtil = luceneMoveTaskThreadPoolModel.fetch(null);
        long costTime = System.currentTimeMillis() - startTime;
        if (costTime > 2000) {
            LOGGER.warn("class=LuceneFileReaderService||method=executeLuceneIndexInfoReader" +
                    "||taskId={}||sourceIndex={}({})" +
                    "||targetIndices={}({})||msg=fetch bulk FutureUtil " +
                        "cost(ms):{}",
                    taskId, luceneReader.getSourceIndex(), luceneReader.getSourceCluster(),
                    esIndexDataSinker.getTargetIndex(), esIndexDataSinker.getTargetCluster(),
                costTime);
        }
        return shardSegmentBulksFutureUtil;
    }

    private Map<String, AtomicLong> getShard2SuccSinkDocMap(List<String> shardDataPathList) {
        Map<String, AtomicLong> shard2SuccSinkDocMap = new HashMap<>();
        for (String shardDataPath : shardDataPathList) {
            shard2SuccSinkDocMap.put(shardDataPath, new AtomicLong(0));
        }
        return shard2SuccSinkDocMap;
    }
    
    private IndexNodeMoveTaskStats buildIndexNodeMoveTaskStats(LuceneReader reader,
                                                               ESIndexDataSinker sinker,
                                                               Map<String, Integer> shard2ValidDocMap,
                                                               List<String> shardDataPathList) {

        long validSumDoc = shard2ValidDocMap.values().stream().mapToLong(Integer::longValue).sum();

        String taskId = reader.getTaskId();
        IndexNodeMoveTaskStats indexNodeMoveTaskStats = indexMoveTaskMetadata.getMoveTaskStats(taskId);
        if (null == indexNodeMoveTaskStats) {
            indexNodeMoveTaskStats = new IndexNodeMoveTaskStats();
            indexNodeMoveTaskStats.setTaskId(taskId);
            indexNodeMoveTaskStats.setSourceIndex(reader.getSourceIndex());
            indexNodeMoveTaskStats.setSourceCluster(reader.getSourceCluster());

            indexNodeMoveTaskStats.setTargetIndex(sinker.getTargetIndex());
            indexNodeMoveTaskStats.setTargetCluster(sinker.getTargetCluster());

            indexNodeMoveTaskStats.setStatus(TaskStatusEnum.WAIT.getValue());
            indexNodeMoveTaskStats.setStatusCode(TaskStatusEnum.WAIT.getCode());

            indexNodeMoveTaskStats.setCostTime(0L);

            indexMoveTaskMetadata.putTaskStats(taskId, indexNodeMoveTaskStats);
        }

        indexNodeMoveTaskStats.setFailedShardDataPath(new CopyOnWriteArrayList<>());

        indexNodeMoveTaskStats.setTotalDocumentNum(validSumDoc);

        indexNodeMoveTaskStats.setFailedDocumentNum(new AtomicLong(0));
        indexNodeMoveTaskStats.setSuccDocumentNum(new AtomicLong(0));

        indexNodeMoveTaskStats.setAllShardDataPath(shardDataPathList);
        indexNodeMoveTaskStats.setStatus(TaskStatusEnum.WAIT.getValue());
        indexNodeMoveTaskStats.setStatusCode(TaskStatusEnum.WAIT.getCode());
        indexNodeMoveTaskStats.setCostTime(0L);

        indexNodeMoveTaskStats.setShardNum(shardDataPathList.size());
        indexNodeMoveTaskStats.setSuccShardNum(0);

        indexNodeMoveTaskStats.setDetail("");

        return indexNodeMoveTaskStats;
    }

    /**
     * 后置处理
     *
     * @param luceneReader           reader
     * @param esIndexDataSinker      sinker
     * @param shard2ValidDocMap      shards文档总数
     * @param indexNodeMoveTaskStats 任务状态
     * @param startTime              开始时间
     * @param shard2SuccSinkDocMap   shard成功sinker总数
     */
    private void postProcessing(LuceneReader           luceneReader,
                                ESIndexDataSinker      esIndexDataSinker,
                                Map<String, Integer>   shard2ValidDocMap,
                                IndexNodeMoveTaskStats indexNodeMoveTaskStats,
                                long                    startTime,
                                Map<String, AtomicLong> shard2SuccSinkDocMap) throws UnknownHostException {

        long sumDoc = shard2ValidDocMap.values().stream().mapToLong(Integer::longValue).sum();

        long costTime = System.currentTimeMillis() - startTime;
        long succDocSum = shard2SuccSinkDocMap.values().stream().mapToLong(AtomicLong::get).sum();
        if (succDocSum == sumDoc) {
            // 文档全部sinker完成
            indexNodeMoveTaskStats.getSuccDocumentNum().set(sumDoc);
            indexNodeMoveTaskStats.setStatus(TaskStatusEnum.SUCCESS.getValue());
            indexNodeMoveTaskStats.setStatusCode(TaskStatusEnum.SUCCESS.getCode());
            indexNodeMoveTaskStats.setCostTime(costTime);
            LOGGER.info(
                "class=LuceneFileReaderService||method=postProcessing||sourceIndex={}||success sinker doc num:{},cost(ms)={}",
                luceneReader.getSourceIndex(), sumDoc, costTime);
        } else if (!indexNodeMoveTaskStats.isInterruptMark() && sumDoc > succDocSum) {
            // 任务没有被暂停, 且没有写完, 失败
            indexNodeMoveTaskStats.getFailedDocumentNum().set(sumDoc - succDocSum);
            indexNodeMoveTaskStats.setStatus(TaskStatusEnum.FAILED.getValue());
            indexNodeMoveTaskStats.setStatusCode(TaskStatusEnum.FAILED.getCode());
            indexNodeMoveTaskStats.setCostTime(costTime);

            // 发布事件
            InetAddress localHost = InetAddress.getLocalHost();
            IndexMoveMetrics indexMoveMetrics = IndexMoveMetrics.builder()
                    .taskId(luceneReader.getTaskId())
                    .level(MetricsLevelEnum.ERROR.getLevel())
                    .sourceIndex(luceneReader.getSourceIndex())
                    .ip(localHost.getHostAddress())
                    .sourceClusterName(luceneReader.getSourceCluster())
                    .targetIndex(esIndexDataSinker.getTargetIndex())
                    .targetClusterName(esIndexDataSinker.getTargetCluster())
                    .ip(luceneReader.getIp())
                    .message(String.format(
                    "sourceIndex=%s,succ sinked doc num:%d,failed to sinker doc num: %d, cost(ms)=%d",
                            luceneReader.getSourceIndex(), succDocSum, sumDoc - succDocSum, costTime))
                    .build();
            SpringTool.publish(new ESIndexMoveMetricsEvent(this, indexMoveMetrics));

            LOGGER.error("class=LuceneFileReaderService||method=postProcessing||sourceIndex={}||succ sinked doc num:{}"
                         + ", failed to sinker doc num: {}, cost(ms)={}",
                luceneReader.getSourceIndex(), succDocSum, sumDoc - succDocSum, costTime);
        } else if (indexNodeMoveTaskStats.isInterruptMark()) {
            // 任务被暂停
            String msg = String.format("task[sourceIndex:%s -> targetIndex:%s] has been pause, "
                                       + "success sinker doc num:%s, failed to sinker doc num:%s, cost(ms)=%s",
                luceneReader.getSourceIndex(), esIndexDataSinker.getTargetIndex(),
                    succDocSum, sumDoc - succDocSum, costTime);
            indexNodeMoveTaskStats.setDetail(msg);
            indexNodeMoveTaskStats.setStatus(TaskStatusEnum.PAUSE.getValue());
            indexNodeMoveTaskStats.setStatusCode(TaskStatusEnum.PAUSE.getCode());
            indexNodeMoveTaskStats.setCostTime(costTime);

            LOGGER.warn("class=LuceneFileReaderService||method=postProcessing||sourceIndex={}"
                        + "||msg=task[sourceIndex:{} -> targetIndex:{}] has been pause, ||succ sinked doc num:{}"
                        + ", failed to sinker doc num: {}, cost(ms)={}",
                luceneReader.getSourceIndex(), luceneReader.getSourceIndex(),
                esIndexDataSinker.getTargetIndex(), succDocSum,
                sumDoc - succDocSum, costTime);
        }
    }

    private Map<String/*shardDataPath*/, Integer/*singleReadBulkSize*/> estimateShardDataPath2SingleReadBulkSizeMap(
            LuceneReader         luceneReader,
            ESIndexDataSinker    sinker,
            LuceneClientAdapter  luceneClientAdapter,
            List<String>         shardDataPathList,
            Map<String, Integer> shard2ValidDocMap)
            throws IOException, ExecutionException {
        Map<String/*shardDataPath*/, Integer/*singleReadBulkSize*/> shardDataPath2SizeMap = new HashMap<>();

        MemoryUsage memUsage  = MEMORY_MX_BEAN.getHeapMemoryUsage();
        long        heapMax   = memUsage.getMax() < 0 ? 0 : memUsage.getMax();
        long availableHeapMem = (long) (heapMax * jvmHeapMemPercentProtectThreshold);

        // 将目前节点的可用堆内存划分为多个相同的值的region区域
        long singleShardAvailableHeapMem = availableHeapMem / shardDataPathList.size();

        for (String shardDataPath : shardDataPathList) {
            // 判断有效文档是否为空
            int docNum = shard2ValidDocMap.get(shardDataPath);
            if (docNum == 0) {
                shardDataPath2SizeMap.put(shardDataPath, 0);
                continue;
            }

            long    singleDocMemory    = getSingleDocMemory(luceneReader, sinker, luceneClientAdapter, shardDataPath);
            // 计算单个bulk在可容下的文档数
            int     singleReadBulkDocSize = doCompute(singleDocMemory, singleShardAvailableHeapMem);
            boolean isCover = luceneReader.getSingleReadBulkSize() != null
                              && (luceneReader.getSingleReadBulkSize() < singleReadBulkDocSize);
            shardDataPath2SizeMap.put(shardDataPath, isCover ? luceneReader.getSingleReadBulkSize() : singleReadBulkDocSize);
        }

        return shardDataPath2SizeMap;
    }

    private long getSingleDocMemory(LuceneReader luceneReader,
                                    ESIndexDataSinker sinker,
                                    LuceneClientAdapter luceneClientAdapter,
                                    String shardDataPath) throws ExecutionException, IOException {
        Integer maxDoc = luceneClientAdapter.getMaxDoc(shardDataPath);
        if (0 == maxDoc) { return 0L;}
        long singleDocMemory;

        // TODO： 获取lucene index 前中后中最大的值 作为评估值

        String singleDocInfo = luceneClientAdapter.getSingleDocInfo(
                shardDataPath,
                sinker.getTargetClusterVersion(),
                luceneReader.getType(),
                sinker.getTargetIndex(),
                luceneReader.getIgnoreVersion(),
                VERSION_TYPE,
                luceneReader.getIgnoreId(),
                sinker.getIndexMode());

        singleDocMemory = RamUsageEstimator.sizeOf(singleDocInfo);
        return singleDocMemory;
    }

    private int doCompute(long singleDocMemory, long singleShardAvailableHeapMem) {
        // 计算单个bulk可容下的最大吞吐量(byte)
        long singleBulkAvailableHeapMemMax = singleShardAvailableHeapMem / singleBulkPoolSize;
        // 计算单个bulk在可容下的文档数
        int tempSingleReadBulkSize         = Long.valueOf(singleBulkAvailableHeapMemMax / singleDocMemory).intValue();
        return Math.min(tempSingleReadBulkSize, singleBulkMaxDocNum);
    }

    private List<LuceneIndexInfo> buildLuceneIndexInfos(Map<String/*shardDataPath*/, Integer/*singleReadBulkSize*/>
                                                                shardDataPath2SizeMap,
                                                        List<String> shardDataPathList,
                                                        Map<String, Integer> shard2ValidDocMap,
                                                        String taskId,
                                                        String type,
                                                        LuceneClientAdapter luceneClientAdapter) throws ExecutionException, IOException {
        List<LuceneIndexInfo> luceneIndexInfoList = new ArrayList<>();
        for (String shardDataPath : shardDataPathList) {
            LuceneIndexInfo luceneIndexInfo = new LuceneIndexInfo();
            luceneIndexInfo.setTaskId(taskId);
            luceneIndexInfo.setShardDataPath(shardDataPath);
            luceneIndexInfo.setLuceneIndexDataPath(shardDataPath);
            // 获取segment前缀数组, 根据全局文档编号(递增) 二分查找到相应的segment
            int[] segmentPreSumArr = luceneClientAdapter.getSegmentPreSum(shardDataPath);
            luceneIndexInfo.setSegmentPreSumArr(segmentPreSumArr);
            // 总文档数(文档id递增), 包含删除的文档
            Integer maxDoc         = luceneClientAdapter.getMaxDoc(shardDataPath);
            luceneIndexInfo.setMaxDoc(maxDoc);
            // 有效文档数(文档id离散)
            Integer validDoc       = shard2ValidDocMap.get(shardDataPath);
            luceneIndexInfo.setValidDoc(validDoc);
            // index type
            luceneIndexInfo.setType(type);
            // 根据总数对划分bulk区间: [[0,1000], [1001,2000], [2001,3000]....]
            List<Tuple<Integer/*startPoint*/, Integer/*endPoint*/>> bulkList = LuceneReaderUtil.getBulkList(
                    maxDoc, shardDataPath2SizeMap.get(shardDataPath));

            List<LuceneIndexInfo.LuceneSegmentBulkInfo> luceneSegmentBulkInfos = new ArrayList<>();
            for (Tuple<Integer, Integer> bulkTuple : bulkList) {
                LuceneIndexInfo.LuceneSegmentBulkInfo build = LuceneIndexInfo.LuceneSegmentBulkInfo.builder()
                        .startPointer(bulkTuple.v1())
                        .endPointer(bulkTuple.v2())
                        .build();
                luceneSegmentBulkInfos.add(build);
            }
            luceneIndexInfo.setLuceneSegmentBulkInfos(luceneSegmentBulkInfos);
            luceneIndexInfoList.add(luceneIndexInfo);
        }
        return luceneIndexInfoList;
    }

    private void UpdateReadFileRateLimit(IndexNodeMoveTaskStats indexNodeMoveTaskStats,
                                         Map<String, Long> shardDataPath2ShardReadFileRateLimitMap,
                                         LuceneIndexInfo luceneIndexInfo) {
        // 自定义文件读取限流值，无需计算
        if (indexNodeMoveTaskStats.getCustomReadFileRateLimitFlag().get()) {
            return;
        }

        // 非自定义文件读取限流值, 去除已经完成的shard任务文件读取限流值
        long shardReadFileRateLimit = shardDataPath2ShardReadFileRateLimitMap
                .get(luceneIndexInfo.getShardDataPath());
        AtomicLong readFileRateLimit   = indexNodeMoveTaskStats.getReadFileRateLimit();
        long originalReadFileRateLimit = readFileRateLimit.get();
        readFileRateLimit.set(originalReadFileRateLimit - shardReadFileRateLimit);
    }
}
