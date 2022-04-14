package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant;

/**
 * 操作记录模块枚举
 *
 * Created by d06679 on 2017/7/14.
 */
public enum CapacityPlanRegionTaskStatusEnum {

                                              NO_FREE_RACK(1, "集群资源不足"),

                                              OP_ES_ERROR(2, "操作es失败"),

                                              DATA_MOVING(3, "数据搬迁中"),

                                              EXE_PENDING(4, "待执行"),

                                              FINISHED(5, "完成");

    CapacityPlanRegionTaskStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int    code;

    private String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CapacityPlanRegionTaskStatusEnum valueOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (CapacityPlanRegionTaskStatusEnum state : CapacityPlanRegionTaskStatusEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return null;
    }

}
