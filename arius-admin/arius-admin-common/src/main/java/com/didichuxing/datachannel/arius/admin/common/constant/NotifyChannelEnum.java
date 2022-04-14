package com.didichuxing.datachannel.arius.admin.common.constant;

/**
 * @author fitz
 * @date 2021/1/15 11:42 上午
 */
public enum NotifyChannelEnum {
    SMS("sms","短信"),
    VOICE("voice","电话"),
    EMAIL("email","邮件"),
    DINGTALK("dingtalk","钉钉"),
    WECOM("wecom","企业微信");

    NotifyChannelEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    private String value;
    private String text;

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static String val2Text(String value) {
        for (NotifyChannelEnum notifyGroupStatusEnum : NotifyChannelEnum.values()) {
            if (notifyGroupStatusEnum.getValue().equals(value)) {
                return notifyGroupStatusEnum.getText();
            }
        }
        return "";
    }
}
