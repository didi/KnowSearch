package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author gyp
 * @date 2022/5/9
 * @version 1.0
 */
public enum ClusterPhyClusterMetricsEnum {
                                          /*** 未知 */
                                          UNKNOWN("", "未知"),

                                          BASIC("basic", "基本指标"),

                                          ELAPSEDTIME("elapsedTime", "采集用时"),

                                          /*************************************1.CPU**************************************/
                                          /**
                                           * 单位：%
                                           */
                                          CPU_USAGE("cpuUsage", "cpu使用率"),
                                          /**
                                           * 单位：无
                                           */
                                          CPU_LOAD_1M("cpuLoad1M", "cpu 1分钟负载"),
                                          /**
                                           * 单位：无
                                           */
                                          CPU_LOAD_5M("cpuLoad5M", "cpu 5分钟负载"),
                                          /**
                                           * 单位：无
                                           */
                                          CPU_LOAD_15M("cpuLoad15M", "cpu 15分钟负载"),
                                          /*************************************2.磁盘******************************************/
                                          /**
                                           * 单位：%
                                           */
                                          DISK_USAGE("diskUsage", "磁盘使用率"),
                                          /**
                                           * 单位：GB
                                           */
                                          DISK_INFO("diskInfo", "磁盘使用情况"),
                                          /**
                                           * 单位：%
                                           */
                                          NODES_FOR_DISK_USAGE_GTE_75PERCENT("nodesForDiskUsageGte75Percent",
                                                                             "磁盘使用率（大于75%）"),

                                          /*************************************3.网络流量**************************************/
                                          /**
                                           * 单位：MB/S
                                           */
                                          RECV_TRANS_SIZE("recvTransSize", "网络入口流量"),
                                          /**
                                           * 单位：MB/S
                                           */
                                          SEND_TRANS_SIZE("sendTransSize", "网络出口流量"),

                                          /*************************************4.shard******************************************/
                                          /**
                                           * 单位：个
                                           */
                                          SHARD_NUM("shardNu", "shard总数"),
                                          /**
                                           * 单位：个
                                           */
                                          MOVING_SHARDS("movingShards", "正在搬迁shard"),
                                          /**
                                           * 单位：个
                                           */
                                          BIG_SHARDS("bigShards", "大shard（大于50G）"),

                                          /*************************************5.index******************************************/
                                          /**
                                           * 单位：个
                                           */
                                          BIG_INDICES("bigIndices", "大索引（大于10亿）"),

                                          /*************************************6.node******************************************/
                                          /**
                                           * 单位：个
                                           */
                                          INVALID_NODES("invalidNodes", "无效节点"),

                                          /*************************************7.任务进程******************************************/
                                          /**
                                           * 单位：个
                                           */
                                          PENDING_TASKS("pendingTasks", "集群元数据变更堆积任务"),

                                          /*************************************8.read/write******************************************/
                                          /**
                                           * 单位：个/S
                                           */
                                          READ_QPS("readTps", "查询QPS"),
                                          /**
                                           * 单位：个/S
                                           */
                                          WRITE_TPS("writeTps", "写入TPS"),
                                          /**
                                           * 单位：ms
                                           */
                                          SEARCH_LATENCY("searchLatency", "查询耗时"),
                                          /**
                                           * 单位：ms
                                           */
                                          INDEXING_LATENCY("indexingLatency", "写入耗时"),
                                          /**
                                           * 单位：ms
                                           */
                                          TASK_COST("taskCost", "任务耗时"),
                                          /**
                                           * 单位：个
                                           */
                                          TASK_NUM("taskCount", "任务数量");

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
