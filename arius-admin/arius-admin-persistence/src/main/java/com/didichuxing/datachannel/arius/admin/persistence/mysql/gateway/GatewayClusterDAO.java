package com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
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
    
    /**
     * 通过 id 获取一个 GatewayClusterVO
     *
     * @param id 网关集群的 ID。
     * @return 一个 GatewayClusterVO 对象。
     */
    GatewayClusterPO getOneById(@Param("id") Integer id);
    
    /**
     * 按 id 删除网关集群
     *
     * @param id 要删除的网关集群的id。
     * @return 一个布尔值。
     */
    boolean deleteOneById(@Param("id")Integer id);
    
    /**
     * 更新数据库中的指定对象
     *
     * @param obj2Obj 要更新的对象。
     * @return 布尔值
     */
    boolean updateOne(GatewayClusterPO obj2Obj);
    
    /**
     * 它返回具有给定 id 的集群的名称。
     *
     * @param id 网关集群的 ID。
     * @return 包含集群名称的字符串。
     */
    String getClusterNameById(@Param("id") Integer id);
    
    /**
     * > 该函数返回给定 id 的组件的组件 id
     *
     * @param id 组件的id
     * @return 具有给定 id 的组件的组件 id。
     */
    Integer getComponentIdById(@Param("id")Integer id);
    
    /**
     * 它按名称返回一个 GatewayClusterPO 对象。
     *
     * @param name 网关集群的名称
     * @return 一个 GatewayClusterPO 对象。
     */
    GatewayClusterPO getOneByName(@Param("name")String name);

    /**
     * 通过gateway集群名称获取gateway
     * @param gatewayClusterName
     * @return
     */
    List<GatewayClusterPO> listByNames(List<String> gatewayClusterName);

    /**
     * 通过 componentId 获取一个 GatewayClusterPO
     *
     * @param componentId 网关集群的组件ID。
     * @return GatewayClusterPO 对象
     */
    GatewayClusterPO getOneByComponentId(@Param("componentId")Integer componentId);
    
    /**
     * 它更新组件的版本。
     *
     * @param componentId 要更新的组件的 ID
     * @param version 要更新的组件的版本
     * @return 布尔值
     */
    Boolean updateVersion(@Param("componentId") Integer componentId,@Param("version") String version);
}