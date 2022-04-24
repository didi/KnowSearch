package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String       type;

    /**
     * 指标看板top信息 集群名称/节点名称/模板名称/索引名称等列表
     */
    private List<String> topNames;
}
