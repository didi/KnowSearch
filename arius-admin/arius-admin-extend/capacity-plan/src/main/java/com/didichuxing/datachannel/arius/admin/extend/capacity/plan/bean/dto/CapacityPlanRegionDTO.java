package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
@ToString
@ApiModel(description = "规划region信息")
public class CapacityPlanRegionDTO extends BaseDTO {

    /**
     * 所属area
     */
    private Long areaId;

    /**
     * 主键
     */
    @ApiModelProperty("regionId")
    private Long regionId;

    /**
     * 逻辑集群ID
     */
    @ApiModelProperty("logicClusterId")
    private Long logicClusterId;

    /**
     * 集群名字
     */
    @ApiModelProperty("物理集群名称")
    private String clusterName;

    /**
     * rack
     */
    @ApiModelProperty("rack")
    private String racks;

    /**
     * region free的总量  单位为Docker
     */
    @ApiModelProperty("空闲quota")
    private Double freeQuota;

    /**
     * 配置
     */
    @ApiModelProperty("配置")
    private String configJson;

    /**
     * 1 可以接工单  0 不可以接工单
     */
    @ApiModelProperty("属性（1 可以接工单；0 不可以接工单）")
    private Integer share;

    /**
     * 利用率
     */
    @ApiModelProperty("容量使用率")
    private Double usage;

}
