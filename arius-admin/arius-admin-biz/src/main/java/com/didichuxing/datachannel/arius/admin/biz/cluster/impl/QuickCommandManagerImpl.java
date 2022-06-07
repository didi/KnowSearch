package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.cluster.QuickCommandManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ECSegmentsOnIps;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.PendingTaskVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterQuickCommandEnum;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
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
    @Autowired
    protected ClusterPhyService clusterPhyService;



    @Override
    public Result<List<NodeStateVO>> nodeStateAnalysis(String cluster) {
        DirectResponse directResponse = getDirectResponse(cluster, NODE_STATE.getMethod(), NODE_STATE.getUri());
        if (directResponse == null) {
            return Result.buildFail();
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            List<NodeStateVO> result = new ArrayList<>();
            JSONObject responseJson = JSONObject.parseObject(directResponse.getResponseContent());
            JSONObject nodes = (JSONObject) responseJson.get("nodes");
            nodes.keySet().forEach(key -> {
                NodeStateVO nodeMap = buildNodeState((JSONObject) nodes.get(key));
                nodeMap.setNodeName(key);
                result.add(nodeMap);
            });
            return Result.buildSucc(result);
        }
        return Result.buildFail();
    }

    @Override
    public Result<List<IndicesDistributionVO>> indicesDistribution(String cluster) {
        DirectResponse directResponse = getDirectResponse(cluster, INDICES.getMethod(), INDICES.getUri());
        if (directResponse == null) {
            return Result.buildFail();
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            JSONArray responseJson = JSONObject.parseArray(directResponse.getResponseContent());
            return Result.buildSucc(buildIndicesDistributionVO(responseJson));
        }
        return Result.buildFail();
    }

    private List<IndicesDistributionVO> buildIndicesDistributionVO(JSONArray responseJson) {
        List<IndicesDistributionVO> vos = new ArrayList<>();
        for (int i = 0; i < responseJson.size(); i++) {
            JSONObject indice = (JSONObject) responseJson.get(i);
            IndicesDistributionVO vo = new IndicesDistributionVO();
            vo.setHealth(indice.getString("health"));
            vo.setIndex(indice.getString("index"));
            vo.setDocsCount(indice.getString("docs.count"));
            vo.setPri(indice.getString("pri"));
            vo.setRep(indice.getString("rep"));
            vo.setDocsDeleted(indice.getString("docs.deleted"));
            vo.setStatus(indice.getString("status"));
            vo.setUuid(indice.getString("uuid"));
            vo.setStoreSize(indice.getString("store.size"));
            vo.setPriStoreSize(indice.getString("pri.store.size"));
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public Result<List<ShardDistributionVO>> shardDistribution(String cluster) {
        DirectResponse directResponse = getDirectResponse(cluster, SHARD.getMethod(), SHARD.getUri());
        if (directResponse == null) {
            return Result.buildFail();
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            JSONArray responseJson = JSONObject.parseArray(directResponse.getResponseContent());
            return Result.buildSucc(JSONObject.parseArray(responseJson.toJSONString(), ShardDistributionVO.class));
        }
        return Result.buildFail();
    }

    @Override
    public Result<List<PendingTaskAnalysisVO>> pendingTaskAnalysis(String cluster) {
        DirectResponse directResponse = getDirectResponse(cluster, PENDING_TASK.getMethod(), PENDING_TASK.getUri());
        if (directResponse == null) {
            return Result.buildFail();
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            JSONObject responseJson = JSONObject.parseObject(directResponse.getResponseContent());
            JSONArray tasks = responseJson.getJSONArray("tasks");
            return Result.buildSucc(JSONObject.parseArray(tasks.toJSONString(), PendingTaskAnalysisVO.class));
        }
        return Result.buildFail();
    }

    @Override
    public Result<List<TaskMissionAnalysisVO>> taskMissionAnalysis(String cluster) {
        DirectResponse directResponse = getDirectResponse(cluster, TASK_MISSION_ANALYSIS.getMethod(), TASK_MISSION_ANALYSIS.getUri());
        if (directResponse == null) {
            return Result.buildFail();
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            JSONObject responseJson = JSONObject.parseObject(directResponse.getResponseContent());
            List<TaskMissionAnalysisVO> result = buildTaskMission(responseJson);
            return Result.buildSucc(result);
        }
        return Result.buildFail();
    }

    @Override
    public Result<String> hotThreadAnalysis(String cluster) {
        DirectResponse directResponse = getDirectResponse(cluster, HOT_THREAD.getMethod(), HOT_THREAD.getUri());
        if (directResponse == null) {
            return Result.buildFail();
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            return Result.buildSucc(directResponse.getResponseContent());
        }
        return Result.buildFail();
    }

    @Override
    public Result<ShardAssignmentDescriptionVO> shardAssignmentDescription(String cluster) {
        DirectResponse directResponse = getDirectResponse(cluster, SHARD_ASSIGNMENT.getMethod(), SHARD_ASSIGNMENT.getUri());
        if (directResponse == null) {
            return Result.buildFail();
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            JSONObject responseJson = JSONObject.parseObject(directResponse.getResponseContent());
            return Result.buildSucc(buildShardAssignment(responseJson));
        }
        return Result.buildFail();
    }

    private ShardAssignmentDescriptionVO buildShardAssignment(JSONObject responseJson) {
        ShardAssignmentDescriptionVO descriptionVO = new ShardAssignmentDescriptionVO();
        descriptionVO.setShard((Integer) responseJson.get("shard"));
        descriptionVO.setIndex((String) responseJson.get("responseJson"));
        descriptionVO.setPrimary((Boolean) responseJson.get("primary"));
        descriptionVO.setCurrentState((String) responseJson.get("current_state"));
        JSONArray decisionsArray = responseJson.getJSONArray("node_allocation_decisions");
        List<ShardAssignmenNodeVO> decisions = new ArrayList<>();
        for (int i = 0; i < decisionsArray.size(); i++) {
            ShardAssignmenNodeVO decisionMap = new ShardAssignmenNodeVO();
            JSONObject decisionObject = decisionsArray.getJSONObject(i);
            decisionMap.setNodeName((String) decisionObject.get("node_name"));
            JSONArray deciders = decisionObject.getJSONArray("deciders");
            JSONObject decider = (JSONObject) deciders.get(0);
            decisionMap.setNodeDecide((String) decider.get("decider"));
            decisionMap.setExplanation((String) decider.get("explanation"));
            decisions.add(decisionMap);
        }
        descriptionVO.setDecisions(decisions);
        return descriptionVO;
    }

    @Override
    public Result<Void> abnormalShardAllocationRetry(String cluster) {
        boolean result = false;
        DirectResponse directResponse = getDirectResponse(cluster, ABNORMAL_SHARD_RETRY.getMethod(), ABNORMAL_SHARD_RETRY.getUri());
        if (directResponse == null) {
            return Result.buildFail();
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            JSONObject responseJson = JSONObject.parseObject(directResponse.getResponseContent());
            result = responseJson.getBoolean("acknowledged");
        }
        if (result) {
            return Result.buildSucc();
        } else {
            return Result.buildFail();
        }
    }

    @Override
    public Result<Void> clearFielddataMemory(String cluster) {
        Integer failed = 0;
        DirectResponse directResponse = getDirectResponse(cluster, CLEAR_FIELDDATA_MEMORY.getMethod(), CLEAR_FIELDDATA_MEMORY.getUri());
        if (directResponse == null) {
            return Result.buildFail();
        }
        if (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            JSONObject responseJson = JSONObject.parseObject(directResponse.getResponseContent());
            JSONObject shards = responseJson.getJSONObject("_shards");
            failed = shards.getInteger("failed");
        }
        if (failed == 0) {
            return Result.buildSucc();
        } else {
            return Result.buildFail();
        }
    }

    @Nullable
    private DirectResponse getDirectResponse(String cluster, String method, String uri) {
        //判断物理集群是否存在
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(cluster);
        if (null == clusterPhy) {
            return null;
        }
        ESClient client = esOpClient.getESClient(cluster);
        DirectRequest directRequest = new DirectRequest(method, uri);
        DirectResponse directResponse = client.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
        return directResponse;
    }

    private NodeStateVO buildNodeState(JSONObject node) {
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
        nodeMap.put("threads_count", threads.getInteger("count"));
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

        NodeStateVO nodeStateVO = JSON.parseObject(JSON.toJSONString(nodeMap), NodeStateVO.class);
        return nodeStateVO;
    }

    private Map buildPendingTask(JSONObject taskObject) {
        PendingTaskAnalysisVO pendingTaskVO = new PendingTaskAnalysisVO();
        pendingTaskVO.setInsertOrder((Long) taskObject.get("taskObject"));
        pendingTaskVO.setSource((String) taskObject.get("source"));
        pendingTaskVO.setPriority((String) taskObject.get("priority"));
        pendingTaskVO.setTimeInQueue((String) taskObject.get("time_in_queue"));
        pendingTaskVO.setTimeInQueueMillis((String) taskObject.get("time_in_queue_millis"));
        Map task = new HashMap();
        task.put("insert_order", taskObject.get("taskObject"));
        task.put("priority", taskObject.get("priority"));
        task.put("source", taskObject.get("source"));
        task.put("time_in_queue_millis", taskObject.get("time_in_queue_millis"));
        task.put("time_in_queue", taskObject.get("time_in_queue"));
        return task;
    }

    private  List<TaskMissionAnalysisVO>  buildTaskMission(JSONObject responseJson) {
       List<TaskMissionAnalysisVO> vos = new ArrayList<>();
        JSONObject nodes = responseJson.getJSONObject("nodes");
        nodes.keySet().forEach(key -> {
            JSONObject node = (JSONObject) nodes.get(key);
            JSONObject nodeTasks = (JSONObject) node.get("tasks");
            nodeTasks.keySet().forEach(key1->{
                TaskMissionAnalysisVO taskMissionAnalysisVO = new TaskMissionAnalysisVO();
                JSONObject nodeInfo = (JSONObject) nodeTasks.get(key1);
                taskMissionAnalysisVO.setAction((String) nodeInfo.get("action"));
                taskMissionAnalysisVO.setNode((String)nodeInfo.get("node"));
                taskMissionAnalysisVO.setDescription((String) nodeInfo.get("description"));
                taskMissionAnalysisVO.setStartTimeInMillis((Long) nodeInfo.get("start_time_in_millis"));
                taskMissionAnalysisVO.setRunningTimeInNanos((Integer) nodeInfo.get("running_time_in_nanos"));
                vos.add(taskMissionAnalysisVO);
            });
        });
        return vos;
    }
}