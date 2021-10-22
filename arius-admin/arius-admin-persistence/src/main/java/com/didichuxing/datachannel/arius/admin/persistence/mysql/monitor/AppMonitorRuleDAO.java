package com.didichuxing.datachannel.arius.admin.persistence.mysql.monitor;

import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.AppMonitorRulePO;

import java.util.List;

public interface AppMonitorRuleDAO {

    int insert(AppMonitorRulePO param);

    int deleteById(Long id);

    int updateById(Long id, Integer appId, String operator);

    AppMonitorRulePO selectById(Long id);

    AppMonitorRulePO selectByStrategyId(Long strategyId);

    List<AppMonitorRulePO> listAll();
}
