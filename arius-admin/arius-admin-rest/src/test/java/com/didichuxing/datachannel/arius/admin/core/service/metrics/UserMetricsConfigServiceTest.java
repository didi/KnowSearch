package com.didichuxing.datachannel.arius.admin.core.service.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.metrics.UserMetricsConfigPO;
import com.didichuxing.datachannel.arius.admin.core.service.metrics.impl.MetricsConfigServiceImpl;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.metrics.UserMetricsConfigDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import java.util.Arrays;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class UserMetricsConfigServiceTest {

    @Mock
    private UserMetricsConfigDAO     userMetricsConfigDAO;

    @InjectMocks
    private MetricsConfigServiceImpl userMetricsConfigService;

    @Test
    void testGetMetricsByTypeAndUserName() {
        final UserMetricsConfigPO metricsConfigPO = CustomDataSource.getMetricsConfigPO();
        metricsConfigPO.setMetricInfo(CustomDataSource.metricInfo());
        // Setup
        final MetricsConfigInfoDTO param = new MetricsConfigInfoDTO("admin", "cluster", "overview",
            Arrays.asList("cpuUsage", "cpuLoad1M"));

        // Configure UserMetricsConfigDAO.selectOne(...).
        when(userMetricsConfigDAO.selectOne(any(QueryWrapper.class))).thenReturn(metricsConfigPO);

        // Run the test
        param.setUserName(null);
        Assertions.assertFalse(CollectionUtils.isNotEmpty(userMetricsConfigService.getMetricsByTypeAndUserName(param)));
        param.setUserName("admin");
        metricsConfigPO.setMetricInfo(null);

        when(userMetricsConfigDAO.selectOne(any(QueryWrapper.class))).thenReturn(metricsConfigPO);
        Assertions.assertFalse(CollectionUtils.isNotEmpty(userMetricsConfigService.getMetricsByTypeAndUserName(param)));
        metricsConfigPO.setMetricInfo("[]");
        when(userMetricsConfigDAO.selectOne(any(QueryWrapper.class))).thenReturn(metricsConfigPO);
        Assertions.assertFalse(CollectionUtils.isNotEmpty(userMetricsConfigService.getMetricsByTypeAndUserName(param)));
        metricsConfigPO.setMetricInfo(CustomDataSource.metricInfo());
        when(userMetricsConfigDAO.selectOne(any(QueryWrapper.class))).thenReturn(metricsConfigPO);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(userMetricsConfigService.getMetricsByTypeAndUserName(param)));
    }

    @Test
    void testUpdateByMetricsByTypeAndUserName() {
        final UserMetricsConfigPO metricsConfigPO = CustomDataSource.getMetricsConfigPO();
        metricsConfigPO.setMetricInfo(CustomDataSource.metricInfo());

        // Configure UserMetricsConfigDAO.selectOne(...).

        when(userMetricsConfigDAO.insert(any())).thenReturn(1);

        final MetricsConfigInfoDTO param = new MetricsConfigInfoDTO("admin", "cluster", "overview",
            Arrays.asList("cpuUsage", "cpuLoad1M"));
        param.setUserName(null);

        // Verify the results
        assertThat(userMetricsConfigService.updateByMetricsByTypeAndUserName(param).getMessage())
            .isEqualTo(Result.buildFail("用户账号为空").getMessage());
        param.setUserName("admin");
        param.setFirstMetricsType(null);
        // Verify the results
        assertThat(userMetricsConfigService.updateByMetricsByTypeAndUserName(param).getMessage())
            .isEqualTo(Result.buildFail("指标看板未知").getMessage());
        param.setFirstMetricsType("cluster");
        param.setSecondMetricsType(null);
        assertThat(userMetricsConfigService.updateByMetricsByTypeAndUserName(param).getMessage())
            .isEqualTo(Result.buildFail("指标看板未知").getMessage());
        param.setSecondMetricsType("overview");
        when(userMetricsConfigDAO.selectOne(any())).thenReturn(null);

        assertThat(userMetricsConfigService.updateByMetricsByTypeAndUserName(param).success()).isTrue();
        when(userMetricsConfigDAO.selectOne(any())).thenReturn(metricsConfigPO);
        when(userMetricsConfigDAO.update(any(), any())).thenReturn(1);
        assertThat(userMetricsConfigService.updateByMetricsByTypeAndUserName(param).success()).isTrue();
    }

    @Test
    void testDeleteByUserName() {
        // Setup
        when(userMetricsConfigDAO.delete(any())).thenReturn(1);

        // Run the test
        userMetricsConfigService.deleteByUserName("userName");

        // Verify the results
        verify(userMetricsConfigDAO).delete(any());
    }
    
    //void setTemplateInit(){
    //      for (IndexTemplate indexTemplateDTO : indexTemplateService.listAllLogicTemplates()) {
    //        final Integer logicTemplateId = indexTemplateDTO.getId();
    //        List<Integer> openSrvList = Lists.newArrayList();
    //        final String openSrv = indexTemplateDTO.getOpenSrv();
    //        if (StringUtils.isNotBlank(openSrv)) {
    //            for (String s : StringUtils.split(openSrv, ",")) {
    //                if (StringUtils.isNumeric(s)) {
    //                    openSrvList.add(Integer.parseInt(s));
    //                }
    //            }
    //        }
    //
    //
    //        if (openSrvList.contains(TemplateServiceEnum.TEMPLATE_PRE_CREATE.getCode()) && openSrvList.contains(
    //                TemplateServiceEnum.TEMPLATE_DEL_EXPIRE.getCode()) && openSrvList.contains(
    //                TemplateServiceEnum.TEMPLATE_PIPELINE.getCode())) {
    //            System.out.println("跳过");
    //            continue;
    //        }
    //        //如果是分区模版
    //        final boolean isExpression = indexTemplateDTO.getExpression().endsWith("*");
    //
    //        if (Boolean.TRUE.equals(isExpression)) {
    //            openSrvList.add(TemplateServiceEnum.TEMPLATE_PRE_CREATE.getCode());
    //            openSrvList.add(TemplateServiceEnum.TEMPLATE_DEL_EXPIRE.getCode());
    //        }
    //        final ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(
    //                indexTemplateDTO.getResourceId());
    //        final TupleTwo</*dcdrExist*/Boolean,/*pipelineExist*/ Boolean> existDCDRAndPipelineModule = esClusterNodeService.existDCDRAndPipelineModule(
    //                clusterRegion.getPhyClusterName());
    //        if (logicTemplateId.equals(25135)){
    //            System.out.println(25135);
    //        }
    //        if (Boolean.TRUE.equals(existDCDRAndPipelineModule.v2)) {
    //            openSrvList.add(TemplateServiceEnum.TEMPLATE_PIPELINE.getCode());
    //        }
    //        final IndexTemplatePO indexTemplatePO = new IndexTemplatePO();
    //        if (CollectionUtils.isNotEmpty(openSrvList)) {
    //            //如果集群支持pipelien
    //            indexTemplatePO.setOpenSrv(
    //                    ConvertUtil.list2String(openSrvList.stream().distinct().collect(Collectors.toList()), ","));
    //            indexTemplatePO.setId(logicTemplateId);
    //            final boolean update = indexTemplateService.update(indexTemplatePO);
    //            if (update) {
    //                System.err.println("success");
    //            }
    //        }
    //
    //    }
    //    System.out.println("执行更新完成.......");
    //}
}