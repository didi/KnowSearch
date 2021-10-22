package com.didichuxing.datachannel.arius.admin.common.constant.template;

import static javax.management.timer.Timer.ONE_DAY;

/**
 * @author didi
 */
public enum TemplateLabelPeriodEnum {

    FOREVER("1", "永久", -1L),

    ONE_MONTH("2", "一个月", ONE_DAY * 30),

    THREE_MONTH("3", "三个月", ONE_DAY * 90);


    private String id;

    private String period;

    private Long intervalMillis;


    TemplateLabelPeriodEnum(String id, String period, Long intervalMillis) {
        this.id = id;
        this.period = period;
        this.intervalMillis = intervalMillis;
    }

    public String getId() {
        return id;
    }


    public String getPeriod() {
        return period;
    }


    public Long getIntervalMillis() {
        return intervalMillis;
    }}
