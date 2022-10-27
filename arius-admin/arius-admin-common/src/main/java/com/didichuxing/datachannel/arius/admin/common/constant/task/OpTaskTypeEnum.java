package com.didichuxing.datachannel.arius.admin.common.constant.task;

import java.util.Arrays;

/**
 * 任务枚举类型
 *
 * @author ohushenglin_v
 * @date 2022-05-20
 */
public enum OpTaskTypeEnum {
                            /**新增*/
                            CLUSTER_NEW(1, "集群新建", "cluster-create"),

                            CLUSTER_EXPAND(2, "集群扩容", "cluster-expand"),

                            CLUSTER_SHRINK(3, "集群缩容", "cluster-shrink"),

                            CLUSTER_RESTART(4, "集群重启", "cluster-restart"),

                            CLUSTER_UPGRADE(5, "集群升级", "cluster-upgrade"),
                            @Deprecated
                            CLUSTER_PLUG_OPERATION(6, "集群插件操作", "cluster-plug-operation"),
                            @Deprecated
                            //TODO 集群下线操作不在走op-task
                            CLUSTER_OFFLINE(7, "集群下线操作", "cluster-offline"),

                            TEMPLATE_DCDR(10, "索引模板主从切换", "template-dcdr"),
                             @Deprecated
                            //TODO 移除：全部改走 配置编辑
                            CLUSTER_CONFIG_ADD(11, "集群配置新增", "cluster-config-add"),

                            CLUSTER_CONFIG_EDIT(12, "集群配置编辑", "cluster-config-edit"),
                            @Deprecated
                            //TODO 移除：全部改走 配置编辑
                            CLUSTER_CONFIG_DELETE(13, "集群配置删除", "cluster-config-delete"),

                             CLUSTER_PLUG_INSTALL(14, "集群插件安装", "cluster-plug-install"),
    CLUSTER_PLUG_UNINSTALL(15, "集群插件卸载", "cluster-plug-uninstall"),
    CLUSTER_PLUG_UPGRADE(16, "集群插件升级", "cluster-plug-upgrade"),
    CLUSTER_PLUG_RESTART(17, "集群插件重启", "cluster-plug-restart"),
    CLUSTER_PLUG_ROLLBACK(18, "集群插件回滚", "cluster-plug-rollback"),
    CLUSTER_ROLLBACK(19, "集群回滚", "cluster-rollback"),
    CLUSTER_CONFIG_ROLLBACK(20, "集群配置回滚", "cluster-config-rollback"),
    GATEWAY_NEW(21, "GATEWAY集群新建", "gateway-create"),
    GATEWAY_EXPAND(22, "GATEWAY集群扩容", "gateway-expand"),
    
    GATEWAY_SHRINK(23, "GATEWAY集群缩容", "gateway-shrink"),
    
    GATEWAY_RESTART(24, "GATEWAY集群重启", "gateway-restart"),
    
    GATEWAY_UPGRADE(25, "GATEWAY集群升级", "gateway-upgrade"),
    //TODO 0.3.2 不会去实现，需等待gateway具备安装平台插件能力后进行实现
    GATEWAY_PLUG_INSTALL(26, "GATEWAY集群插件安装", "gateway-plug-install"),
    //TODO 0.3.2 不会去实现，需等待gateway具备安装平台插件能力后进行实现
    GATEWAY_PLUG_UNINSTALL(27, "GATEWAY集群插件卸载", "gateway-plug-uninstall"),
    GATEWAY_CONFIG_EDIT(28, "GATEWAY集群配置新增", "gateway-config-edit"),
    GATEWAY_CONFIG_ROLLBACK(29, "GATEWAY集群配置回滚", "gateway-config-rollback"),
    GATEWAY_ROLLBACK(30, "GATEWAY集群回滚", "gateway-rollback"),
                            UNKNOWN(-1, "unknown");

    OpTaskTypeEnum(Integer type, String message) {
        this.type = type;
        this.message = message;
    }

    OpTaskTypeEnum(Integer type, String message, String apiPath) {
        this.type = type;
        this.message = message;
        this.apiPath = apiPath;
    }

    private Integer type;

    private String  message;
    private String  apiPath;

    public Integer getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getApiPath() {
        return apiPath;
    }

    public static OpTaskTypeEnum valueOfType(Integer type) {
        if (type == null) {
            return OpTaskTypeEnum.UNKNOWN;
        }
        for (OpTaskTypeEnum typeEnum : OpTaskTypeEnum.values()) {
            if (type.equals(typeEnum.getType())) {
                return typeEnum;
            }
        }

        return OpTaskTypeEnum.UNKNOWN;
    }

    public static OpTaskTypeEnum valueOfPath(String apiPath) {
        if (apiPath == null || "".equals(apiPath.trim())) {
            return OpTaskTypeEnum.UNKNOWN;
        }

        return Arrays.stream(OpTaskTypeEnum.values()).filter(typeEnum -> apiPath.equals(typeEnum.getApiPath()))
            .findAny().orElse(OpTaskTypeEnum.UNKNOWN);
    }

}