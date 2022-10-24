package com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * gateway 集群简要信息
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 0.3.2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "gateway 集群简要信息")
public class GatewayClusterBriefVO {
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("gateway 集群名称")
    private String  clusterName;
    @ApiModelProperty("绑定ecm的组件id")
    private Integer componentId;
}