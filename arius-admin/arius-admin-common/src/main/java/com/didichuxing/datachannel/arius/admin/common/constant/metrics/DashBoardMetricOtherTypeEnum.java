package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Created by linyunan on 3/11/22
 */
public enum DashBoardMetricOtherTypeEnum {
    UNKNOWN("", "未知"),
    CLUSTER_HEALTH("clusterPhyHealth", "健康度");

    DashBoardMetricOtherTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    private String type;
    private String desc;

    public String getType() {
        return type;
    }
    public String getDesc() {
        return desc;
    }

    public static boolean hasExist(String metricsType) {
        if (null == metricsType) { return false;}
        for (DashBoardMetricOtherTypeEnum typeEnum : DashBoardMetricOtherTypeEnum.values()) {
            if (metricsType.equals(typeEnum.getType())) { return true;}
        }

        return false;
    }

    public static DashBoardMetricOtherTypeEnum valueOfType(String type) {
        if (null == type) { return DashBoardMetricOtherTypeEnum.UNKNOWN;}

        for (DashBoardMetricOtherTypeEnum typeEnum : DashBoardMetricOtherTypeEnum.values()) {
            if (type.equals(typeEnum.getType())) { return typeEnum;}
        }

        return DashBoardMetricOtherTypeEnum.UNKNOWN;
    }

    public static List<DashBoardMetricOtherTypeEnum> valueOfTypes(List<String> types) {
        List<DashBoardMetricOtherTypeEnum> resList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(types)) { return resList;}

        for (String s : types) {
            if (null == s) { continue;}

            for (DashBoardMetricOtherTypeEnum typeEnum : DashBoardMetricOtherTypeEnum.values()) {
                if (s.equals(typeEnum.getType())) { resList.add(typeEnum);}
            }
        }

        return resList;
    }
}
