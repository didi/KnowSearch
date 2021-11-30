package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

/**
 * Created by linyunan on 2021-08-01
 */
public enum ClusterPhyTypeMetricsEnum {
                                       /*** 未知*/
                                       UNKNOWN(-1, "未知"),
									   CLUSTER(1, "clusterPhyOverviewMetrics"),
	                                   NODE(2, "clusterPhyNodeMetrics"),
	                                   INDICES(3, "clusterPhyNodeIndicesMetrics");

    ClusterPhyTypeMetricsEnum(int code, String type) {
        this.code = code;
        this.type = type;
    }

    private int    code;

    private String type;

    public int getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public static ClusterPhyTypeMetricsEnum valueOfCode(Integer code) {
        if (null == code) {
            return ClusterPhyTypeMetricsEnum.UNKNOWN;
        }
        for (ClusterPhyTypeMetricsEnum typeEnum : ClusterPhyTypeMetricsEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }

        return ClusterPhyTypeMetricsEnum.UNKNOWN;
    }

    public static ClusterPhyTypeMetricsEnum valueOfType(String type) {
        if (null == type) {
            return ClusterPhyTypeMetricsEnum.UNKNOWN;
        }
        for (ClusterPhyTypeMetricsEnum typeEnum : ClusterPhyTypeMetricsEnum.values()) {
            if (type.equals(typeEnum.getType())) {
                return typeEnum;
            }
        }

        return ClusterPhyTypeMetricsEnum.UNKNOWN;
    }
}
