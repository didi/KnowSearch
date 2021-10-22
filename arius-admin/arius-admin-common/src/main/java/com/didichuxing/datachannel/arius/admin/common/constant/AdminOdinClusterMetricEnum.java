package com.didichuxing.datachannel.arius.admin.common.constant;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public enum AdminOdinClusterMetricEnum {
    CLUSTER_HEALTH_STATUS(-1, "es.cluster.health.status", "集群健康状态"),
    CLUSTER_HEALTH_UNASSIGNED_SHARDS(-1, "es.cluster.health.unassignedShards", "集群未分配的shard数"),
    CLUSTER_HEALTH_PENDING_TASK(-1, "es.cluster.health.pendingTask", "集群待执行任务"),
//    CLUSTER_HEALTH_NUMBER_OF_DATA_NODES(-1, "es.cluster.health.number.of.data.nodes", "集群数据节点数"),
//    CLUSTER_HEALTH_NUMBER_OF_NODES(-1, "es.cluster.health.number.of.nodes", "集群节点数"),

    ;

    AdminOdinClusterMetricEnum(int code, String metric, String desc) {
        this.code   = code;
        this.metric = metric;
        this.desc   = desc;
    }

    private int code;

    private String metric;

    private String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getMetric() {return metric;}

    public static String metricCluster(){return "cluster.health";}

    public static List<AdminOdinClusterMetricEnum> getAllAdminOdinMetric(){
        return Arrays.asList(AdminOdinClusterMetricEnum.values());
    }

    public static List<String> getAllAdminOdinMetricName(){
        return getAllAdminOdinMetric().stream().map(a -> a.getMetric()).collect(Collectors.toList());
    }
}