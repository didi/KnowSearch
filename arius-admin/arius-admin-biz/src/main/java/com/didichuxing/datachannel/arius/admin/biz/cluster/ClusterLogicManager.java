package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleAppVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

public interface ClusterLogicManager {

    /**
     * 构建运维页面的逻辑集群VO
     * @param logicClusters     逻辑集群列表
     * @param projectIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     * @return
     */
    List<ConsoleClusterVO> batchBuildOpClusterVOs(List<ClusterLogic> logicClusters, Integer projectIdForAuthJudge);

    /**
     * 构建运维页面的逻辑集群VO
     * @param clusterLogic    逻辑集群
     * @param appIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     * @return
     */
    ConsoleClusterVO buildOpClusterVO(ClusterLogic clusterLogic, Integer appIdForAuthJudge);

    /**
     * 获取逻辑集群所有访问的APP
     *
     * @param logicClusterId 逻辑集群ID
     * @return
     */
    Result<List<ConsoleAppVO>> getAccessAppsOfLogicCluster(Long logicClusterId);

    /**
     * 获取APP拥有的集群列表
     * @param projectId
     * @return
     */
    Result<List<ConsoleClusterVO>> getAppLogicClusters(Integer projectId);

    /**
     * 获取APP拥有的逻辑集群或者物理集群名称列表
     * @param projectId 应用id
     * @return
     */
    Result<List<String>> getAppLogicOrPhysicClusterNames(Integer projectId);

    /**
     * 获取项目下的逻辑集群信息
     *
     * @param projectId 项目id
     * @return
     */
    Result<List<ConsoleClusterVO>> getAppLogicClusterInfo(Integer projectId);

    /**
     * 获取平台所有的集群列表
     * @param projectId
     * @return
     */
    Result<List<ConsoleClusterVO>> getDataCenterLogicClusters(Integer projectId);

    /**
     * 获取集群详情
     * @param clusterId
     * @param projectId
     * @return
     */
    Result<ConsoleClusterVO> getAppLogicClusters(Long clusterId, Integer projectId);

    /**
     * 获取逻辑集群所有逻辑模板列表
     * @param request
     * @param clusterId
     * @return
     */
    Result<List<ConsoleTemplateVO>> getClusterLogicTemplates(HttpServletRequest request, Long clusterId);

    /**
     * 获取指定逻辑集群datanode的规格接口
     * @param clusterId
     * @return
     */
    Result<Set<ESClusterNodeSepcVO>> getLogicClusterDataNodeSpec(Long clusterId);

    /**
     * 获取指定罗集群节点列表接口
     * @param clusterId
     * @return
     */
    Result<List<ESClusterRoleHostVO>> getLogicClusterNodes(Long clusterId);

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
     * 获取所有逻辑集群列表接口
     */
    List<ConsoleClusterVO> getConsoleClusterVOS(ESLogicClusterDTO param, Integer projectId);

    /**
     * 获取单个逻辑集群overView信息
     * @param clusterLogicId 逻辑集群id
     * @param currentProjectId 当前登录项目
     */
    ConsoleClusterVO getConsoleCluster(Long clusterLogicId, Integer currentProjectId);

    /**
     * 新建逻辑集群, 关联 logicCluster 关联 region
     */
    Result<Void> addLogicClusterAndClusterRegions(ESLogicClusterWithRegionDTO param, String operator);

    /**
     * 根据逻辑集群Id和appId创建逻辑集群信息
     */
    ConsoleClusterVO getConsoleClusterVOByIdAndAppId(Long clusterLogicId, Integer projectId);

    Result<Long> addLogicCluster(ESLogicClusterDTO param, String operator, Integer projectId);

    Result<Void> deleteLogicCluster(Long logicClusterId, String operator, Integer projectId) throws AdminOperateException;

    Result<Void> editLogicCluster(ESLogicClusterDTO param, String operator, Integer projectId);

    /**
     * 组合查询带分页信息的逻辑集群列表
     * @param condition
     * @param projectId
     * @return
     */
    PaginationResult<ConsoleClusterVO> pageGetConsoleClusterVOS(ClusterLogicConditionDTO condition, Integer projectId);

    /**
     * 获取项目下指定权限类型的逻辑集群列表
     * @param appId           项目
     * @param authType        权限类型
     * @return
     */
    List<ClusterLogic> getClusterLogicByAppIdAndAuthType(Integer appId, Integer authType);

    /**
     * 获取项目可访问逻辑集群列表
     * @param projectId  项目
     * @return
     */
    List<ClusterLogic> getAppAccessClusterLogicList(Integer projectId);

    /**
     * 根据项目和集群类型获取逻辑集群(项目对其有管理权限)名称列表
     * @param projectId
     * @param type
     * @return
     */
    Result<List<ConsoleClusterVO>> getAppLogicClusterInfoByType(Integer projectId, Integer type);

    /**
     * 更新逻辑集群状态
     * @param clusterLogicId
     * @return
     */
    boolean updateClusterLogicHealth(Long clusterLogicId);

    /**
     * 校验模板大小资源是否充足
     * @param logicClusterId 逻辑集群id
     * @param templateSize 模板新建的时候设置的数据大小
     * @return
     */
    Result<Void> checkTemplateDataSizeValidForCreate(Long logicClusterId, String templateSize);
}