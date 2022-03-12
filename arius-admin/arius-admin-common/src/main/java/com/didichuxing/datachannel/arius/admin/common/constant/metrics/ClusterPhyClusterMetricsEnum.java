package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by linyunan on 2021-07-30
 */
public enum ClusterPhyClusterMetricsEnum {
                                          /*** 未知 */
                                          UNKNOWN("", "未知"),

                                          BASIC("basic", "基本指标"),

                                          /*************************************1.CPU**************************************/

                                          CPU_USAGE("cpuUsage", "cpu使用率"),

                                          CPU_LOAD_1M("cpuLoad1M", "cpu 1分钟负载"),

                                          CPU_LOAD_5M("cpuLoad5M", "cpu 5分钟负载"),

                                          CPU_LOAD_15M("cpuLoad15M", "cpu 15分钟负载"),
                                          /*************************************2.磁盘******************************************/

                                          DISK_USAGE("diskUsage", "磁盘使用率"),

                                          DISK_INFO("diskInfo", "磁盘使用情况"),

                                          NODES_FOR_DISK_USAGE_GTE_75PERCENT("nodesForDiskUsageGte75Percent", "磁盘使用情况"),

                                          /*************************************3.网络流量**************************************/

                                          RECV_TRANS_SIZE("recvTransSize", "网络入口流量"),

                                          SEND_TRANS_SIZE("sendTransSize", "网络出口流量"),

                                          /*************************************4.shard******************************************/

                                          SHARD_NU("shardNu", "shard总数/未分配shard数"),

                                          MOVING_SHARDS("movingShards", "正在搬迁shard"),

                                          BIG_SHARDS("bigShards", "大shard"),

                                          /*************************************5.index******************************************/

                                          BIG_INDICES("bigIndices", "大索引"),

                                          /*************************************6.node******************************************/

                                          INVALID_NODES("invalidNodes", "无效节点"),

                                          /*************************************7.任务进程******************************************/

                                          PENDING_TASKS("pendingTasks", "挂载任务"),

                                          /*************************************8.read/write******************************************/

                                          READ_TPS("readTps", "查询QPS"),

                                          WRITE_TPS("writeTps", "写入TPS"),

                                          SEARCH_LATENCY("searchLatency", "查询耗时"),

                                          INDEXING_LATENCY("indexingLatency", "写入耗时");

    ClusterPhyClusterMetricsEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    private String type;

    private String desc;

    public String getDesc() {
        return desc;
    }

    public String getType() {
        return type;
    }

    public static ClusterPhyClusterMetricsEnum valueOfType(String type) {
        if (null == type) {
            return ClusterPhyClusterMetricsEnum.UNKNOWN;
        }
        for (ClusterPhyClusterMetricsEnum typeEnum : ClusterPhyClusterMetricsEnum.values()) {
            if (type.equals(typeEnum.getType())) {
                return typeEnum;
            }
        }

        return ClusterPhyClusterMetricsEnum.UNKNOWN;
    }

    public static boolean hasExist(String metricsType) {
        if (null == metricsType) {
            return false;
        }
        for (ClusterPhyClusterMetricsEnum typeEnum : ClusterPhyClusterMetricsEnum.values()) {
            if (metricsType.equals(typeEnum.getType())) {
                return true;
            }
        }

        return false;
    }

    public static List<String> getClusterPhyMetricsType() {
        List<String> clusterPhyMetricsTypes = Lists.newArrayList();
        for (ClusterPhyClusterMetricsEnum value : ClusterPhyClusterMetricsEnum.values()) {
            if (UNKNOWN.getType().equals(value.getType())) {
                continue;
            }

            clusterPhyMetricsTypes.add(value.type);
        }

        return clusterPhyMetricsTypes;
    }

    public static List<String> getDefaultClusterPhyMetricsCode() {
        return getClusterPhyMetricsType();
    }
}
