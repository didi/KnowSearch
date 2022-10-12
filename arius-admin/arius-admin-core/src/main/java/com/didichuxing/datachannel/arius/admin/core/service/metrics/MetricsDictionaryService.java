package com.didichuxing.datachannel.arius.admin.core.service.metrics;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricDictionaryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.metrics.MetricsDictionaryPO;

import java.util.List;

/**
 * @author gyp
 */
public interface MetricsDictionaryService {
    /**
     * 获取物理集群指标看板下的配置
     * @return 二级目录下的指标名称列表
     */
    List<MetricsDictionaryPO> list(String model);

    List<MetricsDictionaryPO> listByCondition(MetricDictionaryDTO param);
}