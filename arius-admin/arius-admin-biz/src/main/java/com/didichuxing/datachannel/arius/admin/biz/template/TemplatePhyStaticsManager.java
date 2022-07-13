package com.didichuxing.datachannel.arius.admin.biz.template;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexStats;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ProjectIdTemplateAccessCountVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateStatsInfoVO;

import java.util.List;
import java.util.Map;

/**
 * @author d06679
 * @date 2019-06-24
 */
public interface TemplatePhyStaticsManager {
    /**
     * 根据模板id获取最近days天的projectid访问统计信息
     * @param logicTemplateId 逻辑索引模板ID
     * @param days 最近多少天
     * @return map
     */
    Result<Map<Integer, Long>> getAccessStatsInfoByTemplateIdAndDays(int logicTemplateId, int days);

    /**
     * 根据模板id获取模板的基本统计信息
     * @param logicTemplateId 模板id
     * @return result
     */
    Result<TemplateStatsInfoVO> getTemplateBaseStatisticalInfoByLogicTemplateId(Long logicTemplateId);

    /**
     * 根据模板id获取模板的基本统计信息
     * @param logicTemplateId 模板id
     * @param startDate 开始时刻
     * @param endDate 结束时刻
     * @return list
     */
    Result<List<ESIndexStats>> getIndexStatics(Long logicTemplateId, Long startDate, Long endDate);

    /**
     * 根据模板Id获取[startDate, endDate]的projectid访问统计信息
     * @param logicTemplateId
     * @param startDate
     * @param endDate
     * @return
     */
    Result<List<ProjectIdTemplateAccessCountVO>> getAccessAppInfos(int logicTemplateId, Long startDate, Long endDate);

}