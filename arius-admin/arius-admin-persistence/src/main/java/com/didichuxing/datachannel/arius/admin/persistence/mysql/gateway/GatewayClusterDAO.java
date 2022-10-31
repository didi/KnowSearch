package com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterPO;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author d06679
 */
@Repository
public interface GatewayClusterDAO {

    /**
     * 插入数据
     * @param param po
     * @return int
     */
    int insert(GatewayClusterPO param);

    /**
     * 查询所有数据
     * @return List<GatewayClusterPO>
     */
    List<GatewayClusterPO> listAll();
    
    /**
     * 按 clusterName 选择一个 GatewayClusterPO
     *
     * @param clusterName 集群名称
     * @return 一个 GatewayClusterPO 对象。
     */
    GatewayClusterPO selectOneByClusterName(String clusterName);
    
    /**
     * > 列出所有符合给定条件的网关集群
     *
     * @param condition 网关条件DTO
     * @return List<GatewayClusterVO>
     */
    List<GatewayClusterPO> listByCondition(GatewayConditionDTO condition);
   
    /**
     * > 计算符合给定条件的记录数
     *
     * @param condition 用于过滤数据的条件。
     * @return 符合条件的行数。
     */
    Long countByCondition(GatewayConditionDTO condition);
}