package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "集群Region信息")
public class ClusterRegionVO extends BaseVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("region 名称")
    private String name;

    @ApiModelProperty("逻辑集群ID")
    private String logicClusterIds;

    @ApiModelProperty("物理集群名称")
    private String clusterName;

    @ApiModelProperty("配置项")
    private String config;
}
