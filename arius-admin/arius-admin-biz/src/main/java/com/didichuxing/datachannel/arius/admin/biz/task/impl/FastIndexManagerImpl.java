package com.didichuxing.datachannel.arius.admin.biz.task.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.FAST_INDEX_TASK_LOG;
import static com.didichuxing.datachannel.arius.admin.common.constant.task.FastIndexConstant.*;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.TEMPLATE_INDEX_INCLUDE_NODE_NAME;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastIndexBriefVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexLogsConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexRateLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexTaskDTO;
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
import com.didichuxing.datachannel.arius.admin.common.constant.task.FastIndexConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.task.FastIndexTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
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
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.IndexConfig;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
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
    private static final long                                        TASK_WAITING_TIME                     = 5 * 1000L;

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
            taskIndexListRet = createTemplateAndGetTaskIndexListByTemplate(fastIndexDTO, targetClusterRegion, taskList);
        } else if (FastIndexConstant.DATA_TYPE_INDEX.equals(fastIndexDTO.getDataType())) {
            taskIndexListRet = getTaskIndexListByIndex(fastIndexDTO, taskList);
        }
        if (taskIndexListRet.failed()) {
            return Result.buildFrom(taskIndexListRet);
        } else if (CollectionUtils.isNotEmpty(taskIndexListRet.getData())) {
            taskIndexList.addAll(taskIndexListRet.getData());
        } else {
            return Result.buildFail("获取索引任务失败！");
        }

        //创建目标索引
        Result<Void> createIndexRet = createTargetIndex(targetCluster, taskIndexList);
        if (createIndexRet.failed()) {
            return Result.buildFrom(createIndexRet);
        }
        //2.1 设置基础数据
        String businessKey = String.format("%s=>>%S", sourceCluster.getCluster(), targetCluster.getCluster());
        Result<OpTask> workTaskResult = addOpTask(fastIndexDTO, operator, businessKey);
        if (workTaskResult.failed()) {
            return Result.buildFrom(workTaskResult);
        }
        //拆分任务数据到数据库
        OpTask opTask = workTaskResult.getData();
        boolean ret = saveIndexTask(fastIndexDTO, taskIndexList, opTask.getId());

        //记录操作
        if (ret) {
            String indexName = "";
            String dataTypeName = "";
            if (FastIndexConstant.DATA_TYPE_TEMPLATE.equals(fastIndexDTO.getDataType())) {
                //索引模板（索引）
                dataTypeName = "索引模版";
                indexName = taskIndexList.stream().map(FastIndexTaskInfo::getTemplateName)
                    .collect(Collectors.joining("、"));
            } else if (FastIndexConstant.DATA_TYPE_INDEX.equals(fastIndexDTO.getDataType())) {
                dataTypeName = "索引";
                indexName = taskIndexList.stream().map(FastIndexTaskInfo::getIndexName)
                    .collect(Collectors.joining("、"));
            }
            operateRecordService
                .saveOperateRecordWithManualTrigger(
                    String.format("%s数据迁移至%s，迁移%s：%s", sourceCluster.getCluster(), targetCluster.getCluster(),
                        dataTypeName, indexName),
                    operator, projectId, businessKey, OperateTypeEnum.PHYSICAL_CLUSTER_FAST_INDEX);
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
                FastIndexStats templateStats = calculationTaskStats(taskIndexStatsList);
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
                FAST_INDEX_MANAGER_FUTURE_UTIL.callableTask(() -> templateLogicManager.transferTemplate(templateId,
                    targetProjectId, targetLogicClusterId, phyClusterName)).waitResultQueue()));
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
    public Result< Map<String, List<FastIndexBriefVO>>> getTemplateAndIndexBrief(Integer taskId) {
        OpTask opTask = opTaskService.getById(taskId);
        if (null == opTask) {
            return Result.buildFrom(FAIL_RESULT_GET_OP_TASK_ERROR);
        }
        List<FastIndexTaskInfo> taskIndexList = fastIndexTaskService.listByTaskId(taskId);
        List<FastIndexBriefVO> fastIndexBriefVOS = ConvertUtil.list2List(taskIndexList, FastIndexBriefVO.class);
        Map<String, List<FastIndexBriefVO>> fastIndexBriefVO = fastIndexBriefVOS.stream().collect(Collectors.groupingBy(FastIndexBriefVO::getTemplateName));
        return Result.buildSucc(fastIndexBriefVO);
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
                taskInfo.setTaskStatus(FastIndexTaskStatusEnum.SUCCESS.getValue());//成功
                taskInfo.setSuccDocumentNum(BigDecimal.ZERO);
                taskInfo.setTaskCostTime(BigDecimal.ZERO);
                taskInfo.setTaskStartTime(new Date());
                taskInfo.setTaskEndTime(new Date());
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
                taskInfo.setTotalDocumentNum(new BigDecimal(catIndexResult.getDocsCount()));
                return Result.buildSucc(taskInfo);
            }).waitResultQueue())));
        resultList.addAll(FAST_INDEX_TASK_INDEX_FUTURE_UTIL.waitResult());
        List<Result<FastIndexTaskInfo>> failedList = resultList.stream().filter(BaseResult::failed)
            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(failedList)) {
            return Result.buildFail(failedList.stream().map(BaseResult::getMessage).collect(Collectors.joining("\n")));
        }
        return Result.buildSucc(resultList.stream().map(Result::getData).collect(Collectors.toList()));
    }

    private Result<List<FastIndexTaskInfo>> createTemplateAndGetTaskIndexListByTemplate(FastIndexDTO fastIndexDTO,
                                                                                        ClusterRegion targetClusterRegion,
                                                                                        List<FastIndexTaskDTO> taskList) {
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

        List<Result<List<FastIndexTaskInfo>>> resultList = Lists.newCopyOnWriteArrayList();
        taskList.forEach(task -> resultList.addAll(FAST_INDEX_TASK_TEMPLATE_FUTURE_UTIL.callableTask(() -> {
            try {
                IndexTemplate logic = templateId2Logic.get(task.getSourceTemplateId());
                //创建模版
                Result<Void> createRet = createTargetTemplate(fastIndexDTO, targetClusterRegion, task, logic);
                if (createRet.failed()) {
                    return Result.buildFrom(createRet);
                }
                //根据模版在原集群中获取索引列表
                List<CatIndexResult> results = esIndexService.syncCatIndexByExpression(fastIndexDTO.getSourceCluster(),
                    logic.getExpression());
                if (null == results) {
                    return Result.buildFail("根据模版【" + logic.getName() + "】获取原集群中的索引失败！");
                }

                return Result.buildSucc(results.stream().filter(Objects::nonNull).map(catIndex -> {
                    FastIndexTaskInfo taskInfo = new FastIndexTaskInfo();
                    taskInfo.setTaskType(DATA_TYPE_TEMPLATE);//template
                    taskInfo.setTemplateId(logic.getId());
                    taskInfo.setTemplateName(logic.getName());
                    taskInfo.setIndexName(catIndex.getIndex());
                    taskInfo.setTargetIndexName(catIndex.getIndex());
                    taskInfo.setTaskStatus(FastIndexTaskStatusEnum.NOT_SUBMITTED.getValue());//未提交
                    taskInfo.setTotalDocumentNum(new BigDecimal(catIndex.getDocsCount()));
                    return taskInfo;
                }).collect(Collectors.toList()));
            } catch (AdminOperateException e) {
                //pass
                return Result.buildFail("创建模版失败");
            }
        }).waitResultQueue()));
        resultList.addAll(FAST_INDEX_TASK_TEMPLATE_FUTURE_UTIL.waitResult());
        List<Result<List<FastIndexTaskInfo>>> failedList = resultList.stream().filter(BaseResult::failed)
            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(failedList)) {
            return Result.buildFail(failedList.stream().map(BaseResult::getMessage).collect(Collectors.joining("\n")));
        }
        return Result.buildSucc(
            resultList.stream().map(Result::getData).flatMap(Collection::stream).collect(Collectors.toList()));
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
        if (DATA_TYPE_TEMPLATE.equals(fastIndexDTO.getDataType())) {
            ClusterRegion targetClusterRegion = clusterRegionService
                .getRegionByLogicClusterId(fastIndexDTO.getTargetLogicClusterId());
            if (fastIndexDTO.getTargetLogicClusterId() == -1 || targetClusterRegion == null) {
                return Result.buildFail("目标逻辑集群异常：region不存在！");
            }
        }
        Result<Void> checkConnectionStatusRet = checkConnectionStatus(fastIndexDTO);
        if (checkConnectionStatusRet.failed()) {
            return checkConnectionStatusRet;
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
        //挑选任务提交到内核并更新任务状态
        needSubmitTaskList.sort(Comparator.comparingInt(FastIndexTaskInfo::getId));
        List<FastIndexTaskInfo> executeList = needSubmitTaskList.subList(0,
            Math.min(needSubmitTaskList.size(), TASK_EXECUTE_BATCH_SIZE));

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
        moveTaskContext.setSource(new ESIndexMoveTaskContext.SourceDTO(indexTaskInfo.getIndexName(),
            sourceCluster.getHttpAddress(), sourcePw.getV1(), sourcePw.getV2(), indexTaskInfo.getIndexTypes(), null));
        Tuple<String, String> targetPw = split2Tuple(targetCluster.getPassword());
        moveTaskContext.setSinker(new ESIndexMoveTaskContext.SinkerDTO(indexTaskInfo.getTargetIndexName(),
            targetCluster.getHttpAddress(), targetPw.getV1(), targetPw.getV2(), fastIndexDTO.getTargetIndexType()));
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

}
