package com.didichuxing.datachannel.arius.admin.core.service.monitor;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor.AppMonitorRuleDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.MonitorRulePO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.monitor.MonitorRuleDAO;
import com.didichuxing.datachannel.arius.admin.remote.monitor.RemoteMonitorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static com.didichuxing.datachannel.arius.admin.util.CustomDataSource.appMonitorRuleDTOFactory;
import static org.mockito.Mockito.when;

/**
 * 后端开发会对此部分进行重写
 */
@Transactional
@Rollback
@Deprecated
public class MonitorServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private MonitorRuleDAO monitorRuleDAO;

    @MockBean
    private RemoteMonitorService remoteMonitorService;

    private final String OPERATOR = "wpk";

    @Test
    public void deleteMonitorRuleTest() {
        Long id = this.addAppMonitorRule();

        // 设定Mock规则
        when(remoteMonitorService.deleteStrategyById(monitorRuleDAO.selectById(id).getAlertRuleId()))
                .thenReturn(false);

        // 规则判定
        Assertions.assertEquals(ResultType.FAIL.getCode(),monitorService.deleteMonitorRule(id).getCode().intValue());

        //null查询
        Assertions.assertEquals(ResultType.FAIL.getCode(),monitorService.deleteMonitorRule(id).getCode().intValue());
    }

    private Long addAppMonitorRule() {
        AppMonitorRuleDTO appMonitorRuleDTO = appMonitorRuleDTOFactory();
        MonitorRulePO monitorRulePO = new MonitorRulePO();
        monitorRulePO.setAppId(appMonitorRuleDTO.getAppId());
        monitorRulePO.setName(appMonitorRuleDTO.getName());
        monitorRulePO.setOperator(OPERATOR);
        monitorRulePO.setAlertRuleId(1l);
        monitorRuleDAO.insert(monitorRulePO);
        return monitorRulePO.getId();
    }
}
