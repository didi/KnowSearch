package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
@ApiModel(description ="容量规划Area信息")
public class CapacityPlanAreaDTO {

    /**
     * 主键
     */
    @ApiModelProperty("AreaID")
    private Long    id;

    /**
     * 集群名字
     */
    @ApiModelProperty("物理集群名称")
    private String  clusterName;

    /**
     * 逻辑资源的id
     */
    @ApiModelProperty("逻辑集群ID")
    private Long    resourceId;

    /**
     * 状态  1 规划中   2 暂停规划
     */
    @ApiModelProperty("状态（1 规划中；2 暂停规划）")
    private Integer status;

    /**
     * 配置
     */
    @ApiModelProperty("配置")
    private String  configJson;

}
