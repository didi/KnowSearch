package com.didichuxing.datachannel.arius.admin.biz.template.srv.cold.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.cold.ColdManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegionFSInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.constant.template.NewTemplateSrvEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    
    public static final int MAX_HOT_DAY = 2;
    public static final int MIN_HOT_DAY = -2;
    
    private final static Integer RETRY_TIME = 3;

    @Override
    public NewTemplateSrvEnum templateSrv() {
        return NewTemplateSrvEnum.TEMPLATE_COLD;
    }

    @Override
    public Result<Void> move2ColdNode(Integer logicTemplateId) {
        if (!isTemplateSrvOpen(logicTemplateId)) {
            return Result.buildFail("没有开启冷热分离模板服务");
        }

        IndexTemplateWithPhyTemplates logicTemplateWithPhysicals = indexTemplateService.getLogicTemplateWithPhysicalsById(logicTemplateId);
        if (null == logicTemplateWithPhysicals) {
            return Result.buildFail("模板不存在");
        }

        IndexTemplatePhy masterPhyTemplate = logicTemplateWithPhysicals.getMasterPhyTemplate();
        if (null == masterPhyTemplate) {
            return Result.buildFail("主模板不存在");
        }

        List<ClusterRegion> coldRegionList = clusterRegionService.listColdRegionByCluster(masterPhyTemplate.getCluster());
        if (CollectionUtils.isEmpty(coldRegionList)) {
            LOGGER.warn("class=ColdManagerImpl||method=move2ColdNode||logicTemplate={}||no cold rack", logicTemplateId);
            return Result.buildFail("没有冷节点");
        }
        ClusterRegion minUsageColdRegion = getMinUsageColdRegion(masterPhyTemplate.getCluster(), coldRegionList);

        try {
            Result<Void> moveResult = movePerTemplate(masterPhyTemplate, minUsageColdRegion.getId().intValue());
            if (moveResult.failed()) {
                LOGGER.warn("class=ColdManagerImpl||method=move2ColdNode||template={}||msg=move2ColdNode fail", masterPhyTemplate.getName());
                return moveResult;
            }
        } catch (Exception e) {
            LOGGER.warn("class=ColdManagerImpl||method=move2ColdNode||template={}||errMsg={}", masterPhyTemplate.getName(), e.getMessage(), e);
            return Result.buildFail();
        }

        return Result.buildSucc();
    }

    @Override
    public int fetchClusterDefaultHotDay(String phyCluster) {
        return 0;
    }

    ////////////////////////////private method/////////////////////////////////////

    /**
     * 移动单个物理模板下的索引到冷节点
     * @param templatePhysical
     * @param coldRegionId
     * @return
     * @throws ESOperateException
     */
    private Result<Void> movePerTemplate(IndexTemplatePhy templatePhysical, Integer coldRegionId) throws ESOperateException {
        Tuple<Set<String>, Set<String>> coldAndHotIndices = getColdAndHotIndex(templatePhysical.getId());
        Set<String> coldIndex = coldAndHotIndices.getV1();
        Set<String> hotIndices = coldAndHotIndices.getV2();

        Boolean moveSuccFlag = Boolean.TRUE;
        if (!CollectionUtils.isEmpty(coldIndex)) {
            moveSuccFlag= esIndexService.syncBatchUpdateRegion(templatePhysical.getCluster(), Lists.newArrayList(coldIndex), coldRegionId, RETRY_TIME);
        }

        if (!moveSuccFlag && !CollectionUtils.isEmpty(hotIndices)) {
            moveSuccFlag = esIndexService.syncBatchUpdateRegion(templatePhysical.getCluster(), Lists.newArrayList(hotIndices), templatePhysical.getRegionId(), RETRY_TIME);
        }

        return Result.build(moveSuccFlag);
    }

    /**
     * 获取指定物理模板下的冷热索引
     * @param physicalId
     * @return
     */
    private Tuple</*冷节点索引列表*/Set<String>, /*热节点索引列表*/Set<String>> getColdAndHotIndex(Long physicalId) {
        IndexTemplatePhyWithLogic templatePhysicalWithLogic = indexTemplatePhyService.getTemplateWithLogicById(physicalId);
        if (templatePhysicalWithLogic == null) {
            return new Tuple<>();
        }

        int hotTime = templatePhysicalWithLogic.getLogicTemplate().getHotTime();

        if (hotTime <= 0) {
            LOGGER.info("class=ColdManagerImpl||method=getColdAndHotIndex||template={}||msg=hotTime illegal", templatePhysicalWithLogic.getName());
            return new Tuple<>();
        }

        if (hotTime >= templatePhysicalWithLogic.getLogicTemplate().getExpireTime()) {
            LOGGER.info("class=ColdManagerImpl||method=getColdAndHotIndex||||template={}||msg=all index is hot", templatePhysicalWithLogic.getName());
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
     * 根据接入集群可以连接的地址校验是否可以开启冷热分离服务
     * @param httpAddresses client地址
     * @return 校验的结果，返回模板服务id
     */
    @Override
    public Result<Boolean> checkOpenTemplateSrvWhenClusterJoin(String httpAddresses, String password) {
        return Result.buildSucc();
    }


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
        return Result.buildSucc();
    }



    /**************************************************** private method ****************************************************/
  

 

 



}