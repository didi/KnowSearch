package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectClusterLogicAuthEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by linyunan on 2021-10-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "逻辑集群条件查询信息")
public class ClusterLogicConditionDTO extends ESLogicClusterDTO {
    /**
     * @see ProjectClusterLogicAuthEnum
     */
    @ApiModelProperty("权限类型 1:配置管理, 2:访问, -1:无权限")
    private Integer      authType;

    @ApiModelProperty("排序字段，type，disk_usage_percent,es_cluster_version,data_node_num")
    private String       sortTerm;

    @ApiModelProperty("DESC,ASC")
    private String       sortType;

    @ApiModelProperty(value = "是否降序排序（默认降序）", dataType = "Boolean", required = false)
    private Boolean      orderByDesc = true;

    @ApiModelProperty("集群名称，用于后端根据项目筛选")
    private List<String> clusterNames;
}