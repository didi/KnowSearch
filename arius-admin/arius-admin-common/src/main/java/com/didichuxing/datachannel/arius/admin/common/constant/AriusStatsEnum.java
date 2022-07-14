package com.didichuxing.datachannel.arius.admin.common.constant;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/3/11 下午2:19
 * @modified By D10865
 *
 * 不同维度的es监控数据
 */
public enum AriusStatsEnum {

                            /**
                             * 索引维度
                             */
                            INDEX_INFO("index_info"),

                            /**
                             * 节点到索引维度
                             */
                            NODE_INDEX_INFO("node_index_info"),

                            /**
                             * 索引到节点维度
                             */
                            INDEX_NODE_INFO("index_node_info"),

                            /**
                             * 节点维度
                             */
                            NODE_INFO("node_info"),

                            /**
                             * ingest写入维度
                             */
                            INGEST_INFO("ingest_info"),

                            /**
                             * 集群维度
                             */
                            CLUSTER_INFO("cluster_info"),

                            /**
                             * task维度
                             */
                            TASK_INFO("task_info"),

                            /**
                             * dcdr维度
                             */
                            DCDR_INFO("dcdr_info"),

                            /**
                             * dashboard
                             */
                            DASHBOARD_INFO("dashboard_info");

    private String type;

    AriusStatsEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static AriusStatsEnum valueOfType(String type) {
        if (type == null) {
            return null;
        }
        for (AriusStatsEnum infoType : AriusStatsEnum.values()) {
            if (infoType.getType().equals(type)) {
                return infoType;
            }
        }

        return null;
    }
}
