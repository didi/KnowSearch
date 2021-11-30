package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author lyn
 * @date 2021/09/26
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopMetrics {
    /**
     * 指标类型、节点维度指标、索引维度指标
     * see ClusterPhyNodeMetricsEnum
     * see ClusterPhyIndicesMetricsEnum
     */
    private String type;

    /**
     * top索引名称/节点名称
     */
    private List<String> topName;
}
