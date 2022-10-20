package com.didichuxing.datachannel.arius.admin.persistence.mysql.metrics;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricDictionaryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.metrics.MetricsDictionaryPO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户指标配置dao
 *
 * @author gyp
 * @date 2022/05/24
 */
@Repository
public interface MetricsDictionaryDAO extends BaseMapper<MetricsDictionaryPO> {
    /**
     * 查询所有的配置
     * @return 集群列表
     */
    List<MetricsDictionaryPO> listAll();

    /**
     * 通过model查询所有的配置
     * @return 集群列表
     */
    List<MetricsDictionaryPO> listByModel(String model);

    List<MetricsDictionaryPO> listByCondition(MetricDictionaryDTO param);
}