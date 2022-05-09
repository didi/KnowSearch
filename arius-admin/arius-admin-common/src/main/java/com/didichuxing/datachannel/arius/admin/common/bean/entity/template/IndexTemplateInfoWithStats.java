package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 *
 * @author apsarazhouyunfan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexTemplateInfoWithStats extends IndexTemplateInfo {

    /**
     * 平均tps
     */
    private Double      avgTps;

    /**
     * 平均qps
     */
    private Double      avgQps;

    /**
     * 实际的磁盘消耗
     */
    private Double      actualDiskG;

    /**
     * 索引成本
     */
    private Double      cost;

    /**
     * 索引存储容量
     */
    private double      store;

    /**
     * 索引健康分
     */
    private Double      indexHealthDegree;

    /**
     * 索引价值分
     */
    private Double      indexValueDegree;

    /**
     * 不健康标签
     */
    private Set<String> unhealthyLabels;

}
