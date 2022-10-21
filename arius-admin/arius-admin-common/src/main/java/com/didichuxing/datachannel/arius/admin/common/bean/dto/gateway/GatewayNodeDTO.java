package com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网关 dto
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 0.3.2
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("gatewayDTO")
public class GatewayNodeDTO {
    @ApiModelProperty("gateway 集群 id")
    private String  clusterName;
    @ApiModelProperty("集群健康")
    private Integer health;
    @ApiModelProperty("是否为 ecm 接入")
    private Boolean isWhetherECMAccess;
    @ApiModelProperty("是否为 ecm 接入")
    private Boolean memo;
    @ApiModelProperty("节点名称")
    private String nodeName;
}