package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhyContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 集群上下文类, 包含以下信息:
 * 1. 包括逻辑集群（共享、独享、独占）关联的物理集群信息（region、rack、node等）
 * 2. 物理集群信息关联逻辑集群（共享、独享、独占）信息
 * 3. 校验模型 ————> 获取逻辑集群可绑定的物理集群列表
 *
 * Created by linyunan on 2021-06-08
 */
@Service
public class ClusterContextManagerImpl implements ClusterContextManager {
    private static final ILog              LOGGER                           = LogFactory
        .getLog(ClusterContextManagerImpl.class);

    /**
     * key-> 逻辑集群Id
     */
    private Map<Long, ClusterLogicContext> id2ClusterLogicContextMap        = Maps.newConcurrentMap();

    /**
     * key-> 物理集群名称
     */
    private Map<String, ClusterPhyContext> name2ClusterPhyContextMap        = Maps.newConcurrentMap();

    private static final Integer           LOGIC_ASSOCIATED_PHY_MAX_NUMBER  = 2 << 9;

    private static final Integer           PHY_ASSOCIATED_LOGIC_MAX_NUMBER  = 2 << 9;

    @Autowired
    private ClusterLogicService            clusterLogicService;

    @Autowired
    private ClusterPhyService              clusterPhyService;

    @Autowired
    private RegionRackService              regionRackService;

    @Autowired
    private RoleClusterHostService         roleClusterHostService;
    
    private static FutureUtil<Void> futureUtil = FutureUtil.initBySystemAvailableProcessors("ClusterContextManagerImpl", 100);

    @Override
    public void flushClusterPhyContext(String clusterPhyName) {
        try {
            Thread.sleep(1000);
            ClusterPhyContext clusterPhyContext = getClusterPhyContext(clusterPhyName);
            if (null != clusterPhyContext) {
                name2ClusterPhyContextMap.put(clusterPhyContext.getClusterName(), clusterPhyContext);
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            LOGGER.error("class=ClusterContextManagerImpl||method=flushClusterPhyContext||clusterPhyName={}||errMsg={}",
                clusterPhyName, e.getMessage(), e);
        }
    }

    @Override
    public void flushClusterLogicContext(Long clusterLogicId) {
        try {
            Thread.sleep(1000);
            ClusterLogicContext clusterLogicContext = getClusterLogicContext(clusterLogicId);
            if (null != clusterLogicContext) {
                id2ClusterLogicContextMap.put(clusterLogicContext.getClusterLogicId(), clusterLogicContext);
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            LOGGER.error(
                "class=ClusterContextManagerImpl||method=flushClusterLogicContext||clusterLogicId={}||errMsg={}",
                clusterLogicId, e.getMessage(), e);

        }
    }

    @Override
    public void flushClusterContextByClusterRegion(ClusterRegion clusterRegion) {
        if (null == clusterRegion) {
            return;
        }

        flushClusterPhyContext(clusterRegion.getPhyClusterName());
        flushClusterLogicContext(clusterRegion.getLogicClusterId());
    }

    @Override
    public Result<Boolean> canClusterLogicAssociatedPhyCluster(Long clusterLogicId, String clusterPhyName,
                                                               Long regionId, Integer clusterLogicType) {
        //新建时clusterLogicId为空, 防止NPE
        if (AriusObjUtils.isNull(clusterLogicId)) {
            clusterLogicId = -1L;
        }

        ClusterLogicContext clusterLogicContext = getClusterLogicContext(clusterLogicId);
        ClusterPhyContext   clusterPhyContext   = getClusterPhyContext(clusterPhyName);

        int associatedPhyNum = 0;
        int associatedLogicNum = 0;
        if (null != clusterLogicContext) {
            associatedPhyNum = clusterLogicContext.getAssociatedPhyNum();
        }
        if (null != clusterPhyContext) {
            associatedLogicNum = clusterPhyContext.getAssociatedLogicNum();
        }

        return doValid(associatedPhyNum, associatedLogicNum, clusterLogicId, clusterPhyName, regionId, clusterLogicType);
    }

    /**
     *   1. Type为独立, LP = 1, PL = 1
     *   2. Type为共享, LP = 1, PL = n ,  1 <= n <= 1024
     * 	 3. Type为独占, LP = n, PL = 1 ,  1 <= n <= 1024
     */
    @Override
    public Result<List<String>> getCanBeAssociatedClustersPhys(Integer clusterLogicType, Long clusterLogicId) {
        if (!ResourceLogicTypeEnum.isExist(clusterLogicType)) {
            return Result.buildParamIllegal("逻辑集群类型非法");
        }

        List<String> canBeAssociatedClustersPhyNames = Lists.newArrayList();

        if (PRIVATE.getCode() == clusterLogicType) {
            handleClusterLogicTypePrivate(clusterLogicId, canBeAssociatedClustersPhyNames);
        }

        if (EXCLUSIVE.getCode() == clusterLogicType) {
            handleClusterLogicTypeExclusive(clusterLogicId, canBeAssociatedClustersPhyNames);
        }

        if (PUBLIC.getCode() == clusterLogicType) {
            handleClusterLogicTypePublic(clusterLogicId, canBeAssociatedClustersPhyNames);
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

        return clusterLogicIds.stream()
                .map(r -> clusterLogicService.getClusterLogicById(r))
                .map(ClusterLogic::getName)
                .distinct()
                .collect(Collectors.toList());
    }

    /***********************************************private*********************************************/

    private void loadClusterLogicContexts() {
        List<ClusterLogic> clusterLogicList = clusterLogicService.listAllClusterLogics();
        if (CollectionUtils.isEmpty(clusterLogicList)) {
            LOGGER.warn("class=ClusterContextManagerImpl||method=loadId2ESClusterLogicContextMap||msg=平台无逻辑集群");
        }

        for (ClusterLogic clusterLogic : clusterLogicList) {
            futureUtil.runnableTask(() -> {
                ClusterLogicContext clusterLogicContext = getClusterLogicContext(clusterLogic.getId());
                if (null != clusterLogicContext) {
                    id2ClusterLogicContextMap.put(clusterLogicContext.getClusterLogicId(), clusterLogicContext);
                }
            });
        }
        futureUtil.waitExecute();
    }

    @Override
    public ClusterLogicContext getClusterLogicContext(Long clusterLogicId) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(clusterLogicId);
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

    @Override
    public ClusterPhyContext getClusterPhyContext(String clusterPhyName) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
        if (null == clusterPhy) {
            LOGGER.error(
                    "class=ClusterContextManagerImpl||method=flushClusterPhyContext||clusterPhyName={}||msg=clusterPhy is empty",
                    clusterPhyName);
            return null;
        }

        ClusterPhyContext build = ClusterPhyContext.builder()
                                .clusterPhyId(clusterPhy.getId().longValue())
                                .clusterName(clusterPhy.getCluster())
                                .associatedLogicNumMax(PHY_ASSOCIATED_LOGIC_MAX_NUMBER)
                                .build();

        setClusterPhyNodeInfo(build);
        setRegionAndClusterLogicInfo(build);
        return build;
    }

    /**
     *   定义规则:
     *   1. Type为独立, LP = 1, PL = 1
     *   2. Type为共享, LP = n, PL = 1 ,  1 <= n <= 1024   多个逻辑集群共享一个物理集群, 每个逻辑集群关联物理集群一部分region, 不可跨其他物理集群
     *   3. Type为独占, LP = 1, PL = n ,  1 <= n <= 1024   一个逻辑集群独占一个或者多个物理集群
     */
    private ClusterLogicContext buildInitESClusterLogicContextByType(ClusterLogic clusterLogic) {

        if (PRIVATE.getCode() == clusterLogic.getType() || EXCLUSIVE.getCode() == clusterLogic.getType()) {
            return ClusterLogicContext.builder()
                    .clusterLogicName(clusterLogic.getName())
                    .clusterLogicId(clusterLogic.getId())
                    .logicClusterType(clusterLogic.getType())
                    .associatedPhyNumMax(1)
                    .build();
        } else if (PUBLIC.getCode() == clusterLogic.getType()) {
            return ClusterLogicContext.builder()
                    .clusterLogicName(clusterLogic.getName())
                    .clusterLogicId(clusterLogic.getId())
                    .logicClusterType(clusterLogic.getType())
                    .associatedPhyNumMax(LOGIC_ASSOCIATED_PHY_MAX_NUMBER)
                    .build();
        } else {
            LOGGER.error(
                "class=ClusterContextManagerImpl||method=buildInitESClusterLogicContextByType||esClusterLogicId={}||msg={}",
                    clusterLogic.getId(), String.format("请确认逻辑集群%s类型是否存在", clusterLogic.getType()));

            return ClusterLogicContext.builder()
                    .clusterLogicName(clusterLogic.getName())
                    .clusterLogicId(clusterLogic.getId())
                    .logicClusterType(clusterLogic.getType())
                    .associatedPhyNumMax(-1)
                    .build();
        }
    }

    private void setAssociatedClusterPhyInfo(ClusterLogicContext build) {
        List<String> clusterPhyNames = regionRackService.listPhysicClusterNames(build.getClusterLogicId());
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
        List<ClusterRegion> regions = regionRackService.listLogicClusterRegions(build.getClusterLogicId());
        build.setAssociatedRegionIds(regions.stream().map(ClusterRegion::getId).collect(Collectors.toList()));

        //获取逻辑集群关联region下的rack节点信息
        List<RoleClusterHost> associatedRackClusterHosts = Lists.newArrayList();
        for (ClusterRegion region : regions) {
            List<RoleClusterHost> roleClusterHosts = roleClusterHostService.listRacksNodes(region.getPhyClusterName(), region.getRacks());
            associatedRackClusterHosts.addAll(roleClusterHosts);
        }

        //设置数据节点总数
        build.setAssociatedDataNodeNum(associatedRackClusterHosts.size());

        //设置数据节点Ip地址
        build.setAssociatedDataNodeIps(
            associatedRackClusterHosts.stream().map(RoleClusterHost::getIp).collect(Collectors.toList()));
    }

    private void loadClusterPhyContexts() {
        List<ClusterPhy> clusterPhyList = clusterPhyService.listAllClusters();
        if (CollectionUtils.isEmpty(clusterPhyList)) {
            LOGGER.warn("class=ClusterContextManagerImpl||method=loadClusterPhyContexts||msg=集群上下文信息为空");
        }

        for (ClusterPhy clusterPhy : clusterPhyList) {
            futureUtil.runnableTask(() -> {
                try {
                    ClusterPhyContext clusterPhyContext = getClusterPhyContext(clusterPhy.getCluster());
                    if (null != clusterPhyContext) {
                        name2ClusterPhyContextMap.put(clusterPhyContext.getClusterName(), clusterPhyContext);
                    }
                } catch (Exception e) {
                    LOGGER.error("class=ClusterContextManagerImpl||method=loadClusterPhyContexts||msg={}",e.getMessage(), e);
                }
            });
        }
        futureUtil.waitExecute();
    }

    private void setRegionAndClusterLogicInfo(ClusterPhyContext build) {
        List<ClusterRegion> regions = regionRackService.listPhyClusterRegions(build.getClusterName());
        build.setAssociatedRegionIds(regions.stream().map(ClusterRegion::getId).collect(Collectors.toList()));

        List<Long> associatedClusterLogicIds = regions.stream().map(ClusterRegion::getLogicClusterId).filter(r -> r > 0)
            .distinct().collect(Collectors.toList());

        build.setAssociatedClusterLogicIds(associatedClusterLogicIds);
        build.setAssociatedLogicNum(associatedClusterLogicIds.size());
    }

    private void setClusterPhyNodeInfo(ClusterPhyContext build) {
        List<RoleClusterHost> nodes = roleClusterHostService.getNodesByCluster(build.getClusterName());
        List<RoleClusterHost> dataNodes = nodes.stream().filter(r -> DATA_NODE.getCode() == r.getRole())
            .collect(Collectors.toList());

        build.setAssociatedDataNodeNum(dataNodes.size());
        build.setAssociatedDataNodeIps(dataNodes.stream().map(RoleClusterHost::getIp).collect(Collectors.toList()));
        build.setAssociatedNodeIps(nodes.stream().map(RoleClusterHost::getIp).collect(Collectors.toList()));
        build.setAssociatedRacks(
            dataNodes.stream().map(RoleClusterHost::getRack).distinct().collect(Collectors.toList()));
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

        if (UNKNOWN.getCode() == ResourceLogicTypeEnum.valueOf(clusterLogicType).getCode()) {
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

        //独占逻辑集群不能跨集群
        if (EXCLUSIVE.getCode() == clusterLogicType && associatedPhyNumber > 1) {
            return Result.buildFail(String.format("集群间关联失败, 逻辑集群类型为独占, 已关联了大集群%s, 不可跨集群", clusterPhyName));
        }

        if (PUBLIC.getCode() == clusterLogicType && associatedLogicNumber > 1) {
            return Result.buildFail(String.format("集群间关联失败, 逻辑集群类型为共享, 物理集群%s已有关联逻辑集群, 禁止物理集群夸逻辑集群关联", clusterPhyName));
        }

        return Result.buildSucc(Boolean.TRUE);
    }

    private boolean canClusterLogicBoundRegion(Long regionId, String clusterPhyName, Long clusterLogicId) {
        ClusterRegion region                =  regionRackService.getRegionById(regionId);
        ClusterPhyContext clusterPhyContext =  getClusterPhyContext(clusterPhyName);
        List<Long> clusterLogicIds          =  clusterPhyContext.getAssociatedClusterLogicIds();
        if (CollectionUtils.isNotEmpty(clusterLogicIds) && !clusterLogicIds.contains(clusterLogicId)) {
            return false;
        }

        if (region.getLogicClusterId() > 0) {
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
     * LP = n , PL = 1
     * @param clusterLogicId    逻辑集群Id
     * @param clusterPhyContext 物理集群上下文
     * @return                  true/false
     */
    private boolean checkForExclusive(Long clusterLogicId, ClusterPhyContext clusterPhyContext) {
        if (null == clusterLogicId) {
            if (0 == clusterPhyContext.getAssociatedLogicNum()) {
                return true;
            } else {
                return clusterPhyContext.getAssociatedLogicNum() >= 1
                       && hasClusterPhyContextAssociatedLogicTypeIsCode(clusterPhyContext, EXCLUSIVE.getCode());
            }
        } else {
            ClusterLogicContext clusterLogicContext = id2ClusterLogicContextMap.get(clusterLogicId);
            if (0 == clusterLogicContext.getAssociatedPhyNum() && 0 == clusterPhyContext.getAssociatedLogicNum()) {
                return true;
            } else {
                return (clusterPhyContext.getAssociatedLogicNum() >= 1 && hasClusterPhyContextAssociatedLogicTypeIsCode(clusterPhyContext, EXCLUSIVE.getCode()))
                       || (1 == clusterLogicContext.getAssociatedPhyNum() && hasBelongClusterLogicContextAssociatedClusterNames(clusterLogicContext, clusterPhyContext.getClusterName()));
            }
        }
    }

    /**
     * LP = 1 , PL = n
     * @param clusterLogicId    逻辑集群Id
     * @param clusterPhyContext 物理集群上下文
     * @return true/false
     */
    private boolean checkForPublic(Long clusterLogicId, ClusterPhyContext clusterPhyContext) {
        if (null == clusterLogicId) {
            if (0 == clusterPhyContext.getAssociatedLogicNum()) {
                return true;
            } else {
                return clusterPhyContext.getAssociatedLogicNum() == 1
                       && hasClusterPhyContextAssociatedLogicTypeIsCode(clusterPhyContext, PUBLIC.getCode());
            }
        } else {
            ClusterLogicContext clusterLogicContext = id2ClusterLogicContextMap.get(clusterLogicId);
            if (0 == clusterLogicContext.getAssociatedPhyNum() && 0 == clusterPhyContext.getAssociatedLogicNum()) {
                return true;
            } else {
                return (clusterPhyContext.getAssociatedLogicNum() == 1 && hasClusterPhyContextAssociatedLogicTypeIsCode(clusterPhyContext, PUBLIC.getCode()))
                       || (clusterLogicContext.getAssociatedPhyNum() >= 1 && hasBelongClusterLogicContextAssociatedClusterNames(clusterLogicContext,
                               clusterPhyContext.getClusterName()))
                       || (clusterLogicContext.getAssociatedPhyNum() >= 1 && clusterPhyContext.getAssociatedLogicNum() == 0);
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

    private void handleClusterLogicTypePublic(Long clusterLogicId, List<String> canBeAssociatedClustersPhyNames) {
        for (ClusterPhyContext clusterPhyContext : name2ClusterPhyContextMap.values()) {
            if (checkForPublic(clusterLogicId, clusterPhyContext)) {
                canBeAssociatedClustersPhyNames.add(clusterPhyContext.getClusterName());
            }
        }
    }

    private void handleClusterLogicTypeExclusive(Long clusterLogicId, List<String> canBeAssociatedClustersPhyNames) {
        for (ClusterPhyContext clusterPhyContext : name2ClusterPhyContextMap.values()) {
            if (checkForExclusive(clusterLogicId, clusterPhyContext)) {
                canBeAssociatedClustersPhyNames.add(clusterPhyContext.getClusterName());
            }
        }
    }

    private void handleClusterLogicTypePrivate(Long clusterLogicId, List<String> canBeAssociatedClustersPhyNames) {
        for (ClusterPhyContext clusterPhyContext : name2ClusterPhyContextMap.values()) {
            if (checkForPrivate(clusterLogicId, clusterPhyContext)) {
                canBeAssociatedClustersPhyNames.add(clusterPhyContext.getClusterName());
            }
        }
    }
}
