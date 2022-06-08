package com.didichuxing.datachannel.arius.admin.biz.template.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyStatisManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexStats;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.AppIdTemplateAccessCountVO;
import com.didichuxing.datachannel.arius.admin.common.bean.common.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ProjectIdTemplateAccessCountVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateHealthDegreeRecordVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateStatsInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateValueRecordVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateHealthDegreeService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateSattisService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateValueService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Service
public class TemplatePhyStatisManagerImpl implements TemplatePhyStatisManager {

    private static final ILog         LOGGER = LogFactory.getLog( TemplatePhyStatisManagerImpl.class);

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private TemplateSattisService       templateSattisService;

    @Autowired
    private ClusterLogicService clusterLogicService;

    @Autowired
    private ClusterRegionService clusterRegionService;

    @Autowired
    private TemplateHealthDegreeService templateHealthDegreeService;

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
    public Result<List<ESIndexStats>> getIndexStatis(Long logicTemplateId, Long startDate, Long endDate) {
        return templateSattisService.getIndexStatis(logicTemplateId, startDate, endDate);
    }

    @Override
    public Result<List<TemplateHealthDegreeRecordVO>> getHealthDegreeRecordByLogicTemplateId(Long logicTemplateId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
                templateHealthDegreeService.getRecordByLogicTemplateId(logicTemplateId, startDate, endDate),
                TemplateHealthDegreeRecordVO.class));
    }

    @Override
    public Result<List<TemplateValueRecordVO>> getValueRecordByLogicTemplateId(Long logicTemplateId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
                templateValueService.getRecordByLogicTemplateId(logicTemplateId, startDate, endDate),
                TemplateValueRecordVO.class));
    }
}