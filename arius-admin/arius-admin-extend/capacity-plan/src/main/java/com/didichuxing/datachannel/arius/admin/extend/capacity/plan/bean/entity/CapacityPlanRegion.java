package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity;

import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.common.CapacityPlanConfig;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
public class CapacityPlanRegion {

    /**
     * 所属area
     */
    private Long               areaId;


    /**
     * region的ID
     */
    private Long               regionId;


    /**
     * 逻辑集群ID
     */
    private Long               logicClusterId;

    /**
     * 物理Region ID
     */
    // private Long               phyRegionId;

    /**
     * 集群名字
     */
    private String             clusterName;

    /**
     * racks
     */
    private String             racks;

    /**
     * 配置
     */
    private String             configJson;

    /**
     * 结构化的配置
     */
    private CapacityPlanConfig config;

    /**
     * region free的总量  单位为Docker
     */
    private Double             freeQuota;

    /**
     * 1 可以接工单  0 不可以接工单
     */
    private Integer            share;

    /**
     * 状态
     */
    // private Integer            status;

    /**
     * 利用率
     */
    private Double             usage;

    /**
     * 超卖比
     */
    private Double             overSold;

}
