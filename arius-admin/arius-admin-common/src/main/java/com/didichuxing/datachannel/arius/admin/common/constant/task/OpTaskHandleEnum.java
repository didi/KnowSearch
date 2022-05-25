package com.didichuxing.datachannel.arius.admin.common.constant.task;

import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmTaskTypeEnum;
import org.apache.tomcat.util.http.parser.Upgrade;

/**
 * @author ohushenglin_v
 * @date 2022-05-24
 */
public enum OpTaskHandleEnum {
    /**新增*/
    CLUSTER_NEW(1, OpTaskConstant.CLUSTER_CREATE_TASK),

    CLUSTER_EXPAND(2, OpTaskConstant.CLUSTER_INDECREASE_TASK),

    CLUSTER_SHRINK(3, OpTaskConstant.CLUSTER_INDECREASE_TASK),

    CLUSTER_RESTART(4, OpTaskConstant.CLUSTER_RESTART_TASK),

    CLUSTER_UPGRADE(5, OpTaskConstant.CLUSTER_UPGRADE_TASK),

    CLUSTER_PLUG_OPERATION(6, OpTaskConstant.ECM_WORK_TASK),

    CLUSTER_OFFLINE(7, OpTaskConstant.CLUSTER_OFFLINE_TASK),

    TEMPLATE_DCDR(10, OpTaskConstant.DCDR_WORK_TASK),

    CLUSTER_CONFIG_ADD(11, OpTaskConstant.CLUSTER_CONFIG_RESTART_TASK),

    CLUSTER_CONFIG_EDIT(12, OpTaskConstant.ECM_WORK_TASK),

    CLUSTER_CONFIG_DELETE(13, OpTaskConstant.ECM_WORK_TASK),

    UNKNOWN(-1, OpTaskConstant.UNKNOWN);

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

    private OpTaskConstant() {}

    public static final String ECM_WORK_TASK     = "ecmOpTask";

    public static final String CLUSTER_CREATE_TASK     = "clusterCreateTask";

    public static final String CLUSTER_INDECREASE_TASK     = "clusterIndecreaseTask";

    public static final String CLUSTER_RESTART_TASK     = "clusterRestartTask";

    public static final String CLUSTER_UPGRADE_TASK   = "clusterUpgradeTask";

    public static final String CLUSTER_OFFLINE_TASK   = "clusterOfflineTask";
    public static final String CLUSTER_CONFIG_RESTART_TASK   = "clusterConfigRestartTask";

    public static final String DCDR_WORK_TASK    = "dcdrOpTask";
    public static final String UNKNOWN           = "unknown";
}
