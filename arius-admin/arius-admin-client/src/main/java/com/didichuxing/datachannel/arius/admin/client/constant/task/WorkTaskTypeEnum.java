package com.didichuxing.datachannel.arius.admin.client.constant.task;

public enum WorkTaskTypeEnum {
                              /**新增*/
                              CLUSTER_NEW(1, "集群新增"),

                              CLUSTER_EXPAND(2, "集群扩容"),

                              CLUSTER_SHRINK(3, "集群缩容"),

                              CLUSTER_RESTART(4, "集群重启"),

                              CLUSTER_UPGRADE(5, "集群升级"),

                              CLUSTER_PLUG_OPERATION(6, "集群插件操作"),

                              CLUSTER_OFFLINE(7, "集群下线操作"),

                              TEMPLATE_DCDR(10, "索引模板主从切换"),

                              CLUSTER_CONFIG_ADD(11, "集群配置新增"),

                              CLUSTER_CONFIG_EDIT(12, "集群配置编辑"),

                              CLUSTER_CONFIG_DELETE(13, "集群配置删除"),

                              UNKNOWN(-1, "unknown");

    WorkTaskTypeEnum(Integer type, String message) {
        this.type = type;
        this.message = message;
    }

    private Integer type;

    private String  message;

    public Integer getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public static WorkTaskTypeEnum valueOfType(Integer type) {
        if (type == null) {
            return WorkTaskTypeEnum.UNKNOWN;
        }
        for (WorkTaskTypeEnum typeEnum : WorkTaskTypeEnum.values()) {
            if (type.equals(typeEnum.getType())) {
                return typeEnum;
            }
        }

        return WorkTaskTypeEnum.UNKNOWN;
    }

}
