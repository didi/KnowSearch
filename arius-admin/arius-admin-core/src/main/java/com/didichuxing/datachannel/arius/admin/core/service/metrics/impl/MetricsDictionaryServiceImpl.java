package com.didichuxing.datachannel.arius.admin.core.service.metrics.impl;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricDictionaryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.metrics.MetricsDictionaryPO;
import com.didichuxing.datachannel.arius.admin.core.service.metrics.MetricsDictionaryService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.metrics.MetricsDictionaryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 详细介绍类情况.
 *
 * @ClassName MetricsDictionaryServiceImpl
 * @Author gyp
 * @Date 2022/9/28
 * @Version 1.0
 */
@Service
public class MetricsDictionaryServiceImpl implements MetricsDictionaryService {
    @Autowired
    private MetricsDictionaryDAO metricsDictionaryDAO;
    @Override
    public List<MetricsDictionaryPO> list(String model) {
        return metricsDictionaryDAO.listByModel(model);
    }

    @Override
    public List<MetricsDictionaryPO> listByCondition(MetricDictionaryDTO param) {
        return metricsDictionaryDAO.listByCondition(param);
    }
}