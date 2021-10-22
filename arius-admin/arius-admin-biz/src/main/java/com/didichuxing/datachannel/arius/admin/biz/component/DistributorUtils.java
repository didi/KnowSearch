package com.didichuxing.datachannel.arius.admin.biz.component;

import com.didichuxing.datachannel.arius.admin.biz.extend.intfc.ExtendServiceFactory;
import com.didichuxing.datachannel.arius.admin.biz.extend.intfc.TemplateClusterDistributor;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.TemplateDistributedRack;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;

import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType.NO_CAPACITY_PLAN;

/**
 * @author wangshu
 * @date 2020/06/10
 */
@Component
public class DistributorUtils {

    private static final ILog     LOGGER = LogFactory.getLog(DistributorUtils.class);

    @Autowired
    private ESClusterPhyService   esClusterPhyService;

    @Autowired
    private ESClusterLogicService esClusterLogicService;

    @Autowired
    private ESRegionRackService   esRegionRackService;

    @Autowired
    private ExtendServiceFactory extendServiceFactory;

    /**
     * 通过IDC获取集群名称列表
     * @return
     */
    public List<String> fetchClusterNames() {
        List<String> clusterNames = null;
            clusterNames = new ArrayList<>();
            List<ESClusterPhy> esClusterPhies = esClusterPhyService.listAllClusters();
            for (ESClusterPhy esClusterPhy : esClusterPhies) {
                clusterNames.add(esClusterPhy.getCluster());
            }
        return clusterNames;
    }

    /**
     * 新建使用
     * @param resourceId 逻辑
     * @param quota quota
     * @return result
     */
    public Result<TemplateDistributedRack> getTemplateDistributedRack(Long resourceId, double quota) {
        Result<TemplateClusterDistributor> extendResult = extendServiceFactory
            .getExtend(TemplateClusterDistributor.class);

        TemplateClusterDistributor extendDistributor = null;
        if (extendResult.success()) {
            extendDistributor = extendResult.getData();
        } else {
            LOGGER.warn("method=getTemplateResourceInner||msg=extendDistributor not find");
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
            return Result.buildFrom(distributedRackResult);
        }

        return distributedRackResult;
    }

    public Result validateAndGetLogicItems(Long resourceId) {
        if (!isLogicClusterExists(resourceId)) {
            return Result.buildFrom(Result.buildNotExist("逻辑资源不存在"));
        }

        List<ESClusterLogicRackInfo> items = esRegionRackService.listLogicClusterRacks(resourceId);
        if (CollectionUtils.isEmpty(items)) {
            return Result.buildFrom(Result.buildNotExist("逻辑资源没有对应的物理资源"));
        }

        return Result.buildSucc(items);
    }

    /**
     * 逻辑集群是否存在
     * @param logicClusterId 逻辑集群ID
     * @return
     */
    public boolean isLogicClusterExists(Long logicClusterId) {
        ESClusterLogic esClusterLogic = esClusterLogicService.getLogicClusterById(logicClusterId);
        if (esClusterLogic == null) {
            return false;
        }
        return true;
    }
}
