package com.didichuxing.datachannel.arius.admin.biz.dsl;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.DslMetricsVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.DslTemplateVO;

import java.util.List;

public interface DslMetricsManager {
    /**
     * 根据appId获取dsl的指标信息
     * @param appId 应用账号
     * @param startDate 开始时刻
     * @param endDate 结束时刻
     * @return list
     */
    Result<List<DslTemplateVO>> getDSLMetricsInfoByAppId(Long appId, Long startDate, Long endDate);

    /**
     * 获取批量dslMetrics接口
     * @param appId 应用账号
     * @param dslTemplateMd5 查询模板MD5
     * @param startDate 开始时刻
     * @param endDate 结束时刻
     * @return list
     */
    Result<List<DslMetricsVO>> getDetailMetrics(int appId, String dslTemplateMd5, Long startDate, Long endDate);
}
