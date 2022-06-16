package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author linyunan
 * @date 2021-10-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "物理集群条件查询信息")
public class ClusterPhyConditionDTO extends ClusterPhyDTO {

    @ApiModelProperty("排序字段 diskUsagePercent")
    private String  sortTerm;

    private String sortType;

    @ApiModelProperty(value = "是否降序排序（默认降序）", dataType = "Boolean", required = false)
    private Boolean orderByDesc = true;

    @ApiModelProperty("集群名称，用于后端根据项目筛选")
    private List<String> clusterNames;
}
