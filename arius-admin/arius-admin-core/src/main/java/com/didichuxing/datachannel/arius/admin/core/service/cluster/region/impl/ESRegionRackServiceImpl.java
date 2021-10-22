package com.didichuxing.datachannel.arius.admin.core.service.cluster.region.impl;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterRackInfoDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.template.TemplatePhysicalStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterRegionPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionBindEvent;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionCreateEvent;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionUnbindEvent;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.RackUtils;
import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.region.ClusterRegionDAO;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.CLUSTER_REGION;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.REGION;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.DELETE;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.EDIT;

/**
 * @Author: lanxinzheng
 * @Date: 2021/1/2
 * @Comment:
 */
@Service
public class ESRegionRackServiceImpl implements ESRegionRackService {

    private static final ILog     LOGGER = LogFactory.getLog(ESRegionRackServiceImpl.class);

    @Autowired
    private ClusterRegionDAO      clusterRegionDAO;

    @Autowired
    private ESClusterLogicService esClusterLogicService;

    @Autowired
    private ESClusterPhyService   esClusterPhyService;

    @Autowired
    private OperateRecordService  operateRecordService;

    @Autowired
    private TemplatePhyService    templatePhyService;

    @Deprecated
    @Override
    public boolean deleteRackById(Long rackId) {
        return false;
    }

    @Override
    public ClusterRegion getRegionById(Long regionId) {
        if (regionId == null) {
            return null;
        }
        return ConvertUtil.obj2Obj(clusterRegionDAO.getById(regionId), ClusterRegion.class);
    }

    @Override
    public List<ESClusterLogicRackInfo> listAllLogicClusterRacks() {

        // 获取已经被绑定的region
        List<ClusterRegion> regions = listAllBoundRegions();

        // 根据逻辑集群排序，没有必要但方便调试
        regions.sort(Comparator.comparing(ClusterRegion::getLogicClusterId));

        // 构建rack信息
        return buildRackInfos(regions);

    }

    @Override
    public List<ESClusterLogicRackInfo> listLogicClusterRacks(ESLogicClusterRackInfoDTO param) {
        // 目前仅逻辑集群ID条件有效，为兼容暂时保留该方法
        if (param == null || param.getLogicClusterId() == null) {
            return new ArrayList<>();
        }

        return listLogicClusterRacks(param.getLogicClusterId());
    }

    @Override
    public List<ESClusterLogicRackInfo> listLogicClusterRacks(Long logicClusterId) {
        List<ClusterRegion> regions = listLogicClusterRegions(logicClusterId);
        return buildRackInfos(regions);
    }

    @Override
    public Result addRackToLogicCluster(ESLogicClusterRackInfoDTO param, String operator) {
        return Result.buildFail("不再支持直接给逻辑集群添加rack资源");
    }

    @Override
    public List<ESClusterLogicRackInfo> listAssignedRacksByClusterName(String phyClusterName) {

        List<ClusterRegion> regionsInPhyCluster = listRegionsByClusterName(phyClusterName);
        List<ClusterRegion> boundRegions = regionsInPhyCluster.stream().filter(this::isRegionBound)
            .collect(Collectors.toList());

        return buildRackInfos(boundRegions);
    }

    @Override
    public List<String> listPhysicClusterNames(Long logicClusterId) {
        // 获取逻辑集群有的region
        List<ClusterRegion> regions = listLogicClusterRegions(logicClusterId);
        // 从region获取物理集群名
        Set<String> clusterNames = regions.stream().map(ClusterRegion::getPhyClusterName).collect(Collectors.toSet());
        return new ArrayList<>(clusterNames);
    }

    @Override
    public List<Integer> listPhysicClusterId(Long logicClusterId) {
        List<String> clusterNames = listPhysicClusterNames(logicClusterId);
        // 从物理集群名获取物理集群ID
        return clusterNames.stream().map(clusterName -> esClusterPhyService.getClusterByName(clusterName).getId())
            .collect(Collectors.toList());
    }

    @Override
    public int countRackMatchedRegion(String cluster, String racks) {

        if (StringUtils.isAnyBlank(cluster, racks)) {
            return 0;
        }

        // 获取物理集群下的region
        List<ClusterRegion> regions = listRegionsByClusterName(cluster);
        // 匹配到的region计数
        int count = 0;
        for (ClusterRegion region : regions) {
            if (RackUtils.hasIntersect(racks, RackUtils.racks2List(region.getRacks()))) {
                count++;
            }
        }
        return count;
    }

    @Override
    public List<ClusterRegion> listRegionsByLogicAndPhyCluster(Long logicClusterId, String phyClusterName) {
        if (logicClusterId == null || StringUtils.isBlank(phyClusterName)) {
            return new ArrayList<>();
        }

        ClusterRegionPO condt = new ClusterRegionPO();
        condt.setLogicClusterId(logicClusterId);
        condt.setPhyClusterName(phyClusterName);

        return ConvertUtil.list2List(clusterRegionDAO.listBoundRegionsByCondition(condt), ClusterRegion.class);
    }

    @Override
    public List<ClusterRegion> listPhyClusterRegions(String phyClusterName) {
        return ConvertUtil.list2List(clusterRegionDAO.getByPhyClusterName(phyClusterName), ClusterRegion.class);
    }

    @Override
    public List<ClusterRegion> listAllBoundRegions() {
        return ConvertUtil.list2List(clusterRegionDAO.listBoundRegions(), ClusterRegion.class);
    }

    @Override
    public Result<Long> createPhyClusterRegion(String clusterName, String racks, Integer share, String operator) {

        // 参数检查
        if (StringUtils.isBlank(clusterName)) {
            return Result.buildParamIllegal("物理集群名不能为空");
        }

        if (esClusterPhyService.getClusterByName(clusterName) == null) {
            return Result.buildParamIllegal(String.format("物理集群 %s 不存在", clusterName));
        }

        if (AriusObjUtils.isNull(racks)) {
            return Result.buildParamIllegal("racks为空");
        }

        Result checkRacksResult = checkRacks(clusterName, racks);
        if (checkRacksResult.failed()) {
            return Result.buildFrom(checkRacksResult);
        }

        if (share == null) {
            share = AdminConstant.YES;
        }

        if (!share.equals(AdminConstant.YES) && !share.equals(AdminConstant.NO)) {
            return Result.buildParamIllegal("指定的share非法");
        }

        ClusterRegionPO clusterRegionPO = new ClusterRegionPO();
        clusterRegionPO.setLogicClusterId(AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID);
        clusterRegionPO.setPhyClusterName(clusterName);
        clusterRegionPO.setRacks(racks);

        // 创建
        boolean succeed = clusterRegionDAO.insert(clusterRegionPO) == 1;
        LOGGER.info(
            "class=ESRegionRackServiceImpl||method=createPhyClusterRegion||region={}||result={}||msg=create phy cluster region",
            clusterRegionPO, succeed);

        if (succeed) {
            // 发送消息
            SpringTool.publish(new RegionCreateEvent(this, ConvertUtil.obj2Obj(clusterRegionPO, ClusterRegion.class),
                share, operator));
            // 操作记录
            operateRecordService.save(CLUSTER_REGION, ADD, clusterRegionPO.getId(), "", operator);
        }

        return Result.build(succeed, clusterRegionPO.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Long> createAndBindRegion(String clusterName, String racks, Long logicClusterId, Integer share,
                                            String operator) {
        try {
            // 创建region
            Result<Long> createRegionResult = createPhyClusterRegion(clusterName, racks, share, operator);
            if (createRegionResult.failed()) {
                throw new RuntimeException(createRegionResult.getMessage());
            }
            Long regionId = createRegionResult.getData();

            // 绑定region
            Result bindResult = bindRegion(regionId, logicClusterId, share, operator);
            if (bindResult.failed()) {
                throw new RuntimeException(bindResult.getMessage());
            }

            return Result.buildSucc(regionId);
        } catch (Exception e) {
            LOGGER.error(
                "class=ESRegionRackServiceImpl||method=createAndBindRegion||clusterName={}||racks={}||logicClusterId={}||share={}"
                         + "||operator={}||msg=create and bind region failed||e->",
                clusterName, racks, logicClusterId, share, operator, e);
            // 事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFrom(Result.buildFail(e.getMessage()));
        }
    }

    @Override
    public Result deletePhyClusterRegion(Long regionId, String operator) {
        if (regionId == null) {
            return Result.buildFail("regionId为null");
        }

        ClusterRegion region = getRegionById(regionId);
        if (region == null) {
            return Result.buildFail(String.format("region %d 不存在", regionId));
        }

        // 已经绑定过的region不能删除
        if (isRegionBound(region)) {
            return Result.buildFail(String.format("region %d 已经被绑定到逻辑集群 %d", regionId, region.getLogicClusterId()));
        }

        boolean succeed = clusterRegionDAO.delete(regionId) == 1;

        if (succeed) {
            // 发送消息
            SpringTool.publish(new RegionDeleteEvent(this, region, operator));
            // 操作记录
            operateRecordService.save(CLUSTER_REGION, DELETE, regionId, "", operator);
        }

        return Result.build(succeed);
    }

    @Override
    public Result deletePhyClusterRegionWithoutCheck(Long regionId, String operator) {
        if (regionId == null) {
            return Result.buildFail("regionId为null");
        }

        ClusterRegion regionPO = getRegionById(regionId);
        if (regionPO == null) {
            return Result.buildFail(String.format("region %d 不存在", regionId));
        }

        boolean succeed = clusterRegionDAO.delete(regionId) == 1;
        if (succeed) {
            // 发送消息
            SpringTool.publish(new RegionDeleteEvent(this, regionPO, operator));
            // 操作记录
            operateRecordService.save(CLUSTER_REGION, DELETE, regionId, "", operator);
        }
        return Result.build(succeed);
    }

    @Override
    public Result bindRegion(Long regionId, Long logicClusterId, Integer share, String operator) {

        try {
            // 判断region存在
            ClusterRegion region = getRegionById(regionId);
            if (region == null) {
                return Result.buildFail(String.format("region %d 不存在", regionId));
            }

            // 判断在未绑定状态
            if (isRegionBound(region)) {
                return Result.buildFail(String.format("region %d 已经被绑定", regionId));
            }

            // 检查逻辑集群存在
            if (esClusterLogicService.getLogicClusterById(logicClusterId) == null) {
                return Result.buildFail(String.format("逻辑集群 %S 不存在", logicClusterId));
            }

            if (share == null) {
                share = AdminConstant.YES;
            }

            if (!share.equals(AdminConstant.YES) && !share.equals(AdminConstant.NO)) {
                return Result.buildParamIllegal("指定的share非法");
            }

            // 绑定
            updateRegion(regionId, logicClusterId, null);

            // 发送消息，添加容量规划area（幂等地），添加容量规划容量信息
            SpringTool.publish(new RegionBindEvent(this, region, share, operator));
            operateRecordService.save(REGION, OperationEnum.REGION_BIND, regionId, "", operator);

            return Result.buildSucc();
        } catch (Exception e) {
            LOGGER.error(
                "class=ESRegionRackServiceImpl||method=bindRegion||regionId={}||logicClusterId={}||share={}||operator={}"
                         + "msg=bind region failed||e->",
                regionId, logicClusterId, share, operator, e);
            return Result.buildFail(e.getMessage());
        }
    }

    @Override
    public Result editRegionRacks(Long regionId, String racks, String operator) {
        if (regionId == null) {
            return Result.buildFail("未指定regionId");
        }

        ClusterRegion region = getRegionById(regionId);
        if (region == null) {
            return Result.buildFail(String.format("region %d 不存在", regionId));
        }

        Result checkRacksResult = checkRacks(region.getPhyClusterName(), racks);
        if (checkRacksResult.failed()) {
            return Result.buildFrom(checkRacksResult);
        }

        // 更新rack
        String oldRacks = region.getRacks();
        updateRegion(regionId, null, racks.trim());

        LOGGER.info(
            "class=ESRegionRackServiceImpl||method=editRegionRacks||regionId={}||oldRacks={}||newRacks={}||operator={}"
                    + "msg=edit region",
            regionId, oldRacks, racks, operator);

        // 操作记录
        operateRecordService.save(REGION, EDIT, regionId, String.format("%s -> %s", oldRacks, racks), operator);

        return Result.buildSucc();
    }

    @Override
    public Result unbindRegion(Long regionId, String operator) {
        try {
            // 判断region存在
            ClusterRegion region = getRegionById(regionId);
            if (region == null) {
                return Result.buildFail(String.format("region %d 不存在", regionId));
            }

            // 判断在绑定状态
            if (!isRegionBound(region)) {
                return Result.buildFail(String.format("region %d 未被绑定", regionId));
            }

            // 判断region上没有模板
            List<IndexTemplatePhy> clusterTemplates = templatePhyService.getTemplateByRegionId(regionId);
            if (CollectionUtils.isNotEmpty(clusterTemplates)) {
                return Result.buildFail(String.format("region %d 上已经分配模板", regionId));
            }

            // 删除绑定
            updateRegion(regionId, AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID, null);

            // 发送消息，删除容量规划容量信息
            SpringTool.publish(new RegionUnbindEvent(this, region, operator));

            // 操作记录
            operateRecordService.save(REGION, OperationEnum.REGION_UNBIND, regionId, "", operator);

            return Result.buildSucc();
        } catch (Exception e) {
            LOGGER.error("class=ESRegionRackServiceImpl||method=unbindRegion||regionId={}||operator={}"
                         + "msg=unbind region failed||e->",
                regionId, operator, e);
            return Result.buildFail(e.getMessage());
        }
    }

    /**
     * 获取逻辑集群拥有的region
     * @param logicClusterId 逻辑集群ID
     * @return 逻辑集群拥有的region
     */
    @Override
    public List<ClusterRegion> listLogicClusterRegions(Long logicClusterId) {

        if (logicClusterId == null) {
            return new ArrayList<>();
        }

        return ConvertUtil.list2List(clusterRegionDAO.listByLogicClusterId(logicClusterId), ClusterRegion.class);
    }

    /**
     * 获取物理下的region
     * @param phyClusterName 物理集群名
     * @return 物理集群下的region
     */
    @Override
    public List<ClusterRegion> listRegionsByClusterName(String phyClusterName) {
        if (StringUtils.isBlank(phyClusterName)) {
            return new ArrayList<>();
        }
        return ConvertUtil.list2List(clusterRegionDAO.getByPhyClusterName(phyClusterName), ClusterRegion.class);
    }

    /**
     * 判断region是否已经被绑定给逻辑集群
     * @param region region
     * @return true-已经被绑定，false-没有被绑定
     */
    @Override
    public boolean isRegionBound(ClusterRegion region) {
        if (region == null) {
            return false;
        }

        return !region.getLogicClusterId().equals(AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID);
    }

    @Override
    public Long getLogicClusterIdByPhyClusterId(Integer phyClusterId) {
        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterById(phyClusterId);
        if (esClusterPhy == null) {
            return null;
        }
        List<ClusterRegion> clusterRegions = listRegionsByClusterName(esClusterPhy.getCluster());
        if (clusterRegions == null || clusterRegions.size() == 0) {
            return null;
        }
        ClusterRegion clusterRegion = getRegionById(clusterRegions.get(0).getId());
        return clusterRegion.getLogicClusterId();
    }

    /***************************************** private method ****************************************************/
    /**
     * 构建region下的rack信息
     * @param region region
     * @return
     */
    private List<ESClusterLogicRackInfo> buildRackInfos(ClusterRegion region) {
        List<ESClusterLogicRackInfo> rackInfos = new LinkedList<>();
        if (region == null) {
            return rackInfos;
        }

        for (String rack : RackUtils.racks2List(region.getRacks())) {
            ESClusterLogicRackInfo rackInfo = new ESClusterLogicRackInfo();
            rackInfo.setLogicClusterId(region.getLogicClusterId());
            rackInfo.setPhyClusterName(region.getPhyClusterName());
            rackInfo.setRegionId(region.getId());
            rackInfo.setRack(rack);
            rackInfos.add(rackInfo);
        }

        return rackInfos;
    }

    private List<ESClusterLogicRackInfo> buildRackInfos(List<ClusterRegion> regions) {
        List<ESClusterLogicRackInfo> rackInfos = new LinkedList<>();
        if (CollectionUtils.isEmpty(regions)) {
            return rackInfos;
        }

        for (ClusterRegion region : regions) {
            rackInfos.addAll(buildRackInfos(region));
        }
        return rackInfos;
    }

    /**
     * 根据regionId更新region的logicClusterId或racks
     * @param regionId       要更新的region的ID
     * @param logicClusterId 逻辑集群ID，为null则不更新
     * @param racks          racks，为null则不更新
     */
    private void updateRegion(Long regionId, Long logicClusterId, String racks) {
        if (regionId == null) {
            return;
        }

        ClusterRegionPO updateParam = new ClusterRegionPO();
        updateParam.setId(regionId);
        updateParam.setLogicClusterId(logicClusterId);
        updateParam.setRacks(racks);

        clusterRegionDAO.update(updateParam);
    }

    private Result checkRacks(String phyClusterName, String racks) {

        Set<String> rackSet = RackUtils.racks2Set(racks);
        if (CollectionUtils.isEmpty(rackSet)) {
            return Result.buildParamIllegal("racks is blank");
        }

        Set<String> racksInCluster = esClusterPhyService.getClusterRacks(phyClusterName);

        if (!racksInCluster.containsAll(rackSet)) {
            return Result.buildParamIllegal(String.format("racks %s not found in cluster %s",
                RackUtils.removeRacks(racks, racksInCluster), phyClusterName));
        }

        return Result.buildSucc();
    }

}
