package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.ConsoleTemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.ConsoleAppVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterHostVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

public interface ClusterLogicManager {

    /**
     * 构建运维页面的逻辑集群VO
     * @param logicClusters     逻辑集群列表
     * @param appIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     * @return
     */
    List<ConsoleClusterVO> batchBuildOpClusterVOs(List<ESClusterLogic> logicClusters,
                                                         Integer appIdForAuthJudge);

    /**
     * 构建运维页面的逻辑集群VO
     * @param esClusterLogic    逻辑集群
     * @param appIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     * @return
     */
     ConsoleClusterVO buildOpClusterVO(ESClusterLogic esClusterLogic, Integer appIdForAuthJudge);

    /**
     * 批量
     * @param logicClusters             逻辑集群列表
     * @param currentUserAppId          当前用户App Id
     * @return
     */
     List<ConsoleClusterVO> batchBuildConsoleClusters(List<ESClusterLogic> logicClusters,
                                                            Integer currentUserAppId);

    /**
     * @param esClusterLogic            逻辑集群元数据信息
     * @return
     */
     ConsoleClusterVO buildConsoleClusterVO(ESClusterLogic esClusterLogic, Integer currentUserAppId);

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
     Result clearIndices(ConsoleTemplateClearDTO clearDTO, String operator) throws ESOperateException;

    /**
     * 获取逻辑集群分派的物理集群列表
     *
     * @param logicClusterId 逻辑集群ID
     * @return
     */
     List<ESClusterPhy> getLogicClusterAssignedPhysicalClusters(Long logicClusterId);

    /**
     * 获取所有逻辑集群列表接口
     */
    List<ConsoleClusterVO> getConsoleClusterVOS(ESLogicClusterDTO param, Integer appId);

    /**
     * 新建逻辑集群, 关联 logicCluster 关联 region
     */
    Result<Long> addLogicClusterAndClusterRegions(ESLogicClusterWithRegionDTO param, String operator);

    /**
     * 根据逻辑集群Id和appId创建逻辑集群信息
     */
    ConsoleClusterVO getConsoleClusterVOByIdAndAppId(Long clusterLogicId, Integer appId);
}
