package com.didichuxing.datachannel.arius.admin.method.v3.op.metrics;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsDashboardListDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsDashboardTopNDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.list.MetricListVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.other.dashboard.ClusterPhyHealthMetricsVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.top.VariousLineChartMetricsVO;

import java.io.IOException;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author chengxiang
 */
public class DashboardMetricsControllerMethod {
    public static final String DashboardMetrics = V3_OP + "/dashboard/metrics";

    public static Result<ClusterPhyHealthMetricsVO> getClusterHealthInfo() throws IOException {
        String path = String.format("%s/health", DashboardMetrics);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<ClusterPhyHealthMetricsVO>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getTopClusterMetricsInfo(MetricsDashboardTopNDTO param) throws IOException {
        String path = String.format("%s/top/cluster", DashboardMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getTopNodeMetricsInfo(MetricsDashboardTopNDTO param) throws IOException {
        String path = String.format("%s/top/node", DashboardMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getTopTemplateMetricsInfo(MetricsDashboardTopNDTO param) throws IOException {
        String path = String.format("%s/top/template", DashboardMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getTopIndexMetricsInfo(MetricsDashboardTopNDTO param) throws IOException {
        String path = String.format("%s/top/index", DashboardMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }

    public static Result<List<VariousLineChartMetricsVO>> getTopClusterThreadPoolQueueMetricsInfo(MetricsDashboardTopNDTO param) throws IOException {
        String path = String.format("%s/top/clusterThreadPoolQueue", DashboardMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<VariousLineChartMetricsVO>>>(){});
    }

    public static Result<List<MetricListVO>> getListClusterMetricsInfo(MetricsDashboardListDTO param) throws IOException {
        String path = String.format("%s/list/cluster", DashboardMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<MetricListVO>>>(){});
    }

    public static Result<List<MetricListVO>> getListNodeMetricsInfo(MetricsDashboardListDTO param) throws IOException {
        String path = String.format("%s/list/node", DashboardMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<MetricListVO>>>(){});
    }

    public static Result<List<MetricListVO>> getListTemplateMetricsInfo(MetricsDashboardListDTO param) throws IOException {
        String path = String.format("%s/list/template", DashboardMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<MetricListVO>>>(){});
    }

    public static Result<List<MetricListVO>> getListIndexMetricsInfo(MetricsDashboardListDTO param) throws IOException {
        String path = String.format("%s/list/index", DashboardMetrics);
        return JSON.parseObject(AriusClient.post(path, param), new TypeReference<Result<List<MetricListVO>>>(){});
    }
}
