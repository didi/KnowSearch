package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.alibaba.fastjson.JSONArray;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

import java.util.List;
import java.util.Map;

/**
 * 快捷指令.
 *
 * @ClassName QuickCommandManager
 * @Author gyp
 * @Date 2022/6/1
 * @Version 1.0
 */
public interface QuickCommandManager {

    /**
     * node_state分析
     * @param clusterId
     * @return
     */
    Result<List<Map>> nodeStateAnalysis(String clusterId);

    /**
     * indices分布
     * @param cluster
     * @return
     */
    Result<JSONArray> indicesDistribution(String cluster);

    /**
     * shard分布
     * @param cluster
     * @return
     */
    Result<JSONArray> shardDistribution(String cluster);

    /**
     * pending task分析
     * @param cluster
     * @return
     */
    Result<List<Map>> pendingTaskAnalysis(String cluster);

    /**
     * task任务分析
     * @param cluster
     * @return
     */
    Result<List<Map>> taskMissionAnalysis(String cluster);

    /**
     * 热点线程分析
     * @param cluster
     * @return
     */
    Result<String> hotThreadAnalysis(String cluster);

    /**
     * shard分配说明
     * @param cluster
     * @return
     */
    Result<Map> shardAssignmentDescription(String cluster);

    /**
     * 异常shard分配重试
     * @param cluster
     * @return
     */
    Result<Void> abnormalShardAllocationRetry(String cluster);

    /**
     * 清除fielddata内存
     * @param cluster
     * @return
     */
    Result<Void> clearFielddataMemory(String cluster);
}