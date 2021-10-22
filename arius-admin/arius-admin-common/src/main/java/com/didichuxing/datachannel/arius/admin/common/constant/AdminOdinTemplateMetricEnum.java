package com.didichuxing.datachannel.arius.admin.common.constant;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum AdminOdinTemplateMetricEnum {
    TEMPLATE_QUOTA_DISK_USAGE(1,  "template.quota.disk.usage",                     "模板的磁盘quota使用率"),
    TEMPLATE_QUOTA_CPU_USAGE(2,   "template.quota.cpu.usage",                     "模板的CPU quota使用率"),
    TEMPLATE_QUOTA_DISK_USAGE_80(8,  "template.quota.disk.usage.80.percent",       "模板的磁盘quota使用率达到80%"),
    TEMPLATE_QUOTA_CPU_USAGE_80(9,   "template.quota.cpu.usage.80.percent",        "模板的CPU quota使用率达到80%");

    AdminOdinTemplateMetricEnum(int code, String metric, String desc) {
        this.code   = code;
        this.metric = metric;
        this.desc   = desc;
    }

    private int code;

    private String metric;

    private String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getMetric() {return metric;}

    public static String metricTemplte(){return "template";}

    public static List<AdminOdinTemplateMetricEnum> getAllAdminOdinMetric(){
        return Arrays.asList(AdminOdinTemplateMetricEnum.values());
    }

    public static List<String> getAllAdminOdinMetricName(){
        return getAllAdminOdinMetric().stream().map(a -> a.getMetric()).collect(Collectors.toList());
    }
}
