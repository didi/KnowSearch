package com.didi.cloud.fastdump.core.service.sinker;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.cloud.fastdump.common.bean.adapter.FastDumpBulkInfo;
import com.didi.cloud.fastdump.common.bean.sinker.es.ESIndexDataSinker;
import com.didi.cloud.fastdump.common.bean.stats.IndexNodeMoveTaskStats;
import com.didi.cloud.fastdump.common.client.es.ESRestClient;
import com.didi.cloud.fastdump.common.utils.RetryUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Created by linyunan on 2022/8/10
 */
@Service
public class ESRestDataSinkerService implements DataSinkerService<ESIndexDataSinker, IndexNodeMoveTaskStats, FastDumpBulkInfo> {
    @Value("${print.index.move.info.log.flag:true}")
    private boolean                                                    printIndexMoveInfoLogFlag;
    
    private static final Logger LOGGER  = LoggerFactory.getLogger(ESRestDataSinkerService.class);
    private static final Cache<String/*clusterAddress*/, ESRestClient> ES_REST_CLIENT_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    @Override
    public void beforeSink(ESIndexDataSinker sinker, IndexNodeMoveTaskStats taskStats, FastDumpBulkInfo fastDumpBulkInfo) throws Exception {

    }

    @Override
    public Integer doSink(ESIndexDataSinker      indexDataSinker,
                          IndexNodeMoveTaskStats indexNodeMoveTaskStats,
                          FastDumpBulkInfo       fastDumpBulkInfo) throws Exception {
        String        targetIndex        = indexDataSinker.getTargetIndex();
        String        sourceIndex        = indexDataSinker.getSourceIndex();
        ESRestClient  targetESRestClient = getTargetESRestClient(indexDataSinker);
        return innerWrite(targetESRestClient, indexNodeMoveTaskStats, fastDumpBulkInfo, sourceIndex, targetIndex);
    }

    @Override
    public void afterSink(ESIndexDataSinker sinker, IndexNodeMoveTaskStats taskStats, FastDumpBulkInfo fastDumpBulkInfo) throws Exception {

    }

    @Override
    public void commit(ESIndexDataSinker sinker, IndexNodeMoveTaskStats taskStats, FastDumpBulkInfo fastDumpBulkInfo) throws Exception {

    }

    /*****************************************************private***********************************************************/
    private int innerWrite(ESRestClient           targetESRestClient,
                           IndexNodeMoveTaskStats indexNodeMoveTaskStats,
                           FastDumpBulkInfo       fastDumpBulkInfo,
                           String                 sourceIndex,
                           String                 targetIndex) {
        String   bulkDocFlatToString = fastDumpBulkInfo.getBulkDocFlatToString();
        Integer  bulkDocNum          = fastDumpBulkInfo.getBulkDocNum();

        // 任务是否被中断
        if (indexNodeMoveTaskStats.isInterruptMark()) { return 0;}

        long oneBatchStartTime = System.currentTimeMillis();

        // 执行写入
        String response = RetryUtil.retryWhenExceptionAndFailedThrowRuntimeException(
                "syncRetryBulkWrite",
                100,
                5000,
                () -> {
                    // 任务是否被中断
                    if (indexNodeMoveTaskStats.isInterruptMark()) { return null;}
                    return targetESRestClient.syncRetryBulkWrite(bulkDocFlatToString);
                });

        // 特殊失败场景重试
        AtomicReference<String> finalResp = new AtomicReference<>(response);
        while (!indexNodeMoveTaskStats.isInterruptMark() && hasFailures(finalResp.get())) {
            RetryUtil.retryWhenExceptionAndFailedThrowRuntimeException(
                    "innerWrite",
                    50,
                    3000,
                    ()-> {
                        finalResp.set(targetESRestClient.syncRetryBulkWrite(bulkDocFlatToString));
                        return null;
                    }
            );
        }

        // 写入成功, 记录sinker成功数(原文档成功数)
        if (printIndexMoveInfoLogFlag) {
            LOGGER.info("sourceCluster={}||sourceIndex={}||sourceIndexType={}||" +
                            "targetCluster={}||targetIndex={}||targetIndexType={}" +
                            "||start={}||end={}||documentNum={}||cost(ms)={}",
                    indexNodeMoveTaskStats.getSourceCluster(),
                    sourceIndex,
                    fastDumpBulkInfo.getReaderIndexType(),
                    indexNodeMoveTaskStats.getTargetCluster(),
                    targetIndex,
                    fastDumpBulkInfo.getSinkerIndexType(),
                    fastDumpBulkInfo.getStartPointer(),
                    fastDumpBulkInfo.getEndPointer(),
                    bulkDocNum,
                    System.currentTimeMillis() - oneBatchStartTime);
        }

        return bulkDocNum;
    }

    private ESRestClient getTargetESRestClient(ESIndexDataSinker indexDataSinker) throws ExecutionException {
        String targetClusterAddress  = indexDataSinker.getTargetClusterAddress();
        String targetClusterUserName = indexDataSinker.getTargetClusterUserName();
        String targetClusterPassword = indexDataSinker.getTargetClusterPassword();
        return ES_REST_CLIENT_CACHE.get(targetClusterAddress,
                () -> new ESRestClient(targetClusterAddress, targetClusterUserName, targetClusterPassword));
    }

    private boolean hasFailures(String content) {
        JSONObject restJson = JSON.parseObject(content);
        if (!restJson.getBoolean("errors")) { return false;}

        JSONArray items = restJson.getJSONArray("items");
        for (int i = 0; i < items.size(); i++) {
            JSONObject item = (JSONObject) items.getJSONObject(i).entrySet().iterator().next().getValue();
            if (item.containsKey("error") &&
                    item.getJSONObject("error").getString("type").equals("version_conflict_engine_exception")) {
            } else {
                if (item.containsKey("error")) { return true;}
            }
        }
        return false;
    }
}
