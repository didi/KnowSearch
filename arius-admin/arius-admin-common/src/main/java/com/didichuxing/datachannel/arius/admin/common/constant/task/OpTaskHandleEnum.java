package com.didichuxing.datachannel.arius.admin.common.constant.task;

/**
 * @author ohushenglin_v
 * @date 2022-05-24
 */
public enum OpTaskHandleEnum {
    /**
     * TODO 关于ES的相关操作无需改动，这里需要保留至下个迭代之后才可以进行下线，所以这里的枚举类是不能改动的
     */
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
    //gateway相关操作
    GATEWAY_NEW(OpTaskTypeEnum.GATEWAY_NEW.getType(), OpTaskConstant.GATEWAY_CREATE),
    GATEWAY_EXPAND(OpTaskTypeEnum.GATEWAY_EXPAND.getType(), OpTaskConstant.GATEWAY_EXPAND),
    GATEWAY_SHRINK(OpTaskTypeEnum.GATEWAY_SHRINK.getType(), OpTaskConstant.GATEWAY_SHRINK),
    GATEWAY_RESTART(OpTaskTypeEnum.GATEWAY_RESTART.getType(), OpTaskConstant.GATEWAY_RESTART),
    GATEWAY_UPGRADE(OpTaskTypeEnum.GATEWAY_UPGRADE.getType(), OpTaskConstant.GATEWAY_UPGRADE),
    GATEWAY_CONFIG_EDIT(OpTaskTypeEnum.GATEWAY_CONFIG_EDIT.getType(),
        OpTaskConstant.GATEWAY_CONFIG_EDIT),
    GATEWAY_CONFIG_ROLLBACK(OpTaskTypeEnum.GATEWAY_CONFIG_ROLLBACK.getType(),
        OpTaskConstant.GATEWAY_CONFIG_ROLLBACK),
    GATEWAY_ROLLBACK(OpTaskTypeEnum.GATEWAY_ROLLBACK.getType(), OpTaskConstant.GATEWAY_ROLLBACK),
    GATEWAY_OFFLINE(OpTaskTypeEnum.GATEWAY_OFFLINE.getType(), OpTaskConstant.GATEWAY_OFFLINE),
    //ES相关操作 0.3.2
    ES_CLUSTER_NEW(OpTaskTypeEnum.ES_CLUSTER_NEW.getType(), OpTaskConstant.ES_CLUSTER_CREATE),

    ES_CLUSTER_EXPAND(OpTaskTypeEnum.ES_CLUSTER_EXPAND.getType(), OpTaskConstant.ES_CLUSTER_EXPAND),

    ES_CLUSTER_SHRINK(OpTaskTypeEnum.ES_CLUSTER_SHRINK.getType(), OpTaskConstant.ES_CLUSTER_SHRINK),

    ES_CLUSTER_RESTART(OpTaskTypeEnum.ES_CLUSTER_RESTART.getType(), OpTaskConstant.ES_CLUSTER_RESTART),

    ES_CLUSTER_UPGRADE(OpTaskTypeEnum.ES_CLUSTER_UPGRADE.getType(), OpTaskConstant.ES_CLUSTER_UPGRADE),


    ES_CLUSTER_CONFIG_EDIT(OpTaskTypeEnum.ES_CLUSTER_CONFIG_EDIT.getType(), OpTaskConstant.ES_CLUSTER_CONFIG_EDIT),

    ES_CLUSTER_PLUG_INSTALL(OpTaskTypeEnum.ES_CLUSTER_PLUG_INSTALL.getType(), OpTaskConstant.ES_CLUSTER_PLUG_INSTALL),
    ES_CLUSTER_PLUG_UNINSTALL(OpTaskTypeEnum.ES_CLUSTER_PLUG_UNINSTALL.getType(), OpTaskConstant.ES_CLUSTER_PLUG_UNINSTALL),
    ES_CLUSTER_PLUG_UPGRADE(OpTaskTypeEnum.ES_CLUSTER_PLUG_UPGRADE.getType(), OpTaskConstant.ES_CLUSTER_PLUG_UPGRADE),
    ES_CLUSTER_PLUG_RESTART(OpTaskTypeEnum.ES_CLUSTER_PLUG_RESTART.getType(), OpTaskConstant.ES_CLUSTER_PLUG_RESTART),
    ES_CLUSTER_ROLLBACK(OpTaskTypeEnum.ES_CLUSTER_ROLLBACK.getType(), OpTaskConstant.ES_CLUSTER_ROLLBACK),
    ES_CLUSTER_CONFIG_ROLLBACK(OpTaskTypeEnum.ES_CLUSTER_CONFIG_ROLLBACK.getType(), OpTaskConstant.ES_CLUSTER_CONFIG_ROLLBACK),
    ES_CLUSTER_OFFLINE(OpTaskTypeEnum.ES_CLUSTER_OFFLINE.getType(), OpTaskConstant.ES_CLUSTER_OFFLINE),

                              FAST_INDEX(OpTaskTypeEnum.FAST_INDEX.getType(),
                                         OpTaskConstant.FAST_INDEX_TASK),

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
    public static final String GATEWAY_CREATE             = "gatewayCreateTask";
    public static final String GATEWAY_EXPAND = "gatewayExpandTask";
    public static final String GATEWAY_SHRINK = "gatewayShrinkTask";
    public static final String GATEWAY_RESTART = "gatewayRestartTask";
    public static final String GATEWAY_UPGRADE = "gatewayUpgradeTask";
    public static final String GATEWAY_CONFIG_EDIT = "gatewayConfigEditTask";
    public static final String GATEWAY_CONFIG_ROLLBACK = "gatewayConfigRollbackTask";
    public static final String GATEWAY_ROLLBACK = "gatewayRollbackTask";
    public static final String ES_CLUSTER_CREATE          = "esClusterCreateTask";
    public static final String ES_CLUSTER_EXPAND          = "esClusterExpandTask";
    public static final String ES_CLUSTER_SHRINK          = "esClusterShrinkTask";
    public static final String ES_CLUSTER_RESTART         = "esClusterRestartTask";
    public static final String ES_CLUSTER_UPGRADE         = "esClusterUpgradeTask";
    public static final String ES_CLUSTER_CONFIG_EDIT     = "esClusterConfigEditTask";
    public static final String ES_CLUSTER_PLUG_INSTALL    = "esClusterPluginInstallTask";
    public static final String ES_CLUSTER_PLUG_UNINSTALL  = "esClusterPluginUninstallTask";
    public static final String ES_CLUSTER_PLUG_UPGRADE    = "esClusterPluginUpgradeTask";
    public static final String ES_CLUSTER_PLUG_RESTART    = "esClusterPluginRestartTask";
    public static final String ES_CLUSTER_ROLLBACK        = "esClusterRollbackTask";
    public static final String ES_CLUSTER_CONFIG_ROLLBACK = "esClusterConfigRollbackTask";
    public static final String GATEWAY_OFFLINE            = "gatewayOfflineTask";
    public static final String ES_CLUSTER_OFFLINE         = "esClusterOfflineTask";

    public static final String FAST_INDEX_TASK             = "fastIndexOpTask";

    public static final String UNKNOWN                     = "unknown";
}