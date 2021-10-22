package com.didichuxing.datachannel.arius.admin.client.bean.common;

import com.didichuxing.datachannel.arius.admin.client.constant.quota.Resource;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
public class RegionMetric {

    /**
     * 集群
     */
    private String   cluster;

    /**
     * rack名字
     */
    private String   racks;
    /**
     * 节点个数
     */
    private Integer  nodeCount;

    /**
     * region资源总量
     */
    private Resource resource;

    /**
     * 磁盘空闲空间
     */
    private Double   diskFreeG;

    public int getRackCount() {
        return racks.split(",").length;
    }
}
