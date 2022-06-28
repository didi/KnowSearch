package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 详细介绍类情况.
 *
 * @ClassName ClusterVO
 * @Author gyp
 * @Date 2022/6/28
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "逻辑集群和物理集群对应关系")
public class ClusterLogicPhyRelationVO {

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("集群类型")
    private Integer type;

    @ApiModelProperty("集群名称")
    private String name;
}