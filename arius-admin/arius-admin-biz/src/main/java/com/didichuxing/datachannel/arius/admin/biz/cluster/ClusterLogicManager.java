package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.ConsoleTemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.ConsoleAppVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterHostVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

public interface ClusterLogicManager {

    /**
     * 构建运维页面的逻辑集群VO
     * @param logicClusters     逻辑集群列表
     * @param appIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     * @return
     */
    List<ConsoleClusterVO> batchBuildOpClusterVOs(List<ClusterLogic> logicClusters, Integer appIdForAuthJudge);

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
     * @param appId
     * @return
     */
    Result<List<ConsoleClusterVO>> getAppLogicClusters(Integer appId);

    /**
     * 获取APP拥有的逻辑集群名称列表
     */
    Result<List<String>> getAppLogicClusterNames(Integer appId);

    /**
     * 获取项目下的逻辑集群信息
     *
     * @param appId 项目id
     * @return
     */
    Result<List<ConsoleClusterVO>> getAppLogicClusterInfo(Integer appId);

    /**
     * 获取平台所有的集群列表
     * @param appId
     * @return
     */
    Result<List<ConsoleClusterVO>> getDataCenterLogicClusters(Integer appId);

    /**
     * 获取集群详情
     * @param clusterId
     * @param appId
     * @return
     */
    Result<ConsoleClusterVO> getAppLogicClusters(Long clusterId, Integer appId);

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
    Result<List<ESRoleClusterHostVO>> getLogicClusterNodes(Long clusterId);

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
    List<ConsoleClusterVO> getConsoleClusterVOS(ESLogicClusterDTO param, Integer appId);

    /**
     * 获取单个逻辑集群overView信息
     * @param clusterLogicId 逻辑集群id
     * @param currentAppId 当前登录项目
     */
    ConsoleClusterVO getConsoleCluster(Long clusterLogicId, Integer currentAppId);

    /**
     * 新建逻辑集群, 关联 logicCluster 关联 region
     */
    Result<Void> addLogicClusterAndClusterRegions(ESLogicClusterWithRegionDTO param, String operator);

    /**
     * 根据逻辑集群Id和appId创建逻辑集群信息
     */
    ConsoleClusterVO getConsoleClusterVOByIdAndAppId(Long clusterLogicId, Integer appId);

    Result<Long> addLogicCluster(ESLogicClusterDTO param, String operator, Integer appId);

    Result<Void> deleteLogicCluster(Long logicClusterId, String operator, Integer appId) throws AdminOperateException;

    Result<Void> editLogicCluster(ESLogicClusterDTO param, String operator, Integer appId);

    /**
     * 组合查询带分页信息的逻辑集群列表
     * @param condition
     * @param appId
     * @return
     */
    PaginationResult<ConsoleClusterVO> pageGetConsoleClusterVOS(ClusterLogicConditionDTO condition, Integer appId);

    /**
     * 获取项目下指定权限类型的逻辑集群列表
     * @param appId           项目
     * @param authType        权限类型
     * @return
     */
    List<ClusterLogic> getClusterLogicByAppIdAndAuthType(Integer appId, Integer authType);

    /**
     * 获取项目可访问逻辑集群列表
     * @param appId  项目
     * @return
     */
    List<ClusterLogic> getAppAccessClusterLogicList(Integer appId);

    /**
     * 根据项目和集群类型获取逻辑集群(项目对其有管理权限)名称列表
     * @param appId
     * @param type
     * @return
     */
    Result<List<ConsoleClusterVO>> getAppLogicClusterInfoByType(Integer appId, Integer type);

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
