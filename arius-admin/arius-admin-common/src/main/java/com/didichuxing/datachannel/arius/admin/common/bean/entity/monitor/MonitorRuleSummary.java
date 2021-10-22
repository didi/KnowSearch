package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import lombok.Data;

@Data
public class MonitorRuleSummary {
    private Long id;

    private String name;

    private Integer appId;

    private String appName;

    private String principals;

    private String operator;

    private Long createTime;
}