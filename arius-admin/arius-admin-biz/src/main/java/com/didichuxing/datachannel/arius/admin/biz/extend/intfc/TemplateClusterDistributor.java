package com.didichuxing.datachannel.arius.admin.biz.extend.intfc;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.TemplateDistributedRack;

/**
 * @author d06679
 * @date 2019-07-09
 */
public interface TemplateClusterDistributor {

    /**
     * 分配资源 在指定逻辑集群分配
     * @param resourceId 资源
     * @param quota 配额
     * @return rack
     */
    Result<TemplateDistributedRack> distribute(Long resourceId, Double quota);

    /**
     * 在指定逻辑集群的物理集群分配
     * @param resourceId 资源
     * @param cluster 物理集群
     * @param quota quota
     * @return result
     */
    Result<TemplateDistributedRack> indecrease(Long resourceId, String cluster, String rack, Double quota);

}
