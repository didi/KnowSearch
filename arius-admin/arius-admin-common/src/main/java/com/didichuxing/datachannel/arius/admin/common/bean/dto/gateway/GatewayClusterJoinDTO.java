package com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 网关接入
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("gateway集群DTO")
public class GatewayClusterJoinDTO extends BaseDTO {
    
    @ApiModelProperty("集群名字")
    private String clusterName;
    
    @ApiModelProperty("集群角色 对应主机列表")
    private List<GatewayNodeHostDTO>  gatewayNodeHosts;
    
    @ApiModelProperty("描述")
    private String memo;
    
    @ApiModelProperty("代理地址")
    private String proxyAddress;
    
    @ApiModelProperty("数据中心")
    private String dataCenter;
   
    @ApiModelProperty("机器规格")
    private String machineSpec;
}