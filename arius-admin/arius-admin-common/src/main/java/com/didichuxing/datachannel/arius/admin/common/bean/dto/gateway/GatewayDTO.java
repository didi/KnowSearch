package com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 网关 dto
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 0.3.2
 */

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("gatewayDTO")
public class GatewayDTO extends PageDTO {
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty(value = "gateway 集群：支持查询")
    private String  clusterName;
    @ApiModelProperty("集群健康")
    private Integer health;
    @ApiModelProperty(value = "是否为 ecm 接入",hidden = true)
    private Boolean ecmAccess;
    @ApiModelProperty(value = "备注",hidden = true)
    private String memo;
}