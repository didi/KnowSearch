package com.didichuxing.datachannel.arius.admin.biz.dsl.impl;

import com.didichuxing.datachannel.arius.admin.biz.dsl.DslMetricsManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.DslMetricsVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.DslTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.metadata.service.DslMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DslMetricsManagerImpl implements DslMetricsManager {

    @Autowired
    private DslMetricsService dslMetricsService;

    @Override
    public Result<List<DslTemplateVO>> getDSLMetricsInfoByAppId(Long appId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
                dslMetricsService.getDSLMetricsInfoByAppId(appId, startDate, endDate).getData(),
                DslTemplateVO.class));
    }

    @Override
    public Result<List<DslMetricsVO>> getDetailMetrics(int appId, String dslTemplateMd5, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
                dslMetricsService.getDetailMetrics(appId, dslTemplateMd5, startDate, endDate).getData(),
                DslMetricsVO.class));
    }
}
