package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lyn
 * @date 2022/5/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "带region信息的节点实体")
public class ESClusterRoleHostWithRegionInfoVO extends ESClusterRoleHostVO {
    @ApiModelProperty("Region名称")
    private String regionName;
}
