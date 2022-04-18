package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.component.DistributorUtils;
import com.didichuxing.datachannel.arius.admin.biz.extend.intfc.TemplateClusterDistributor;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateDistributedRack;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author d06679
 * @date 2019-08-02
 */
@Service("defaultTemplateClusterDistributor")
public class DefaultTemplateClusterDistributor implements TemplateClusterDistributor {

    private static final ILog      LOGGER = LogFactory.getLog(DefaultTemplateClusterDistributor.class);

    @Autowired
    private RegionRackService      regionRackService;

    @Autowired
    private DistributorUtils       distributorUtils;

    /**
     * 分配资源
     *
     * 平台默认实现是随机选一个集群的全部rack
     *
     * @param resourceId 资源
     * @param quota      配额
     * @return rack
     */
    @Override
    public Result<TemplateDistributedRack> distribute(Long resourceId, Double quota) {
        Result<List<ClusterLogicRackInfo>> result = distributorUtils.validateAndGetLogicItems(resourceId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }

        List<ClusterLogicRackInfo> items = result.getData();
        Multimap<String, ClusterLogicRackInfo> clusterName2ResourceLogicItemMultiMap = ConvertUtil.list2MulMap(items,
            ClusterLogicRackInfo::getPhyClusterName);

        List<String> clusters = Lists
            .newArrayList(ConvertUtil.list2MulMap(items, ClusterLogicRackInfo::getPhyClusterName).asMap().keySet());

        Result<String> fetchOneClusterResult = randomFetchOneCluster(clusters);
        if (fetchOneClusterResult.failed()) {
            return Result.buildFail(fetchOneClusterResult.getMessage());
        }

        String cluster = fetchOneClusterResult.getData();
        Collection<ClusterLogicRackInfo> racks = clusterName2ResourceLogicItemMultiMap.get(cluster);
        return getTemplateDistributedRackResult(cluster, racks);
    }

    private Result<TemplateDistributedRack> getTemplateDistributedRackResult(String cluster,
                                                                             Collection<ClusterLogicRackInfo> racks) {
        List<String> rackNames = racks.stream().map(ClusterLogicRackInfo::getRack).collect(Collectors.toList());

        TemplateDistributedRack templateDistributedRack = new TemplateDistributedRack();
        templateDistributedRack.setCluster(cluster);
        templateDistributedRack.setRack(String.join(",", rackNames));
        templateDistributedRack.setIsResourceSuitable(true);

        LOGGER.info("class=DefaultTemplateClusterDistributor||method=distribute||cluster={}||rack={}", templateDistributedRack.getCluster(),
            templateDistributedRack.getRack());

        return Result.buildSucc(templateDistributedRack);
    }

    /**
     * 在指定逻辑集群的物理集群分配
     *
     * @param resourceId 资源
     * @param cluster    物理集群
     * @param quota      quota
     * @return result
     */
    @Override
    public Result<TemplateDistributedRack> indecrease(Long resourceId, String cluster, String rack, Double quota) {
        if (!distributorUtils.isLogicClusterExists(resourceId)) {
            return Result.buildNotExist("逻辑资源不存在");
        }

        List<ClusterLogicRackInfo> items = regionRackService.listLogicClusterRacks(resourceId);
        items = items.stream().filter(item -> cluster.equals(item.getPhyClusterName())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(items)) {
            return Result.buildNotExist("逻辑资源没有对应的物理资源");
        }

        return getTemplateDistributedRackResult(cluster, items);
    }

    /*************************************************private**********************************************************/
    /**
     * 随机选择一个符合要求的集群
     * @param clusters 待选机器列表
     * @return
     */
    private Result<String> randomFetchOneCluster(List<String> clusters) {
        Collections.shuffle(clusters);

        List<String> allClusters = distributorUtils.fetchClusterNames();

        LOGGER.info("class=DefaultTemplateClusterDistributor||method=randomFetchOneCluster||clusters={}||allClusters={}",
            JSON.toJSONString(clusters), JSON.toJSONString(allClusters));

        for (String cluster : clusters) {
            if (allClusters.contains(cluster)) {
                return Result.buildSucc(cluster);
            }
        }

        return Result.buildNotExist("没有合适的集群");
    }
}
