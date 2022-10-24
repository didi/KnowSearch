package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lyn
 * @date 2022/5/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "带节点信息的集群Region实体")
public class ClusterRegionWithNodeInfoVO extends ClusterRegionVO {
    @ApiModelProperty("划分到region的节点名称, 用逗号分隔")
    private String nodeNames;
}
