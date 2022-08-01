package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Created by linyunan on 3/11/22
 */
public enum DashBoardMetricTopTypeEnum {

    UNKNOWN(OneLevelTypeEnum.UNKNOWN, "", "未知"),
    CLUSTER_ELAPSED_TIME(OneLevelTypeEnum.CLUSTER, "clusterElapsedTime", "cluster_stats 接口平均采集耗时(s)"),
    NODE_ELAPSED_TIME(OneLevelTypeEnum.CLUSTER, "nodeElapsedTime", "nodes_stats 接口平均采集耗时(s)"),

    CLUSTER_ELAPSED_TIME_GTE_5MIN(OneLevelTypeEnum.CLUSTER, "elapsedTimeGte5Min", "采集耗时大于5分钟"),
    CLUSTER_INDEXING_LATENCY(OneLevelTypeEnum.CLUSTER, "indexingLatency", "写入耗时"),
    CLUSTER_SEARCH_LATENCY(OneLevelTypeEnum.CLUSTER, "searchLatency", "查询耗时"),
    CLUSTER_INDEX_REQ_NUM(OneLevelTypeEnum.CLUSTER, "indexReqNum", "写入文档数"),
    CLUSTER_GATEWAY_SUC_PER(OneLevelTypeEnum.CLUSTER, "gatewaySucPer", "网关成功率"),
    CLUSTER_GATEWAY_FAILED_PER(OneLevelTypeEnum.CLUSTER, "gatewayFailedPer", "网关失败率"),
    CLUSTER_PENDING_TASK_NUM(OneLevelTypeEnum.CLUSTER, "pendingTaskNum", "集群pending-task数"),
    CLUSTER_HTTP_NUM(OneLevelTypeEnum.CLUSTER, "httpNum", "集群http连接数"),
    CLUSTER_DOC_UPRUSH_NUM(OneLevelTypeEnum.CLUSTER, "docUprushNum", "查询请求数突增集群"),
    CLUSTER_REQ_UPRUSH_NUM(OneLevelTypeEnum.CLUSTER, "reqUprushNum", "写入文档数突增集群"),
    CLUSTER_SHARD_NUM(OneLevelTypeEnum.CLUSTER, "shardNum", "集群shard个数"),
    NODE_TASK_CONSUMING(OneLevelTypeEnum.NODE, "taskConsuming", "节点分片耗时"),
    INDEX_REQ_UPRUSH_NUM(OneLevelTypeEnum.INDEX, "reqUprushNum", "查询请求数突增索引"),
    NODE_DOC_UPRUSH_NUM(OneLevelTypeEnum.INDEX, "docUprushNum", "写入文档数突增索引"),

    MANAGEMENT_QUEUE(OneLevelTypeEnum.CLUSTER_THREAD_POOL_QUEUE, "management", "集群管理queue数"),
    REFRESH_QUEUE(OneLevelTypeEnum.CLUSTER_THREAD_POOL_QUEUE, "refresh", "集群刷新线程池queue数"),
    FLUSH_QUEUE(OneLevelTypeEnum.CLUSTER_THREAD_POOL_QUEUE, "flush", "集群落盘刷新线程池queue数"),
    MERGE_QUEUE(OneLevelTypeEnum.CLUSTER_THREAD_POOL_QUEUE, "merge", "集群合并线程池queue数"),
    SEARCH_QUEUE(OneLevelTypeEnum.CLUSTER_THREAD_POOL_QUEUE, "search", "集群查询理线程池queue数"),
    WRITE_QUEUE(OneLevelTypeEnum.CLUSTER_THREAD_POOL_QUEUE, "write", "集群写入线程池queue数");

    DashBoardMetricTopTypeEnum(OneLevelTypeEnum oneLevelTypeEnum, String type, String desc) {
        this.oneLevelTypeEnum = oneLevelTypeEnum;
        this.type = type;
        this.desc = desc;
    }

    private OneLevelTypeEnum oneLevelTypeEnum;
    private String desc;
    private String type;

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public OneLevelTypeEnum getOneLevelTypeEnum() {
        return oneLevelTypeEnum;
    }

    public static boolean hasExist(String oneLevelType, String metricsType) {
        if (null == metricsType) {
            return false;
        }
        for (DashBoardMetricTopTypeEnum typeEnum : DashBoardMetricTopTypeEnum.values()) {
            OneLevelTypeEnum oneLevelTypeEnum = typeEnum.getOneLevelTypeEnum();
            if (oneLevelTypeEnum.getType().equals(oneLevelType) && metricsType.equals(typeEnum.getType())) {
                return true;
            }
        }

        return false;
    }

    public static DashBoardMetricTopTypeEnum valueOfType(String type) {
        if (null == type) {
            return DashBoardMetricTopTypeEnum.UNKNOWN;
        }

        for (DashBoardMetricTopTypeEnum typeEnum : DashBoardMetricTopTypeEnum.values()) {
            if (type.equals(typeEnum.getType())) {
                return typeEnum;
            }
        }

        return DashBoardMetricTopTypeEnum.UNKNOWN;
    }

    public static List<DashBoardMetricTopTypeEnum> valueOfTypes(List<String> types) {
        List<DashBoardMetricTopTypeEnum> resList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(types)) {
            return resList;
        }

        for (String s : types) {
            if (null == s) {
                continue;
            }

            for (DashBoardMetricTopTypeEnum typeEnum : DashBoardMetricTopTypeEnum.values()) {
                if (s.equals(typeEnum.getType())) {
                    resList.add(typeEnum);
                }
            }
        }

        return resList;
    }

    /**
     * 需要特殊处理的指标项，这里需要过滤去除指标值为-1
     *
     * @return List<String>
     */
    public static List<String> listNoNegativeMetricTypes() {
        return Lists.newArrayList(CLUSTER_GATEWAY_SUC_PER.getType(),
                /**
                 * 未知
                 */CLUSTER_GATEWAY_FAILED_PER.getType());
    }
}
