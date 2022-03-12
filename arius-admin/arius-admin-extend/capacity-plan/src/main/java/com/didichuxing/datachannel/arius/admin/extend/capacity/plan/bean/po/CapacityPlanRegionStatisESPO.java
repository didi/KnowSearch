package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.po;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-09-04
 */
@Data
public class CapacityPlanRegionStatisESPO extends BaseESPO {

    /**
     * regionID
     */
    private Long    regionId;

    /**
     * areaId
     */
    private Long    areaId;

    /**
     * regionId
     */
    private Long    resourceId;

    /**
     * 集群
     */
    private String  cluster;

    /**
     * rack
     */
    private String  racks;

    /**
     * 节点个数
     */
    private Integer nodeCount;

    /**
     * quota个数
     */
    private Double  quota;

    /**
     * 磁盘(G)
     */
    private Double  regionDiskG;

    /**
     * CPU(核)
     */
    private Double  regionCpuCount;

    /**
     * quota
     */
    private Double  costQuota;

    /**
     * 磁盘消耗(G)
     */
    private Double  costDiskG;

    /**
     * CPU消耗(核)
     */
    private Double  costCpuCount;

    /**
     * 出售的quota个数
     */
    private Double  soldQuota;

    /**
     * 出售的quota磁盘(G)
     */
    private Double  soldDiskG;

    /**
     * 出售的quotaCPU(核)
     */
    private Double  soldCpuCount;

    /**
     * freeQuota
     */
    private Double  freeQuota;

    /**
     * 空闲的quota磁盘(G)
     */
    private Double  freeDiskG;

    /**
     * 空闲的quotaCPU(核)
     */
    private Double  freeCpuCount;

    /**
     * 时间戳
     */
    private Date    timestamp;

    /**
     * 获取主键key
     *
     * @return
     */
    @Override
    public String getKey() {
        return regionId + "@" + AriusDateUtils.date2Str(timestamp, "yyyy-MM-dd HH:mm");
    }
}
