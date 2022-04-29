package com.didichuxing.datachannel.arius.admin.common.bean.common;

import javax.annotation.Nullable;

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
public class PhysicalTemplateTpsMetric {

    /**
     * 历史每小时tps峰值 单位 条/s
     * 是取各个物理模板最大值
     */
    private Double maxTps;

    /**
     * 每个物理模板，最近15分钟平均值 单位 条/s
     *
     */
    private Double currentTps;

    /**
     * 当前写入失败的格式
     * 这个值只针对ingestPipeline写入的有效；当有值的时候，表示模板被限流了
     */
    @Nullable
    private Double currentFailCount;

}
