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
    Result<List<Map>> nodeStateAnalysis(String clusterId);

    Result<JSONArray> indicesDistribution(String cluster);

    Result<JSONArray> shardDistribution(String cluster);

    Result<List<Map>> pendingTaskAnalysis(String cluster);

    Result<List<Map>> taskMissionAnalysis(String cluster);

    Result<String> hotThreadAnalysis(String cluster);

    Result<Map> shardAssignmentDescription(String cluster);

    Result<Void> abnormalShardAllocationRetry(String cluster);

    Result<Void> clearFielddataMemory(String cluster);
}