package com.didichuxing.datachannel.arius.admin.biz.dsl;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.DslMetricsVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.DslTemplateVO;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.SearchDslTemplateResponseVO;
import java.util.List;

public interface DslMetricsManager {
    /**
     * 根据appId获取dsl的指标信息
     * @param appId 应用账号
     * @param startDate 开始时刻
     * @param endDate 结束时刻
     * @return list
     */
    Result<List<DslTemplateVO>> getDSLMetricsInfoByAppId(Integer appId, Long startDate, Long endDate);

    /**
     * 获取批量dslMetrics接口
     * @param appId 应用账号
     * @param dslTemplateMd5 查询模板MD5
     * @param startDate 开始时刻
     * @param endDate 结束时刻
     * @return list
     */
    Result<List<DslMetricsVO>> getDetailMetrics(Integer appId, String dslTemplateMd5, Long startDate, Long endDate);

    /**
     * 根据查询条件获取查询模板数据
     *
     * @param appId
     * @param startDate
     * @param endDate
     * @return
     */
    Result<SearchDslTemplateResponseVO> getDslTemplateByCondition(Integer appId, String searchKeyword, String dslTag,
                                                                  String sortInfo, Long from, Long size, Long startDate,
                                                                  Long endDate);
}
