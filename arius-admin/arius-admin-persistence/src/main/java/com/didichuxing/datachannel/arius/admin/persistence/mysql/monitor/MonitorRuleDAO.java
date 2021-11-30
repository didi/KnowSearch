package com.didichuxing.datachannel.arius.admin.persistence.mysql.monitor;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.MonitorRulePO;
import org.springframework.stereotype.Repository;

@Repository
public interface MonitorRuleDAO extends BaseMapper<MonitorRulePO> {
}
