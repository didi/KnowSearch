package com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "节点信息")
public class GatewayClusterNodeVO extends BaseVO {

    @ApiModelProperty("节点ID")
    private Integer id;

    @ApiModelProperty("集群名称")
    private String  clusterName;

    @ApiModelProperty("主机名")
    private String  hostName;

    @ApiModelProperty("端口")
    private Integer port;

    @ApiModelProperty("上报时间")
    private Date    heartbeatTime;

}
