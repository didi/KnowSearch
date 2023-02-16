package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.CLUSTER_LOGIC;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;
import static com.didichuxing.datachannel.arius.admin.common.util.SizeUtil.getUnitSize;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.biz.page.ClusterLogicPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.common.Triple;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionWithNodeInfoDTO.ClusterRegionWithNodeInfoDTOBuilder;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO.ESLogicClusterWithRegionDTOBuilder;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicStatis;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterLogicEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESMachineNormsService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.knowframework.security.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Component
public class ClusterLogicManagerImpl implements ClusterLogicManager {

    private static final ILog              LOGGER       = LogFactory.getLog(ClusterLogicManagerImpl.class);

    @Autowired
    private ESIndexService                 esIndexService;

    @Autowired
    private ClusterPhyService              clusterPhyService;

    @Autowired
    private ClusterLogicService            clusterLogicService;

    @Autowired
    private ClusterRegionService           clusterRegionService;

    @Autowired
    private ClusterRoleHostService         clusterRoleHostService;

    @Autowired
    private IndexTemplateService           indexTemplateService;


    @Autowired
    private IndexTemplatePhyService        indexTemplatePhyService;

    @Autowired
    private ProjectClusterLogicAuthService projectClusterLogicAuthService;

    @Autowired
    private OperateRecordService           operateRecordService;

    @Autowired
    private ESMachineNormsService          esMachineNormsService;

    @Autowired
    private ProjectService                 projectService;

    @Autowired
    private ESGatewayClient                esGatewayClient;

    @Autowired
    private ClusterRegionManager           clusterRegionManager;

 

    @Autowired
    private ESClusterService               esClusterService;

    @Autowired
    private HandleFactory                  handleFactory;


    @Autowired
    private ESIndexCatService              esIndexCatService;

    @Autowired
    private ESClusterNodeService eSClusterNodeService;
    @Autowired
    private ClusterPhyManager    clusterPhyManager;
    @Autowired
    private ClusterNodeManager clusterNodeManager;
    
    private static final FutureUtil<Void>         FUTURE_UTIL        = FutureUtil.init("ClusterLogicManager", 10, 10,
            100);

    private static final Long              UNKNOWN_SIZE = -1L;

    /**
     * 构建运维页面的逻辑集群VO
     * @param logicClusters     逻辑集群列表
     * @return 逻辑集群列表
     */
    @Override
    public List<ClusterLogicVO> buildClusterLogics(List<ClusterLogic> logicClusters) {
        if (CollectionUtils.isEmpty(logicClusters)) {
            return Lists.newArrayList();
        }

        List<ClusterLogicVO> clusterLogicVOS = Lists.newArrayList();
        for (ClusterLogic logicCluster : logicClusters) {
            clusterLogicVOS.add(buildClusterLogic(logicCluster));
        }

        Collections.sort(clusterLogicVOS);
        return clusterLogicVOS;
    }

    /**
     * 构建运维页面的逻辑集群VO
     * @param clusterLogic    逻辑集群
     * @return ClusterLogicVO
     */
    @Override
    public ClusterLogicVO buildClusterLogic(ClusterLogic clusterLogic) {
        ClusterLogicVO clusterLogicVO = ConvertUtil.obj2Obj(clusterLogic, ClusterLogicVO.class);

        FUTURE_UTIL.runnableTask(() -> buildLogicClusterStatus(clusterLogicVO, clusterLogic))
            .runnableTask(() -> buildLogicRole(clusterLogicVO, clusterLogic))
            .runnableTask(() -> buildConsoleClusterVersions(clusterLogicVO));

        buildClusterNodeInfo(clusterLogicVO);
        Optional.ofNullable(projectService.getProjectBriefByProjectId(clusterLogic.getProjectId()))
            .map(ProjectBriefVO::getProjectName).ifPresent(clusterLogicVO::setProjectName);

        return clusterLogicVO;
    }

    @Override
    public Result<Void> clearIndices(TemplateClearDTO clearDTO, String operator) throws ESOperateException {
        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人为空");
        }

        if (CollectionUtils.isEmpty(clearDTO.getDelIndices())) {
            return Result.buildParamIllegal("删除索引为空");
        }
        final TupleTwo<Result<Void>, Integer> resultIntegerTuple2 = checkIndices(clearDTO.getDelIndices(),
            clearDTO.getLogicId());
        Result<Void> checkResult = resultIntegerTuple2.v1;
        if (checkResult.failed()) {
            return checkResult;
        }

        LOGGER.info("class=TemplateLogicServiceImpl||method=clearIndex||"
                    + "operator={}||logicId={}||delIndices={}||delQueryDsl={}",
            operator, clearDTO.getLogicId(), JSON.toJSONString(clearDTO.getDelIndices()), clearDTO.getDelQueryDsl());

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(clearDTO.getLogicId());

        if (StringUtils.isNotBlank(clearDTO.getDelQueryDsl())) {
            Result<Void> deleteResult = batchDeletePhysicalTemplateIndicesByQuery(
                templateLogicWithPhysical.getPhysicals(), clearDTO.getDelQueryDsl(), clearDTO.getDelIndices());
            if (deleteResult.failed()) {
                return deleteResult;
            }
        } else {
            Result<Void> deleteIndicesResult = batchDeletePhysicalTemplateIndices(
                templateLogicWithPhysical.getPhysicals(), clearDTO.getDelIndices());
            if (deleteIndicesResult.failed()) {
                return deleteIndicesResult;
            }
        }

        if (StringUtils.isNotBlank(clearDTO.getDelQueryDsl())) {
            String delIndices = String.join(",", clearDTO.getDelIndices());
            operateRecordService.saveOperateRecordWithManualTrigger(
                    String.format("需要清理的索引列表:%s; 删除条件:%s", delIndices, clearDTO.getDelQueryDsl()),
                    operator, resultIntegerTuple2.v2, clearDTO.getLogicId(), OperateTypeEnum.TEMPLATE_SERVICE_CLEAN);

            return Result.buildSucWithTips("删除任务下发成功，请到sense中执行查询语句，确认删除进度");
        } else {
            return Result.buildSucWithTips("索引删除成功");
        }
    }

    /**
     * 获取逻辑集群分派的物理集群列表
     *
     * @param logicClusterId 逻辑集群ID
     * @return 物理集群集合
     */
    @Override
    public ClusterPhy getLogicClusterAssignedPhysicalClusters(Long logicClusterId) {
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(logicClusterId);
        if (clusterRegion == null) {
            return null;
        }

        return clusterPhyService.getClusterByName(clusterRegion.getPhyClusterName());
    }

    @Override
    public Result<List<ClusterLogicVO>> getLogicClustersByProjectId(Integer projectId) {
        List<ClusterLogicVO> list = ConvertUtil
            .list2List(clusterLogicService.getHasAuthClusterLogicsByProjectId(projectId), ClusterLogicVO.class);
        for (ClusterLogicVO clusterLogicVO : list) {
            List<String> clusterPhyNames = clusterRegionService.listPhysicClusterNames(clusterLogicVO.getId());
            clusterLogicVO.setPhyClusterAssociated(!AriusObjUtils.isEmptyList(clusterPhyNames));
            clusterLogicVO.setAssociatedPhyClusterName(clusterPhyNames);
            Optional.ofNullable(clusterLogicVO.getProjectId()).map(projectService::getProjectBriefByProjectId)
                .map(ProjectBriefVO::getProjectName).ifPresent(clusterLogicVO::setProjectName);
        }
        return Result.buildSucc(list);
    }
    
    /**
     * 验证集群逻辑的参数
     *
     * @param param     要验证的参数对象。
     * @param operation OperationEnum.ADD、OperationEnum.UPDATE、OperationEnum.DELETE
     * @param projectId 项目编号
     */
    @Override
    public Result<Void> validateClusterLogicParams(ESLogicClusterDTO param, OperationEnum operation,
                                                   Integer projectId) {
        return clusterLogicService.validateClusterLogicParams(param,operation,projectId);
    }

    /**
     * “将具有给定 id 的集群加入到具有给定 id 的项目中。”
     * <p>
     * 函数的第一行是注释。编译器会忽略注释
     *
     * @param logicClusterId            要加入的集群的 ID。
     * @param joinProjectId 加入的项目ID
     * @return 返回类型是 Result<Void>
     */
    @Override
    public Result<Void> joinClusterLogic(Long logicClusterId, Integer joinProjectId) {
        final ESLogicClusterDTO param = new ESLogicClusterDTO();
        param.setId(logicClusterId);
        param.setProjectId(joinProjectId);
        param.setType(clusterLogicService.getClusterLogicByIdThatNotContainsProjectId(logicClusterId).getType());
        return Result.buildFrom(clusterLogicService.joinClusterLogic(param));
    }
    /**
     * @param level level
     * @return Result<List<ClusterLogicVO>>
     */
    @Override
    public Result<List<ClusterLogicVO>> getLogicClustersByLevel(Integer level) {
    
        List<ClusterLogicVO> list = ConvertUtil.list2List(clusterLogicService.listLogicClustersByLevelThatProjectIdStrConvertProjectIdList(level),
                ClusterLogicVO.class);
        for (ClusterLogicVO clusterLogicVO : list) {
            List<String> clusterPhyNames = clusterRegionService.listPhysicClusterNames(clusterLogicVO.getId());
            clusterLogicVO.setPhyClusterAssociated(!AriusObjUtils.isEmptyList(clusterPhyNames));
            clusterLogicVO.setAssociatedPhyClusterName(clusterPhyNames);
            Optional.ofNullable(clusterLogicVO.getProjectId()).map(projectService::getProjectBriefByProjectId)
                    .map(ProjectBriefVO::getProjectName).ifPresent(clusterLogicVO::setProjectName);
        }
        return Result.buildSucc(list);
    }
    
    @Override
    public Result<List<Tuple<Long/*逻辑集群Id*/, String/*逻辑集群名称*/>>> listProjectClusterLogicIdsAndNames(Integer projectId) {
        List<Tuple<Long/*逻辑集群Id*/, String/*逻辑集群名称*/>> res = Lists.newArrayList();
        List<ClusterLogic> tempAuthLogicClusters = Lists.newArrayList();

        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            tempAuthLogicClusters.addAll(clusterLogicService.listAllClusterLogics());
        } else {
            tempAuthLogicClusters.addAll(clusterLogicService.getHasAuthClusterLogicsByProjectId(projectId));
        }

        for (ClusterLogic clusterLogic : tempAuthLogicClusters) {
            Tuple<Long, String> logicClusterId2logicClusterNameTuple = new Tuple<>();
            logicClusterId2logicClusterNameTuple.setV1(clusterLogic.getId());
            logicClusterId2logicClusterNameTuple.setV2(clusterLogic.getName());
            res.add(logicClusterId2logicClusterNameTuple);
        }
        return Result.buildSucc(res);
    }

    @Override
    public Result<List<ClusterLogicVO>> getProjectLogicClusterInfoByType(Integer projectId, Integer type) {
        ESLogicClusterDTO logicClusterDTO = new ESLogicClusterDTO();
        if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            logicClusterDTO.setProjectId(projectId);
        }

        logicClusterDTO.setType(type);
        return Result.buildSucc(
            ConvertUtil.list2List(clusterLogicService.listClusterLogics(logicClusterDTO), ClusterLogicVO.class));
    }



    @Override
    public Result<List<ESClusterNodeSepcVO>> listMachineSpec() {
        List<ESMachineNormsPO> esMachineNormsPOS = esMachineNormsService.listMachineNorms();
        return Result.buildSucc(ConvertUtil.list2List(esMachineNormsPOS, ESClusterNodeSepcVO.class));
    }



    @Override
    public ClusterLogicVO getClusterLogic(Long clusterLogicId, Integer currentProjectId) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByIdAndProjectId(clusterLogicId, currentProjectId);
        ClusterLogicVO clusterLogicVO = ConvertUtil.obj2Obj(clusterLogic, ClusterLogicVO.class);

        FUTURE_UTIL.runnableTask(() -> buildConsoleClusterVersions(clusterLogicVO))
            .runnableTask(() -> buildOpLogicClusterPermission(clusterLogicVO, currentProjectId))
            .runnableTask(
                () -> Optional.ofNullable(projectService.getProjectBriefByProjectId(clusterLogicVO.getProjectId()))
                    .map(ProjectBriefVO::getProjectName).ifPresent(clusterLogicVO::setProjectName))
            .runnableTask(() -> buildClusterNodeInfo(clusterLogicVO)).waitExecute();

        return clusterLogicVO;
    }

    @Override
    public Result<Void> addLogicClusterAndClusterRegions(ESLogicClusterWithRegionDTO param,
                                                         String operator) throws AdminOperateException {
        return clusterRegionManager.batchBindRegionToClusterLogic(param, operator, Boolean.TRUE);
    }
    
    /**
     * 删除逻辑集群 这里是及其容易触发操作超时的，
     * 1.如果模板没有下线干净，那么这里就不能把逻辑集群下线干净，
     * 2.如果索引没有下线干净，那么逻辑集群也是不能下线的，否则系统和数据库共同残留下来，业务侧就出现了问题
     *
     * @param logicClusterId logicclusterid
     * @param operator
     * @param projectId      projectid
     * @return {@link Result}<{@link Void}>
     * @throws AdminOperateException
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Void> deleteLogicCluster(Long logicClusterId, String operator,
                                           Integer projectId) {
        //一个逻辑集群对应了多个项目的情况
        final List<ClusterLogic> clusterLogicList = clusterLogicService.listClusterLogicByIdThatProjectIdStrConvertProjectIdList(logicClusterId);
        if (CollectionUtils.isEmpty(clusterLogicList)) {
            return Result.buildSucc();
        }
        final ClusterLogic clusterLogic = clusterLogicList.stream()
                .filter(c -> Objects.equals(c.getProjectId(), projectId)).findFirst()
                .orElse(null);
        if (Objects.isNull(clusterLogic)) {
            return Result.buildFail(String.format("项目【%s】不存在逻辑集群",
                    projectService.getProjectBriefByProjectId(projectId).getProjectName()));
        }
      
        final Result<Void> checkProjectCorrectly = ProjectUtils.checkProjectCorrectly(ClusterLogic::getProjectId,
            clusterLogic, projectId);
        if (checkProjectCorrectly.failed()) {
            return checkProjectCorrectly;
        }
        
        //获取逻辑模板和索引
        ClusterLogicTemplateIndexDetailDTO templateIndexVO = getTemplateIndexVO(clusterLogic, projectId);
        try {
          
            //但是索引下线没有完成，导致了脏数据的产生
            if (CollectionUtils.isNotEmpty( templateIndexVO.getCatIndexResults())||
                CollectionUtils.isNotEmpty(templateIndexVO.getTemplates())){
                return Result.buildFail(String.format(
                        "该集群下还有%d项模板资源、%d项索引资源，如需下线集群，请前往模板管理、索引管理下线掉对应的模板及索引",
                        Optional.ofNullable(templateIndexVO.getTemplates()).orElse(Collections.emptyList()).size(),
                        Optional.ofNullable(templateIndexVO.getCatIndexResults()).orElse(Collections.emptyList()).size()
                ));
            }
          
            if (clusterLogicList.size() == 1) {
                //将region解绑
                ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(logicClusterId);
                if (Objects.nonNull(clusterRegion)) {
                    //将region解绑
                    Result<Void> unbindRes = clusterRegionService.unbindRegion(clusterRegion.getId(), logicClusterId,
                            operator);
                    if (unbindRes.failed()) {
                       throw new AdminOperateException(unbindRes.getMessage());
                    }
                }
            }
            //删除逻辑集群
            Result<Void> result = clusterLogicService.deleteClusterLogicById(logicClusterId, operator, projectId);
            if (result.success()) {
                SpringTool.publish(new ClusterLogicEvent(logicClusterId, projectId));
                //操作记录 集群下线
                operateRecordService.saveOperateRecordWithManualTrigger(clusterLogic.getName(), operator, projectId, logicClusterId,
                        OperateTypeEnum.MY_CLUSTER_OFFLINE);
                return Result.buildSuccWithTips(result.getData(),
                        String.format("逻辑集群%s下线成功", clusterLogic.getName()));
            }
            return Result.buildFailWithMsg(result.getData(),String.format("逻辑集群%s下线失败,%s", clusterLogic.getName(),
                    result.getMessage()));
        } catch (AdminOperateException | ElasticsearchTimeoutException e) {
            LOGGER.error("class={}||method=deleteLogicCluster||clusterLogic={}||es operation errMsg={}",
                    getClass().getSimpleName(), clusterLogic.getName(), e);
            // 这里必须显示事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail(String.format("逻辑集群%s下线失败,, 请重新尝试下线集群,多次重试不成功,请联系管理员", clusterLogic.getName()));
        } catch (Exception e) {
            LOGGER.error("class={}||method=deleteLogicCluster||clusterLogic={}||es operation errMsg={}",
                    getClass().getSimpleName(), clusterLogic.getName(), e);
            // 这里必须显示事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail("操作失败,请联系管理员");
        }
        
    }

    private ClusterLogicTemplateIndexDetailDTO getTemplateIndexVO(ClusterLogic clusterLogic,Integer projectId) {
        IndexTemplateDTO param = new IndexTemplateDTO();
        param.setResourceId(clusterLogic.getId());
        param.setProjectId(projectId);
        List<IndexTemplate> indexTemplates = indexTemplateService.listLogicTemplates(param);
        //通过逻辑集群获取index
        List<IndexCatCellDTO> catIndexResults = esIndexCatService.syncGetIndexByCluster(clusterLogic.getName(),projectId);
        ClusterLogicTemplateIndexDetailDTO templateIndexVO = new ClusterLogicTemplateIndexDetailDTO();
        templateIndexVO.setCatIndexResults(catIndexResults);
        templateIndexVO.setTemplates(indexTemplates);
        return templateIndexVO;
    }

    @Override
    public Result<Void> editLogicCluster(ESLogicClusterDTO param, String operator, Integer projectId) {
        final ClusterLogic logic = clusterLogicService.getClusterLogicByIdAndProjectId(param.getId(), projectId);
        Result<Void> result = clusterLogicService.editClusterLogic(param, operator, projectId);
        if (result.success()) {
            SpringTool.publish(new ClusterLogicEvent(param.getId(), projectId));
            //操作记录 我的集群信息修改
            if (StringUtils.isNotBlank(param.getMemo())) {
                 operateRecordService.saveOperateRecordWithManualTrigger(String.format("%s 修改集群描述，%s-->%s", logic.getName(), logic.getMemo(),
                         param.getMemo()), operator, projectId, param.getId(),OperateTypeEnum.MY_CLUSTER_INFO_MODIFY);
            
            }
        }
        return result;
    }
    
 
    
    @Override
    public PaginationResult<ClusterLogicVO> pageGetClusterLogics(ClusterLogicConditionDTO condition,
                                                                 Integer projectId) throws NotFindSubclassException, ESOperateException {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(CLUSTER_LOGIC.getPageSearchType());
        if (baseHandle instanceof ClusterLogicPageSearchHandle) {
            ClusterLogicPageSearchHandle pageSearchHandle = (ClusterLogicPageSearchHandle) baseHandle;
            return pageSearchHandle.doPage(condition, projectId);
        }

        LOGGER.warn(
            "class=ClusterLogicManagerImpl||method=pageGetConsoleClusterVOS||msg=failed to get the ClusterLogicPageSearchHandle");
        return PaginationResult.buildFail("分页获取逻辑集群信息失败");
    }

    @Override
    public boolean updateClusterLogicHealth(Long clusterLogicId) {
        ESLogicClusterDTO updateLogicClusterDTO = new ESLogicClusterDTO();
        Set<Integer> clusterHealthSet = Sets.newHashSet();
        updateLogicClusterDTO.setId(clusterLogicId);
        try {
            final ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogicId);
            if (Objects.isNull(clusterRegion)) {
                LOGGER.error(
                    "class=ClusterLogicManagerImpl||method=updateClusterLogicHealth||clusterLogicId={}||errMsg=clusterLogicContext is empty",
                    clusterLogicId);
                clusterHealthSet.add(UNKNOWN.getCode());
            } else {
                clusterHealthSet.add(esClusterService.syncGetClusterHealthEnum(clusterRegion.getPhyClusterName()).getCode());
            }

            updateLogicClusterDTO.setHealth(ClusterUtils.getClusterLogicHealthByClusterHealth(clusterHealthSet));
            setClusterLogicInfo(updateLogicClusterDTO,clusterRegion);
            clusterLogicService.editClusterLogicNotCheck(updateLogicClusterDTO);
        } catch (Exception e) {
            LOGGER.error("class=ClusterLogicManagerImpl||method=updateClusterLogicHealth||clusterLogicId={}||errMsg={}",
                    clusterLogicId, e.getMessage(), e);
            return false;
        }

        return true;
    }

    @Override
    public Result<ClusterLogicTemplateIndexCountVO> indexTemplateCount(Long clusterId, String operator,
                                                                       Integer projectId) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByIdAndProjectId(clusterId,projectId);
        ClusterLogicTemplateIndexDetailDTO detailVO = getTemplateIndexVO(clusterLogic,projectId);
        ClusterLogicTemplateIndexCountVO countVO = new ClusterLogicTemplateIndexCountVO();
        countVO.setCatIndexResults(detailVO.getCatIndexResults().size());
        countVO.setTemplateLogicAggregates(detailVO.getTemplates().size());
        return Result.buildSucc(countVO);
    }

    @Override
    public Result<Long> estimatedDiskSize(Long clusterLogicId, Integer count) {
        ClusterLogic clusterLogic =
                clusterLogicService.getClusterLogicByIdThatNotContainsProjectId(clusterLogicId);
        if (Objects.isNull(clusterLogic)){
            return Result.buildFail("逻辑集群不存在");
        }
        String nodeSpec = clusterLogic.getDataNodeSpec();
        if (StringUtils.isNotBlank(nodeSpec)) {
            return Result.buildSucc(getUnitSize(nodeSpec.split("-")[2]) * count);
        }
        return Result.buildSucc(UNKNOWN_SIZE);
    }

    @Override
    public Result<String> getClusterDataNodeSpec(Long clusterLogicId){
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogicId);
        Result<List<ClusterRoleHost>> result = clusterRoleHostService.listByRegionId(clusterRegion.getId().intValue());
        ClusterRoleHost clusterRoleHost = result.getData().stream().findFirst().orElse(null);
        return Result.buildSucc(clusterRoleHost.getMachineSpec());
    }

    @Override
    public Result<List<String>> listClusterLogicNameByProjectId(Integer projectId) {
        List<ClusterLogic> tempAuthLogicClusters = Lists.newArrayList();
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            tempAuthLogicClusters.addAll(clusterLogicService.listAllClusterLogics());
        } else {
            tempAuthLogicClusters.addAll(clusterLogicService.getHasAuthClusterLogicsByProjectId(projectId));
        }
        List<String> names = tempAuthLogicClusters.stream().map(ClusterLogic::getName).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(names)){
            return Result.buildFail("无集群信息，请前往集群管理-->我的集群，进行集群申请。");
        }
        return Result.buildSucc(names);
    }

    @Override
    public Result<List<Tuple<String, ClusterPhyVO>>> getClusterRelationByProjectId(Integer projectId) {

        List<Tuple<String, ClusterPhyVO>> collect;
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            List<ClusterPhy> phyList = clusterPhyService.listAllClusters();
            collect = phyList.stream().map(
                clusterPhy -> new Tuple<>(clusterPhy.getCluster(), ConvertUtil.obj2Obj(clusterPhy, ClusterPhyVO.class)))
                .collect(Collectors.toList());
        } else {
            List<ClusterLogic> logicList = clusterLogicService.getOwnedClusterLogicListByProjectId(projectId);
            collect = logicList.stream()
                .map(clusterLogic -> new Tuple<>(clusterLogic.getName(), getPhyNameByLogic(clusterLogic.getId())))
                .collect(Collectors.toList());
        }
        return Result.buildSucc(collect);
    }

    @Override
    public Result<List<PluginVO>> getClusterLogicPlugins(Long clusterId) {
        return Result
            .buildSucc(ConvertUtil.list2List(clusterLogicService.getClusterLogicPlugins(clusterId), PluginVO.class));
    }

    @Override
    public Result<Boolean> isLogicClusterRegionIsNotEmpty(Long logicClusterId) {
        if (!clusterLogicService.existClusterLogicById(logicClusterId)) {
            return Result.buildWithMsg(false, "逻辑集群不存在！");
        }
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(logicClusterId);
        if (null == clusterRegion) {
            return Result.buildWithMsg(false, "逻辑集群Region不存在！");
        }
        Result<List<ClusterRoleHost>> roleHostResult = clusterRoleHostService
            .listByRegionId(Math.toIntExact(clusterRegion.getId()));

        return Result.buildSucc(roleHostResult.success() && CollectionUtils.isNotEmpty(roleHostResult.getData()));
    }

    /**************************************************** private method ****************************************************/

    /**
     * 设置磁盘使用信息
     * @param clusterDTO
     */
    public void setClusterLogicInfo(ESLogicClusterDTO clusterDTO,ClusterRegion clusterRegion) {
        Result<List<ClusterRoleHost>> result = clusterRoleHostService
                .listByRegionId(Math.toIntExact(clusterRegion.getId()));
        if (result.success()) {
            List<String> clusterRoleHostList = result.getData().stream().map(ClusterRoleHost::getNodeSet).collect(Collectors.toList());

            Long diskTotal = 0L;
            Long diskUsage = 0L;
            Map<String, Triple<Long, Long, Double>> map = eSClusterNodeService
                    .syncGetNodesDiskUsage(clusterRegion.getPhyClusterName());
            Set<Map.Entry<String, Triple<Long, Long, Double>>> entries = map.entrySet();
            for (Map.Entry<String, Triple<Long, Long, Double>> entry : entries) {
                if (clusterRoleHostList.contains(entry.getKey())) {
                    diskTotal += entry.getValue().v1();
                    diskUsage += entry.getValue().v2();
                }
            }
            //设置节点数
            clusterDTO.setDataNodeNum(clusterRoleHostList.size());
            clusterDTO.setDiskTotal(diskTotal);
            clusterDTO.setDiskUsage(diskUsage);
            double diskUsagePercent = diskUsage != 0L && diskTotal != 0L ? CommonUtils.divideDoubleAndFormatDouble(
                    diskUsage,
                    diskTotal, 2, 1) : 0.0;
            clusterDTO.setDiskUsagePercent(diskUsagePercent);
        }

        //设置es集群版本
        ClusterPhy physicalCluster = getLogicClusterAssignedPhysicalClusters(clusterDTO.getId());
        if (physicalCluster == null) {
            return;
        }
        clusterDTO.setEsClusterVersion(physicalCluster.getEsVersion());

    }

    private ClusterPhyVO getPhyNameByLogic(Long clusterLogicId) {
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogicId);
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterRegion.getPhyClusterName());
        return ConvertUtil.obj2Obj(clusterPhy, ClusterPhyVO.class);
    }

    /**
     * 构建OP逻辑集群权限
     * @param clusterLogicVO  逻辑集群
     * @param projectIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     */
    private void buildOpLogicClusterPermission(ClusterLogicVO clusterLogicVO, Integer projectIdForAuthJudge) {
        if (clusterLogicVO == null) {
            return;
        }

        if (projectIdForAuthJudge == null) {
            // 未指定需要判断权限的app，取运维人员权限
            clusterLogicVO.setAuthId(null);
            clusterLogicVO.setAuthType(ProjectClusterLogicAuthEnum.OWN.getCode());
            clusterLogicVO.setPermissions(ProjectClusterLogicAuthEnum.OWN.getDesc());
        } else {
            // 指定了需要判断权限的app
            buildLogicClusterPermission(clusterLogicVO, projectIdForAuthJudge);
        }
    }

    /**
     * 根据物理集群名获取对应的逻辑集群列表，若传入为空，则返回全量
     * @param phyClusterName 物理集群的名称
     * @return List<String> 逻辑集群名称列表
     */
    @Override
    public List<String> listClusterLogicNameByPhyName(String phyClusterName) {
        //若传入为空，则返回全量
        if (null == phyClusterName) {
            return clusterLogicService.listAllClusterLogics()
                    .stream()
                    .map(ClusterLogic::getName)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
           return getClusterPhyAssociatedClusterLogicNames(phyClusterName);
        }
    }

    /**
     * 返回与给定应用关联的逻辑集群名称列表
     *
     * @param projectId 项目id
     * @return List<String> 逻辑集群名称列表
     */
    @Override
    public List<String> listClusterLogicNameByApp(Integer projectId) {
        List<ClusterLogic> clusterLogicList = clusterLogicService.getHasAuthClusterLogicsByProjectId(projectId);
        List<String> names = Lists.newArrayList();
        for (ClusterLogic clusterLogic : clusterLogicList) {
            names.add(clusterLogic.getName());
        }
        return names;
    }

    /**
     * 返回与给定物理集群名称关联的逻辑集群名称列表
     *
     * @param phyClusterName 物理集群的名称。
     * @return 与给定集群物理名称关联的集群逻辑名称列表。
     */
    @Override
    public List<String> getClusterPhyAssociatedClusterLogicNames(String phyClusterName) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(phyClusterName);
        if (null == clusterPhy) {
            LOGGER.error(
                    "class=ClusterContextManagerImpl||method=flushClusterPhyContext||clusterPhyName={}||msg=clusterPhy is empty",
                    phyClusterName);
            return Collections.emptyList();
        }

        final List<Long> logicIds = clusterRegionManager.listRegionByPhyCluster(phyClusterName).stream()
                .filter(clusterRegion -> Objects.nonNull(clusterRegion.getLogicClusterIds()))
                .map(clusterRegion -> ListUtils.string2LongList(clusterRegion.getLogicClusterIds()))
                .filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream)
                .filter(logicId -> !Objects.equals(logicId,
                        Long.parseLong(AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID))).distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(logicIds)){
            return Collections.emptyList();
        }
       return clusterLogicService.getClusterLogicListByIds(logicIds)
                .stream()
                .map(ClusterLogic::getName)
                .distinct()
                .collect(Collectors.toList());

    }
    
    /**
     * 加入物理集群并创建逻辑集群
     *
     * @param param     ClusterJoinDTO
     * @param projectId 项目编号
     * @return joinClusterPhyAndCreateLogicCluster 方法的结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> joinClusterPhyAndCreateLogicCluster(ClusterJoinDTO param, Integer projectId)
            throws AdminOperateException {
        final Result<ClusterPhyVO> voResult = clusterPhyManager.joinCluster(param, AriusUser.SYSTEM.getDesc(), projectId);
        if (voResult.failed()) {
            return Result.buildFrom(voResult);
        }
        final Integer clusterPhyId = voResult.getData().getId();
        final Result<List<ESClusterRoleHostWithRegionInfoVO>> listResult = clusterNodeManager.listDivide2ClusterNodeInfo(
                clusterPhyId.longValue());
        if (listResult.failed()) {
            return Result.buildFrom(listResult);
        }
        final List<Integer> regionIds = listResult.getData().stream()
                .map(ESClusterRoleHostVO::getId).distinct().map(Long::intValue).collect(Collectors.toList());
        final ClusterRegionWithNodeInfoDTO clusterRegionWithNodeInfoDTO =
                new ClusterRegionWithNodeInfoDTOBuilder()
                .withBindingNodeIds(regionIds).withName(param.getCluster()).withLogicClusterIds("-1")
                .withPhyClusterName(param.getCluster()).build();
    
        final Result<List<Long>> result = clusterNodeManager.createMultiNode2Region(
                Lists.newArrayList(clusterRegionWithNodeInfoDTO), AriusUser.SYSTEM.getDesc(), projectId);
        if (result.failed()) {
            return Result.buildFrom(result);
        }
        final Long regionId = result.getData().get(0);
        ClusterRegionDTO clusterRegionDTO = ClusterRegionDTO.builder().id(regionId).logicClusterIds("-1")
                .phyClusterName(param.getCluster()).name(param.getCluster()).build();
        final ESLogicClusterWithRegionDTO esLogicClusterWithRegionDTO = new ESLogicClusterWithRegionDTOBuilder()
                .withProjectId(AuthConstant.DEFAULT_METADATA_PROJECT_ID).withName(clusterRegionDTO.getName()).withLevel(1).withType(1)
                .withDataNodeSpec("").withClusterRegionDTOS(Lists.newArrayList(clusterRegionDTO)).build();
       
        final Result<Void> voidResult = addLogicClusterAndClusterRegions(esLogicClusterWithRegionDTO, AriusUser.SYSTEM.getDesc());
        if (voidResult.failed()) {
            return Result.buildFrom(voidResult);
        }
        final ClusterLogic logic = clusterLogicService.getClusterLogicByNameThatNotContainsProjectId(
                param.getCluster());
    
        return Result.buildSucc(logic.getId());
    }
    
    @Override
    public Result<List<ClusterPhyWithLogicClusterVO>> listLogicClusterWithClusterPhyByProjectId(Integer projectId) {
        List<ClusterLogic> clusterLogics = clusterLogicService.getOwnedClusterLogicListByProjectId(projectId);
        if (CollectionUtils.isEmpty(clusterLogics)) {
            return Result.buildSucc(Collections.emptyList());
        }
        Map<Long, String> clusterLogicId2ClusterLogicNameMap = ConvertUtil.list2Map(clusterLogics, ClusterLogic::getId,
                ClusterLogic::getName);
        List<Long> logicClusterIdList = clusterLogics.stream().map(ClusterLogic::getId).distinct()
                .collect(Collectors.toList());
        List<ClusterRegion> regions = clusterRegionService.getClusterRegionsByLogicIds(logicClusterIdList);
        if (CollectionUtils.isEmpty(regions)) {
            return Result.buildSucc(Collections.emptyList());
        }
        // 逻辑集群 -》region 1-1 所以
        Function<ClusterRegion, List<TupleTwo<Long, String>>> logicClusterIds2logicClusterIdLongWithClusterPhyFunc = clusterRegion -> Arrays.stream(
                        StringUtils.split(clusterRegion.getLogicClusterIds(), ",")).filter(StringUtils::isNumeric)
                .map(Long::parseLong)
                .map(logicClusterId -> Tuples.of(logicClusterId, clusterRegion.getPhyClusterName()))
                .collect(Collectors.toList());
        // 转换
        Function<TupleTwo</*clusterLogicId*/Long,/*clusterPhy*/ String>, ClusterPhyWithLogicClusterVO> clusterLogicId2clusterPhyFunc = tuple -> {
            Long logicId = tuple.v1;
            String clusterPhy = tuple.v2;
            if (clusterLogicId2ClusterLogicNameMap.containsKey(logicId)) {
                return ClusterPhyWithLogicClusterVO.builder().clusterPhy(clusterPhy).clusterLogicId(logicId)
                        .clusterLogic(clusterLogicId2ClusterLogicNameMap.get(logicId)).build();
            }
        
            return null;
        };
    
        List<ClusterPhyWithLogicClusterVO> clusterPhyWithLogicClusterList = regions.stream()
                .map(logicClusterIds2logicClusterIdLongWithClusterPhyFunc).flatMap(Collection::stream).distinct()
                .map(clusterLogicId2clusterPhyFunc).filter(Objects::nonNull).collect(Collectors.toList());
        return Result.buildSucc(clusterPhyWithLogicClusterList);
    }

    @Override
    public Result<List<ClusterLogicVO>> listClusterLogicByPhyName(String phyClusterName) {
        if (StringUtils.isNotBlank(phyClusterName)) {
            ClusterPhy clusterPhy = clusterPhyService.getClusterByName(phyClusterName);
            if (null == clusterPhy) {
                LOGGER.error(
                    "class=ClusterContextManagerImpl||method=flushClusterPhyContext||clusterPhyName={}||msg=clusterPhy is empty",
                    phyClusterName);
                return Result.buildFail("物理集群不存在！");
            }

            final List<Long> logicIds = clusterRegionManager.listRegionByPhyCluster(phyClusterName).stream()
                .filter(clusterRegion -> Objects.nonNull(clusterRegion.getLogicClusterIds()))
                .map(clusterRegion -> ListUtils.string2LongList(clusterRegion.getLogicClusterIds()))
                .filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream)
                .filter(logicId -> !Objects.equals(logicId,
                    Long.parseLong(AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID)))
                .distinct().collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(logicIds)) {
                return Result.buildSucc(ConvertUtil.list2List(clusterLogicService.getClusterLogicListByIds(logicIds),
                    ClusterLogicVO.class));
            }
            return Result.buildSucc(Collections.emptyList());
        }
        //若传入为空，则返回全量
        return Result
            .buildSucc(ConvertUtil.list2List(clusterLogicService.listAllClusterLogics(), ClusterLogicVO.class));
    }

    /**
     * 构建ES集群版本
     * @param logicCluster 逻辑集群
     */
    private void buildConsoleClusterVersions(ClusterLogicVO logicCluster) {
        try {
            if (logicCluster != null) {
                ClusterPhy physicalCluster = getLogicClusterAssignedPhysicalClusters(logicCluster.getId());
                if (physicalCluster == null) {
                    return;
                }
                logicCluster.setEsClusterVersion(physicalCluster.getEsVersion());
            }
        } catch (Exception e) {
            LOGGER.warn("class=LogicClusterManager||method=buildConsoleClusterVersions||logicClusterId={}",
                logicCluster.getId(), e);
        }
    }

    /**
     * 设置app对指定逻辑集群的权限
     * @param logicClusterVO      逻辑集群
     * @param projectIdForAuthJudge 需要判断的app的ID
     */
    private void buildLogicClusterPermission(ClusterLogicVO logicClusterVO, Integer projectIdForAuthJudge) {
        try {
            if (logicClusterVO == null || projectIdForAuthJudge == null) {
                return;
            }
            if (AuthConstant.SUPER_PROJECT_ID.equals(projectIdForAuthJudge)) {
                logicClusterVO.setAuthType(ProjectClusterLogicAuthEnum.OWN.getCode());
                logicClusterVO.setPermissions(ProjectClusterLogicAuthEnum.OWN.getDesc());
                return;
            }

            ProjectClusterLogicAuth auth = projectClusterLogicAuthService.getLogicClusterAuth(projectIdForAuthJudge,
                logicClusterVO.getId());

            if (auth == null) {
                // 没有权限
                logicClusterVO.setAuthId(null);
                logicClusterVO.setAuthType(ProjectClusterLogicAuthEnum.NO_PERMISSIONS.getCode());
                logicClusterVO.setPermissions(ProjectClusterLogicAuthEnum.NO_PERMISSIONS.getDesc());
            } else {
                // 有权限
                logicClusterVO.setAuthId(auth.getId());
                logicClusterVO.setAuthType(ProjectClusterLogicAuthEnum.valueOf(auth.getType()).getCode());
                logicClusterVO.setPermissions(ProjectClusterLogicAuthEnum.valueOf(auth.getType()).getDesc());
            }

        } catch (Exception e) {
            LOGGER.warn("class=LogicClusterManager||method=buildLogicClusterPermission||logicClusterId={}",
                logicClusterVO.getId(), e);
        }
    }

    /**
     * 更新逻辑集群状态信息
     */
    private void buildLogicClusterStatus(ClusterLogicVO logicCluster, ClusterLogic clusterLogic) {
        ClusterLogicStatis esClusterLogicStatus = buildDefaultLogicStatus();
        try {
            ClusterLogicStatis clusterLogicStatus = getClusterLogicStatus(clusterLogic.getId());
            if (null != clusterLogicStatus) {
                esClusterLogicStatus = clusterLogicStatus;
            }
        } catch (Exception e) {
            LOGGER.error("class=ClusterLogicManagerImpl||method=buildLogicClusterStatus||logicClusterId={}",
                logicCluster.getId(), e);
        }

        logicCluster.setClusterStatus(ConvertUtil.obj2Obj(esClusterLogicStatus, ConsoleClusterStatusVO.class));
    }

    /**
     * 创建默认逻辑集群状态
     * @return  ClusterLogicStatis
     */
    private ClusterLogicStatis buildDefaultLogicStatus() {
        ClusterLogicStatis logicStatus = new ClusterLogicStatis();
        logicStatus.setStatus(ClusterHealthEnum.RED.getDesc());
        logicStatus.setDocNu(0.0);
        logicStatus.setIndexNu(0);
        logicStatus.setTotalDisk(0.0);
        logicStatus.setUsedDisk(0.0);
        return logicStatus;
    }

    private void buildLogicRole(ClusterLogicVO logicCluster, ClusterLogic clusterLogic) {
        if (logicCluster != null) {
            try {
                Long logicClusterId = logicCluster.getId();

                List<String> phyClusterNames = clusterRegionService.listPhysicClusterNames(logicClusterId);
                if (CollectionUtils.isEmpty(phyClusterNames)) {
                    return;
                }

                //拿第一个物理集群的client、master信息，因为只有Arius维护的大公共共享集群才会有一个逻辑集群映射成多个物理集群
                ClusterPhy clusterPhy = clusterPhyService.getClusterByName(phyClusterNames.get(0));
                if (null == clusterPhy) {
                    return;
                }

                List<ClusterRoleInfo> esRolePhyClusters = clusterPhy.getClusterRoleInfos();
                List<ClusterRoleHost> esRolePhyClusterHosts = clusterPhy.getClusterRoleHosts();

                logicCluster.setEsClusterRoleVOS(
                    buildESRoleClusterVOList(clusterLogic, logicClusterId, esRolePhyClusters, esRolePhyClusterHosts));
            } catch (Exception e) {
                LOGGER.warn("class=LogicClusterManager||method=buildLogicRole||logicClusterId={}", logicCluster.getId(),
                    e);
            }
        }
    }

    private List<ESClusterRoleVO> buildESRoleClusterVOList(ClusterLogic clusterLogic, Long logicClusterId,
                                                           List<ClusterRoleInfo> esRolePhyClusters,
                                                           List<ClusterRoleHost> esRolePhyClusterHosts) {
        List<ESClusterRoleVO> esClusterRoleVOS = new ArrayList<>();
        for (ClusterRoleInfo clusterRoleInfo : esRolePhyClusters) {
            ESClusterRoleVO esClusterRoleVO = ConvertUtil.obj2Obj(clusterRoleInfo, ESClusterRoleVO.class);

            List<ESClusterRoleHostVO> esClusterRoleHostVOS = new ArrayList<>();

            //如果是datanode节点，那么使用逻辑集群申请的节点个数和阶段规格配置
            if (DATA_NODE.getDesc().equals(clusterRoleInfo.getRoleClusterName())) {
                buildEsClusterRoleHostVOList(clusterLogic, logicClusterId, esClusterRoleVO, esClusterRoleHostVOS);
            } else {
                for (ClusterRoleHost clusterRoleHost : esRolePhyClusterHosts) {
                    if (clusterRoleHost.getRoleClusterId().longValue() == clusterRoleInfo.getId().longValue()) {
                        esClusterRoleHostVOS.add(ConvertUtil.obj2Obj(clusterRoleHost, ESClusterRoleHostVO.class));
                    }
                }
            }

            esClusterRoleVO.setEsClusterRoleHostVO(esClusterRoleHostVOS);
            esClusterRoleVO.setPodNumber(esClusterRoleHostVOS.size());
            esClusterRoleVOS.add(esClusterRoleVO);
        }
        return esClusterRoleVOS;
    }

    private void buildEsClusterRoleHostVOList(ClusterLogic clusterLogic, Long logicClusterId,
                                              ESClusterRoleVO esClusterRoleVO,
                                              List<ESClusterRoleHostVO> esClusterRoleHostVOS) {
        esClusterRoleVO.setPodNumber(clusterLogic.getDataNodeNum());
        esClusterRoleVO.setMachineSpec(clusterLogic.getDataNodeSpec());

        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(logicClusterId);
        if (null != clusterRegion) {
            Result<List<ClusterRoleHost>> ret = clusterRoleHostService.listByRegionId(clusterRegion.getId().intValue());
            if (ret.success() && CollectionUtils.isNotEmpty(ret.getData())) {
                for (ClusterRoleHost clusterRoleHost : ret.getData()) {
                    ESClusterRoleHostVO esClusterRoleHostVO = new ESClusterRoleHostVO();
                    esClusterRoleHostVO.setHostname(clusterRoleHost.getIp());
                    esClusterRoleHostVO.setRole(DATA_NODE.getCode());

                    esClusterRoleHostVOS.add(esClusterRoleHostVO);
                }
            }
        }
    }

    /**
     * 构建节点信息:
     * 1. 是否关联物理集群
     * 2. 获取关联物理集群列表
     * 3. 逻辑集群拥有的数据节点数
     * 4. 防止没有关联物理集群, 或者取消关联region, 逻辑集群状态为red
     * 5. 获取gateway地址
     */
    private void buildClusterNodeInfo(ClusterLogicVO clusterLogicVO) {
        //获取gateway地址
        clusterLogicVO.setGatewayAddress(esGatewayClient.getGatewayAddress());
        //获取活跃分片数
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogicVO.getId());
        AtomicReference<Long> activeShardNum = new AtomicReference<>(0L);
        if (Objects.nonNull(clusterRegion)){
            Map<String/*node*/, Long /*shardNum*/>  nodeShardsNum = eSClusterNodeService.syncGetNode2ShardNumMap(clusterRegion.getPhyClusterName());
            Result<List<ESClusterRoleHostVO>>  ESClusterRoleHostVORes =clusterNodeManager.listClusterLogicNode(Math.toIntExact(clusterLogicVO.getId()));
            ESClusterRoleHostVORes.getData().stream().forEach(node->{
                activeShardNum.updateAndGet(v -> v + nodeShardsNum.get(node.getNodeSet()));
            });
        }

        clusterLogicVO.setActiveShardNum(activeShardNum.get());
    }

    private TupleTwo<Result<Void>, /*projectId*/Integer> checkIndices(List<String> delIndices, Integer logicId) {
        for (String index : delIndices) {
            if (index.endsWith("*")) {
                return Tuples.of(Result.buildParamIllegal("索引名字不能以*结尾"), null);
            }
        }

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);
        IndexTemplatePhy templatePhysical = templateLogicWithPhysical.getMasterPhyTemplate();

        List<String> matchIndices = indexTemplatePhyService.getMatchNoVersionIndexNames(templatePhysical.getId());
        for (String index : delIndices) {
            if (!matchIndices.contains(index)) {
                return Tuples.of(Result.buildParamIllegal(index + "不属于该索引模板"), null);
            }
        }
        return Tuples.of(Result.buildSucc(), templateLogicWithPhysical.getProjectId());
    }

    /**
     * 批量删除物理模板对应分区索引
     * @param physicals 物理模板列表
     * @param delIndices 待删除分区索引列表
     * @return 成功/失败
     */
    private Result<Void> batchDeletePhysicalTemplateIndices(List<IndexTemplatePhy> physicals, List<String> delIndices) {
        for (IndexTemplatePhy templatePhysical : physicals) {
            if (templatePhysical.getVersion() > 0) {
                List<String> delIndicesWithVersion = genDeleteIndicesWithVersion(delIndices);

                LOGGER.info(
                    "class=TemplateLogicServiceImpl||method=clearIndex||templateName={}||version={}||indices={}",
                    templatePhysical.getName(), templatePhysical.getVersion(), delIndicesWithVersion);

                if (CollectionUtils.isNotEmpty(delIndicesWithVersion)) {
                    esIndexService.syncBatchDeleteIndices(templatePhysical.getCluster(), delIndicesWithVersion, 3);
                }
            }

            int count = delIndices.size();
            if (count != esIndexService.syncBatchDeleteIndices(templatePhysical.getCluster(), delIndices, 3)) {
                return Result.buildFail("删除索引失败，请重试");
            }
        }

        return Result.buildSucc();
    }

    /**
     * 通过请求的方式批量删除物理模板分区索引
     * @param physicals 物理模板
     * @param delQueryDsl DSL语句
     * @param delIndices 删除索引列表
     * @return 成功/失败
     */
    private Result<Void> batchDeletePhysicalTemplateIndicesByQuery(List<IndexTemplatePhy> physicals, String delQueryDsl,
                                                                   List<String> delIndices) throws ESOperateException {
        if (StringUtils.isNotBlank(delQueryDsl)) {
            for (IndexTemplatePhy templatePhysical : physicals) {
                if (!esIndexService.syncDeleteByQuery(templatePhysical.getCluster(), delIndices, delQueryDsl)) {
                    return Result.buildFail("删除索引失败，请重试");
                }
            }
        }

        return Result.buildSucc();
    }

    /**
     * 生成带有版本模式的待删除索引列表
     *
     * @param delIndices 待删除索引列表
     * @return 索引名称列表
     */
    private List<String> genDeleteIndicesWithVersion(List<String> delIndices) {
        List<String> indicesWithVersion = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(delIndices)) {
            for (String delIndex : delIndices) {
                indicesWithVersion.add(delIndex + "_v*");
            }
        }

        return indicesWithVersion;
    }

    private ClusterLogicStatis getClusterLogicStatus(Long logicClusterId) {
        ClusterLogicStatis clusterLogicStatis = new ClusterLogicStatis();

        //获取集群上下文
        //todo 暂时留存
        //ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContext(logicClusterId);
        //if (null == clusterLogicContext) {
        //    return null;
        //}
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByIdThatNotContainsProjectId(
                logicClusterId);
        if (Objects.isNull(clusterLogic)){
            return null;
        }

        //设置逻辑集群名称
        //clusterLogicStatis.setName(clusterLogicService.getClusterLogicName());
        clusterLogicStatis.setName(clusterLogic.getName());
        clusterLogicStatis.setId(logicClusterId);

        List<ESClusterStatsResponse> esClusterStatsResponseList =
                //clusterLogicContext.getAssociatedClusterPhyNames()
        clusterRegionService.listPhysicClusterNames(clusterLogic.getId())
            .stream().map(esClusterService::syncGetClusterStats).collect(Collectors.toList());

        //设置基础数据
        clusterLogicStatis
            .setIndexNu(esClusterStatsResponseList.stream().mapToLong(ESClusterStatsResponse::getIndexCount).sum());
        clusterLogicStatis
            .setDocNu(esClusterStatsResponseList.stream().mapToDouble(ESClusterStatsResponse::getDocsCount).sum());
        clusterLogicStatis
            .setTotalDisk(esClusterStatsResponseList.stream().mapToDouble(item -> item.getTotalFs().getBytes()).sum());
        clusterLogicStatis.setUsedDisk(esClusterStatsResponseList.stream()
            .mapToDouble(item -> item.getTotalFs().getBytes() - item.getFreeFs().getBytes()).sum());

        //设置逻辑集群状态
        Set<String> statusSet = esClusterStatsResponseList.stream().map(ESClusterStatsResponse::getStatus)
            .collect(Collectors.toSet());
        clusterLogicStatis.setStatus(getClusterLogicStatus(statusSet));
        return clusterLogicStatis;
    }

    @Override
    public Result<Void> deleteTemplatesIndicesInfo(Long clusterLogicId,Integer projectId,String operator) {
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogicId);
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByIdAndProjectId(clusterLogicId,projectId);
        if (Objects.isNull(clusterRegion)){
            return Result.buildFail("该逻辑集群未绑定region!");
        }
        //获取物理模板
        Result<List<IndexTemplatePhy>> indexTemplatePhys = indexTemplatePhyService.listByRegionId(Math.toIntExact(clusterRegion.getId()));
        //获取逻辑模板
        Result<List<IndexTemplate>> indexTemplates = indexTemplateService.listByRegionId(Math.toIntExact(clusterRegion.getId()));
        List<String> indices = esIndexCatService.syncGetIndexListByProjectId(projectId,clusterLogic.getName());
        if (indexTemplatePhys.getData().size() == 0&&indexTemplates.getData().size() == 0&&indices.size()==0){
            return Result.buildFail("该逻辑集群下无数据!");
        }
        //删除数据
        for (IndexTemplate indexTemplate:indexTemplates.getData()) {
            try {
                indexTemplateService.delTemplate(indexTemplate.getId(),operator);
                indexTemplatePhyService.delTemplateByLogicId(indexTemplate.getId(),operator);
            } catch (AdminOperateException e) {
                return Result.buildFail("数据删除错误!");
            }
        }
        return Result.buildSucc();
    }

    /**
     * 获取逻辑集群状态
     * @return ClusterStatusEnum
     */
    private String getClusterLogicStatus(Set<String> statusSet) {
        if (statusSet.contains(RED.getDesc())) {
            return RED.getDesc();
        }

        if (statusSet.size() == 1 && statusSet.contains(GREEN.getDesc())) {
            return GREEN.getDesc();
        }

        if (statusSet.contains(YELLOW.getDesc())) {
            return YELLOW.getDesc();
        }
        return RED.getDesc();
    }
}