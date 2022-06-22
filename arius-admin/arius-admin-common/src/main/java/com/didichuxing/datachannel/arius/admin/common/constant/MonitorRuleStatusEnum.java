package com.didichuxing.datachannel.arius.admin.common.constant;

/**
 * @author fitz
 * @date 2021/1/15 11:42 上午
 */
@Deprecated
public enum MonitorRuleStatusEnum {
    DELETE(-1,"删除"),
    ENABLE(0,"启用"),
    DISABLE(1,"禁用");

    MonitorRuleStatusEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    private Integer value;
    private String text;

    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static String val2Text(int value) {
        for (MonitorRuleStatusEnum notifyGroupStatusEnum : MonitorRuleStatusEnum.values()) {
            if (notifyGroupStatusEnum.getValue() == value) {
                return notifyGroupStatusEnum.getText();
            }
        }
        return "";
    }
}