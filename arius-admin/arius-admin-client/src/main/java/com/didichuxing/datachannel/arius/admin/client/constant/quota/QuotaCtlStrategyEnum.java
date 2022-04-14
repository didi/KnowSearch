package com.didichuxing.datachannel.arius.admin.client.constant.quota;

/**
 * quota管控策略
 *
 * Created by d06679 on 2017/7/14.
 */
public enum QuotaCtlStrategyEnum {
                                  /**增大*/
                                  INCREASE(10, "增大"),

                                  NOT_ADJUST(20, "不变"),

                                  DECREASE(30, "减小"),

                                  FORBID_SINK(40, "写入禁止");

    QuotaCtlStrategyEnum(int code, String desc) {
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

}
