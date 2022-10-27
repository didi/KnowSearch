package com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
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
    //TODO 0.3.2 新增
    @ApiModelProperty("绑定 ecm 的组件 id")
    private Integer componentId;
    @ApiModelProperty("cpu使用率")
    private Double cpuUsage;
    @ApiModelProperty("当前节点http连接数")
    private Integer httpConnectionNum;
    @ApiModelProperty("节点名称")
    private String nodeName;
    

}