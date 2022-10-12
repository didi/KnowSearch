package com.didichuxing.datachannel.arius.admin.biz.metrics;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricDictionaryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.dictionary.MetricsDictionaryVO;

import java.util.List;

/**
 * @description: 指标字典manager
 * @author gyp
 * @date 2022/9/28 10:01
 * @version 1.0
 */
public interface MetricsDictionaryManager {
    /**
     * 获取gateway不同组的指标项
     */
    Result<List<MetricsDictionaryVO>> listMetricsDictionaries(String group);

    Result<List<MetricsDictionaryVO>> listByCondition(MetricDictionaryDTO param);
}
