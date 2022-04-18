package com.didichuxing.datachannel.arius.admin.common.constant.task;

public enum WorkTaskHandleEnum {
                                /**新增*/
                                CLUSTER_NEW(1, WorkTaskConstant.ECM_WORK_TASK),

                                CLUSTER_EXPAND(2, WorkTaskConstant.ECM_WORK_TASK),

                                CLUSTER_SHRINK(3, WorkTaskConstant.ECM_WORK_TASK),

                                CLUSTER_RESTART(4, WorkTaskConstant.ECM_WORK_TASK),

                                CLUSTER_UPGRADE(5, WorkTaskConstant.ECM_WORK_TASK),

                                CLUSTER_PLUG_OPERATION(6, WorkTaskConstant.ECM_WORK_TASK),

                                TEMPLATE_DCDR(10, WorkTaskConstant.DCDR_WORK_TASK),

                                CLUSTER_CONFIG_ADD(11, WorkTaskConstant.ECM_WORK_TASK),

                                CLUSTER_CONFIG_EDIT(12, WorkTaskConstant.ECM_WORK_TASK),

                                CLUSTER_CONFIG_DELETE(13, WorkTaskConstant.ECM_WORK_TASK),

                                UNKNOWN(-1, WorkTaskConstant.UNKNOWN);

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

class WorkTaskConstant {

    private WorkTaskConstant() {}

    public static final String ECM_WORK_TASK     = "ecmWorkTask";
    public static final String DCDR_WORK_TASK    = "dcdrWorkTask";
    public static final String UNKNOWN           = "unknown";
}
