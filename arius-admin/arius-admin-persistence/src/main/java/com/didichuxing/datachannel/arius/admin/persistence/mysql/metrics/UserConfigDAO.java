package com.didichuxing.datachannel.arius.admin.persistence.mysql.metrics;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.didichuxing.datachannel.arius.admin.common.bean.po.metrics.UserConfigPO;
import org.springframework.stereotype.Repository;

/**
 * 用户指标配置dao
 *
 * @author shizeying
 * @date 2022/05/24
 */
@Repository
public interface UserConfigDAO extends BaseMapper<UserConfigPO> {

}