package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.vo;

import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.common.CapacityPlanConfig;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
@ApiModel(description = "规划region信息")
public class CapacityPlanRegionVO {

    /**
     * 主键
     */
    @ApiModelProperty("regionId")
    private Long               id;

    /**
     * 物理Region ID
     */
    private Long               phyRegionId;

    /**
     *
     */
    @ApiModelProperty("areaId")
    private Long               areaId;

    /**
     * 集群名字
     */
    @ApiModelProperty("物理集群名称")
    private String             clusterName;

    /**
     * rack
     */
    @ApiModelProperty("rack")
    private String             racks;

    /**
     * region free的总量  单位为Docker
     */
    @ApiModelProperty("空闲资源")
    private Double             freeQuota;

    /**
     * 配置
     */
    @ApiModelProperty("配置")
    private String             configJson;

    /**
     * 结构化的配置
     */
    @ApiModelProperty("配置")
    private CapacityPlanConfig config;

    /**
     * 1 可以接工单  0 不可以接工单
     */
    @ApiModelProperty("属性（1 可以接工单；0 不可以接工单）")
    private Integer            share;

    /**
     * 利用率
     */
    @ApiModelProperty("利用率")
    private Double             usage;

    /**
     * 超卖比
     */
    @ApiModelProperty("炒卖比")
    private Double             overSold;

}
