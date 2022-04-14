package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant;

/**
 * 操作记录模块枚举
 * <p>
 *
 * @author d06679
 * @date 2017/7/14
 */
public enum CapacityPlanAreaStatusEnum {

    /**
     * 规划中
     */
    PLANING(1, "规划中"),

    /**
     * 暂停规划
     */
    SUSPEND(2, "规划暂停");

    CapacityPlanAreaStatusEnum(int code, String desc) {
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

    public static CapacityPlanAreaStatusEnum valueOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (CapacityPlanAreaStatusEnum state : CapacityPlanAreaStatusEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return null;
    }

}
