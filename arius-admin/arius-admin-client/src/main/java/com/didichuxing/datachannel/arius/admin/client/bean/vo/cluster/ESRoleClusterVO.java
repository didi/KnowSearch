package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * ES集群表对应各角色主机列表
 * @author didi
 * @since 2020-08-24
 */
@Data
@ApiModel(description = "ES角色集群信息")
public class ESRoleClusterVO extends BaseVO {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 集群ID
     */
    @ApiModelProperty("集群ID")
    private Long elasticClusterId;

    /**
     * role集群名称
     */
    @ApiModelProperty("role集群名称")
    private String roleClusterName;

    /**
     *  集群角色(masternode/datanode/clientnode)
     * @see ESClusterNodeRoleEnum
     */
    @ApiModelProperty("集群角色(masternode/datanode/clientnode)")
    private String role;

    /**
     * pod数量
     */
    @ApiModelProperty("pod数量")
    private Integer podNumber;

    /**
     * 单机实例数
     */
    @ApiModelProperty("单机实例数")
    private Integer pidCount;

    /**
     * 机器规格
     */
    @ApiModelProperty("机器规格")
    private String machineSpec;

    /**
     * 角色名下角色列表
     */
    @ApiModelProperty("角色名下角色列表")
    private List<ESRoleClusterHostVO> esRoleClusterHostVO;

}

