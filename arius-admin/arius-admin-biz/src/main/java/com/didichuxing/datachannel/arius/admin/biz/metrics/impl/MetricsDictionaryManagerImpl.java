package com.didichuxing.datachannel.arius.admin.biz.metrics.impl;

import com.didichuxing.datachannel.arius.admin.biz.metrics.MetricsDictionaryManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricDictionaryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.dictionary.MetricsDictionaryVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.metrics.MetricsDictionaryService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 详细介绍类情况.
 *
 * @ClassName MetricsDictionaryManagerImpl
 * @Author gyp
 * @Date 2022/9/28
 * @Version 1.0
 */
@Component
public class MetricsDictionaryManagerImpl implements MetricsDictionaryManager {
    @Autowired
    private MetricsDictionaryService metricsDictionaryService;

    
    @Override
    public Result<List<MetricsDictionaryVO>> listByCondition(MetricDictionaryDTO param) {
        return Result.buildSucc(ConvertUtil.list2List(metricsDictionaryService.listByCondition(param), MetricsDictionaryVO.class));
    }
}