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
@ApiModel(description = "规划area信息")
public class CapacityPlanAreaVO {

    /**
     * 主键
     */
    @ApiModelProperty("AreaID")
    private Long               id;

    /**
     * 集群名字
     */
    @ApiModelProperty("物理集群名称")
    private String             clusterName;

    /**
     * 逻辑资源的id
     */
    @ApiModelProperty("逻辑集群ID")
    private Long               resourceId;

    /**
     * 逻辑资源名字
     */
    @ApiModelProperty("逻辑集群名称")
    private String             resourceName;

    /**
     * 状态  1 规划中   2 暂停规划
     */
    @ApiModelProperty("装填（1 规划中；2 暂停规划）")
    private Integer            status;

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
     * 利用率
     */
    @ApiModelProperty("利用率")
    private Double             usage;

    /**
     * 超卖率
     */
    @ApiModelProperty("超卖比")
    private Double             overSold;

    /**
     * 空闲的rack
     */
    @ApiModelProperty("空闲资源")
    private String             freeRacks;

}
