package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType.FAIL;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ClusterRegionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.LogicClusterRackVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.PhyClusterRackVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.exception.BaseRunTimeException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;

@Component
public class ClusterRegionManagerImpl implements ClusterRegionManager {

    @Autowired
    private ESRegionRackService   esRegionRackService;

    @Autowired
    private ClusterContextManager clusterContextManager;

    @Autowired
    private ESClusterLogicService esClusterLogicService;

    /**
     * 构建regionVO
     * @param regions region列表
     * @return
     */
    @Override
    public List<ClusterRegionVO> buildLogicClusterRegionVO(List<ClusterRegion> regions) {
        if (CollectionUtils.isEmpty(regions)) {
            return new ArrayList<>();
        }

        return regions.stream().filter(Objects::nonNull).map(this::buildLogicClusterRegionVO)
            .collect(Collectors.toList());
    }

    /**
     * 构建regionVO
     * @param region region
     * @return
     */
    @Override
    public ClusterRegionVO buildLogicClusterRegionVO(ClusterRegion region) {
        if (region == null) {
            return null;
        }

        ClusterRegionVO logicClusterRegionVO = new ClusterRegionVO();
        logicClusterRegionVO.setId(region.getId());
        logicClusterRegionVO.setLogicClusterId(region.getLogicClusterId());
        logicClusterRegionVO.setClusterName(region.getPhyClusterName());
        logicClusterRegionVO.setRacks(region.getRacks());
        return logicClusterRegionVO;
    }

    /**
     * 构建物理集群Racks
     * @param cluster       物理集群名
     * @param clusterRacks  物理集群的所有Racks
     * @param usedRacksInfo 物理已经被使用（绑定成region）的rack信息
     * @return
     */
    @Override
    public List<PhyClusterRackVO> buildPhyClusterRackVOs(String cluster, Set<String> clusterRacks,
                                                         List<ESClusterLogicRackInfo> usedRacksInfo) {

        List<PhyClusterRackVO> racks = new ArrayList<>();

        Set<String> usedRacks = filterClusterUsedRacks(cluster, usedRacksInfo);
        Set<String> unusedRacks = fetchUnusedRacks(clusterRacks, usedRacks);

        racks.addAll(batchBuildClusterRackVOs(cluster, usedRacks, 1));
        racks.addAll(batchBuildClusterRackVOs(cluster, unusedRacks, 0));

        return racks;
    }

    /**
     * 构建逻辑集群物RackVO
     * @param logicClusterRackInfos 逻辑集群rack信息
     * @return
     */
    @Override
    public List<LogicClusterRackVO> buildLogicClusterRackVOs(List<ESClusterLogicRackInfo> logicClusterRackInfos) {
        if (CollectionUtils.isEmpty(logicClusterRackInfos)) {
            return new ArrayList<>();
        }

        return logicClusterRackInfos.stream().filter(Objects::nonNull).map(this::buildLogicClusterRackVO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> batchBindRegionToClusterLogic(ESLogicClusterWithRegionDTO param, String operator,
                                                      boolean isAddClusterLogicFlag) {
        //1. 前置校验
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("参数为空");
        }

        List<ClusterRegionDTO> clusterRegionDTOS = param.getClusterRegionDTOS();
        if (CollectionUtils.isEmpty(clusterRegionDTOS)) {
            return Result.buildParamIllegal("逻辑集群关联region信息为空");
        }

        //2. 集群关联校验
        clusterRegionDTOS
                .stream()
                .map(ClusterRegionDTO::getPhyClusterName)
                .distinct()
                .forEach(clusterPhyName -> checkCanBeBound(param.getId(), clusterPhyName, param.getType()));

        //3. 是否要创建逻辑集群
        if (isAddClusterLogicFlag) {
            param.setDataCenter(EnvUtil.getDC().getCode());
            Result<Long> createLogicClusterResult = esClusterLogicService.createLogicCluster(param, operator);
            if (createLogicClusterResult.failed()) {
                return createLogicClusterResult;
            }
            param.setId(createLogicClusterResult.getData());
        }

        //4. 为逻辑集群绑定region
        clusterRegionDTOS.forEach(region -> doBindRegionToClusterLogic(param.getId(), region.getId(), operator));

        return Result.buildSucc();
    }

    /***************************************** private method ****************************************************/
    /**
     * 获取没有使用的Rack列表
     * @param clusterRacks 物理集群Rack
     * @param usedRacks    使用的Rack
     * @return
     */
    private Set<String> fetchUnusedRacks(Set<String> clusterRacks, Set<String> usedRacks) {

        Set<String> unusedRacks = new HashSet<>(clusterRacks);

        for (String rack : clusterRacks) {
            if (usedRacks.contains(rack)) {
                unusedRacks.remove(rack);
            }
        }

        return unusedRacks;
    }

    /**
     * 获取物理集群使用的Rack列表
     * @param cluster       物理集群
     * @param usedRackInfos 使用的Rack列表
     * @return
     */
    private Set<String> filterClusterUsedRacks(String cluster, List<ESClusterLogicRackInfo> usedRackInfos) {
        if (CollectionUtils.isEmpty(usedRackInfos)) {
            return new HashSet<>();
        }

        return usedRackInfos.stream().filter(usedRackInfo -> usedRackInfo.getPhyClusterName().equals(cluster))
            .map(ESClusterLogicRackInfo::getRack).collect(Collectors.toSet());
    }

    /**
     * 批量构建物理集群RackVO
     * @param cluster   物理集群
     * @param racks     Rack列表
     * @param usedFlags 使用标示
     * @return
     */
    private List<PhyClusterRackVO> batchBuildClusterRackVOs(String cluster, Set<String> racks, Integer usedFlags) {

        return racks.stream().map(rack -> buildClusterRackVO(cluster, rack, usedFlags)).collect(Collectors.toList());
    }

    /**
     * 构建物理集群RackVO
     * @param cluster   物理集群
     * @param rack      rack信息
     * @param usedFlags 使用标示
     * @return
     */
    private PhyClusterRackVO buildClusterRackVO(String cluster, String rack, Integer usedFlags) {
        PhyClusterRackVO clusterRack = new PhyClusterRackVO();
        clusterRack.setCluster(cluster);
        clusterRack.setRack(rack);
        clusterRack.setUsageFlags(usedFlags);
        return clusterRack;
    }

    private LogicClusterRackVO buildLogicClusterRackVO(ESClusterLogicRackInfo esClusterLogicRackInfo) {
        if (esClusterLogicRackInfo == null) {
            return null;
        }

        LogicClusterRackVO logicClusterRackVO = new LogicClusterRackVO();
        logicClusterRackVO.setResourceId(esClusterLogicRackInfo.getLogicClusterId());
        logicClusterRackVO.setCluster(esClusterLogicRackInfo.getPhyClusterName());
        logicClusterRackVO.setRack(esClusterLogicRackInfo.getRack());
        return logicClusterRackVO;
    }

    private void checkCanBeBound(Long clusterLogicId, String clusterPhyName, Integer clusterLogicType) {
        Result validResult = clusterContextManager.canClusterLogicAssociatedPhyCluster(clusterLogicId, clusterPhyName,
            clusterLogicType);
        if (validResult.failed()) {
            throw new BaseRunTimeException(validResult.getMessage(), FAIL);
        }
    }

    private void doBindRegionToClusterLogic(Long clusterLogicId, Long regionId, String operator) {
        Result bindRegionResult = esRegionRackService.bindRegion(regionId, clusterLogicId, null, operator);
        if (bindRegionResult.failed()) {
            throw new BaseRunTimeException(bindRegionResult.getMessage(), FAIL);
        }
    }
}
