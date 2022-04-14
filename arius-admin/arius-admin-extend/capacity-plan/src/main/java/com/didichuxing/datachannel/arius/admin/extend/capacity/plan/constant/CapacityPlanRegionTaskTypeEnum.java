package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant;

/**
 * 操作记录模块枚举
 *
 * Created by d06679 on 2017/7/14.
 */
public enum CapacityPlanRegionTaskTypeEnum {

                                            INIT(0, "初始化"),

                                            INCREASE(1, "扩容"),

                                            DECREASE(2, "缩容"),

                                            NORMAL(3, "常规");

    CapacityPlanRegionTaskTypeEnum(int code, String desc) {
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

    public static CapacityPlanRegionTaskTypeEnum valueOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (CapacityPlanRegionTaskTypeEnum state : CapacityPlanRegionTaskTypeEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return null;
    }

}
