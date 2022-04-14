package com.didichuxing.datachannel.arius.admin.common.constant;

/**
 * @author fitz
 * @date 2021/1/15 11:42 上午
 */
public enum NotifyGroupStatusEnum {
    DELETE(-1,"删除"),
    DISABLE(0,"停用"),
    ENABLE(1,"启用");

    NotifyGroupStatusEnum(Integer value, String text) {
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
        for (NotifyGroupStatusEnum notifyGroupStatusEnum : NotifyGroupStatusEnum.values()) {
            if (notifyGroupStatusEnum.getValue() == value) {
                return notifyGroupStatusEnum.getText();
            }
        }
        return "";
    }
}
