package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.indices;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

/**
 * Created by linyunan on 2022/7/7
 */

public abstract class BaseIndicesController {

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    /**
     * 临时黑名单列表, 禁止通过索引管理操作物理集群
     * @param clusterPhyNames   物理集群列表
     * @return                  Result<Boolean>
     */
    protected Result<Boolean> checkClusterValid(List<String> clusterPhyNames) {
        if (CollectionUtils.isEmpty(clusterPhyNames)) {
            return Result.buildSucc();
        }

        // 暂时代码写死，防止页面上通过配置修改
        List<String> filterClustersFromDidi = Lists.newArrayList("didi-cluster-test");

        Set<String> filterClustersFromAriusConfig = ariusConfigInfoService
            .stringSettingSplit2Set("arius.cluster.blacklist", "cluster.phy.name", "", ",");

        filterClustersFromAriusConfig.addAll(filterClustersFromDidi);

        for (String clusterPhyName : clusterPhyNames) {
            if (filterClustersFromAriusConfig.contains(clusterPhyName)) {
                return Result.buildFail(String.format("该物理集群[%s]已添加黑名单, 禁止对集群进行任何新增、编辑、删除等操作", clusterPhyName));
            }
        }

        return Result.buildSucc();
    }
}
