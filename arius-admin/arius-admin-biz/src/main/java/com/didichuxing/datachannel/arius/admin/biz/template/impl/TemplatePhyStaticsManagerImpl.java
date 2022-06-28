package com.didichuxing.datachannel.arius.admin.biz.template.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyStaticsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexStats;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ProjectIdTemplateAccessCountVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateStatsInfoVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateSattisService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateValueService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Service
public class TemplatePhyStaticsManagerImpl implements TemplatePhyStaticsManager {

    private static final ILog         LOGGER = LogFactory.getLog( TemplatePhyStaticsManagerImpl.class);

 

    @Autowired
    private TemplateSattisService       templateSattisService;


    @Autowired
    private TemplateValueService templateValueService;

    @Override
    public Result<Map<Integer, Long>> getAccessStatsInfoByTemplateIdAndDays(int logicTemplateId, int days) {
        return templateSattisService.getAccessStatsInfoByTemplateIdAndDays(logicTemplateId, days);
    }

    @Override
    public Result<TemplateStatsInfoVO> getTemplateBaseStatisticalInfoByLogicTemplateId(Long logicTemplateId) {
        return Result.buildSucc(ConvertUtil.obj2Obj(
                templateSattisService.getTemplateBaseStatisticalInfoByLogicTemplateId(logicTemplateId).getData(),
                TemplateStatsInfoVO.class));
    }

    @Override
    public Result<List<ProjectIdTemplateAccessCountVO>> getAccessAppInfos(int logicTemplateId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
                templateSattisService.getAccessAppInfos(logicTemplateId, startDate, endDate).getData(),
                ProjectIdTemplateAccessCountVO.class));
    }

    @Override
    public Result<List<ESIndexStats>> getIndexStatics(Long logicTemplateId, Long startDate, Long endDate) {
        return templateSattisService.getIndexStatis(logicTemplateId, startDate, endDate);
    }



  
}