package com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 网关节点 dto
 *
 * @author shizeying
 * @date 2022/10/31
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("gateway节点DTO")
public class GatewayNodeHostDTO extends BaseDTO {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty("主键")
    private Long id;
    
    @ApiModelProperty("主机名称")
    private String hostName;
    
    @ApiModelProperty(value = "gateway集群名称",hidden = true)
    private String clusterName;
    
    @ApiModelProperty("端口")
    private Integer port;
    @ApiModelProperty("机器规格")
    private String machineSpec;
    
    
}