package com.didichuxing.datachannel.arius.admin.common.constant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author fitz
 * @date 2021/1/15 11:42 上午
 */
@Deprecated
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum MonitorCategoryEnum {
    PHY_CLUSTER("cluster","物理集群", Lists.newArrayList("es.cluster.cpu.usage", "es.cluster.disk.usage", "es.cluster.health.pendingTask")),
    PHY_ES_TEMPLATE("template","物理模版", Lists.newArrayList("es.indices.indexing.index_total_rate", "es.indices.indexing.index_time_in_millis")),
    PHY_NODE("node","节点", Lists.newArrayList(
            "es.node.os.cpu.percent",
            "es.node.fs.total.disk_free_percent",
            "es.node.transport.tx_size_in_bytes_rate",
            "es.node.transport.rx_size_in_bytes_rate",
            "es.node.thread_pool.bulk.rejected",
            "es.node.thread_pool.search.rejected")),
    UNKNOWN("unknown","未知", Lists.newArrayList());

    MonitorCategoryEnum(String value, String text, List<String> metrics) {
        this.value = value;
        this.text = text;
        this.metrics = metrics;
    }

    private String value;
    private String text;
    private List<String> metrics;

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public List<String> getMetrics() {
        return metrics;
    }

    public static String val2Text(String value) {
        for (MonitorCategoryEnum monitorCategoryEnum : MonitorCategoryEnum.values()) {
            if (monitorCategoryEnum.getValue().equals(value)) {
                return monitorCategoryEnum.getText();
            }
        }
        return UNKNOWN.getText();
    }

    public static MonitorCategoryEnum findByValue(String value) {
        for (MonitorCategoryEnum monitorCategoryEnum : MonitorCategoryEnum.values()) {
            if (monitorCategoryEnum.getValue().equals(value)) {
                return monitorCategoryEnum;
            }
        }
        return UNKNOWN;
    }

}