package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linyunan
 * @date 2021-03-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "带有region信息的逻辑集群")
public class ESLogicClusterWithRegionDTO extends ESLogicClusterDTO {

    @ApiModelProperty("集群Region")
    private List<ClusterRegionDTO> clusterRegionDTOS;
     @ApiModelProperty(value = "招行内置",hidden = true)
    private Boolean bindExistLogicCluster;
    
}