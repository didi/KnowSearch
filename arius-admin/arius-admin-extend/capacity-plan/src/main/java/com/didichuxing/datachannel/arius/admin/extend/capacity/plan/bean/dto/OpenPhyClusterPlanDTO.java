package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 开启集群容量规划DTO
 * @author wangshu
 * @date 2020/10/13
 */
@Data
@ApiModel(description = "开启物理集群容量规划功能")
public class OpenPhyClusterPlanDTO {
    /**
     * 集群名字
     */
    @ApiModelProperty("物理集群名称")
    private String cluster;
}
