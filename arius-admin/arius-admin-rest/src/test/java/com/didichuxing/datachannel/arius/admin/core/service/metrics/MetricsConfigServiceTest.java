package com.didichuxing.datachannel.arius.admin.core.service.metrics;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.metrics.ClusterPhyMetricsManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.metrics.MetricsConfigPO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.MetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.metrics.MetricsConfigDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Transactional
@Rollback
public class MetricsConfigServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private MetricsConfigService metricsConfigService;

    @Autowired
    private ClusterPhyMetricsManager clusterPhyMetricsManager;

    @MockBean
    private MetricsConfigDAO metricsConfigDAO;

    @Test
    public void getMetricsByTypeAndDomainAccountTest() {
        MetricsConfigInfoDTO dto = new MetricsConfigInfoDTO();
        Assertions.assertTrue(metricsConfigService.getMetricsByTypeAndDomainAccount(dto).isEmpty());
        // 设置账号
        dto.setDomainAccount("undefined");
        Assertions.assertTrue(metricsConfigService.getMetricsByTypeAndDomainAccount(dto).isEmpty());
        // 设置账号和metricsTypes
        dto.setFirstMetricsType(MetricsTypeEnum.CLUSTER_NODE.getFirstMetricsType());
        dto.setSecondMetricsType(MetricsTypeEnum.CLUSTER_NODE.getSecondMetricsType());
        Mockito.when(metricsConfigDAO.selectOne(Mockito.any())).thenReturn(CustomDataSource.getMetricsConfigPO());
        Assertions.assertFalse(metricsConfigService.getMetricsByTypeAndDomainAccount(dto).isEmpty());
    }

    @Test
    public void deleteByDomainAccountTest() {
        Mockito.when(metricsConfigDAO.delete(Mockito.any())).thenReturn(1);
        metricsConfigService.deleteByDomainAccount(Mockito.anyString());
        Assertions.assertFalse(false);
    }

    @Test
    public void updateByMetricsByTypeAndDomainAccountTest() {
        MetricsConfigInfoDTO param = metricsConfigInfoDTOFactory();
        Assertions.assertTrue(metricsConfigService.getMetricsByTypeAndDomainAccount(param).isEmpty());
        Result<Integer> result = metricsConfigService.updateByMetricsByTypeAndDomainAccount(param);
        Assertions.assertTrue(param.getMetricsTypes().containsAll(metricsConfigService.getMetricsByTypeAndDomainAccount(param)));
        param.setMetricsTypes(Arrays.asList("2001", "2002", "2004"));
        result = clusterPhyMetricsManager.updateDomainAccountConfigMetrics(param, param.getDomainAccount());
        Assertions.assertFalse(result.success());
        MetricsConfigInfoDTO param1 = ConvertUtil.obj2Obj(param,MetricsConfigInfoDTO.class);
        param1.setSecondMetricsType(MetricsTypeEnum.GATEWAY_NODE.getSecondMetricsType());
        Assertions.assertTrue(metricsConfigService.getMetricsByTypeAndDomainAccount(param1).isEmpty());
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
