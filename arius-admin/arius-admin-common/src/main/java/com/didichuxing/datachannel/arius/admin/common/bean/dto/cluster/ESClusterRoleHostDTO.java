package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESClusterRoleHostDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("集群角色Id")
    private Long roleClusterId;

    @ApiModelProperty("主机名称")
    private String hostname;

    @ApiModelProperty("主机ip")
    private String ip;

    @ApiModelProperty("集群名称")
    private String cluster;

    @ApiModelProperty("端口")
    private String  port;

    @ApiModelProperty("冷热节点标识,是热节点则设置为false")
    private Boolean beCold;

    /**
     * @see ESClusterNodeRoleEnum
     */
    @ApiModelProperty("角色(1data   2client    3master)")
    private Integer role;

    /**
     * @see ESClusterNodeStatusEnum
     */
    @ApiModelProperty("状态（1 在线    2 离线   3 故障）")
    private Integer status;

    @ApiModelProperty("节点rack信息")
    private String  rack;

    @ApiModelProperty("节点set信息")
    private String  nodeSet;

    @ApiModelProperty("regionId")
    private Integer regionId;

    @ApiModelProperty("attributes, 逗号分隔 key1:value1,key2:value2")
    private String attributes;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ESClusterRoleHostDTO nodeDTO = (ESClusterRoleHostDTO) o;

        if (!hostname.equals(nodeDTO.hostname)) {
            return false;
        }
        return cluster.equals(nodeDTO.cluster);

    }

    @Override
    public int hashCode() {
        int result = hostname.hashCode();
        result = 31 * result + cluster.hashCode();
        return result;
    }
}

