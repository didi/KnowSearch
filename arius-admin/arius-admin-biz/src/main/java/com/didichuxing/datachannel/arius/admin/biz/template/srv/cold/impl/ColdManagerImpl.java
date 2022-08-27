package com.didichuxing.datachannel.arius.admin.biz.template.srv.cold.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.cold.ColdManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegionFSInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.*;

/**
 * @author chengxiang, zqr
 * @date 2022/5/13
 */
@Service
public class ColdManagerImpl extends BaseTemplateSrvImpl implements ColdManager {

    @Autowired
    private ESIndexService       esIndexService;

    @Autowired
    private ClusterRegionService   clusterRegionService;
    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    public static final int      MAX_HOT_DAY = 2;
    public static final int      MIN_HOT_DAY = -2;

    private final static Integer RETRY_TIME  = 3;

    @Override
    public TemplateServiceEnum templateSrv() {
        return TemplateServiceEnum.TEMPLATE_COLD;
    }

    @Override
    public Result<Boolean> move2ColdNode(Integer logicTemplateId) throws ESOperateException {
        if (Boolean.FALSE.equals(isTemplateSrvOpen(logicTemplateId))) {
            return Result.buildSucc();
        }

        IndexTemplateWithPhyTemplates logicTemplateWithPhysicals = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicTemplateId);
        if (null == logicTemplateWithPhysicals) {
            LOGGER.info(
                    "class=ColdManagerImpl||method=move2ColdNode||logicTemplateId={}||msg=ColdDataMoveTask no template",
                    logicTemplateId);
            return Result.buildSucc();
        }

        IndexTemplatePhy masterPhyTemplate = logicTemplateWithPhysicals.getMasterPhyTemplate();
        if (null == masterPhyTemplate) {
            LOGGER.info(
                    "class=ColdManagerImpl||method=move2ColdNode||logicTemplateId={}||msg=ColdDataMoveTask no master template",
                    logicTemplateId);
            return Result.buildSucc();
        }

        List<ClusterRegion> coldRegionList = clusterRegionService
            .listColdRegionByCluster(masterPhyTemplate.getCluster());
        if (CollectionUtils.isEmpty(coldRegionList)) {
            LOGGER.warn("class=ColdManagerImpl||method=move2ColdNode||logicTemplate={}||no cold rack", logicTemplateId);
            return Result.buildSucc();
        }
        ClusterRegion minUsageColdRegion = getMinUsageColdRegion(masterPhyTemplate.getCluster(), coldRegionList);
        //minUsageColdRegion可能为空
        if (Objects.isNull(minUsageColdRegion)){
            LOGGER.warn("class=ColdManagerImpl||method=move2ColdNode||logicTemplate={}||no cold rack", logicTemplateId);
            return Result.buildSucc();
        }
            Result<Void> moveResult = movePerTemplate(masterPhyTemplate, minUsageColdRegion.getId().intValue());
            if (moveResult.failed()) {
                LOGGER.warn("class=ColdManagerImpl||method=move2ColdNode||template={}||msg=move2ColdNode fail",
                    masterPhyTemplate.getName());
                return Result.buildFrom(moveResult);
            }
       

        return Result.build(Boolean.TRUE);
    }

    @Override
    public int fetchClusterDefaultHotDay(String phyCluster) {
        int hotDay = -1;
        Set<String> enableClusterSet = ariusConfigInfoService.stringSettingSplit2Set(ARIUS_COMMON_GROUP,
            "platform.govern.cold.data.move2ColdNode.enable.clusters", "", ",");
        if (enableClusterSet.contains(phyCluster)) {
            int defaultHotDay = getDefaultHotDay();
            if (defaultHotDay > 0) {
                hotDay = defaultHotDay;
            }
        }

        LOGGER.info(
            "class=TemplateColdManagerImpl||method=fetchClusterDefaultHotDay||msg=no changed||cluster={}||enableClusters={}||version={}",
            phyCluster, JSON.toJSONString(enableClusterSet), hotDay);

        return hotDay;
    }

    ////////////////////////////private method/////////////////////////////////////
    /**
    * 获取配置默认hotDay值
    *
    * @return
    */
    private int getDefaultHotDay() {
        String defaultDay = ariusConfigInfoService.stringSetting(ARIUS_TEMPLATE_COLD_GROUP,
            INDEX_TEMPLATE_COLD_DAY_DEFAULT, INDEX_TEMPLATE_COLD_DAY_DEFAULT_VALUE);
        LOGGER.info("class=TemplateColdManagerImpl||method=getDefaultHotDay||msg=defaultDay: {}", defaultDay);
        if (StringUtils.isNotBlank(defaultDay)) {
            try {
                JSONObject object = JSON.parseObject(defaultDay);
                return object.getInteger("defaultHotDay");
            } catch (JSONException e) {
                LOGGER.warn("class=TemplateColdManagerImpl||method=getDefaultHotDay||errMsg={}", e.getMessage());
            }
        }
        return -1;
    }

    /**
     * 移动单个物理模板下的索引到冷节点
     * @param templatePhysical
     * @param coldRegionId
     * @return
     * @throws ESOperateException
     */
    private Result<Void> movePerTemplate(IndexTemplatePhy templatePhysical,
                                         Integer coldRegionId) throws ESOperateException {
        Tuple<Set<String>, Set<String>> coldAndHotIndices = getColdAndHotIndex(templatePhysical.getId());
        Set<String> coldIndex = coldAndHotIndices.getV1();
        Set<String> hotIndices = coldAndHotIndices.getV2();
        
        Boolean moveSuccFlag = Boolean.TRUE;
        Function</*coldRegionId*/Integer,Result<List<ClusterRoleHost>>> coldRegionIdFunc=
                coldId-> clusterRoleHostService.listByRegionId(coldId);
        if (!CollectionUtils.isEmpty(coldIndex)) {
            moveSuccFlag = esIndexService.syncBatchUpdateRegion(templatePhysical.getCluster(),
                Lists.newArrayList(coldIndex), coldRegionId, RETRY_TIME,coldRegionIdFunc );
        }

        if (!moveSuccFlag && !CollectionUtils.isEmpty(hotIndices)) {
            moveSuccFlag = esIndexService.syncBatchUpdateRegion(templatePhysical.getCluster(),
                Lists.newArrayList(hotIndices), templatePhysical.getRegionId(), RETRY_TIME, coldRegionIdFunc);
        }

        return Result.build(moveSuccFlag);
    }

    /**
     * 获取指定物理模板下的冷热索引
     * @param physicalId
     * @return
     */
    private Tuple</*冷节点索引列表*/Set<String>, /*热节点索引列表*/Set<String>> getColdAndHotIndex(Long physicalId) {
        IndexTemplatePhyWithLogic templatePhysicalWithLogic = indexTemplatePhyService
            .getTemplateWithLogicById(physicalId);
        if (templatePhysicalWithLogic == null) {
            return new Tuple<>();
        }

        int hotTime = templatePhysicalWithLogic.getLogicTemplate().getHotTime();

        if (hotTime <= 0) {
            LOGGER.info("class=ColdManagerImpl||method=getColdAndHotIndex||template={}||msg=hotTime illegal",
                templatePhysicalWithLogic.getName());
            return new Tuple<>();
        }

        if (hotTime >= templatePhysicalWithLogic.getLogicTemplate().getExpireTime()) {
            LOGGER.info("class=ColdManagerImpl||method=getColdAndHotIndex||||template={}||msg=all index is hot",
                templatePhysicalWithLogic.getName());
            return new Tuple<>();
        }

        return templatePhyManager.getHotAndColdIndexByBeforeDay(templatePhysicalWithLogic, hotTime);
    }

    private ClusterRegion getMinUsageColdRegion(String cluster, List<ClusterRegion> regionList) {
        if (CollectionUtils.isEmpty(regionList)) {
            return null;
        }

        Map<Integer, ClusterRegionFSInfo> regionId2FsInfoMap = clusterRegionService.getClusterRegionFSInfo(cluster);
        if (MapUtils.isEmpty(regionId2FsInfoMap)) {
            return null;
        }

        ClusterRegion minUsageColdRegion = regionList.get(0);
        Double maxFreeDiskRatio = Double.MIN_VALUE;
        for (ClusterRegion region : regionList) {
            ClusterRegionFSInfo fsInfo = regionId2FsInfoMap.get(region.getId().intValue());
            if (null == fsInfo) {
                continue;
            }

            Double freeDiskRatio = fsInfo.getAvailableInBytes().doubleValue() / fsInfo.getTotalInBytes();
            if (freeDiskRatio > maxFreeDiskRatio) {
                minUsageColdRegion = region;
                maxFreeDiskRatio = freeDiskRatio;
            }
        }

        return minUsageColdRegion;
    }
    

    /////////////////srv

   
    /**
     * 确保搬迁配置是打开的
     *
     * 修改索引的rack
     *
     * 通过tts任务触发，任务需要幂等，需要多次重试，确保成功
     *
     * @return result
     */
    @Override
    public Result<Boolean> move2ColdNode(String phyCluster) {
        final List<ClusterRegion> coldRegionByPhyCluster = clusterRegionManager.getColdRegionByPhyCluster(phyCluster);
        if (CollectionUtils.isEmpty(coldRegionByPhyCluster)){
            //没有冷节点
            return Result.buildFail(String.format("【%s】没有冷节点", phyCluster));
        }
        
    
        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService.getNormalTemplateByCluster(phyCluster);
    
        if (CollectionUtils.isEmpty(templatePhysicals)) {
            return Result.buildSucc(true);
        }
        final ClusterRegion region = coldRegionByPhyCluster.get(0);
    
        int succ = 0;
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            try {
                //该逻辑模版没有开启冷热分离的节点
                if (Boolean.FALSE.equals(isTemplateSrvOpen(templatePhysical.getLogicId()))) {
                    continue;
                }
                Result<Void> moveResult = movePerTemplate(templatePhysical, region.getId().intValue());
                if (moveResult.success()) {
                    succ++;
                } else {
                    LOGGER.warn(
                            "class=TemplateColdManagerImpl||method=move2ColdNode||template={}||msg=move2ColdNode fail",
                            templatePhysical.getName());
                }
            } catch (Exception e) {
                LOGGER.warn("class=TemplateColdManagerImpl||method=move2ColdNode||template={}||errMsg={}",
                        templatePhysical.getName(), e.getMessage(), e);
            }
        }
    
        return Result.buildSucc(succ * 1.0 / templatePhysicals.size() > 0.8);
    }

    /**
     * 批量修改hotDays
     *
     * @param days           变量
     * @param operator       操作人
     * @param templateIdList
     * @param projectId
     * @return result
     */
    @Override
    public Result<Integer> batchChangeHotDay(Integer days, String operator, List<Integer> templateIdList,
                                             Integer projectId) {
        if (days > MAX_HOT_DAY || days < MIN_HOT_DAY) {
            return Result.buildParamIllegal("冷热分离的时间参数非法, 介于[1, 3]");
        }

        int count = indexTemplateService.batchChangeHotDay(days, templateIdList);

        LOGGER.info("class=TemplateColdManagerImpl||method=batchChangeHotDay||days={}||count={}||operator={}", days,
            count, operator);
        for (Integer id : templateIdList) {
            operateRecordService.save(
                new OperateRecord.Builder().userOperation(operator).operationTypeEnum(OperateTypeEnum.TEMPLATE_SERVICE)
                    .bizId(id).project(projectService.getProjectBriefByProjectId(projectId))

                    .content("deltaHotDays:" + days).buildDefaultManualTrigger());
        }

        return Result.buildSucc(count);
    }

    /**************************************************** private method ****************************************************/

}