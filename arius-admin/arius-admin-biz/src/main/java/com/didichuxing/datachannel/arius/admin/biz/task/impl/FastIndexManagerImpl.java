package com.didichuxing.datachannel.arius.admin.biz.task.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.FAST_INDEX_TASK_LOG;
import static com.didichuxing.datachannel.arius.admin.common.constant.task.FastIndexConstant.*;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.TEMPLATE_INDEX_INCLUDE_NODE_NAME;
import static java.util.regex.Pattern.compile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplatePhySettingManager;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySetting;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastIndexBriefVO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.*;
import com.didichuxing.datachannel.arius.admin.common.event.template.LogicTemplateCreatePipelineEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.index.MultiIndexsConfig;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.template.TemplateConfig;
import com.didiglobal.knowframework.elasticsearch.client.utils.JsonUtils;
import com.didiglobal.knowframework.security.service.ProjectService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.page.FastIndexTaskLogPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.task.FastIndexManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.BaseResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex.ESIndexMoveTaskAdjustReadRateContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex.ESIndexMoveTaskContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex.ESIndexMoveTaskStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex.FastIndexTaskInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastDumpTaskLogVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastIndexDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastIndexStats;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.task.OpTaskService;
import com.didichuxing.datachannel.arius.admin.core.service.task.fastindex.FastIndexTaskService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.remote.fastindex.ESIndexMoveTaskService;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.index.IndexConfig;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Service
public class FastIndexManagerImpl implements FastIndexManager {
    protected static final ILog                                      LOGGER                                = LogFactory
        .getLog(BaseTemplateSrvImpl.class);

    private static final Integer                                     TASK_EXECUTE_BATCH_SIZE               = 10;
    private static final Result<Void>                                FAIL_RESULT_GET_OP_TASK_ERROR         = Result
        .buildFail("获取数据迁移主任务失败");
    private static final Result<Void>                                FAIL_RESULT_GET_FAST_INDEX_TASK_ERROR = Result
        .buildFail("获取数据迁移任务原始信息失败");
    private static final String                                      GET_INDEX_FAILED_MSG                  = "获取原集群中的索引【%s】失败！";
    private static final String                                      GET_READ_FILE_RATE_LIMIT_MSG          = "设置的任务读取速率不能小于【%d】";
    private static final String                                      INDEX_CREATION_DATE          = "index.creation_date";
    private static final String                                      INDEX_UUID          = "index.uuid";
    private static final String                                      INDEX_VERSION_CREATED          = "index.version.created";
    private static final String                                      INDEX_PROVIDED_NAME          = "index.provided_name";
    private static final String                                      INDEX_ROUTING_ALLOCATION_INCLUDE_NAME          = "index.routing.allocation.include._name";
    private static final String                                      INDEX_ROUTING_ALLOCATION_INCLUDE_TIER_PREFERENCE          = "index.routing.allocation.include._tier_preference";
    private static final long                                        TASK_WAITING_TIME                     = 5 * 1000L;
    private static final String                                   VERSION_PREFIX_PATTERN                      = "^\\d*.\\d*.\\d*";
    @Value(value = "${fast.dump.port}")
    private int                                      fastDumpPort;

    @Autowired
    protected IndexTemplateService                                   indexTemplateService;

    @Autowired
    protected IndexTemplatePhyService                                indexTemplatePhyService;

    @Autowired
    protected TemplateLogicManager                                   templateLogicManager;

    @Autowired
    protected ClusterPhyService                                      clusterPhyService;
    @Autowired
    private ClusterRegionService                                     clusterRegionService;
    @Autowired
    protected OperateRecordService                                   operateRecordService;

    @Autowired
    private ClusterRoleHostService                                   clusterRoleHostService;
    @Autowired
    protected ESClusterService                                       esClusterService;

    @Autowired
    private ESIndexService                                           esIndexService;

    @Autowired
    private ESTemplateService                                        esTemplateService;
    @Autowired
    private FastIndexTaskService                                     fastIndexTaskService;
    @Autowired
    private OpTaskService                                            opTaskService;
    @Autowired
    private ESIndexMoveTaskService                                   esIndexMoveTaskService;
    @Autowired
    private HandleFactory                                            handleFactory;
    @Autowired
    private ClusterLogicService                                      clusterLogicService;
    @Autowired
    private ProjectService                                            projectService;
    @Autowired
    private TemplatePhySettingManager                                 templatePhySettingManager;

    private static final FutureUtil<Result<List<FastIndexTaskInfo>>> FAST_INDEX_TASK_TEMPLATE_FUTURE_UTIL  = FutureUtil
        .init("FastIndexTaskTemplate", 10, 20, 1000);

    private static final FutureUtil<Result<FastIndexTaskInfo>>       FAST_INDEX_TASK_INDEX_FUTURE_UTIL     = FutureUtil
        .init("FastIndexTaskIndex", 10, 20, 1000);

    private static final FutureUtil<Result>                          FAST_INDEX_MANAGER_FUTURE_UTIL        = FutureUtil
        .init("FastIndexManager", 10, 20, 1000);

    /**
     * 提交数据迁移任务
     * 1.检查集群是否存在
     * 2.检查任务提交地址是否连通
     * 3.检查索引是否存在，拉取原集群的索引信息
     * 4.目标集群中创建索引数据
     * 5.保存主任务
     * 6.拆分为子任务存储
     *
     * @param fastIndexDTO 数据迁移任务
     * @param operator 操作人
     * @param projectId 当前项目
     * @return  Result<WorkTaskVO>
     */
    @Override
    @Transactional
    public Result<WorkTaskVO> submitTask(FastIndexDTO fastIndexDTO, String operator, Integer projectId) {

        Result<Void> checkRet = checkTask(fastIndexDTO);
        if (checkRet.failed()) {
            return Result.buildFrom(checkRet);
        }
        ClusterPhy sourceCluster = clusterPhyService.getClusterByName(fastIndexDTO.getSourceCluster());
        ClusterPhy targetCluster = clusterPhyService.getClusterByName(fastIndexDTO.getTargetCluster());

        //检查原索引是否存在，创建目标模版，获取索引任务列表
        List<FastIndexTaskDTO> taskList = fastIndexDTO.getTaskList();
        List<FastIndexTaskInfo> taskIndexList = Lists.newArrayList();
        Result<List<FastIndexTaskInfo>> taskIndexListRet = Result.buildFail("获取索引任务失败！");
        if (FastIndexConstant.DATA_TYPE_TEMPLATE.equals(fastIndexDTO.getDataType())) {
            ClusterRegion targetClusterRegion = clusterRegionService
                .getRegionByLogicClusterId(fastIndexDTO.getTargetLogicClusterId());
            taskIndexListRet = createTemplateAndGetTaskIndexListByTemplate(fastIndexDTO, targetClusterRegion, taskList, sourceCluster, targetCluster);
        } else if (FastIndexConstant.DATA_TYPE_INDEX.equals(fastIndexDTO.getDataType())) {
            taskIndexListRet = getTaskIndexListByIndex(fastIndexDTO, taskList);
        }
        if (taskIndexListRet.failed()) {
            return Result.buildFrom(taskIndexListRet);
        } else if (CollectionUtils.isNotEmpty(taskIndexListRet.getData())) {
            taskIndexList.addAll(taskIndexListRet.getData());
        } else {
            return Result.buildFail("未获取到索引！");
        }

        //创建目标索引
        Result<Void> createIndexRet = createTargetIndex(targetCluster, taskIndexList);
        if (createIndexRet.failed()) {
            return Result.buildFrom(createIndexRet);
        }
        //2.1 设置基础数据
        String businessKey = String.format("%s=>>%s", sourceCluster.getCluster(), targetCluster.getCluster());
        Result<OpTask> workTaskResult = addOpTask(fastIndexDTO, operator, businessKey);
        if (workTaskResult.failed()) {
            return Result.buildFrom(workTaskResult);
        }
        //拆分任务数据到数据库
        OpTask opTask = workTaskResult.getData();
        boolean ret = saveIndexTask(fastIndexDTO, taskIndexList, opTask.getId());

        //记录操作
        if (ret) {
            if (FastIndexConstant.DATA_TYPE_TEMPLATE.equals(fastIndexDTO.getDataType())) {
                //索引模板（索引）
                String dataTypeName = "索引模版";
                String indexName = taskIndexList.stream().map(FastIndexTaskInfo::getTemplateName)
                    .collect(Collectors.joining("、"));
                operateRecordService
                        .saveOperateRecordWithManualTrigger(
                                String.format("%s数据迁移至%s，迁移%s：%s", sourceCluster.getCluster(), targetCluster.getCluster(),
                                        dataTypeName, indexName),
                                operator, projectId, businessKey, OperateTypeEnum.PHYSICAL_CLUSTER_FAST_INDEX, fastIndexDTO.getSourceProjectId());
            } else if (FastIndexConstant.DATA_TYPE_INDEX.equals(fastIndexDTO.getDataType())) {
                String dataTypeName = "索引";
                String indexName = taskIndexList.stream().map(FastIndexTaskInfo::getIndexName)
                    .collect(Collectors.joining("、"));
                operateRecordService
                        .saveOperateRecordWithManualTrigger(
                                String.format("%s数据迁移至%s，迁移%s：%s", sourceCluster.getCluster(), targetCluster.getCluster(),
                                        dataTypeName, indexName),
                                operator, projectId, businessKey, OperateTypeEnum.PHYSICAL_CLUSTER_FAST_INDEX);
            }
        } else {
            //这里如果子任务写入失败，则显式进行事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail("子任务保存失败！");
        }

        return Result.buildSucc(ConvertUtil.obj2Obj(workTaskResult.getData(), WorkTaskVO.class));
    }

    @Override
    public Result<Void> refreshTask(Integer taskId) {
        OpTask opTask = opTaskService.getById(taskId);
        if (null == opTask) {
            return FAIL_RESULT_GET_OP_TASK_ERROR;
        }

        OpTaskStatusEnum taskStatusEnum = OpTaskStatusEnum.valueOfStatus(opTask.getStatus());
        if (taskStatusEnum != OpTaskStatusEnum.RUNNING && taskStatusEnum != OpTaskStatusEnum.WAITING) {
            return Result.buildSucc();
        }
        FastIndexDTO fastIndexDTO = JSON.parseObject(opTask.getExpandData(), FastIndexDTO.class);
        //挑选任务并提交
        if (null == fastIndexDTO) {
            //打印日志
            return FAIL_RESULT_GET_FAST_INDEX_TASK_ERROR;
        }
        Date scheduledTaskStartTime = fastIndexDTO.getTaskStartTime();
        if (null != scheduledTaskStartTime && DateTimeUtil.isAfterDateTime(scheduledTaskStartTime, 0)) {
            //如果任务调度时间在当前时间之后，则暂时不提交任务
            return Result.buildSucc();
        }

        ClusterPhy sourceCluster = clusterPhyService.getClusterByName(fastIndexDTO.getSourceCluster());
        if (null == sourceCluster) {
            return Result.buildFail("获取源物理集群信息失败");
        }
        ClusterPhy targetCluster = clusterPhyService.getClusterByName(fastIndexDTO.getTargetCluster());
        if (null == targetCluster) {
            return Result.buildFail("获取目标物理集群信息失败");
        }

        Result<Void> submitIndexTaskRet = submitIndexTask(opTask.getId(), fastIndexDTO, sourceCluster, targetCluster);
        if (submitIndexTaskRet.failed()) {
            return Result.buildFrom(submitIndexTaskRet);
        }
        Result<Void> refreshIndexTaskStatsRet = refreshIndexTaskStats(opTask.getId(), fastIndexDTO);
        if (refreshIndexTaskStatsRet.failed()) {
            return Result.buildFrom(refreshIndexTaskStatsRet);
        }
        return refreshTaskStatus(opTask);
    }

    @Override
    public Result<Void> cancelTask(Integer taskId, Integer projectId) {
        if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            return Result.buildFail("越权操作，请更换项目或者更换账号");
        }
        OpTask opTask = opTaskService.getById(taskId);
        if (null == opTask) {
            return FAIL_RESULT_GET_OP_TASK_ERROR;
        }

        if (!OpTaskTypeEnum.FAST_INDEX.getType().equals(opTask.getTaskType())) {
            return Result.buildFail("任务类型异常，非数据迁移任务不支持取消任务！");
        }
        OpTaskStatusEnum taskStatusEnum = OpTaskStatusEnum.valueOfStatus(opTask.getStatus());
        if (OpTaskStatusEnum.RUNNING != taskStatusEnum
            && OpTaskStatusEnum.WAITING != OpTaskStatusEnum.valueOfStatus(opTask.getStatus())) {
            return Result.buildFail("只有等待和运行中的数据迁移任务可以取消，该任务状态异常，不支持取消操作！");
        }

        FastIndexDTO fastIndexDTO = JSON.parseObject(opTask.getExpandData(), FastIndexDTO.class);
        //挑选任务并提交
        if (null == fastIndexDTO) {
            return FAIL_RESULT_GET_FAST_INDEX_TASK_ERROR;
        }

        Result<Void> refreshIndexTaskStatsRet = refreshIndexTaskStats(taskId, fastIndexDTO);
        if (refreshIndexTaskStatsRet.failed()) {
            return Result.buildFrom(refreshIndexTaskStatsRet);
        }
        Result<Void> cancelIndexTaskRet = cancelIndexTask(taskId, fastIndexDTO);
        if (cancelIndexTaskRet.failed()) {
            return Result.buildFrom(cancelIndexTaskRet);
        }
        return refreshTaskStatus(opTask);
    }

    @Override
    public Result<Void> restartTask(Integer taskId, Integer projectId) {
        if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            return Result.buildFail("越权操作，请更换项目或者更换账号");
        }
        OpTask opTask = opTaskService.getById(taskId);
        if (null == opTask) {
            return FAIL_RESULT_GET_OP_TASK_ERROR;
        }
        if (!OpTaskTypeEnum.FAST_INDEX.getType().equals(opTask.getTaskType())) {
            return Result.buildFail("任务类型异常，非数据迁移任务不支持重试任务！");
        }
        OpTaskStatusEnum taskStatusEnum = OpTaskStatusEnum.valueOfStatus(opTask.getStatus());
        if (OpTaskStatusEnum.FAILED != taskStatusEnum && OpTaskStatusEnum.CANCEL != taskStatusEnum) {
            return Result.buildFail("只有取消和失败的数据迁移任务可以重试，该任务状态异常，暂时不可重试！");
        }

        Result<Void> cancelIndexTaskRet = restartIndexTask(taskId);
        if (cancelIndexTaskRet.failed()) {
            return Result.buildFrom(cancelIndexTaskRet);
        }

        return refreshTaskStatus(opTask);
    }

    @Override
    public Result<Void> modifyTaskRateLimit(Integer taskId, FastIndexRateLimitDTO fastIndexRateLimitDTO) {
        OpTask opTask = opTaskService.getById(taskId);
        if (null == opTask) {
            return FAIL_RESULT_GET_OP_TASK_ERROR;
        }
        FastIndexDTO fastIndexDTO = JSON.parseObject(opTask.getExpandData(), FastIndexDTO.class);
        //挑选任务并提交
        if (null == fastIndexDTO) {
            return FAIL_RESULT_GET_FAST_INDEX_TASK_ERROR;
        }
        Long newRateLimit = fastIndexRateLimitDTO.getTaskReadRate();

        if (null != newRateLimit && newRateLimit > 0 && !Objects.equals(newRateLimit, fastIndexDTO.getTaskReadRate())) {
            return modifyIndexTaskRateLimit(taskId, fastIndexDTO, newRateLimit);
        }
        return Result.buildSucc();
    }

    @Override
    public Result<FastIndexDetailVO> getTaskDetail(Integer taskId) {
        OpTask opTask = opTaskService.getById(taskId);
        if (null == opTask) {
            return Result.buildFrom(FAIL_RESULT_GET_OP_TASK_ERROR);
        }

        FastIndexDTO fastIndexDTO = JSON.parseObject(opTask.getExpandData(), FastIndexDTO.class);
        OpTaskStatusEnum taskStatusEnum = OpTaskStatusEnum.valueOfStatus(opTask.getStatus());
        if (taskStatusEnum == OpTaskStatusEnum.RUNNING || taskStatusEnum == OpTaskStatusEnum.WAITING) {
            try {
                refreshTask(taskId);
            } catch (Exception e) {
                // 刷新任务状态失败
                // pass
                LOGGER.error("refreshTask failed! ", e);
            }
        }

        List<FastIndexTaskInfo> taskIndexList = fastIndexTaskService.listByTaskId(taskId);
        List<FastIndexStats> taskIndexStatsList = ConvertUtil.list2List(taskIndexList, FastIndexStats.class);
        FastIndexStats totalStats = null;

        if (DATA_TYPE_INDEX.equals(fastIndexDTO.getDataType())) {
            totalStats = calculationTaskStats(taskIndexStatsList);
        } else if (DATA_TYPE_TEMPLATE.equals(fastIndexDTO.getDataType())) {
            Map<Integer, List<FastIndexTaskInfo>> templateId2IndexTaskList = taskIndexList.stream()
                .collect(Collectors.groupingBy(FastIndexTaskInfo::getTemplateId));
            Map<Integer, IndexTemplate> templateMap = indexTemplateService
                .getLogicTemplatesMapByIds(Lists.newArrayList(templateId2IndexTaskList.keySet()));
            List<FastIndexStats> list = Lists.newArrayList();
            templateId2IndexTaskList.forEach((key, val) -> {
                List<FastIndexStats> taskTemplateIndexStatsList = ConvertUtil.list2List(val, FastIndexStats.class);
                FastIndexStats templateStats = calculationTaskStats(taskTemplateIndexStatsList);
                IndexTemplate indexTemplate = templateMap.getOrDefault(key, null);
                if (null != indexTemplate) {
                    templateStats.setTemplateName(indexTemplate.getName());
                }
                templateStats.setTemplateId(key);
                list.add(templateStats);
            });
            totalStats = calculationTaskStats(list);
        }
        opTask = opTaskService.getById(taskId);
        FastIndexDetailVO detailVO = ConvertUtil.obj2Obj(opTask, FastIndexDetailVO.class);
        if (null != totalStats) {
            totalStats.setTaskType(fastIndexDTO.getDataType());
            detailVO.setFastIndexStats(totalStats);
        }
        return Result.buildSucc(detailVO);
    }

    @Override
    public Result<Void> transferTemplate(Integer taskId) {
        OpTask opTask = opTaskService.getById(taskId);
        if (null == opTask) {
            return FAIL_RESULT_GET_OP_TASK_ERROR;
        }
        if (!OpTaskTypeEnum.FAST_INDEX.getType().equals(opTask.getTaskType())) {
            return Result.buildFail("任务类型异常，非数据迁移任务不支持转让模板所属！");
        }
        if (OpTaskStatusEnum.SUCCESS != OpTaskStatusEnum.valueOfStatus(opTask.getStatus())) {
            return Result.buildFail("任务状态未完成，暂时不可转让模板所属！");
        }
        FastIndexDTO fastIndexDTO = JSON.parseObject(opTask.getExpandData(), FastIndexDTO.class);
        if (!DATA_TYPE_TEMPLATE.equals(fastIndexDTO.getDataType())) {
            return Result.buildFail("非模版类型的数据迁移任务，暂时不可转让模板所属！");
        }

        if (TRANSFER_STATUS_SUCCESS.equals(fastIndexDTO.getTransferStatus())) {
            return Result.buildSucc();
        }
        if (!TRANSFER_STATUS_WAITING.equals(fastIndexDTO.getTransferStatus())) {
            return Result.buildFail("当前迁移任务不可转让模板所属！");
        }

        if (Boolean.TRUE.equals(fastIndexDTO.getTransfer())) {
            Integer targetProjectId = fastIndexDTO.getTargetProjectId();
            Long targetLogicClusterId = fastIndexDTO.getTargetLogicClusterId();
            String phyClusterName = fastIndexDTO.getTargetCluster();
            Result<Void> transferTemplateCheckRet = templateLogicManager.transferTemplateCheck(phyClusterName,
                targetLogicClusterId, targetProjectId);
            if (transferTemplateCheckRet.failed()) {
                return transferTemplateCheckRet;
            }
            List<Integer> templateIdList = fastIndexTaskService.listTemplateIdByTaskId(taskId);
            List<Result> resultList = Lists.newCopyOnWriteArrayList();
            templateIdList.forEach(templateId -> resultList.addAll(
                FAST_INDEX_MANAGER_FUTURE_UTIL.callableTask(() -> {
                    Result<Integer> transferResult = templateLogicManager.transferTemplate(templateId,
                            targetProjectId, targetLogicClusterId, phyClusterName);
                    if(transferResult.success()){
                        //发布创建pipeline的事件
                        SpringTool.publish(new LogicTemplateCreatePipelineEvent(this,templateId));
                    }
                    return transferResult;
                }).waitResultQueue()));
            resultList.addAll(FAST_INDEX_MANAGER_FUTURE_UTIL.waitResult());
            List<Object> succTemplateList = resultList.stream().filter(BaseResult::success).map(Result::getData)
                .collect(Collectors.toList());
            List<Object> failedTemplateList = resultList.stream().filter(BaseResult::failed).map(Result::getData)
                .collect(Collectors.toList());

            fastIndexDTO.setTransferResult(String.format("succTemplateIds[%s];failedTemplateIds[%s]",
                ConvertUtil.list2String(succTemplateList, ","), ConvertUtil.list2String(failedTemplateList, ",")));
            fastIndexDTO.setTransferStatus(TRANSFER_STATUS_SUCCESS);
        }
        //刷新主任务状态
        OpTaskProcessDTO processDTO = new OpTaskProcessDTO();
        processDTO.setTaskId(opTask.getId());
        processDTO.setStatus(opTask.getStatus());
        processDTO.setExpandData(JSON.toJSONString(fastIndexDTO));
        return processTask(processDTO);
    }

    @Override
    public Result<Void> rollbackTemplate(Integer taskId) {
        OpTask opTask = opTaskService.getById(taskId);
        if (null == opTask) {
            return FAIL_RESULT_GET_OP_TASK_ERROR;
        }
        if (!OpTaskTypeEnum.FAST_INDEX.getType().equals(opTask.getTaskType())) {
            return Result.buildFail("任务类型异常，暂时不可回切！");
        }
        if (OpTaskStatusEnum.SUCCESS != OpTaskStatusEnum.valueOfStatus(opTask.getStatus())) {
            return Result.buildFail("任务状态未完成，暂时不可回切！");
        }
        FastIndexDTO fastIndexDTO = JSON.parseObject(opTask.getExpandData(), FastIndexDTO.class);
        if (!DATA_TYPE_TEMPLATE.equals(fastIndexDTO.getDataType())) {
            return Result.buildFail("非模版类型的数据迁移任务，暂时不可回切！");
        }
        if (!TRANSFER_STATUS_SUCCESS.equals(fastIndexDTO.getTransferStatus())) {
            return Result.buildFail("模版转让状态异常，暂时不可回切！");
        }
        if (Boolean.TRUE.equals(fastIndexDTO.getTransfer())) {
            Integer targetProjectId = fastIndexDTO.getSourceProjectId();
            Long targetLogicClusterId = fastIndexDTO.getSourceLogicClusterId();
            String phyClusterName = fastIndexDTO.getSourceCluster();
            Result<Void> transferTemplateCheckRet = templateLogicManager.transferTemplateCheck(phyClusterName,
                targetLogicClusterId, targetProjectId);
            if (transferTemplateCheckRet.failed()) {
                return transferTemplateCheckRet;
            }
            List<Integer> templateIdList = fastIndexTaskService.listTemplateIdByTaskId(taskId);
            List<Result> resultList = Lists.newCopyOnWriteArrayList();
            templateIdList.forEach(templateId -> resultList.addAll(
                FAST_INDEX_MANAGER_FUTURE_UTIL.callableTask(() -> templateLogicManager.transferTemplate(templateId,
                    targetProjectId, targetLogicClusterId, phyClusterName)).waitResultQueue()));
            resultList.addAll(FAST_INDEX_MANAGER_FUTURE_UTIL.waitResult());
            List<Object> succTemplateList = resultList.stream().filter(BaseResult::success).map(Result::getData)
                .collect(Collectors.toList());
            List<Object> failedTemplateList = resultList.stream().filter(BaseResult::failed).map(Result::getData)
                .collect(Collectors.toList());

            fastIndexDTO.setTransferResult(String.format("succTemplateIds[%s];failedTemplateIds[%s]",
                ConvertUtil.list2String(succTemplateList, ","), ConvertUtil.list2String(failedTemplateList, ",")));

            fastIndexDTO.setTransferStatus(TRANSFER_STATUS_ROLLBACK);
        }
        //刷新主任务状态
        OpTaskProcessDTO processDTO = new OpTaskProcessDTO();
        processDTO.setTaskId(opTask.getId());
        processDTO.setStatus(opTask.getStatus());
        processDTO.setExpandData(JSON.toJSONString(fastIndexDTO));
        return processTask(processDTO);
    }

    @Override
    public String TaskSubmitAddress(String sourceClusterName) {
        //返回任意一个fastdump地址
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(sourceClusterName);
        List<String> ipList = Arrays.stream(StringUtils.split(clusterPhy.getHttpAddress(), ","))
                .map(httpAddress -> split2Tuple(httpAddress).getV1()+ ":" + fastDumpPort).collect(Collectors.toList());
        String taskSubmitAddress = ipList.stream().findAny().orElse(null);
        return taskSubmitAddress;
    }

    @Override
    public List<ClusterPhyVO> ESClustersInstalledFastDump() {
        List<ClusterPhy> supportESVersionList;
        List<ClusterPhy> clusterPhyList = clusterPhyService.listClustersByCondt(new ClusterPhyDTO());
        //获取所有物理集群地址
        List<String> ipList = clusterPhyList.stream()
                .map(clusterPhy -> Arrays.stream(StringUtils.split(clusterPhy.getHttpAddress(), ","))
                        .map(httpAddress -> split2Tuple(httpAddress).getV1()).collect(Collectors.toList()))
                .flatMap(Collection::stream).distinct().collect(Collectors.toList());
        //校验这些地址是否安装了fastdump
        List<String> installedFastDumpIpList = ipList.stream().filter(ip -> {
            FastIndexDTO checkDTO = new FastIndexDTO();
            checkDTO.setTaskSubmitAddress(ip + ":" + fastDumpPort);
            Result<Boolean> checkHealthResult = esIndexMoveTaskService.checkHealth(checkDTO);
            return checkHealthResult.success();
        }).collect(Collectors.toList());
        //匹配安装了fastdump的物理集群
        List<ClusterPhy> phyClustersInstalledFastDump = clusterPhyList.stream()
                .filter(clusterPhy -> Arrays.stream(StringUtils.split(clusterPhy.getHttpAddress(), ","))
                        .map(httpAddress -> split2Tuple(httpAddress).getV1())
                        .allMatch(ip -> installedFastDumpIpList.contains(ip))).collect(Collectors.toList());
        //去获取fastdump支持的es版本
        Optional<String> ipOptional = installedFastDumpIpList.stream().findFirst();
        if (ipOptional.isPresent()) {
            FastIndexDTO supportDTO = new FastIndexDTO();
            supportDTO.setTaskSubmitAddress(ipOptional.get() + ":" + fastDumpPort);
            Result<List<String>> res = esIndexMoveTaskService.getSupportESVersion(supportDTO);
            if (res.success()) {
                supportESVersionList = phyClustersInstalledFastDump.stream()
                        .filter(clusterPhy -> res.getData().contains(getESBigVersion(clusterPhy.getEsVersion())))
                        .collect(Collectors.toList());
                return ConvertUtil.list2List(supportESVersionList, ClusterPhyVO.class);
            }
        }
        return Lists.newArrayList();
    }

    @Override
    public List<ClusterPhyVO> supportESClusterVersions() {
        ClusterPhyDTO clusterPhyDTO = new ClusterPhyDTO();
        List<ClusterPhy> supportESVersionList;
        List<ClusterPhy> clusterPhyList = clusterPhyService.listClustersByCondt(clusterPhyDTO);
        //获取成功的数据迁移任务，去内核获取es版本
        Optional<OpTask> opTaskOptional = opTaskService.getSuccessTaskByType(OpTaskTypeEnum.FAST_INDEX.getType()).stream()
                .sorted(Comparator.comparing(OpTask::getId).reversed()).limit(1).findFirst();
        if (opTaskOptional.isPresent()) {
            FastIndexDTO fastIndexDTO = JSON.parseObject(opTaskOptional.get().getExpandData(), FastIndexDTO.class);
            Result<List<String>> res = esIndexMoveTaskService.getSupportESVersion(fastIndexDTO);
            if (res.success()) {
                supportESVersionList = clusterPhyList.stream()
                        .filter(clusterPhy -> res.getData().contains(getESBigVersion(clusterPhy.getEsVersion())))
                        .collect(Collectors.toList());
                return ConvertUtil.list2List(supportESVersionList, ClusterPhyVO.class);
            }
        }
        supportESVersionList = clusterPhyList.stream()
                .filter(clusterPhy -> FastDumpSupportESVersionEnum.isExist(getESBigVersion(clusterPhy.getEsVersion())))
                .collect(Collectors.toList());
        return ConvertUtil.list2List(supportESVersionList, ClusterPhyVO.class);
    }

    @Override
    public Result<IndexTemplatePhySetting> getTemplateSettings(Integer logicId, Long logicClusterId) {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService.getLogicTemplateWithPhysicalsById(logicId);
        if (templateLogicWithPhysical == null) {
            return Result.buildNotExist("逻辑模板不存在, ID:" + logicId);
        }
        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildNotExist("物理模板不存在，ID:" + logicId);
        }
        ClusterRegion region = clusterRegionService.getRegionByLogicClusterId(logicClusterId);
        if (region == null) {
            return Result.buildFail("目标逻辑集群异常：region不存在！");
        }
        Result<List<ClusterRoleHost>> roleHostResult = clusterRoleHostService.listByRegionId(Math.toIntExact(region.getId()));
        if (roleHostResult.failed()) {
            return Result.buildFail(String.format("获取region[%d]节点列表异常", region.getId()));
        }
        IndexTemplatePhy indexTemplatePhy = templateLogicWithPhysical.getMasterPhyTemplate();
        if (indexTemplatePhy != null) {
            try {
                IndexTemplatePhySetting indexTemplatePhySetting = templatePhySettingManager.fetchTemplateSettings(indexTemplatePhy.getCluster(),
                        indexTemplatePhy.getName());
                Map<String, String> indexTemplateMap = indexTemplatePhySetting.flatSettings();
                String nodesSets = roleHostResult.getData().stream().map(ClusterRoleHost::getNodeSet).collect(Collectors.joining(","));
                indexTemplateMap.put(INDEX_ROUTING_ALLOCATION_INCLUDE_NAME,nodesSets);
                return Result.buildSucc(new IndexTemplatePhySetting(indexTemplateMap));
            } catch (ESOperateException e) {
                return Result.buildFail(e.getMessage());
            }
        }

        return Result.buildFail("不存在Master角色物理模板，ID：" + logicId);
    }

    @Override
    public Result<IndexSettingVO> getSetting(String cluster, String indexName, Integer projectId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(cluster, projectId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        Result<Void> ret = basicCheckParam(phyCluster, indexName, projectId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }

        IndexSettingVO indexSettingVO = new IndexSettingVO();
        MultiIndexsConfig multiIndexsConfig = esIndexService.syncGetIndexConfigs(phyCluster, indexName);
        if (null == multiIndexsConfig) {
            LOGGER.warn(
                    "class=IndicesManagerImpl||method=getSetting||cluster={}||index={}||errMsg=get empty Index configs ",
                    phyCluster, indexName);
            return Result.buildSucc(indexSettingVO);
        }

        IndexConfig indexConfig = multiIndexsConfig.getIndexConfig(indexName);
        if (null == indexConfig) {
            LOGGER.warn(
                    "class=IndicesManagerImpl||method=getSetting||cluster={}||index={}||errMsg=get empty Index configs ",
                    phyCluster, indexName);
            return Result.buildSucc(indexSettingVO);
        }
        //删除不需要的setting配置
        Map<String, String> setting = indexConfig.getSettings();
        setting.remove(INDEX_CREATION_DATE);
        setting.remove(INDEX_UUID);
        setting.remove(INDEX_VERSION_CREATED);
        setting.remove(INDEX_PROVIDED_NAME);
        setting.remove(INDEX_ROUTING_ALLOCATION_INCLUDE_NAME);
        setting.remove(INDEX_ROUTING_ALLOCATION_INCLUDE_TIER_PREFERENCE);
        indexSettingVO.setProperties(JsonUtils.reFlat(setting));
        indexSettingVO.setIndexName(indexName);
        return Result.buildSucc(indexSettingVO);
    }

    @Override
    public Result<List<FastIndexBriefVO>> getFastIndexBrief(Integer taskId) {
        OpTask opTask = opTaskService.getById(taskId);
        if (null == opTask) {
            return Result.buildFrom(FAIL_RESULT_GET_OP_TASK_ERROR);
        }
        FastIndexDTO fastIndexDTO = JSON.parseObject(opTask.getExpandData(), FastIndexDTO.class);
        //查询出索引和模板
        List<FastIndexTaskInfo> taskIndexList = fastIndexTaskService.listByTaskId(taskId);
        //拼接结果
        List<FastIndexBriefVO> fastIndexBriefVOList = Lists.newArrayList();
        if (DATA_TYPE_INDEX.equals(fastIndexDTO.getDataType())) {
            List<String> indexList = taskIndexList.stream().map(FastIndexTaskInfo::getIndexName).collect(Collectors.toList());
            fastIndexBriefVOList.add(new FastIndexBriefVO(indexList));
        } else if (DATA_TYPE_TEMPLATE.equals(fastIndexDTO.getDataType())) {
            Map<String, List<String>> templateMap = ConvertUtil.list2MapOfList(taskIndexList, FastIndexTaskInfo::getTemplateName, FastIndexTaskInfo::getIndexName);
            templateMap.forEach((template, indexList) -> {
                FastIndexBriefVO fastIndexBriefVO = new FastIndexBriefVO(template, indexList);
                fastIndexBriefVOList.add(fastIndexBriefVO);
            });
        }
        return Result.buildSucc(fastIndexBriefVOList);
    }

    @Override
    public PaginationResult<FastDumpTaskLogVO> pageGetTaskLogs(Integer projectId,
                                                               FastIndexLogsConditionDTO queryDTO) throws NotFindSubclassException {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(FAST_INDEX_TASK_LOG.getPageSearchType());
        if (baseHandle instanceof FastIndexTaskLogPageSearchHandle) {
            FastIndexTaskLogPageSearchHandle handle = (FastIndexTaskLogPageSearchHandle) baseHandle;
            return handle.doPage(queryDTO, projectId);
        }
        LOGGER.warn(
            "class=FastIndexTaskManager||method=pageGetTasks||msg=failed to get the FastIndexTaskLogPageSearchHandle");

        return PaginationResult.buildFail("分页获取任务中心信息失败");
    }

    private Result<OpTask> addOpTask(FastIndexDTO fastIndexDTO, String operator, String businessKey) {
        OpTask opTask = new OpTask();
        opTask.setBusinessKey(businessKey);
        opTask.setTitle(businessKey + OpTaskTypeEnum.FAST_INDEX.getMessage());
        opTask.setTaskType(OpTaskTypeEnum.FAST_INDEX.getType());
        opTask.setCreator(operator);
        opTask.setDeleteFlag(false);
        opTask.setStatus(OpTaskStatusEnum.WAITING.getStatus());
        //2.4 保存任务
        opTask.setExpandData(ConvertUtil.obj2Json(fastIndexDTO));
        opTask.setCreateTime(new Date());
        opTask.setUpdateTime(new Date());

        opTaskService.insert(opTask);
        boolean succ = 0 < opTask.getId();
        if (!succ) {
            LOGGER.error(
                "class=FastIndexManagerImpl||method=addOpTask||taskType={}||businessKey={}||errMsg=failed to insert",
                opTask.getTaskType(), opTask.getBusinessKey());
            return Result.buildFail();
        }
        return Result.buildSucc(opTask);
    }

    private Result<Void> processTask(OpTaskProcessDTO processDTO) {
        OpTask opTask = new OpTask();
        opTask.setId(processDTO.getTaskId());
        opTask.setStatus(processDTO.getStatus());
        opTask.setExpandData(processDTO.getExpandData());
        opTaskService.update(opTask);
        return Result.buildSucc();
    }

    private FastIndexStats calculationTaskStats(List<FastIndexStats> taskIndexList) {
        FastIndexStats fastIndexStats = new FastIndexStats();
        AtomicReference<BigDecimal> totalDocumentNum = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> succDocumentNum = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> failedDocumentNum = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> shardNum = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> succShardNum = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> costTime = new AtomicReference<>(BigDecimal.ZERO);

        AtomicReference<Boolean> hasNotSubmited = new AtomicReference<>(Boolean.FALSE);
        AtomicReference<Boolean> isRunning = new AtomicReference<>(Boolean.FALSE);
        AtomicReference<Boolean> isCancel = new AtomicReference<>(Boolean.FALSE);
        AtomicReference<Boolean> hasFail = new AtomicReference<>(Boolean.FALSE);
        AtomicReference<Boolean> hasSucc = new AtomicReference<>(Boolean.FALSE);

        List<FastIndexStats> childrenList = Lists.newArrayList();
        taskIndexList.forEach(indexTask -> {
            FastIndexTaskStatusEnum taskStatusEnum = FastIndexTaskStatusEnum.enumOfValue(indexTask.getTaskStatus());
            updateStatusValue(isCancel, Sets.newHashSet(FastIndexTaskStatusEnum.CANCEL), taskStatusEnum);
            updateStatusValue(hasNotSubmited, Sets.newHashSet(FastIndexTaskStatusEnum.NOT_SUBMITTED), taskStatusEnum);
            updateStatusValue(isRunning,
                Sets.newHashSet(FastIndexTaskStatusEnum.RUNNING, FastIndexTaskStatusEnum.WAITING), taskStatusEnum);
            updateStatusValue(hasFail, Sets.newHashSet(FastIndexTaskStatusEnum.FAILED), taskStatusEnum);
            updateStatusValue(hasSucc, Sets.newHashSet(FastIndexTaskStatusEnum.SUCCESS), taskStatusEnum);

            addBigDecimal(totalDocumentNum, indexTask.getTotalDocumentNum());
            addBigDecimal(succDocumentNum, indexTask.getSuccDocumentNum());
            addBigDecimal(failedDocumentNum, indexTask.getFailedDocumentNum());
            addBigDecimal(shardNum, indexTask.getShardNum());
            addBigDecimal(succShardNum, indexTask.getSuccShardNum());
            addBigDecimal(costTime, indexTask.getTaskCostTime());
            if (indexTask.getSuccDocumentNum() != null && indexTask.getTaskCostTime() != null
                && indexTask.getTaskCostTime().compareTo(BigDecimal.ZERO) > 0) {
                //总文档数除以时间
                indexTask.setIndexMoveRate(indexTask.getSuccDocumentNum().multiply((BigDecimal.valueOf(1000L)))
                    .divide(indexTask.getTaskCostTime(), RoundingMode.DOWN));
            }
            childrenList.add(ConvertUtil.obj2Obj(indexTask, FastIndexStats.class));
        });
        FastIndexTaskStatusEnum taskStatusEnum = getFastIndexTaskStatusEnum(hasNotSubmited.get(), isRunning.get(),
            isCancel.get(), hasFail.get(), hasSucc.get());
        fastIndexStats.setTaskStatus(taskStatusEnum.getValue());

        fastIndexStats.setTotalDocumentNum(totalDocumentNum.get());
        fastIndexStats.setSuccDocumentNum(succDocumentNum.get());
        fastIndexStats.setFailedDocumentNum(failedDocumentNum.get());
        fastIndexStats.setIndexMoveRate(BigDecimal.ZERO);
        if (succDocumentNum.get() != null && costTime.get() != null && costTime.get().compareTo(BigDecimal.ZERO) > 0) {
            //总文档数除以时间
            fastIndexStats.setIndexMoveRate(
                succDocumentNum.get().multiply(BigDecimal.valueOf(1000L)).divide(costTime.get(), RoundingMode.DOWN));
        }
        fastIndexStats.setTaskCostTime(costTime.get());
        fastIndexStats.setShardNum(shardNum.get());
        fastIndexStats.setSuccShardNum(succShardNum.get());
        fastIndexStats.setChildrenList(childrenList);
        return fastIndexStats;
    }

    private void updateStatusValue(AtomicReference<Boolean> booleanAtomicReference,
                                   Set<FastIndexTaskStatusEnum> taskStatusEnumSet,
                                   FastIndexTaskStatusEnum taskStatusEnum) {
        if (Boolean.FALSE.equals(booleanAtomicReference.get()) && taskStatusEnumSet.contains(taskStatusEnum)) {
            booleanAtomicReference.set(Boolean.TRUE);
        }
    }

    private FastIndexTaskStatusEnum getFastIndexTaskStatusEnum(Boolean hasNotSubmited, Boolean isRunning,
                                                               Boolean isCancel, Boolean hasFail, Boolean hasSucc) {
        FastIndexTaskStatusEnum taskStatusEnum = FastIndexTaskStatusEnum.NOT_SUBMITTED;
        if (Boolean.TRUE.equals(isCancel)) {
            taskStatusEnum = FastIndexTaskStatusEnum.CANCEL;
        } else if (Boolean.TRUE.equals(hasFail) && Boolean.FALSE.equals(isRunning)
                   && Boolean.FALSE.equals(hasNotSubmited)) {
            taskStatusEnum = FastIndexTaskStatusEnum.FAILED;
        } else if (Boolean.TRUE.equals(isRunning)) {
            taskStatusEnum = FastIndexTaskStatusEnum.RUNNING;
        } else if (Boolean.FALSE.equals(hasNotSubmited) && Boolean.TRUE.equals(hasSucc)) {
            taskStatusEnum = FastIndexTaskStatusEnum.SUCCESS;
        }
        return taskStatusEnum;
    }

    private void addBigDecimal(AtomicReference<BigDecimal> source, BigDecimal num) {
        if (null != num) {
            source.updateAndGet(v -> v.add(num));
        }
    }

    private boolean saveIndexTask(FastIndexDTO fastIndexDTO, List<FastIndexTaskInfo> taskIndexList, Integer taskId) {
        BigDecimal readFileRateLimit = null;
        if (null != fastIndexDTO.getTaskReadRate() && fastIndexDTO.getTaskReadRate() > 0) {
            readFileRateLimit = BigDecimal.valueOf(fastIndexDTO.getTaskReadRate())
                .divide(BigDecimal.valueOf(taskIndexList.size()), RoundingMode.UP);
        }
        BigDecimal finalReadFileRateLimit = readFileRateLimit;
        taskIndexList.forEach(taskInfo -> {
            taskInfo.setTaskId(taskId);
            taskInfo.setTaskStatus(FastIndexTaskStatusEnum.NOT_SUBMITTED.getValue());//未提交
            taskInfo.setCreateTime(new Date());
            taskInfo.setUpdateTime(new Date());
            if (null == taskInfo.getTotalDocumentNum()
                || BigDecimal.ZERO.compareTo(taskInfo.getTotalDocumentNum()) >= 0) {
                taskInfo.setTotalDocumentNum(BigDecimal.ZERO);
            }
            if (null != finalReadFileRateLimit) {
                taskInfo.setReadFileRateLimit(finalReadFileRateLimit);
            }
            if (null != fastIndexDTO.getTaskStartTime()) {
                taskInfo.setScheduledTaskStartTime(fastIndexDTO.getTaskStartTime());
            }
        });
        return fastIndexTaskService.saveTasks(taskIndexList);
    }

    private Result<Void> createTargetIndex(ClusterPhy targetCluster, List<FastIndexTaskInfo> taskIndexList) {
        //在目标集群中创建索引
        List<Result> resultList = Lists.newCopyOnWriteArrayList();
        taskIndexList.forEach(taskIndex -> resultList.addAll(FAST_INDEX_MANAGER_FUTURE_UTIL.callableTask(() -> {

            IndexConfig config = new IndexConfig();
            if (StringUtils.isNotBlank(taskIndex.getMappings())) {
                Result<MappingConfig> result = AriusIndexMappingConfigUtils.parseMappingConfig(taskIndex.getMappings());
                config.setMappings(result.getData());
            }
            if (StringUtils.isNotBlank(taskIndex.getSettings())) {
                config.setSettings(AriusIndexTemplateSetting.flat(JSON.parseObject(taskIndex.getSettings())));
            }
            try {
                esIndexService.syncCreateIndex(targetCluster.getCluster(), taskIndex.getTargetIndexName(), config, 3);
            } catch (Exception e) {
                //pass
            }
            if (esIndexService.syncIsIndexExist(targetCluster.getCluster(), taskIndex.getTargetIndexName())) {
                return Result.buildSucc();
            }

            return Result.buildFail("目标集群中创建索引【" + taskIndex.getTargetIndexName() + "】失败");
        }).waitResultQueue()));
        resultList.addAll(FAST_INDEX_MANAGER_FUTURE_UTIL.waitResult());
        List<Result> failedList = resultList.stream().filter(BaseResult::failed).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(failedList)) {
            return Result.buildFail(failedList.stream().map(BaseResult::getMessage).collect(Collectors.joining("\n")));
        }
        return Result.buildSucc();
    }

    private Result<List<FastIndexTaskInfo>> getTaskIndexListByIndex(FastIndexDTO fastIndexDTO,
                                                                    List<FastIndexTaskDTO> taskList) {
        //防重校验
        String key = buildIndexMoveTaskKey(taskList, fastIndexDTO);
        List<OpTask> opTaskList = opTaskService.getPendingTaskByType(OpTaskTypeEnum.FAST_INDEX.getType());
        Boolean duplicateCheck = opTaskList.stream().anyMatch(opTask -> {
            FastIndexDTO pendingFastIndexDTO = JSON.parseObject(opTask.getExpandData(), FastIndexDTO.class);
            if (Objects.nonNull(pendingFastIndexDTO)) {
                String pendingOpTaskKey = buildIndexMoveTaskKey(pendingFastIndexDTO.getTaskList(), pendingFastIndexDTO);
                return key.equals(pendingOpTaskKey);
            }
            return false;
        });
        if (duplicateCheck) {
            return Result.buildFail("已提交该任务，请勿重复提交");
        }
        List<Result<FastIndexTaskInfo>> resultList = Lists.newCopyOnWriteArrayList();
        //如果是索引则直接检查原索引是否存在
        taskList.forEach(task -> task.getSourceIndexList()
            .forEach(indexDTO -> resultList.addAll(FAST_INDEX_TASK_INDEX_FUTURE_UTIL.callableTask(() -> {
                List<CatIndexResult> catIndexResultList = esIndexService
                    .syncCatIndexByExpression(fastIndexDTO.getSourceCluster(), indexDTO.getResourceNames());
                if (null == catIndexResultList) {
                    return Result.buildFail(String.format(GET_INDEX_FAILED_MSG, indexDTO.getResourceNames()));
                }
                CatIndexResult catIndexResult = catIndexResultList.stream()
                    .filter(catIndex -> StringUtils.equals(indexDTO.getResourceNames(), catIndex.getIndex()))
                    .findFirst().orElse(null);
                if (null == catIndexResult) {
                    return Result.buildFail(String.format(GET_INDEX_FAILED_MSG, indexDTO.getResourceNames()));
                }
                FastIndexTaskInfo taskInfo = new FastIndexTaskInfo();
                taskInfo.setTaskType(FastIndexConstant.DATA_TYPE_INDEX);//index
                taskInfo.setIndexName(indexDTO.getResourceNames());
                taskInfo.setIndexTypes(StringUtils.join(indexDTO.getIndexTypes(), ","));
                taskInfo.setTargetIndexName(task.getTargetName());
                taskInfo.setMappings(task.getMappings());
                taskInfo.setSettings(task.getSettings());
                taskInfo.setTotalDocumentNum(new BigDecimal(Optional.ofNullable(catIndexResult.getDocsCount()).orElse("0")));
                return Result.buildSucc(taskInfo);
            }).waitResultQueue())));
        resultList.addAll(FAST_INDEX_TASK_INDEX_FUTURE_UTIL.waitResult());
        List<Result<FastIndexTaskInfo>> failedList = resultList.stream().filter(BaseResult::failed)
            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(failedList)) {
            return Result.buildFail(failedList.stream().map(BaseResult::getMessage).collect(Collectors.joining("\n")));
        }
        //校验任务读取速率
        Result<List<FastIndexTaskInfo>> checkReadFileRateLimitResult = checkReadFileRateLimit(fastIndexDTO, resultList.size());
        if (checkReadFileRateLimitResult.failed()) {
            return checkReadFileRateLimitResult;
        }
        return Result.buildSucc(resultList.stream().map(Result::getData).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    /**
     * 校验任务读取速率
     * @param fastIndexDTO
     * @param size
     * @return
     */
    private Result<List<FastIndexTaskInfo>> checkReadFileRateLimit(FastIndexDTO fastIndexDTO, int size) {
        Long taskReadRate = fastIndexDTO.getTaskReadRate();
        if (null != taskReadRate && taskReadRate > 0) {
            //当没有索引时
            if (size == 0 && taskReadRate < 1000L) {
                return Result.buildFail(String.format(GET_READ_FILE_RATE_LIMIT_MSG, 1000));
            }
            if(size == 0){
                return Result.buildSucc();
            }
            BigDecimal readFileRateLimit = BigDecimal.valueOf(taskReadRate).divide(BigDecimal.valueOf(size), RoundingMode.UP);
            if (readFileRateLimit.compareTo(new BigDecimal(1000)) < 0) {
                return Result.buildFail(String.format(GET_READ_FILE_RATE_LIMIT_MSG, 1000 * size));
            }
        }
        return Result.buildSucc();
    }

    private Result<List<FastIndexTaskInfo>> createTemplateAndGetTaskIndexListByTemplate(FastIndexDTO fastIndexDTO,
                                                                                        ClusterRegion targetClusterRegion,
                                                                                        List<FastIndexTaskDTO> taskList,ClusterPhy sourceCluster,
                                                                                        ClusterPhy targetCluster) {
        //防重校验
        String key = buildTemplateMoveTaskKey(taskList, fastIndexDTO);
        List<OpTask> opTaskList = opTaskService.getPendingTaskByType(OpTaskTypeEnum.FAST_INDEX.getType());
        Boolean duplicateCheck = opTaskList.stream().anyMatch(opTask -> {
            FastIndexDTO pendingFastIndexDTO = JSON.parseObject(opTask.getExpandData(), FastIndexDTO.class);
            if (Objects.nonNull(pendingFastIndexDTO)) {
                String pendingOpTaskKey = buildTemplateMoveTaskKey(pendingFastIndexDTO.getTaskList(), pendingFastIndexDTO);
                return key.equals(pendingOpTaskKey);
            }
            return false;
        });
        if (duplicateCheck) {
            return Result.buildFail("已提交该任务，请勿重复提交");
        }
        //如果是模版，则判断模版状态是否正常，并根据在目标集群中创建模板，后获取索引列表
        List<Integer> templateIds = taskList.stream().map(FastIndexTaskDTO::getSourceTemplateId)
            .collect(Collectors.toList());
        Map<Integer, IndexTemplate> templateId2Logic = indexTemplateService.getLogicTemplatesMapByIds(templateIds);
        Set<Integer> templateIdSet = templateId2Logic.values().stream().filter(Objects::nonNull)
            .map(IndexTemplate::getId).collect(Collectors.toSet());
        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicIds(templateIds);
        Map<Integer, IndexTemplatePhy> templateLogicId2MasterTemplatePhy = templatePhyList.stream()
            .filter(templatePhy -> TemplateDeployRoleEnum.MASTER.getCode().equals(templatePhy.getRole()))
            .collect(Collectors.toMap(IndexTemplatePhy::getLogicId, a -> a));
        Set<Integer> templateIdSet2TemplatePhy = templateLogicId2MasterTemplatePhy.keySet();
        List<String> failedTemplateNames = taskList.stream()
            .filter(task -> !templateIdSet.contains(task.getSourceTemplateId())
                            || !templateIdSet2TemplatePhy.contains(task.getSourceTemplateId()))
            .map(FastIndexTaskDTO::getTargetName).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(failedTemplateNames)) {
            return Result.buildFail("所选模板【" + StringUtils.join(failedTemplateNames, ",") + "】异常");
        }
        AtomicInteger indexCount = new AtomicInteger(0);
        //根据模版在原集群中获取索引列表
        List<Result<List<FastIndexTaskInfo>>> resultList = Lists.newCopyOnWriteArrayList();
        taskList.forEach(task -> resultList.addAll(FAST_INDEX_TASK_TEMPLATE_FUTURE_UTIL.callableTask(() -> {
            IndexTemplate logic = templateId2Logic.get(task.getSourceTemplateId());
            List<CatIndexResult> results = esIndexService.syncCatIndexByExpression(fastIndexDTO.getSourceCluster(),
                    logic.getExpression());
            if (null == results) {
                return Result.buildFail("根据模版【" + logic.getName() + "】获取原集群中的索引失败！");
            }
            String mappings = "{}";
            String indexTypes = "";
            if (StringUtils.isNotBlank(task.getMappings())) {
                Result<MappingConfig> result = AriusIndexMappingConfigUtils.parseMappingConfig(task.getMappings());
                if (result.failed()) {
                    return Result.buildFrom(result);
                }
                TemplateConfig templateConfig = new TemplateConfig();
                templateConfig.setMappings(result.getData());
                mappings = templateConfig.toJson().getJSONObject("mappings").toJSONString();
                indexTypes = result.getData().getMapping().keySet().stream().collect(Collectors.joining(","));
                if (FastDumpSupportESVersionEnum.ES_6_6_1.getVersion().equals(getESBigVersion(targetCluster.getEsVersion())) ||
                        FastDumpSupportESVersionEnum.ES_2_3_3.getVersion().equals(getESBigVersion(targetCluster.getEsVersion()))) {
                    fastIndexDTO.setTargetIndexType(indexTypes);
                }
            }
            String finalMappings = mappings;
            String finalIndexTypes = indexTypes;
            List<FastIndexTaskInfo> indexTaskInfoList = results.stream().filter(Objects::nonNull).map(catIndex -> {
                FastIndexTaskInfo taskInfo = new FastIndexTaskInfo();
                taskInfo.setTaskType(DATA_TYPE_TEMPLATE);//template
                taskInfo.setTemplateId(logic.getId());
                taskInfo.setTemplateName(logic.getName());
                taskInfo.setIndexName(catIndex.getIndex());
                taskInfo.setTargetIndexName(catIndex.getIndex());
                taskInfo.setTaskStatus(FastIndexTaskStatusEnum.NOT_SUBMITTED.getValue());//未提交
                taskInfo.setTotalDocumentNum(new BigDecimal(Optional.ofNullable(catIndex.getDocsCount()).orElse("0")));
                taskInfo.setMappings(finalMappings);
                taskInfo.setSettings(task.getSettings());
                if (FastDumpSupportESVersionEnum.ES_6_6_1.getVersion().equals(getESBigVersion(sourceCluster.getEsVersion())) ||
                        FastDumpSupportESVersionEnum.ES_2_3_3.getVersion().equals(getESBigVersion(sourceCluster.getEsVersion()))) {
                    taskInfo.setIndexTypes(finalIndexTypes);
                }
                return taskInfo;
            }).collect(Collectors.toList());
            indexCount.accumulateAndGet(indexTaskInfoList.size(), Integer::sum);
            return Result.buildSucc(indexTaskInfoList);
        }).waitResultQueue()));
        resultList.addAll(FAST_INDEX_TASK_TEMPLATE_FUTURE_UTIL.waitResult());
        Result<List<FastIndexTaskInfo>> failedListResult = getListResult(resultList);
        if (failedListResult.failed()) {
            return failedListResult;
        }
        //校验任务读取速率
        Result<List<FastIndexTaskInfo>> checkReadFileRateLimitResult = checkReadFileRateLimit(fastIndexDTO, indexCount.get());
        if (checkReadFileRateLimitResult.failed()) {
            return checkReadFileRateLimitResult;
        }
        //创建模板
        List<Result<List<FastIndexTaskInfo>>> createTemplateResultList = Lists.newCopyOnWriteArrayList();
        taskList.forEach(task -> createTemplateResultList.addAll(FAST_INDEX_TASK_TEMPLATE_FUTURE_UTIL.callableTask(() -> {
            try {
                IndexTemplate logic = templateId2Logic.get(task.getSourceTemplateId());
                //创建模版
                Result<Void> createRet = createTargetTemplate(fastIndexDTO, targetClusterRegion, task, logic);
                if (createRet.failed()) {
                    return Result.buildFrom(createRet);
                }
                return Result.buildSucc();
            } catch (AdminOperateException e) {
                return Result.buildFail("创建模版【" + task.getSourceTemplateId() + "】失败");
            }
        }).waitResultQueue()));
        createTemplateResultList.addAll(FAST_INDEX_TASK_TEMPLATE_FUTURE_UTIL.waitResultQueue());
        Result<List<FastIndexTaskInfo>> createTemplateFailedResult = getListResult(createTemplateResultList);
        if (createTemplateFailedResult.failed()) {
            return createTemplateFailedResult;
        }
        return Result.buildSucc(
                resultList.stream().map(Result::getData).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    /**
     * 构建模板迁移任务去重校验key
     * @param taskList
     * @param fastIndexDTO
     * @return
     */
    private String buildTemplateMoveTaskKey(List<FastIndexTaskDTO> taskList, FastIndexDTO fastIndexDTO) {
        String key = taskList.stream().filter(task -> Objects.nonNull(task.getSourceTemplateId())).map(fastIndexTaskDTO -> fastIndexDTO.getSourceCluster()
                        + "@" + fastIndexTaskDTO.getSourceTemplateId() + "@" + fastIndexDTO.getTargetCluster() + "@" + fastIndexTaskDTO.getTargetName())
                .collect(Collectors.joining(","));
        return key;
    }

    /**
     * 构建索引迁移任务去重校验key
     * @param taskList
     * @param fastIndexDTO
     * @return
     */
    private String buildIndexMoveTaskKey(List<FastIndexTaskDTO> taskList, FastIndexDTO fastIndexDTO) {
        String key = taskList.stream().filter(task -> CollectionUtils.isNotEmpty(task.getSourceIndexList())).map(fastIndexTaskDTO -> fastIndexDTO.getSourceCluster()
                        + "@" + fastIndexTaskDTO.getSourceIndexList().stream().map(FastIndexTaskIndexDTO::getResourceNames)
                        .collect(Collectors.joining(";")) + "@" + fastIndexDTO.getTargetCluster()
                        + "@" + fastIndexTaskDTO.getTargetName())
                .collect(Collectors.joining(","));
        return key;
    }

    /**
     * 判断异步线程执行结果是否失败
     * @param ResultList
     * @return
     */
    private Result<List<FastIndexTaskInfo>> getListResult(List<Result<List<FastIndexTaskInfo>>> ResultList) {
        List<Result<List<FastIndexTaskInfo>>> FailedList = ResultList.stream().filter(BaseResult::failed)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(FailedList)) {
            return Result.buildFail(FailedList.stream().map(BaseResult::getMessage).collect(Collectors.joining("\n")));
        }
        return Result.buildSucc();
    }

    private Result<Void> createTargetTemplate(FastIndexDTO fastIndexDTO, ClusterRegion targetClusterRegion,
                                              FastIndexTaskDTO task, IndexTemplate logic) throws AdminOperateException {
        MappingConfig mappings = null;
        if (StringUtils.isNotBlank(task.getMappings())) {
            Result<MappingConfig> result = AriusIndexMappingConfigUtils.parseMappingConfig(task.getMappings());
            if (result.failed()) {
                return Result.buildFrom(result);
            }
            mappings = result.getData();
        }
        Map<String, String> settingsMap = getSettingsMap(Math.toIntExact(targetClusterRegion.getId()),
            task.getSettings());
        //在目标集群中创建模板
        boolean ret = esTemplateService.syncCreate(settingsMap, fastIndexDTO.getTargetCluster(), task.getTargetName(),
            logic.getExpression(), mappings, 3);
        if (!ret) {
            return Result.buildFail("在目标集群中创建模版【" + logic.getName() + "】失败");
        }
        return Result.buildSucc();
    }

    private Result<Void> checkTask(FastIndexDTO fastIndexDTO) {
        ClusterPhy sourceCluster = clusterPhyService.getClusterByName(fastIndexDTO.getSourceCluster());
        if (null == sourceCluster) {
            return Result.buildFail("获取源物理集群信息失败");
        }
        ClusterPhy targetCluster = clusterPhyService.getClusterByName(fastIndexDTO.getTargetCluster());
        if (null == targetCluster) {
            return Result.buildFail("获取目标物理集群信息失败");
        }
        //检查源集群和目标集群和任务提交地址连接状态
        Result<Void> checkConnectionStatusRet = checkConnectionStatus(fastIndexDTO);
        if (checkConnectionStatusRet.failed()) {
            return checkConnectionStatusRet;
        }
        //校验物理集群节点是否在线
        Result<Void> checkNodeConnectionStatusRet = checkNodeConnectionStatus(fastIndexDTO);
        if (checkNodeConnectionStatusRet.failed()) {
            return checkNodeConnectionStatusRet;
        }
        //检查原索引是否存在，拉取原集群的索引信息，并目标集群中创建索引数据
        List<FastIndexTaskDTO> taskList = fastIndexDTO.getTaskList();
        if (CollectionUtils.isEmpty(taskList)) {
            return Result.buildFail("待迁移索引或模板不能为空");
        }

        fastIndexDTO.setTransferStatus(TRANSFER_STATUS_NO_NEED);
        if (!DATA_TYPE_TEMPLATE.equals(fastIndexDTO.getDataType())
            || Boolean.FALSE.equals(fastIndexDTO.getTransfer())) {
            return Result.buildSucc();
        }
        Result<Void> templateCheckRet = checkTemplate(fastIndexDTO);
        if (templateCheckRet.failed()) {
            return templateCheckRet;
        }
        Result<Void> transferTemplateCheckRet = templateLogicManager.transferTemplateCheck(
            fastIndexDTO.getTargetCluster(), fastIndexDTO.getTargetLogicClusterId(), fastIndexDTO.getTargetProjectId());
        if (transferTemplateCheckRet.failed()) {
            return transferTemplateCheckRet;
        }
        fastIndexDTO.setTransferStatus(TRANSFER_STATUS_WAITING);

        return Result.buildSucc();
    }

    /**
     * 校验目标集群节点连接状态
     * @param fastIndexDTO
     * @return
     */
    private Result<Void> checkNodeConnectionStatus(FastIndexDTO fastIndexDTO) {
        //校验目标集群节点状态
        Result<Void> checkTargetClusterNodeStatus = checkTargetClusterNodeStatus(fastIndexDTO);
        if(checkTargetClusterNodeStatus.failed()){
            return Result.buildFrom(checkTargetClusterNodeStatus);
        }
        //校验源集群节点状态
        Result<Void> checkSourceClusterNodeStatus = checkSourceClusterNodeStatus(fastIndexDTO);
        if(checkSourceClusterNodeStatus.failed()){
            return Result.buildFrom(checkSourceClusterNodeStatus);
        }
        return Result.buildSucc();
    }

    /**
     * 校验源集群节点状态
     * @param fastIndexDTO
     * @return
     */
    private Result<Void> checkSourceClusterNodeStatus(FastIndexDTO fastIndexDTO) {
        if (DATA_TYPE_TEMPLATE.equals(fastIndexDTO.getDataType())) {
            ClusterRegion sourceClusterRegion = clusterRegionService
                    .getRegionByLogicClusterId(fastIndexDTO.getSourceLogicClusterId());
            if (fastIndexDTO.getSourceLogicClusterId() == -1 || sourceClusterRegion == null) {
                return Result.buildFail("源逻辑集群异常：region不存在！");
            }
            Result<List<ClusterRoleHost>> clusterRoleHostRet = clusterRoleHostService.listByRegionId(Math.toIntExact(sourceClusterRegion.getId()));
            if (clusterRoleHostRet.failed()) {
                return Result.buildFail(String.format("获取源region[%s]节点列表异常", sourceClusterRegion.getName()));
            }
            List<ClusterRoleHost> hostList = clusterRoleHostRet.getData();
            if (CollectionUtils.isEmpty(hostList)) {
                return Result.buildFail(String.format("获取源region[%s]节点列表为空, 请检查region中是否存在数据节点", sourceClusterRegion.getName()));
            }
            Boolean offLineFlag = hostList.stream().anyMatch(clusterRoleHost ->
                    ESClusterNodeStatusEnum.OFFLINE.getCode() == clusterRoleHost.getStatus());
            if (offLineFlag) {
                return Result.buildFail(String.format("源region[%s]的节点已离线，请恢复后再迁移", sourceClusterRegion.getName()));
            }
        } else {
            List<ClusterRoleHost> nodesByCluster = clusterRoleHostService.getNodesByCluster(fastIndexDTO.getSourceCluster());
            if (CollectionUtils.isEmpty(nodesByCluster)) {
                return Result.buildFail(String.format("获取源集群[%s]节点列表为空, 请检查物理集群中是否存在数据节点", fastIndexDTO.getSourceCluster()));
            }
            Boolean offLineFlag = nodesByCluster.stream().anyMatch(clusterRoleHost ->
                    ESClusterNodeStatusEnum.OFFLINE.getCode() == clusterRoleHost.getStatus());
            if (offLineFlag) {
                return Result.buildFail(String.format("源物理集群[%s]的节点已离线，请恢复后再迁移", fastIndexDTO.getSourceCluster()));
            }
        }
        return Result.buildSucc();
    }

    /**
     * 校验目标集群节点状态
     * @param fastIndexDTO
     * @return
     */
    private Result<Void> checkTargetClusterNodeStatus(FastIndexDTO fastIndexDTO) {
        if (DATA_TYPE_TEMPLATE.equals(fastIndexDTO.getDataType())) {
            ClusterRegion targetClusterRegion = clusterRegionService
                    .getRegionByLogicClusterId(fastIndexDTO.getTargetLogicClusterId());
            if (fastIndexDTO.getTargetLogicClusterId() == -1 || targetClusterRegion == null) {
                return Result.buildFail("目标逻辑集群异常：region不存在！");
            }
            Result<List<ClusterRoleHost>> clusterRoleHostRet = clusterRoleHostService.listByRegionId(Math.toIntExact(targetClusterRegion.getId()));
            if (clusterRoleHostRet.failed()) {
                return Result.buildFail(String.format("获取目标region[%s]节点列表异常", targetClusterRegion.getName()));
            }
            List<ClusterRoleHost> hostList = clusterRoleHostRet.getData();
            if (CollectionUtils.isEmpty(hostList)) {
                return Result.buildFail(String.format("获取目标region[%s]节点列表为空, 请检查region中是否存在数据节点", targetClusterRegion.getName()));
            }
            Boolean offLineFlag = hostList.stream().anyMatch(clusterRoleHost ->
                    ESClusterNodeStatusEnum.OFFLINE.getCode() == clusterRoleHost.getStatus());
            if (offLineFlag) {
                return Result.buildFail(String.format("目标region[%s]的节点已离线，请恢复后再迁移", targetClusterRegion.getName()));
            }
        } else {
            List<ClusterRoleHost> nodesByCluster = clusterRoleHostService.getNodesByCluster(fastIndexDTO.getTargetCluster());
            if (CollectionUtils.isEmpty(nodesByCluster)) {
                return Result.buildFail(String.format("获取目标集群[%s]节点列表为空, 请检查物理集群中是否存在数据节点", fastIndexDTO.getTargetCluster()));
            }
            Boolean offLineFlag = nodesByCluster.stream().anyMatch(clusterRoleHost ->
                    ESClusterNodeStatusEnum.OFFLINE.getCode() == clusterRoleHost.getStatus());
            if (offLineFlag) {
                return Result.buildFail(String.format("目标物理集群[%s]的节点已离线，请恢复后再迁移", fastIndexDTO.getTargetCluster()));
            }
        }
        return Result.buildSucc();
    }

    private Result<Void> checkTemplate(FastIndexDTO fastIndexDTO) {
        List<FastIndexTaskDTO> taskDTOList = fastIndexDTO.getTaskList();
        if (CollectionUtils.isEmpty(taskDTOList)) {
            return Result.buildFail("待迁移任务模版为空！");
        }
        Integer sourceProjectId = fastIndexDTO.getSourceProjectId();
        Long sourceLogicClusterId = fastIndexDTO.getSourceLogicClusterId();
        String phyClusterName = fastIndexDTO.getSourceCluster();

        List<Integer> templateIdList = taskDTOList.stream().map(FastIndexTaskDTO::getSourceTemplateId)
            .collect(Collectors.toList());
        Map<Integer, IndexTemplate> templateMap = indexTemplateService.getLogicTemplatesMapByIds(templateIdList);
        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicIds(templateIdList);
        if (MapUtils.isEmpty(templateMap) || CollectionUtils.isEmpty(templatePhyList)) {
            return Result.buildFail("待迁移任务模版不存在！");
        }
        Map<Integer, IndexTemplatePhy> templateId2TemplatePhy = templatePhyList.stream()
            .filter(
                templatePhy -> TemplateDeployRoleEnum.MASTER == TemplateDeployRoleEnum.valueOf(templatePhy.getRole()))
            .collect(Collectors.toMap(IndexTemplatePhy::getLogicId, o -> o));
        List<Integer> templateNotFoundList = Lists.newArrayList();
        List<Integer> templateFailedList = Lists.newArrayList();

        templateIdList.forEach(templateId -> {
            IndexTemplate template = templateMap.getOrDefault(templateId, null);
            IndexTemplatePhy templatePhy = templateId2TemplatePhy.getOrDefault(templateId, null);
            if (null == template || null == templatePhy) {
                templateNotFoundList.add(templateId);
                return;
            }
            if (!Objects.equals(sourceProjectId, template.getProjectId())
                || !Objects.equals(sourceLogicClusterId, template.getResourceId())
                || !Objects.equals(phyClusterName, templatePhy.getCluster())) {
                templateFailedList.add(templateId);
            }
        });
        if (CollectionUtils.isNotEmpty(templateNotFoundList)) {
            return Result.buildFail("模版不存在：【" + ConvertUtil.list2String(templateNotFoundList, ",") + "】");
        }
        if (CollectionUtils.isNotEmpty(templateFailedList)) {
            return Result.buildFail("模板项目与集群非当前所选原集群：【" + ConvertUtil.list2String(templateFailedList, ",") + "】");
        }

        return Result.buildSucc();
    }

    private Result<Void> checkConnectionStatus(FastIndexDTO fastIndexDTO) {
        //检查任务提交地址是否连通
        if (!esClusterService.isConnectionStatus(fastIndexDTO.getSourceCluster())) {
            return Result.buildFail("原物理集群连通状态异常");
        }
        if (!esClusterService.isConnectionStatus(fastIndexDTO.getTargetCluster())) {
            return Result.buildFail("目标物理集群连通状态异常");
        }
        Result<Boolean> ret = esIndexMoveTaskService.checkHealth(fastIndexDTO);
        if (ret.failed() || Boolean.FALSE.equals(ret.getData())) {
            return Result.buildFail("任务提交地址连通状态异常");
        }
        return Result.buildSucc();
    }

    private Result<Void> modifyIndexTaskRateLimit(Integer taskId, FastIndexDTO fastIndexDTO, Long rateLimit) {
        List<FastIndexTaskInfo> taskInfoList = fastIndexTaskService.listByTaskId(taskId);
        if (CollectionUtils.isEmpty(taskInfoList)) {
            LOGGER.warn("class=FastIndexManagerImpl||method=modifyIndexTaskRateLimit||msg= index task is empty !");
            return Result.buildSucc();
        }
        //未提交、等待执行、执行中的任务需要更新限流值
        List<FastIndexTaskInfo> needModifyIndexTask = taskInfoList.stream()
            .filter(task -> task.getTaskStatus() < FastIndexTaskStatusEnum.SUCCESS.getValue())
            .collect(Collectors.toList());
        BigDecimal finalRateLimit = BigDecimal.valueOf(rateLimit).divide(BigDecimal.valueOf(taskInfoList.size()),
            RoundingMode.UP);
        //校验任务读取速率
        FastIndexDTO newFastIndexDTO = new FastIndexDTO();
        newFastIndexDTO.setTaskReadRate(rateLimit.longValue());
        Result<List<FastIndexTaskInfo>> checkReadFileRateLimitResult = checkReadFileRateLimit(newFastIndexDTO, taskInfoList.size());
        if (checkReadFileRateLimitResult.failed()) {
            return Result.buildFrom(checkReadFileRateLimitResult);
        }
        needModifyIndexTask.forEach(indexTask -> {
            String fastDumpTaskId = indexTask.getFastDumpTaskId();
            if (StringUtils.isNotBlank(fastDumpTaskId)) {
                ESIndexMoveTaskAdjustReadRateContext context = new ESIndexMoveTaskAdjustReadRateContext(fastDumpTaskId,
                    finalRateLimit.longValue());
                Tuple<String, Result<Void>> resultTuple = esIndexMoveTaskService.adjustReadRate(fastIndexDTO, context);
                indexTask.setLastResponse(resultTuple.getV1());
            }
            indexTask.setReadFileRateLimit(finalRateLimit);
            fastIndexTaskService.refreshTask(indexTask);
        });
        return Result.buildSucc();
    }

    private Result<Void> restartIndexTask(Integer taskId) {
        List<FastIndexTaskInfo> taskInfoList = fastIndexTaskService.listByTaskIdAndStatus(taskId,
            Arrays.asList(FastIndexTaskStatusEnum.FAILED.getValue(), FastIndexTaskStatusEnum.CANCEL.getValue()));
        //如不存在需要重试的任务，则结束方法
        if (CollectionUtils.isEmpty(taskInfoList)) {
            return Result.buildSucc();
        }

        //这里刷新子任务的状态
        taskInfoList.forEach(indexTask -> {
            indexTask.setFastDumpTaskId("");
            indexTask.setTaskStatus(FastIndexTaskStatusEnum.NOT_SUBMITTED.getValue());
            indexTask.setTaskSubmitResult("");
            indexTask.setTaskStartTime(null);
            indexTask.setTaskEndTime(null);
            indexTask.setSuccDocumentNum(BigDecimal.ZERO);
            indexTask.setFailedDocumentNum(BigDecimal.ZERO);
            indexTask.setSuccShardNum(BigDecimal.ZERO);
            indexTask.setTaskCostTime(BigDecimal.ZERO);
            indexTask.setLastResponse("");
            fastIndexTaskService.refreshTask(indexTask);
        });

        return Result.buildSucc();
    }

    private Result<Void> cancelIndexTask(Integer taskId, FastIndexDTO fastIndexDTO) {
        List<FastIndexTaskInfo> taskInfoList = fastIndexTaskService.listByTaskIdAndStatus(taskId,
            Arrays.asList(FastIndexTaskStatusEnum.NOT_SUBMITTED.getValue(), FastIndexTaskStatusEnum.WAITING.getValue(),
                FastIndexTaskStatusEnum.RUNNING.getValue()));
        //如不存在拥有等待中或者运行中的任务，则结束方法
        if (CollectionUtils.isEmpty(taskInfoList)) {
            return Result.buildSucc();
        }

        //这里刷新子任务的状态
        taskInfoList.forEach(indexTask -> {
            String fastDumpTaskId = indexTask.getFastDumpTaskId();
            if (StringUtils.isNotBlank(fastDumpTaskId)) {
                Tuple<String, Result<Void>> resultTuple = esIndexMoveTaskService.stopTask(fastIndexDTO, fastDumpTaskId);
                indexTask.setLastResponse(resultTuple.getV1());
            }
            indexTask.setTaskStatus(FastIndexTaskStatusEnum.CANCEL.getValue());
            indexTask.setTaskEndTime(new Date());
            fastIndexTaskService.refreshTask(indexTask);
        });

        return Result.buildSucc();
    }

    private Result<Void> refreshTaskStatus(OpTask opTask) {
        List<FastIndexTaskInfo> taskInfoList = fastIndexTaskService.listByTaskId(opTask.getId());
        Map<Integer, List<FastIndexTaskInfo>> status2TaskList = taskInfoList.stream()
            .collect(Collectors.groupingBy(FastIndexTaskInfo::getTaskStatus));
        //是否需要提交任务
        List<FastIndexTaskInfo> needSubmitTaskList = status2TaskList
            .getOrDefault(FastIndexTaskStatusEnum.NOT_SUBMITTED.getValue(), Lists.newArrayList());
        List<FastIndexTaskInfo> runningTaskList = Lists.newArrayList();
        runningTaskList
            .addAll(status2TaskList.getOrDefault(FastIndexTaskStatusEnum.WAITING.getValue(), Lists.newArrayList()));
        runningTaskList
            .addAll(status2TaskList.getOrDefault(FastIndexTaskStatusEnum.RUNNING.getValue(), Lists.newArrayList()));
        //主任务是否执行中（拥有等待中或者运行中的任务）
        boolean isRunning = CollectionUtils.isNotEmpty(runningTaskList);
        boolean hasSuccess = CollectionUtils
            .isNotEmpty(status2TaskList.get(FastIndexTaskStatusEnum.SUCCESS.getValue()));
        boolean hasFail = CollectionUtils.isNotEmpty(status2TaskList.get(FastIndexTaskStatusEnum.FAILED.getValue()));
        boolean hasCancel = CollectionUtils.isNotEmpty(status2TaskList.get(FastIndexTaskStatusEnum.CANCEL.getValue()));

        FastIndexTaskStatusEnum fastIndexTaskStatusEnum = getFastIndexTaskStatusEnum(
            CollectionUtils.isNotEmpty(needSubmitTaskList), isRunning, hasCancel, hasFail, hasSuccess);
        OpTaskStatusEnum taskStatusEnum = OpTaskStatusEnum.valueOfStatus(fastIndexTaskStatusEnum.getCode());
        if (taskStatusEnum == OpTaskStatusEnum.UNKNOWN
            && fastIndexTaskStatusEnum == FastIndexTaskStatusEnum.NOT_SUBMITTED) {
            taskStatusEnum = OpTaskStatusEnum.WAITING;
        }
        FastIndexDTO fastIndexDTO = JSON.parseObject(opTask.getExpandData(), FastIndexDTO.class);
        //挑选任务并提交
        if (null == fastIndexDTO) {
            //打印日志
            return FAIL_RESULT_GET_FAST_INDEX_TASK_ERROR;
        }
        //刷新主任务状态
        OpTaskProcessDTO processDTO = new OpTaskProcessDTO();
        processDTO.setTaskId(opTask.getId());
        processDTO.setStatus(taskStatusEnum.getStatus());
        processDTO.setExpandData(opTask.getExpandData());
        Result<Void> result = processTask(processDTO);

        if (result.success() && OpTaskStatusEnum.SUCCESS == taskStatusEnum
            && Boolean.TRUE.equals(fastIndexDTO.getTransfer())
            && TRANSFER_STATUS_WAITING.equals(fastIndexDTO.getTransferStatus())) {
            return transferTemplate(opTask.getId());
        }
        return result;
    }

    private Result<Void> refreshIndexTaskStats(Integer taskId, FastIndexDTO fastIndexDTO) {
        List<FastIndexTaskInfo> runningTaskList = fastIndexTaskService.listByTaskIdAndStatus(taskId,
            Arrays.asList(FastIndexTaskStatusEnum.WAITING.getValue(), FastIndexTaskStatusEnum.RUNNING.getValue()));
        //如不存在拥有等待中或者运行中的任务，则结束方法
        if (CollectionUtils.isEmpty(runningTaskList)) {
            return Result.buildSucc();
        }
        Map<String, ESIndexMoveTaskStats> fastDumpTaskId2stats = new HashMap<>();
        //这里从内核拿到全部的任务
        Result<List<ESIndexMoveTaskStats>> allTaskStats = esIndexMoveTaskService.getAllTaskStats(fastIndexDTO);
        if (allTaskStats.failed()) {
            return Result.buildFrom(allTaskStats);
        }
        fastDumpTaskId2stats
            .putAll(allTaskStats.getData().stream().collect(Collectors.toMap(ESIndexMoveTaskStats::getTaskId, o -> o)));

        //这里刷新子任务的状态
        runningTaskList.forEach(indexTask -> {
            String key = indexTask.getFastDumpTaskId();
            ESIndexMoveTaskStats taskStats = fastDumpTaskId2stats.getOrDefault(key, null);
            if (null == taskStats) {
                Tuple<String, Result<ESIndexMoveTaskStats>> resultTuple = esIndexMoveTaskService
                    .getTaskStats(fastIndexDTO, key);
                if (resultTuple.getV2().success()) {
                    taskStats = resultTuple.getV2().getData();
                } else {
                    indexTask.setTaskStatus(FastIndexTaskStatusEnum.FAILED.getValue());
                }
                indexTask.setLastResponse(resultTuple.getV1());
            } else {
                indexTask.setLastResponse(JSON.toJSONString(taskStats));
            }
            if (null == taskStats) {
                //如果提交任务的时间与当前时间相差超过 TASK_WAITING_TIME，则认为任务失败，否则暂时不更新任务状态
                Date taskStartTime = indexTask.getTaskStartTime();
                if (null == taskStartTime || System.currentTimeMillis() - taskStartTime.getTime() > TASK_WAITING_TIME) {
                    indexTask.setTaskStatus(FastIndexTaskStatusEnum.FAILED.getValue());
                    indexTask.setFailedDocumentNum(Optional.ofNullable(indexTask.getTotalDocumentNum()).orElse(BigDecimal.ZERO));
                    indexTask.setTaskEndTime(new Date());
                    fastIndexTaskService.refreshTask(indexTask);
                }
                return;
            }
            FastIndexTaskStatusEnum fastIndexTaskStatusEnum = FastIndexTaskStatusEnum.enumOfCode(taskStats.getStatus());
            if (fastIndexTaskStatusEnum == FastIndexTaskStatusEnum.PAUSE) {
                //由于内核中任务stop后状态为 pause暂停，索引这里将暂停状态设置为取消
                fastIndexTaskStatusEnum = FastIndexTaskStatusEnum.CANCEL;
            }
            indexTask.setTaskStatus(fastIndexTaskStatusEnum.getValue());
            indexTask.setTotalDocumentNum(valueOfBigDecimal(taskStats.getTotalDocumentNum()));
            indexTask.setSuccDocumentNum(valueOfBigDecimal(taskStats.getSuccDocumentNum()));
            indexTask.setFailedDocumentNum(valueOfBigDecimal(taskStats.getFailedDocumentNum()));
            indexTask.setShardNum(valueOfBigDecimal(taskStats.getShardNum()));
            indexTask.setSuccShardNum(valueOfBigDecimal(taskStats.getSuccShardNum()));
            indexTask.setTaskCostTime(valueOfBigDecimal(taskStats.getCostTime()));
            if (fastIndexTaskStatusEnum == FastIndexTaskStatusEnum.SUCCESS) {
                indexTask.setTaskEndTime(new Date());
            }
            fastIndexTaskService.refreshTask(indexTask);
        });

        return Result.buildSucc();
    }

    private BigDecimal valueOfBigDecimal(Long num) {
        if (null != num) {
            return BigDecimal.valueOf(num);

        }
        return null;
    }

    private Result<Void> submitIndexTask(Integer taskId, FastIndexDTO fastIndexDTO, ClusterPhy sourceCluster,
                                         ClusterPhy targetCluster) {
        //查询未提交、等待中、运行中的任务
        List<FastIndexTaskInfo> taskInfoList = fastIndexTaskService.listByTaskIdAndStatus(taskId,
            Arrays.asList(FastIndexTaskStatusEnum.NOT_SUBMITTED.getValue(), FastIndexTaskStatusEnum.WAITING.getValue(),
                FastIndexTaskStatusEnum.RUNNING.getValue()));
        Map<Integer, List<FastIndexTaskInfo>> status2TaskList = taskInfoList.stream()
            .collect(Collectors.groupingBy(FastIndexTaskInfo::getTaskStatus));
        //是否需要提交任务
        List<FastIndexTaskInfo> needSubmitTaskList = status2TaskList
            .getOrDefault(FastIndexTaskStatusEnum.NOT_SUBMITTED.getValue(), Lists.newArrayList());
        List<FastIndexTaskInfo> runningTaskList = Lists.newArrayList();
        runningTaskList
            .addAll(status2TaskList.getOrDefault(FastIndexTaskStatusEnum.WAITING.getValue(), Lists.newArrayList()));
        runningTaskList
            .addAll(status2TaskList.getOrDefault(FastIndexTaskStatusEnum.RUNNING.getValue(), Lists.newArrayList()));
        //主任务是否执行中（拥有等待中或者运行中的任务）
        if (CollectionUtils.isNotEmpty(runningTaskList) || CollectionUtils.isEmpty(needSubmitTaskList)) {
            return Result.buildSucc();
        }
        //挑选出文档数为0的任务置为成功
        needSubmitTaskList.forEach(taskInfo -> {
            if (BigDecimal.ZERO.compareTo(Optional.ofNullable(taskInfo.getTotalDocumentNum()).orElse(BigDecimal.ZERO)) == 0) {
                taskInfo.setTotalDocumentNum(BigDecimal.ZERO);
                taskInfo.setTaskStatus(FastIndexTaskStatusEnum.SUCCESS.getValue());//成功
                taskInfo.setSuccDocumentNum(BigDecimal.ZERO);
                taskInfo.setTaskCostTime(BigDecimal.ZERO);
                taskInfo.setTaskStartTime(new Date());
                taskInfo.setTaskEndTime(new Date());
                fastIndexTaskService.refreshTask(taskInfo);
            }
        });
        //挑选任务提交到内核并更新任务状态
        List<FastIndexTaskInfo> submitTaskList = needSubmitTaskList.stream()
                .filter(taskInfo -> !FastIndexTaskStatusEnum.SUCCESS.getValue().equals(taskInfo.getTaskStatus())).collect(Collectors.toList());
        submitTaskList.sort(Comparator.comparingInt(FastIndexTaskInfo::getId));
        List<FastIndexTaskInfo> executeList = submitTaskList.subList(0,
            Math.min(submitTaskList.size(), TASK_EXECUTE_BATCH_SIZE));

        executeList.forEach(taskInfo -> {
            ESIndexMoveTaskContext context = buildIndexMoveTaskContext(fastIndexDTO, taskInfo, sourceCluster,
                targetCluster);
            Tuple<String, Result<String>> resultTuple = esIndexMoveTaskService.submitTask(fastIndexDTO, context);
            taskInfo.setTaskSubmitResult(resultTuple.getV1());
            if (resultTuple.getV2() != null && resultTuple.getV2().success()) {
                taskInfo.setTaskStatus(FastIndexTaskStatusEnum.WAITING.getValue());
                taskInfo.setFastDumpTaskId(resultTuple.getV2().getData());
                taskInfo.setTaskStartTime(new Date());
            } else {
                taskInfo.setTaskStatus(FastIndexTaskStatusEnum.FAILED.getValue());
                taskInfo.setFailedDocumentNum(Optional.ofNullable(taskInfo.getTotalDocumentNum()).orElse(BigDecimal.ZERO));
                taskInfo.setTaskStartTime(new Date());
                taskInfo.setTaskEndTime(new Date());
            }
            fastIndexTaskService.refreshTask(taskInfo);
        });
        return Result.buildSucc();
    }

    private ESIndexMoveTaskContext buildIndexMoveTaskContext(FastIndexDTO fastIndexDTO, FastIndexTaskInfo indexTaskInfo,
                                                             ClusterPhy sourceCluster, ClusterPhy targetCluster) {
        ESIndexMoveTaskContext moveTaskContext = new ESIndexMoveTaskContext();
        Boolean ignoreVersion = null;
        Boolean ignoreId = null;
        if (Objects.equals(WRITE_TYPE_CREATE, fastIndexDTO.getWriteType())) {
            ignoreVersion = Boolean.TRUE;
            ignoreId = Boolean.TRUE;
        } else if (Objects.equals(WRITE_TYPE_INDEX, fastIndexDTO.getWriteType())) {
            ignoreVersion = Boolean.TRUE;
            ignoreId = Boolean.FALSE;
        } else if (Objects.equals(WRITE_TYPE_INDEX_WITH_ID, fastIndexDTO.getWriteType())) {
            ignoreVersion = Boolean.FALSE;
            ignoreId = Boolean.FALSE;
        }
        Integer readFileRateLimit = null;
        if (null != indexTaskInfo.getReadFileRateLimit()) {
            readFileRateLimit = indexTaskInfo.getReadFileRateLimit().intValue();
        }

        moveTaskContext.setReader(new ESIndexMoveTaskContext.ReaderDTO(ignoreVersion, ignoreId, readFileRateLimit));
        Tuple<String, String> sourcePw = split2Tuple(sourceCluster.getPassword());
        if (AriusObjUtils.isNull(sourcePw.getV1()) && AriusObjUtils.isNull(sourcePw.getV2())) {
            moveTaskContext.setSource(new ESIndexMoveTaskContext.SourceDTO(indexTaskInfo.getIndexName(),
                    sourceCluster.getHttpAddress(),indexTaskInfo.getIndexTypes(), null));
        } else {
            moveTaskContext.setSource(new ESIndexMoveTaskContext.SourceDTO(indexTaskInfo.getIndexName(),
                    sourceCluster.getHttpAddress(), sourcePw.getV1(), sourcePw.getV2(), indexTaskInfo.getIndexTypes(), null));
        }

        Tuple<String, String> targetPw = split2Tuple(targetCluster.getPassword());
        if (AriusObjUtils.isNull(targetPw.getV1()) && AriusObjUtils.isNull(targetPw.getV2())) {
            moveTaskContext.setSinker(new ESIndexMoveTaskContext.SinkerDTO(indexTaskInfo.getTargetIndexName(),
                    targetCluster.getHttpAddress(), fastIndexDTO.getTargetIndexType()));
        } else {
            moveTaskContext.setSinker(new ESIndexMoveTaskContext.SinkerDTO(indexTaskInfo.getTargetIndexName(),
                    targetCluster.getHttpAddress(), targetPw.getV1(), targetPw.getV2(), fastIndexDTO.getTargetIndexType()));
        }
        return moveTaskContext;
    }

    private Map<String, String> getSettingsMap(Integer regionId, String settings) throws AdminOperateException {
        Map<String, String> settingsMap = new HashMap<>();
        if (null != settings) {
            settingsMap.putAll(AriusIndexTemplateSetting.flat(JSON.parseObject(settings)));
        }
        if (null != regionId) {
            Result<List<ClusterRoleHost>> roleHostResult = clusterRoleHostService.listByRegionId(regionId);
            if (roleHostResult.failed()) {
                throw new AdminOperateException(String.format("获取region[%d]节点列表异常", regionId));
            }
            List<ClusterRoleHost> data = roleHostResult.getData();
            if (CollectionUtils.isEmpty(data)) {
                throw new AdminOperateException(String.format("获取region[%d]节点列表为空, 请检查region中是否存在数据节点", regionId));
            }
            List<String> nodeNames = data.stream().map(ClusterRoleHost::getNodeSet)
                .filter(nodeName -> !AriusObjUtils.isBlank(nodeName)).distinct().collect(Collectors.toList());
            settingsMap.put(TEMPLATE_INDEX_INCLUDE_NODE_NAME, String.join(",", nodeNames));
        }
        return settingsMap;
    }

    private Tuple<String, String> split2Tuple(String pw) {
        String[] arr = StringUtils.split(pw, ":");
        if (null != arr && arr.length == 2) {
            return new Tuple<>(arr[0], arr[1]);
        }
        return new Tuple<>("", "");
    }

    /**
     * 注意， 这里普通用户侧前端传输cluster值是：逻辑集群名称，运维侧是：物理集群名称
     * @param cluster
     * @param projectId
     * @return
     */
    private Result<String> getClusterPhyByClusterNameAndProjectId(String cluster, Integer projectId) {
        String phyClusterName;
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            phyClusterName = cluster;
        } else {
            ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByNameAndProjectId(cluster,projectId );
            if (null == clusterLogic) {
                return Result.buildParamIllegal(String.format("逻辑集群[%s]不存在", cluster));
            }
            ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
            if (null == clusterRegion) {
                return Result.buildParamIllegal("逻辑集群未绑定Region");
            }
            phyClusterName = clusterRegion.getPhyClusterName();
            if (!esClusterService.isConnectionStatus(phyClusterName)){
                return Result.buildFail(String.format("%s 集群不正常",cluster));
            }
        }
        return Result.buildSucc(phyClusterName);
    }

    private Result<Void> basicCheckParam(String cluster, String index, Integer projectId) {
        if (!projectService.checkProjectExist(projectId)) {
            return Result.buildParamIllegal(String.format("当前登录项目Id[%s]不存在, 无权限操作", projectId));
        }

        if (!clusterPhyService.isClusterExists(cluster)) {
            return Result.buildParamIllegal(String.format("物理集群[%s]不存在", cluster));
        }

        if (!esIndexService.syncIsIndexExist(cluster, index)) {
            return Result.buildParamIllegal(String.format("集群[%s]中的索引[%s]不存在", cluster, index));
        }

        return Result.buildSucc();
    }

    /**
     * 获取es的版本前缀
     *
     * @param esVersion
     * @return {@link String}
     */
    private static String getESBigVersion(String esVersion) {
        Pattern pattern = compile(VERSION_PREFIX_PATTERN);
        final Matcher matcher = pattern.matcher(esVersion);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }
}
