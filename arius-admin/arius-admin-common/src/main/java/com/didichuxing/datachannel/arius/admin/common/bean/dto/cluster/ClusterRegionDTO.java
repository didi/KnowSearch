package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-06-04
 */
@Data
@Builder()
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "集群Region")
public class ClusterRegionDTO {
    @ApiModelProperty("主键")
    private Long   id;

    @ApiModelProperty("region 名称")
    private String name;

    @ApiModelProperty("逻辑集群ID")
    private String logicClusterIds;

    @ApiModelProperty("物理集群名称")
    private String phyClusterName;

    @ApiModelProperty("配置项")
    private String config;
}