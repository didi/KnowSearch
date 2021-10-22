package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_COMMON_GROUP;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.extend.intfc.TemplateClusterDistributor;
import com.didichuxing.datachannel.arius.admin.biz.component.DistributorUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.TemplateDistributedRack;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * @author d06679
 * @date 2019-08-02
 */
@Service("defaultTemplateClusterDistributor")
public class DefaultTemplateClusterDistributor implements TemplateClusterDistributor {

    private static final ILog      LOGGER = LogFactory.getLog(DefaultTemplateClusterDistributor.class);

    @Autowired
    private ESRegionRackService    ESRegionRackService;

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

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
        Result result = distributorUtils.validateAndGetLogicItems(resourceId);
        if (result.failed()) {
            return result;
        }

        List<ESClusterLogicRackInfo> items = (List<ESClusterLogicRackInfo>) result.getData();
        Multimap<String, ESClusterLogicRackInfo> clusterName2ResourceLogicItemMultiMap = ConvertUtil.list2MulMap(items,
            ESClusterLogicRackInfo::getPhyClusterName);

        List<String> clusters = Lists
            .newArrayList(ConvertUtil.list2MulMap(items, ESClusterLogicRackInfo::getPhyClusterName).asMap().keySet());

        Result fetchOneClusterResult = randomFetchOneCluster(clusters);
        if (fetchOneClusterResult.failed()) {
            return fetchOneClusterResult;
        }

        String cluster = (String) fetchOneClusterResult.getData();
        Collection<ESClusterLogicRackInfo> racks = clusterName2ResourceLogicItemMultiMap.get(cluster);
        return getTemplateDistributedRackResult(cluster, racks);
    }

    private Result<TemplateDistributedRack> getTemplateDistributedRackResult(String cluster,
                                                                             Collection<ESClusterLogicRackInfo> racks) {
        List<String> rackNames = racks.stream().map(ESClusterLogicRackInfo::getRack).collect(Collectors.toList());

        TemplateDistributedRack templateDistributedRack = new TemplateDistributedRack();
        templateDistributedRack.setCluster(cluster);
        templateDistributedRack.setRack(String.join(",", rackNames));
        templateDistributedRack.setIsResourceSuitable(true);

        LOGGER.info("method=distribute||cluster={}||rack={}", templateDistributedRack.getCluster(),
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
            return Result.buildFrom(Result.buildNotExist("逻辑资源不存在"));
        }

        List<ESClusterLogicRackInfo> items = ESRegionRackService.listLogicClusterRacks(resourceId);
        items = items.stream().filter(item -> cluster.equals(item.getPhyClusterName())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(items)) {
            return Result.buildFrom(Result.buildNotExist("逻辑资源没有对应的物理资源"));
        }

        return getTemplateDistributedRackResult(cluster, items);
    }

    /*************************************************private**********************************************************/
    /**
     * 随机选择一个符合要求的集群
     * @param clusters 待选机器列表
     * @return
     */
    private Result randomFetchOneCluster(List<String> clusters) {
        Collections.shuffle(clusters);
        Set<String> clusterBlackList = ariusConfigInfoService.stringSettingSplit2Set(ARIUS_COMMON_GROUP,
            "auto.process.work.order.cluster.blacks", "", ",");

        List<String> idcClusters = distributorUtils.fetchClusterNames();

        LOGGER.info("method=randomFetchOneCluster||clusters={}||idc={}||clusterBlackList={}||idcClusters={}",
            JSON.toJSONString(clusters), JSON.toJSONString(clusterBlackList), JSON.toJSONString(idcClusters));

        for (String cluster : clusters) {
            if (!clusterBlackList.contains(cluster)) {
                if (idcClusters == null || idcClusters.contains(cluster)) {
                    return Result.buildSucc(cluster);
                }
            }
        }

        return Result.buildFrom(Result.buildNotExist("没有合适的集群"));
    }
}
