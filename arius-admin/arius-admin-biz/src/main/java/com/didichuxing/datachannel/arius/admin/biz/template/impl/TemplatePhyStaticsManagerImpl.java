package com.didichuxing.datachannel.arius.admin.biz.template.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyStaticsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexStats;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ProjectIdTemplateAccessCountVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateStatsInfoVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateStatsService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Service
public class TemplatePhyStaticsManagerImpl implements TemplatePhyStaticsManager {

    private static final ILog    LOGGER = LogFactory.getLog(TemplatePhyStaticsManagerImpl.class);

    @Autowired
    private TemplateStatsService templateStatsService;

   

    @Override
    public Result<Map<Integer, Long>> getAccessStatsInfoByTemplateIdAndDays(int logicTemplateId, int days) {
        return templateStatsService.getAccessStatsInfoByTemplateIdAndDays(logicTemplateId, days);
    }

    @Override
    public Result<TemplateStatsInfoVO> getTemplateBaseStatisticalInfoByLogicTemplateId(Long logicTemplateId) {
        return Result.buildSucc(ConvertUtil.obj2Obj(
            templateStatsService.getTemplateBaseStatisticalInfoByLogicTemplateId(logicTemplateId).getData(),
            TemplateStatsInfoVO.class));
    }

    @Override
    public Result<List<ESIndexStats>> getIndexStatics(Long logicTemplateId, Long startDate, Long endDate) {
        return templateStatsService.getIndexStatis(logicTemplateId, startDate, endDate);
    }

    @Override
    public Result<List<ProjectIdTemplateAccessCountVO>> getAccessAppInfos(int logicTemplateId, Long startDate,
                                                                          Long endDate) {
        return Result.buildSucc(
            ConvertUtil.list2List(templateStatsService.getAccessAppInfos(logicTemplateId, startDate, endDate).getData(),
                ProjectIdTemplateAccessCountVO.class));
    }

}