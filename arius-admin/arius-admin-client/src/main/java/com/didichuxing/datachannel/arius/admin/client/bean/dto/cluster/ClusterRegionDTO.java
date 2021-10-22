package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by linyunan on 2021-06-04
 */

@Data
@ApiModel(description = "集群Region")
public class ClusterRegionDTO {

	@ApiModelProperty("主键")
    private Long   id;

	@ApiModelProperty("逻辑集群ID")
    private Long   logicClusterId;

	@ApiModelProperty("物理集群名称")
    private String phyClusterName;

	@ApiModelProperty("Rack列表")
    private String racks;
}
