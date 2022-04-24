package com.didichuxing.datachannel.arius.admin.v3.op.metrics;

import com.didichuxing.datachannel.arius.admin.BaseContextTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.*;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.other.cluster.ESClusterOverviewMetricsVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.other.cluster.ESClusterTaskDetailVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.method.v3.op.metrics.ClusterPhyMetricsControllerMethod;
import com.didichuxing.datachannel.arius.admin.source.CustomDataSource;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.List;

public class ClusterPhyMetricsTest extends BaseContextTest {

    @Test
    public void getClusterPhyMetricsTypesTest() throws IOException {
        Result<List<String>> result = ClusterPhyMetricsControllerMethod.getClusterPhyMetricsTypes("clusterOverview");
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getClusterPhyMetricsDTOTypesTest() throws IOException {
        MetricsConfigInfoDTO param = new MetricsConfigInfoDTO();
        param.setFirstMetricsType("cluster");
        param.setSecondMetricsType("node");
        Result<List<String>> result = ClusterPhyMetricsControllerMethod.getClusterPhyMetricsTypes(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void updateClusterPhyMetricsTypesTest() throws IOException {
        MetricsConfigInfoDTO param = new MetricsConfigInfoDTO();
        param.setFirstMetricsType("cluster");
        param.setSecondMetricsType("node");
        param.setMetricsTypes(Lists.newArrayList("os-cpu-percent", "fs-total-disk_free_percent"));
        Result<Integer> result = ClusterPhyMetricsControllerMethod.updateClusterPhyMetricsTypes(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getClusterPhyMetricsTest() throws IOException {
        MetricsClusterPhyDTO param = new MetricsClusterPhyDTO();
        CustomDataSource.setMetricsClusterPhyDTO(param);
        param.setMetricsTypes(Lists.newArrayList("basic"));
        Result<ESClusterOverviewMetricsVO> result = ClusterPhyMetricsControllerMethod.getClusterPhyMetrics(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getClusterPhyNodesMetricsTest() throws IOException {
        MetricsClusterPhyNodeDTO param = new MetricsClusterPhyNodeDTO();
        CustomDataSource.setMetricsClusterPhyDTO(param);
        param.setMetricsTypes(Lists.newArrayList("os-cpu-percent"));
        param.setTopNu(5);
        Result<List<VariousLineChartMetricsVO>> result = ClusterPhyMetricsControllerMethod.getClusterPhyNodesMetrics(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getMultiClusterPhyNodesMetricsTest() throws IOException {
        MultiMetricsClusterPhyNodeDTO param = new MultiMetricsClusterPhyNodeDTO();
        CustomDataSource.setMetricsClusterPhyDTO(param);
        param.setMetricsTypes(Lists.newArrayList("os-cpu-percent"));
        param.setNodeNames(Lists.newArrayList("logi02-master02", "logi02-datanode02"));
        param.setTopNu(0);
        Result<List<VariousLineChartMetricsVO>> result = ClusterPhyMetricsControllerMethod.getMultiClusterPhyNodesMetrics(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getClusterPhyIndicesMetricsTest() throws IOException {
        MetricsClusterPhyIndicesDTO param = new MetricsClusterPhyIndicesDTO();
        CustomDataSource.setMetricsClusterPhyDTO(param);
        param.setMetricsTypes(Lists.newArrayList("shardNu"));
        param.setTopNu(5);
        Result<List<VariousLineChartMetricsVO>> result = ClusterPhyMetricsControllerMethod.getClusterPhyIndicesMetrics(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getClusterPhyTemplateMetricsTest() throws IOException {
        MetricsClusterPhyTemplateDTO param = new MetricsClusterPhyTemplateDTO();
        CustomDataSource.setMetricsClusterPhyDTO(param);
        param.setMetricsTypes(Lists.newArrayList("shardNu"));
        param.setTopNu(5);
        Result<List<VariousLineChartMetricsVO>> result = ClusterPhyMetricsControllerMethod.getClusterPhyTemplateMetrics(param);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getClusterPhyIndexNameTest() throws IOException {
        Result<List<String>> result = ClusterPhyMetricsControllerMethod.getClusterPhyIndexName("logi-elasticsearch-7.6.0");
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getClusterPhyTaskDetailTest() throws IOException {
        Long nowTime = System.currentTimeMillis();
        Result<List<ESClusterTaskDetailVO>> result = ClusterPhyMetricsControllerMethod.getClusterPhyTaskDetail("logi-elasticsearch-7.6.0",
                "logi02-master02", String.valueOf(nowTime - 5 * 60 * 1000), String.valueOf(nowTime));
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getClusterPhyNodesTaskMetricsTest() throws IOException {
        MultiMetricsClusterPhyNodeTaskDTO param = new MultiMetricsClusterPhyNodeTaskDTO();
        CustomDataSource.setMetricsClusterPhyDTO(param);
        param.setNodeNames(Lists.newArrayList("logi02-master02", "logi02-datanode02"));
        Result<List<VariousLineChartMetricsVO>> result = ClusterPhyMetricsControllerMethod.getClusterPhyNodesTaskMetrics(param);
        Assertions.assertTrue(result.success());
    }

}
