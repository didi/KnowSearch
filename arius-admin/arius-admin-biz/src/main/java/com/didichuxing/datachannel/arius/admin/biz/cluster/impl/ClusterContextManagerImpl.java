package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum.EXCLUSIVE;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum.PRIVATE;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum.PUBLIC;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum.UNKNOWN;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhyContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.exception.ClusterLogicTypeException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Created by linyunan on 2021-06-08
 */
@Service
public class ClusterContextManagerImpl implements ClusterContextManager {
    private static final ILog                LOGGER                          = LogFactory
        .getLog(ClusterContextManagerImpl.class);

    /**
     * key-> 逻辑集群Id
     */
    private Map<Long, ESClusterLogicContext> id2ESClusterLogicContextMap     = Maps.newConcurrentMap();

    /**
     * key-> 物理集群名称
     */
    private Map<String, ESClusterPhyContext> name2ESClusterPhyContextMap     = Maps.newConcurrentMap();

    private static final Integer             LOGIC_ASSOCIATED_PHY_MAX_NUMBER = 2 << 9;

    private static final Integer             PHY_ASSOCIATED_LOGIC_MAX_NUMBER = 2 << 9;

    @Autowired
    private ESClusterLogicService            esClusterLogicService;

    @Autowired
    private ESClusterPhyService              esClusterPhyService;

    @Autowired
    private ESRegionRackService              esRegionRackService;

    @Autowired
    private ESRoleClusterHostService         esRoleClusterHostService;

    private final static FutureUtil          futureUtil       = FutureUtil.init("ClusterContextManagerImpl");

    @PostConstruct
    @Scheduled(cron = "0 3/10 * * * ?")
    @Override
    public void flushClusterContext() {
        LOGGER
            .info("class=ClusterContextManagerImpl||method=flushClusterContext||ClusterContextManagerImpl init start.");
        try {
        	//上下文刷新太快, 数据未写入DB, 会出现NPE
			Thread.sleep(1000L);

            loadESClusterPhyContexts();

            loadESClusterLogicContexts();
        } catch (Exception e) {
            LOGGER.error("class=ClusterContextManagerImpl||method=flushClusterContext||msg={}", e.getMessage());
        }
        LOGGER.info(
            "class=ClusterContextManagerImpl||method=flushClusterContext||ClusterContextManagerImpl init finished.");
    }

    @Override
    public ESClusterLogicContext getESClusterLogicContext(Long clusterLogicId) {
        return id2ESClusterLogicContextMap.getOrDefault(clusterLogicId,
				ESClusterLogicContext.builder().associatedPhyNum(0).associatedPhyNumMax(1).build());
    }

    @Override
    public ESClusterPhyContext getESClusterPhyContext(String clusterPhyName) {
        return name2ESClusterPhyContextMap.getOrDefault(clusterPhyName,
				ESClusterPhyContext.builder().associatedLogicNum(0).associatedLogicNumMax(1).build());
    }

    @Override
    public Result canClusterLogicAssociatedPhyCluster(Long clusterLogicId, String clusterPhyName,
                                                      Integer clusterLogicType) {

        //新建时clusterLogicId为空, 防止NPE
        if (AriusObjUtils.isNull(clusterLogicId)) {
            clusterLogicId = -1L;
        }

        ESClusterLogicContext esClusterLogicContext = getESClusterLogicContext(clusterLogicId);
        ESClusterPhyContext esClusterPhyContext     = getESClusterPhyContext(clusterPhyName);

        Integer associatedPhyNum   = esClusterLogicContext.getAssociatedPhyNum();
        Integer associatedLogicNum = esClusterPhyContext.getAssociatedLogicNum();
        return doValid(associatedPhyNum, associatedLogicNum, clusterLogicId, clusterPhyName, clusterLogicType);
    }

    @Override
    public List<String> getCanBeAssociatedClustersPhys(Integer clusterLogicType) {
        List<String> canBeAssociatedClustersPhyNames = Lists.newArrayList();
        for (ESClusterPhyContext clusterPhyContext : name2ESClusterPhyContextMap.values()) {
            if (PRIVATE.getCode() == clusterLogicType || PUBLIC.getCode() == clusterLogicType) {
                if (clusterPhyContext.getAssociatedLogicNum() == 0) {
                    canBeAssociatedClustersPhyNames.add(clusterPhyContext.getClusterName());
                }
            } else if (EXCLUSIVE.getCode() == clusterLogicType) {
                canBeAssociatedClustersPhyNames.add(clusterPhyContext.getClusterName());
            } else {
                throw new ClusterLogicTypeException(String.format("请确认逻辑集群%s类型是否存在", clusterLogicType));
            }
        }

        return canBeAssociatedClustersPhyNames;
    }

	@Override
    public List<String> getClusterPhyAssociatedClusterLogicNames(String clusterPhyName) {
        ESClusterPhyContext esClusterPhyContext = getESClusterPhyContext(clusterPhyName);
        List<Long> clusterLogicIds = esClusterPhyContext.getAssociatedClusterLogicIds();
        if (CollectionUtils.isEmpty(clusterLogicIds)) {
            return Lists.newArrayList();
        }

        return clusterLogicIds
				.stream()
				.map(r -> esClusterLogicService.getLogicClusterById(r))
				.map(ESClusterLogic::getName)
				.distinct()
				.collect(Collectors.toList());
    }

	/***********************************************private*********************************************/

	private void loadESClusterLogicContexts() {
        List<ESClusterLogic> esClusterLogics = esClusterLogicService.listAllLogicClusters();
        if (CollectionUtils.isEmpty(esClusterLogics)) {
            LOGGER.warn("class=ClusterContextManagerImpl||method=loadId2ESClusterLogicContextMap||msg=平台无逻辑集群");
        }

		esClusterLogics.forEach(this::buildESClusterLogicContext);
	}

    private void buildESClusterLogicContext(ESClusterLogic esClusterLogic) {
		ESClusterLogicContext build = buildInitESClusterLogicContextByType(esClusterLogic.getType(), esClusterLogic);

		futureUtil.runnableTask(() -> setAssociatedClusterPhyInfo(build))
				  .runnableTask(() -> setRegionAndAssociatedClusterPhyDataNodeInfo(build))
				  .waitExecute();

		id2ESClusterLogicContextMap.put(build.getClusterLogicId(), build);
    }

	/**
	 * 定义规则:
	 * 1. Type为独立, LP = 1, PL = 1
	 * 2. Type为共享, LP = n, PL = 1 ,  1 <= n <= 1024
	 * 3. Type为独占, LP = 1, PL = n ,  1 <= n <= 1024
	 */
	private ESClusterLogicContext buildInitESClusterLogicContextByType(Integer type, ESClusterLogic esClusterLogic) {

		if (PRIVATE.getCode() == type || EXCLUSIVE.getCode() == type) {
            return ESClusterLogicContext
					.builder()
					.clusterLogicId(esClusterLogic.getId())
					.logicClusterType(esClusterLogic.getType())
					.associatedPhyNumMax(1)
					.build();
		} else if (PUBLIC.getCode() == type) {
			return ESClusterLogicContext
					.builder()
					.clusterLogicId(esClusterLogic.getId())
					.logicClusterType(esClusterLogic.getType())
					.associatedPhyNumMax(LOGIC_ASSOCIATED_PHY_MAX_NUMBER)
					.build();
		}else {
			LOGGER.error(
					"class=ClusterContextManagerImpl||method=buildInitESClusterLogicContextByType||esClusterLogicId={}||msg={}",
					esClusterLogic.getId(), String.format("请确认逻辑集群%s类型是否存在", esClusterLogic.getType()));

			return ESClusterLogicContext
					.builder()
					.clusterLogicId(esClusterLogic.getId())
					.logicClusterType(esClusterLogic.getType())
					.associatedPhyNumMax(-1)
					.build();
		}
	}

    private void setAssociatedClusterPhyInfo(ESClusterLogicContext build) {
        List<String> clusterPhyNames = esRegionRackService.listPhysicClusterNames(build.getClusterLogicId());
        if (build.getAssociatedPhyNumMax() < clusterPhyNames.size()) {
            LOGGER.error("class=ClusterContextManagerImpl||method=buildESClusterLogicContext"
                         + "||logicClusterType={}||esClusterLogicId={}||msg=集群间关联超过最大限制数{}, 请纠正",
                build.getLogicClusterType(), build.getClusterLogicId(), build.getAssociatedPhyNumMax());
            return;
        }

        build.setAssociatedClusterPhyNames(clusterPhyNames);
        build.setAssociatedPhyNum(clusterPhyNames.size());
    }

    private void setRegionAndAssociatedClusterPhyDataNodeInfo(ESClusterLogicContext build) {
        List<ClusterRegion> regions = esRegionRackService.listLogicClusterRegions(build.getClusterLogicId());
        build.setAssociatedRegionIds(regions.stream().map(ClusterRegion::getId).collect(Collectors.toList()));

        //从ESClusterPhyContextMap中获取物理集群相关信息
        List<String> clusterPhyNames = regions.stream().map(ClusterRegion::getPhyClusterName).distinct()
            .collect(Collectors.toList());
        List<ESClusterPhyContext> esClusterPhyContexts = clusterPhyNames.stream().map(this::getESClusterPhyContext)
            .collect(Collectors.toList());

        //设置数据节点总数
        int sumDataNodeNum = esClusterPhyContexts.stream().mapToInt(ESClusterPhyContext::getAssociatedDataNodeNum)
            .sum();
        build.setAssociatedDataNodeNum(sumDataNodeNum);

        //设置数据节点Ip地址
        List<String> dataNodeIps = Lists.newArrayList();
        for (ESClusterPhyContext esClusterPhyContext : esClusterPhyContexts) {
            dataNodeIps.addAll(esClusterPhyContext.getAssociatedDataNodeIps());
        }
        build.setAssociatedDataNodeIps(dataNodeIps);
    }

    private void loadESClusterPhyContexts() {
        List<ESClusterPhy> esClusterPhies = esClusterPhyService.listAllClusters();
        if (CollectionUtils.isEmpty(esClusterPhies)) {
            LOGGER.warn(
                "class=ClusterContextManagerImpl||method=loadPhyClusterAssociatedLogicClusterInfo||msg=集群上下文信息为空");
        }

        esClusterPhies.forEach(this::buildESClusterPhyContext);
    }

	private void buildESClusterPhyContext(ESClusterPhy clusterPhy) {
		ESClusterPhyContext build = ESClusterPhyContext
									.builder()
									.clusterPhyId(clusterPhy.getId().longValue())
									.clusterName(clusterPhy.getCluster())
									.associatedLogicNumMax(PHY_ASSOCIATED_LOGIC_MAX_NUMBER)
									.build();

		futureUtil.runnableTask(() -> setClusterPhyNodeInfo(build))
				  .runnableTask(() -> setRegionAndClusterLogicInfo(build))
				  .waitExecute();

        name2ESClusterPhyContextMap.put(build.getClusterName(), build);
	}

	private void setRegionAndClusterLogicInfo(ESClusterPhyContext build) {
        List<ClusterRegion> regions = esRegionRackService.listPhyClusterRegions(build.getClusterName());
        build.setAssociatedRegionIds(regions.stream().map(ClusterRegion::getId).collect(Collectors.toList()));

        List<Long> associatedClusterLogicIds = regions
												.stream()
												.map(ClusterRegion::getLogicClusterId)
												.filter(r -> r > 0)
												.distinct()
												.collect(Collectors.toList());

        build.setAssociatedClusterLogicIds(associatedClusterLogicIds);
        build.setAssociatedLogicNum(associatedClusterLogicIds.size());
	}

	private void setClusterPhyNodeInfo(ESClusterPhyContext build) {
        List<ESRoleClusterHost> dataNodes = esRoleClusterHostService.getDataNodesByCluster(build.getClusterName());
        build.setAssociatedDataNodeNum(dataNodes.size());
        build.setAssociatedDataNodeIps(dataNodes.stream().map(ESRoleClusterHost::getIp).collect(Collectors.toList()));
    }

	/**
	 * 具体校验逻辑
	 * @param associatedPhyNumber    逻辑集群关联物理集群个数
	 * @param associatedLogicNumber  物理集群关联逻辑集群个数
	 * @param clusterLogicType       逻辑集群类型
	 */
	private Result doValid(int associatedPhyNumber, int associatedLogicNumber, Long clusterLogicId,
			String clusterPhyName, Integer clusterLogicType) {

		if (AriusObjUtils.isNull(clusterLogicType)) {
			return Result.buildParamIllegal("逻辑集群类型为空");
		}

		if (UNKNOWN.getCode() == ResourceLogicTypeEnum.valueOf(clusterLogicType).getCode()) {
			return Result.buildParamIllegal("无法识别逻辑集群类型");
		}

		ESClusterPhy esClusterPhy = esClusterPhyService.getClusterByName(clusterPhyName);
		if (AriusObjUtils.isNull(esClusterPhy)) {
			return Result.buildFail("物理集群不存在");
		}

		if (PRIVATE.getCode() == clusterLogicType) {
			//先判断logic -> phy, 二次关联region需要先校验逻辑集群对应的物理集群数据是否合理
			if (associatedPhyNumber > 0) {
				return Result.buildParamIllegal(String.format("集群间关联失败 ,该独立逻辑集群%s已有关联物理集群", clusterLogicId));
			}
			//在判断phy -> logic
			if (associatedLogicNumber > 0) {
				return Result.buildFail(String.format("集群间关联失败, 物理集群%s已有关联逻辑集群", clusterPhyName));
			}

		}

		if (PUBLIC.getCode() == clusterLogicType && associatedLogicNumber > 0) {
			return Result.buildFail(String.format("集群间关联失败, 逻辑集群类型为共享, 物理集群%s已有关联逻辑集群, 禁止物理集群夸逻辑集群关联", clusterPhyName));
		}

		//独占逻辑集群不能跨集群
		if (PUBLIC.getCode() == clusterLogicType && associatedPhyNumber > 1) {
			return Result.buildFail(String.format("集群间关联失败, 逻辑集群类型为独立, 已关联了大集群%s, 不可跨集群", clusterPhyName));
		}

		return Result.buildSucc();
	}
}
