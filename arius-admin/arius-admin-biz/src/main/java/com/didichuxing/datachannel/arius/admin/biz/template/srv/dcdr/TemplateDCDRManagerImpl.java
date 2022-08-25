package com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr;

import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum.MASTER;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum.SLAVE;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.OpTaskManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.DCDRMasterSlaveSwitchDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalCopyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalDCDRDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.DCDRSingleTemplateMasterSlaveSwitchDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.DCDRTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.DCDRTasksDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DCDRSingleTemplateMasterSlaveSwitchDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DCDRTasksDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateDCDRInfoVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.dcdr.DCDRStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.dcdr.DCDRSwithTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDCDRStepEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusTaskThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.BatchProcessor;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ESVersionUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.dcdr.ESDCDRService;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 索引DCDR服务实现
 * @author zqr
 * @date 2020-09-09
 */
@Service
public class TemplateDCDRManagerImpl extends BaseTemplateSrvImpl implements TemplateDCDRManager {

    private static final ILog     LOGGER                      = LogFactory.getLog(TemplateDCDRManagerImpl.class);

    private static final String   DCDR_TEMPLATE_NAME_FORMAT   = "%s_to_%s";

    private static final String   DCDR_INDEX_SETTING          = "dcdr.replica_index";

    private static final int      DCDR_SWITCH_STEP_1          = 1;
    private static final int      DCDR_SWITCH_STEP_2          = 2;
    private static final int      DCDR_SWITCH_STEP_3          = 3;
    private static final int      DCDR_SWITCH_STEP_4          = 4;
    private static final int      DCDR_SWITCH_STEP_5          = 5;
    private static final int      DCDR_SWITCH_STEP_6          = 6;
    private static final int      DCDR_SWITCH_STEP_7          = 7;
    private static final int      DCDR_SWITCH_STEP_8          = 8;
    private static final int      DCDR_SWITCH_STEP_9          = 9;
    private static final String   DCDR_SWITCH_TODO            = "TODO";
    private static final String   DCDR_SWITCH_DONE            = "DONE";
    private static final String   DCDR_SWITCH_FAIL            = "FAIL";
    private static final String   SEPARATOR                   = "@@@";
    private static final String   SUCCESS_INFO                = "Successful Execution";

    private static final String[] DCDR_SWITCH_STEP_ARR_SMOOTH = new String[] { TemplateDCDRStepEnum.STEP_1.getValue(),
                                                                               TemplateDCDRStepEnum.STEP_2.getValue(),
                                                                               TemplateDCDRStepEnum.STEP_3.getValue(),
                                                                               TemplateDCDRStepEnum.STEP_4.getValue(),
                                                                               TemplateDCDRStepEnum.STEP_5.getValue(),
                                                                               TemplateDCDRStepEnum.STEP_6.getValue(),
                                                                               TemplateDCDRStepEnum.STEP_7.getValue(),
                                                                               TemplateDCDRStepEnum.STEP_8.getValue(),
                                                                               TemplateDCDRStepEnum.STEP_9.getValue() };

    private static final String[] DCDR_SWITCH_STEP_ARR_FORCE  = new String[] { TemplateDCDRStepEnum.STEP_5.getValue(),
                                                                               TemplateDCDRStepEnum.STEP_9.getValue() };

    private static final String   TEMPLATE_NO_EXIST           = "模板不存在";

    private static final String   TASK_IS_CANCEL              = "任务已取消";

    private static final String   TASK_EMPTY                  = "根据任务Id[%s]获取任务失败";
    public static final int       MAX_PHY_TEMPLATE_NUM        = 2;

    @Value("${dcdr.concurrent:2}")
    private Integer               dcdrConcurrent;

    @Value("${dcdr.fault.tolerant:5}")
    private Integer               dcdrFaultTolerant;
    
    @Autowired
    private ESDCDRService esDCDRService;

    @Autowired
    private ESIndexService        esIndexService;

    @Autowired
    private ESTemplateService     esTemplateService;

    @Autowired
    private OpTaskManager         opTaskManager;
  

    
    private static final int      TRY_LOCK_TIMEOUT            = 5;
    private static final int      ONE_STEP                    = 1;
    private static final int      TRY_TIMES_THREE             = 3;

    /**
     * @return
     */
    @Override
    public TemplateServiceEnum templateSrv() {
        return TemplateServiceEnum.TEMPLATE_DCDR;
    }

    private AriusTaskThreadPool           ariusTaskThreadPool;

    private Cache<Integer, ReentrantLock> taskId2ReentrantLockCache = CacheBuilder.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES).maximumSize(100).build();

    @PostConstruct
    public void init() {
        ariusTaskThreadPool = new AriusTaskThreadPool();
        ariusTaskThreadPool.init(10, "TemplateDCDRManagerImpl", 10000);
    }
  
    
    private static final FutureUtil<Void> BATCH_DCDR_FUTURE_UTIL = FutureUtil.init("BATCH_DCDR_FUTURE_UTIL", 10, 10,
        100);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> copyAndCreateDCDR(Integer templateId, String targetCluster, Integer regionId, String operator,
                                          Integer projectId) throws AdminOperateException {
        //1. 判断目标集群是否存在模板, 存在则需要删除, 避免copy失败，确保copy流程的执行来保证主从模板setting mapping等信息的一致性。
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(templateId);
        if (null == templateLogicWithPhysical) {
            return Result.buildParamIllegal(TEMPLATE_NO_EXIST);
        }

        IndexTemplatePhy slavePhyTemplate = templateLogicWithPhysical.getSlavePhyTemplate();
        if (null != slavePhyTemplate) {
            //1.1删除DCDR链路
            Result<Void> deleteDCDRResult = deleteDCDR(templateId, operator, projectId);
            if (deleteDCDRResult.failed()) {
                return deleteDCDRResult;
            }

            //1.2清理slave模板
            Result<Void> delTemplateResult = indexTemplatePhyService.delTemplate(slavePhyTemplate.getId(), operator);
            if (delTemplateResult.failed()) {
                return delTemplateResult;
            }
        }

        // 2. 校验目标集群合法性
        Result<ClusterPhy> targetClusterPhyResult = clusterPhyManager.getClusterByName(targetCluster);
        if (null == targetClusterPhyResult.getData()) {
            return Result.buildFail(String.format("目标集群[%s]不存在", targetCluster));
        }
        //校验target具备dcdr
        if (Boolean.FALSE.equals(
                clusterPhyManager.getDCDRAndPipelineAndColdRegionTupleByClusterPhyWithCache(targetCluster).v1)) {
            return Result.buildFail(String.format("目标集群【%s】不支持dcdr", targetCluster));
        }
        IndexTemplatePhy masterPhyTemplate = templateLogicWithPhysical.getMasterPhyTemplate();
        if (null == masterPhyTemplate) {
            return Result.buildFail(String.format("模板Id[%s]不存在", templateId));
        }

        if (AriusObjUtils.isBlack(masterPhyTemplate.getCluster())) {
            return Result.buildFail(String.format("模板Id[%s]所在集群[%s]不存在", templateId, masterPhyTemplate.getCluster()));
        }

        Result<ClusterPhy> sourceClusterPhyResult = clusterPhyManager.getClusterByName(masterPhyTemplate.getCluster());
        if (null == sourceClusterPhyResult.getData()) {
            return Result.buildFail(String.format("原集群[%s]不存在", masterPhyTemplate.getCluster()));
        }
        //大版本一致就可以，小之间是不应该产生影响的
        if (Boolean.FALSE.equals(ESVersionUtil.compareBigVersionConsistency(sourceClusterPhyResult.getData().getEsVersion(),
                targetClusterPhyResult.getData().getEsVersion()))) {
            return Result.buildFail("主从集群版本必须一致");
        }

        // 3. 执行复制流程
        TemplatePhysicalCopyDTO templatePhysicalCopyDTO = buildTemplatePhysicalCopyDTO(templateId, targetCluster,
            regionId);
        if (null == templatePhysicalCopyDTO) {
            return Result.buildFail(TEMPLATE_NO_EXIST);
        }

        Result<Void> copyTemplateResult = templatePhyManager.copyTemplate(templatePhysicalCopyDTO, operator);
        if (copyTemplateResult.failed()) {
            throw new ESOperateException(copyTemplateResult.getMessage());
        }

        //3. 创建DCDR链路
        Result<Void> result = createPhyDCDR(createDCDRMeta(templateId), operator);

        //4. 记录操作
        if (result.success()) {
            operateRecordService.save(new OperateRecord.Builder()
                .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID)).content(

                    String.format("创建DCDR链路，主集群：%s，从集群：%s", sourceClusterPhyResult.getData().getCluster(),
                        targetClusterPhyResult.getData().getCluster()))
                .userOperation(operator).bizId(templateId).triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                .operationTypeEnum(OperateTypeEnum.TEMPLATE_SERVICE_DCDR_SETTING)

                .build());
        }
        return result;
    }

    /**
     * 删除DCDR
     *
     * @param templateId 模板ID
     * @param operator   操作人
     * @param projectId
     * @return result
     * @throws ESOperateException
     */
    @Override
    public Result<Void> deleteDCDR(Integer templateId, String operator, Integer projectId) throws ESOperateException {
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        Result<Void> checkResult = checkDCDRParam(templateId);

        if (checkResult.failed()) {
            return checkResult;
        }

        return deletePhyDCDR(createDCDRMeta(templateId), operator,projectId);
    }

    /**
     * 创建DCDR链路
     *
     * @param param    参数
     * @param operator 操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createPhyDCDR(TemplatePhysicalDCDRDTO param, String operator) throws ESOperateException {
        Result<Void> checkDCDRResult = checkDCDRParam(param);

        if (checkDCDRResult.failed()) { return Result.buildFrom(checkDCDRResult);}

        for (int i = 0; i < param.getPhysicalIds().size(); ++i) {
            IndexTemplatePhy templatePhysicalPO = indexTemplatePhyService
                .getTemplateById(param.getPhysicalIds().get(i));

            // 判断集群与从集群是否配置了
            if (!clusterPhyManager.ensureDCDRRemoteCluster(templatePhysicalPO.getCluster(),
                param.getReplicaClusters().get(i))) {
                return Result.buildFail("创建remote-cluster失败, 请检查从集群是否正常");
            }

            if (!syncCreateTemplateDCDR(param.getPhysicalIds().get(i), param.getReplicaClusters().get(i), 3)) {
                return Result.buildFail("创建DCDR链路失败, 请检查主从集群是否正常");

            }
        }

        return Result.buildSucc();
    }

    /**
     * 删除DCDR链路
     *
     * @param param     参数
     * @param operator  操作人
     * @param projectId
     * @return result
     */
    @Override
    public Result<Void> deletePhyDCDR(TemplatePhysicalDCDRDTO param, String operator, Integer projectId) throws ESOperateException {
        Result<Void> checkDCDRResult = checkDCDRParam(param);

        if (checkDCDRResult.failed()) {
            return checkDCDRResult;
        }

        for (int i = 0; i < param.getPhysicalIds().size(); ++i) {
            if (syncDeleteTemplateDCDR(param.getPhysicalIds().get(i), param.getReplicaClusters().get(i), 3)) {
                IndexTemplatePhy templatePhysicalPO = indexTemplatePhyService
                    .getTemplateById(param.getPhysicalIds().get(i));

                if (param.getDeleteIndexDcdr() == null || param.getDeleteIndexDcdr()) {
                    if (syncDeleteIndexDCDR(templatePhysicalPO.getCluster(), param.getReplicaClusters().get(i),
                        indexTemplatePhyService.getMatchIndexNames(templatePhysicalPO.getId()), 3)) {
                        LOGGER.info("method=deletePhyDCDR||physicalId={}||msg=delete index DCDR succ",
                            param.getPhysicalIds());
                    } else {
                        LOGGER.warn("method=deletePhyDCDR||physicalId={}||msg=delete index DCDR fail",
                            param.getPhysicalIds());
                    }
                }
                operateRecordService
                    .save(new OperateRecord.Builder()
                            .project(projectService.getProjectBriefByProjectId(projectId))
                            .operationTypeEnum(OperateTypeEnum.TEMPLATE_SERVICE_DCDR_SETTING)
                        .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).bizId(templatePhysicalPO.getLogicId())
                        .userOperation(operator).content("replicaCluster:" + param.getReplicaClusters())

                        .build());
                return Result.buildSucc();
            }
        }

        return Result.buildFail("删除DCDR链路失败");
    }

    @Override
    public Result<WorkTaskVO> batchDCDRSwitchMaster2Slave(DCDRMasterSlaveSwitchDTO dcdrMasterSlaveSwitchDTO,
                                                          String operator, Integer projectId) {
        Result<OpTask> workTaskResult = Result.buildSucc();
        try {
            //1. 批量校验模板DCDR是否可以切换, 仅有一个模板校验不通过结果为不通过。
            List<Long> templateIdList = dcdrMasterSlaveSwitchDTO.getTemplateIds();
            String dcdrType = dcdrMasterSlaveSwitchDTO.getType() == 1 ? "平滑" : "强制";
            Result<Void> batchCheckValidForDCDRSwitchResult = batchCheckValidForDCDRSwitch(templateIdList, operator);
            if (batchCheckValidForDCDRSwitchResult.failed()) {
                return Result.buildFrom(batchCheckValidForDCDRSwitchResult);
            }

            //2.1 设置基础数据
            OpTaskDTO opTaskDTO = new OpTaskDTO();
            String businessKey = getBusinessKey(templateIdList);
            opTaskDTO.setBusinessKey(businessKey);
            opTaskDTO.setTitle(OpTaskTypeEnum.TEMPLATE_DCDR.getMessage());
            opTaskDTO.setTaskType(OpTaskTypeEnum.TEMPLATE_DCDR.getType());
            opTaskDTO.setCreator(operator);
            opTaskDTO.setDeleteFlag(false);
            opTaskDTO.setStatus(OpTaskStatusEnum.RUNNING.getStatus());

            //2.2 设置多个模板DCDR任务信息
            DCDRTasksDetail dcdrTasksDetail = buildDCDRTasksDetail(dcdrMasterSlaveSwitchDTO, templateIdList);

            //2.3 计算状态
            dcdrTasksDetail.calculateProcess();

            //2.4 保存任务
            opTaskDTO.setExpandData(ConvertUtil.obj2Json(dcdrTasksDetail));
            workTaskResult = opTaskManager.addTask(opTaskDTO, projectId);
            if (workTaskResult.failed()) {
                return Result.buildFrom(workTaskResult);
            }

            //2.5 记录操作
            for (DCDRSingleTemplateMasterSlaveSwitchDetail dcdrTask : dcdrTasksDetail
                .getDcdrSingleTemplateMasterSlaveSwitchDetailList()) {
                operateRecordService.save(
                        new OperateRecord.Builder()
                                .operationTypeEnum(OperateTypeEnum.TEMPLATE_SERVICE_DCDR_SETTING)
                                .bizId(dcdrTask.getTemplateId())
                                .userOperation(operator).content(String.format("【%s】%s",
                                        indexTemplateService.getNameByTemplateLogicId(dcdrTask.getTemplateId().intValue()),
                                        dcdrType))
                                .project(projectService.getProjectBriefByProjectId(projectId))
                        
                                    .buildDefaultManualTrigger());
                
            }

        } catch (Exception e) {
            LOGGER.error("method=batchDCDRSwitchMaster2Slave||templateIds={}||msg={}",
                dcdrMasterSlaveSwitchDTO.getTemplateIds(), e.getMessage(), e);
        }

        return Result.buildSucc(ConvertUtil.obj2Obj(workTaskResult.getData(), WorkTaskVO.class));
    }

    @Override
    public Result<Void> cancelDCDRSwitchMasterSlaveByTaskId(Integer taskId, String operator,
                                                            Integer projectId) throws ESOperateException {
        return cancelDCDRSwitchMasterSlaveByTaskIdAndTemplateIds(taskId, null, true, operator, projectId);
    }

    @Override
    public Result<Void> cancelDCDRSwitchMasterSlaveByTaskIdAndTemplateIds(Integer taskId, List<Long> templateIds,
                                                                          boolean fullDeleteFlag, String operator,
                                                                          Integer projectId) throws ESOperateException {
        try {
            Result<OpTask> taskForDcdrSwitchResult = opTaskManager.getById(taskId);
            if (taskForDcdrSwitchResult.failed()) {
                LOGGER.error("method=cancelDcdrSwitchMasterSlaveByTaskIdAndTemplateIds||taskId={}||msg=taskId is empty",
                    taskId);
                return Result.buildFail(String.format(TASK_EMPTY, taskId));
            }

            OpTask taskForDCDRSwitch = taskForDcdrSwitchResult.getData();
            DCDRTasksDetail dcdrTasksDetail = ConvertUtil.str2ObjByJson(taskForDCDRSwitch.getExpandData(),
                DCDRTasksDetail.class);
            List<DCDRSingleTemplateMasterSlaveSwitchDetail> dcdrSingleTemplateMasterSlaveSwitchDetailList = dcdrTasksDetail
                .getDcdrSingleTemplateMasterSlaveSwitchDetailList();
            if (CollectionUtils.isEmpty(dcdrSingleTemplateMasterSlaveSwitchDetailList)) {
                return Result.buildSucc();
            }

            for (DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail : dcdrSingleTemplateMasterSlaveSwitchDetailList) {
                if (fullDeleteFlag) {
                    //取消任务中全部DCDR模板链路
                    if (DCDRStatusEnum.SUCCESS.getCode().equals(switchDetail.getTaskStatus())) {
                        continue;
                    }
                    switchDetail.setTaskStatus(DCDRStatusEnum.CANCELLED.getCode());
                } else {
                    if (!CollectionUtils.isEmpty(templateIds) && templateIds.contains(switchDetail.getTemplateId())) {
                        //取消任务中指定DCDR模板链路
                        switchDetail.setTaskStatus(DCDRStatusEnum.CANCELLED.getCode());
                    }
                }
            }

            saveNewestWorkTaskStatusToDB(taskForDCDRSwitch, dcdrTasksDetail, projectId);
        } catch (Exception e) {
            LOGGER.error(
                "method=cancelDCDRSwitchMasterSlaveByTaskIdAndTemplateIds||taskId={}||templateIds={}||" + "msg={}",
                taskId, templateIds, e.getMessage(), e);
            return Result.buildFail("取消失败, 请联系管理员");
        }

        return Result.buildSucc();
    }

    @Override
    public Result<Void> refreshDCDRChannelState(Integer taskId, Integer templateId, String operator,
                                                Integer projectId) {
        Result<OpTask> taskForDCDRSwitchResult = opTaskManager.getById(taskId);
        if (taskForDCDRSwitchResult.failed()) {
            return Result.buildFrom(taskForDCDRSwitchResult);
        }

        OpTask taskForDCDRSwitch = taskForDCDRSwitchResult.getData();
        if (null == taskForDCDRSwitch) {
            return Result.buildFail("任务不存在");
        }

        DCDRTasksDetail dcdrTasksDetail = ConvertUtil.str2ObjByJson(taskForDCDRSwitch.getExpandData(),
            DCDRTasksDetail.class);

        // 1. 初始化单个模板DCDR任务状态
        initSwitchTaskInfo(templateId, dcdrTasksDetail);

        // 2. 保存初始化状态
        saveNewestWorkTaskStatusToDB(taskForDCDRSwitch, dcdrTasksDetail, projectId);

        return Result.buildSucc();
    }

    @Override
    public Result<Void> asyncRefreshDCDRChannelState(Integer taskId, Integer templateId, String operator) {
        ariusTaskThreadPool.run(() -> {
            // 这里引入锁, 来确保同一时刻只有单个任务在执行。
            ReentrantLock reentrantLock = null;
            try {
                reentrantLock = taskId2ReentrantLockCache.get(taskId, ReentrantLock::new);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if (reentrantLock != null) {
                try {
                    if (reentrantLock.tryLock(TRY_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
                        // 刷新全量状态
                        doRefreshDCDRChannelsState(taskId, ONE_STEP, operator);
                    } else {
                        LOGGER.info(
                            "method=asyncRefreshDCDRChannelState||taskId={}||thread={}||errMsg=failed to fetch the lock",
                            taskId, Thread.currentThread().getName());
                    }
                } catch (Exception e) {
                    LOGGER.error("method=asyncRefreshDCDRChannelState||taskId={}||errMsg={}", taskId, e);
                } finally {
                    reentrantLock.unlock();
                }
            }
        });

        return Result.buildSucc();
    }

    @Override
    public Result<Void> forceSwitchMasterSlave(Integer taskId, Integer templateId, String operator, Integer projectId) {
        if (null == templateId) {
            return Result.buildFail("模板Id不存在");
        }

        try {
            Result<OpTask> taskForDcdrSwitchResult = opTaskManager.getById(taskId);
            if (taskForDcdrSwitchResult.failed()) {
                LOGGER.error("method=forceSwitchMasterSlave||taskId={}||msg=taskId is empty", taskId);
                return Result.buildFail(String.format(TASK_EMPTY, taskId));
            }

            OpTask data = taskForDcdrSwitchResult.getData();
            if (null == data) {
                LOGGER.error("method=forceSwitchMasterSlave||taskId={}||msg=WorkTask is empty", taskId);
                return Result.buildFail("获取DCDR任务详情失败, 请检查任务是否存在");
            }

            DCDRTasksDetail dcdrTasksDetail = ConvertUtil.str2ObjByJson(data.getExpandData(), DCDRTasksDetail.class);
            List<DCDRSingleTemplateMasterSlaveSwitchDetail> switchDetailList = dcdrTasksDetail
                .getDcdrSingleTemplateMasterSlaveSwitchDetailList();
            if (CollectionUtils.isEmpty(switchDetailList)) {
                Result.buildFail("强切失败, 请确认是否有DCDR任务");
            }

            for (DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail : switchDetailList) {
                if (templateId.equals(switchDetail.getTemplateId().intValue())) {
                    switchDetail.setSwitchType(DCDRSwithTypeEnum.FORCE.getCode());
                }
            }

            //1.初始化单个模板DCDR任务状态
            initSwitchTaskInfo(templateId, dcdrTasksDetail);

            //2. 更新任务状态
            saveNewestWorkTaskStatusToDB(data, dcdrTasksDetail, projectId);
        } catch (Exception e) {
            LOGGER.error("method=forceSwitchMasterSlave||taskId={}||templateId={}||msg="
                         + "failed to save newest workTask to db",
                taskId, templateId, e);
            return Result.buildFail("主从强切失败, 请联系管理员");
        }

        return Result.buildSucc();
    }

    @Override
    public Result<DCDRTasksDetailVO> getDCDRMasterSlaveSwitchDetailVO(Integer taskId) {
        Result<OpTask> taskForDcdrSwitchResult = opTaskManager.getById(taskId);
        if (taskForDcdrSwitchResult.failed()) {
            LOGGER.error("method=getDCDRMasterSlaveSwitchDetailVO||taskId={}||msg=taskId is empty", taskId);
            return Result.buildFail(String.format(TASK_EMPTY, taskId));
        }

        OpTask data = taskForDcdrSwitchResult.getData();
        if (null == data) {
            LOGGER.error("method=getDCDRMasterSlaveSwitchDetailVO||taskId={}||msg=OpTask is empty", taskId);
            return Result.buildFail("获取DCDR任务详情失败");
        }
        DCDRTasksDetail dcdrTasksDetail = ConvertUtil.str2ObjByJson(data.getExpandData(), DCDRTasksDetail.class);
        dcdrTasksDetail.calculateProcess();

        //刷新DCDR任务状态
        if (DCDRStatusEnum.RUNNING.getCode().equals(dcdrTasksDetail.getState())) {
            asyncRefreshDCDRChannelState(taskId, null, null);
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(dcdrTasksDetail, DCDRTasksDetailVO.class));
    }

    @Override
    public Result<DCDRSingleTemplateMasterSlaveSwitchDetailVO> getDCDRSingleTemplateMasterSlaveSwitchDetailVO(Integer taskId,
                                                                                                              Long templateId) {
        if (null == templateId) {
            return Result.buildParamIllegal("模板为空");
        }

        Result<OpTask> taskForDCDRSwitchResult = opTaskManager.getById(taskId);
        if (taskForDCDRSwitchResult.failed()) {
            return Result.buildFail(String.format(TASK_EMPTY, taskId));
        }

        OpTask taskForDcdrSwitch = taskForDCDRSwitchResult.getData();
        if (null == taskForDcdrSwitch) {
            return Result.buildFail("任务不存在");
        }

        DCDRTasksDetail dcdrTasksDetail = ConvertUtil.str2ObjByJson(taskForDcdrSwitch.getExpandData(),
            DCDRTasksDetail.class);

        List<DCDRSingleTemplateMasterSlaveSwitchDetail> switchDetailList = dcdrTasksDetail
            .getDcdrSingleTemplateMasterSlaveSwitchDetailList();

        // 检查是否有需要刷新DCDR任务
        switchDetailList.stream()
            .filter(switchDetail -> templateId.equals(switchDetail.getTemplateId())
                                    && DCDRStatusEnum.RUNNING.getCode().equals(switchDetail.getTaskStatus()))
            .forEach(switchDetail -> asyncRefreshDCDRChannelState(taskId, templateId.intValue(), null));

        // 返回详情
        for (DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail : switchDetailList) {
            if (templateId.equals(switchDetail.getTemplateId())) {
                return Result
                    .buildSucc(ConvertUtil.obj2Obj(switchDetail, DCDRSingleTemplateMasterSlaveSwitchDetailVO.class));
            }
        }

        return Result.buildFail();
    }

    /**
     * 创建DCDR模板
     *
     * @param physicalId     物理模板ID
     * @param replicaCluster 从集群名称
     * @return result
     */
    @Override
    public boolean syncCreateTemplateDCDR(Long physicalId, String replicaCluster,
                                          int retryCount) throws ESOperateException {

        IndexTemplatePhy templatePhysical = indexTemplatePhyService.getTemplateById(physicalId);
        if (null == templatePhysical) {
            LOGGER.error("method=syncCreateTemplateDCDR||physicalId={}||replicaCluster={}||errMsg=templatePhysical is null", physicalId, replicaCluster);
            return false;
        }

        LOGGER.info("method=syncCreateTemplateDCDR||physicalId={}||replicaCluster={}", physicalId, replicaCluster);
        return esDCDRService.put("putDCDRForTemplate",templatePhysical.getCluster(),
                String.format(DCDR_TEMPLATE_NAME_FORMAT, templatePhysical.getName(), replicaCluster),
                templatePhysical.getName(), replicaCluster,retryCount );
     
    }

    /**
     * 删除DCDR模板
     *
     *
     * @param physicalId     物理模板ID
     * @param replicaCluster 从集群名称
     * @return result
     */
    @Override
    public boolean syncDeleteTemplateDCDR(Long physicalId, String replicaCluster,
                                          int retryCount) throws ESOperateException {
        IndexTemplatePhy templatePhysical = indexTemplatePhyService.getTemplateById(physicalId);

        LOGGER.info("method=syncDeleteTemplateDCDR||physicalId={}||replicaCluster={}", physicalId, replicaCluster);
    
        return esDCDRService.delete("deleteDCDRForTemplate", templatePhysical.getCluster(),
                String.format(DCDR_TEMPLATE_NAME_FORMAT, templatePhysical.getName(), replicaCluster), retryCount);
    }

    /**
     * 是否存在
     *
     * @param physicalId     物理模板ID
     * @param replicaCluster 从集群名称
     * @return true/false
     */
    @Override
    public boolean syncExistTemplateDCDR(Long physicalId, String replicaCluster) throws ESOperateException {
        IndexTemplatePhy templatePhysical = indexTemplatePhyService.getTemplateById(physicalId);
        if (Objects.isNull(templatePhysical)) {
            throw new ESOperateException("获取不到物理模版:【%s】");
        }
        LOGGER.info("method=syncExistTemplateDCDR||physicalId={}||replicaCluster={}", physicalId, replicaCluster);
        
        return esDCDRService.exist(templatePhysical.getCluster(),String.format(DCDR_TEMPLATE_NAME_FORMAT, templatePhysical.getName(), replicaCluster));
    }

    /**
     * 删除索引DCDR链路
     *
     * @param cluster                  集群
     * @param replicaCluster           从集群
     * @param indices 索引列表
     * @param retryCount               重试次数
     * @return result
     */
    @Override
    public boolean syncDeleteIndexDCDR(String cluster, String replicaCluster, List<String> indices,
                                       int retryCount) throws ESOperateException {
        return esDCDRService.delete("syncDeleteIndexDCDR",cluster,replicaCluster,retryCount);
    }

    /**
     * 修改索引配置
     *
     * @param cluster      集群
     * @param indices      索引
     * @param replicaIndex DCDR配置
     * @param retryCount   重试次数
     * @return result
     */
    @Override
    public boolean syncDCDRSetting(String cluster, List<String> indices, boolean replicaIndex,
                                   int retryCount) throws ESOperateException {

        BatchProcessor.BatchProcessResult<String, Boolean> result = new BatchProcessor<String, Boolean>()
            .batchList(indices).batchSize(30).processor(items -> {
                try {
                    return esIndexService.syncPutIndexSetting(cluster, items, DCDR_INDEX_SETTING,
                        String.valueOf(replicaIndex), "false", retryCount);
                } catch (ESOperateException e) {
                    return false;
                }
            }).succChecker(succ -> succ).process();

        return result.isSucc();
    }

    /**
     * 判断集群是否支持DCDR
     *
     * @param phyCluster 集群名称
     * @return
     */
    @Override
    public boolean clusterSupport(String phyCluster) {
        //直接获取插件信息
        return esClusterNodeService.existDCDRAndPipelineModule(phyCluster).v1;
    }

    @Override
    public Tuple<Long, Long> getMasterAndSlaveTemplateCheckPoint(Integer templateId) {
        //1.初始化信息
        Tuple<Long, Long> masterAndSlaveCheckPointTuple = new Tuple<>();
        masterAndSlaveCheckPointTuple.setV1(0L);
        masterAndSlaveCheckPointTuple.setV2(0L);

        IndexTemplateWithPhyTemplates logicTemplateWithPhysicals = indexTemplateService
            .getLogicTemplateWithPhysicalsById(templateId);
        IndexTemplatePhy masterPhyTemplate = logicTemplateWithPhysicals.getMasterPhyTemplate();
        IndexTemplatePhy slavePhyTemplate = logicTemplateWithPhysicals.getSlavePhyTemplate();
        if (null == masterPhyTemplate) {
            LOGGER.warn(
                "class=TemplateDCDRManagerImpl||method=setCheckPointDiff||templateId={}||msg=masterPhyTemplate is empty",
                templateId);
            return masterAndSlaveCheckPointTuple;
        }

        if (null == slavePhyTemplate) {
            LOGGER.warn(
                "class=TemplateDCDRManagerImpl||method=setCheckPointDiff||templateId={}||msg=slavePhyTemplate is empty",
                templateId);
            return masterAndSlaveCheckPointTuple;
        }

        //2. 根据索引主从位点信息构建模板主从位点信息
        List<String> indexNames = indexTemplatePhyService.getMatchIndexNames(masterPhyTemplate.getId());

        Map<String, IndexNodes> indexStatForMasterMap = esIndexService
            .syncBatchGetIndices(masterPhyTemplate.getCluster(), indexNames);
        Map<String, IndexNodes> indexStatForSlaveMap = esIndexService.syncBatchGetIndices(slavePhyTemplate.getCluster(),
            indexNames);

        long masterCheckPointTotal = 0;
        long slaveCheckPointTotal = 0;
        for (String index : indexNames) {
            IndexNodes statForMaster = indexStatForMasterMap.get(index);
            IndexNodes statForSlave = indexStatForSlaveMap.get(index);
            AtomicLong totalCheckpointForMaster = esIndexService.syncGetTotalCheckpoint(index, statForMaster, null);
            AtomicLong totalCheckpointForSlave = esIndexService.syncGetTotalCheckpoint(index, statForSlave, null);
            masterCheckPointTotal += totalCheckpointForMaster.get();
            slaveCheckPointTotal += totalCheckpointForSlave.get();
        }

        masterAndSlaveCheckPointTuple.setV1(masterCheckPointTotal);
        masterAndSlaveCheckPointTuple.setV2(slaveCheckPointTotal);
        return masterAndSlaveCheckPointTuple;
    }

    @Override
    public Result<TemplateDCDRInfoVO> getTemplateDCDRInfoVO(Integer templateId) {
        TemplateDCDRInfoVO templateDCDRInfoVO = new TemplateDCDRInfoVO();
        IndexTemplateWithPhyTemplates logicTemplateWithPhysicals = indexTemplateService
            .getLogicTemplateWithPhysicalsById(templateId);
        IndexTemplatePhy slavePhyTemplate = Optional.ofNullable(logicTemplateWithPhysicals)
                .map(IndexTemplateWithPhyTemplates::getSlavePhyTemplate)
                .orElse(null);
        IndexTemplatePhy masterPhyTemplate = Optional.ofNullable(logicTemplateWithPhysicals)
                .map(IndexTemplateWithPhyTemplates::getMasterPhyTemplate)
                .orElse(null);
        Optional.ofNullable(masterPhyTemplate)
                .map(IndexTemplatePhy::getCluster).ifPresent(templateDCDRInfoVO::setMasterClusterName);
        Optional.ofNullable(slavePhyTemplate)
                .map(IndexTemplatePhy::getCluster).ifPresent(templateDCDRInfoVO::setSlaveClusterName);
        if (null == masterPhyTemplate) {
            return Result.buildFail(TEMPLATE_NO_EXIST);
        }
        // 1. 判断模板是否存在DCDR
        if (null == slavePhyTemplate) {
            templateDCDRInfoVO.setDcdrFlag(false);
            return Result.buildSuccWithTips(templateDCDRInfoVO, "模板未开启DCDR链路");
        } else {
            try {
        
                templateDCDRInfoVO.setDcdrFlag(
                        syncExistTemplateDCDR(masterPhyTemplate.getId(), slavePhyTemplate.getCluster()));
               
            } catch (Exception e) {
                LOGGER.error("method=getTemplateDCDRInfoVO||templateId={}", templateId,e);
               return Result.buildFailWithMsg(templateDCDRInfoVO,"主集群异常，获取主从位点差失败");
            }
        }

        if (Boolean.FALSE.equals(templateDCDRInfoVO.getDcdrFlag())) {
            return Result.buildSuccWithTips(templateDCDRInfoVO, "模板未开启DCDR链路");
        }

        // 2. 获取主从模板checkpoint信息
        Tuple<Long, Long> masterAndSlaveTemplateCheckPointTuple = new Tuple<>();
        try {
            masterAndSlaveTemplateCheckPointTuple = getMasterAndSlaveTemplateCheckPoint(templateId);
        } catch (Exception e) {
            LOGGER.error(
                "class=TemplateDCDRManagerImpl||method=getTemplateDCDRInfoVO||templateId={}||msg=masterAndSlaveTemplateCheckPointTuple is empty",
                templateId, e);
            return Result.buildFailWithMsg(templateDCDRInfoVO,"从集群异常，获取主从位点差失败");
        }
        
        templateDCDRInfoVO.setMasterTemplateCheckPoint(masterAndSlaveTemplateCheckPointTuple.getV1());

        templateDCDRInfoVO.setSlaveTemplateCheckPoint(masterAndSlaveTemplateCheckPointTuple.getV2());

        long checkPointDiff =
                Math
            .abs(masterAndSlaveTemplateCheckPointTuple.getV1() - masterAndSlaveTemplateCheckPointTuple.getV2());
        templateDCDRInfoVO.setTemplateCheckPointDiff(checkPointDiff);
        return Result.buildSucc(templateDCDRInfoVO);
    }

    /**************************************** private method ****************************************************/
    private Result<Void> checkDCDRParam(Integer logicId) {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);
        if (templateLogicWithPhysical == null) {
            return Result.buildNotExist(TEMPLATE_NO_EXIST);
        }

        if (templateLogicWithPhysical.getMasterPhyTemplate() == null) {
            return Result.buildParamIllegal("模板没有部署master");
        }

        if (templateLogicWithPhysical.getSlavePhyTemplate() == null) {
            return Result.buildParamIllegal("模板没有部署slave");
        }

        if (templateLogicWithPhysical.getPhysicals().size() != MAX_PHY_TEMPLATE_NUM) {
            return Result.buildParamIllegal("DCDR仅支持一主一从部署的模板");
        }

        return Result.buildSucc();
    }

    private Result<Void> checkDCDRParam(TemplatePhysicalDCDRDTO param) {
        if (param == null) {
            return Result.buildParamIllegal("DCDR参数不存在");
        }

        if (CollectionUtils.isEmpty(param.getPhysicalIds())) {
            return Result.buildParamIllegal("模板ID必须存在");
        }

        if (CollectionUtils.isEmpty(param.getReplicaClusters())) {
            return Result.buildParamIllegal("从集群必须存在");
        }

        for (int i = 0; i < param.getPhysicalIds().size(); ++i) {
            IndexTemplatePhy templatePhysical = indexTemplatePhyService.getTemplateById(param.getPhysicalIds().get(i));
            if (templatePhysical == null) {
                return Result.buildNotExist("物理模板不存在");
            }

            if (!clusterPhyManager.isClusterExists(param.getReplicaClusters().get(i))) {
                return Result.buildNotExist("从集群不存在");
            }

            if (!clusterSupport(templatePhysical.getCluster())) {
                return Result.buildParamIllegal("模板所在集群不支持DCDR");
            }

            if (!clusterSupport(param.getReplicaClusters().get(i))) {
                return Result.buildParamIllegal("所选的从集群不支持DCDR");
            }

            if (templatePhysical.getCluster().equals(param.getReplicaClusters().get(i))) {
                return Result.buildParamIllegal("所选的从集群与主集群不能一样");
            }
        }

        return Result.buildSucc();
    }

    /**
     * 根据逻辑索引ID创建物理模板DCDR
     *
     * @param templateId
     * @return {@link  TemplatePhysicalDCDRDTO}
     */
    private TemplatePhysicalDCDRDTO createDCDRMeta(Integer templateId) {
        TemplatePhysicalDCDRDTO dcdrMeta = new TemplatePhysicalDCDRDTO();

        dcdrMeta.setPhysicalIds(new ArrayList<>());
        dcdrMeta.setReplicaClusters(new ArrayList<>());

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(templateId);

        List<IndexTemplatePhy> masterPhysicals = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
        for (IndexTemplatePhy indexTemplatePhysicalInfo : masterPhysicals) {
            IndexTemplatePhy slave = null;
            if (StringUtils.isNotBlank(indexTemplatePhysicalInfo.getGroupId())) {
                slave = templateLogicWithPhysical.fetchMasterSlave(indexTemplatePhysicalInfo.getGroupId());
            }

            if (null == slave) {
                slave = templateLogicWithPhysical.getSlavePhyTemplate();
            }

            if (slave != null) {
                dcdrMeta.getPhysicalIds().add(indexTemplatePhysicalInfo.getId());
                dcdrMeta.getReplicaClusters().add(slave.getCluster());
            }
        }

        return dcdrMeta;
    }

    private TemplatePhysicalDCDRDTO buildCreateDCDRParam(IndexTemplatePhy masterTemplate,
                                                         IndexTemplatePhy slaveTemplate) {
        TemplatePhysicalDCDRDTO dcdrdto = new TemplatePhysicalDCDRDTO();
        dcdrdto.setPhysicalIds(Arrays.asList(masterTemplate.getId()));
        dcdrdto.setReplicaClusters(Arrays.asList(slaveTemplate.getCluster()));
        return dcdrdto;
    }

    private Result<Void> changeDCDRConfig(String cluster, List<String> indices,
                                          boolean replicaIndex) throws ESOperateException {

        // 修改配置
        if (!syncDCDRSetting(cluster, indices, replicaIndex, TRY_TIMES_THREE)) {
            return Result.buildFail("修改" + cluster + "索引dcdr配置失败");
        }

        // reopen索引
        if (!esIndexService.reOpenIndex(cluster, indices, TRY_TIMES_THREE)) {
            return Result.buildFail("reOpen " + cluster + "索引失败");
        }

        return Result.buildSucc();
    }

    private Result<Void> deleteSrcDCDR(IndexTemplatePhy masterTemplate, IndexTemplatePhy slaveTemplate,

                                       List<String> matchNoVersionIndexNames,
                                       String operator) throws ESOperateException {

        TemplatePhysicalDCDRDTO dcdrDTO = new TemplatePhysicalDCDRDTO();

        dcdrDTO.setPhysicalIds(Arrays.asList(masterTemplate.getId()));
        dcdrDTO.setReplicaClusters(Arrays.asList(slaveTemplate.getCluster()));
        dcdrDTO.setDeleteIndexDcdr(false);

        Result<Void> delTemDCDRResult = deletePhyDCDR(dcdrDTO, operator, AuthConstant.SUPER_PROJECT_ID);
        boolean delIndexDCDRResult = syncDeleteIndexDCDR(masterTemplate.getCluster(), slaveTemplate.getCluster(),
            matchNoVersionIndexNames, 3);

        return Result.build(delTemDCDRResult.success() && delIndexDCDRResult);
    }

    /**
     * 执行
     *
     * @param workTaskId                     任务id
     * @param switchDetail
     * @param expectMasterPhysicalId         期望的模板Id
     * @param step                           切换起始步骤
     * @param masterTemplate                 主模板元数据信息
     * @param slaveTemplate                  从模板元数据信息
     * @param operator
     * @return
     */
    private Result<List<String>> executeDCDRForForce(Integer workTaskId,
                                                     DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail,
                                                     Long expectMasterPhysicalId, int step,
                                                     IndexTemplatePhy masterTemplate, IndexTemplatePhy slaveTemplate,
                                                     String operator) {
        List<String> matchIndexNames = indexTemplatePhyService.getMatchIndexNames(slaveTemplate.getId());
        int templateId = switchDetail.getTemplateId().intValue();
        try {
            if (DCDR_SWITCH_STEP_1 == step) {
                // 修改DCDR索引配置 index.dcdr.replica_index = true/false
                // 然后还需要reopen索引，配置才能生效
                Result<Void> setSettingResult = Result.buildSucc();
                if (hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    setSettingResult = Result.buildFail(TASK_IS_CANCEL);
                } else {
                    Result<Void> changeSlaveDCDRConfig = changeDCDRConfig(slaveTemplate.getCluster(), matchIndexNames,
                            false);
                    if (changeSlaveDCDRConfig.failed()) {
                        setSettingResult = Result.buildFail(changeSlaveDCDRConfig.getMessage());
                    }
                }

                Result<List<String>> step1Result = buildStepMsg(DCDRSwithTypeEnum.FORCE.getCode(), setSettingResult,
                    templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_1, operator,
                    switchDetail.getTaskProgressList());
                if (step1Result.failed()) {
                    return step1Result;
                }
                step++;
                sleep(1000L);
            }

            if (DCDR_SWITCH_STEP_2 == step) {
                // 主从角色切换
                Result<Void> switchMasterSlave;
                if (hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    switchMasterSlave = Result.buildFail(TASK_IS_CANCEL);
                } else {
                    if (hasFinishSwitchMasterSlave(switchDetail)) {
                        switchMasterSlave = Result.buildSucc();
                    } else {
                        switchMasterSlave = templatePhyManager.switchMasterSlave(masterTemplate.getLogicId(),
                            slaveTemplate.getId(), AriusUser.SYSTEM.getDesc());
                    }
                }

                Result<List<String>> step2Result = buildStepMsg(DCDRSwithTypeEnum.FORCE.getCode(), switchMasterSlave,
                    templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_2, operator,
                    switchDetail.getTaskProgressList());
                if (step2Result.failed()) {
                    return step2Result;
                }
            }

        } catch (Exception e) {
            LOGGER.warn("method=executeDCDRForForce||templateId={}||errMsg={}", templateId, e.getMessage(), e);
            return buildStepMsg(DCDRSwithTypeEnum.FORCE.getCode(), Result.buildFail(e.getMessage()), templateId,
                expectMasterPhysicalId, step, operator, switchDetail.getTaskProgressList());
        }

        return Result.buildSucc(switchDetail.getTaskProgressList());
    }

    /**
     * todo：alibaba规范 方法总行数超过80行
     * 执行
     * @param switchDetail
     * @param expectMasterPhysicalId         期望的模板Id
     * @param step                           切换起始步骤
     * @param masterTemplate                 主模板元数据信息
     * @param slaveTemplate                  从模板元数据信息
     * @param operator
     * @return
     */
    private Result<List<String>> executeDCDRForSmooth(Integer workTaskId,
                                                      DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail,
                                                      Long expectMasterPhysicalId, int step,
                                                      IndexTemplatePhy masterTemplate, IndexTemplatePhy slaveTemplate,
                                                      String operator) {
        List<String> matchIndexNames = indexTemplatePhyService.getMatchIndexNames(slaveTemplate.getId());

        int templateId = switchDetail.getTemplateId().intValue();

        try {
            /**
             * 注意这里的if不能使用else if代替，这里的代码需要顺序执行下去
             */
            if (DCDR_SWITCH_STEP_1 == step) {
                Result<Void> stopMasterIndexResult;
                if (hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    stopMasterIndexResult = Result.buildFail(TASK_IS_CANCEL);
                } else {
                    // 停止索引写入
                    boolean suc = esIndexService.syncBatchBlockIndexWrite(masterTemplate.getCluster(), matchIndexNames,
                        true, 3);
                    stopMasterIndexResult = Result.build(suc);
                }

                Result<List<String>> step1Result = buildStepMsg(DCDRSwithTypeEnum.SMOOTH.getCode(),
                    stopMasterIndexResult, templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_1, operator,
                    switchDetail.getTaskProgressList());
                if (step1Result.failed()) {
                    return step1Result;
                }
                step++;
                sleep(1000L);
            }

            if (DCDR_SWITCH_STEP_2 == step) {
                // 确保主从数据同步完成
                Result<Void> checkDataResult = Result.buildSucc();
                if (hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    checkDataResult = Result.buildFail(TASK_IS_CANCEL);
                } else {
                    if (!esIndexService.ensureDateSame(masterTemplate.getCluster(), slaveTemplate.getCluster(),
                        matchIndexNames)) {
                        checkDataResult = Result.buildFail("校验索引数据不一致!");
                        // 恢复实时数据写入

                        Result<Void> sttartMasterIndexResult = Result.build(esIndexService
                            .syncBatchBlockIndexWrite(masterTemplate.getCluster(), matchIndexNames, false, 3));
                        if (sttartMasterIndexResult.failed()) {
                            checkDataResult
                                .setMessage(checkDataResult.getMessage() + "|" + sttartMasterIndexResult.getMessage());
                        }
                    }
                }

                Result<List<String>> step2Result = buildStepMsg(DCDRSwithTypeEnum.SMOOTH.getCode(), checkDataResult,
                    templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_2, operator,
                    switchDetail.getTaskProgressList());
                if (step2Result.failed()) {
                    return step2Result;
                }
                step++;
                sleep(1000L);
            }

            if (DCDR_SWITCH_STEP_3 == step) {
                Result<Void> deleteSrcDCDRResult;
                // 删除DCDR链路（模板和索引）
                if (hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    deleteSrcDCDRResult = Result.buildFail(TASK_IS_CANCEL);
                } else {
                    deleteSrcDCDRResult = deleteSrcDCDR(masterTemplate, slaveTemplate, matchIndexNames,
                        AriusUser.SYSTEM.getDesc());
                }

                Result<List<String>> step3Result = buildStepMsg(DCDRSwithTypeEnum.SMOOTH.getCode(), deleteSrcDCDRResult,
                    templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_3, operator,
                    switchDetail.getTaskProgressList());
                if (step3Result.failed()) {
                    return step3Result;
                }
                step++;
                sleep(1000L);
            }

            if (DCDR_SWITCH_STEP_4 == step) {
                Result<Void> copyResult = Result.buildSucc();

                if (hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    copyResult = Result.buildFail(TASK_IS_CANCEL);
                } else {
                    // 拷贝主模板到从模板
                    if (!esTemplateService.syncCopyMappingAndAlias(masterTemplate.getCluster(),
                        masterTemplate.getName(), slaveTemplate.getCluster(), slaveTemplate.getName(), 3)) {
                        copyResult = Result.buildFail("拷贝模板失败");
                    }
                }

                Result<List<String>> step4Result = buildStepMsg(DCDRSwithTypeEnum.SMOOTH.getCode(), copyResult,
                    templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_4, operator,
                    switchDetail.getTaskProgressList());
                if (step4Result.failed()) {
                    return step4Result;
                }
                step++;
                sleep(1000L);
            }

            if (DCDR_SWITCH_STEP_5 == step) {
                // 修改DCDR索引配置 index.dcdr.replica_index = true/false
                // 然后还需要reopen索引，配置才能生效
                Result<Void> setSettingResult = Result.buildSucc();
                if (hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    setSettingResult = Result.buildFail(TASK_IS_CANCEL);
                } else {
                    Result<Void> changeMasterDCDRConfig = changeDCDRConfig(masterTemplate.getCluster(), matchIndexNames,
                        true);
                    Result<Void> changeSlaveDCDRConfig = changeDCDRConfig(slaveTemplate.getCluster(), matchIndexNames,
                        false);

                    if (changeMasterDCDRConfig.failed() || changeSlaveDCDRConfig.failed()) {
                        setSettingResult = Result
                            .buildFail(changeMasterDCDRConfig.getMessage() + "|" + changeSlaveDCDRConfig.getMessage());
                    }
                }

                Result<List<String>> step5Result = buildStepMsg(DCDRSwithTypeEnum.SMOOTH.getCode(), setSettingResult,
                    templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_5, operator,
                    switchDetail.getTaskProgressList());
                if (step5Result.failed()) {
                    return step5Result;
                }
                step++;
                sleep(1000L);
            }

            if (DCDR_SWITCH_STEP_6 == step) {
                // 停止索引写入
                Result<Void> stopSlaveIndexResult;
                if (hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    stopSlaveIndexResult = Result.buildFail(TASK_IS_CANCEL);
                } else {
                    stopSlaveIndexResult = Result.build(
                        esIndexService.syncBatchBlockIndexWrite(masterTemplate.getCluster(), matchIndexNames, true, 3));
                }
                Result<List<String>> step6Result = buildStepMsg(DCDRSwithTypeEnum.SMOOTH.getCode(),
                    stopSlaveIndexResult, templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_6, operator,
                    switchDetail.getTaskProgressList());
                if (step6Result.failed()) {
                    return step6Result;
                }
                step++;
                sleep(1000L);
            }

            if (DCDR_SWITCH_STEP_7 == step) {
                Result<Void> createDCDRResult;
                // 创建新的主从链路
                if (hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    createDCDRResult = Result.buildFail(TASK_IS_CANCEL);
                } else {
                    createDCDRResult = createPhyDCDR(buildCreateDCDRParam(slaveTemplate, masterTemplate),
                        AriusUser.SYSTEM.getDesc());
                }
                Result<List<String>> step7Result = buildStepMsg(DCDRSwithTypeEnum.SMOOTH.getCode(), createDCDRResult,
                    templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_7, operator,
                    switchDetail.getTaskProgressList());
                if (step7Result.failed()) {
                    return step7Result;
                }
                step++;
                sleep(1000L);
            }

            if (DCDR_SWITCH_STEP_8 == step) {
                // 恢复实时写入
                Result<Void> startIndexResult = Result.buildSucc();
                if (hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    startIndexResult = Result.buildFail(TASK_IS_CANCEL);
                } else {
                    Result<Void> startMasterIndexResult = Result.build(esIndexService
                        .syncBatchBlockIndexWrite(masterTemplate.getCluster(), matchIndexNames, false, 3));

                    Result<Void> startSlaveIndexResult = Result.build(
                        esIndexService.syncBatchBlockIndexWrite(slaveTemplate.getCluster(), matchIndexNames, false, 3));

                    if (startMasterIndexResult.failed() || startSlaveIndexResult.failed()) {
                        startIndexResult = Result
                            .buildFail(startMasterIndexResult.getMessage() + "|" + startSlaveIndexResult.getMessage());
                    }
                }

                Result<List<String>> step8Result = buildStepMsg(DCDRSwithTypeEnum.SMOOTH.getCode(), startIndexResult,
                    templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_8, operator,
                    switchDetail.getTaskProgressList());
                if (step8Result.failed()) {
                    return step8Result;
                }
                step++;
                sleep(1000L);
            }

            if (DCDR_SWITCH_STEP_9 == step) {
                // 主从角色切换
                Result<Void> switchMasterSlave;
                if (hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    switchMasterSlave = Result.buildFail(TASK_IS_CANCEL);
                } else {
                    if (hasFinishSwitchMasterSlave(switchDetail)) {
                        switchMasterSlave = Result.buildSucc();
                    } else {
                        switchMasterSlave = templatePhyManager.switchMasterSlave(masterTemplate.getLogicId(),
                            slaveTemplate.getId(), AriusUser.SYSTEM.getDesc());
                    }
                }

                Result<List<String>> step9Result = buildStepMsg(DCDRSwithTypeEnum.SMOOTH.getCode(), switchMasterSlave,
                    templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_9, operator,
                    switchDetail.getTaskProgressList());
                if (step9Result.failed()) {
                    return step9Result;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("method=executeDCDRForSmooth||templateId={}||errMsg={}", templateId, e.getMessage(), e);
            return buildStepMsg(DCDRSwithTypeEnum.SMOOTH.getCode(), Result.buildFail(e.getMessage()), templateId,
                expectMasterPhysicalId, step, operator, switchDetail.getTaskProgressList());
        }
        return Result.buildSucc(switchDetail.getTaskProgressList());
    }

    /**
     * 是否已经成功切换
     * @param switchDetail
     * @return
     */
    private boolean hasFinishSwitchMasterSlave(DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail) {
        IndexTemplateWithPhyTemplates logicTemplateWithPhysicals = indexTemplateService
            .getLogicTemplateWithPhysicalsById(switchDetail.getTemplateId().intValue());
        IndexTemplatePhy masterTemplate = logicTemplateWithPhysicals.getMasterPhyTemplate();
        IndexTemplatePhy slaveTemplate = logicTemplateWithPhysicals.getSlavePhyTemplate();

        String masterTemplateClusterFromDB = masterTemplate.getCluster();
        String slaveTemplateClusterFromDB = slaveTemplate.getCluster();

        String masterCluster = switchDetail.getMasterCluster();
        String slaveCluster = switchDetail.getSlaveCluster();
        return masterCluster.equals(masterTemplateClusterFromDB) && slaveCluster.equals(slaveTemplateClusterFromDB);
    }

    private void sleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 单个DCDR子任务是否取消
     * @param workTaskId
     * @param templateId
     * @return
     */
    private boolean hasCancelSubTask(Integer workTaskId, Long templateId) {
        Result<OpTask> workTaskResult = opTaskManager.getById(workTaskId);
        if (null == workTaskResult || null == workTaskResult.getData()) {
            LOGGER.error("method=submitTask||workTaskId={}||msg= workTaskId is empty", workTaskId);
            return false;
        }
        OpTask opTask = workTaskResult.getData();

        DCDRTasksDetail dcdrTasksDetail = JSON.parseObject(opTask.getExpandData(), DCDRTasksDetail.class);
        List<DCDRSingleTemplateMasterSlaveSwitchDetail> switchDetailList = dcdrTasksDetail
            .getDcdrSingleTemplateMasterSlaveSwitchDetailList();
        if (CollectionUtils.isEmpty(switchDetailList)) {
            return false;
        }

        for (DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail : switchDetailList) {
            if (templateId.equals(switchDetail.getTemplateId())
                && DCDRStatusEnum.CANCELLED.getCode().equals(switchDetail.getTaskStatus())) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    private List<String> buildInitTaskProgressInfo(String[] dcdrSwitchStepArrSmooth) {
        List<String> stepMsgList = Lists.newArrayList();
        for (String stepMsgFormat : dcdrSwitchStepArrSmooth) {
            stepMsgList.add(String.format(stepMsgFormat, SEPARATOR + DCDR_SWITCH_TODO));
        }
        return stepMsgList;
    }

    private Result<List<String>> buildStepMsg(Integer swithType, Result<Void> result, Integer logicId,
                                              Long expectMasterPhysicalId, int localStep, String operator,
                                              List<String> stepMsgList) {
        if (DCDRSwithTypeEnum.FORCE.getCode().equals(swithType)) {
            int localStepTemp = DCDR_SWITCH_STEP_5;

            if (localStep == DCDR_SWITCH_STEP_2) {
                localStepTemp = DCDR_SWITCH_STEP_9;
            }

            if (result.failed()) {
                buildSwitchStepMsgForFailedList(stepMsgList, result, localStep, localStepTemp);
                return Result.buildFail(stepMsgList);
            }
            buildSwitchStepMsgForSuccessList(stepMsgList, localStep, localStepTemp);
        }

        if (DCDRSwithTypeEnum.SMOOTH.getCode().equals(swithType)) {
            if (result.failed()) {
                buildSwitchStepMsgForFailedList(stepMsgList, result, localStep, localStep);
                return Result.buildFail(stepMsgList);
            }
            buildSwitchStepMsgForSuccessList(stepMsgList, localStep, localStep);
        }

        LOGGER.info(
            "method=DCDRSwitchMasterSlave||logicId={}||operator={}||expectMasterPhysicalId={}||msg=step {} succ",
            logicId, operator, expectMasterPhysicalId, localStep);
        return Result.buildSucc(stepMsgList);
    }

    private void buildSwitchStepMsgForFailedList(List<String> stepMsgList, Result<Void> result, int localStep,
                                                 int localStepTemp) {
        stepMsgList.set(localStep - 1,
            String.format(TemplateDCDRStepEnum.valueOfStep(localStepTemp).getValue(), SEPARATOR + DCDR_SWITCH_FAIL)
                                       + SEPARATOR + result.getMessage());
    }

    private void buildSwitchStepMsgForSuccessList(List<String> stepMsgList, int localStep, int localStepTemp) {
        stepMsgList.set(localStep - 1, String.format(TemplateDCDRStepEnum.valueOfStep(localStepTemp).getValue(),
            SEPARATOR + DCDR_SWITCH_DONE + SEPARATOR + SUCCESS_INFO));
    }

    private Result<Void> processDcdrTask(Integer logicId, Result<Void> dcdrResult,
                                         int step) throws NotFindSubclassException {
        Result<OpTask> result = opTaskManager.getLatestTask(String.valueOf(logicId),
            OpTaskTypeEnum.TEMPLATE_DCDR.getType());
        if (result.failed()) {
            return Result.buildFrom(result);
        }
        OpTaskProcessDTO processDTO = new OpTaskProcessDTO();
        processDTO.setStatus(
            dcdrResult.success() ? OpTaskStatusEnum.SUCCESS.getStatus() : OpTaskStatusEnum.FAILED.getStatus());
        processDTO.setTaskId(result.getData().getId());
        processDTO.setTaskProgress(step);

        if (dcdrResult.failed()) {
            DCDRTaskDetail detail = new DCDRTaskDetail();
            detail.setComment(result.getMessage());
            processDTO.setExpandData(JSON.toJSONString(detail));
        }
        return opTaskManager.processTask(processDTO);
    }

    private TemplatePhysicalCopyDTO buildTemplatePhysicalCopyDTO(Integer templateId, String targetCluster,
                                                                 Integer regionId) {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(templateId);

        TemplatePhysicalCopyDTO templatePhysicalCopyDTO = new TemplatePhysicalCopyDTO();
        IndexTemplatePhy masterPhyTemplate = templateLogicWithPhysical.getMasterPhyTemplate();
        if (null == masterPhyTemplate) {
            return null;
        }

        templatePhysicalCopyDTO.setCluster(targetCluster);
        templatePhysicalCopyDTO.setPhysicalId(masterPhyTemplate.getId());
        templatePhysicalCopyDTO.setShard(masterPhyTemplate.getShard());
        templatePhysicalCopyDTO.setRegionId(regionId);
        return templatePhysicalCopyDTO;
    }

    /**
     * 构建业务key
     * @param templateIdList
     * @return
     */
    @NotNull
    private String getBusinessKey(List<Long> templateIdList) {
        List<String> templateIdStrList = templateIdList.stream().map(String::valueOf).collect(Collectors.toList());
        return ListUtils.strList2String(templateIdStrList);
    }

    private Result<Void> checkValidForDCDRSwitch(Integer logicId, Long expectMasterPhysicalId, int step,
                                                 String operator) {
        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService.getTemplateByLogicId(logicId);
        if (CollectionUtils.isEmpty(templatePhysicals)) {
            return Result.buildNotExist("逻辑模板有没有部署物理模板");
        }

        if (templatePhysicals.size() > MAX_PHY_TEMPLATE_NUM) {
            return Result.buildParamIllegal("DCDR主从切换只支持2副本部署");
        }

        IndexTemplatePhy masterTemplate = null;
        IndexTemplatePhy slaveTemplate = null;
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            if (MASTER.getCode().equals(templatePhysical.getRole())) {
                masterTemplate = templatePhysical;
            }
            if (SLAVE.getCode().equals(templatePhysical.getRole())) {
                slaveTemplate = templatePhysical;
            }
        }

        if (masterTemplate == null || slaveTemplate == null) {
            return Result.buildParamIllegal("模板主从部署角色异常");
        }

        if (masterTemplate.getId().equals(expectMasterPhysicalId)) {
            return Result.buildSucc();
        }

        if (step > TemplateDCDRStepEnum.STEP_9.getStep() || step < TemplateDCDRStepEnum.STEP_1.getStep()) {
            step = TemplateDCDRStepEnum.STEP_1.getStep();
        }
     
       

        return Result.buildSucc();
    }

    private Result<Void> batchCheckValidForDCDRSwitch(List<Long> templateIdList, String operator) {
        if (CollectionUtils.isEmpty(templateIdList)) {
            return Result.buildParamIllegal("模板id为空");
        }
        for (Long templateId : templateIdList) {
            IndexTemplateWithPhyTemplates logicTemplateWithPhysicals = indexTemplateService
                .getLogicTemplateWithPhysicalsById(templateId.intValue());

            IndexTemplatePhy slaveTemplate = logicTemplateWithPhysicals.getSlavePhyTemplate();
            if (null == slaveTemplate) {
                return Result.buildFail(String.format("模板Id[%s]不存在从模板, 无法进行DCDR主从切换", templateId));
            }

            Result<Void> checkValidForDCDRSwitchResult = checkValidForDCDRSwitch(templateId.intValue(),
                slaveTemplate.getId(), 1, operator);
            if (checkValidForDCDRSwitchResult.failed()) {
                return checkValidForDCDRSwitchResult;
            }
        }

        return Result.buildSucc();
    }

    private DCDRTasksDetail buildDCDRTasksDetail(DCDRMasterSlaveSwitchDTO dcdrMasterSlaveSwitchDTO,
                                                 List<Long> templateIdList) {
        DCDRTasksDetail dcdrTasksDetail = new DCDRTasksDetail();
        List<DCDRSingleTemplateMasterSlaveSwitchDetail> dcdrSingleTemplateMasterSlaveSwitchDetailList = new ArrayList<>();
        //1. 设置DCDR详情信息
        for (Long templateId : templateIdList) {
            DCDRSingleTemplateMasterSlaveSwitchDetail singleSwitchDetail = new DCDRSingleTemplateMasterSlaveSwitchDetail();

            IndexTemplate logicTemplate = indexTemplateService.getLogicTemplateById(templateId.intValue());
            singleSwitchDetail.editTaskTitle(logicTemplate.getName());

            //1.1 设置切换类型 强切、平滑
            singleSwitchDetail.setSwitchType(dcdrMasterSlaveSwitchDTO.getType());

            //1.2 构建DCDR主从切换基础信息
            singleSwitchDetail.setTemplateId(templateId);

            IndexTemplateWithPhyTemplates logicTemplateWithPhysicals = indexTemplateService
                .getLogicTemplateWithPhysicalsById(templateId.intValue());
            IndexTemplatePhy masterTemplate = logicTemplateWithPhysicals.getMasterPhyTemplate();
            IndexTemplatePhy slaveTemplate = logicTemplateWithPhysicals.getSlavePhyTemplate();
            singleSwitchDetail.setMasterCluster(slaveTemplate.getCluster());
            singleSwitchDetail.setSlaveCluster(masterTemplate.getCluster());
            singleSwitchDetail.setDeleteDcdrChannelFlag(false);
            singleSwitchDetail.setCreateTime(new Date());

            //1.3 构建DCDR主从切换初始化任务进度信息
            List<String> stepMsgList = new ArrayList<>();
            if (DCDRSwithTypeEnum.SMOOTH.getCode().equals(singleSwitchDetail.getSwitchType())) {
                stepMsgList = buildInitTaskProgressInfo(DCDR_SWITCH_STEP_ARR_SMOOTH);
            }

            if (DCDRSwithTypeEnum.FORCE.getCode().equals(singleSwitchDetail.getSwitchType())) {
                stepMsgList = buildInitTaskProgressInfo(DCDR_SWITCH_STEP_ARR_FORCE);
            }
            singleSwitchDetail.setTaskProgressList(stepMsgList);

            dcdrSingleTemplateMasterSlaveSwitchDetailList.add(singleSwitchDetail);
        }

        //2. 分批次执行
        int tempDCDRConcurrent = dcdrConcurrent;
        Collections.shuffle(dcdrSingleTemplateMasterSlaveSwitchDetailList);
        for (DCDRSingleTemplateMasterSlaveSwitchDetail singleTemplateMasterSlaveSwitchDetail : dcdrSingleTemplateMasterSlaveSwitchDetailList) {
            if (tempDCDRConcurrent > 0) {
                singleTemplateMasterSlaveSwitchDetail.setTaskStatus(DCDRStatusEnum.RUNNING.getCode());
                tempDCDRConcurrent--;
            } else {
                singleTemplateMasterSlaveSwitchDetail.setTaskStatus(DCDRStatusEnum.WAIT.getCode());
            }
        }

        dcdrTasksDetail.setDcdrSingleTemplateMasterSlaveSwitchDetailList(dcdrSingleTemplateMasterSlaveSwitchDetailList);
        return dcdrTasksDetail;
    }

    /**
     * 刷新DCDR链路状态
     * @param taskId                 DCDR主从切换任务id
     * @param step                   是否需要根据起始执行步骤往后执行
     * @param operator
     *
     */
    private void doRefreshDCDRChannelsState(Integer taskId, Integer step, String operator) {
        Result<OpTask> taskForDcdrSwitchResult = opTaskManager.getById(taskId);
        if (taskForDcdrSwitchResult.failed()) {
            LOGGER.error("method=asyncRefreshDCDRChannelState||taskId={}||msg=taskId is empty", taskId);
            return;
        }

        OpTask taskForDCDRSwitch = taskForDcdrSwitchResult.getData();

        DCDRTasksDetail dcdrTasksDetail = ConvertUtil.str2ObjByJson(taskForDCDRSwitch.getExpandData(),
            DCDRTasksDetail.class);
        if (null == dcdrTasksDetail) {
            return;
        }

        if (hasSkipForTask(taskForDCDRSwitch, dcdrTasksDetail)) {
            return;
        }

        List<DCDRSingleTemplateMasterSlaveSwitchDetail> singleSwitchDetailList = dcdrTasksDetail
            .getDcdrSingleTemplateMasterSlaveSwitchDetailList();
        if (CollectionUtils.isEmpty(singleSwitchDetailList)) {
            return;
        }

        for (DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail : singleSwitchDetailList) {
            //前置过滤处理
            if (hasSkipForSingleDCDRRefresh(switchDetail)) {
                continue;
            }

            //并发去刷新多个模板状态
            BATCH_DCDR_FUTURE_UTIL.runnableTask(() -> {
                try {
                    IndexTemplateWithPhyTemplates logicTemplateWithPhysicals = indexTemplateService
                        .getLogicTemplateWithPhysicalsById(switchDetail.getTemplateId().intValue());
                    IndexTemplatePhy masterTemplate = logicTemplateWithPhysicals.getMasterPhyTemplate();
                    IndexTemplatePhy slaveTemplate = logicTemplateWithPhysicals.getSlavePhyTemplate();

                    if (null == slaveTemplate) {
                        return;
                    }

                    // 防止并发问题带来的逆向主从切换
                    if (switchDetail.getMasterCluster().equals(masterTemplate.getCluster())
                        && switchDetail.getSlaveCluster().equals(slaveTemplate.getCluster())) {
                        return;
                    }

                    syncRefreshStatus(taskId, step, switchDetail, masterTemplate, slaveTemplate, operator);

                    switchDetail.setUpdateTime(new Date());
                } catch (Exception e) {
                    LOGGER.error("method=doRefreshDCDRChannelsState||taskId={}||templateId={}||msg={}",
                        taskForDCDRSwitch.getId(), switchDetail.getTemplateId(), e.getMessage(), e);
                }

            });
        }
        BATCH_DCDR_FUTURE_UTIL.waitExecute();

        try {
            saveNewestWorkTaskStatusToDB(taskForDCDRSwitch, dcdrTasksDetail, AuthConstant.SUPER_PROJECT_ID);
        } catch (Exception e) {
            LOGGER.error("method=doRefreshDCDRChannelsState||taskId={}||msg=failed to save newest workTask to db",
                taskForDCDRSwitch.getId(), e);
        }
    }

    /**
     * 同步刷新状态
     *
     * @param taskId
     * @param step                步骤
     * @param switchDetail        元数据
     * @param masterTemplate      主模板信息
     * @param slaveTemplate       从模板信息
     * @param operator            操作人
     * @return
     */
    @NotNull
    private void syncRefreshStatus(Integer taskId, Integer step, DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail,
                                   IndexTemplatePhy masterTemplate, IndexTemplatePhy slaveTemplate, String operator) {

        Result<List<String>> executeDCDRResult = Result.buildSucc();
        // 平滑切换刷新状态
        if (DCDRSwithTypeEnum.SMOOTH.getCode().equals(switchDetail.getSwitchType())) {
            executeDCDRResult = executeDCDRForSmooth(taskId, switchDetail, slaveTemplate.getId(), step, masterTemplate,
                slaveTemplate, operator);
        }

        // 强制切换刷新状态
        if (DCDRSwithTypeEnum.FORCE.getCode().equals(switchDetail.getSwitchType())) {
            executeDCDRResult = executeDCDRForForce(taskId, switchDetail, slaveTemplate.getId(), step, masterTemplate,
                slaveTemplate, operator);
        }

        // 最新状态
        if (executeDCDRResult.failed()) {
            switchDetail.setTaskStatus(DCDRStatusEnum.FAILED.getCode());
        }
        if (executeDCDRResult.success()) {
            switchDetail.setTaskStatus(DCDRStatusEnum.SUCCESS.getCode());
        }
    }

    /**
     * 更新db中 DCDR任务状态
     *
     * @param taskForDCDRSwitch 原任务状态信息
     * @param dcdrTasksDetail   新具体状态信息
     * @param projectId
     */
    private void saveNewestWorkTaskStatusToDB(OpTask taskForDCDRSwitch, DCDRTasksDetail dcdrTasksDetail,
                                              Integer projectId) {
        //根据多个detail task 来计算状态
        dcdrTasksDetail.calculateProcess();

        //是否需要设置下一批DCDR模板切换任务的状态为running
        setNextBatchDCDRTaskDetailStateToRunning(dcdrTasksDetail);

        if (DCDRStatusEnum.SUCCESS.getCode().equals(dcdrTasksDetail.getState())) {
            taskForDCDRSwitch.setStatus(OpTaskStatusEnum.SUCCESS.getStatus());
            //成功删除DCDR链路
            deleteDCDRChannelForSuccForceSwitch(taskForDCDRSwitch, dcdrTasksDetail, projectId);
        }

        if (DCDRStatusEnum.FAILED.getCode().equals(dcdrTasksDetail.getState())) {
            taskForDCDRSwitch.setStatus(OpTaskStatusEnum.FAILED.getStatus());
        }
        if (DCDRStatusEnum.CANCELLED.getCode().equals(dcdrTasksDetail.getState())) {
            taskForDCDRSwitch.setStatus(OpTaskStatusEnum.CANCEL.getStatus());
        }
        if (DCDRStatusEnum.RUNNING.getCode().equals(dcdrTasksDetail.getState())) {
            taskForDCDRSwitch.setStatus(OpTaskStatusEnum.RUNNING.getStatus());
        }

        taskForDCDRSwitch.setExpandData(ConvertUtil.obj2Json(dcdrTasksDetail));

        // 解决分布式部署由于时序不一致带来更新不一致的问题
        Result<OpTask> workTaskResult = opTaskManager.getById(taskForDCDRSwitch.getId());
        if (null != workTaskResult.getData()
            && OpTaskStatusEnum.SUCCESS.getStatus().equals(workTaskResult.getData().getStatus())) {
            return;
        }

        // 这里由于多线程更新，可能会出现不可重复读的问题，所以这里加上了一个判断
        // 临时打个补丁，等待下一个版本ecm 重构
        if (workTaskResult.getData().getUpdateTime().after(taskForDCDRSwitch.getUpdateTime())) {
            return;
        }
        taskForDCDRSwitch.setUpdateTime(new Date());

        opTaskManager.updateTask(taskForDCDRSwitch);
    }

    /**
     * 强切成功删除DCDR链路
     *
     * @param taskForDCDRSwitch 任务信息
     * @param dcdrTasksDetail   DCDR任务信息
     * @param projectId
     */
    private void deleteDCDRChannelForSuccForceSwitch(OpTask taskForDCDRSwitch, DCDRTasksDetail dcdrTasksDetail,
                                                     Integer projectId) {
        for (DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail : dcdrTasksDetail
            .getDcdrSingleTemplateMasterSlaveSwitchDetailList()) {
            if (DCDRSwithTypeEnum.FORCE.getCode().equals(switchDetail.getSwitchType())) {
                try {
                    Result<Void> deleteDCDRResult = deleteDCDR(switchDetail.getTemplateId().intValue(),
                        AriusUser.SYSTEM.getDesc(), projectId);
                    if (deleteDCDRResult.failed()) {
                        LOGGER.error(
                            "method=deleteDCDRChannelForSuccForceSwitch||taskId={}||msg=failed to deleteDCDR for force switch",
                            taskForDCDRSwitch.getId());
                        switchDetail.setDeleteDcdrChannelFlag(false);
                    } else {
                        switchDetail.setDeleteDcdrChannelFlag(true);
                    }
                } catch (ESOperateException e) {
                    LOGGER.error(
                        "method=deleteDCDRChannelForSuccForceSwitch||taskId={}||msg=failed to deleteDCDR for force switch",
                        taskForDCDRSwitch.getId(), e);
                    switchDetail.setDeleteDcdrChannelFlag(false);
                }
            }
        }
    }

    /**
     * 是否需要更新下一批DCDR模板切换任务的状态为running
     * @param dcdrTasksDetail
     */
    private void setNextBatchDCDRTaskDetailStateToRunning(DCDRTasksDetail dcdrTasksDetail) {
        List<DCDRSingleTemplateMasterSlaveSwitchDetail> slaveSwitchDetailList = dcdrTasksDetail
            .getDcdrSingleTemplateMasterSlaveSwitchDetailList();

        // 按任务状态分组
        Map<Integer, List<DCDRSingleTemplateMasterSlaveSwitchDetail>> status2SwitchDetailMap = ConvertUtil
            .list2MapOfList(slaveSwitchDetailList, DCDRSingleTemplateMasterSlaveSwitchDetail::getTaskStatus,
                dcdrSingleTemplateMasterSlaveSwitchDetail -> dcdrSingleTemplateMasterSlaveSwitchDetail);

        //获取任务成功数
        List<DCDRSingleTemplateMasterSlaveSwitchDetail> runningSwitchDetailList = status2SwitchDetailMap
            .get(DCDRStatusEnum.RUNNING.getCode());
        int runingTaskSize = CollectionUtils.isNotEmpty(runningSwitchDetailList) ? runningSwitchDetailList.size() : 0;

        //获取任务失败数
        List<DCDRSingleTemplateMasterSlaveSwitchDetail> failedSwitchDetailList = status2SwitchDetailMap
            .get(DCDRStatusEnum.FAILED.getCode());
        int failedTaskSize = CollectionUtils.isNotEmpty(failedSwitchDetailList) ? failedSwitchDetailList.size() : 0;

        // 运行数小于 并发数, 并且在失败数上限
        if (hasSetNextBatch(runingTaskSize, failedTaskSize)) {
            List<DCDRSingleTemplateMasterSlaveSwitchDetail> waitingSwitchDetailList = status2SwitchDetailMap
                .get(DCDRStatusEnum.WAIT.getCode());

            if (CollectionUtils.isEmpty(waitingSwitchDetailList)) {
                return;
            }

            int tempDCDRConcurrent = dcdrConcurrent;
            for (DCDRSingleTemplateMasterSlaveSwitchDetail waitingSwitchDetail : waitingSwitchDetailList) {
                if (tempDCDRConcurrent > 0) {
                    waitingSwitchDetail.setTaskStatus(DCDRStatusEnum.RUNNING.getCode());
                    tempDCDRConcurrent--;
                }
            }
        }
    }

    /**
     * 是否需要跳过 DCDRChannel 刷新流程
     * @param dcdrSingleTemplateMasterSlaveSwitchDetail
     * @return
     */
    private boolean hasSkipForSingleDCDRRefresh(DCDRSingleTemplateMasterSlaveSwitchDetail dcdrSingleTemplateMasterSlaveSwitchDetail) {
        return DCDRStatusEnum.CANCELLED.getCode().equals(dcdrSingleTemplateMasterSlaveSwitchDetail.getTaskStatus())
               || DCDRStatusEnum.SUCCESS.getCode().equals(dcdrSingleTemplateMasterSlaveSwitchDetail.getTaskStatus())
               || DCDRStatusEnum.WAIT.getCode().equals(dcdrSingleTemplateMasterSlaveSwitchDetail.getTaskStatus())
               || DCDRStatusEnum.FAILED.getCode().equals(dcdrSingleTemplateMasterSlaveSwitchDetail.getTaskStatus());
    }

    /**
     * 是否需要跳过 任务刷新
     * @param taskForDCDRSwitch
     * @param dcdrTasksDetail
     * @return
     */
    private boolean hasSkipForTask(OpTask taskForDCDRSwitch, DCDRTasksDetail dcdrTasksDetail) {
        List<DCDRSingleTemplateMasterSlaveSwitchDetail> switchDetailList = dcdrTasksDetail
            .getDcdrSingleTemplateMasterSlaveSwitchDetailList();
        List<Integer> runningTaskStatusList = switchDetailList.stream()
            .filter(switchDetail -> DCDRStatusEnum.RUNNING.getCode().equals(switchDetail.getTaskStatus()))
            .map(DCDRSingleTemplateMasterSlaveSwitchDetail::getTaskStatus).collect(Collectors.toList());

        return OpTaskStatusEnum.CANCEL.getStatus().equals(taskForDCDRSwitch.getStatus())
               || OpTaskStatusEnum.SUCCESS.getStatus().equals(taskForDCDRSwitch.getStatus())
               || runningTaskStatusList.isEmpty();
    }

    /**
     * 初始化主从切换任务信息
     * @param templateId
     * @param dcdrTasksDetail
     */
    private void initSwitchTaskInfo(Integer templateId, DCDRTasksDetail dcdrTasksDetail) {
        if (null != templateId) {
            List<DCDRSingleTemplateMasterSlaveSwitchDetail> switchDetailList = dcdrTasksDetail
                .getDcdrSingleTemplateMasterSlaveSwitchDetailList();
            for (DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail : switchDetailList) {
                if (templateId.equals(switchDetail.getTemplateId().intValue())) {
                    switchDetail.setTaskStatus(DCDRStatusEnum.RUNNING.getCode());
                    if (DCDRSwithTypeEnum.SMOOTH.getCode().equals(switchDetail.getSwitchType())) {
                        switchDetail.setTaskProgressList(buildInitTaskProgressInfo(DCDR_SWITCH_STEP_ARR_SMOOTH));
                    }

                    if (DCDRSwithTypeEnum.FORCE.getCode().equals(switchDetail.getSwitchType())) {
                        switchDetail.setTaskProgressList(buildInitTaskProgressInfo(DCDR_SWITCH_STEP_ARR_FORCE));
                    }
                }
            }
        }
    }

    /**
     * 判断是否需要切换执行下批任务
     * @param runingTaskSize  运行任务数
     * @param failedTaskSize  失败任务数
     * @return
     */
    private boolean hasSetNextBatch(int runingTaskSize, int failedTaskSize) {
        return dcdrConcurrent > runingTaskSize && (runingTaskSize / dcdrConcurrent) == 0
               && dcdrFaultTolerant >= failedTaskSize;
    }
}