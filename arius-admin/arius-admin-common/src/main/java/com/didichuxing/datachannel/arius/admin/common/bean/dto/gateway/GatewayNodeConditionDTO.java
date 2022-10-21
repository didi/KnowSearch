package com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author linyunan
 * @date 2021-10-14
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "gateway节点集群条件查询信息")
public class GatewayNodeConditionDTO extends GatewayNodeDTO {

    @ApiModelProperty("排序字段 ")
    private String       sortTerm;

    private String       sortType;

    @ApiModelProperty(value = "是否降序排序（默认降序）", dataType = "Boolean", required = false)
    private Boolean      orderByDesc = true;
    
}