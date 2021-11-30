package com.didichuxing.datachannel.arius.admin.core.service.metrics;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.biz.metrics.ClusterPhyMetricsManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.MetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Transactional
@Rollback
public class MetricsConfigServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private MetricsConfigService metricsConfigService;

    @Autowired
    private ClusterPhyMetricsManager clusterPhyMetricsManager;

    @Test
    void getMetricsByTypeAndDomainAccountTest() {
        MetricsConfigInfoDTO dto = new MetricsConfigInfoDTO();
        List<String> metricsByTypeAndDomainAccount = metricsConfigService.getMetricsByTypeAndDomainAccount(dto);
        Assertions.assertTrue(metricsByTypeAndDomainAccount.isEmpty());
        // 设置账号
        dto.setDomainAccount("undefined");
        metricsByTypeAndDomainAccount = metricsConfigService.getMetricsByTypeAndDomainAccount(dto);
        Assertions.assertTrue(metricsByTypeAndDomainAccount.isEmpty());
        // 设置账号和metricsTypes
        dto.setFirstMetricsType(MetricsTypeEnum.GATEWAY_NODE.getFirstMetricsType());
        dto.setSecondMetricsType(MetricsTypeEnum.GATEWAY_NODE.getSecondMetricsType());
        metricsByTypeAndDomainAccount = metricsConfigService.getMetricsByTypeAndDomainAccount(dto);
        Assertions.assertFalse(metricsByTypeAndDomainAccount.isEmpty());
    }

    @Autowired
    void deleteByDomainAccount() {
        // 存在的账户
        String account = "undefined";
        metricsConfigService.deleteByDomainAccount(account);
        // 不存在的账户
        account = "testtest";
        metricsConfigService.deleteByDomainAccount(account);
    }

    @Test
    void updateByMetricsByTypeAndDomainAccountTest() {
        MetricsConfigInfoDTO param = metricsConfigInfoDTOFactory();
        Assertions.assertTrue(metricsConfigService.getMetricsByTypeAndDomainAccount(param).isEmpty());
        Result<Integer> result = metricsConfigService.updateByMetricsByTypeAndDomainAccount(param);
        Assertions.assertTrue(param.getMetricsTypes().containsAll(metricsConfigService.getMetricsByTypeAndDomainAccount(param)));
        param.setMetricsTypes(Arrays.asList("2001", "2002", "2004"));
        result = clusterPhyMetricsManager.updateDomainAccountConfigMetrics(param, param.getDomainAccount());
        MetricsConfigInfoDTO param1 = ConvertUtil.obj2Obj(param,MetricsConfigInfoDTO.class);
        param1.setSecondMetricsType(MetricsTypeEnum.GATEWAY_NODE.getSecondMetricsType());
        Assertions.assertTrue(metricsConfigService.getMetricsByTypeAndDomainAccount(param1).isEmpty());
        Assertions.assertTrue(result.success());
        Assertions.assertTrue(param.getMetricsTypes().containsAll(metricsConfigService.getMetricsByTypeAndDomainAccount(param)));
        metricsConfigService.deleteByDomainAccount(param.getDomainAccount());
        Assertions.assertTrue(metricsConfigService.getMetricsByTypeAndDomainAccount(param).isEmpty());
        metricsConfigService.deleteByDomainAccount(param.getDomainAccount());
    }

    private MetricsConfigInfoDTO metricsConfigInfoDTOFactory() {
        List<String> str = Arrays.asList("2001", "2002", "2003");
        String domainAccount = "wpk";
        MetricsConfigInfoDTO metricsConfigInfoDTO = new MetricsConfigInfoDTO();
        metricsConfigInfoDTO.setMetricsTypes(str);
        metricsConfigInfoDTO.setFirstMetricsType(MetricsTypeEnum.CLUSTER_OVERVIEW.getFirstMetricsType());
        metricsConfigInfoDTO.setSecondMetricsType(MetricsTypeEnum.CLUSTER_OVERVIEW.getSecondMetricsType());
        metricsConfigInfoDTO.setDomainAccount(domainAccount);
        return metricsConfigInfoDTO;
    }
}
