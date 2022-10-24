package com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
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
@ApiModel(description = "gateway集群条件查询信息")
public class GatewayConditionDTO extends GatewayDTO {

    @ApiModelProperty("排序字段 diskUsagePercent")
    private String       sortTerm;

    private String       sortType;

    @ApiModelProperty(value = "是否降序排序（默认降序）", dataType = "Boolean", required = false)
    private Boolean      orderByDesc = true;
    
}