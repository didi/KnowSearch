package com.didichuxing.datachannel.arius.admin.persistence.mysql.plugin;

import com.didichuxing.datachannel.arius.admin.common.bean.po.plugin.PluginInfoPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 插件 dao
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Repository
public interface PluginDAO {
		
		/**
		 * 它从数据库中删除一条记录。
		 *
		 * @param id 表的主键。
		 * @return 整数
		 */
		int deleteByPrimaryKey(Long id);
		
		/**
		 * 向表中插入一条新记录。
		 *
		 * @param record 要插入的对象
		 * @return 受插入影响的行数。
		 */
		int insert(PluginInfoPO record);
		
		
		/**
		 * 通过主键从表中选择一行。
		 *
		 * @param id 表的主键。
		 * @return 一个 PluginInfoPO 对象
		 */
		PluginInfoPO selectByPrimaryKey(Long id);
		
		
		/**
		 * 更新数据库中的记录。
		 *
		 * @param record 要更新的对象。
		 * @return 整数
		 */
		int updateByPrimaryKeySelective(PluginInfoPO record);
		
		
		/**
		 * > 将 PluginInfo 对象列表插入数据库
		 *
		 * @param list 要插入的数据列表。
		 * @return 受插入影响的行数。
		 */
		int batchInsert(@Param("list") List<PluginInfoPO> list);
		
		/**
		 * 它返回与给定的 clusterId 和 clusterType 关联的 PluginInfoPO 对象的列表。
		 *
		 * @param clusterId 要查询的集群的集群ID。
		 * @param clusterType 集群类型，1：es，2：gateway
		 * @return 列表<PluginInfoPO>
		 */
		List<PluginInfoPO> listByClusterIdAndClusterType(@Param("clusterId")Integer clusterId, @Param("clusterType")Integer clusterType);
		
		/**
		 * 它从表中删除一条记录。
		 *
		 * @param clusterId 集群ID
		 * @param clusterType ES GATEWAY
		 * @param componentId 要删除的组件的组件 ID。
		 */
		boolean deleteByClusterIdAndClusterTypeAndComponentId(@Param("clusterId")Integer clusterId, @Param("clusterType") Integer clusterType, @Param("componentId") Integer componentId);
		
		/**
		 *
		 *
		 * @param clusterId 集群编号
		 * @param clusterType 集群类型，1：k8s，2：mesos
		 * @param componentId 插件的组件 ID。
		 */
		PluginInfoPO selectByClusterIdAndComponentIdAndClusterType(@Param("clusterId")Integer clusterId, @Param("clusterType") Integer clusterType, @Param("componentId") Integer componentId);
		
		/**
		 * 按条件选择
		 *
		 * @param pluginInfoPO 插件信息po
		 * @return {@link PluginInfoPO}
		 */
		PluginInfoPO selectByCondition(PluginInfoPO pluginInfoPO);
		
		/**
		 * 选择一个通过组件id和集群类型
		 *
		 * @param componentId 组件id
		 * @param clusterType 集群类型
		 * @return {@link PluginInfoPO}
		 */
		PluginInfoPO selectOneByComponentIdAndClusterType(@Param("componentId")Integer componentId,  @Param("clusterType") Integer clusterType);
}