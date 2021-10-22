package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by linyunan on 2021-05-26
 */
@Data
@ApiModel(description = "物理集群划分节点信息")
public class ESClusterPhyRegionInfoVO {

    @ApiModelProperty("regionId")
    private Long    regionId;

    @ApiModelProperty("逻辑集群名称")
    private String  logicClusterName;

    @ApiModelProperty("rack")
    private String  rack;

    @ApiModelProperty("主机ip")
    private String  ip;

    /** @see ESClusterNodeRoleEnum */
    @ApiModelProperty("角色(1 DataNode  2 ClientNode  3 MasterNode)")
    private Integer role;

    @ApiModelProperty("状态（1 在线    2 离线   3 故障）")
    private Integer status;
}
