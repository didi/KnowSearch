package com.didichuxing.datachannel.arius.admin.persistence.mysql.metrics;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.didichuxing.datachannel.arius.admin.common.bean.po.metrics.MetricsConfigPO;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricsConfigDAO extends BaseMapper<MetricsConfigPO> {
}
