package com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterNodePO;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

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
    int aliveCountByClusterNameAndTime(@Param("clusterName") String clusterName, @Param("time") Date time);

    /**
     * 根据集群名和时间获取列表
     * @param clusterName 集群名
     * @param time 时间
     * @return List<GatewayClusterNodePO> 列表
     */
    List<GatewayClusterNodePO> listAliveNodeByClusterNameAndTime(@Param("clusterName") String clusterName,
                                                                 @Param("time") Date time);
    
    /**
     * 将 GatewayClusterNodePO 对象插入数据库
     *
     * @param gatewayClusterNodePO 要插入的对象。
     * @return 受插入影响的行数。
     */
    int insert(GatewayClusterNodePO gatewayClusterNodePO);
    
    /**
     * 按 clusterNames 选择所有 GatewayClusterNodePO
     *
     * @param clusterNames 要查询的集群名称。
     * @return 列表<GatewayClusterNodePO>
     */
    List<GatewayClusterNodePO> selectByBatchClusterName(@Param("clusterNames") List<String> clusterNames);
    
    /**
     * 按 clusterName 选择所有 GatewayClusterNodePO 记录
     *
     * @param clusterName 集群名称
     * @return GatewayClusterNodePO 对象列表
     */
    List<GatewayClusterNodePO> selectByClusterName(@Param("clusterName") String clusterName);
    
    /**
     * 从表中删除 cluster_name 列等于 clusterName 参数的所有记录
     *
     * @param clusterName 要删除的集群的名称。
     */
    boolean deleteByClusterName(@Param("clusterName") String clusterName);
    
    /**
     * > 按条件查询网关集群节点列表
     *
     * @param condition 条件对象是查询的条件对象。
     * @return List<GatewayClusterNodePO>
     */
    List<GatewayClusterNodePO> listByCondition(GatewayNodeConditionDTO condition);
    
    /**
     * > 按指定条件统计GatewayNode记录数
     *
     * @param condition 查询的条件。
     * @return Long
     */
    Long countByCondition(GatewayNodeConditionDTO condition);
}