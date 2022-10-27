package com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网关集群 VO
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 0.3.2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "gateway集群信息")
public class GatewayClusterVO {
    private Integer id;
    @ApiModelProperty("gateway 集群 id")
    private String  clusterName;
    @ApiModelProperty("绑定 ecm 的组件 id")
    private Integer componentId;
    @ApiModelProperty("集群健康:-1:unknown;1.green;2.yellow;3.red")
    private Integer health;
    @ApiModelProperty("是否为 ecm 接入")
    private Boolean whetherECMAccess;
    @ApiModelProperty("代理地址")
    private String  proxyAddress;
    @ApiModelProperty("版本")
    private String  version;
    @ApiModelProperty("节点列表信息")
    private List<GatewayClusterNodeVO> nodes;
    @ApiModelProperty("备注")
    private String memo;
    
    
}