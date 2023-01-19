package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhyContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusScheduleThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.knowframework.security.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 集群上下文类, 包含以下信息:
 * 1. 包括逻辑集群（共享、独享、独占）关联的物理集群信息（region、node、project消息等）
 * 2. 物理集群信息关联逻辑集群（共享、独享、独占）信息
 * 3. 校验模型 ————> 获取逻辑集群可绑定的物理集群列表
 *
 * Created by linyunan on 2021-06-08
 */
@Component
public class ClusterContextManagerImpl implements ClusterContextManager {
    private static final ILog                    LOGGER                          = LogFactory
        .getLog(ClusterContextManagerImpl.class);

    /**
     * key-> 逻辑集群Id
     */
    private final Map<Long, ClusterLogicContext> id2ClusterLogicContextMap       = Maps.newConcurrentMap();

    /**
     * key-> 物理集群名称, value 上下文信息
     */
    private final Map<String, ClusterPhyContext> name2ClusterPhyContextMap       = Maps.newConcurrentMap();

    private static final Integer                 LOGIC_ASSOCIATED_PHY_MAX_NUMBER = 2 << 9;

    private static final Integer                 PHY_ASSOCIATED_LOGIC_MAX_NUMBER = 2 << 9;

    @Autowired
    private ClusterLogicService                  clusterLogicService;

    @Autowired
    private ClusterPhyService                    clusterPhyService;

    @Autowired
    private ClusterRegionService                 clusterRegionService;

    @Autowired
    private ClusterRoleHostService               clusterRoleHostService;

    @Autowired
    private AriusScheduleThreadPool              ariusScheduleThreadPool;

    @Autowired
    private ProjectService                       projectService;

    @PostConstruct
    private void init() {
        ariusScheduleThreadPool.submitScheduleAtFixedDelayTask(this::flushClusterLogicContexts, 60, 120);
        ariusScheduleThreadPool.submitScheduleAtFixedDelayTask(this::flushClusterPhyContexts, 120, 120);
    }

    @Override
    public ClusterPhyContext flushClusterPhyContext(String clusterPhyName) {
        try {
            ClusterPhyContext clusterPhyContext = getClusterPhyContext(clusterPhyName);
            if (null != clusterPhyContext) {
                name2ClusterPhyContextMap.put(clusterPhyContext.getClusterName(), clusterPhyContext);
                return clusterPhyContext;
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            LOGGER.error("class=ClusterContextManagerImpl||method=flushClusterPhyContext||clusterPhyName={}||errMsg={}",
                clusterPhyName, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public ClusterLogicContext flushClusterLogicContext(Long clusterLogicId) {
        try {
            ClusterLogicContext clusterLogicContext = getClusterLogicContext(clusterLogicId);
            if (null != clusterLogicContext) {
                id2ClusterLogicContextMap.put(clusterLogicContext.getClusterLogicId(), clusterLogicContext);
                return clusterLogicContext;
            }

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            LOGGER.error(
                "class=ClusterContextManagerImpl||method=flushClusterLogicContext||clusterLogicId={}||errMsg={}",
                clusterLogicId, e.getMessage(), e);

        }

        return null;
    }

    @Override
    public void flushClusterContextByClusterRegion(ClusterRegion clusterRegion) {
        if (null == clusterRegion) {
            return;
        }

        flushClusterPhyContext(clusterRegion.getPhyClusterName());

        // 一个物理集群可以关联多个逻辑集群
        List<Long> logicClusterIds = ListUtils.string2LongList(clusterRegion.getLogicClusterIds());
        if (!CollectionUtils.isEmpty(logicClusterIds)) {
            logicClusterIds.forEach(this::flushClusterLogicContext);
        }
    }

    @Override
    public Result<Boolean> canClusterLogicAssociatedPhyCluster(Long clusterLogicId, String clusterPhyName,
                                                               Long regionId, Integer clusterLogicType) {
        //新建时clusterLogicId为空, 防止NPE
        if (AriusObjUtils.isNull(clusterLogicId)) {
            clusterLogicId = -1L;
        }

        ClusterLogicContext clusterLogicContext = getClusterLogicContext(clusterLogicId);
        ClusterPhyContext clusterPhyContext = getClusterPhyContext(clusterPhyName);

        int associatedPhyNum = 0;
        int associatedLogicNum = 0;
        if (null != clusterLogicContext) {
            associatedPhyNum = clusterLogicContext.getAssociatedPhyNum();
        }
        if (null != clusterPhyContext) {
            associatedLogicNum = clusterPhyContext.getAssociatedLogicNum();
        }

        return doValid(associatedPhyNum, associatedLogicNum, clusterLogicId, clusterPhyName, regionId,
            clusterLogicType);
    }

    /**
     *   1. Type为独立, LP = 1, PL = 1
     *   2. Type为共享, LP = n, PL = 1
     * 	 3. Type为独享, LP = n, PL = 1
     */
    @Override
    public Result<List<String>> getCanBeAssociatedClustersPhys(Integer clusterLogicType, Long clusterLogicId) {
        if (!ClusterResourceTypeEnum.isExist(clusterLogicType)) {
            return Result.buildParamIllegal("逻辑集群类型非法");
        }

        List<String> canBeAssociatedClustersPhyNames = Lists.newArrayList();

        if (PRIVATE.getCode() == clusterLogicType) {
            handleClusterLogicTypePrivate(clusterLogicId, canBeAssociatedClustersPhyNames);
        }

        if (PUBLIC.getCode() == clusterLogicType) {
            handleClusterLogicTypePublic(clusterLogicId, canBeAssociatedClustersPhyNames);
        }

        if (EXCLUSIVE.getCode() == clusterLogicType) {
            handleClusterLogicTypeExclusive(clusterLogicId, canBeAssociatedClustersPhyNames);
        }

        return Result.buildSucc(canBeAssociatedClustersPhyNames);
    }

    @Override
    public List<String> getClusterPhyAssociatedClusterLogicNames(String clusterPhyName) {
        ClusterPhyContext clusterPhyContext = getClusterPhyContext(clusterPhyName);
        if (null == clusterPhyContext) {
            return Lists.newArrayList();
        }

        List<Long> clusterLogicIds = clusterPhyContext.getAssociatedClusterLogicIds();
        if (CollectionUtils.isEmpty(clusterLogicIds)) {
            return Lists.newArrayList();
        }

        return clusterLogicIds.stream().map(r -> clusterLogicService.getClusterLogicByIdThatNotContainsProjectId(r ))
                .map(ClusterLogic::getName)
            .distinct().collect(Collectors.toList());
    }

    @Override
    public ClusterPhyContext getClusterPhyContext(String clusterPhyName) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
        if (null == clusterPhy) {
            LOGGER.error(
                "class=ClusterContextManagerImpl||method=flushClusterPhyContext||clusterPhyName={}||msg=clusterPhy is empty",
                clusterPhyName);
            return null;
        }

        ClusterPhyContext build = ClusterPhyContext.builder().clusterPhyId(clusterPhy.getId().longValue())
            .clusterName(clusterPhy.getCluster()).associatedLogicNumMax(PHY_ASSOCIATED_LOGIC_MAX_NUMBER).build();

        setClusterPhyNodeInfo(build);
        setRegionAndClusterLogicInfoAndProjectId(build);
        return build;
    }

    @Override
    public ClusterPhyContext getClusterPhyContextCache(String cluster) {
        return name2ClusterPhyContextMap.get(cluster);
    }

    @Override
    public Map<String, ClusterPhyContext> listClusterPhyContextMap() {
        return name2ClusterPhyContextMap;
    }

    @Override
    public ClusterLogicContext getClusterLogicContextCache(Long clusterLogicId) {
        return id2ClusterLogicContextMap.get(clusterLogicId);
    }

    @Override
    public ClusterLogicContext getClusterLogicContext(Long clusterLogicId) {
        ClusterLogic clusterLogic =
                clusterLogicService.listClusterLogicByIdThatProjectIdStrConvertProjectIdList(clusterLogicId).stream().findFirst().orElse(null);
        if (null == clusterLogic) {
            LOGGER.error(
                "class=ClusterContextManagerImpl||method=flushClusterLogicContext||clusterLogicId={}||msg=clusterLogic is empty",
                clusterLogicId);
            return null;
        }

        ClusterLogicContext build = buildInitESClusterLogicContextByType(clusterLogic);
        setAssociatedClusterPhyInfo(build);
        setRegionAndAssociatedClusterPhyDataNodeInfo(build);
        return build;
    }

    /***********************************************private*********************************************/
    public void flushClusterPhyContexts() {
        LOGGER.info("class=ClusterContextManagerImpl||method=flushClusterPhyContexts||msg=start...");
        long currentTimeMillis = System.currentTimeMillis();
        List<ClusterPhy> clusterPhyList = clusterPhyService.listAllClusters();
        if (CollectionUtils.isEmpty(clusterPhyList)) {
            LOGGER.info(
                "class=ClusterContextManagerImpl||method=flushClusterLogicContexts||msg=finish...||consumingTime={}",
                System.currentTimeMillis() - currentTimeMillis);
            return;
        }

        // regionk信息按【cluster】分组
        List<ClusterRegion> clusterRegionList = clusterRegionService.listAllBoundRegions();
        Map<String/*phyClusterName*/, List<ClusterRegion>> phyClusterName2ClusterLogicRackListMap = ConvertUtil
            .list2MapOfList(clusterRegionList, ClusterRegion::getPhyClusterName, clusterRegion -> clusterRegion);

        // clusterLogic信息按主键分组
        List<ClusterLogic> clusterLogicList = clusterLogicService.listAllClusterLogics();
        Map<Long, ClusterLogic> id2ClusterLogicMap = ConvertUtil.list2Map(clusterLogicList, ClusterLogic::getId);

        // host信息按【cluster】分组
        List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.listAllNode();
        Map<String, List<ClusterRoleHost>> cluster2RoleListMap = ConvertUtil.list2MapOfList(clusterRoleHosts,
            ClusterRoleHost::getCluster, clusterRoleHost -> clusterRoleHost);

        // project信息分组
        final List<ProjectBriefVO> briefVOS = projectService.getProjectBriefList();
        Map<Integer/*projectId*/, String/*projectName*/> projectId2ProjectNameMap = ConvertUtil.list2Map(briefVOS,
            ProjectBriefVO::getId, ProjectBriefVO::getProjectName);

        for (ClusterPhy phy : clusterPhyList) {
            // 初始化
            ClusterPhyContext clusterPhyContext = ClusterPhyContext.builder().clusterPhyId(phy.getId().longValue())
                .clusterName(phy.getCluster()).associatedLogicNumMax(PHY_ASSOCIATED_LOGIC_MAX_NUMBER).build();

            List<ClusterRoleHost> hostList = cluster2RoleListMap.get(phy.getCluster());
            if (CollectionUtils.isEmpty(hostList)) {
                name2ClusterPhyContextMap.put(phy.getCluster(), clusterPhyContext);
                continue;
            }

            // 设置物理集群管理的host信息, 这里暂时不去区分单机器多es实例的场景
            List<ClusterRoleHost> dataNodes = hostList.stream()
                .filter(r -> Objects.nonNull(r) && DATA_NODE.getCode() == r.getRole()).collect(Collectors.toList());

            clusterPhyContext.setAssociatedDataNodeNum(dataNodes.size());
            clusterPhyContext
                .setAssociatedDataNodeIps(dataNodes.stream().map(ClusterRoleHost::getIp).collect(Collectors.toList()));
            clusterPhyContext
                .setAssociatedNodeIps(hostList.stream().map(ClusterRoleHost::getIp).collect(Collectors.toList()));

            // 设置region信息
            List<ClusterRegion> clusterRegions = phyClusterName2ClusterLogicRackListMap.get(phy.getCluster());
            if (CollectionUtils.isEmpty(clusterRegions)) {
                name2ClusterPhyContextMap.put(phy.getCluster(), clusterPhyContext);
                continue;
            }

            clusterPhyContext
                .setAssociatedRegionIds(clusterRegions.stream().map(ClusterRegion::getId).collect(Collectors.toList()));

            // 设置关联逻辑集群信息
            List<String> associatedClusterLogicIdsStr = clusterRegions.stream()
                .filter(r -> Objects.nonNull(r)
                             && !AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID.equals(r.getLogicClusterIds()))
                .map(ClusterRegion::getLogicClusterIds).distinct().collect(Collectors.toList());
            Set<Long> associatedClusterLogicIds = Sets.newHashSet();
            for (String associatedClusterLogicIdStr : associatedClusterLogicIdsStr) {
                associatedClusterLogicIds.addAll(ListUtils.string2LongList(associatedClusterLogicIdStr));
            }
            clusterPhyContext.setAssociatedClusterLogicIds(Lists.newArrayList(associatedClusterLogicIds));
            clusterPhyContext.setAssociatedLogicNum(associatedClusterLogicIds.size());

            // 设置project信息
            Set<Integer> projectIdSet = Sets.newHashSet();
            Set<String> projectNameSet = Sets.newHashSet();
            for (Long associatedClusterLogicId : associatedClusterLogicIds) {
                ClusterLogic clusterLogic = id2ClusterLogicMap.get(associatedClusterLogicId);
                if (null == clusterLogic) {
                    continue;
                }
                projectIdSet.add(clusterLogic.getProjectId());

                String projectName = projectId2ProjectNameMap.get(clusterLogic.getProjectId());
                if (AriusObjUtils.isBlack(projectName)) {
                    continue;
                }
                projectNameSet.add(projectName);
            }
            clusterPhyContext.setAssociatedProjectIds(Lists.newArrayList(projectIdSet));
            clusterPhyContext.setAssociatedProjectNames(Lists.newArrayList(projectNameSet));

            name2ClusterPhyContextMap.put(phy.getCluster(), clusterPhyContext);
        }
        LOGGER.info("class=ClusterContextManagerImpl||method=flushClusterPhyContexts||msg=finish...||consumingTime={}",
            System.currentTimeMillis() - currentTimeMillis);
    }

    /**
     * 刷新逻辑集群上下文，其中包括 关联的物理集群信息、 region信息、 host信息等
     */
    public void flushClusterLogicContexts() {
        LOGGER.info("class=ClusterContextManagerImpl||method=flushClusterLogicContexts||msg=start...");
        long currentTimeMillis = System.currentTimeMillis();

        List<ClusterLogic> clusterLogics = clusterLogicService.listAllClusterLogics();
        if (CollectionUtils.isEmpty(clusterLogics)) {
            LOGGER.info(
                "class=ClusterContextManagerImpl||method=flushClusterLogicContexts||msg=finish...||consumingTime={}",
                System.currentTimeMillis() - currentTimeMillis);
            return;
        }

        // 获取全量逻辑集群绑定的Region信息
        Map<Long, List<ClusterRegion>> clusterLogicId2ClusterLogicRackListMap = getClusterLogicId2ClusterRegionListMap();

        // host信息按【regionId】分组
        Map<Integer, List<ClusterRoleHost>> regionId2HostListMap = getRegionId2HostListMap();

        for (ClusterLogic clusterLogic : clusterLogics) {
            // 构建初始化上下文, 按照逻辑集群类型限制上下文信息数量
            ClusterLogicContext clusterLogicContext = buildInitESClusterLogicContextByType(clusterLogic);

            // 设置关联集群信息
            List<ClusterRegion> clusterRegions = clusterLogicId2ClusterLogicRackListMap.get(clusterLogic.getId());
            if (CollectionUtils.isEmpty(clusterRegions)) {
                id2ClusterLogicContextMap.put(clusterLogic.getId(), clusterLogicContext);
                continue;
            }

            List<String> associatedClusterPhyNameList = clusterRegions.stream().map(ClusterRegion::getPhyClusterName)
                .distinct().collect(Collectors.toList());

            clusterLogicContext.setAssociatedClusterPhyNames(associatedClusterPhyNameList);
            clusterLogicContext.setAssociatedPhyNum(associatedClusterPhyNameList.size());
            // 遇到异常数据，先简单去打印错误日志
            if (clusterLogicContext.getAssociatedPhyNumMax() < associatedClusterPhyNameList.size()) {
                LOGGER.error("class=ClusterContextManagerImpl||method=flushClusterLogicContexts"
                             + "||logicClusterType={}||esClusterLogicId={}||msg=集群间关联超过最大限制数{}, 请纠正",
                    clusterLogicContext.getLogicClusterType(), clusterLogicContext.getClusterLogicId(),
                    clusterLogicContext.getAssociatedPhyNumMax());
            }

            // 获取逻辑集群已关联的Region信息
            clusterLogicContext
                .setAssociatedRegionIds(clusterRegions.stream().map(ClusterRegion::getId).collect(Collectors.toList()));

            // 获取逻辑集群关联region下的rack节点信息
            List<ClusterRoleHost> associatedRegionDataNodeList = Lists.newArrayList();
            for (ClusterRegion clusterRegion : clusterRegions) {
                List<ClusterRoleHost> dataNodeList = regionId2HostListMap.get(clusterRegion.getId().intValue());
                if (CollectionUtils.isEmpty(dataNodeList)) {
                    continue;
                }

                associatedRegionDataNodeList.addAll(dataNodeList);
            }

            //设置数据节点总数
            clusterLogicContext.setAssociatedDataNodeNum(associatedRegionDataNodeList.size());

            //设置数据节点Ip地址
            clusterLogicContext.setAssociatedDataNodeIps(
                associatedRegionDataNodeList.stream().map(ClusterRoleHost::getIp).collect(Collectors.toList()));

            id2ClusterLogicContextMap.put(clusterLogic.getId(), clusterLogicContext);
        }

        LOGGER.info(
            "class=ClusterContextManagerImpl||method=flushClusterLogicContexts||msg=finish...||consumingTime={}",
            System.currentTimeMillis() - currentTimeMillis);
    }

    /**
     * 获取全量逻辑集群绑定的Region信息
     * @return  key -> clusterLogicId   value -> List<ClusterRegion>
     */
    @NotNull
    private Map<Long, List<ClusterRegion>> getClusterLogicId2ClusterRegionListMap() {
        List<ClusterRegion> clusterRegionList = clusterRegionService.listAllBoundRegions();
        Map<String/*clusterLogicIds 逗号分隔*/, List<ClusterRegion>> clusterLogicIds2ClusterRegionListMap = ConvertUtil
            .list2MapOfList(clusterRegionList, ClusterRegion::getLogicClusterIds, region -> region);

        // 按【逻辑集群Id】分组, 特殊处理table中clusterLogicIds列的数据
        Map<Long/*clusterLogicId*/, List<ClusterRegion>> clusterLogicId2ClusterLogicRackListMap = Maps.newHashMap();
        for (Map.Entry<String, List<ClusterRegion>> e : clusterLogicIds2ClusterRegionListMap.entrySet()) {
            String key = e.getKey();
            List<ClusterRegion> clusterRegions = e.getValue();
            List<Long> clusterLogicIdList = ListUtils.string2LongList(key);
            for (Long clusterLogicId : clusterLogicIdList) {
                clusterLogicId2ClusterLogicRackListMap.put(clusterLogicId, clusterRegions);
            }
        }
        return clusterLogicId2ClusterLogicRackListMap;
    }

    /**
     * host信息按【regionId】分组
     * @return key -> regionId  value -> List<RoleClusterHost>
     */
    @NotNull
    private Map<Integer, List<ClusterRoleHost>> getRegionId2HostListMap() {
        List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.listAllNodeByRole(DATA_NODE.getCode());
        if (CollectionUtils.isEmpty(clusterRoleHosts)) {
            return Maps.newHashMap();
        }
        return ConvertUtil.list2MapOfList(clusterRoleHosts, ClusterRoleHost::getRegionId, node -> node);
    }

    /**
     *   定义规则:
     *   1. Type为独立, LP = 1, PL = 1
     *   2. Type为共享, LP = n, PL = 1 ,  1 <= n <= 1024   多个逻辑集群共享一个物理集群, 每个逻辑集群关联物理集群一部分region,不可跨其他物理集群，
     *                                                    多个逻辑集群可关联同一部分region。
     *   3. Type为独占, LP = 1, PL = n ,  1 <= n <= 1024   一个逻辑集群独占一个或者多个物理集群
     */
    private ClusterLogicContext buildInitESClusterLogicContextByType(ClusterLogic clusterLogic) {

        if (PRIVATE.getCode() == clusterLogic.getType() || EXCLUSIVE.getCode() == clusterLogic.getType()) {
            return ClusterLogicContext.builder().clusterLogicName(clusterLogic.getName())
                .clusterLogicId(clusterLogic.getId()).logicClusterType(clusterLogic.getType()).associatedPhyNumMax(1)
                .build();
        } else if (PUBLIC.getCode() == clusterLogic.getType()) {
            return ClusterLogicContext.builder().clusterLogicName(clusterLogic.getName())
                .clusterLogicId(clusterLogic.getId()).logicClusterType(clusterLogic.getType())
                .associatedPhyNumMax(LOGIC_ASSOCIATED_PHY_MAX_NUMBER).build();
        } else {
            LOGGER.error(
                "class=ClusterContextManagerImpl||method=buildInitESClusterLogicContextByType||esClusterLogicId={}||msg={}",
                clusterLogic.getId(), String.format("请确认逻辑集群%s类型是否存在", clusterLogic.getType()));

            return ClusterLogicContext.builder().clusterLogicName(clusterLogic.getName())
                .clusterLogicId(clusterLogic.getId()).logicClusterType(clusterLogic.getType()).associatedPhyNumMax(-1)
                .build();
        }
    }

    private void setAssociatedClusterPhyInfo(ClusterLogicContext build) {
        List<String> clusterPhyNames = clusterRegionService.listPhysicClusterNames(build.getClusterLogicId());
        if (build.getAssociatedPhyNumMax() < clusterPhyNames.size()) {
            LOGGER.error("class=ClusterContextManagerImpl||method=setAssociatedClusterPhyInfo"
                         + "||logicClusterType={}||esClusterLogicId={}||msg=集群间关联超过最大限制数{}, 请纠正",
                build.getLogicClusterType(), build.getClusterLogicId(), build.getAssociatedPhyNumMax());
            return;
        }

        build.setAssociatedClusterPhyNames(clusterPhyNames);
        build.setAssociatedPhyNum(clusterPhyNames.size());
    }

    private void setRegionAndAssociatedClusterPhyDataNodeInfo(ClusterLogicContext build) {
        //获取逻辑集群已关联的Region信息
        List<ClusterRegion> regions = clusterRegionService.getClusterRegionsByLogicIds(Collections.singletonList(build.getClusterLogicId()));
        build.setAssociatedRegionIds(regions.stream().map(ClusterRegion::getId).collect(Collectors.toList()));

        //获取逻辑集群关联region下的rack节点信息
        List<ClusterRoleHost> associatedRackClusterHosts = Lists.newArrayList();
        for (ClusterRegion region : regions) {
            Result<List<ClusterRoleHost>> regionDataNodeListRet = clusterRoleHostService
                .listByRegionId(region.getId().intValue());
            if (regionDataNodeListRet.failed()) {
                LOGGER.warn(
                    "class=ClusterContextManagerImpl||method=setRegionAndAssociatedClusterPhyDataNodeInfo||regionId={}||msg=failed to get regionDataNodeList:{}",
                    region.getId(), regionDataNodeListRet.getMessage());
                continue;
            }
            associatedRackClusterHosts.addAll(regionDataNodeListRet.getData());
        }

        //设置数据节点总数
        build.setAssociatedDataNodeNum(associatedRackClusterHosts.size());

        //设置数据节点Ip地址
        build.setAssociatedDataNodeIps(
            associatedRackClusterHosts.stream().map(ClusterRoleHost::getIp).collect(Collectors.toList()));
    }

    private void setRegionAndClusterLogicInfoAndProjectId(ClusterPhyContext build) {
        // 1. set region
        List<ClusterRegion> regions = clusterRegionService.listPhyClusterRegions(build.getClusterName());
        build.setAssociatedRegionIds(regions.stream().map(ClusterRegion::getId).collect(Collectors.toList()));

        // 2. set ClusterLogicInfo
        Set<Long> associatedClusterLogicIds = Sets.newHashSet();
        for (ClusterRegion clusterRegion : regions) {
            // 添加每一个物理集群下每一个region所被绑定的逻辑集群
            List<Long> logicClusterIds = ListUtils.string2LongList(clusterRegion.getLogicClusterIds());
            if (!CollectionUtils.isEmpty(logicClusterIds)
                && !logicClusterIds.contains(Long.parseLong(AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID))) {
                associatedClusterLogicIds.addAll(logicClusterIds);
            }
        }

        build.setAssociatedClusterLogicIds(Lists.newArrayList(associatedClusterLogicIds));
        build.setAssociatedLogicNum(associatedClusterLogicIds.size());

        // 3. set projectId
        Set<Integer> projectIdSet = new HashSet<>();
        Set<String> clusterLogicSet = new HashSet<>();
        if (!CollectionUtils.isEmpty(associatedClusterLogicIds)) {
            for (Long associatedClusterLogicId : associatedClusterLogicIds) {
                clusterLogicService.listClusterLogicByIdThatProjectIdStrConvertProjectIdList(associatedClusterLogicId).stream().filter(Objects::nonNull)
                        .filter(clusterLogic -> null != clusterLogic.getProjectId() && null != clusterLogic.getName())
                        .forEach(clusterLogic -> {
                            projectIdSet.add(clusterLogic.getProjectId());
                            clusterLogicSet.add(clusterLogic.getName());
                        });
                
            }
        }

        build.setAssociatedProjectIds(Lists.newArrayList(projectIdSet));
        build.setAssociatedProjectNames(Lists.newArrayList(clusterLogicSet));
    }

    private void setClusterPhyNodeInfo(ClusterPhyContext build) {
        List<ClusterRoleHost> nodes = clusterRoleHostService.getNodesByCluster(build.getClusterName());
        List<ClusterRoleHost> dataNodes = nodes.stream().filter(r -> DATA_NODE.getCode() == r.getRole())
            .collect(Collectors.toList());

        build.setAssociatedDataNodeNum(dataNodes.size());
        build.setAssociatedDataNodeIps(dataNodes.stream().map(ClusterRoleHost::getIp).collect(Collectors.toList()));
        build.setAssociatedNodeIps(nodes.stream().map(ClusterRoleHost::getIp).collect(Collectors.toList()));
    }

    /**
     * 具体校验逻辑
     * @param associatedPhyNumber    逻辑集群关联物理集群个数
     * @param associatedLogicNumber  物理集群关联逻辑集群个数
     * @param regionId               需要绑定的regionId
     * @param clusterLogicType       逻辑集群类型
     */
    private Result<Boolean> doValid(int associatedPhyNumber, int associatedLogicNumber, Long clusterLogicId,
                                    String clusterPhyName, Long regionId, Integer clusterLogicType) {

        if (AriusObjUtils.isNull(clusterLogicType)) {
            return Result.buildParamIllegal("逻辑集群类型为空");
        }

        if (UNKNOWN.getCode() == ClusterResourceTypeEnum.valueOf(clusterLogicType).getCode()) {
            return Result.buildParamIllegal("无法识别逻辑集群类型");
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildFail("物理集群不存在");
        }

        if (PRIVATE.getCode() == clusterLogicType
            && !canClusterLogicBoundRegion(regionId, clusterPhyName, clusterLogicId)) {
            //先判断logic -> phy, 二次关联region需要先校验逻辑集群对应的物理集群数据是否合理
            if (associatedPhyNumber > 0) {
                return Result.buildParamIllegal(String.format("集群间关联失败 ,该独立逻辑集群%s已有关联物理集群", clusterLogicId));
            }
            //在判断phy -> logic
            if (associatedLogicNumber > 0) {
                return Result.buildFail(String.format("集群间关联失败, 物理集群%s已有关联逻辑集群", clusterPhyName));
            }
        }

        return Result.buildSucc(Boolean.TRUE);
    }

    /**
     * 判断指定的物理集群下的region是否被对应的逻辑集群绑定
     * @param regionId regionId
     * @param clusterPhyName 物理集群名称
     * @param clusterLogicId 逻辑集群id
     * @return  true of false
     */
    private boolean canClusterLogicBoundRegion(Long regionId, String clusterPhyName, Long clusterLogicId) {
        ClusterRegion region = clusterRegionService.getRegionById(regionId);
        ClusterPhyContext clusterPhyContext = getClusterPhyContext(clusterPhyName);
        List<Long> clusterLogicIds = clusterPhyContext.getAssociatedClusterLogicIds();
        if (CollectionUtils.isNotEmpty(clusterLogicIds) && !clusterLogicIds.contains(clusterLogicId)) {
            return false;
        }

        if (!region.getLogicClusterIds().equals(AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID)) {
            return false;
        }

        return region.getPhyClusterName().equals(clusterPhyName);
    }

    /**
     * LP = 1 , PL = 1
     * @param clusterLogicId    逻辑集群Id
     * @param clusterPhyContext 物理集群上下文
     * @return                  true/false
     */
    private boolean checkForPrivate(Long clusterLogicId, ClusterPhyContext clusterPhyContext) {
        //采集测associatedLogicNumber 会存在null问题
        if (null == clusterPhyContext.getAssociatedLogicNum()) {
            return true;
        }
        if (null == clusterLogicId) {
            if (0 == clusterPhyContext.getAssociatedLogicNum()) {
                return true;
            } else if (clusterPhyContext.getAssociatedLogicNum() >= 1) {
                return false;
            }

            return false;
        } else {
            ClusterLogicContext clusterLogicContext = getClusterLogicContext(clusterLogicId);
            if (0 == clusterLogicContext.getAssociatedPhyNum() && 0 == clusterPhyContext.getAssociatedLogicNum()) {
                return true;
            } else {
                return 1 == clusterLogicContext.getAssociatedPhyNum() && clusterLogicContext
                    .getAssociatedClusterPhyNames().contains(clusterPhyContext.getClusterName());
            }
        }
    }

    /**
     * 校验当前的物理集群可否被指定的共享类型的逻辑集群关联
     * @param clusterLogicId    逻辑集群Id
     * @param clusterPhyContext 物理集群上下文
     * @return                  true/false
     */
    private boolean checkForPublic(Long clusterLogicId, ClusterPhyContext clusterPhyContext) {
        //采集测associatedLogicNumber 会存在null问题
        if (null == clusterPhyContext.getAssociatedLogicNum()) {
            return true;
        }
        if (null == clusterLogicId) {
            if (0 == clusterPhyContext.getAssociatedLogicNum()) {
                return true;
            } else {
                return !hasClusterPhyContextAssociatedLogicTypeIsCode(clusterPhyContext, PRIVATE.getCode());
            }
        } else {
            ClusterLogicContext clusterLogicContext = id2ClusterLogicContextMap.get(clusterLogicId);
            //不允许一个逻辑集群绑定多个物理集群
            if (1 < clusterLogicContext.getAssociatedPhyNum()) {
                return false;
            }

            //逻辑集群绑定物理集群数为0 且 物理集群无逻辑集群绑定
            if (0 == clusterLogicContext.getAssociatedPhyNum() && 0 == clusterPhyContext.getAssociatedLogicNum()) {
                return true;
            } else {
                return clusterLogicContext.getAssociatedPhyNum() == 1
                       && hasBelongClusterLogicContextAssociatedClusterNames(clusterLogicContext,
                           clusterPhyContext.getClusterName())
                       || clusterPhyContext.getAssociatedLogicNum() > 0
                          && !hasClusterPhyContextAssociatedLogicTypeIsCode(clusterPhyContext, PRIVATE.getCode());
            }
        }
    }

    /**
     * 校验当前的物理集群可否被指定的独享类型的逻辑集群关联
     * @param clusterLogicId    逻辑集群Id
     * @param clusterPhyContext 物理集群上下文
     * @return                  true/false
     */
    private boolean checkForExclusive(Long clusterLogicId, ClusterPhyContext clusterPhyContext) {
        //采集测associatedLogicNumber 会存在null问题
        if (null == clusterPhyContext.getAssociatedLogicNum()) {
            return true;
        }
        if (null == clusterLogicId) {
            if (0 == clusterPhyContext.getAssociatedLogicNum()) {
                return true;
            } else {
                return !hasClusterPhyContextAssociatedLogicTypeIsCode(clusterPhyContext, PRIVATE.getCode());
            }
        } else {
            ClusterLogicContext clusterLogicContext = id2ClusterLogicContextMap.get(clusterLogicId);
            //不允许一个逻辑集群绑定多个物理集群
            if (1 < clusterLogicContext.getAssociatedPhyNum()) {
                return false;
            }

            //逻辑集群绑定物理集群数为0 且 物理集群无逻辑集群绑定
            if (0 == clusterLogicContext.getAssociatedPhyNum() && 0 == clusterPhyContext.getAssociatedLogicNum()) {
                return true;
            } else {
                return clusterLogicContext.getAssociatedPhyNum() == 1
                       && hasBelongClusterLogicContextAssociatedClusterNames(clusterLogicContext,
                           clusterPhyContext.getClusterName())
                       || clusterPhyContext.getAssociatedLogicNum() > 0
                          && !hasClusterPhyContextAssociatedLogicTypeIsCode(clusterPhyContext, PRIVATE.getCode());
            }
        }
    }

    /**
     * 物理集群上下文中的逻辑集群类型是否全为传入类型
     * @param clusterPhyContext  物理集群上下文
     * @param code               逻辑集群类型
     * @return                   true/false
     */
    private boolean hasClusterPhyContextAssociatedLogicTypeIsCode(ClusterPhyContext clusterPhyContext, int code) {
        Set<Integer> typeSet = clusterPhyContext.getAssociatedClusterLogicIds().stream()
            .map(this::getClusterLogicContext).map(ClusterLogicContext::getLogicClusterType)
            .collect(Collectors.toSet());

        return 1 == typeSet.size() && typeSet.contains(code);
    }

    /**
     * 逻辑集群上下文中是否包含此物理集群
     * @param clusterLogicContext  逻辑集群上下文
     * @param clusterName          物理集群名称
     * @return                     true/false
     */
    private boolean hasBelongClusterLogicContextAssociatedClusterNames(ClusterLogicContext clusterLogicContext,
                                                                       String clusterName) {
        return clusterLogicContext.getAssociatedClusterPhyNames().contains(clusterName);
    }

    private void handleClusterLogicTypeExclusive(Long clusterLogicId, List<String> canBeAssociatedClustersPhyNames) {
        if (hasClusterLogicContextMapEmpty()) {
            return;
        }
        if (hasClusterPhyContextMapEmpty()) {
            return;
        }

        for (ClusterPhyContext clusterPhyContext : name2ClusterPhyContextMap.values()) {
            if (checkForExclusive(clusterLogicId, clusterPhyContext)) {
                canBeAssociatedClustersPhyNames.add(clusterPhyContext.getClusterName());
            }
        }
    }

    private void handleClusterLogicTypePublic(Long clusterLogicId, List<String> canBeAssociatedClustersPhyNames) {
        if (hasClusterLogicContextMapEmpty()) {
            return;
        }
        if (hasClusterPhyContextMapEmpty()) {
            return;
        }

        for (ClusterPhyContext clusterPhyContext : name2ClusterPhyContextMap.values()) {
            if (checkForPublic(clusterLogicId, clusterPhyContext)) {
                canBeAssociatedClustersPhyNames.add(clusterPhyContext.getClusterName());
            }
        }
    }

    private void handleClusterLogicTypePrivate(Long clusterLogicId, List<String> canBeAssociatedClustersPhyNames) {
        if (hasClusterLogicContextMapEmpty()) {
            return;
        }
        if (hasClusterPhyContextMapEmpty()) {
            return;
        }

        for (ClusterPhyContext clusterPhyContext : name2ClusterPhyContextMap.values()) {
            if (checkForPrivate(clusterLogicId, clusterPhyContext)) {
                canBeAssociatedClustersPhyNames.add(clusterPhyContext.getClusterName());
            }
        }
    }

    private boolean hasClusterPhyContextMapEmpty() {
        return name2ClusterPhyContextMap.isEmpty();
    }

    private boolean hasClusterLogicContextMapEmpty() {
        return id2ClusterLogicContextMap.isEmpty();
    }
}