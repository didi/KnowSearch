package com.didichuxing.datachannel.arius.admin.common.constant.cluster;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ClusterDynamicConfigTypeCheckFunUtil;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ClusterDynamicConfigsEnum {
    /**
     * 未知动态配置项
     */
    UNKNOWN("unknown", ClusterDynamicConfigsTypeEnum.UNKNOWN,
            ClusterDynamicConfigTypeCheckFunUtil::defaultTypeCheck),

    /**
     * 动态配置项中集群下breaker类型
     */
    INDICES_BREAKER_TOTAL_LIMIT("indices.breaker.total.limit",
            ClusterDynamicConfigsTypeEnum.BREAKER,
            ClusterDynamicConfigTypeCheckFunUtil::percentCheck),
    INDICES_BREAKER_FIELDDATA_LIMIT("indices.breaker.fielddata.limit",
            ClusterDynamicConfigsTypeEnum.BREAKER,
            ClusterDynamicConfigTypeCheckFunUtil::percentCheck),
    INDICES_BREAKER_FIELDDATA_OVERHEAD("indices.breaker.fielddata.overhead",
            ClusterDynamicConfigsTypeEnum.BREAKER,
            ClusterDynamicConfigTypeCheckFunUtil::floatValueCheck1to100),
    INDICES_BREAKER_REQUEST_LIMIT("indices.breaker.request.limit",
            ClusterDynamicConfigsTypeEnum.BREAKER,
            ClusterDynamicConfigTypeCheckFunUtil::percentCheck),
    INDICES_BREAKER_REQUEST_OVERHEAD("indices.breaker.request.overhead",
            ClusterDynamicConfigsTypeEnum.BREAKER,
            ClusterDynamicConfigTypeCheckFunUtil::floatValueCheck1to100),
    NETWORK_BREAKER_INFLIGHT_REQUESTS_LIMIT("network.breaker.inflight_requests.limit",
            ClusterDynamicConfigsTypeEnum.BREAKER,
            ClusterDynamicConfigTypeCheckFunUtil::percentCheck),
    NETWORK_BREAKER_INFLIGHT_REQUESTS_OVERHEAD("network.breaker.inflight_requests.overhead",
            ClusterDynamicConfigsTypeEnum.BREAKER,
            ClusterDynamicConfigTypeCheckFunUtil::floatValueCheck1to100),

    /**
     * 动态配置项中集群下routing类型
     */
    CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_INCOMING_RECOVERIES("cluster.routing.allocation.node_concurrent_incoming_recoveries",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::intCheck),
    CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_OUTGOING_RECOVERIES("cluster.routing.allocation.node_concurrent_outgoing_recoveries",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::intCheck),
    CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES("cluster.routing.allocation.node_concurrent_recoveries",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::intCheck),
    CLUSTER_ROUTING_ALLOCATION_NODE_INITIAL_PRIMARIES_RECOVERIES("cluster.routing.allocation.node_initial_primaries_recoveries",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::intCheck),
    CLUSTER_ROUTING_ALLOCATION_SAME_SHARD_HOST("cluster.routing.allocation.same_shard.host",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::booleanCheck),
    CLUSTER_ROUTING_REBALANCE_ENABLE("cluster.routing.rebalance.enable",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::reBalanceEnableTypeCheck),
    CLUSTER_ROUTING_ALLOCATION_ALLOW_REBALANCE("cluster.routing.allocation.allow_rebalance",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::allowReBalanceTypeCheck),
    CLUSTER_ROUTING_ALLOCATION_CLUSTER_CONCURRENT_REBALANCE("cluster.routing.allocation.cluster_concurrent_rebalance",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::intCheck),
    CLUSTER_ROUTING_ALLOCATION_BALANCE_SHARD("cluster.routing.allocation.balance.shard",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::floatValueCheckPositive),
    CLUSTER_ROUTING_ALLOCATION_BALANCE_INDEX("cluster.routing.allocation.balance.index",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::floatValueCheckPositive),
    CLUSTER_ROUTING_ALLOCATION_BALANCE_THRESHOLD("cluster.routing.allocation.balance.threshold",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::floatValueCheckPositive),
    CLUSTER_ROUTING_ALLOCATION_DISK_THRESHOLD_ENABLED("cluster.routing.allocation.disk.threshold_enabled",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::booleanCheck),
    CLUSTER_ROUTING_ALLOCATION_DISK_WATERMARK_LOW("cluster.routing.allocation.disk.watermark.low",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::percentCheck),
    CLUSTER_ROUTING_ALLOCATION_DISK_WATERMARK_HIGH("cluster.routing.allocation.disk.watermark.high",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::percentCheck),
    CLUSTER_ROUTING_ALLOCATION_DISK_WATERMARK_FLOOD_STAGE("cluster.routing.allocation.disk.watermark.flood_stage",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::percentCheck),
    CLUSTER_ROUTING_ALLOCATION_TOTAL_SHARDS_PER_NODE("cluster.routing.allocation.total_shards_per_node",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::intCheck1000),
    CLUSTER_ROUTING_ALLOCATION_AWARENESS_ATTRIBUTES("cluster.routing.allocation.awareness.attributes",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::attributesTypeCheck),
    CLUSTER_ROUTING_ALLOCATION_ENABLE("cluster.routing.allocation.enable",
            ClusterDynamicConfigsTypeEnum.ROUTING,
            ClusterDynamicConfigTypeCheckFunUtil::allocationEnableTypeCheck),

    /**
     * 动态配置项中集群下zen类型
     */
    DISCOVERY_ZEN_COMMIT_TIMEOUT("discovery.zen.commit_timeout",
            ClusterDynamicConfigsTypeEnum.ZEN,
            ClusterDynamicConfigTypeCheckFunUtil::timeCheck),
    DISCOVERY_ZEN_MINIMUM_MASTER_NODES("discovery.zen.minimum_master_nodes",
            ClusterDynamicConfigsTypeEnum.ZEN,
            ClusterDynamicConfigTypeCheckFunUtil::intCheck),
    DISCOVERY_ZEN_NO_MASTER_BLOCK("discovery.zen.no_master_block",
            ClusterDynamicConfigsTypeEnum.ZEN,
            ClusterDynamicConfigTypeCheckFunUtil::masterBlockTypeCheck),
    DISCOVERY_ZEN_PUBLISH_DIFF_ENABLE("discovery.zen.publish_diff.enable",
            ClusterDynamicConfigsTypeEnum.ZEN,
            ClusterDynamicConfigTypeCheckFunUtil::booleanCheck),
    DISCOVERY_ZEN_PUBLISH_TIMEOUT("discovery.zen.publish_timeout",
            ClusterDynamicConfigsTypeEnum.ZEN,
            ClusterDynamicConfigTypeCheckFunUtil::timeCheck);

    private String name;
    private ClusterDynamicConfigsTypeEnum clusterDynamicConfigsType;
    private Function<String, Boolean> checkFun;

    ClusterDynamicConfigsEnum(String name, ClusterDynamicConfigsTypeEnum clusterDynamicConfigsType, Function<String, Boolean> checkFun) {
        this.name = name;
        this.clusterDynamicConfigsType = clusterDynamicConfigsType;
        this.checkFun = checkFun;
    }

    public String getName() {
        return name;
    }

    public ClusterDynamicConfigsTypeEnum getClusterDynamicConfigsType() {
        return clusterDynamicConfigsType;
    }

    public Function<String, Boolean> getCheckFun() {
        return checkFun;
    }

    public static ClusterDynamicConfigsEnum valueCodeOfName(String name) {
        if (AriusObjUtils.isNull(name)) {
            return ClusterDynamicConfigsEnum.UNKNOWN;
        }

        for (ClusterDynamicConfigsEnum param : ClusterDynamicConfigsEnum.values()) {
            if (param.getName().equals(name)) {
                return param;
            }
        }
        return ClusterDynamicConfigsEnum.UNKNOWN;
    }

    public static List<ClusterDynamicConfigsEnum> valuesWithoutUnknown() {
        return Arrays.stream(values()).filter(clusterDynamicConfigsEnum ->
                clusterDynamicConfigsEnum != ClusterDynamicConfigsEnum.UNKNOWN).collect(Collectors.toList());
    }
}
