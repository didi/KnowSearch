package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant;

/**
 * 操作记录模块枚举
 * <p>
 *
 * @author d06679
 * @date 2017/7/14
 */
public enum CapacityPlanRegionTaskEnum {

    /**
     * 初始化
     */
    INIT(0, "初始化"),

    /**
     * 规划中
     */
    PLAN(1, "plan"),

    /**
     * Check中
     */
    CHECK(2, "check");

    CapacityPlanRegionTaskEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int code;

    private String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CapacityPlanRegionTaskEnum valueOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (CapacityPlanRegionTaskEnum state : CapacityPlanRegionTaskEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return null;
    }

}
