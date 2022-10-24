package com.didichuxing.datachannel.arius.admin.common.constant.workorder;

import org.apache.commons.lang3.StringUtils;

/**
 * arius工单状态枚举
 * name要求是对应的handler的bean的名字的前缀;框架会根据前缀去找handler
 *
 * @author d06679
 * @date 2018/9/18
 */
public enum WorkOrderTypeEnum {

                               /**
                                * 逻辑集群创建
                                */
                               LOGIC_CLUSTER_CREATE("logicClusterCreate", "集群创建"),

                               /**
                                * 逻辑集群indecrease
                                */
                               LOGIC_CLUSTER_INDECREASE("logicClusterIndecrease", "集群扩(缩)容"),

                               LOGIC_CLUSTER_OP_DELETE("logicClusterDelete", "逻辑集群删除"),

                               LOGIC_CLUSTER_AUTH("logicClusterAuth", "集群权限"),

                               LOGIC_CLUSTER_TRANSFER("logicClusterTransfer", "逻辑集群转让"),

                               LOGIC_CLUSTER_PLUGIN("logicClusterPlugin", "集群插件"),

                               TEMPLATE_AUTH("templateAuth", "索引权限"),

                               TEMPLATE_CREATE("templateCreate", "模版创建"),

                               TEMPLATE_INDECREASE("templateIndecrease", "模版扩缩"),

                               TEMPLATE_QUERY_DSL("templateQueryDsl", "查询语句"),

                               TEMPLATE_TRANSFER("templateTransfer", "模版转移"),

                               QUERY_DSL_LIMIT_EDIT("queryDslLimitEdit", "查询语句编辑"),

                               CLUSTER_OP_INDECREASE("clusterOpIndecrease", "集群扩缩(OP)"),

                               CLUSTER_OP_NEW("clusterOpNew", "集群创建(OP)"),

                               CLUSTER_OP_PLUG_OPERATION("logicClusterPlugOperation", "集群插件操作"),

                               CLUSTER_OP_OFFLINE("clusterOpOffline", "集群下线(OP)"),

                               CLUSTER_OP_RESTART("clusterOpRestart", "集群重启(OP)"),

                               CLUSTER_OP_CONFIG_RESTART("clusterOpConfigRestart", "集群配置重启(OP)"),

                               CLUSTER_OP_UPDATE("clusterOpUpdate", "集群升级(OP)"),

                               CLUSTER_DELETE("clusterDelete", "物理集群删除"),

                               PHY_CLUSTER_PLUGIN_OPERATION("clusterOpPluginRestart", "物理集群插件操作"),
    LOGIC_CLUSTER_JOIN("logicClusterJoin", "集群加入"),

                               UNKNOWN("unknown", "未知");

    WorkOrderTypeEnum(String name, String message) {
        this.name = name;
        this.message = message;
    }

    private String name;

    private String message;

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public static WorkOrderTypeEnum valueOfName(String name) {
        if (StringUtils.isBlank(name)) {
            return WorkOrderTypeEnum.UNKNOWN;
        }
        for (WorkOrderTypeEnum typeEnum : WorkOrderTypeEnum.values()) {
            if (name.equals(typeEnum.getName())) {
                return typeEnum;
            }
        }

        return WorkOrderTypeEnum.UNKNOWN;
    }
}