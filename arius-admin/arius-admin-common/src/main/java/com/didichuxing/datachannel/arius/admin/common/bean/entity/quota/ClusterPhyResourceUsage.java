package com.didichuxing.datachannel.arius.admin.common.bean.entity.quota;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 物理集群资源使用情况
 * @author wangshu
 * @date 2020/10/04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterPhyResourceUsage implements Serializable {
    /**
     * 物理集群名称
     */
    private String cluster;

    /**
     * 集群所有磁盘空间总和，单位是G
     */
    private Double totalDiskG;

    /**
     * 磁盘空间使用总和，单位是G
     */
    private Double diskUsageG;

    /**
     * CPU数量
     */
    private Double cpuCount;

    /**
     * CPU使用情况
     */
    private Double cpuUsage;

    /**
     * 集群总的内存总量，单位为G
     */
    private Double totalMemG;

    /**
     * 内存使用量，单位是G
     */
    private Double memUsageG;
}
