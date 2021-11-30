package com.didichuxing.datachannel.arius.admin.metadata.service;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.SearchDslTemplateResponse;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.DslMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.DslTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslMetricsESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslTemplateESDAO;

@Service
public class DslMetricsService {
    @Autowired
    private DslTemplateESDAO dslTemplateESDAO;

    @Autowired
    private DslMetricsESDAO dslMetricsESDAO;

    public Result<List<DslTemplate>> getDSLMetricsInfoByAppId(Integer appId, Long startDate, Long endDate) {
        List<DslTemplatePO> dslTemplatePos = dslTemplateESDAO.getDslMertricsByAppid(appId, startDate, endDate);

        if (CollectionUtils.isEmpty(dslTemplatePos)) {
            return Result.buildSucc(new ArrayList<>());
        }

        dslTemplatePos = dslTemplatePos.stream().filter(d -> !StringUtils.isBlank(d.getIndiceSample())).collect(Collectors.toList());

        for (DslTemplatePO dslTemplatePo : dslTemplatePos) {
            if (dslTemplatePo.getQueryLimit() == null) {
                dslTemplatePo.setQueryLimit(50.0);
            }
        }

        return Result.buildSucc(ConvertUtil.list2List(dslTemplatePos, DslTemplate.class));
    }

    public Result<List<DslMetrics>> getDetailMetrics(int appId, String dslTemplateMd5, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
                dslMetricsESDAO.getDslDetailMetricByAppidAndDslTemplateMd5(appId, dslTemplateMd5, startDate, endDate),
                DslMetrics.class));
    }

    public Result<SearchDslTemplateResponse> getDslTemplateByCondition(Integer appId, String searchKeyword, String dslTag,
                                                                       String sortInfo, Long from, Long size,
                                                                       Long startDate, Long endDate) {
        SearchDslTemplateResponse response = new SearchDslTemplateResponse();

        Tuple<Long, List<DslTemplatePO>> dslTemplatePos = dslTemplateESDAO.getDslTemplateByCondition(appId,
                searchKeyword, dslTag, sortInfo, from, size, startDate, endDate);

        if (null == dslTemplatePos) {
            response.setTotalHits(0L);
            response.setRecords(Lists.newArrayList());
            return Result.buildSucc(response);
        }

        response.setTotalHits(dslTemplatePos.v1());
        response.setRecords(dslTemplatePos.v2());
        return Result.buildSucc(response);
    }
}