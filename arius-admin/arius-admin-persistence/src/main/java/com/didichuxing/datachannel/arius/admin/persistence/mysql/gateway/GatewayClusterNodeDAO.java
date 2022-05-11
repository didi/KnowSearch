package com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterNodePO;

/**
 * @author d06679
 */
@Repository
public interface GatewayClusterNodeDAO {

    /**
     * 插入数据
     * @param gatewayClusterNodePO 对应po类
     * @return int
     */
    int recordGatewayNode(GatewayClusterNodePO gatewayClusterNodePO);

    /**
     * 根据集群名和时间获取存活的节点数
     * @param clusterName 集群名
     * @param time 时间
     * @return int
     */
    int aliveCountByClusterNameAndTime(@Param("clusterName") String clusterName,
                                       @Param("time") Date time);

    /**
     * 根据集群名和时间获取列表
     * @param clusterName 集群名
     * @param time 时间
     * @return List<GatewayClusterNodePO> 列表
     */
    List<GatewayClusterNodePO> listAliveNodeByClusterNameAndTime(@Param("clusterName") String clusterName,
                                                                 @Param("time") Date time);

}
