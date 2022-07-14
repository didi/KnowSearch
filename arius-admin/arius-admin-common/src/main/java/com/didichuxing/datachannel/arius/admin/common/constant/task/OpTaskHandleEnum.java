package com.didichuxing.datachannel.arius.admin.common.constant.task;

/**
 * @author ohushenglin_v
 * @date 2022-05-24
 */
public enum OpTaskHandleEnum {
                              /**新增*/
                              CLUSTER_NEW(OpTaskTypeEnum.CLUSTER_NEW.getType(), OpTaskConstant.CLUSTER_CREATE_TASK),

                              CLUSTER_EXPAND(OpTaskTypeEnum.CLUSTER_EXPAND.getType(),
                                             OpTaskConstant.CLUSTER_SCALE_TASK),

                              CLUSTER_SHRINK(OpTaskTypeEnum.CLUSTER_SHRINK.getType(),
                                             OpTaskConstant.CLUSTER_SCALE_TASK),

                              CLUSTER_RESTART(OpTaskTypeEnum.CLUSTER_RESTART.getType(),
                                              OpTaskConstant.CLUSTER_RESTART_TASK),

                              CLUSTER_UPGRADE(OpTaskTypeEnum.CLUSTER_UPGRADE.getType(),
                                              OpTaskConstant.CLUSTER_UPGRADE_TASK),

                              CLUSTER_PLUG_OPERATION(OpTaskTypeEnum.CLUSTER_PLUG_OPERATION.getType(),
                                                     OpTaskConstant.ECM_WORK_TASK),

                              CLUSTER_OFFLINE(OpTaskTypeEnum.CLUSTER_OFFLINE.getType(),
                                              OpTaskConstant.CLUSTER_OFFLINE_TASK),

                              TEMPLATE_DCDR(OpTaskTypeEnum.TEMPLATE_DCDR.getType(), OpTaskConstant.DCDR_WORK_TASK),

                              CLUSTER_CONFIG_ADD(OpTaskTypeEnum.CLUSTER_CONFIG_ADD.getType(),
                                                 OpTaskConstant.CLUSTER_CONFIG_RESTART_TASK),

                              CLUSTER_CONFIG_EDIT(OpTaskTypeEnum.CLUSTER_CONFIG_EDIT.getType(),
                                                  OpTaskConstant.ECM_WORK_TASK),

                              CLUSTER_CONFIG_DELETE(OpTaskTypeEnum.CLUSTER_CONFIG_DELETE.getType(),
                                                    OpTaskConstant.ECM_WORK_TASK),

                              UNKNOWN(OpTaskTypeEnum.UNKNOWN.getType(), OpTaskConstant.UNKNOWN);

    OpTaskHandleEnum(Integer type, String message) {
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

    public static OpTaskHandleEnum valueOfType(Integer type) {
        if (type == null) {
            return OpTaskHandleEnum.UNKNOWN;
        }
        for (OpTaskHandleEnum typeEnum : OpTaskHandleEnum.values()) {
            if (type.equals(typeEnum.getType())) {
                return typeEnum;
            }
        }

        return OpTaskHandleEnum.UNKNOWN;
    }

}

class OpTaskConstant {

    private OpTaskConstant() {
    }

    public static final String ECM_WORK_TASK               = "ecmOpTask";

    public static final String CLUSTER_CREATE_TASK         = "clusterCreateTask";

    public static final String CLUSTER_SCALE_TASK          = "clusterScaleTask";

    public static final String CLUSTER_RESTART_TASK        = "clusterRestartTask";

    public static final String CLUSTER_UPGRADE_TASK        = "clusterUpgradeTask";

    public static final String CLUSTER_OFFLINE_TASK        = "clusterOfflineTask";
    public static final String CLUSTER_CONFIG_RESTART_TASK = "clusterConfigRestartTask";

    public static final String DCDR_WORK_TASK              = "dcdrOpTask";
    public static final String UNKNOWN                     = "unknown";
}
