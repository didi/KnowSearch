package com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.annotation.security.DenyAll;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 网关节点 dto
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("gateway节点DTO")
public class GatewayNodeHostDTO extends BaseDTO {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty("主键")
    private Long id;
    
    @ApiModelProperty("主机名称")
    private String hostname;
    
    @ApiModelProperty("gateway集群名称")
    private String clusterName;
    
    @ApiModelProperty("端口")
    private String port;
    
    
}