package com.didichuxing.datachannel.arius.admin.common.constant.task;

import java.util.Arrays;

import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmTaskTypeEnum;

/**
 * 任务枚举类型
 *
 * @author ohushenglin_v
 * @date 2022-05-20
 */
public enum OpTaskTypeEnum {
                            /**新增*/
                            CLUSTER_NEW(EcmTaskTypeEnum.NEW.getCode(), "集群新建", "cluster-create"),

                            CLUSTER_EXPAND(EcmTaskTypeEnum.EXPAND.getCode(), "集群扩容", "cluster-expand"),

                            CLUSTER_SHRINK(EcmTaskTypeEnum.SHRINK.getCode(), "集群缩容", "cluster-shrink"),

                            CLUSTER_RESTART(EcmTaskTypeEnum.RESTART.getCode(), "集群重启", "cluster-restart"),

                            CLUSTER_UPGRADE(EcmTaskTypeEnum.UPGRADE.getCode(), "集群升级", "cluster-upgrade"),

                            CLUSTER_PLUG_OPERATION(EcmTaskTypeEnum.PLUG_OPERATION.getCode(), "集群插件操作",
                                                   "cluster-plug-operation"),

                            CLUSTER_OFFLINE(EcmTaskTypeEnum.OFFLINE.getCode(), "集群下线操作", "cluster-offline"),

                            TEMPLATE_DCDR(10, "索引模板主从切换", "template-dcdr"),

                            CLUSTER_CONFIG_ADD(EcmTaskTypeEnum.CONFIG_ADD.getCode(), "集群配置新增", "cluster-config-add"),

                            CLUSTER_CONFIG_EDIT(EcmTaskTypeEnum.CONFIG_EDIT.getCode(), "集群配置编辑", "cluster-config-edit"),

                            CLUSTER_CONFIG_DELETE(EcmTaskTypeEnum.CONFIG_DELETE.getCode(), "集群配置删除",
                                                  "cluster-config-delete"),

                            UNKNOWN(EcmTaskTypeEnum.UNKNOWN.getCode(), "unknown");

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