package com.didichuxing.datachannel.arius.admin.method.v3.op.metrics;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.gateway.GatewayOverviewMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author chengxiang
 */
public class GatewayMetricsControllerMethod {
    public static final String GatewayMetrics = V3_OP + "/gateway/metrics";

    public static Result<List<String>> getGatewayMetrics(String group) throws IOException {
        String path = String.format("%s/config/%s", GatewayMetrics, group);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<String>>>(){});
    }

    public static Result<List<String>> getDslMd5List(String startTime, String endTime) throws IOException {
        String path = String.format("%s/dslMd5/list", GatewayMetrics);
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        return JSON.parseObject(AriusClient.get(path, params), new TypeReference<Result<List<String>>>(){});
    }

    public static Result<List<GatewayOverviewMetricsVO>> getGatewayOverviewMetrics(GatewayOverviewDTO param) throws IOException {
        String path = String.format("%s/overview", GatewayMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<GatewayOverviewMetricsVO>>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getGatewayNodeMetrics(GatewayNodeDTO param) throws IOException {
        String path = String.format("%s/node", GatewayMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getMultiGatewayNodesMetrics(MultiGatewayNodesDTO param) throws IOException {
        String path = String.format("%s/nodes", GatewayMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getClientNodeMetrics(ClientNodeDTO param) throws IOException {
        String path = String.format("%s/node/client", GatewayMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }

    public static Result<List<String>> getClientNodeIpList(String gatewayNode, Long startTime, Long endTime) throws IOException {
        String path = String.format("%s/node/client/list", GatewayMetrics);
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("gatewayNode", gatewayNode);
        return JSON.parseObject(AriusClient.get(path, params), new TypeReference<Result<List<String>>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getGatewayIndexMetrics(GatewayIndexDTO param) throws IOException {
        String path = String.format("%s/index", GatewayMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getGatewayAppMetrics(GatewayAppDTO param) throws IOException {
        String path = String.format("%s/app", GatewayMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getGatewayDslMetrics(GatewayDslDTO param) throws IOException {
        String path = String.format("%s/dsl", GatewayMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }
}
