package com.didichuxing.datachannel.arius.admin.common.bean.po.ecm;


import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
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
public class ESRoleClusterHostPO extends BasePO {

    private Long id;

    /**
     * elastic_cluster外键ID
     */
    private Long roleClusterId;

    /**
     * 主机名或IP
     */
    private String hostname;

    /**
     * IP
     */
    private String ip;

    /**
     * 集群名称
     */
    private String cluster;

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
     * 节点名称
     */
    private String  nodeSet;

    /**
     * 机器规格 例如32C-64G-SSD-6T
     */
    private String machineSpec;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ESRoleClusterHostPO nodePO = (ESRoleClusterHostPO) o;

        if (!hostname.equals(nodePO.hostname)) {
            return false;
        }
        return cluster.equals(nodePO.cluster);

    }

    @Override
    public int hashCode() {
        int result = hostname.hashCode();
        result = 31 * result + cluster.hashCode();
        return result;
    }

    public String getKey() {
        return roleClusterId + "@" + ip + "@" + port;
    }
}
