package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.cluster.QuickCommandManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ECSegmentsOnIps;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterQuickCommandEnum;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectRequest;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterQuickCommandEnum.*;

/**
 * 快捷指令实现.
 *
 * @ClassName QuickCommandManagerImpl
 * @Author gyp
 * @Date 2022/6/1
 * @Version 1.0
 */
@Component
public class QuickCommandManagerImpl implements QuickCommandManager {
    /**
     * Arius操作es集群的client
     */
    @Autowired
    protected ESOpClient esOpClient;

    @Override
    public Result<List<Map>> nodeStateAnalysis(String cluster) {
        List<Map> result = new ArrayList<>();
        DirectResponse directResponse = getDirectResponse(cluster, NODE_STATE.getMethod(), NODE_STATE.getUri());
        if (directResponse == null) {
            return null;
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            JSONObject responseJson = JSONObject.parseObject(directResponse.getResponseContent());
            JSONObject nodes = (JSONObject) responseJson.get("nodes");
            nodes.keySet().forEach(key -> {
                Map nodeMap = buildNodeState((JSONObject) nodes.get(key));
                nodeMap.put("node_name", key);
                result.add(nodeMap);
            });
        }
        return Result.buildSucc(result);
    }

    @Override
    public Result<JSONArray> indicesDistribution(String cluster) {
        JSONArray result = new JSONArray();
        DirectResponse directResponse = getDirectResponse(cluster, INDICES.getMethod(), INDICES.getUri());
        if (directResponse == null) {
            return null;
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            JSONArray responseJson = JSONObject.parseArray(directResponse.getResponseContent());
            result = responseJson;
        }
        return Result.buildSucc(result);

    }

    @Override
    public Result<JSONArray> shardDistribution(String cluster) {
        JSONArray result = new JSONArray();
        DirectResponse directResponse = getDirectResponse(cluster, SHARD.getMethod(), SHARD.getUri());
        if (directResponse == null) {
            return null;
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            JSONArray responseJson = JSONObject.parseArray(directResponse.getResponseContent());
            return Result.buildSucc(responseJson);
//            result = responseJson;
        }
        return Result.buildSucc(result);
    }

    @Override
    public Result<List<Map>> pendingTaskAnalysis(String cluster) {
        List<Map> result = new ArrayList<>();
        DirectResponse directResponse = getDirectResponse(cluster, PENDING_TASK.getMethod(), PENDING_TASK.getUri());
        if (directResponse == null) {
            return null;
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            JSONObject responseJson = JSONObject.parseObject(directResponse.getResponseContent());
            JSONArray tasks = responseJson.getJSONArray("tasks");
            for (int i = 0; i < tasks.size(); i++) {
                JSONObject taskObject = (JSONObject) tasks.get(i);
                result.add(buildTask(taskObject));
            }
        }
        return Result.buildSucc(result);
    }

    @Override
    public Result<List<Map>> taskMissionAnalysis(String cluster) {
        List<Map> result = new ArrayList<>();
        DirectResponse directResponse = getDirectResponse(cluster, TASK_MISSION_ANALYSIS.getMethod(), TASK_MISSION_ANALYSIS.getUri());
        if (directResponse == null) {
            return null;
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            JSONObject responseJson = JSONObject.parseObject(directResponse.getResponseContent());
            JSONObject nodes = responseJson.getJSONObject("nodes");
            nodes.keySet().forEach(key -> {
                JSONObject node = (JSONObject) nodes.get(key);
                result.add(buildTaskMission(node, key));
            });
        }
        return Result.buildSucc(result);
    }

    @Override
    public Result<String> hotThreadAnalysis(String cluster) {
        String result = "";
        DirectResponse directResponse = getDirectResponse(cluster, HOT_THREAD.getMethod(), HOT_THREAD.getUri());
        if (directResponse == null) {
            return null;
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            result = directResponse.getResponseContent();
        }
        return Result.buildSucc(result);
    }

    @Override
    public Result<Map> shardAssignmentDescription(String cluster) {
        Map result = new HashMap();
        DirectResponse directResponse = getDirectResponse(cluster, SHARD_ASSIGNMENT.getMethod(), SHARD_ASSIGNMENT.getUri());
        if (directResponse == null) {
            return null;
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            JSONObject responseJson = JSONObject.parseObject(directResponse.getResponseContent());
            result = buildShardAssignment(responseJson);
        }
        return Result.buildSucc(result);
    }

    private Map buildShardAssignment(JSONObject responseJson) {
        Map map = new HashMap();
        map.put("index", responseJson.get("responseJson"));
        map.put("shard ", responseJson.get("shard"));
        map.put("primary ", responseJson.get("primary"));
        map.put("current_state ", responseJson.get("current_state"));
        JSONArray decisionsArray = responseJson.getJSONArray("node_allocation_decisions");
        List<Map> decisions = new ArrayList<>();
        for (int i = 0; i < decisionsArray.size(); i++) {
            Map decisionMap = new HashMap();
            JSONObject decisionObject = decisionsArray.getJSONObject(i);
            decisionMap.put("node_name", decisionObject.get("node_name"));
            decisionMap.put("deciders", decisionObject.get("deciders"));
            decisions.add(decisionMap);
        }
        map.put("current_state ", responseJson.get("current_state"));
        return map;
    }

    @Override
    public Result<Void> abnormalShardAllocationRetry(String cluster) {
        return Result.buildSucc();
    }

    @Override
    public Result<Void> clearFielddataMemory(String cluster) {
        return Result.buildSucc();
    }


    @Nullable
    private DirectResponse getDirectResponse(String cluster, String method, String uri) {
        ESClient client = esOpClient.getESClient(cluster);
        if (null == client) {
            return null;
        }
        DirectRequest directRequest = new DirectRequest(method, uri);
        DirectResponse directResponse = client.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
        return directResponse;
    }

    private Map buildNodeState(JSONObject node) {
        Map nodeMap = new HashMap();
        //获取memoryInBytes
        JSONObject indices = (JSONObject) node.get("indices");
        JSONObject segments = (JSONObject) indices.get("segments");
        //os.cpu
        JSONObject os = (JSONObject) node.get("os");
        JSONObject cpu = (JSONObject) os.get("cpu");
        JSONObject loadAve = (JSONObject) cpu.get("load_average");
        //jvm_heap_used_percent
        JSONObject jvm = (JSONObject) node.get("jvm");
        JSONObject mem = (JSONObject) jvm.get("mem");
        //threads.count
        JSONObject threads = (JSONObject) jvm.get("threads");
        //http_current_open
        JSONObject http = (JSONObject) node.get("http");

        //thread_pool_write_active
        JSONObject thread_pool = (JSONObject) node.get("thread_pool");
        JSONObject write = (JSONObject) thread_pool.get("write");

        JSONObject search = (JSONObject) thread_pool.get("search");
        //thread_pool_management
        JSONObject management = (JSONObject) thread_pool.get("management");

        nodeMap.put("segments_memory", segments.getInteger("memory_in_bytes"));
        nodeMap.put("os_cpu", cpu.getInteger("percent"));
        nodeMap.put("load_average_1m", loadAve.getBigDecimal("1m"));
        nodeMap.put("load_average_5m", loadAve.getBigDecimal("5m"));
        nodeMap.put("load_average_15m", loadAve.getBigDecimal("15m"));
        nodeMap.put("jvm_heap_used_percent", mem.getInteger("heap_used_percent"));
        nodeMap.put("threads.count", threads.getInteger("count"));
        nodeMap.put("current_open", http.getInteger("current_open"));
        nodeMap.put("thread_pool_write_active", write.getInteger("active"));
        nodeMap.put("thread_pool_write_queue", write.getInteger("queue"));
        nodeMap.put("thread_pool_write_reject", write.getInteger("rejected"));
        nodeMap.put("thread_pool_search_active", search.getInteger("active"));
        nodeMap.put("thread_pool_search_queue", search.getInteger("queue"));
        nodeMap.put("thread_pool_search_reject", search.getInteger("rejected"));
        nodeMap.put("thread_pool_management_active", management.getInteger("active"));
        nodeMap.put("thread_pool_management_queue", management.getInteger("queue"));
        nodeMap.put("thread_pool_management_reject", management.getInteger("rejected"));

        return nodeMap;
    }

    private Map buildTask(JSONObject taskObject) {
        Map task = new HashMap();
        task.put("insert_order", taskObject.get("taskObject"));
        task.put("priority", taskObject.get("priority"));
        task.put("source", taskObject.get("source"));
        task.put("time_in_queue_millis", taskObject.get("time_in_queue_millis"));
        task.put("time_in_queue", taskObject.get("time_in_queue"));
        return task;
    }

    private Map buildTaskMission(JSONObject node, String key) {
        Map map = new HashMap();
        JSONObject nodeTasks = (JSONObject) node.get("tasks");
        map.put("node", key);
        map.put("action", nodeTasks.get("action"));
        map.put("description", nodeTasks.get("description "));
        map.put("start_time_in_millis", nodeTasks.get("start_time_in_millis"));
        map.put("running_time_in_nanos", nodeTasks.get("running_time_in_nanos"));
        return map;
    }
}