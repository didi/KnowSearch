package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author linyunan
 * @date 2021-03-15
 */
@Data
@ApiModel(description = "带有region信息的逻辑集群")
public class ESLogicClusterWithRegionDTO extends ESLogicClusterDTO{

    @ApiModelProperty("集群Region")
    private List<ClusterRegionDTO> clusterRegionDTOS;
}
