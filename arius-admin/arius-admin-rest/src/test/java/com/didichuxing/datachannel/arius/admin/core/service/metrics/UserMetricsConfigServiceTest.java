package com.didichuxing.datachannel.arius.admin.core.service.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.UserConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.metrics.UserConfigPO;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.metrics.impl.UserConfigServiceImpl;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.metrics.UserConfigDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import java.util.Arrays;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { SpringTool.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserConfigServiceTest {

    @Mock
    private UserConfigDAO     userConfigDAO;

    @InjectMocks
    private UserConfigServiceImpl userConfigService;

    @Test
    void testGetMetricsByTypeAndUserName() {
        final UserConfigPO metricsConfigPO = CustomDataSource.getMetricsConfigPO();
        metricsConfigPO.setConfigInfo(CustomDataSource.metricInfo());
        // Setup
        final UserConfigInfoDTO param = new UserConfigInfoDTO("admin", "cluster", "overview",
            Arrays.asList("cpuUsage", "cpuLoad1M"),1,1);

        // Configure UserConfigDAO.selectOne(...).
        when(userConfigDAO.selectOne(any(QueryWrapper.class))).thenReturn(metricsConfigPO);

        // Run the test
        param.setUserName(null);
        Assertions.assertFalse(CollectionUtils.isNotEmpty(userConfigService.getUserConfigByConfigTypeAndUserNameAndProjectId(param)));
        param.setUserName("admin");
        metricsConfigPO.setConfigInfo(null);

        when(userConfigDAO.selectOne(any(QueryWrapper.class))).thenReturn(metricsConfigPO);
        Assertions.assertFalse(CollectionUtils.isNotEmpty(userConfigService.getUserConfigByConfigTypeAndUserNameAndProjectId(param)));
        metricsConfigPO.setConfigInfo("[]");
        when(userConfigDAO.selectOne(any(QueryWrapper.class))).thenReturn(metricsConfigPO);
        Assertions.assertFalse(CollectionUtils.isNotEmpty(userConfigService.getUserConfigByConfigTypeAndUserNameAndProjectId(param)));
        metricsConfigPO.setConfigInfo(CustomDataSource.metricInfo());
        when(userConfigDAO.selectOne(any(QueryWrapper.class))).thenReturn(metricsConfigPO);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(userConfigService.getUserConfigByConfigTypeAndUserNameAndProjectId(param)));
    }

    @Test
    void testupdateUserConfigByConfigTypeAndUserNameAndProjectId() {
        final UserConfigPO metricsConfigPO = CustomDataSource.getMetricsConfigPO();
        metricsConfigPO.setConfigInfo(CustomDataSource.metricInfo());

        // Configure UserConfigDAO.selectOne(...).

        when(userConfigDAO.insert(any())).thenReturn(1);

        final UserConfigInfoDTO param = new UserConfigInfoDTO("admin", "cluster", "overview",
            Arrays.asList("cpuUsage", "cpuLoad1M"),1,1);
        param.setUserName(null);

        // Verify the results
        assertThat(userConfigService.updateUserConfigByConfigTypeAndUserNameAndProjectId(param).getMessage())
            .isEqualTo(Result.buildFail("用户账号为空").getMessage());
        param.setUserName("admin");
        param.setFirstUserConfigType(null);
        // Verify the results
        assertThat(userConfigService.updateUserConfigByConfigTypeAndUserNameAndProjectId(param).getMessage())
            .isEqualTo(Result.buildFail("配置类型未知").getMessage());
        param.setFirstUserConfigType("cluster");
        param.setSecondUserConfigType(null);
        assertThat(userConfigService.updateUserConfigByConfigTypeAndUserNameAndProjectId(param).getMessage())
            .isEqualTo(Result.buildFail("配置类型未知").getMessage());
        param.setSecondUserConfigType("overview");
        when(userConfigDAO.selectOne(any())).thenReturn(null);

        assertThat(userConfigService.updateUserConfigByConfigTypeAndUserNameAndProjectId(param).success()).isTrue();
        when(userConfigDAO.selectOne(any())).thenReturn(metricsConfigPO);
        when(userConfigDAO.update(any(), any())).thenReturn(1);
        assertThat(userConfigService.updateUserConfigByConfigTypeAndUserNameAndProjectId(param).success()).isTrue();
    }

    @Test
    void testDeleteByUserName() {
        // Setup
        when(userConfigDAO.delete(any())).thenReturn(1);

        // Run the test
        userConfigService.deleteByUserName("userName",1);

        // Verify the results
        verify(userConfigDAO).delete(any());
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