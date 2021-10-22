package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.common;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.RackMetaMetric;
import com.didichuxing.datachannel.arius.admin.client.bean.common.RegionMetric;
import com.didichuxing.datachannel.arius.admin.client.bean.common.TemplateMetaMetric;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegion;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-25
 */
@Data
public class CapacityPlanRegionContext {

    private Integer                  taskType;

    /**
     * region信息
     */
    private CapacityPlanRegion       region;

    /**
     * 模板指标
     */
    private List<TemplateMetaMetric> templateMetaMetrics;

    /**
     * 模板指标
     */
    private List<RackMetaMetric>     rackMetas;

    /**
     * region信息
     */
    private RegionMetric             regionMetric;

    /**
     * 磁盘消耗
     */
    private Double                   regionCostDiskG;

    /**
     * CPU消耗
     */
    private Double                   regionCostCpuCount;

    public CapacityPlanRegionContext(Integer taskType, CapacityPlanRegion region) {
        this.taskType = taskType;
        this.region = region;
    }
}
