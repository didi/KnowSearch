package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType.FAIL;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionWithNodeInfoVO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ResourceLogicTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.LogicClusterRackVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PhyClusterRackVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.exception.AriusRunTimeException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;

@Component
public class ClusterRegionManagerImpl implements ClusterRegionManager {

    private static final ILog     LOGGER = LogFactory.getLog(ClusterRegionManagerImpl.class);

    @Autowired
    private ClusterRegionService clusterRegionService;

    @Autowired
    private ClusterContextManager clusterContextManager;

    @Autowired
    private ClusterLogicService   clusterLogicService;

    @Autowired
    private ClusterPhyService     clusterPhyService;

    @Autowired
    private TemplateSrvManager    templateSrvManager;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

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
     * 逻辑集群绑定同一个物理集群的region的时候需要根据类型进行过滤
     * @param clusterLogicId 逻辑集群id
     * @param phyCluster 物理集群名称
     * @param clusterLogicType 逻辑集群类型
     * @return
     */
    @Override
    public List<ClusterRegion> filterClusterRegionByLogicClusterType(Long clusterLogicId, String phyCluster, Integer clusterLogicType) {
        if (ResourceLogicTypeEnum.valueOf(clusterLogicType).equals(ResourceLogicTypeEnum.UNKNOWN)) {
            return new ArrayList<>();
        }

        //根据物理集群获取全量的region数据
        List<ClusterRegion> clusterRegions = clusterRegionService.listPhyClusterRegions(phyCluster);
        if (CollectionUtils.isEmpty(clusterRegions)) {
            return clusterRegions;
        }

        //只有当物理集群上的region没有被绑定或者逻辑集群类型为public时region只被绑定到了共享类型的逻辑集群
        return clusterRegions.stream()
                .filter(clusterRegion -> canBindRegionToLogicCluster(clusterRegion, clusterLogicType, clusterLogicId))
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
        logicClusterRegionVO.setLogicClusterIds(region.getLogicClusterIds());
        logicClusterRegionVO.setClusterName(region.getPhyClusterName());
        logicClusterRegionVO.setRacks(region.getRacks());
        return logicClusterRegionVO;
    }

    @Override
    public List<PhyClusterRackVO> buildCanDividePhyClusterRackVOs(String cluster) {
        // 所有的racks
        Set<String> clusterRacks                 =   clusterPhyService.getClusterRacks(cluster);
        List<PhyClusterRackVO> racks             =   Lists.newArrayList();
        Set<String>            usedRacks         =   fetchDivideRacks(cluster, clusterRacks);
        Set<String>            unusedRacks       =   fetchUnusedRacks(clusterRacks, usedRacks);

        racks.addAll(batchBuildClusterRackVOs(cluster, usedRacks, 1));
        racks.addAll(batchBuildClusterRackVOs(cluster, unusedRacks, 0));

        LOGGER.info(
            "class=ClusterRegionManagerImpl||method=buildCanDividePhyClusterRackVOs||clusterRacks={}||usedRacksInfo={}||racks={}",
            clusterRacks, usedRacks, racks);
        return racks;
    }

    /**
     * 获取已经划分在region中的racks
     * @param phyClusterName
     * @param clusterRacks
     * @return
     */
    private Set<String> fetchDivideRacks(String phyClusterName, Set<String> clusterRacks) {
        List<ClusterRegion> regions = clusterRegionService.listRegionsByClusterName(phyClusterName);
        List<String> regionRacks    = Lists.newArrayList();
        regions.forEach(region-> regionRacks.addAll(ListUtils.string2StrList(region.getRacks())));

        return clusterRacks.stream().filter(regionRacks::contains).collect(Collectors.toSet());
    }

    /**
     * 构建逻辑集群物RackVO
     * @param logicClusterRackInfos 逻辑集群rack信息
     * @return
     */
    @Override
    public List<LogicClusterRackVO> buildLogicClusterRackVOs(List<ClusterLogicRackInfo> logicClusterRackInfos) {
        if (CollectionUtils.isEmpty(logicClusterRackInfos)) {
            return new ArrayList<>();
        }

        return logicClusterRackInfos.stream().filter(Objects::nonNull).map(this::buildLogicClusterRackVO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> batchBindRegionToClusterLogic(ESLogicClusterWithRegionDTO param, String operator,
                                                      boolean isAddClusterLogicFlag) {
        //1. 前置校验
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("参数为空");
        }
        if (CollectionUtils.isEmpty(param.getClusterRegionDTOS())) {
            return Result.buildParamIllegal("逻辑集群关联region信息为空");
        }

        //2. 集群合法关联性校验
        param.getClusterRegionDTOS().stream().distinct()
            .forEach(clusterRegionDTO -> checkCanBeBound(param.getId(), clusterRegionDTO, param.getType()));

        //3. 逻辑集群绑定的物理集群版本一致性校验
        Result<Void> phyClusterVersionsResult = boundPhyClusterVersionsCheck(param);
        if (phyClusterVersionsResult.failed()) {
            return Result.buildFrom(phyClusterVersionsResult);
        }

        //4. 是否要创建逻辑集群
        if (isAddClusterLogicFlag) {
            param.setDataCenter(EnvUtil.getDC().getCode());
            Result<Long> createLogicClusterResult = clusterLogicService.createClusterLogic(param);
            if (createLogicClusterResult.failed()) {
                return Result.buildFrom(createLogicClusterResult);
            }
            param.setId(createLogicClusterResult.getData());
        }

        //5. 初始化物理集群索引服务
        initTemplateSrvOfClusterPhy(param, operator);

        //6. 为逻辑集群绑定region
        return doBindRegionToClusterLogic(param, operator);
    }

    @Override
    public Result<Void> unbindRegion(Long regionId, Long logicClusterId, String operator) {
        return clusterRegionService.unbindRegion(regionId, logicClusterId, operator);
    }

    @Override
    public Result<Void> bindRegion(Long regionId, Long logicClusterId, Integer share, String operator) {
        return clusterRegionService.bindRegion(regionId, logicClusterId, share, operator);
    }

    @Override
    public Result<List<ClusterRegionWithNodeInfoVO>> getClusterRegionWithNodeInfoByClusterName(String clusterName) {
        List<ClusterRegion> clusterRegions = clusterRegionService.listRegionsByClusterName(clusterName);
        if (CollectionUtils.isEmpty(clusterRegions)) { return Result.buildSucc();}

        List<ClusterRegionWithNodeInfoVO> clusterRegionWithNodeInfoVOS = ConvertUtil.list2List(clusterRegions, ClusterRegionWithNodeInfoVO.class);
        for (ClusterRegionWithNodeInfoVO clusterRegionWithNodeInfoVO : clusterRegionWithNodeInfoVOS) {
            Result<List<ClusterRoleHost>> ret = clusterRoleHostService.listByRegionId(clusterRegionWithNodeInfoVO.getId().intValue());
            if (ret.success() && CollectionUtils.isNotEmpty(ret.getData())) {
                List<ClusterRoleHost> data = ret.getData();
                List<String> nodeNameList = data.stream().filter(Objects::nonNull).map(ClusterRoleHost::getNodeSet).distinct().collect(Collectors.toList());
                String nodeNames = ListUtils.strList2String(nodeNameList);
                clusterRegionWithNodeInfoVO.setNodeNames(nodeNames);
            }
        }

        return Result.buildSucc(clusterRegionWithNodeInfoVOS.stream().filter(r -> !AriusObjUtils.isBlank(r.getName())).distinct().collect(Collectors.toList()));
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
     * 对于逻辑集群绑定的物理集群的版本进行一致性校验
     *
     * @param param 逻辑集群Region
     * @return
     */
    private Result<Void> boundPhyClusterVersionsCheck(ESLogicClusterWithRegionDTO param) {
        Set<String> boundPhyClusterVersions = Sets.newHashSet();
        for (ClusterRegionDTO clusterRegionDTO : param.getClusterRegionDTOS()) {
            ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterRegionDTO.getPhyClusterName());
            if (clusterPhy == null) {
                return Result.buildFail("region对应的物理集群信息为空");
            }

            if (clusterPhy.getEsVersion() == null) {
                return Result.buildFail("region对应的物理集群信息对应的版本号不不存在");
            }
            boundPhyClusterVersions.add(clusterPhy.getEsVersion());
        }

        if (boundPhyClusterVersions.size() != 1) {
            return Result.buildFail("逻辑集群绑定的物理集群的版本号应该一致");
        }

        return Result.buildSucc();
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

    private LogicClusterRackVO buildLogicClusterRackVO(ClusterLogicRackInfo clusterLogicRackInfo) {
        if (clusterLogicRackInfo == null) {
            return null;
        }

        LogicClusterRackVO logicClusterRackVO = new LogicClusterRackVO();
        logicClusterRackVO.setResourceIds(clusterLogicRackInfo.getLogicClusterIds());
        logicClusterRackVO.setResourceId(genResourceIdFromLogicClusterIds(clusterLogicRackInfo.getLogicClusterIds()));
        logicClusterRackVO.setCluster(clusterLogicRackInfo.getPhyClusterName());
        logicClusterRackVO.setRack(clusterLogicRackInfo.getRack());
        return logicClusterRackVO;
    }

    /**
     * 为了兼容调用该视图的应用，所以这里resourceId字段取rack关联的逻辑集群id列表的首位
     * @param logicClusters 逻辑集群id列表
     * @return 关联的首位逻辑集群id
     */
    private Long genResourceIdFromLogicClusterIds(String logicClusters) {
        List<Long> logicClusterIds = ListUtils.string2LongList(logicClusters);
        if (!CollectionUtils.isEmpty(logicClusterIds)) {
            return logicClusterIds.get(0);
        }

        return null;
    }

    /**
     * 校验region是否可以被逻辑集群绑定
     * @param clusterLogicId         逻辑集群Id
     * @param clusterRegionDTO       region信息
     * @param clusterLogicType       逻辑集群类型
     */
    private void checkCanBeBound(Long clusterLogicId, ClusterRegionDTO clusterRegionDTO, Integer clusterLogicType) {
        Result<Boolean> validResult = clusterContextManager.canClusterLogicAssociatedPhyCluster(clusterLogicId,
            clusterRegionDTO.getPhyClusterName(), clusterRegionDTO.getId(), clusterLogicType);
        if (validResult.failed()) {
            throw new AriusRunTimeException(validResult.getMessage(), FAIL);
        }
    }

    private Result<Void> doBindRegionToClusterLogic(ESLogicClusterWithRegionDTO param, String operator) {
        List<ClusterRegionDTO> clusterRegionDTOS = param.getClusterRegionDTOS();
        if (CollectionUtils.isEmpty(clusterRegionDTOS)) {
            return Result.buildParamIllegal("region相关参数非法");
        }

        for (ClusterRegionDTO clusterRegionDTO : clusterRegionDTOS) {
            Result<Void> bindRegionResult = clusterRegionService.bindRegion(clusterRegionDTO.getId(), param.getId(), null,
                    operator);
            if (bindRegionResult.failed()) {
                throw new AriusRunTimeException(bindRegionResult.getMessage(), FAIL);
            }
        }

        return Result.buildSucc();
    }

    /**
     * 1. 逻辑集群无关联物理集群, 直接清理
     * 2. (共享类型)逻辑集群已关联物理集群, 新关联的物理集群添加逻辑集群已有索引服务
     * @param param             region实体
     * @param operator          操作者
     * @return
     */
    private void initTemplateSrvOfClusterPhy(ESLogicClusterWithRegionDTO param, String operator) {
        
        ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContext(param.getId());
        if (null == clusterLogicContext) {
            LOGGER.error(
                "class=ClusterRegionManagerImpl||method=initTemplateSrvOfClusterPhy||clusterLogicId={}||errMsg=clusterLogicContext is empty",
                param.getId());
            return;
        }
        
        List<ClusterRegionDTO> clusterRegionDTOS = param.getClusterRegionDTOS();
        List<String> associatedClusterPhyNames = clusterLogicContext.getAssociatedClusterPhyNames();
        if (CollectionUtils.isEmpty(associatedClusterPhyNames)) {
            clearTemplateSrvOfClusterPhy(param.getId(), associatedClusterPhyNames, clusterRegionDTOS, operator);
        } else {
            addTemplateSrvToNewClusterPhy(param.getId(), associatedClusterPhyNames, clusterRegionDTOS, operator);
        }
    }

    /**
     * (共享类型)逻辑集群已关联物理集群, 新关联的物理集群默开启逻辑集群已有索引服务
     * @param clusterLogicId               逻辑集群ID
     * @param associatedClusterPhyNames    已关联物理集群名称
     * @param clusterRegionDTOS            region信息
     * @param operator                     操作者
     */
    private void addTemplateSrvToNewClusterPhy(Long clusterLogicId, List<String> associatedClusterPhyNames,
                                               List<ClusterRegionDTO> clusterRegionDTOS, String operator) {
        //获取已有逻辑集群索引服务
        List<Integer> clusterTemplateSrvIdList = templateSrvManager.getPhyClusterTemplateSrvIds(associatedClusterPhyNames.get(0));

        //更新已有新绑定物理集群中的索引服务
        for (ClusterRegionDTO clusterRegionDTO : clusterRegionDTOS) {
            if (associatedClusterPhyNames.contains(clusterRegionDTO.getPhyClusterName())) {
                continue;
            }

            try {
                String phyClusterName = clusterRegionDTO.getPhyClusterName();
                templateSrvManager.replaceTemplateServes(phyClusterName, clusterTemplateSrvIdList, operator);
            } catch (Exception e) {
                LOGGER.error(
                    "class=ClusterRegionManagerImpl||method=addTemplateSrvToNewClusterPhy||clusterLogicId={}||clusterPhy={}||errMsg={}",
                    clusterLogicId, clusterRegionDTO.getPhyClusterName(), e.getMessage());
            }
        }
    }

    /**
     * 逻辑集群无关联物理集群, 清理绑定物理集群索引服务
     * @param clusterLogicId               逻辑集群Id
     * @param associatedClusterPhyNames    已关联物理集群名称
     * @param clusterRegionDTOS             region信息
     * @param operator                      操作者
     */
    private void clearTemplateSrvOfClusterPhy(Long clusterLogicId, List<String> associatedClusterPhyNames,
                                              List<ClusterRegionDTO> clusterRegionDTOS, String operator) {
        for (ClusterRegionDTO clusterRegionDTO : clusterRegionDTOS) {
            if (associatedClusterPhyNames.contains(clusterRegionDTO.getPhyClusterName())) {
                continue;
            }
            
            try {
                templateSrvManager.delAllTemplateSrvByClusterPhy(clusterRegionDTO.getPhyClusterName(), operator);
            } catch (Exception e) {
                LOGGER.error(
                        "class=ClusterRegionManagerImpl||method=clearTemplateSrvOfClusterPhy||clusterLogicId={}||errMsg={}",
                        clusterLogicId, e.getMessage());
            }
        }
    }

    /**
     * 判断当前的region可否被指定类型的逻辑集群继续绑定
     * @param clusterRegion 集群region
     * @param clusterLogicType 逻辑集群类型
     * @param clusterLogicId 逻辑集群id
     * @return 校验结果
     */
    private boolean canBindRegionToLogicCluster(ClusterRegion clusterRegion, Integer clusterLogicType, Long clusterLogicId) {
        List<Long> logicClusterIds = ListUtils.string2LongList(clusterRegion.getLogicClusterIds());
        // 获取region绑定的逻辑集群类型
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(logicClusterIds.get(0));

        // region没有被任何的逻辑集群绑定，则该region可以被绑定
        if (AriusObjUtils.isNull(clusterLogic)) {
            return true;
        }

        // 当region有被逻辑集群绑定时，如需被指定逻辑集群绑定需要满足：region已绑定的逻辑集群类型为共享且指定的逻辑集群类型为共享
        // 当满足上述条件之后，分为两种情况讨论：新建共享逻辑集群可以绑定该region;已建立逻辑集群需要过滤掉本来已经绑定了的region模块
        return ResourceLogicTypeEnum.valueOf(clusterLogicType).equals(ResourceLogicTypeEnum.PUBLIC)
                && clusterLogic.getType().equals(ResourceLogicTypeEnum.PUBLIC.getCode())
                && (AriusObjUtils.isNull(clusterLogicId) || !logicClusterIds.contains(clusterLogicId));
    }
}
