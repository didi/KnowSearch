package com.didichuxing.datachannel.arius.admin.core.service.cluster.logic;

import java.util.List;
import java.util.Set;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ESPlugin;
import com.didichuxing.datachannel.arius.admin.client.bean.common.LogicResourceConfig;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPluginDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicWithRack;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterNodeSepc;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;

/**
 * 逻辑集群service
 * @author d06679
 * @date 2019/3/25
 */
public interface ClusterLogicService {

    /**
     * 条件查询逻辑集群
     * @param param 条件
     * @return 逻辑集群列表
     */
    List<ClusterLogic> listClusterLogics(ESLogicClusterDTO param);

    /**
     * 获取所有逻辑集群
     * @return 逻辑集群列表
     */
    List<ClusterLogic> listAllClusterLogics();

    /**
     * 获取所有逻辑集群，返回结果里包含逻辑集群拥有的rack信息
     * @return
     */
    List<ClusterLogicWithRack> listAllClusterLogicsWithRackInfo();

    /**
     * 获取逻辑集群，返回结果里包含逻辑集群拥有的rack信息
     * @return
     */
    ClusterLogicWithRack getClusterLogicWithRackInfoById(Long logicClusterId);

    /**
     * 删除逻辑集群
     * @param logicClusterId 逻辑集群ID
     * @param operator 操作人
     * @return result
     * @throws AdminOperateException
     */
    Result<Void> deleteClusterLogicById(Long logicClusterId, String operator) throws AdminOperateException;

    /**
     * 判断逻辑集群是否有模板
     * @param logicClusterId
     * @return
     */
    Boolean hasLogicClusterWithTemplates(Long logicClusterId);

    /**
     * 新建逻辑集群
     * @param param 参数
     * @param operator 操作人
     * @return result
     */
    Result<Long> createClusterLogic(ESLogicClusterDTO param, String operator);

    /**
     * 验证逻辑集群创建、更新参数是否合法
     * @param param 参数
     * @param operation 操作
     * @return result
     */
    Result<Void> validateClusterLogicParams(ESLogicClusterDTO param, OperationEnum operation);

    /**
     * 编辑逻辑集群信息
     * @param param 参数
     * @param operator 操作人
     * @return result
     */
    Result<Void> editClusterLogic(ESLogicClusterDTO param, String operator);

    Result<Void> editClusterLogicNotCheck(ESLogicClusterDTO param, String operator);

    /**
     * 查询指定逻辑集群
     * @param logicClusterId 逻辑集群id
     * @return 逻辑集群 不存在返回null
     */
    ClusterLogic getClusterLogicById(Long logicClusterId);

    ClusterLogic getClusterLogicByName(String logicClusterName);

    /**
     * 获取逻辑集群配置
     * @param logicClusterId 逻辑集群id
     * @return 逻辑集群配置 不存在返回null
     */
    LogicResourceConfig getClusterLogicConfigById(Long logicClusterId);

    /**
     * 查询指定app所创建（作为owner）的逻辑集群
     * @param appId APP ID
     * @return 逻辑集群列表
     */
    List<ClusterLogic> getOwnedClusterLogicListByAppId(Integer appId);

    /**
     * 查询指定app有权限的逻辑集群（包括ALL、OWN、ACCESS）
     * @param appId APP ID
     * @return 逻辑集群列表
     */
    List<ClusterLogic> getHasAuthClusterLogicsByAppId(Integer appId);

    /**
     * 判断逻辑集群是否存在
     * @param resourceId 逻辑集群id
     * @return result
     */
    Boolean isClusterLogicExists(Long resourceId);

    /**
     * 获取rack匹配到的逻辑集群
     * @param cluster 集群
     * @param racks racks
     * @return count
     */
    ClusterLogic getClusterLogicByRack(String cluster, String racks);

    /**
     * 根据责任人查询
     * @param responsibleId 责任人id
     * @return list
     */
    List<ClusterLogic> getLogicClusterByOwnerId(Long responsibleId);

    /**
     * 根据配置字符创生成配置，填充默认值
     * @param configJson JSON
     * @return config
     */
    LogicResourceConfig genClusterLogicConfig(String configJson);

    /**
     * 获取逻辑集群的所有role
     * @param logicClusterId 逻辑集群ID
     * @return
     */
    List<RoleCluster> getClusterLogicRole(Long logicClusterId);

    /**
     * 获取逻辑集群datanode的规格信息
     * @param logicClusterId
     * @return
     */
    Set<RoleClusterNodeSepc> getLogicDataNodeSepc(Long logicClusterId);

    /**
     * 根据逻辑集群ID获取插件信息
     * @param  logicClusterId 逻辑集群ID
     * @return config
     */
    List<ESPlugin> getClusterLogicPlugins(Long logicClusterId);

    /**
     * 根据逻辑集群Id添加插件信息
     * @param  logicClusterId        逻辑集群ID
     * @param  esPluginDTO           插件信息
     * @param  operator              操作人
     * @return ESPlugin
     */
    Result<Long> addPlugin(Long logicClusterId, ESPluginDTO esPluginDTO, String operator);

    /**
     * 转移逻辑集群
     * @param clusterLogicId       逻辑集群Id
     * @param targetAppId          项目Id
     * @param targetResponsible    目标负责人
     * @param submitor             提交人
     * @return
     */
    Result<Void> transferClusterLogic(Long clusterLogicId, Integer targetAppId, String targetResponsible, String submitor);

    /**
     * 模糊分页查询物理集群列表信息，仅获取部分属性
     */
    List<ClusterLogic> pagingGetClusterLogicByCondition(ClusterLogicConditionDTO condition);

    /**
     * 模糊查询统计总命中数
     * @param param 模糊查询条件
     * @return
     */
    Long fuzzyClusterLogicHitByCondition(ClusterLogicConditionDTO param);

    List<ClusterLogic> getClusterLogicListByIds(List<Long> clusterLogicIdList);
}
