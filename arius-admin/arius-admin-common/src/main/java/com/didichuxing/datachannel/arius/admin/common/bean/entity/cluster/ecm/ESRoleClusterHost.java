package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm;


import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import lombok.Data;

/**
 * ES集群表对应各角色主机列表
 *
 * @author didi
 * @since 2020-10-20
 */
@Data
public class ESRoleClusterHost extends BaseEntity {

    private Long id;

    /**
    * elastic_cluster外键ID
    */
    private Long roleClusterId;

    /**
    * 主机名
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
     * set 冷热存
     */
    private String  nodeSet;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ESRoleClusterHost node = (ESRoleClusterHost) o;

        if (!hostname.equals(node.hostname)) {
            return false;
        }
        return cluster.equals(node.cluster);

    }

    @Override
    public int hashCode() {
        int result = hostname.hashCode();
        result = 31 * result + cluster.hashCode();
        return result;
    }
}
