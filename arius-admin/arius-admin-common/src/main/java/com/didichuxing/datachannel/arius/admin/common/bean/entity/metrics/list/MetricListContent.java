package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.list;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-08-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricListContent {
    /**
     * 物理集群名称
     */
    private String clusterPhyName;

    /**
     * 名称: node Name / index name /template name
     */
    private String name;

    /**
     * 指标值, 某些指标项需要展示指标值, 可能为百分比, 考虑客户端做适配
     */
        private Double value;
}
