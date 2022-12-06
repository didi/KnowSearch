package com.didichuxing.datachannel.arius.admin.common.constant.task;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;

/**
 * 任务枚举类型
 *
 * @author ohushenglin_v
 * @date 2022-05-20
 */
public enum OpTaskTypeEnum {
    /**
     * TODO 关于ES的相关操作无需改动，这里需要保留至下个迭代之后才可以进行下线，所以这里的枚举类是不能改动的
     */
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

    //gateway
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
    GATEWAY_ROLLBACK(30, "GATEWAY 集群回滚", "gateway-rollback"),
    GATEWAY_OFFLINE(31, "GATEWAY 集群回滚", "gateway-rollback"),
    //es cluster 操作列
    ES_CLUSTER_NEW(32, "ES集群新建", "es-cluster-create"),
    
    ES_CLUSTER_EXPAND(33, "ES集群扩容", "es-cluster-expand"),
    
    ES_CLUSTER_SHRINK(34, "ES集群缩容", "es-cluster-shrink"),
    
    ES_CLUSTER_RESTART(35, "ES集群重启", "es-cluster-restart"),
    
    ES_CLUSTER_UPGRADE(36, "ES集群升级", "es-cluster-upgrade"),
    
    
    ES_CLUSTER_CONFIG_EDIT(37, "ES集群配置编辑", "es-cluster-config-edit"),
    
    ES_CLUSTER_PLUG_INSTALL(38, "ES集群插件安装", "es-cluster-plug-install"),
    ES_CLUSTER_PLUG_UNINSTALL(39, "ES集群插件卸载", "es-cluster-plug-uninstall"),
    ES_CLUSTER_PLUG_UPGRADE(40, "ES集群插件升级", "es-cluster-plug-upgrade"),
    ES_CLUSTER_PLUG_RESTART(41, "ES集群插件重启", "es-cluster-plug-restart"),
    ES_CLUSTER_ROLLBACK(42, "ES集群回滚", "es-cluster-rollback"),
    ES_CLUSTER_CONFIG_ROLLBACK(43, "ES 集群配置回滚", "es-cluster-config-rollback"),
    ES_CLUSTER_OFFLINE(44, "ES 集群下线操作", "es-cluster-offline"),
    ES_CLUSTER_PLUG_ROLLBACK(45, "ES 集群插件回滚", "es-cluster-plugin-rollback"),
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
    
    /**
     * > 它返回由 OpManager 管理的所有任务类型的列表
     *
     * @return OpTaskTypeEnum 列表
     */
    public static List<OpTaskTypeEnum> opManagerTask() {
        return Lists.newArrayList(GATEWAY_NEW, GATEWAY_CONFIG_EDIT, GATEWAY_EXPAND, GATEWAY_RESTART,
            GATEWAY_ROLLBACK, GATEWAY_SHRINK, GATEWAY_UPGRADE, ES_CLUSTER_CONFIG_EDIT,
            ES_CLUSTER_CONFIG_ROLLBACK, ES_CLUSTER_EXPAND, ES_CLUSTER_NEW, ES_CLUSTER_PLUG_INSTALL,
            ES_CLUSTER_PLUG_RESTART,  ES_CLUSTER_PLUG_UNINSTALL,
            ES_CLUSTER_PLUG_UPGRADE, ES_CLUSTER_RESTART, ES_CLUSTER_ROLLBACK, ES_CLUSTER_SHRINK,
            ES_CLUSTER_UPGRADE,GATEWAY_CONFIG_ROLLBACK,GATEWAY_OFFLINE,ES_CLUSTER_OFFLINE,
            ES_CLUSTER_PLUG_ROLLBACK);
    }

    public static OpTaskTypeEnum valueOfPath(String apiPath) {
        if (apiPath == null || "".equals(apiPath.trim())) {
            return OpTaskTypeEnum.UNKNOWN;
        }

        return Arrays.stream(OpTaskTypeEnum.values()).filter(typeEnum -> apiPath.equals(typeEnum.getApiPath()))
            .findAny().orElse(OpTaskTypeEnum.UNKNOWN);
    }

}