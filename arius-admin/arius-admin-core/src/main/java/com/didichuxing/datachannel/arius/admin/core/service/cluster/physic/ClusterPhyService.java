package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Plugin;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPhyPO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import java.util.List;
import java.util.Set;

/**
 * @author ohushenglin_v
 * @date 2022-05-30
 */
public interface ClusterPhyService {

    /**
     * 条件查询物理集群
     * @param params 条件
     * @return 集群列表
     *
     */
    List<ClusterPhy> listClustersByCondt(ClusterPhyDTO params);

    /**
     * 删除物理集群
     *
     * @param clusterId 集群id
     * @param projectId
     * @return 成功 true 失败 false
     */
    Result<Boolean> deleteClusterById(Integer clusterId, Integer projectId);

    /**
     * 新建物理集群
     *
     * @param param 集群信息
     * @return 成功 true 失败 false
     */
    Result<Boolean> createCluster(ClusterPhyDTO param);

    /**
     * 编辑物理集群信息
     * @param param 物理集群信息
     * @param operator 操作人
     * @return 成功 true 失败 false
     *
     */
    Result<Boolean> editCluster(ClusterPhyDTO param, String operator);

    /**
     * 根据集群名字查询集群
     * @param clusterName 集群名字
     * @return 集群
     */
    ClusterPhy getClusterByName(String clusterName);

    /**
     * 更新物理集群插件列表
     * @param pluginIds    插件id列表
     * @param phyClusterId 物理集群id
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> updatePluginIdsById(String pluginIds, Integer phyClusterId);

    /**
     * 列出所有集群
     * @return 集群列表,如果没有返回空列表
     */
    List<ClusterPhy> listAllClusters();

    /**
     * 获取所有集群名称列表
     *
     * @return {@link List}<{@link String}>
     */
    List<String> listClusterNames();

    /**
     * 根据names列出所有集群
     * @param names
     * @return 集群列表,如果没有返回空列表
     */
    List<ClusterPhy> listClustersByNames(List<String> names);

    /**
     * 集群是否存在
     *
     * @param clusterName 集群名字
     * @return true 存在
     */
    boolean isClusterExists(String clusterName);

    /**
     * 获取集群插件列表
     * @param cluster 集群名称
     * @return
     */
    List<Plugin> listClusterPlugins(String cluster);

    /**
     * 查询指定集群
     * @param phyClusterId 集群id
     * @return 集群  不存在返回null
     */
    ClusterPhy getClusterById(Integer phyClusterId);

    /**
     * 确保集群配置了DCDR的远端集群地址，如果没有配置尝试配置
     * @param cluster 集群
     * @param remoteCluster 远端集群
     * @throws ESOperateException
     * @return
     */
    boolean ensureDCDRRemoteCluster(String cluster, String remoteCluster) throws ESOperateException;

    /**
     * 更新集群的动态配置信息
     * @param param 更新集群下动态配置项的信息
     * @return result
     */
    Result<Boolean> updatePhyClusterDynamicConfig(ClusterSettingDTO param);

    /**
     * 获取集群路由的属性，例如rack1
     * @param cluster 集群名称
     * @return 属性的列表
     */
    Set<String> getRoutingAllocationAwarenessAttributes(String cluster);

    /**
     * 模糊分页查询物理集群列表信息，仅获取部分属性
     * @param param 参数
     * @return {@link List}<{@link ClusterPhy}>
     */
    List<ClusterPhy> pagingGetClusterPhyByCondition(ClusterPhyConditionDTO param);

    /**
     * 模糊查询统计总命中数
     * @param param 模糊查询条件
     * @return
     */
    Long fuzzyClusterPhyHitByCondition(ClusterPhyConditionDTO param);

    /**
     * 是否存在绑定指定安装包的集群
     *
     * @param packageId 安装包名
     * @return true or false
     */
    boolean isClusterExistsByPackageId(Long packageId);
		
		
		/**
		 * 它返回具有给定集群物理 ID 的组件的组件 ID。
		 *
		 * @param clusterPhyId 集群的物理标识。
		 * @return clusterPhyId 的组件 id
		 */
		Integer getComponentIdById(Integer clusterPhyId);
    
    /**
     * 如果集群具有具有给定组件 ID 的关系组件，则返回 true
     *
     * @param componentId 要检查的组件的组件 ID。
     * @return 一个布尔值。
     */
    boolean hasClusterRelationComponentId(Integer componentId);
    
    /**
     * 使用给定的 ID 更新组件的版本。
     *
     * @param componentId 您要更新的组件的 ID。
     * @param version 要更新的组件的版本。
     * @return 一个布尔值。
     */
    boolean updateVersion(Integer componentId, String version);
    
    /**
     * 它返回具有 componentId 的 ClusterPhyPO 对象。
     *
     * @param componentId 集群的组件 ID。
     * @return ClusterPhyPO 对象列表
     */
    ClusterPhyPO getOneByComponentId(Integer componentId);
    
    /**
     * 它返回网关所属的集群。
     *
     * @param gatewayId 集群的网关 ID。
     * @return ClusterPhyPO
     */
    	List<ClusterPhyPO> listClusterByGatewayId(Integer gatewayId);
}