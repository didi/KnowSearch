package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterDynamicConfigsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;

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

    /**
     * 同步元数据
     * @param cluster    集群名称
     * @param retryCount 重试次数
     * @return
     */
    void syncTemplateMetaData(String cluster, int retryCount);

    /**
     * 集群是否存在
     * @param clusterName 集群名字
     * @return true 存在
     */

    boolean isClusterExists(String clusterName);

    /**
     * 释放racks
     * @param cluster    集群名称
     * @param racks      要释放的racks，逗号分隔
     * @param retryCount 重试次数
     * @return result
     */
    Result<Void> releaseRacks(String cluster, String racks, int retryCount);

    /**
     * 获取控制台物理集群信息列表(ZH有使用)
     * @param currentAppId 当前登录项目
     * @param param         查询参数
     * @return 物理集群列表
     */
    List<ConsoleClusterPhyVO> getConsoleClusterPhys(ClusterPhyDTO param, Integer currentAppId);

    /**
     * 获取控制台物理集群信息列表
     * @param param 查询参数
     * @return 物理集群列表
     */
    List<ConsoleClusterPhyVO> getConsoleClusterPhys(ClusterPhyDTO param);

    /**
     * 构建客户端需要的数据
     *
     * @param clusterPhyList  集群列表源数据
     * @param appId           当前项目
     * @return
     */
    List<ConsoleClusterPhyVO> buildClusterInfo(List<ClusterPhy> clusterPhyList, Integer appId);

    /**
     * 获取单个物理集群overView信息
     * @param clusterId 物理集群id
     * @param currentAppId 当前登录项目
     * @return 物理集群信息
     */
    ConsoleClusterPhyVO getConsoleClusterPhy(Integer clusterId, Integer currentAppId);

    /**
     * 获取物理集群节点划分信息
     * @param clusterPyhId  物理集群ID
     * @return 集群节点信息
     */
    Result<List<ESClusterRoleHostVO>> getClusterPhyRegionInfos(Integer clusterPyhId);

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
     * @param param 逻辑集群Id, 物理集群名称
     * @param operator 操作人
     * @return  ClusterPhyVO
     */
	Result<ClusterPhyVO> joinCluster(ClusterJoinDTO param, String operator);

    /**
     * 删除接入集群
     * 删除顺序: region ——> clusterLogic ——> clusterHost ——> clusterRole  ——> cluster
     * @param clusterId 集群id
     * @param operator  操作人
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> deleteClusterJoin(Integer clusterId, String operator);

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
     * @param param 配置信息参数
     * @return result
     */
    Result<Boolean> updatePhyClusterDynamicConfig(ClusterSettingDTO param);

    /**
     * 获取集群下的属性配置
     * @param cluster 集群名称
     * @return result
     */
    Result<Set<String>> getRoutingAllocationAwarenessAttributes(String cluster);

    /**
     * 获取APP有管理、读写、读权限的物理集群名称列表
     *
     * @param appId appId
     * @return {@link List}<{@link String}>
     */
    List<String> getAppClusterPhyNames(Integer appId);

    /**
     * 根据模板所在集群，获取与该集群相同版本号的集群名称列表
     * @param appId      appId
     * @param templateId 模板id
     * @return {@link Result}<{@link List}<{@link String}>>
     */
    Result<List<String>> getTemplateSameVersionClusterNamesByTemplateId(Integer appId, Integer templateId);

    /**
     * 获取物理集群节点名称列表
     * @param clusterPhyName 集群phy名称
     * @return {@link List}<{@link String}>
     */
    List<String> getAppClusterPhyNodeNames(String clusterPhyName);

    /**
     * 构建单个物理集群统计信息
     * @param cluster 集群
     */
    void buildPhyClusterStatics(ConsoleClusterPhyVO cluster);

    /**
     * 获取APP可查看的物理集群节点名称列表
     * @param appId appId
     * @return {@link List}<{@link String}>
     */
    List<String> getAppNodeNames(Integer appId);

    /**
     * 物理集群信息删除 (host信息、角色信息、集群信息、region信息)
     * @param clusterPhyId 物理集群ID
     * @param operator     操作人
     * @param appId        appId
     * @return {@link Result}<{@link Boolean}>
     */
    Result<Boolean> deleteClusterInfo(Integer clusterPhyId, String operator, Integer appId);

    /**
     * 添加集群
     *
     * @param param    参数
     * @param operator 操作人
     * @param appId    appId
     * @return {@link Result}<{@link Boolean}>
     */
    Result<Boolean> addCluster(ClusterPhyDTO param, String operator, Integer appId);

    /**
     * 编辑集群
     *
     * @param param    参数
     * @param operator 操作人
     * @param appId    appId
     * @return {@link Result}<{@link Boolean}>
     */
    Result<Boolean> editCluster(ClusterPhyDTO param, String operator, Integer appId);

    /**
     * 条件组合、分页查询
     * @param condition
     * @param appId
     * @return
     */
    PaginationResult<ConsoleClusterPhyVO> pageGetClusterPhys(ClusterPhyConditionDTO condition, Integer appId);

    /**
     * 获取项目下指定权限的物理集群列表
     * @param appId
     * @param authType
     * @return
     */
    List<ClusterPhy> getClusterPhyByAppIdAndAuthType(Integer appId, Integer authType);

    /**
     * 获取项目下有管理权限的物理集群列表
     * @param appId
     * @return
     */
    List<ClusterPhy> getAppAccessClusterPhyList(Integer appId);

    /**
     * 获取项目下有访问权限的物理集群列表
     * @param appId
     * @return
     */
    List<ClusterPhy> getAppOwnAuthClusterPhyList(Integer appId);

    /**
     * 构建物理集群角色信息
     * @param cluster
     */
    void buildClusterRole(ConsoleClusterPhyVO cluster);

    /**
     * 构建集群作用
     *
     * @param cluster      集群
     * @param clusterRoleInfos 集群角色
     */
    void buildClusterRole(ConsoleClusterPhyVO cluster, List<ClusterRoleInfo> clusterRoleInfos);

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
     * @param appId
     * @param operator
     * @return
     */
    Result<Boolean> deleteClusterExit(String clusterPhyName, Integer appId, String operator);

    /**
     * 构建物理集群所属项目和所属的appId的信息
     * @param consoleClusterPhyVO 物理集群看板视图
     */
    void buildBelongAppIdsAndNames(ConsoleClusterPhyVO consoleClusterPhyVO);

    /**
     *  根据逻辑集群类型和已选中的物理集群名称筛选出es版本一致的物理集群名称列表
     *  @param hasSelectedClusterNameWhenBind 用户在新建逻辑集群阶段已选择的物理集群名称
     *  @param clusterLogicType 逻辑集群类型
     *  @return 同版本的物理集群名称列表
     */
    Result<List<String>> getPhyClusterNameWithSameEsVersion(Integer clusterLogicType, String hasSelectedClusterNameWhenBind);

    /**
     * 根据已经创建的逻辑集群id筛选出物理集群版本一致的物理集群名称列表
     * @param clusterLogicId 逻辑集群id
     * @return 同版本的物理集群名称列表
     */
    Result<List<String>> getPhyClusterNameWithSameEsVersionAfterBuildLogic(Long clusterLogicId);

    /**
     * 为一个正在接入的物理集群校验是否可以添加索引服务
     * @param clusterJoinDTO 物理集群接入DTO
     * @param strId 索引服务id
     * @param operator 操作人员
     * @return 校验结果
     */
    Result<Boolean> checkTemplateServiceWhenJoin(ClusterJoinDTO clusterJoinDTO, String strId, String operator);

    /**
     * 根据物理集群名称和当前模板审批的工单获取可以绑定的rack列表
     * @param clusterPhy 物理集群名称
     * @param clusterLogic 逻辑集群名称
     * @param templateSize 模板设置的数据大小
     * @return 可以绑定的rack列表
     */
    Result<Set<String>> getValidRacksListByTemplateSize(String clusterPhy, String clusterLogic, String templateSize);

    Result<ClusterPhyVO> updateClusterGateway(ClusterPhyDTO param, String operator, Integer appId);
}
