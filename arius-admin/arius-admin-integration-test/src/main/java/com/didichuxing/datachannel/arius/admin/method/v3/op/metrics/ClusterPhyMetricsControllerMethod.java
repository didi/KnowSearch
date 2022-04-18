package com.didichuxing.datachannel.arius.admin.method.v3.op.metrics;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.ESClusterOverviewMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.ESClusterTaskDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import org.omg.PortableInterceptor.INACTIVE;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author chengxiang
 */
public class ClusterPhyMetricsControllerMethod {
    public static final String ClusterPhyMetrics = V3_OP + "/phy/cluster/metrics";

    public static Result<List<String>> getClusterPhyMetricsTypes(String type) throws IOException {
        String path = String.format("%s/%s", ClusterPhyMetrics, type);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<String>>>(){});
    }

    public static  Result<List<String>> getClusterPhyMetricsTypes(MetricsConfigInfoDTO param) throws IOException{
        String path = String.format("%s/configMetrics", ClusterPhyMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<String>>>(){});
    }

    public static Result<Integer> updateClusterPhyMetricsTypes(MetricsConfigInfoDTO param) throws IOException {
        String path = String.format("%s/updateConfigMetrics", ClusterPhyMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<Integer>>(){});
    }

    public static Result<ESClusterOverviewMetricsVO> getClusterPhyMetrics(MetricsClusterPhyDTO param) throws IOException {
        String path = String.format("%s/overview", ClusterPhyMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<ESClusterOverviewMetricsVO>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getClusterPhyNodesMetrics(MetricsClusterPhyNodeDTO param) throws IOException {
        String path = String.format("%s/node", ClusterPhyMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getMultiClusterPhyNodesMetrics(MultiMetricsClusterPhyNodeDTO param) throws IOException {
        String path = String.format("%s/nodes", ClusterPhyMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getClusterPhyIndicesMetrics(MetricsClusterPhyIndicesDTO param) throws IOException {
        String path = String.format("%s/index", ClusterPhyMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getClusterPhyTemplateMetrics(MetricsClusterPhyTemplateDTO param) throws IOException {
        String path = String.format("%s/template", ClusterPhyMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }

    public static Result<List<String>> getClusterPhyIndexName(String clusterPhyName) throws IOException {
        String path = String.format("%s/%s/indices", ClusterPhyMetrics, clusterPhyName);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<String>>>(){});
    }

    public static Result<List<ESClusterTaskDetailVO>> getClusterPhyTaskDetail(String clusterPhyName, String node, String startTime, String endTime) throws IOException {
        String path = String.format("%s/%s/%s/task", ClusterPhyMetrics, clusterPhyName, node);
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        return JSON.parseObject(AriusClient.get(path, params), new TypeReference<Result<List<ESClusterTaskDetailVO>>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getClusterPhyNodesTaskMetrics(MultiMetricsClusterPhyNodeTaskDTO param) throws IOException {
        String path = String.format("%s/node/task", ClusterPhyMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }

}
