package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * ES集群表对应各角色主机列表
 *
 * @author didi
 * @since 2020-08-24
 */
@Data
public class ESRoleClusterHostDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    /**
     * elastic_cluster外键ID
     */
    @ApiModelProperty("集群角色Id")
    private Long roleClusterId;

    /**
     * 主机名
     */
    @ApiModelProperty("主机名称")
    private String hostname;

    /**
     * 主机ip
     */
    @ApiModelProperty("主机ip")
    private String ip;

    /**
     * 集群名称
     */
    @ApiModelProperty("集群名称")
    private String cluster;

    /**
     * es进程端口
     */
    @ApiModelProperty("端口")
    private String  port;

    /**
     * 1data   2client    3master   4tribe
     * @see ESClusterNodeRoleEnum
     */
    @ApiModelProperty("角色(1data   2client    3master   4tribe)")
    private Integer role;

    /**
     * 1 在线    2 离线   3 故障
     * @see ESClusterNodeStatusEnum
     */
    @ApiModelProperty("状态（1 在线    2 离线   3 故障）")
    private Integer status;

    /**
     * rack
     */
    @ApiModelProperty("节点rack信息")
    private String  rack;

    /**
     * set
     */
    @ApiModelProperty("节点set信息")
    private String  nodeSet;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ESRoleClusterHostDTO nodeDTO = (ESRoleClusterHostDTO) o;

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

