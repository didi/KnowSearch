package com.didichuxing.datachannel.arius.admin.common.constant;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author fitz
 * @date 2021/1/15 11:42 上午
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum MonitorStatsTypeEnum {
    PAPPEN("happen","发生次数"),
    MAX("max","最大值"),
    MIN("min","最小值"),
    AVG("avg","均值"),
    SUM("sum","求和"),
    DIFF("diff","突增突降值"),
    PDIFF("pdiff","突增突降率"),
    C_AVG_RATE_ABS("c_avg_rate_abs","同比变化率"),
    C_AVG_ABS("c_avg_abs","同比变化值");

    MonitorStatsTypeEnum(String value, String text) {
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
