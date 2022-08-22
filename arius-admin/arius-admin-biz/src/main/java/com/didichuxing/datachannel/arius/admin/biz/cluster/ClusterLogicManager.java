package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicTemplateIndexCountVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description: 逻辑集群manager
 * @author gyp
 * @date 2022/5/31 16:26
 * @version 1.0
 */
public interface ClusterLogicManager {

    /**
     * 构建运维页面的逻辑集群VO
     * @param logicClusters     逻辑集群列表
     * @return 逻辑集群VO
     */
    List<ClusterLogicVO> buildClusterLogics(List<ClusterLogic> logicClusters);

    /**
     * 构建运维页面的逻辑集群VO
     * @param clusterLogic    逻辑集群
     * @return
     */
    ClusterLogicVO buildClusterLogic(ClusterLogic clusterLogic);

    /**
     * 获取project拥有的逻辑集群id和名称列表
     * @param projectId 应用id
     * @return
     */
    Result<List<Tuple<Long/*逻辑集群Id*/, String/*逻辑集群名称*/>>> listProjectClusterLogicIdsAndNames(Integer projectId);

    /**
     * 获取项目下的逻辑集群信息
     *
     * @param projectId 项目id
     * @return
     */
    Result<List<ClusterLogicVO>> getLogicClustersByProjectId(Integer projectId);

    /**
     * 根据项目和集群类型获取逻辑集群(项目对其有管理权限)名称列表
     * @param projectId 项目id
     * @param type
     * @return
     */
    Result<List<ClusterLogicVO>> getProjectLogicClusterInfoByType(Integer projectId, Integer type);

    /**
     * 获取逻辑集群所有逻辑模板列表
     * @param request
     * @param clusterId
     * @return
     */
    Result<List<ConsoleTemplateVO>> getClusterLogicTemplates(HttpServletRequest request, Long clusterId);

    /**
     * 获取当前集群支持的套餐列表
     * @return
     */
    Result<List<ESClusterNodeSepcVO>> listMachineSpec();

    /**
     * clearIndices
     * @param clearDTO
     * @param operator
     * @return
     * @throws ESOperateException
     */
    Result<Void> clearIndices(TemplateClearDTO clearDTO, String operator) throws ESOperateException;

    /**
     * 获取逻辑集群分派的物理集群列表
     *
     * @param logicClusterId 逻辑集群ID
     * @return
     */
    ClusterPhy getLogicClusterAssignedPhysicalClusters(Long logicClusterId);



    /**
     *  获取单个逻辑集群overView信息
     * @param clusterLogicId 逻辑集群id
     * @param currentProjectId 当前登录项目
     */
    ClusterLogicVO getClusterLogic(Long clusterLogicId, Integer currentProjectId);

    /**
     * 新建逻辑集群, 关联 logicCluster 关联 region
     *
     * @param param    集群信息
     * @param operator 操作人
     * @return 成功或失败
     */
    Result<Void> addLogicClusterAndClusterRegions(ESLogicClusterWithRegionDTO param,
                                                  String operator) throws AdminOperateException;

    /**
     * 逻辑集群下线
     * @param logicClusterId 逻辑集群id
     * @param operator 操作人
     * @param projectId projectId
     * @return 成功或者失败
     * @throws AdminOperateException
     */
    Result<Void> deleteLogicCluster(Long logicClusterId, String operator,
                                    Integer projectId) ;

    /**
     *  修改逻辑集群信息
     * @param param 逻辑集群dto
     * @param operator 操作人
     * @param projectId projectId
     * @return 成功或者失败
     */
    Result<Void> editLogicCluster(ESLogicClusterDTO param, String operator, Integer projectId);

    /**
     * 组合查询带分页信息的逻辑集群列表
     * @param condition
     * @param projectId 项目id
     * @return
     */
    PaginationResult<ClusterLogicVO> pageGetClusterLogics(ClusterLogicConditionDTO condition,
                                                          Integer projectId) throws NotFindSubclassException;

    /**
     * 更新逻辑集群状态
     * @param clusterLogicId
     * @return
     */
    boolean updateClusterLogicHealth(Long clusterLogicId);

    /**
     * 获取我的集群下索引和模板的数量
     * @param clusterId
     * @param operator
     * @param projectId 项目id
     * @return
     */
    Result<ClusterLogicTemplateIndexCountVO> indexTemplateCount(Long clusterId, String operator, Integer projectId);

    /**
     * 获取预估磁盘大小
     * @param clusterLogicId
     * @param count
     * @return
     */
    Result<Long> estimatedDiskSize(Long clusterLogicId, Integer count);

    /**
     * 根据projectId获取项目下的逻辑集群
     * @param projectId 项目id
     * @return
     */
    Result<List<String>> listClusterLogicNameByProjectId(Integer projectId);

    /**
     * 根据项目id获取集群的映射关系
     * @param projectId 项目id
     * @return
     */
    Result<List<Tuple<String, ClusterPhyVO>>> getClusterRelationByProjectId(Integer projectId);

    /**
     *  获取逻辑集群插件列表
     * @param clusterId 逻辑集群id
     * @return 插件列表
     */
    Result<List<PluginVO>> getClusterLogicPlugins(Long clusterId);

    /**
     * 检查逻辑集群的reigon是否不为空
     *
     * @param logicClusterId 逻辑集群id
     * @return {@link Result}<{@link Boolean}>
     */
    Result<Boolean> isLogicClusterRegionIsNotEmpty(Long logicClusterId);
    
    Result<List<ClusterLogicVO>> getLogicClustersByLevel(Integer level);
    
    /**
     * 验证集群逻辑的参数
     *
     * @param param 要验证的参数对象。
     * @param operation OperationEnum.ADD、OperationEnum.UPDATE、OperationEnum.DELETE
     * @param projectId 项目编号
     */
    Result<Void> validateClusterLogicParams(ESLogicClusterDTO param, OperationEnum operation, Integer projectId);
    
    
   
    /**
     * 加入逻辑集群
     *
     * @param logicClusterId 要加入的逻辑集群 ID。
     * @param joinProjectId 待加入的项目ID
     * @return 返回类型是 Result<Void>，它是操作结果的包装类。
     */
    Result<Void> joinClusterLogic(Long logicClusterId, Integer joinProjectId);
   
    /**
     * 返回与给定物理集群名称关联的逻辑集群名称列表
     *
     * @param phyClusterName 物理集群的名称。
     * @return 与给定集群物理名称关联的集群逻辑名称列表。
     */
    List<String> getClusterPhyAssociatedClusterLogicNames(String phyClusterName);

    /**
     * 根据物理集群名获取对应的逻辑集群列表，若传入为空，则返回全量
     * @param phyClusterName 物理集群的名称
     * @return List<String> 逻辑集群名称列表
     */
    List<String> listClusterLogicNameByPhyName(String phyClusterName);

    /**
     * 根据项目id获取对应的逻辑集群列表
     * @param projectId 项目id
     * @return List<String> 逻辑集群名称列表
     */
    List<String> listClusterLogicNameByApp(Integer projectId);

}