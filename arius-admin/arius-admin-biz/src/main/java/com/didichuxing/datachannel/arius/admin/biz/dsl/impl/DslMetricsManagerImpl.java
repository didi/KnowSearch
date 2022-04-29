package com.didichuxing.datachannel.arius.admin.biz.dsl.impl;

import com.didichuxing.datachannel.arius.admin.biz.dsl.DslMetricsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DslMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DslTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.SearchDslTemplateResponseVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.SearchDslTemplateResponse;
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
    public Result<List<DslTemplateVO>> getDSLMetricsInfoByAppId(Integer appId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
            dslMetricsService.getDSLMetricsInfoByAppId(appId, startDate, endDate).getData(), DslTemplateVO.class));
    }

    @Override
    public Result<List<DslMetricsVO>> getDetailMetrics(Integer appId, String dslTemplateMd5, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
            dslMetricsService.getDetailMetrics(appId, dslTemplateMd5, startDate, endDate).getData(),
            DslMetricsVO.class));
    }

    @Override
    public Result<SearchDslTemplateResponseVO> getDslTemplateByCondition(Integer appId, String searchKeyword,
                                                                         String dslTag, String sortInfo, Long from,
                                                                         Long size, Long startDate, Long endDate) {
        SearchDslTemplateResponse data = dslMetricsService
                .getDslTemplateByCondition(appId, searchKeyword, dslTag, sortInfo, from, size, startDate, endDate)
                .getData();
        return Result.buildSucc(ConvertUtil.obj2Obj(data, SearchDslTemplateResponseVO.class));
    }
}
