package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

/**
 * Created by linyunan on 2021-08-01
 */
public enum ClusterPhyTypeMetricsEnum {
                                       /*** 未知*/
                                       UNKNOWN(-1, "未知", false),
									   CLUSTER(1, "clusterPhyOverviewMetrics", false),
	                                   NODE(2, "clusterPhyNodeMetrics", true),
	                                   INDICES(3, "clusterPhyIndicesMetrics", true),
                                       TEMPLATES(4, "clusterPhyTemplateMetrics", true),
                                       NODE_TASKS(5,"clusterPhyNodesTaskMetricsHandler", true);

    ClusterPhyTypeMetricsEnum(int code, String type, boolean collectCurveMetricsList) {
        this.code = code;
        this.type = type;
        this.collectCurveMetricsList = collectCurveMetricsList;
    }

    private final int    code;

    private final String type;

    private final boolean collectCurveMetricsList;

    public int getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public boolean isCollectCurveMetricsList() { return collectCurveMetricsList; }

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
