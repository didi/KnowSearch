package com.didichuxing.datachannel.arius.admin.common.constant;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author fitz
 * @date 2021/08/25 11:42 上午
 */
@Deprecated
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum MonitorOperatorEnum {
    GT("gt","大于"),
    LT("lt","小于"),
    EQ("eq","等于"),
    GTE("gte","大于等于"),
    LTE("lte","小于等于"),
    NE("ne","不等于");

    MonitorOperatorEnum(String value, String text) {
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

}