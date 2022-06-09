package com.didichuxing.datachannel.arius.admin.core.service.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.metrics.UserMetricsConfigPO;
import com.didichuxing.datachannel.arius.admin.core.service.metrics.impl.UserMetricsConfigServiceImpl;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.metrics.UserMetricsConfigDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import java.util.Arrays;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class UserMetricsConfigServiceTest {
    
    @Mock
    private UserMetricsConfigDAO userMetricsConfigDAO;
    
    @InjectMocks
    private UserMetricsConfigServiceImpl userMetricsConfigService;
    
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
        assertThat(userMetricsConfigService.updateByMetricsByTypeAndUserName(param).getMessage()).isEqualTo(
                Result.buildFail("用户账号为空").getMessage());
        param.setUserName("admin");
        param.setFirstMetricsType(null);
        // Verify the results
        assertThat(userMetricsConfigService.updateByMetricsByTypeAndUserName(param).getMessage()).isEqualTo(
                Result.buildFail("指标看板未知").getMessage());
        param.setFirstMetricsType("cluster");
        param.setSecondMetricsType(null);
        assertThat(userMetricsConfigService.updateByMetricsByTypeAndUserName(param).getMessage()).isEqualTo(
                Result.buildFail("指标看板未知").getMessage());
        param.setSecondMetricsType("overview");
        when(userMetricsConfigDAO.selectOne(any())).thenReturn(null);
        
        assertThat(userMetricsConfigService.updateByMetricsByTypeAndUserName(param).success()).isTrue();
        when(userMetricsConfigDAO.selectOne(any())).thenReturn(metricsConfigPO);
         when(
                        userMetricsConfigDAO.update(any(), any()))
                .thenReturn(1);
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
}