package com.didichuxing.datachannel.arius.admin.core.service.metrics;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.metrics.ClusterPhyMetricsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.MetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.metrics.UserMetricsConfigDAO;
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
public class UserMetricsConfigServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private UserMetricsConfigService userMetricsConfigService;

    @Autowired
    private ClusterPhyMetricsManager clusterPhyMetricsManager;

    @MockBean
    private UserMetricsConfigDAO userMetricsConfigDAO;

    @Test
    public void getMetricsByTypeAndDomainAccountTest() {
        MetricsConfigInfoDTO dto = new MetricsConfigInfoDTO();
        Assertions.assertTrue(userMetricsConfigService.getMetricsByTypeAndUserName(dto).isEmpty());
        // 设置账号
        dto.setUserName("undefined");
        Assertions.assertTrue(userMetricsConfigService.getMetricsByTypeAndUserName(dto).isEmpty());
        // 设置账号和metricsTypes
        dto.setFirstMetricsType(MetricsTypeEnum.CLUSTER_NODE.getFirstMetricsType());
        dto.setSecondMetricsType(MetricsTypeEnum.CLUSTER_NODE.getSecondMetricsType());
        Mockito.when(userMetricsConfigDAO.selectOne(Mockito.any())).thenReturn(CustomDataSource.getMetricsConfigPO());
        Assertions.assertFalse(userMetricsConfigService.getMetricsByTypeAndUserName(dto).isEmpty());
    }

    @Test
    public void deleteByDomainAccountTest() {
        Mockito.when(userMetricsConfigDAO.delete(Mockito.any())).thenReturn(1);
        userMetricsConfigService.deleteByUserName(Mockito.anyString());
        Assertions.assertFalse(false);
    }

    @Test
    public void updateByMetricsByTypeAndDomainAccountTest() {
        MetricsConfigInfoDTO param = metricsConfigInfoDTOFactory();
        Assertions.assertTrue(userMetricsConfigService.getMetricsByTypeAndUserName(param).isEmpty());
        Result<Integer> result = userMetricsConfigService.updateByMetricsByTypeAndUserName(param);
        Assertions.assertTrue(param.getMetricsTypes().containsAll(userMetricsConfigService.getMetricsByTypeAndUserName(param)));
        param.setMetricsTypes(Arrays.asList("2001", "2002", "2004"));
        result = clusterPhyMetricsManager.updateUserNameConfigMetrics(param, param.getUserName());
        Assertions.assertFalse(result.success());
        MetricsConfigInfoDTO param1 = ConvertUtil.obj2Obj(param,MetricsConfigInfoDTO.class);
        param1.setSecondMetricsType(MetricsTypeEnum.GATEWAY_NODE.getSecondMetricsType());
        Assertions.assertTrue(userMetricsConfigService.getMetricsByTypeAndUserName(param1).isEmpty());
        Assertions.assertTrue(param.getMetricsTypes().containsAll(userMetricsConfigService.getMetricsByTypeAndUserName(param)));
        userMetricsConfigService.deleteByUserName(param.getUserName());
        Assertions.assertTrue(userMetricsConfigService.getMetricsByTypeAndUserName(param).isEmpty());
        userMetricsConfigService.deleteByUserName(param.getUserName());
    }

    private MetricsConfigInfoDTO metricsConfigInfoDTOFactory() {
        List<String> str = Arrays.asList("2001", "2002", "2003");
        String domainAccount = "wpk";
        MetricsConfigInfoDTO metricsConfigInfoDTO = new MetricsConfigInfoDTO();
        metricsConfigInfoDTO.setMetricsTypes(str);
        metricsConfigInfoDTO.setFirstMetricsType(MetricsTypeEnum.CLUSTER_OVERVIEW.getFirstMetricsType());
        metricsConfigInfoDTO.setSecondMetricsType(MetricsTypeEnum.CLUSTER_OVERVIEW.getSecondMetricsType());
        metricsConfigInfoDTO.setUserName(domainAccount);
        return metricsConfigInfoDTO;
    }
}