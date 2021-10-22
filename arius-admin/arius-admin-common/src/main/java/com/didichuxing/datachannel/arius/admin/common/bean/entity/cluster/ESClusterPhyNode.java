package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;

import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import lombok.Data;

/**
 * es集群节点信息,信息是同步自es集群
 * @author d06679
 */
@Data
public class ESClusterPhyNode extends BaseEntity {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 节点名字
     */
    private String  name;

    /**
     * 集群
     */
    private String  cluster;

    /**
     * IP地址
     */
    private String  ipAddress;

    /**
     * es进程端口
     */
    private String  port;

    /**
     * 1data   2client    3master   4tribe
     * @see ESClusterNodeRoleEnum
     */
    private Integer role;

    /**
     * 1 在线    2 离线   3 故障
     * @see ESClusterNodeStatusEnum
     */
    private Integer status;

    /**
     * rack
     */
    private String  rack;

    /**
     * set
     */
    private String  nodeSet;

    /**
     * odin名字
     */
    private String  odinName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ESClusterPhyNode nodePO = (ESClusterPhyNode) o;

        if (!name.equals(nodePO.name)) {
            return false;
        }
        return cluster.equals(nodePO.cluster);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + cluster.hashCode();
        return result;
    }

}