package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterConnectionStatusWithTemplateEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterDynamicConfigsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleThree;

/**
 *
 * @author ohushenglin_v
 * @date 2022-05-10
 */
public interface ClusterPhyManager {

    /**
     * 对集群下所有模板执行拷贝索引的mapping到模板操作
     * @param cluster 集群
     * @param retryCount 重试次数
     * @return true/false
     */
    boolean copyMapping(String cluster, int retryCount);
    TupleThree</*dcdrExist*/Boolean,/*pipelineExist*/ Boolean,/*existColdRegion*/ Boolean> getDCDRAndPipelineAndColdRegionTupleByClusterPhyWithCache(String clusterPhy);
     /**
      * 它返回集群和缓存之间的连接状态。
      *
      * @param clusterPhy 集群的物理名称。
      * @return 正在返回 ClusterConnectionStatusWithTemplateEnum。
      */
     ClusterConnectionStatusWithTemplateEnum getClusterConnectionStatusWithCache(String clusterPhy);
    

    /**
     * 同步元数据
     * @param cluster    集群名称
     * @param retryCount 重试次数
     * @return
     */
    void syncTemplateMetaData(String cluster, int retryCount) throws ESOperateException;

    /**
     * 集群是否存在
     * @param clusterName 集群名字
     * @return true 存在
     */

    boolean isClusterExists(String clusterName);

    /**
     * 获取控制台物理集群信息列表(ZH有使用)
     * @param param 查询参数
     * @return 物理集群列表
     */
    List<ClusterPhyVO> listClusterPhys(ClusterPhyDTO param);

    /**
     * 构建客户端需要的数据
     *
     * @param clusterPhyList  集群列表源数据
     * @return
     */
    List<ClusterPhyVO> buildClusterInfo(List<ClusterPhy> clusterPhyList);

    /**
     * 获取单个物理集群overView信息
     * @param clusterId 物理集群id
     * @param currentProjectId 当前登录项目
     * @return 物理集群信息
     */
    ClusterPhyVO getClusterPhyOverview(Integer clusterId, Integer currentProjectId);

    /**
     * 获取逻辑集群可关联region的物理集群名称列表
     * @param clusterLogicType 逻辑集群类型
     * @param clusterLogicId   逻辑集群Id
     * @see ClusterResourceTypeEnum
     * @return 物理集群名称
     */
    Result<List<String>> listCanBeAssociatedRegionOfClustersPhys(Integer clusterLogicType, Long clusterLogicId);

    /**
     * 获取新建逻辑集群可关联的物理集群名称
     * @param clusterLogicType  逻辑集群类型
     * @see ClusterResourceTypeEnum
     * @return 物理集群名称
     */
    Result<List<String>> listCanBeAssociatedClustersPhys(Integer clusterLogicType);

    /**
     * 集群接入
     *
     * @param param     逻辑集群Id, 物理集群名称
     * @param operator  操作人
     * @param projectId
     * @return ClusterPhyVO
     */
    Result<ClusterPhyVO> joinCluster(ClusterJoinDTO param, String operator, Integer projectId);

    /**
     * 删除接入集群 删除顺序: region ——> clusterLogic ——> clusterHost ——> clusterRole  ——> cluster
     *
     * @param clusterId 集群id
     * @param operator  操作人
     * @param projectId
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> deleteClusterJoin(Integer clusterId, String operator, Integer projectId);

    /**
     * 插件列表
     *
     * @param cluster 集群
     * @return {@link Result}<{@link List}<{@link PluginVO}>>
     */
    Result<List<PluginVO>> listPlugins(String cluster);

    /**
     * 获取集群下的动态配置信息
     * @param cluster 物理集群的名称
     * @return 动态配置信息 Map中的String见于动态配置的字段，例如cluster.routing.allocation.awareness.attributes
     */
    Result<Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>> getPhyClusterDynamicConfigs(String cluster);

    /**
     * 更新集群下的动态配置信息
     *
     * @param param     配置信息参数
     * @param operator
     * @param projectId
     * @return result
     */
    Result<Boolean> updatePhyClusterDynamicConfig(ClusterSettingDTO param, String operator, Integer projectId);

    /**
     * 获取集群下的属性配置
     * @param cluster 集群名称
     * @return result
     */
    Result<Set<String>> getRoutingAllocationAwarenessAttributes(String cluster);

    /**
     * 获取APP有管理、读写、读权限的物理集群名称列表
     *
     * @param projectId projectId
     * @return {@link List}<{@link String}>
     */
    List<String> listClusterPhyNameByProjectId(Integer projectId);

    /**
     * 根据模板所在集群，获取与该集群相同版本号的集群名称列表
     * @param projectId      projectId
     * @param templateId 模板id
     * @return {@link Result}<{@link List}<{@link String}>>
     */
    Result<List<String>> getTemplateSameVersionClusterNamesByTemplateId(Integer projectId, Integer templateId);
    
    Result<List<String>> getTemplateSameVersionClusterNamesByTemplateIdExistDCDR(Integer projectId, Integer templateId);


    /**
     * 获取物理集群节点名称列表
     * @param clusterPhyName 集群phy名称
     * @return {@link List}<{@link String}>
     */
    List<String> listClusterPhyNodeName(String clusterPhyName);

    /**
     * 构建单个物理集群统计信息
     * @param cluster 集群
     */
    void buildPhyClusterStatics(ClusterPhyVO cluster);

    /**
     * 获取APP可查看的物理集群节点名称列表
     * @param projectId projectId
     * @return {@link List}<{@link String}>
     */
    List<String> listNodeNameByProjectId(Integer projectId);

    /**
     * 物理集群信息删除 (host信息、角色信息、集群信息、region信息)
     *
     * @param clusterPhyId 物理集群ID
     * @param operator     操作人
     * @param projectId
     * @return {@link Result}<{@link Boolean}>
     */
    Result<Boolean> deleteCluster(Integer clusterPhyId, String operator, Integer projectId);

    /**
     * 添加集群
     *
     * @param param    参数
     * @param operator 操作人
     * @param projectId    projectId
     * @return {@link Result}<{@link Boolean}>
     */
    Result<Boolean> addCluster(ClusterPhyDTO param, String operator, Integer projectId);

    /**
     * 编辑集群
     *
     * @param param    参数
     * @param operator 操作人
     * @return {@link Result}<{@link Boolean}>
     */
    Result<Boolean> editCluster(ClusterPhyDTO param, String operator);

    /**
     * 条件组合、分页查询
     * @param condition
     * @param projectId
     * @return
     */
    PaginationResult<ClusterPhyVO> pageGetClusterPhys(ClusterPhyConditionDTO condition,
                                                      Integer projectId) throws NotFindSubclassException;

    /**
     * 根据projectId获取超级项目下的物理集群列表
     * @param projectId 项目id
     * @return Result<List<String>>
     */
    Result<List<String>> listClusterPhyNameBySuperApp(Integer projectId);

    /**
     * 构建物理集群角色信息
     * @param cluster
     */
    void buildClusterRole(ClusterPhyVO cluster);

    /**
     * 构建集群作用
     *
     * @param cluster      集群
     * @param clusterRoleInfos 集群角色
     */
    void buildClusterRole(ClusterPhyVO cluster, List<ClusterRoleInfo> clusterRoleInfos);

    /**
     * 更新物理集群状态
     * @param clusterPhyName   物理集群名称
     * @param operator         操作者
     * @return
     */
    boolean updateClusterHealth(String clusterPhyName, String operator);

    /**
     * 更新集群资源信息
     * @param cluster
     * @param operator
     * @return
     */
    boolean updateClusterInfo(String cluster, String operator);

    /**
     * 校验集群状态是否有效
     * @param clusterPhyName
     * @param operator
     * @return
     */
    Result<Boolean> checkClusterHealth(String clusterPhyName, String operator);

    /**
     * 集群是否存在
     * @param clusterPhyName
     * @param operator
     * @return
     */
    Result<Boolean> checkClusterIsExit(String clusterPhyName, String operator);

    /**
     * 删除存在集群
     * @param clusterPhyName
     * @param projectId
     * @param operator
     * @return
     */
    Result<Boolean> deleteClusterExit(String clusterPhyName, Integer projectId, String operator);

    /**
     *  根据逻辑集群类型和已选中的物理集群名称筛选出es版本一致的物理集群名称列表
     *  @param hasSelectedClusterNameWhenBind 用户在新建逻辑集群阶段已选择的物理集群名称
     *  @param clusterLogicType 逻辑集群类型
     *  @return 同版本的物理集群名称列表
     */
    Result<List<String>> getPhyClusterNameWithSameEsVersion(Integer clusterLogicType,
                                                            String hasSelectedClusterNameWhenBind);

    /**
     * 根据已经创建的逻辑集群id筛选出物理集群版本一致的物理集群名称列表
     * @param clusterLogicId 逻辑集群id
     * @return 同版本的物理集群名称列表
     */
    Result<List<String>> getPhyClusterNameWithSameEsVersionAfterBuildLogic(Long clusterLogicId);

    /**
     * 更新集群网关
     *
     * @param param    参数
     * @param operator 操作人
     * @return {@link Result}<{@link ClusterPhyVO}>
     */
    Result<ClusterPhyVO> updateClusterGateway(ClusterPhyDTO param, String operator);

    /**
     * 根据集群ID获取物理集群角色
     *
     * @param clusterId 集群id
     * @return {@link List}<{@link ClusterRoleInfo}>
     */
    List<ClusterRoleInfo> listClusterRolesByClusterId(Integer clusterId);

    /**
     * 根据集群名称获获取集群节点列表
     *
     * @param cluster 集群名称
     * @return {@link List}<{@link ClusterRoleHost}>
     */
    List<ClusterRoleHost> listClusterRoleHostByCluster(String cluster);
    /**
     * 按照资源类型查询物理集群名称列表
     *
     * @param clusterResourceType 集群资源类型
     * @param projectId           项目id
     * @return {@link Result}<{@link List}<{@link String}>>
     */
    Result<List<String>> listClusterPhyNameByResourceType(Integer clusterResourceType, Integer projectId);
    
    Result<ClusterPhy> getClusterByName(String masterCluster);
    
    boolean ensureDCDRRemoteCluster(String cluster, String remoteCluster) throws ESOperateException;
    
    /**
     * 它返回满足条件的总数。
     *
     * @param condition 查询的条件。
     * @return 长
     */
    Long fuzzyClusterPhyHitByCondition(ClusterPhyConditionDTO condition);
    
    /**
     * 按条件获取集群物理信息
     *
     * @param condition 查询的条件。
     * @return 列表<ClusterPhy>
     */
    List<ClusterPhy> pagingGetClusterPhyByCondition(ClusterPhyConditionDTO condition);


    /**
     * 批量更新物理集群的动态配置项
     * @param param        要更新的配置项
     * @param operator
     * @param projectId
     * @return
     */
    Result<Boolean> batchUpdateClusterDynamicConfig(MultiClusterSettingDTO param, String operator, Integer projectId) throws ESOperateException;
		
		/**
		 * 验证给定的集群名称是唯一的。
		 *
		 *
		 * @param clusterName 要创建的集群的名称。
		 * @return 一个 CompletableFuture<Boolean>
		 */
		Result<Boolean> verifyNameUniqueness(String clusterName);
		
		/**
		 * “更新一个组件的版本。”
		 *
		 * 注释的第一行是对该功能的简短描述。第二行是参数列表，第三行是返回值的说明
		 *
		 * @param componentId 要更新的组件的组件 ID。
		 * @param version 要更新的组件的版本。
		 * @return 结果 <Void> 对象。
		 */
		Result<Void> updateVersionWithECM(Integer componentId, String version);
    
    /**
     * “给定一个组件 ID，返回组件的名称。”
     *
     * 该函数在 Result 类中定义，该类表示操作的结果。操作的结果可以是成功也可以是失败。在成功的情况下，结果包含操作的值。在失败的情况下，结果包含发生的错误
     *
     * @param componentId 您要获取其名称的组件的组件 ID。
     * @return 包含字符串的结果对象。
     */
    Result<String> getNameByComponentId(Integer componentId);
    
    /**
     * 通过删除指定节点来收缩集群
     *
     * @param nodes 要删除的节点列表。
     * @param clusterName 要操作的集群名称
     * @return 操作的结果。
     */
    Result<Void> shrinkNodesWithEcm(List<ESClusterRoleHostDTO> nodes, String clusterName);
    
 
    /**
     * 检查待缩容的节点是否包含绑定region
     *
     * @param nodes 要删除的节点列表
     * @param clusterName 要操作的集群名称
     */
    Result<Void> checkShrinkNodesContainsBindRegion(List<ESClusterRoleHostDTO> nodes, String clusterName);
    
    /**
     * ecm使用给定参数创建集群
     *
     * @param createDTO ClusterCreateDTO 对象
     * @param operator
     * @return 创建操作的结果。
     */
    Result<Void> createWithECM(ClusterCreateDTO createDTO, String operator);
    
    /**
     * 通过将指定节点添加到集群来扩展集群
     *
     * @param nodes 要扩展的节点列表。
     * @param clusterName 要扩展的集群的名称。
     * @return 操作的结果。
     */
    Result<Void> expandNodesWithECM(List<ESClusterRoleHostDTO> nodes, String clusterName);
    
    /**
     * 获取依赖于给定组件的组件的 ID。
     *
     * @param dependComponentId 您要获取其 ID 的组件的组件 ID。
     * @return 依赖于具有给定 id 的组件的组件的 id。
     */
    Result<Integer> getIdByComponentId(Integer dependComponentId);
    
  
    /**
     * 它检查解除绑定资源操作是否完成。
     *
     * @param clusterPhy 要检查的集群。
     */
    Result<Void> checkCompleteUnbindResources(ClusterPhy clusterPhy);
    
    /**
     * OfflineWithEcm() 用于使用 ECM 使项目离线
     *
     * @param id 待下线的项目id。
     * @param creator 正在执行操作的用户。
     * @param projectId 超级项目的id。
     * @return 带有 Void 对象的 Result 对象。
     */
    Result<Void> offlineWithECM(Integer id, String creator, Integer projectId);

    /**
     * 物理集群绑定 gateway
     * @param clusterPhyId  集群id
     * @param gatewayClusterId gatewayId
     * @param operator 操作人
     * @param projectId 项目id
     */
    Result<Void> bindGatewayCluster(Integer clusterPhyId, List<Integer> bindGatewayClusterIds, String operator, Integer projectId);


    
    /**
     * 获取一个通过组件id
     *
     * @param componentId 组件id
     * @return {@link Result}<{@link Object}>
     */
    Result<ClusterPhyVO> getOneByComponentId(Integer componentId);
    
    /**
     * 检查缩容节点是否影响读写
     *
     * @param nodes       节点
     * @param clusterName 集群名字
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> checkShrinkNodesWhetherToReadWrite(List<ESClusterRoleHostDTO> nodes, String clusterName);
    
    /**
     * 缩容节点更新来读写
     *
     * @param nodes       节点
     * @param clusterName 集群名字
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> shrinkNodesUpdateToReadWrite(List<ESClusterRoleHostDTO> nodes, String clusterName);
    
    /**
     * 获取一个通过id
     *
     * @param clusterId 集群id
     * @return {@link Result}<{@link ClusterPhyVO}>
     */
    Result<ClusterPhyVO> getOneById(Integer clusterId);
}