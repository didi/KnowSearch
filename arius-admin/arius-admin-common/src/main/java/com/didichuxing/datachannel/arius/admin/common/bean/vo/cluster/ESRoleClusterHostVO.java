package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * es集群节点信息,信息是同步自es集群
 * @author d06679
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "节点信息")
public class ESRoleClusterHostVO extends BaseVO {

    @ApiModelProperty("主键")
    private Long    id;

    @ApiModelProperty("集群角色Id")
    private Long    roleClusterId;

    @ApiModelProperty("主机名称")
    private String  hostname;

    @ApiModelProperty("主机ip")
    private String  ip;

    @ApiModelProperty("物理集群名称")
    private String  cluster;

    @ApiModelProperty("逻辑集群名称")
    private String clusterLogicNames;

    @ApiModelProperty("端口")
    private String  port;

    /**
     * 1data   2client    3master   4tribe
     * @see ESClusterNodeRoleEnum
     */
    @ApiModelProperty("角色(1data   2client    3master   4tribe)")
    private Integer role;

    @ApiModelProperty("状态（1 在线    2 离线   3 故障）")
    private Integer status;

    @ApiModelProperty("rack")
    private String  rack;

    @ApiModelProperty("节点规格")
    private String  machineSpec;

    @ApiModelProperty("set")
    private String  nodeSet;

    @ApiModelProperty("RegionId")
    private Long    regionId;

    @ApiModelProperty("逻辑划分")
    private String  logicDepart;
}