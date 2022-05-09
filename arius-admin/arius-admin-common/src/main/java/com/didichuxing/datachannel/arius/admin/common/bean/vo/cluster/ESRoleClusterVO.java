package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * ES集群表对应各角色主机列表
 * @author didi
 * @since 2020-08-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "ES角色集群信息")
public class ESRoleClusterVO extends BaseVO {
    private static final long serialVersionUID = 1L;

    private Long id;

    @ApiModelProperty("集群ID")
    private Long elasticClusterId;

    @ApiModelProperty("role集群名称")
    private String roleClusterName;

    /**
     *  集群角色(masternode/datanode/clientnode)
     * @see ESClusterNodeRoleEnum
     */
    @ApiModelProperty("集群角色(masternode/datanode/clientnode)")
    private String role;

    @ApiModelProperty("pod数量")
    private Integer podNumber;

    @ApiModelProperty("单机实例数")
    private Integer pidCount;

    @ApiModelProperty("机器规格")
    private String machineSpec;

    @ApiModelProperty("角色名下角色列表")
    private List<ESClusterRoleHostInfoVO> esClusterRoleHostInfoVO;

}

