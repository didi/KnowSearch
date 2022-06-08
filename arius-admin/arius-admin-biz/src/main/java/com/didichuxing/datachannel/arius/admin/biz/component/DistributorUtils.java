package com.didichuxing.datachannel.arius.admin.biz.component;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateDistributedRack;
import com.didichuxing.datachannel.arius.admin.biz.extend.foctory.ExtendServiceFactory;
import com.didichuxing.datachannel.arius.admin.biz.extend.foctory.TemplateClusterDistributor;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;

import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType.NO_CAPACITY_PLAN;

/**
 * @author wangshu
 * @date 2020/06/10
 */
@Component
public class DistributorUtils {

    private static final ILog     LOGGER = LogFactory.getLog(DistributorUtils.class);

    @Autowired
    private ClusterPhyService     esClusterPhyService;

    @Autowired
    private ClusterLogicService clusterLogicService;

    @Autowired
    private ClusterRegionService clusterRegionService;

    @Autowired
    private ExtendServiceFactory  extendServiceFactory;

    /**
     * 获取集群名称列表
     * @return
     */
    public List<String> fetchClusterNames() {
        List<String> clusterNames = null;
            clusterNames = new ArrayList<>();
            List<ClusterPhy> esClusterPhies = esClusterPhyService.getAllClusters();
            for (ClusterPhy clusterPhy : esClusterPhies) {
                clusterNames.add(clusterPhy.getCluster());
            }
        return clusterNames;
    }

    /**
     * 新建使用
     * @param resourceId 逻辑
     * @param quota quota
     * @return result
     */
    @Deprecated
    public Result<TemplateDistributedRack> getTemplateDistributedRack(Long resourceId, double quota) {
        Result<TemplateClusterDistributor> extendResult = extendServiceFactory
            .getExtend(TemplateClusterDistributor.class);

        TemplateClusterDistributor extendDistributor = null;
        if (extendResult.success()) {
            extendDistributor = extendResult.getData();
        } else {
            LOGGER.warn("class=DistributorUtils||method=getTemplateResourceInner||msg=extendDistributor not find");
        }

        TemplateClusterDistributor defaultDistributor = extendServiceFactory
            .getDefault(TemplateClusterDistributor.class);

        Result<TemplateDistributedRack> distributedRackResult = null;
        if (extendDistributor != null) {
            distributedRackResult = extendDistributor.distribute(resourceId, quota);
        }

        if (distributedRackResult == null || distributedRackResult.getCode().equals(NO_CAPACITY_PLAN.getCode())) {
            distributedRackResult = defaultDistributor.distribute(resourceId, quota);
        }

        if (distributedRackResult.failed()) {
            return distributedRackResult;
        }

        return distributedRackResult;
    }

    public Result<List<ClusterLogicRackInfo>> validateAndGetLogicItems(Long resourceId) {
        if (!isLogicClusterExists(resourceId)) {
            return Result.buildNotExist("逻辑资源不存在");
        }

        List<ClusterLogicRackInfo> items = clusterRegionService.listLogicClusterRacks(resourceId);
        if (CollectionUtils.isEmpty(items)) {
            return Result.buildNotExist("逻辑资源没有对应的物理资源");
        }

        return Result.buildSucc(items);
    }

    /**
     * 逻辑集群是否存在
     * @param logicClusterId 逻辑集群ID
     * @return
     */
    public boolean isLogicClusterExists(Long logicClusterId) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(logicClusterId);
        return (null != clusterLogic);
    }
}
