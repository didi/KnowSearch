package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicNodeConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicTemplateIndexCountVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

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
     * 获取APP拥有的集群列表
     * @param appId appId
     * @return
     */
    Result<List<ClusterLogicVO>> getAppLogicClusters(Integer appId);

    /**
     * 获取APP拥有的逻辑集群或者物理集群名称列表
     * @param appId 应用id
     * @return
     */
    Result<List<String>> getAppLogicOrPhysicClusterNames(Integer appId);

    /**
     * 获取项目下的逻辑集群信息
     *
     * @param appId 项目id
     * @return
     */
    Result<List<ClusterLogicVO>> getLogicClustersByProjectId(Integer appId);

    /**
     * 获取集群详情
     * @param clusterId
     * @param appId
     * @return
     */
    Result<ClusterLogicVO> getAppLogicClusters(Long clusterId, Integer appId);

    /**
     * 根据项目和集群类型获取逻辑集群(项目对其有管理权限)名称列表
     * @param appId
     * @param type
     * @return
     */
    Result<List<ClusterLogicVO>> getAppLogicClusterInfoByType(Integer appId, Integer type);


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
    Result<Void> clearIndices(ConsoleTemplateClearDTO clearDTO, String operator) throws ESOperateException;

    /**
     * 获取逻辑集群分派的物理集群列表
     *
     * @param logicClusterId 逻辑集群ID
     * @return
     */
    List<ClusterPhy> getLogicClusterAssignedPhysicalClusters(Long logicClusterId);

    /**
     * 获取逻辑集群分派的物理集群列表
     * @param logicCluster
     * @return
     */
    List<ClusterPhy> getLogicClusterAssignedPhysicalClusters(String logicCluster);

    /**
     * 获取所有逻辑集群列表接口
     * @param param
     * @param appId
     * @return
     */
    List<ClusterLogicVO> getClusterLogics(ESLogicClusterDTO param, Integer appId);

    /**
     *  获取单个逻辑集群overView信息
     * @param clusterLogicId 逻辑集群id
     * @param currentAppId 当前登录项目
     * @return
     */
    ClusterLogicVO getClusterLogic(Long clusterLogicId, Integer currentAppId);

    /**
     * 新建逻辑集群, 关联 logicCluster 关联 region
     * @param param 集群信息
     * @param operator 操作人
     * @return 成功或失败
     */
    Result<Void> addLogicClusterAndClusterRegions(ESLogicClusterWithRegionDTO param, String operator);

    /**
     *  根据逻辑集群Id和appId创建逻辑集群信息
     * @param clusterLogicId 集群id
     * @param appId appId
     * @return 集群详情
     */
    ClusterLogicVO getClusterLogicByIdAndAppId(Long clusterLogicId, Integer appId);

    /**
     *  新建带有region信息的逻辑集群
     * @param param 逻辑集群信息
     * @param operator 操作人
     * @param appId appId
     * @return id
     */
    Result<Long> addLogicCluster(ESLogicClusterDTO param, String operator, Integer appId);

    /**
     * 逻辑集群下线
     * @param logicClusterId 逻辑集群id
     * @param operator 操作人
     * @param appId appId
     * @return 成功或者失败
     * @throws AdminOperateException
     */
    Result<Void> deleteLogicCluster(Long logicClusterId, String operator, Integer appId) throws AdminOperateException;

    /**
     *  修改逻辑集群信息
     * @param param 逻辑集群dto
     * @param operator 操作人
     * @param appId appId
     * @return 成功或者失败
     */
    Result<Void> editLogicCluster(ESLogicClusterDTO param, String operator, Integer appId);

    /**
     * 组合查询带分页信息的逻辑集群列表
     * @param condition
     * @param appId
     * @return
     */
    PaginationResult<ClusterLogicVO> pageGetClusterLogics(ClusterLogicConditionDTO condition, Integer appId);

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
     * @param appId
     * @return
     */
    Result<ClusterLogicTemplateIndexCountVO> indexTemplateCount(Long clusterId, String operator, Integer appId);


    /**
     * 逻辑集群下的节点信息分页查询
     * @param condition 分页参数
     * @return PaginationResult
     */
    PaginationResult<ESClusterRoleHostVO> nodesPage(Long clusterLogicId,ClusterLogicNodeConditionDTO condition);

    /**
     * 获取预估磁盘大小
     * @param clusterLogicId
     * @param count
     * @return
     */
    Result<String> estimatedDiskSize(Long clusterLogicId, Integer count);

    /**
     * 根据项目和集群类型获取逻辑集群(项目对其有管理权限)名称列表
     * @param appId
     * @param type
     * @return
     */
    Result<List<String>> getAppLogicClusterNameByType(Integer appId, Integer type);
}