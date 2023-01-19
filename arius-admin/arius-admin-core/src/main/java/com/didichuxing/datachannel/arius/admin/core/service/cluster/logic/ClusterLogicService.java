package com.didichuxing.datachannel.arius.admin.core.service.cluster.logic;

import com.didichuxing.datachannel.arius.admin.common.bean.common.LogicResourceConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Plugin;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterNodeSepc;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import java.util.List;
import java.util.Set;

/**
 * 逻辑集群service
 * @author d06679
 * @date  2019/3/25
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
     * 删除逻辑集群
     *
     * @param logicClusterId 逻辑集群ID
     * @param operator       操作人
     * @param projectId     项目id
     * @return result
     * @throws AdminOperateException admin操作异常
     */
    Result<Void> deleteClusterLogicById(Long logicClusterId, String operator,
                                        Integer projectId) throws AdminOperateException;

    /**
     * > 索引模板的项目id与待删除项目的项目id是否可以匹配
     *
     * @param logicClusterId 逻辑集群的id
     * @param deleteProjectId 要删除的项目
     * @return boolean
     */
    boolean hasLogicClusterWithTemplates(Long logicClusterId, Integer deleteProjectId);

    /**
     * 新建逻辑集群
     * @param param 参数
     * @return result
     */
    Result<Long> createClusterLogic(ESLogicClusterDTO param);

    /**
     * 验证逻辑集群创建、更新参数是否合法
     *
     * @param param     参数
     * @param operation 操作
     * @param projectId 项目id
     * @return result
     */
    Result<Void> validateClusterLogicParams(ESLogicClusterDTO param, OperationEnum operation, Integer projectId);

    /**
     * 编辑逻辑集群信息
     *
     * @param param     参数
     * @param operator  操作人
     * @param projectId 项目id
     * @return result
     */
    Result<Void> editClusterLogic(ESLogicClusterDTO param, String operator, Integer projectId);

    Result<Void> editClusterLogicNotCheck(ESLogicClusterDTO param);

    /**
     * 查询指定逻辑集群
     *
     * @param logicClusterId 逻辑集群id
     * @param projectId
     * @return 逻辑集群 不存在返回null
     */
    ClusterLogic getClusterLogicByIdAndProjectId(Long logicClusterId, Integer projectId);
    
    /**
     * 获取集群逻辑通过id那不包含项目id
     *
     * @param logicClusterId 逻辑集群id
     * @return {@code ClusterLogic}
     */
    ClusterLogic getClusterLogicByIdThatNotContainsProjectId(Long logicClusterId);
    
    boolean  existClusterLogicById(Long logicClusterId);
    
    
    /**
     *通过逻辑集群id获取逻辑集群而且将projectIdStr转换为ProjectIdList
     *
     * @param logicClusterId 逻辑集群id
     * @return {@code List<ClusterLogic>}
     */
    List<ClusterLogic> listClusterLogicByIdThatProjectIdStrConvertProjectIdList(Long logicClusterId);
    
    /**
     * 获取逻辑集群通过名字和项目id
     *
     * @param logicClusterName 逻辑集群名称
     * @param projectId        项目id
     * @return {@code ClusterLogic}
     */
    ClusterLogic getClusterLogicByNameAndProjectId(String logicClusterName, Integer projectId);
    
    /**
     * 获取集群逻辑通过名字然后不包含项目id
     *
     * @param logicClusterName 逻辑集群名称
     * @return {@code ClusterLogic}
     */
    ClusterLogic getClusterLogicByNameThatNotContainsProjectId(String logicClusterName);
    
    /**
     * 通过逻辑集群名称获取逻辑集群而且将projectIdStr转换为ProjectIdList
     *
     * @param logicClusterName 逻辑集群名称
     * @return {@code List<ClusterLogic>}
     */
    List<ClusterLogic> listClusterLogicByNameThatProjectIdStrConvertProjectIdList(String logicClusterName);

    /**
     * 获取逻辑集群配置
     * @param logicClusterId 逻辑集群id
     * @return 逻辑集群配置 不存在返回null
     */
    LogicResourceConfig getClusterLogicConfigById(Long logicClusterId);

    /**
     * 查询指定app所创建（作为owner）的逻辑集群
     * @param projectId project ID
     * @return 逻辑集群列表
     */
    List<ClusterLogic> getOwnedClusterLogicListByProjectId(Integer projectId);

    /**
     * 查询指定app有权限的逻辑集群（包括ALL、OWN、ACCESS）
     * @param projectId project ID
     * @return 逻辑集群列表
     */
    List<ClusterLogic> getHasAuthClusterLogicsByProjectId(Integer projectId);

    /**
     * 根据项目和集群名称获取逻辑集群
     *
     * @param projectId   项目id
     * @param clusterName 集群名称
     * @return {@link List}<{@link ClusterLogic}>
     */
    List<ClusterLogic> listClusterLogicByProjectIdAndName(Integer projectId, String clusterName);

    List<Long> getHasAuthClusterLogicIdsByProjectId(Integer projectId);

    /**
     * 判断逻辑集群是否存在
     * @param resourceId 逻辑集群id
     * @return result
     */
    Boolean isClusterLogicExists(Long resourceId);

    /**
     * 根据配置字符创生成配置，填充默认值
     * @param configJson JSON
     * @return config
     */
    LogicResourceConfig genClusterLogicConfig(String configJson);

    /**
     * 获取逻辑集群的所有role
     * @param logicClusterId 逻辑集群ID
     * @return ClusterRoleInfo
     */
    List<ClusterRoleInfo> getClusterLogicRole(Long logicClusterId);

    /**
     * 获取逻辑集群datanode的规格信息
     * @param logicClusterId 逻辑集群ID
     * @return 规格信息
     */
    Set<RoleClusterNodeSepc> getLogicDataNodeSepc(Long logicClusterId);

    /**
     * 根据逻辑集群ID获取插件信息
     * @param  logicClusterId 逻辑集群ID
     * @return config 插件信息
     */
    List<Plugin> getClusterLogicPlugins(Long logicClusterId);

    /**
     * 根据逻辑集群Id添加插件信息
     * @param  logicClusterId        逻辑集群ID
     * @param  pluginDTO           插件信息
     * @param  operator              操作人
     * @return ESPlugin             成功
     */
    Result<Long> addPlugin(Long logicClusterId, PluginDTO pluginDTO, String operator) throws NotFindSubclassException;

    /**
     * 转移逻辑集群
     *
     * @param clusterLogicId  逻辑集群Id
     * @param targetProjectId 项目Id
     * @param submitor        提交人
     * @return 成功/失败
     */
    Result<Void> transferClusterLogic(Long clusterLogicId, Integer targetProjectId,
                                      String submitor);

    /**
     * 模糊分页查询物理集群列表信息，仅获取部分属性
     */
    List<ClusterLogic> pagingGetClusterLogicByCondition(ClusterLogicConditionDTO condition);

    /**
     * 模糊查询统计总命中数
     * @param param 模糊查询条件
     * @return 命中数
     */
    Long fuzzyClusterLogicHitByCondition(ClusterLogicConditionDTO param);

    /**
     * 根据逻辑集群id列表获取逻辑集群列表信息
     * @param clusterLogicIdList  逻辑集群id列表
     * @return                    List<ClusterLogic>
     */
    List<ClusterLogic> getClusterLogicListByIds(List<Long> clusterLogicIdList);
    
    /**
     *
     * 通过level获取逻辑集群并且将projectIdStr convert projectId list
     * @param level 水平
     * @return {@code List<ClusterLogic>}
     */
    List<ClusterLogic> listLogicClustersByLevelThatProjectIdStrConvertProjectIdList(Integer level);
    
    /**
     * > 加入集群
     *
     * @param param 加入集群逻辑的参数对象。
     * @return 加入集群逻辑的结果。
     */
    Result<Long> joinClusterLogic(ESLogicClusterDTO param);
    
    /**
     * 获取 projectId 等于给定 projectId 的实体的所有 ID。
     *
     * @param projectId 您要为其获取 ID 的项目的 ID。
     * @return 项目中具有给定 ID 的所有任务 ID 的列表。
     */
    List<Long> getAllIdsByProjectId(Integer projectId);
}