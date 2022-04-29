package com.didichuxing.datachannel.arius.admin.common.bean.common;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogicTemplateTpsMetric {

    /**
     * 历史每小时tps峰值 单位 条/s
     * 是取各个物理模板最大值
     */
    private Double                          maxTps;

    /**
     * 最大值对应的小时时间
     */
    private String                          maxTpsTimestamp;

    /**
     * 最大值对应的索引模板物理ID
     */
    private Long                            maxTpsTemplateId;

    /**
     * 每个物理模板，最近15分钟平均值 单位 条/s
     *
     */
    private Map<Long/*templateId*/, Double> currentTpsMap;

    /**
     * 每个物理模板，最近一段时间失败次数的平均值 单位 条/s
     */
    private Map<Long/*templateId*/, Double> currentFailCountMap;

}
