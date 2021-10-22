package com.didichuxing.datachannel.arius.admin.client.constant.task;

public enum WorkTaskHandleEnum {
                                /**新增*/
                                CLUSTER_NEW(1, "ecmWorkTask"),

                                CLUSTER_EXPAND(2, "ecmWorkTask"),

                                CLUSTER_SHRINK(3, "ecmWorkTask"),

                                CLUSTER_RESTART(4, "ecmWorkTask"),

                                CLUSTER_UPGRADE(5, "ecmWorkTask"),

                                CLUSTER_PLUG_OPERATION(6, "ecmWorkTask"),

                                TEMPLATE_DCDR(10, "dcdrWorkTask"),

                                CLUSTER_CONFIG_ADD(11, "ecmWorkTask"),

                                CLUSTER_CONFIG_EDIT(12, "ecmWorkTask"),

                                CLUSTER_CONFIG_DELETE(13, "ecmWorkTask"),

                                UNKNOWN(-1, "unknown");

    WorkTaskHandleEnum(Integer type, String message) {
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

    public static WorkTaskHandleEnum valueOfType(Integer type) {
        if (type == null) {
            return WorkTaskHandleEnum.UNKNOWN;
        }
        for (WorkTaskHandleEnum typeEnum : WorkTaskHandleEnum.values()) {
            if (type.equals(typeEnum.getType())) {
                return typeEnum;
            }
        }

        return WorkTaskHandleEnum.UNKNOWN;
    }

}
