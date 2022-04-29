package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity;

import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateMetaMetric;

import lombok.Data;
import lombok.ToString;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
@ToString
public class CapacityPlanRegionBalanceItem {

    /**
     * 主键
     */
    private Long               areaId;

    /**
     * 集群名字
     */
    private Long               templateId;

    /**
     * 源region
     */
    private CapacityPlanRegion srcRegion;

    /**
     * 目标region
     */
    private CapacityPlanRegion tgtRegion;

    /**
     * 指标
     */
    private TemplateMetaMetric template;

}
