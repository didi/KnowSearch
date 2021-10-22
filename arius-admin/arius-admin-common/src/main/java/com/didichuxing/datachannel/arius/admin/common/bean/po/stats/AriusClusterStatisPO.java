package com.didichuxing.datachannel.arius.admin.common.bean.po.stats;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AriusClusterStatisPO extends BaseESPO {
    /**
     * 集群名称，all则表示全集群信息
     */
    private String clusterName;

    /**
     * 集群状态
     */
    private String status;

    /**
     * 集群已使用容量，单位
     */
    private double storeSize;

    /**
     * 集群总容量，单位
     */
    private double totalStoreSize;

    /**
     * 集群空余容量，单位
     */
    private double freeStoreSize;

    /**
     * 索引容量，单位
     */
    private double indexStoreSize;

    /**
     * 集群索引数
     */
    private double totalIndicesNu;

    /**
     * 集群模板数
     */
    private int totalTemplateNu;

    /**
     * 集群文档数
     */
    private long totalDocNu;

    /**
     * 集群shard数量
     */
    private long shardNu;

    /**
     * 每秒接受流量
     */
    private double recvTransSize;

    /**
     * 每秒发送流量
     */
    private double sendTransSize;

    /**
     * 集群写入tps
     */
    private double writeTps;

    /**
     * 集群读取tps
     */
    private double readTps;

    /**
     * cpu使用率
     */
    private double cpuUsage;

    /**
     * 磁盘使用率
     */
    private double diskUsage;

    /**
     * 集群稳定性
     */
    private double sla;

    /**
     * 集群总数
     */
    private int clusterNu;

    /**
     * es节点数
     */
    private double esNodeNu;

    /**
     * 应用数量
     */
    private int appNu;

    /**
     * 每日查询总量
     */
    private long queryTimesPreDay;

    @Override
    public String getKey() {
        return clusterName;
    }
}
