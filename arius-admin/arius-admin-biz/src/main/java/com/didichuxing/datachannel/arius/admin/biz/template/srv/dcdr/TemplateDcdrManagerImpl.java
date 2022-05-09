package com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr;

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.TEMPLATE;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum.MASTER;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum.SLAVE;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_DCDR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.google.common.base.Strings;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.limit.TemplateLimitManager;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.WorkTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.WorkTaskProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.DCDRMasterSlaveSwitchDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalCopyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalDCDRDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DCDRSingleTemplateMasterSlaveSwitchDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DCDRTasksDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateDCDRInfoVO;
import com.didichuxing.datachannel.arius.admin.common.constant.dcdr.DcdrStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.dcdr.DcdrSwithTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.WorkTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.WorkTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDCDRStepEnum;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.WorkTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.DCDRSingleTemplateMasterSlaveSwitchDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.DCDRTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.DCDRTasksDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusTaskThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.BatchProcessor;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateLabelService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESDCDRDAO;
import com.didiglobal.logi.elasticsearch.client.request.dcdr.DCDRTemplate;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * 索引dcdr服务实现
 * @author zqr
 * @date 2020-09-09
 */
@Service
public class TemplateDcdrManagerImpl extends BaseTemplateSrv implements TemplateDcdrManager {

    private static final ILog          LOGGER                    = LogFactory.getLog( TemplateDcdrManagerImpl.class);

    private static final String        DCDR_TEMPLATE_NAME_FORMAT = "%s_to_%s";

    private static final String        DCDR_INDEX_SETTING        = "dcdr.replica_index";

    private static final int           DCDR_SWITCH_STEP_1        = 1;
    private static final int           DCDR_SWITCH_STEP_2        = 2;
    private static final int           DCDR_SWITCH_STEP_3        = 3;
    private static final int           DCDR_SWITCH_STEP_4        = 4;
    private static final int           DCDR_SWITCH_STEP_5        = 5;
    private static final int           DCDR_SWITCH_STEP_6        = 6;
    private static final int           DCDR_SWITCH_STEP_7        = 7;
    private static final int           DCDR_SWITCH_STEP_8        = 8;
    private static final int           DCDR_SWITCH_STEP_9        = 9;
    private static final String        DCDR_SWITCH_TODO          = "TODO";
    private static final String        DCDR_SWITCH_DONE          = "DONE";
    private static final String        DCDR_SWITCH_FAIL          = "FAIL";
    private static final String        SEPARATOR                 = "@@@";
    private static final String        SUCCESS_INFO              = "Successful Execution";

    private static final String[]       DCDR_SWITCH_STEP_ARR_SMOOTH = new String[] {
                                        TemplateDCDRStepEnum.STEP_1.getValue(),
                                        TemplateDCDRStepEnum.STEP_2.getValue(),
                                        TemplateDCDRStepEnum.STEP_3.getValue(),
                                        TemplateDCDRStepEnum.STEP_4.getValue(),
                                        TemplateDCDRStepEnum.STEP_5.getValue(),
                                        TemplateDCDRStepEnum.STEP_6.getValue(),
                                        TemplateDCDRStepEnum.STEP_7.getValue(),
                                        TemplateDCDRStepEnum.STEP_8.getValue(),
                                        TemplateDCDRStepEnum.STEP_9.getValue()
    };

    private static final String[]       DCDR_SWITCH_STEP_ARR_FORCE = new String[] {
                                        TemplateDCDRStepEnum.STEP_5.getValue(),
                                        TemplateDCDRStepEnum.STEP_9.getValue()
    };

    private static final String   TEMPLATE_NO_EXIST         = "模板不存在";

    private static final String   TASK_IS_CANCEL            = "任务已取消";

    private static final String   TASK_EMPTY                = "根据任务Id[%s]获取任务失败";

    @Value("${dcdr.concurrent:2}")
    private Integer               dcdrConcurrent;

    @Value("${dcdr.fault.tolerant:5}")
    private Integer               dcdrFaultTolerant;

    @Autowired
    private ESDCDRDAO             esdcdrDAO;

    @Autowired
    private ESIndexService        esIndexService;

    @Autowired
    private ESTemplateService     esTemplateService;

    @Autowired
    private TemplateLimitManager  templateLimitManager;

    @Autowired
    private TemplateLabelService  templateLabelService;

    @Autowired
    private WorkTaskManager       workTaskManager;

    @Autowired
    private TemplateLogicService  templateLogicService;

    @Autowired
    private OperateRecordService operateRecordService;

    @Override
    public TemplateServiceEnum templateService() { return TEMPLATE_DCDR; }
    
    private AriusTaskThreadPool ariusTaskThreadPool;

    private Cache<Integer, ReentrantLock> taskId2ReentrantLockCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES).maximumSize(100).build();

    @PostConstruct
    public void init() {
        ariusTaskThreadPool = new AriusTaskThreadPool();
        ariusTaskThreadPool.init(10, "TemplateDcdrManagerImpl", 10000);
    }

    private static final FutureUtil<Void>  BATCH_DCDR_FUTURE_UTIL   = FutureUtil.init("BATCH_DCDR_FUTURE_UTIL",10,10,100);

    /**
     * 创建dcdr
     *
     * @param logicId  模板ID
     * @param operator 操作人
     * @return result
     * @throws ESOperateException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createDcdr(Integer logicId, String operator) throws ESOperateException {
        return createPhyDcdr(createDCDRMeta(logicId), operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> copyAndCreateDcdr(Integer templateId, String targetCluster, String rack, String operator) throws AdminOperateException {
        //1. 判断目标集群是否存在模板, 存在则需要删除, 避免copy失败，确保copy流程的执行来保证主从模板setting mapping等信息的一致性。
        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService.getLogicTemplateWithPhysicalsById(templateId);
        if (null == templateLogicWithPhysical) {return Result.buildParamIllegal(TEMPLATE_NO_EXIST);}

        IndexTemplatePhy slavePhyTemplate = templateLogicWithPhysical.getSlavePhyTemplate();
        if (null != slavePhyTemplate) {
            //1.1删除dcdr链路
            Result<Void> deleteDcdrResult = deleteDcdr(templateId, operator);
            if (deleteDcdrResult.failed()) {return deleteDcdrResult;}

            //1.2清理slave模板
            Result<Void> delTemplateResult = templatePhyService.delTemplate(slavePhyTemplate.getId(), operator);
            if (delTemplateResult.failed()) { return delTemplateResult; }
        }

        // 2. 校验目标集群合法性
        ClusterPhy targetClusterPhy = clusterPhyService.getClusterByName(targetCluster);
        if (null == targetClusterPhy) { return Result.buildFail(String.format("目标集群[%s]不存在", targetCluster));}
        IndexTemplatePhy masterPhyTemplate = templateLogicWithPhysical.getMasterPhyTemplate();
        if (null == masterPhyTemplate) {
            return Result.buildFail(String.format("模板Id[%s]不存在", templateId));
        }

        if (AriusObjUtils.isBlack(masterPhyTemplate.getCluster())) {
            return Result.buildFail(String.format("模板Id[%s]所在集群[%s]不存在", templateId, masterPhyTemplate.getCluster()));
        }

        ClusterPhy sourceClusterPhy = clusterPhyService.getClusterByName(masterPhyTemplate.getCluster());
        if (null == sourceClusterPhy) { return Result.buildFail(String.format("原集群[%s]不存在", masterPhyTemplate.getCluster()));}
        if (null != sourceClusterPhy.getEsVersion() && !sourceClusterPhy.getEsVersion().equals(targetClusterPhy.getEsVersion())){
            return Result.buildFail("主从集群版本必须一致");
        }

        // 3. 执行复制流程
        TemplatePhysicalCopyDTO templatePhysicalCopyDTO = buildTemplatePhysicalCopyDTO(templateId, targetCluster, rack);
        if (null == templatePhysicalCopyDTO) {
            return Result.buildFail(TEMPLATE_NO_EXIST);
        }

        Result<Void> copyTemplateResult = templatePhyManager.copyTemplate(templatePhysicalCopyDTO, operator);
        if (copyTemplateResult.failed()) {
            throw new ESOperateException(copyTemplateResult.getMessage());
        }

        //3. 创建dcdr链路
        Result<Void> result = createPhyDcdr(createDCDRMeta(templateId), operator);

        //4. 记录操作
        if (result.success()) {
            operateRecordService.save(TEMPLATE, CREATE_DCDR, templateId, "创建DCDR链路，主集群：" +
                    templateLogicWithPhysical.getMasterPhyTemplate().getCluster() + "；从集群：" + targetCluster, operator);
        }
        return result;
    }

    /**
     * 删除dcdr
     *
     * @param logicId  模板ID
     * @param operator 操作人
     * @return result
     * @throws ESOperateException
     */
    @Override
    public Result<Void> deleteDcdr(Integer logicId, String operator) throws ESOperateException {
        Result<Void> checkResult = checkDCDRParam(logicId);

        if (checkResult.failed()) {
            return checkResult;
        }

        TemplatePhysicalDCDRDTO dcdrdto = createDCDRMeta(logicId);

        Result<Void> result = deletePhyDcdr(dcdrdto, operator);

        if (result.success()) {
            templateLabelService.updateTemplateLabel(logicId, null,
                    Sets.newHashSet(TemplateLabelService.TEMPLATE_HAVE_DCDR), operator);
        }
        return result;
    }

    /**
     * 创建dcdr链路
     *
     * @param param    参数
     * @param operator 操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createPhyDcdr(TemplatePhysicalDCDRDTO param, String operator) throws ESOperateException {
        Result<Void> checkDCDRResult = checkDCDRParam(param);

        if (checkDCDRResult.failed()) {
            throw new ESOperateException(checkDCDRResult.getMessage());
        }

        for (int i = 0; i < param.getPhysicalIds().size(); ++i) {
            IndexTemplatePhy templatePhysicalPO = templatePhyService.getTemplateById(param.getPhysicalIds().get(i));

            // 判断集群与从集群是否配置了
            if (!clusterPhyService.ensureDcdrRemoteCluster(templatePhysicalPO.getCluster(),
                    param.getReplicaClusters().get(i))) {
                return Result.buildFail("创建remote-cluster失败");
            }

            if (!syncCreateTemplateDCDR(param.getPhysicalIds().get(i), param.getReplicaClusters().get(i), 3)) {
                return Result.buildFail("创建dcdr链路失败");

            }
        }

        return Result.buildSucc();
    }

    /**
     * 删除dcdr链路
     *
     * @param param    参数
     * @param operator 操作人
     * @return result
     */
    @Override
    public Result<Void> deletePhyDcdr(TemplatePhysicalDCDRDTO param, String operator) throws ESOperateException {
        Result<Void> checkDCDRResult = checkDCDRParam(param);

        if (checkDCDRResult.failed()) {
            return checkDCDRResult;
        }

        for (int i = 0; i < param.getPhysicalIds().size(); ++i) {
            if (syncDeleteTemplateDCDR(param.getPhysicalIds().get(i), param.getReplicaClusters().get(i), 3)) {
                IndexTemplatePhy templatePhysicalPO = templatePhyService.getTemplateById(param.getPhysicalIds().get(i));

                if (param.getDeleteIndexDcdr() == null || param.getDeleteIndexDcdr()) {
                    if (syncDeleteIndexDCDR(templatePhysicalPO.getCluster(), param.getReplicaClusters().get(i),
                            templatePhyService.getMatchIndexNames(templatePhysicalPO.getId()), 3)) {
                        LOGGER.info("method=deleteDcdr||physicalId={}||msg=delete index dcdr succ",
                                param.getPhysicalIds());
                    } else {
                        LOGGER.warn("method=deleteDcdr||physicalId={}||msg=delete index dcdr fail",
                                param.getPhysicalIds());
                    }
                }

                operateRecordService.save(TEMPLATE, DELETE_DCDR, templatePhysicalPO.getLogicId(),
                        "replicaCluster:" + param.getReplicaClusters(), operator);
                return Result.buildSucc();
            }
        }

        return Result.buildFail("删除dcdr链路失败");
    }

    /**
     * dcdr主从切换
     *
     * @param logicId                逻辑模板ID
     * @param expectMasterPhysicalId 主
     * @param operator               操作人
     * @return result
     */
    @Override
    public Result<Void> dcdrSwitchMasterSlave(Integer logicId, Long expectMasterPhysicalId, int step, String operator) {
        Result<Void> checkValidForDcdrSwitchResult = checkValidForDcdrSwitch(logicId, expectMasterPhysicalId, step, operator);
        if (checkValidForDcdrSwitchResult.failed()) {
            return checkValidForDcdrSwitchResult;
        }

        //记录dcdr任务开始
        if (step == TemplateDCDRStepEnum.STEP_1.getStep()) {
            WorkTaskDTO workTaskDTO = new WorkTaskDTO();
            workTaskDTO.setBusinessKey(getBusinessKey(new ArrayList<>(logicId)));
            workTaskDTO.setTaskType(WorkTaskTypeEnum.TEMPLATE_DCDR.getType());
            workTaskDTO.setCreator(operator);
            workTaskManager.addTask(workTaskDTO);
        }
        Result<List<String>> executeDcdrResult = Result.buildSucc();
        Result<Void> result = Result.buildFrom(executeDcdrResult);
        processDcdrTask(logicId, result, step);
        return result;
    }


    @Override
    public Result<WorkTaskVO> batchDcdrSwitchMaster2Slave(DCDRMasterSlaveSwitchDTO dcdrMasterSlaveSwitchDTO, String operator) {
        Result<WorkTask> workTaskResult = Result.buildSucc();
        try {
            //1. 批量校验模板dcdr是否可以切换, 仅有一个模板校验不通过结果为不通过。
            List<Long> templateIdList = dcdrMasterSlaveSwitchDTO.getTemplateIds();
            Result<Void> batchCheckValidForDcdrSwitchResult = batchCheckValidForDcdrSwitch(templateIdList, operator);
            if (batchCheckValidForDcdrSwitchResult.failed()) {
                return Result.buildFrom(batchCheckValidForDcdrSwitchResult);
            }

            //2.1 设置基础数据
            WorkTaskDTO workTaskDTO = new WorkTaskDTO();
            String businessKey = getBusinessKey(templateIdList);
            workTaskDTO.setBusinessKey(businessKey);
            workTaskDTO.setTitle(WorkTaskTypeEnum.TEMPLATE_DCDR.getMessage());
            workTaskDTO.setTaskType(WorkTaskTypeEnum.TEMPLATE_DCDR.getType());
            workTaskDTO.setCreator(operator);
            workTaskDTO.setDeleteFlag(false);
            workTaskDTO.setStatus(WorkTaskStatusEnum.RUNNING.getStatus());

            //2.2 设置多个模板dcdr任务信息
            DCDRTasksDetail dcdrTasksDetail = buildDcdrTasksDetail(dcdrMasterSlaveSwitchDTO, templateIdList);

            //2.3 计算状态
            dcdrTasksDetail.calculateProcess();

            //2.4 保存任务
            workTaskDTO.setExpandData(ConvertUtil.obj2Json(dcdrTasksDetail));
            workTaskResult = workTaskManager.addTask(workTaskDTO);
            if (workTaskResult.failed()) return Result.buildFrom(workTaskResult);

            //2.5 记录操作
            for (DCDRSingleTemplateMasterSlaveSwitchDetail dcdrTask: dcdrTasksDetail.getDcdrSingleTemplateMasterSlaveSwitchDetailList()) {
                operateRecordService.save(TEMPLATE, SWITCH_MASTER_SLAVE, dcdrTask.getTemplateId(), "主从切换，主集群：" + dcdrTask.getMasterCluster() +
                        "切换至从集群：" + dcdrTask.getSlaveCluster(), operator);
            }

        } catch (Exception e) {
            LOGGER.error("method=batchDcdrSwitchMaster2Slave||templateIds={}||msg={}", dcdrMasterSlaveSwitchDTO.getTemplateIds(), e.getMessage(), e);
        }

        return Result.buildSucc(ConvertUtil.obj2Obj(workTaskResult.getData(), WorkTaskVO.class));
    }

    @Override
    public Result<Void> cancelDcdrSwitchMasterSlaveByTaskId(Integer taskId, String operator) throws ESOperateException{
        return cancelDcdrSwitchMasterSlaveByTaskIdAndTemplateIds(taskId, null, true, operator);
    }

    @Override
    public Result<Void> cancelDcdrSwitchMasterSlaveByTaskIdAndTemplateIds(Integer taskId, List<Long> templateIds,
                                                                          boolean fullDeleteFlag,
                                                                          String operator) throws ESOperateException {
        try {
            Result<WorkTask> taskForDcdrSwitchResult = workTaskManager.getById(taskId);
            if (taskForDcdrSwitchResult.failed()) {
                LOGGER.error("method=cancelDcdrSwitchMasterSlaveByTaskIdAndTemplateIds||taskId={}||msg=taskId is empty", taskId);
                return Result.buildFail(String.format(TASK_EMPTY,taskId));
            }

            WorkTask        taskForDcdrSwitch = taskForDcdrSwitchResult.getData();
            DCDRTasksDetail dcdrTasksDetail   = ConvertUtil.str2ObjByJson(taskForDcdrSwitch.getExpandData(),
                DCDRTasksDetail.class);
            List<DCDRSingleTemplateMasterSlaveSwitchDetail> dcdrSingleTemplateMasterSlaveSwitchDetailList = dcdrTasksDetail
                .getDcdrSingleTemplateMasterSlaveSwitchDetailList();
            if (CollectionUtils.isEmpty(dcdrSingleTemplateMasterSlaveSwitchDetailList))
                return Result.buildSucc();

            for (DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail : dcdrSingleTemplateMasterSlaveSwitchDetailList) {
                if (fullDeleteFlag) {
                    //取消任务中全部dcdr模板链路
                    if (DcdrStatusEnum.SUCCESS.getCode().equals(switchDetail.getTaskStatus())) { continue; }
                    switchDetail.setTaskStatus(DcdrStatusEnum.CANCELLED.getCode());
                } else {
                    if (!CollectionUtils.isEmpty(templateIds) && templateIds.contains(switchDetail.getTemplateId())) {
                        //取消任务中指定dcdr模板链路
                        switchDetail.setTaskStatus(DcdrStatusEnum.CANCELLED.getCode());
                    }
                }
            }

            saveNewestWorkTaskStatusToDB(taskForDcdrSwitch, dcdrTasksDetail);
        } catch (Exception e) {
            LOGGER.error("method=cancelDcdrSwitchMasterSlaveByTaskIdAndTemplateIds||taskId={}||templateIds={}||"
                         + "msg={}", taskId, templateIds, e.getMessage(), e);
            return Result.buildFail("取消失败, 请联系管理员");
        }

        return Result.buildSucc();
    }

    @Override
    public Result<Void> refreshDcdrChannelState(Integer taskId, Integer templateId, String operator) {
        Result<WorkTask> taskForDcdrSwitchResult = workTaskManager.getById(taskId);
        if (taskForDcdrSwitchResult.failed()) {
            return Result.buildFrom(taskForDcdrSwitchResult);
        }

        WorkTask taskForDcdrSwitch = taskForDcdrSwitchResult.getData();
        if (null == taskForDcdrSwitch) {
            return Result.buildFail("任务不存在");
        }

        DCDRTasksDetail dcdrTasksDetail = ConvertUtil.str2ObjByJson(taskForDcdrSwitch.getExpandData(),
                DCDRTasksDetail.class);

        // 1. 初始化单个模板dcdr任务状态
        initSwitchTaskInfo(templateId, dcdrTasksDetail);

        // 2. 保存初始化状态
        saveNewestWorkTaskStatusToDB(taskForDcdrSwitch, dcdrTasksDetail);

        return Result.buildSucc();
    }

    @Override
    public Result<Void> asyncRefreshDcdrChannelState(Integer taskId, Integer templateId, String operator) {
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
                    if (reentrantLock.tryLock(5, TimeUnit.SECONDS)) {
                        // 刷新全量状态
                        doRefreshDcdrChannelsState(taskId, 1, operator);
                    } else {
                        LOGGER.info(
                            "method=asyncRefreshDcdrChannelState||taskId={}||thread={}||errMsg=failed to fetch the lock",
                                taskId, Thread.currentThread().getName());
                    }
                } catch (Exception e) {
                    LOGGER.error("method=asyncRefreshDcdrChannelState||taskId={}||errMsg={}", taskId, e);
                } finally {
                    reentrantLock.unlock();
                }
            }
        });
        
        return Result.buildSucc();
    }

    @Override
    public Result<Void> forceSwitchMasterSlave(Integer taskId, Integer templateId, String operator) {
        if (null == templateId) {
            return Result.buildFail(String.format("模板Id[%s]不存在", templateId));
        }

        try {
            Result<WorkTask> taskForDcdrSwitchResult = workTaskManager.getById(taskId);
            if (taskForDcdrSwitchResult.failed()) {
                LOGGER.error("method=forceSwitchMasterSlave||taskId={}||msg=taskId is empty", taskId);
                return Result.buildFail(String.format(TASK_EMPTY, taskId));
            }

            WorkTask data = taskForDcdrSwitchResult.getData();
            if (null == data) {
                LOGGER.error("method=forceSwitchMasterSlave||taskId={}||msg=WorkTask is empty", taskId);
                return Result.buildFail("获取dcdr任务详情失败, 请检查任务是否存在");
            }

            DCDRTasksDetail dcdrTasksDetail = ConvertUtil.str2ObjByJson(data.getExpandData(), DCDRTasksDetail.class);
            List<DCDRSingleTemplateMasterSlaveSwitchDetail> switchDetailList = dcdrTasksDetail
                .getDcdrSingleTemplateMasterSlaveSwitchDetailList();
            if (CollectionUtils.isEmpty(switchDetailList)) {
                Result.buildFail("强切失败, 请确认是否有dcdr任务");
            }

            for (DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail : switchDetailList) {
                if (templateId.equals(switchDetail.getTemplateId().intValue())) {
                    switchDetail.setSwitchType(DcdrSwithTypeEnum.FORCE.getCode());
                }
            }

            //1.初始化单个模板dcdr任务状态
            initSwitchTaskInfo(templateId, dcdrTasksDetail);
            
            //2. 更新任务状态
            saveNewestWorkTaskStatusToDB(data, dcdrTasksDetail);
        } catch (Exception e) {
            LOGGER.error("method=cancelDcdrSwitchMasterSlaveByTaskIdAndTemplateIds||taskId={}||templateId={}||msg="
                         + "failed to save newest workTask to db",
                taskId, templateId, e);
            return Result.buildFail("主从强切失败, 请联系管理员");
        }
        
        return Result.buildSucc();
    }

    @Override
    public Result<DCDRTasksDetailVO> getDCDRMasterSlaveSwitchDetailVO(Integer taskId) {
        Result<WorkTask> taskForDcdrSwitchResult = workTaskManager.getById(taskId);
        if (taskForDcdrSwitchResult.failed()) {
            LOGGER.error("method=getDCDRMasterSlaveSwitchDetailVO||taskId={}||msg=taskId is empty", taskId);
            return Result.buildFail(String.format(TASK_EMPTY, taskId));
        }

        WorkTask data = taskForDcdrSwitchResult.getData();
        if (null == data) {
            LOGGER.error("method=getDCDRMasterSlaveSwitchDetailVO||taskId={}||msg=WorkTask is empty", taskId);
            return Result.buildFail("获取DCDR任务详情失败");
        }
        DCDRTasksDetail dcdrTasksDetail = ConvertUtil.str2ObjByJson(data.getExpandData(), DCDRTasksDetail.class);
        dcdrTasksDetail.calculateProcess();

        //刷新dcdr任务状态
        if (DcdrStatusEnum.RUNNING.getCode().equals(dcdrTasksDetail.getState())) {
            asyncRefreshDcdrChannelState(taskId, null,null);
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(dcdrTasksDetail, DCDRTasksDetailVO.class));
    }

    @Override
    public Result<DCDRSingleTemplateMasterSlaveSwitchDetailVO> getDCDRSingleTemplateMasterSlaveSwitchDetailVO(Integer taskId,
                                                                                                              Long templateId) {
        if (null == templateId) return Result.buildParamIllegal("模板为空");

        Result<WorkTask> taskForDcdrSwitchResult = workTaskManager.getById(taskId);
        if (taskForDcdrSwitchResult.failed()) {
            return Result.buildFail(String.format(TASK_EMPTY, taskId));
        }

        WorkTask taskForDcdrSwitch      = taskForDcdrSwitchResult.getData();
        if (null == taskForDcdrSwitch) return Result.buildFail("任务不存在");

        DCDRTasksDetail dcdrTasksDetail = ConvertUtil.str2ObjByJson(taskForDcdrSwitch.getExpandData(), DCDRTasksDetail.class);

        List<DCDRSingleTemplateMasterSlaveSwitchDetail> switchDetailList = dcdrTasksDetail.getDcdrSingleTemplateMasterSlaveSwitchDetailList();

        // 检查是否有需要刷新dcdr任务
        switchDetailList.stream()
                .filter(switchDetail -> templateId.equals(switchDetail.getTemplateId())
                        && DcdrStatusEnum.RUNNING.getCode().equals(switchDetail.getTaskStatus()))
                .forEach(switchDetail -> asyncRefreshDcdrChannelState(taskId, templateId.intValue(),null));

        // 返回详情
        for (DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail : switchDetailList) {
            if (templateId.equals(switchDetail.getTemplateId())) {
                return Result.buildSucc(ConvertUtil.obj2Obj(switchDetail, DCDRSingleTemplateMasterSlaveSwitchDetailVO.class));
            }
        }

        return Result.buildFail();
    }

    /**
     * 创建dcdr模板
     *
     * @param physicalId     物理模板ID
     * @param replicaCluster 从集群名称
     * @return result
     */
    @Override
    public boolean syncCreateTemplateDCDR(Long physicalId, String replicaCluster,
                                          int retryCount) throws ESOperateException {

        IndexTemplatePhy templatePhysical = templatePhyService.getTemplateById(physicalId);

        LOGGER.info("method=syncCreateTemplateDCDR||physicalId={}||replicaCluster={}", physicalId, replicaCluster);

        return ESOpTimeoutRetry.esRetryExecute("putDCDRForTemplate", retryCount,
                () -> esdcdrDAO.putAutoReplication(templatePhysical.getCluster(),
                        String.format(DCDR_TEMPLATE_NAME_FORMAT, templatePhysical.getName(), replicaCluster),
                        templatePhysical.getName(), replicaCluster));
    }

    /**
     * 删除dcdr模板
     *
     *
     * @param physicalId     物理模板ID
     * @param replicaCluster 从集群名称
     * @return result
     */
    @Override
    public boolean syncDeleteTemplateDCDR(Long physicalId, String replicaCluster,
                                          int retryCount) throws ESOperateException {
        IndexTemplatePhy templatePhysical = templatePhyService.getTemplateById(physicalId);

        LOGGER.info("method=syncDeleteTemplateDCDR||physicalId={}||replicaCluster={}", physicalId, replicaCluster);

        return ESOpTimeoutRetry.esRetryExecute("deleteDCDRForTemplate", retryCount,
                () -> esdcdrDAO.deleteAutoReplication(templatePhysical.getCluster(),
                        String.format(DCDR_TEMPLATE_NAME_FORMAT, templatePhysical.getName(), replicaCluster)));
    }

    /**
     * 是否存在
     *
     * @param physicalId     物理模板ID
     * @param replicaCluster 从集群名称
     * @return true/false
     */
    @Override
    public boolean syncExistTemplateDCDR(Long physicalId, String replicaCluster) {
        IndexTemplatePhy templatePhysical = templatePhyService.getTemplateById(physicalId);

        LOGGER.info("method=syncExistTemplateDCDR||physicalId={}||replicaCluster={}", physicalId, replicaCluster);

        DCDRTemplate dcdrTemplate = esdcdrDAO.getAutoReplication(templatePhysical.getCluster(),
                String.format(DCDR_TEMPLATE_NAME_FORMAT, templatePhysical.getName(), replicaCluster));

        return dcdrTemplate != null;
    }

    /**
     * 删除索引dcdr链路
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
        return ESOpTimeoutRetry.esRetryExecute("syncDeleteIndexDCDR", retryCount,
                () -> esdcdrDAO.deleteReplication(cluster, replicaCluster, Sets.newHashSet(indices)));
    }

    /**
     * 修改索引配置
     *
     * @param cluster      集群
     * @param indices      索引
     * @param replicaIndex dcdr配置
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
     * 判断集群是否支持dcdr
     *
     * @param phyCluster 集群名称
     * @return
     */
    @Override
    public boolean clusterSupport(String phyCluster) {
        return isTemplateSrvOpen(phyCluster);
    }

    @Override
    public Tuple<Long, Long> getMasterAndSlaveTemplateCheckPoint(Integer templateId) {
        //1.初始化信息
        Tuple<Long, Long> masterAndSlaveCheckPointTuple = new Tuple<>();
        masterAndSlaveCheckPointTuple.setV1(0L);
        masterAndSlaveCheckPointTuple.setV2(0L);

        IndexTemplateLogicWithPhyTemplates logicTemplateWithPhysicals = templateLogicService.getLogicTemplateWithPhysicalsById(templateId);
        IndexTemplatePhy masterPhyTemplate = logicTemplateWithPhysicals.getMasterPhyTemplate();
        IndexTemplatePhy slavePhyTemplate  = logicTemplateWithPhysicals.getSlavePhyTemplate();
        if(null == masterPhyTemplate) {
            LOGGER.warn("class=TemplateDcdrManagerImpl||method=setCheckPointDiff||templateId={}||msg=masterPhyTemplate is empty", templateId);
            return masterAndSlaveCheckPointTuple;
        }

        if(null == slavePhyTemplate) {
            LOGGER.warn("class=TemplateDcdrManagerImpl||method=setCheckPointDiff||templateId={}||msg=slavePhyTemplate is empty", templateId);
            return masterAndSlaveCheckPointTuple;
        }

        //2. 根据索引主从位点信息构建模板主从位点信息
        List<String> indexNames = templatePhyService.getMatchIndexNames(masterPhyTemplate.getId());

        Map<String, IndexNodes> indexStatForMasterMap = esIndexService.syncBatchGetIndices(masterPhyTemplate.getCluster(), indexNames);
        Map<String, IndexNodes> indexStatForSlaveMap  = esIndexService.syncBatchGetIndices(slavePhyTemplate.getCluster(),  indexNames);

        long masterCheckPointTotal = 0;
        long slaveCheckPointTotal  = 0;
        for (String index : indexNames) {
            IndexNodes statForMaster = indexStatForMasterMap.get(index);
            IndexNodes statForSlave = indexStatForSlaveMap.get(index);
            AtomicLong totalCheckpointForMaster = esIndexService.syncGetTotalCheckpoint(index, statForMaster, null);
            AtomicLong totalCheckpointForSlave  = esIndexService.syncGetTotalCheckpoint(index, statForSlave, null);
            masterCheckPointTotal += totalCheckpointForMaster.get();
            slaveCheckPointTotal  += totalCheckpointForSlave.get();
        }

        masterAndSlaveCheckPointTuple.setV1(masterCheckPointTotal);
        masterAndSlaveCheckPointTuple.setV2(slaveCheckPointTotal);
        return masterAndSlaveCheckPointTuple;
    }

    @Override
    public Result<TemplateDCDRInfoVO> getTemplateDCDRInfoVO(Integer templateId) {
        TemplateDCDRInfoVO templateDCDRInfoVO = new TemplateDCDRInfoVO();
        IndexTemplateLogicWithPhyTemplates logicTemplateWithPhysicals = templateLogicService.getLogicTemplateWithPhysicalsById(templateId);
        IndexTemplatePhy slavePhyTemplate  = logicTemplateWithPhysicals.getSlavePhyTemplate();
        IndexTemplatePhy masterPhyTemplate = logicTemplateWithPhysicals.getMasterPhyTemplate();
        if (null == masterPhyTemplate) {
            return Result.buildFail(TEMPLATE_NO_EXIST);
        }
        // 1. 判断模板是否存在dcdr
        if (null == slavePhyTemplate) {
            templateDCDRInfoVO.setDcdrFlag(false);
            return Result.buildSuccWithTips(templateDCDRInfoVO, "模板未开启DCDR链路");
        }else {
            templateDCDRInfoVO.setDcdrFlag(syncExistTemplateDCDR(masterPhyTemplate.getId(), slavePhyTemplate.getCluster()));
        }

        if (!templateDCDRInfoVO.getDcdrFlag()) {return Result.buildSuccWithTips(templateDCDRInfoVO, "模板未开启DCDR链路");}

        // 2. 获取主从模板checkpoint信息
        Tuple<Long, Long> masterAndSlaveTemplateCheckPointTuple = new Tuple<>();
        try {
            masterAndSlaveTemplateCheckPointTuple = getMasterAndSlaveTemplateCheckPoint(templateId);
        } catch (Exception e) {
            LOGGER.error("class=TemplateDcdrManagerImpl||method=getTemplateDCDRInfoVO||templateId={}||msg=masterAndSlaveTemplateCheckPointTuple is empty", templateId, e);
        }
        templateDCDRInfoVO.setMasterClusterName(masterPhyTemplate.getCluster());
        templateDCDRInfoVO.setMasterTemplateCheckPoint(masterAndSlaveTemplateCheckPointTuple.getV1());

        templateDCDRInfoVO.setSlaveClusterName(slavePhyTemplate.getCluster());
        templateDCDRInfoVO.setSlaveTemplateCheckPoint(masterAndSlaveTemplateCheckPointTuple.getV2());

        long checkPointDiff = Math.abs(masterAndSlaveTemplateCheckPointTuple.getV1() - masterAndSlaveTemplateCheckPointTuple.getV2());
        templateDCDRInfoVO.setTemplateCheckPointDiff(checkPointDiff);
        return Result.buildSucc(templateDCDRInfoVO);
    }

    /**************************************** private method ****************************************************/
    private Result<Void> checkDCDRParam(Integer logicId) {
        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService
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

        if (templateLogicWithPhysical.getPhysicals().size() != 2) {
            return Result.buildParamIllegal("dcdr仅支持一主一从部署的模板");
        }

        return Result.buildSucc();
    }

    private Result<Void> checkDCDRParam(TemplatePhysicalDCDRDTO param) {
        if (param == null) {
            return Result.buildParamIllegal("dcdr参数不存在");
        }

        if (CollectionUtils.isEmpty(param.getPhysicalIds())) {
            return Result.buildParamIllegal("模板ID必须存在");
        }

        if (CollectionUtils.isEmpty(param.getReplicaClusters())) {
            return Result.buildParamIllegal("从集群必须存在");
        }

        for (int i = 0; i < param.getPhysicalIds().size(); ++i) {
            IndexTemplatePhy templatePhysical = templatePhyService.getTemplateById(param.getPhysicalIds().get(i));
            if (templatePhysical == null) {
                return Result.buildNotExist("物理模板不存在");
            }

            if (!clusterPhyService.isClusterExists(param.getReplicaClusters().get(i))) {
                return Result.buildNotExist("从集群不存在");
            }

            if (!isTemplateSrvOpen(templatePhysical.getCluster())) {
                return Result.buildParamIllegal("模板所在集群不支持dcdr");
            }

            if (!isTemplateSrvOpen(param.getReplicaClusters().get(i))) {
                return Result.buildParamIllegal("所选的从集群不支持dcdr");
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
     * @param logicId
     * @return
     */
    private TemplatePhysicalDCDRDTO createDCDRMeta(Integer logicId) {
        TemplatePhysicalDCDRDTO dcdrMeta = new TemplatePhysicalDCDRDTO();

        dcdrMeta.setPhysicalIds(new ArrayList<>());
        dcdrMeta.setReplicaClusters(new ArrayList<>());

        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService
                .getLogicTemplateWithPhysicalsById(logicId);

        List<IndexTemplatePhy> masterPhysicals = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
        for (IndexTemplatePhy indexTemplatePhysical : masterPhysicals) {
            IndexTemplatePhy slave = null;
            if (StringUtils.isNotBlank(indexTemplatePhysical.getGroupId())) {
                slave = templateLogicWithPhysical.fetchMasterSlave(indexTemplatePhysical.getGroupId());
            }

            if (null == slave) {
                slave = templateLogicWithPhysical.getSlavePhyTemplate();
            }

            if (slave != null) {
                dcdrMeta.getPhysicalIds().add(indexTemplatePhysical.getId());
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

    private Result<Void> changeDcdrConfig(String cluster, List<String> indices,
                                          boolean replicaIndex) throws ESOperateException {

        // 修改配置
        if (!syncDCDRSetting(cluster, indices, replicaIndex, 3)) {
            return Result.buildFail("修改" + cluster + "索引dcdr配置失败");
        }

        // reopen索引
        if (!esIndexService.reOpenIndex(cluster, indices, 3)) {
            return Result.buildFail("reOpen " + cluster + "索引失败");
        }

        return Result.buildSucc();
    }

    private Result<Void> deleteSrcDcdr(IndexTemplatePhy masterTemplate, IndexTemplatePhy slaveTemplate,

                                       List<String> matchNoVersionIndexNames, String operator) throws ESOperateException {

        TemplatePhysicalDCDRDTO dcdrDTO = new TemplatePhysicalDCDRDTO();

        dcdrDTO.setPhysicalIds(Arrays.asList(masterTemplate.getId()));
        dcdrDTO.setReplicaClusters(Arrays.asList(slaveTemplate.getCluster()));
        dcdrDTO.setDeleteIndexDcdr(false);

        Result<Void> delTemDCDRResult = deletePhyDcdr(dcdrDTO, operator);
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
    private Result<List<String>> executeDcdrForForce(Integer workTaskId,
                                                     DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail,
                                                     Long expectMasterPhysicalId,
                                                     int step,
                                                     IndexTemplatePhy masterTemplate,
                                                     IndexTemplatePhy slaveTemplate,
                                                     String operator) {
        List<String> matchIndexNames = templatePhyService.getMatchIndexNames(masterTemplate.getId());
        int templateId = switchDetail.getTemplateId().intValue();
        try {
            if (DCDR_SWITCH_STEP_1 == step) {
                // 修改dcdr索引配置 index.dcdr.replica_index = true/false
                // 然后还需要reopen索引，配置才能生效
                Result<Void> setSettingResult = Result.buildSucc();
                if(hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    setSettingResult = Result.buildFail(TASK_IS_CANCEL);
                }else {
                    Result<Void> changeSlaveDCDRConfig  = changeDcdrConfig(slaveTemplate.getCluster(), matchIndexNames, false);
                    changeDcdrConfig(masterTemplate.getCluster(), matchIndexNames, true);

                    if (changeSlaveDCDRConfig.failed()) {
                        setSettingResult = Result.buildFail(changeSlaveDCDRConfig.getMessage());
                    }
                }

                Result<List<String>> step1Result = buildStepMsg(DcdrSwithTypeEnum.FORCE.getCode(), setSettingResult, templateId,
                        expectMasterPhysicalId, DCDR_SWITCH_STEP_1, operator, switchDetail.getTaskProgressList());
                if (step1Result.failed()) {
                    return step1Result;
                }
                step++;
                sleep(1000L);
            }


            if (DCDR_SWITCH_STEP_2 == step) {
                // 主从角色切换
                Result<Void> switchMasterSlave;
                if(hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    switchMasterSlave = Result.buildFail(TASK_IS_CANCEL);
                } else {
                    if (hasFinishSwitchMasterSlave(switchDetail)) {
                        switchMasterSlave = Result.buildSucc();
                    }else {
                        switchMasterSlave = templatePhyManager.switchMasterSlave(masterTemplate.getLogicId(),
                                slaveTemplate.getId(), AriusUser.SYSTEM.getDesc());
                    }
                }

                Result<List<String>> step2Result = buildStepMsg(DcdrSwithTypeEnum.FORCE.getCode(), switchMasterSlave, templateId,
                        expectMasterPhysicalId, DCDR_SWITCH_STEP_2, operator, switchDetail.getTaskProgressList());
                if (step2Result.failed()) {
                    return step2Result;
                }
            }

        } catch (Exception e) {
            LOGGER.warn("method=executeDcdrForForce||templateId={}||errMsg={}", templateId, e.getMessage(), e);
            return buildStepMsg(DcdrSwithTypeEnum.FORCE.getCode(), Result.buildFail(e.getMessage()), templateId,
                    expectMasterPhysicalId, step, operator, switchDetail.getTaskProgressList());
        }

        return Result.buildSucc(switchDetail.getTaskProgressList());
    }

    /**
     * 执行
     * @param switchDetail
     * @param expectMasterPhysicalId         期望的模板Id
     * @param step                           切换起始步骤
     * @param masterTemplate                 主模板元数据信息
     * @param slaveTemplate                  从模板元数据信息
     * @param operator
     * @return
     */
    private Result<List<String>> executeDcdrForSmooth(Integer workTaskId, DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail,
                                                      Long expectMasterPhysicalId,
                                                      int step,
                                                      IndexTemplatePhy masterTemplate,
                                                      IndexTemplatePhy slaveTemplate,
                                                      String operator) {
        List<String> matchIndexNames = templatePhyService.getMatchIndexNames(masterTemplate.getId());

        int templateId = switchDetail.getTemplateId().intValue();

        try {
            /**
             * 注意这里的if不能使用else if代替，这里的代码需要顺序执行下去
             */
            if(DCDR_SWITCH_STEP_1 == step){
                Result<Void> stopMasterIndexResult;
                if(hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    stopMasterIndexResult = Result.buildFail(TASK_IS_CANCEL);
                }else {
                    // 停止索引写入
                    stopMasterIndexResult = templateLimitManager.blockIndexWrite(masterTemplate.getCluster(),
                            matchIndexNames, true);
                }

                Result<List<String>> step1Result = buildStepMsg(DcdrSwithTypeEnum.SMOOTH.getCode(), stopMasterIndexResult,
                        templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_1, operator, switchDetail.getTaskProgressList());
                if (step1Result.failed()) {
                    return step1Result;
                }
                step++;
                sleep(1000L);
            }

            if(DCDR_SWITCH_STEP_2 == step){
                // 确保主从数据同步完成
                Result<Void> checkDataResult = Result.buildSucc();
                if(hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    checkDataResult = Result.buildFail(TASK_IS_CANCEL);
                }else {
                    if (!esIndexService.ensureDateSame(masterTemplate.getCluster(), slaveTemplate.getCluster(), matchIndexNames)) {
                        checkDataResult = Result.buildFail("校验索引数据不一致!");
                        // 恢复实时数据写入
                        Result<Void> sttartMasterIndexResult = templateLimitManager.blockIndexWrite(masterTemplate.getCluster(), matchIndexNames, false);
                        if (sttartMasterIndexResult.failed()) {
                            checkDataResult.setMessage(checkDataResult.getMessage() + "|" + sttartMasterIndexResult.getMessage());
                        }
                    }
                }

                Result<List<String>> step2Result = buildStepMsg(DcdrSwithTypeEnum.SMOOTH.getCode(), checkDataResult,
                        templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_2, operator, switchDetail.getTaskProgressList());
                if (step2Result.failed()) {
                    return step2Result;
                }
                step++;
                sleep(1000L);
            }

            if(DCDR_SWITCH_STEP_3 == step){
                Result<Void> deleteSrcDcdrResult;
                // 删除dcdr链路（模板和索引）
                if(hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    deleteSrcDcdrResult = Result.buildFail(TASK_IS_CANCEL);
                }else {
                    deleteSrcDcdrResult = deleteSrcDcdr(masterTemplate, slaveTemplate, matchIndexNames, AriusUser.SYSTEM.getDesc());
                }

                Result<List<String>> step3Result = buildStepMsg(DcdrSwithTypeEnum.SMOOTH.getCode(), deleteSrcDcdrResult,
                        templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_3, operator, switchDetail.getTaskProgressList());
                if (step3Result.failed()) {
                    return step3Result;
                }
                step++;
                sleep(1000L);
            }

            if(DCDR_SWITCH_STEP_4 == step){
                Result<Void> copyResult = Result.buildSucc();

                if(hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    copyResult = Result.buildFail(TASK_IS_CANCEL);
                }else {
                    // 拷贝主模板到从模板
                    if (!esTemplateService.syncCopyMappingAndAlias(masterTemplate.getCluster(),
                            masterTemplate.getName(), slaveTemplate.getCluster(), slaveTemplate.getName(), 3)) {
                        copyResult = Result.buildFail("拷贝模板失败");
                    }
                }

                Result<List<String>> step4Result = buildStepMsg(DcdrSwithTypeEnum.SMOOTH.getCode(), copyResult,
                        templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_4, operator, switchDetail.getTaskProgressList());
                if (step4Result.failed()) {
                    return step4Result;
                }
                step++;
                sleep(1000L);
            }

            if(DCDR_SWITCH_STEP_5 == step){
                // 修改dcdr索引配置 index.dcdr.replica_index = true/false
                // 然后还需要reopen索引，配置才能生效
                Result<Void> setSettingResult = Result.buildSucc();
                if(hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    setSettingResult = Result.buildFail(TASK_IS_CANCEL);
                }else {
                    Result<Void> changeMasterDCDRConfig = changeDcdrConfig(masterTemplate.getCluster(), matchIndexNames, true);
                    Result<Void> changeSlaveDCDRConfig  = changeDcdrConfig(slaveTemplate.getCluster(), matchIndexNames, false);

                    if (changeMasterDCDRConfig.failed() || changeSlaveDCDRConfig.failed()) {
                        setSettingResult = Result.buildFail(changeMasterDCDRConfig.getMessage() + "|" + changeSlaveDCDRConfig.getMessage());
                    }
                }

                Result<List<String>> step5Result = buildStepMsg(DcdrSwithTypeEnum.SMOOTH.getCode(), setSettingResult,
                        templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_5, operator, switchDetail.getTaskProgressList());
                if (step5Result.failed()) {
                    return step5Result;
                }
                step++;
                sleep(1000L);
            }

            if(DCDR_SWITCH_STEP_6 == step) {
                // 停止索引写入
                Result<Void> stopSlaveIndexResult;
                if(hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    stopSlaveIndexResult = Result.buildFail(TASK_IS_CANCEL);
                }else {
                    stopSlaveIndexResult = templateLimitManager.blockIndexWrite(slaveTemplate.getCluster(), matchIndexNames, true);
                }
                Result<List<String>> step6Result = buildStepMsg(DcdrSwithTypeEnum.SMOOTH.getCode(), stopSlaveIndexResult,
                        templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_6, operator, switchDetail.getTaskProgressList());
                if (step6Result.failed()) {
                    return step6Result;
                }
                step++;
                sleep(1000L);
            }

            if(DCDR_SWITCH_STEP_7 == step){
                Result<Void> createDCDRResult;
                // 创建新的主从链路
                if(hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    createDCDRResult = Result.buildFail(TASK_IS_CANCEL);
                }else {
                    createDCDRResult = createPhyDcdr(buildCreateDCDRParam(slaveTemplate, masterTemplate), AriusUser.SYSTEM.getDesc());
                }
                Result<List<String>> step7Result = buildStepMsg(DcdrSwithTypeEnum.SMOOTH.getCode(), createDCDRResult,
                        templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_7, operator, switchDetail.getTaskProgressList());
                if (step7Result.failed()) {
                    return step7Result;
                }
                step++;
                sleep(1000L);
            }

            if(DCDR_SWITCH_STEP_8 == step){
                // 恢复实时写入
                Result<Void> startIndexResult = Result.buildSucc();
                if(hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    startIndexResult = Result.buildFail(TASK_IS_CANCEL);
                }else {
                    Result<Void> startMasterIndexResult = templateLimitManager.blockIndexWrite(masterTemplate.getCluster(),
                            matchIndexNames, false);
                    Result<Void> startSlaveIndexResult = templateLimitManager.blockIndexWrite(slaveTemplate.getCluster(),
                            matchIndexNames, false);

                    if (startMasterIndexResult.failed() || startSlaveIndexResult.failed()) {
                        startIndexResult = Result.buildFail(startMasterIndexResult.getMessage() + "|" + startSlaveIndexResult.getMessage());
                    }
                }

                Result<List<String>> step8Result = buildStepMsg(DcdrSwithTypeEnum.SMOOTH.getCode(), startIndexResult,
                        templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_8, operator, switchDetail.getTaskProgressList());
                if (step8Result.failed()) {
                    return step8Result;
                }
                step++;
                sleep(1000L);
            }

            if(DCDR_SWITCH_STEP_9 == step){
                // 主从角色切换
                Result<Void> switchMasterSlave;
                if(hasCancelSubTask(workTaskId, switchDetail.getTemplateId())) {
                    switchMasterSlave = Result.buildFail(TASK_IS_CANCEL);
                }else {
                    if (hasFinishSwitchMasterSlave(switchDetail)) {
                        switchMasterSlave = Result.buildSucc();
                    }else {
                        switchMasterSlave = templatePhyManager.switchMasterSlave(masterTemplate.getLogicId(),
                                slaveTemplate.getId(), AriusUser.SYSTEM.getDesc());
                    }
                }

                Result<List<String>> step9Result = buildStepMsg(DcdrSwithTypeEnum.SMOOTH.getCode(), switchMasterSlave,
                        templateId, expectMasterPhysicalId, DCDR_SWITCH_STEP_9, operator, switchDetail.getTaskProgressList());
                if (step9Result.failed()) {
                    return step9Result;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("method=executeDcdrForSmooth||templateId={}||errMsg={}", templateId, e.getMessage(), e);
            return buildStepMsg(DcdrSwithTypeEnum.SMOOTH.getCode(), Result.buildFail(e.getMessage()),
                    templateId, expectMasterPhysicalId, step,
                    operator, switchDetail.getTaskProgressList());
        }
        return Result.buildSucc(switchDetail.getTaskProgressList());
    }

    /**
     * 是否已经成功切换
     * @param switchDetail
     * @return
     */
    private boolean hasFinishSwitchMasterSlave(DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail) {
        IndexTemplateLogicWithPhyTemplates logicTemplateWithPhysicals = templateLogicService.getLogicTemplateWithPhysicalsById(
                switchDetail.getTemplateId().intValue());
        IndexTemplatePhy masterTemplate    =  logicTemplateWithPhysicals.getMasterPhyTemplate();
        IndexTemplatePhy slaveTemplate     =  logicTemplateWithPhysicals.getSlavePhyTemplate();

        String masterTemplateClusterFromDB = masterTemplate.getCluster();
        String slaveTemplateClusterFromDB  = slaveTemplate.getCluster();

        String masterCluster = switchDetail.getMasterCluster();
        String slaveCluster  = switchDetail.getSlaveCluster();
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
     * 单个dcdr子任务是否取消
     * @param workTaskId
     * @param templateId
     * @return
     */
    private boolean hasCancelSubTask(Integer workTaskId, Long templateId) {
        Result<WorkTask> workTaskResult = workTaskManager.getById(workTaskId);
        if (null == workTaskResult || null == workTaskResult.getData()) {
            LOGGER.error("method=submitTask||workTaskId={}||msg= workTaskId is empty", workTaskId);
            return false;
        }
        WorkTask workTask = workTaskResult.getData();

        DCDRTasksDetail dcdrTasksDetail = JSON.parseObject(workTask.getExpandData(), DCDRTasksDetail.class);
        List<DCDRSingleTemplateMasterSlaveSwitchDetail> switchDetailList = dcdrTasksDetail.getDcdrSingleTemplateMasterSlaveSwitchDetailList();
        if (CollectionUtils.isEmpty(switchDetailList)) {return false;}

        for (DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail : switchDetailList) {
            if (templateId.equals(switchDetail.getTemplateId())
                    && DcdrStatusEnum.CANCELLED.getCode().equals(switchDetail.getTaskStatus())) {
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
                                              Long expectMasterPhysicalId, int localStep,
                                              String operator, List<String> stepMsgList) {
        if (DcdrSwithTypeEnum.FORCE.getCode().equals(swithType)) {
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

        if (DcdrSwithTypeEnum.SMOOTH.getCode().equals(swithType)) {
            if (result.failed()) {
                buildSwitchStepMsgForFailedList(stepMsgList, result, localStep, localStep);
                return Result.buildFail(stepMsgList);
            }
            buildSwitchStepMsgForSuccessList(stepMsgList, localStep, localStep);
        }

        LOGGER.info(
            "method=dcdrSwitchMasterSlave||logicId={}||operator={}||expectMasterPhysicalId={}||msg=step {} succ",
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

    private Result<Void> processDcdrTask(Integer logicId, Result<Void> dcdrResult, int step) {
        Result<WorkTask> result = workTaskManager.getLatestTask(String.valueOf(logicId), WorkTaskTypeEnum.TEMPLATE_DCDR.getType());
        if (result.failed()) {
            return Result.buildFrom(result);
        }
        WorkTaskProcessDTO processDTO = new WorkTaskProcessDTO();
        processDTO.setStatus(dcdrResult.success() ? WorkTaskStatusEnum.SUCCESS.getStatus() : WorkTaskStatusEnum.FAILED.getStatus());
        processDTO.setTaskId(result.getData().getId());
        processDTO.setTaskProgress(step);

        if (dcdrResult.failed()) {
            DCDRTaskDetail detail = new DCDRTaskDetail();
            detail.setComment(result.getMessage());
            processDTO.setExpandData(JSON.toJSONString(detail));
        }
        return workTaskManager.processTask(processDTO);
    }

    private TemplatePhysicalCopyDTO buildTemplatePhysicalCopyDTO(Integer templateId, String targetCluster, String rack) {
        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService.getLogicTemplateWithPhysicalsById(templateId);

        TemplatePhysicalCopyDTO templatePhysicalCopyDTO = new TemplatePhysicalCopyDTO();
        IndexTemplatePhy masterPhyTemplate = templateLogicWithPhysical.getMasterPhyTemplate();
        if (null == masterPhyTemplate) {
            return null;
        }

        templatePhysicalCopyDTO.setCluster(targetCluster);
        templatePhysicalCopyDTO.setPhysicalId(masterPhyTemplate.getId());
        if (Strings.isNullOrEmpty(rack)) {
            templatePhysicalCopyDTO.setRack(masterPhyTemplate.getRack());
        } else {
            templatePhysicalCopyDTO.setRack(rack);
        }
        templatePhysicalCopyDTO.setShard(masterPhyTemplate.getShard());
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

    private Result<Void> checkValidForDcdrSwitch(Integer logicId, Long expectMasterPhysicalId, int step, String operator){
        List<IndexTemplatePhy> templatePhysicals = templatePhyService.getTemplateByLogicId(logicId);
        if (CollectionUtils.isEmpty(templatePhysicals)) {
            return Result.buildNotExist("逻辑模板有没有部署物理模板");
        }

        if (templatePhysicals.size() > 2) {
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

        if (step < TemplateDCDRStepEnum.STEP_3.getStep()
                && !syncExistTemplateDCDR(masterTemplate.getId(), slaveTemplate.getCluster())) {
            //不具备DCDR,主从切换
            if (step == TemplateDCDRStepEnum.STEP_1.getStep()) {
                Result<Void> result = templatePhyManager.switchMasterSlave(masterTemplate.getLogicId(),
                        slaveTemplate.getId(), operator);
                if (result.success()) {
                    return Result.buildSuccWithMsg("switch");
                }
                return result;
            }
            return Result.buildParamIllegal("DCDR链路不存在");
        }

        return Result.buildSucc();
    }

    private Result<Void> batchCheckValidForDcdrSwitch(List<Long> templateIdList, String operator) {
        if (CollectionUtils.isEmpty(templateIdList)) {
            return Result.buildParamIllegal("模板id为空");
        }
        for (Long templateId : templateIdList) {
            IndexTemplateLogicWithPhyTemplates logicTemplateWithPhysicals = templateLogicService.getLogicTemplateWithPhysicalsById(
                    templateId.intValue());

            IndexTemplatePhy slaveTemplate   =  logicTemplateWithPhysicals.getSlavePhyTemplate();
            if (null == slaveTemplate) {
                return Result.buildFail(String.format("模板Id[%s]不存在从模板, 无法进行dcdr主从切换", templateId));
            }

            Result<Void> checkValidForDcdrSwitchResult = checkValidForDcdrSwitch(templateId.intValue(), slaveTemplate.getId(), 1, operator);
            if (checkValidForDcdrSwitchResult.failed()) {
                return checkValidForDcdrSwitchResult;
            }
        }

        return Result.buildSucc();
    }

    @NotNull
    private DCDRTasksDetail buildDcdrTasksDetail(DCDRMasterSlaveSwitchDTO dcdrMasterSlaveSwitchDTO, List<Long> templateIdList) {
        DCDRTasksDetail dcdrTasksDetail = new DCDRTasksDetail();
        List<DCDRSingleTemplateMasterSlaveSwitchDetail> dcdrSingleTemplateMasterSlaveSwitchDetailList = new ArrayList<>();
        //1. 设置dcdr详情信息
        for (Long templateId : templateIdList) {
            DCDRSingleTemplateMasterSlaveSwitchDetail singleSwitchDetail = new DCDRSingleTemplateMasterSlaveSwitchDetail();

            IndexTemplateLogic logicTemplate = templateLogicService.getLogicTemplateById(templateId.intValue());
            singleSwitchDetail.editTaskTitle(logicTemplate.getName());

            //1.1 设置切换类型 强切、平滑
            singleSwitchDetail.setSwitchType(dcdrMasterSlaveSwitchDTO.getType());

            //1.2 构建dcdr主从切换基础信息
            singleSwitchDetail.setTemplateId(templateId);

            IndexTemplateLogicWithPhyTemplates  logicTemplateWithPhysicals = templateLogicService.getLogicTemplateWithPhysicalsById(templateId.intValue());
            IndexTemplatePhy masterTemplate  =  logicTemplateWithPhysicals.getMasterPhyTemplate();
            IndexTemplatePhy slaveTemplate   =  logicTemplateWithPhysicals.getSlavePhyTemplate();
            singleSwitchDetail.setMasterCluster(slaveTemplate.getCluster());
            singleSwitchDetail.setSlaveCluster(masterTemplate.getCluster());
            singleSwitchDetail.setDeleteDcdrChannelFlag(false);
            singleSwitchDetail.setCreateTime(new Date());

            //1.3 构建dcdr主从切换初始化任务进度信息
            List<String> stepMsgList = new ArrayList<>();
            if (DcdrSwithTypeEnum.SMOOTH.getCode().equals(singleSwitchDetail.getSwitchType())) {
                stepMsgList = buildInitTaskProgressInfo(DCDR_SWITCH_STEP_ARR_SMOOTH);
            }

            if (DcdrSwithTypeEnum.FORCE.getCode().equals(singleSwitchDetail.getSwitchType())) {
                stepMsgList = buildInitTaskProgressInfo(DCDR_SWITCH_STEP_ARR_FORCE);
            }
            singleSwitchDetail.setTaskProgressList(stepMsgList);

            dcdrSingleTemplateMasterSlaveSwitchDetailList.add(singleSwitchDetail);
        }

        //2. 分批次执行
        int tempDcdrConcurrent = dcdrConcurrent;
        Collections.shuffle(dcdrSingleTemplateMasterSlaveSwitchDetailList);
        for (DCDRSingleTemplateMasterSlaveSwitchDetail singleTemplateMasterSlaveSwitchDetail : dcdrSingleTemplateMasterSlaveSwitchDetailList) {
            if (tempDcdrConcurrent > 0) {
                singleTemplateMasterSlaveSwitchDetail.setTaskStatus(DcdrStatusEnum.RUNNING.getCode());
                tempDcdrConcurrent--;
            }else singleTemplateMasterSlaveSwitchDetail.setTaskStatus(DcdrStatusEnum.WAIT.getCode());
        }  

        dcdrTasksDetail.setDcdrSingleTemplateMasterSlaveSwitchDetailList(dcdrSingleTemplateMasterSlaveSwitchDetailList);
        return dcdrTasksDetail;
    }

    /**
     * 刷新dcdr链路状态
     * @param taskId                 dcdr主从切换任务id
     * @param step                   是否需要根据起始执行步骤往后执行
     * @param operator
     *
     */
    private void doRefreshDcdrChannelsState(Integer taskId, Integer step, String operator) {
        Result<WorkTask> taskForDcdrSwitchResult = workTaskManager.getById(taskId);
        if (taskForDcdrSwitchResult.failed()) {
            LOGGER.error("method=asyncRefreshDcdrChannelState||taskId={}||msg=taskId is empty", taskId);
            return;
        }

        WorkTask taskForDcdrSwitch = taskForDcdrSwitchResult.getData();

        DCDRTasksDetail dcdrTasksDetail = ConvertUtil.str2ObjByJson(taskForDcdrSwitch.getExpandData(), DCDRTasksDetail.class);
        if (null == dcdrTasksDetail) {return;}

        if (hasSkipForTask(taskForDcdrSwitch, dcdrTasksDetail)) {return;}

        List<DCDRSingleTemplateMasterSlaveSwitchDetail> singleSwitchDetailList = dcdrTasksDetail.getDcdrSingleTemplateMasterSlaveSwitchDetailList();
        if (CollectionUtils.isEmpty(singleSwitchDetailList)) {return;}

        for (DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail : singleSwitchDetailList) {
            //前置过滤处理
            if (hasSkipForSingleDcdrRefresh(switchDetail)) { continue; }

            //并发去刷新多个模板状态
            BATCH_DCDR_FUTURE_UTIL.runnableTask(() -> {
                try {
                    IndexTemplateLogicWithPhyTemplates logicTemplateWithPhysicals = templateLogicService.getLogicTemplateWithPhysicalsById(
                            switchDetail.getTemplateId().intValue());
                    IndexTemplatePhy masterTemplate    =  logicTemplateWithPhysicals.getMasterPhyTemplate();
                    IndexTemplatePhy slaveTemplate     =  logicTemplateWithPhysicals.getSlavePhyTemplate();

                    if (null == slaveTemplate) {return;}

                    // 防止并发问题带来的逆向主从切换
                    if (switchDetail.getMasterCluster().equals(masterTemplate.getCluster())
                            && switchDetail.getSlaveCluster().equals(slaveTemplate.getCluster())) { return; }

                    syncRefreshStatus(taskId ,step, switchDetail, masterTemplate, slaveTemplate, operator);

                    switchDetail.setUpdateTime(new Date());
                } catch (Exception e) {
                    LOGGER.error("method=doRefreshDcdrChannelsState||taskId={}||templateId={}||msg={}",
                            taskForDcdrSwitch.getId(), switchDetail.getTemplateId(), e.getMessage(),e);
                }

            });
        }
        BATCH_DCDR_FUTURE_UTIL.waitExecute();

        try {
            saveNewestWorkTaskStatusToDB(taskForDcdrSwitch, dcdrTasksDetail);
        } catch (Exception e) {
            LOGGER.error("method=doRefreshDcdrChannelsState||taskId={}||msg=failed to save newest workTask to db",
                taskForDcdrSwitch.getId(), e);
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
    private void syncRefreshStatus(Integer taskId, Integer step,
                                   DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail,
                                   IndexTemplatePhy masterTemplate,
                                   IndexTemplatePhy slaveTemplate,
                                   String operator) {

        Result<List<String>>     executeDcdrResult = Result.buildSucc();
        // 平滑切换刷新状态
        if (DcdrSwithTypeEnum.SMOOTH.getCode().equals(switchDetail.getSwitchType())) {
            executeDcdrResult =  executeDcdrForSmooth(taskId ,switchDetail, slaveTemplate.getId(), step, masterTemplate, slaveTemplate, operator);
        }

        // 强制切换刷新状态
        if (DcdrSwithTypeEnum.FORCE.getCode().equals(switchDetail.getSwitchType())) {
            executeDcdrResult =  executeDcdrForForce(taskId ,switchDetail, slaveTemplate.getId(), step, masterTemplate, slaveTemplate, operator);
        }

        // 最新状态
        if (executeDcdrResult.failed()) {
            switchDetail.setTaskStatus(DcdrStatusEnum.FAILED.getCode());
        }
        if (executeDcdrResult.success()) {
            switchDetail.setTaskStatus(DcdrStatusEnum.SUCCESS.getCode());
        }
    }

    /**
     * 更新db中 dcdr任务状态
     * @param taskForDcdrSwitch      原任务状态信息
     * @param dcdrTasksDetail        新具体状态信息
     */
    private void saveNewestWorkTaskStatusToDB(WorkTask taskForDcdrSwitch, DCDRTasksDetail dcdrTasksDetail) {
        //根据多个detail task 来计算状态
        dcdrTasksDetail.calculateProcess();

        //是否需要设置下一批dcdr模板切换任务的状态为running
        setNextBatchDcdrTaskDetailStateToRunning(dcdrTasksDetail);

        taskForDcdrSwitch.setExpandData(ConvertUtil.obj2Json(dcdrTasksDetail));

        if (DcdrStatusEnum.SUCCESS.getCode().equals(dcdrTasksDetail.getState())) {
            taskForDcdrSwitch.setStatus(WorkTaskStatusEnum.SUCCESS.getStatus());
            //成功删除dcdr链路
            deleteDcdrChannelForSuccForceSwitch(taskForDcdrSwitch, dcdrTasksDetail);
        }

        if (DcdrStatusEnum.FAILED.getCode().equals(dcdrTasksDetail.getState())) {
            taskForDcdrSwitch.setStatus(WorkTaskStatusEnum.FAILED.getStatus());
        }
        if (DcdrStatusEnum.CANCELLED.getCode().equals(dcdrTasksDetail.getState())) {
            taskForDcdrSwitch.setStatus(WorkTaskStatusEnum.CANCEL.getStatus());
        }
        if (DcdrStatusEnum.RUNNING.getCode().equals(dcdrTasksDetail.getState())) {
            taskForDcdrSwitch.setStatus(WorkTaskStatusEnum.RUNNING.getStatus());
        }

        // 解决分布式部署由于时序不一致带来更新不一致的问题
        Result<WorkTask> workTaskResult = workTaskManager.getById(taskForDcdrSwitch.getId());
        if (null != workTaskResult.getData()
            && WorkTaskStatusEnum.SUCCESS.getStatus().equals(workTaskResult.getData().getStatus())) {
            return;
        }

        // 这里由于多线程更新，可能会出现不可重复读的问题，所以这里加上了一个判断
        // 临时打个补丁，等待下一个版本ecm 重构
        if (workTaskResult.getData().getUpdateTime().after(taskForDcdrSwitch.getUpdateTime())) {
            return;
        }
        taskForDcdrSwitch.setUpdateTime(new Date());

        workTaskManager.updateTask(taskForDcdrSwitch);
    }

    /**
     *  强切成功删除dcdr链路
     *
     * @param taskForDcdrSwitch   任务信息
     * @param dcdrTasksDetail     dcdr任务信息
     */
    private void deleteDcdrChannelForSuccForceSwitch(WorkTask taskForDcdrSwitch, DCDRTasksDetail dcdrTasksDetail) {
        for (DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail : dcdrTasksDetail.getDcdrSingleTemplateMasterSlaveSwitchDetailList()) {
            if (DcdrSwithTypeEnum.FORCE.getCode().equals(switchDetail.getSwitchType())) {
                try {
                    Result<Void> deleteDcdrResult = deleteDcdr(switchDetail.getTemplateId().intValue(), AriusUser.SYSTEM.getDesc());
                    if (deleteDcdrResult.failed()) {
                        LOGGER.error("method=deleteDcdrChannelForSuccForceSwitch||taskId={}||msg=failed to deleteDcdr for force switch",
                                taskForDcdrSwitch.getId());
                        switchDetail.setDeleteDcdrChannelFlag(false);
                    }else {
                        switchDetail.setDeleteDcdrChannelFlag(true);
                    }
                } catch (ESOperateException e) {
                    LOGGER.error("method=deleteDcdrChannelForSuccForceSwitch||taskId={}||msg=failed to deleteDcdr for force switch",
                            taskForDcdrSwitch.getId(), e);
                    switchDetail.setDeleteDcdrChannelFlag(false);
                }
            }
        }
    }

    /**
     * 是否需要更新下一批dcdr模板切换任务的状态为running
     * @param dcdrTasksDetail
     */
    private void setNextBatchDcdrTaskDetailStateToRunning(DCDRTasksDetail dcdrTasksDetail) {
        List<DCDRSingleTemplateMasterSlaveSwitchDetail> slaveSwitchDetailList = dcdrTasksDetail.getDcdrSingleTemplateMasterSlaveSwitchDetailList();
        
        // 按任务状态分组
        Map<Integer, List<DCDRSingleTemplateMasterSlaveSwitchDetail>> status2SwitchDetailMap = ConvertUtil.list2MapOfList(slaveSwitchDetailList,
                DCDRSingleTemplateMasterSlaveSwitchDetail::getTaskStatus,
                DCDRSingleTemplateMasterSlaveSwitchDetail -> DCDRSingleTemplateMasterSlaveSwitchDetail);

        //获取任务成功数
        List<DCDRSingleTemplateMasterSlaveSwitchDetail> runningSwitchDetailList = status2SwitchDetailMap.get(DcdrStatusEnum.RUNNING.getCode());
        int runingTaskSize = CollectionUtils.isNotEmpty(runningSwitchDetailList) ? runningSwitchDetailList.size() : 0;

        //获取任务失败数
        List<DCDRSingleTemplateMasterSlaveSwitchDetail> failedSwitchDetailList = status2SwitchDetailMap.get(DcdrStatusEnum.FAILED.getCode());
        int failedTaskSize = CollectionUtils.isNotEmpty(failedSwitchDetailList) ? failedSwitchDetailList.size() : 0;

        // 运行数小于 并发数, 并且在失败数上限
        if (hasSetNextBatch(runingTaskSize, failedTaskSize)) {
            List<DCDRSingleTemplateMasterSlaveSwitchDetail> waitingSwitchDetailList = status2SwitchDetailMap.get(DcdrStatusEnum.WAIT.getCode());

            if (CollectionUtils.isEmpty(waitingSwitchDetailList)) { return; }

            int tempDcdrConcurrent = dcdrConcurrent;
            for (DCDRSingleTemplateMasterSlaveSwitchDetail waitingSwitchDetail : waitingSwitchDetailList) {
                if (tempDcdrConcurrent > 0) {
                    waitingSwitchDetail.setTaskStatus(DcdrStatusEnum.RUNNING.getCode());
                    tempDcdrConcurrent--;
                }
            }
        }
    }

    /**
     * 是否需要跳过 DcdrChannel 刷新流程
     * @param dcdrSingleTemplateMasterSlaveSwitchDetail
     * @return
     */
    private boolean hasSkipForSingleDcdrRefresh(DCDRSingleTemplateMasterSlaveSwitchDetail dcdrSingleTemplateMasterSlaveSwitchDetail) {
        return DcdrStatusEnum.CANCELLED.getCode().equals(dcdrSingleTemplateMasterSlaveSwitchDetail.getTaskStatus())
                || DcdrStatusEnum.SUCCESS.getCode().equals(dcdrSingleTemplateMasterSlaveSwitchDetail.getTaskStatus())
                || DcdrStatusEnum.WAIT.getCode().equals(dcdrSingleTemplateMasterSlaveSwitchDetail.getTaskStatus())
                || DcdrStatusEnum.FAILED.getCode().equals(dcdrSingleTemplateMasterSlaveSwitchDetail.getTaskStatus());
    }

    /**
     * 是否需要跳过 任务刷新
     * @param taskForDcdrSwitch
     * @param dcdrTasksDetail
     * @return
     */
    private boolean hasSkipForTask(WorkTask taskForDcdrSwitch, DCDRTasksDetail dcdrTasksDetail) {
        List<DCDRSingleTemplateMasterSlaveSwitchDetail> switchDetailList = dcdrTasksDetail.getDcdrSingleTemplateMasterSlaveSwitchDetailList();
        List<Integer> runningTaskStatusList = switchDetailList.stream()
                .filter(switchDetail -> DcdrStatusEnum.RUNNING.getCode().equals(switchDetail.getTaskStatus()))
                .map(DCDRSingleTemplateMasterSlaveSwitchDetail::getTaskStatus)
                .collect(Collectors.toList());

        return WorkTaskStatusEnum.CANCEL.getStatus().equals(taskForDcdrSwitch.getStatus())
                || WorkTaskStatusEnum.SUCCESS.getStatus().equals(taskForDcdrSwitch.getStatus())
                || runningTaskStatusList.isEmpty();
    }

    /**
     * 初始化主从切换任务信息
     * @param templateId
     * @param dcdrTasksDetail
     */
    private void initSwitchTaskInfo(Integer templateId, DCDRTasksDetail dcdrTasksDetail) {
        if (null != templateId) {
            List<DCDRSingleTemplateMasterSlaveSwitchDetail> switchDetailList = dcdrTasksDetail.getDcdrSingleTemplateMasterSlaveSwitchDetailList();
            for (DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail : switchDetailList) {
                if (templateId.equals(switchDetail.getTemplateId().intValue())) {
                    switchDetail.setTaskStatus(DcdrStatusEnum.RUNNING.getCode());
                    if (DcdrSwithTypeEnum.SMOOTH.getCode().equals(switchDetail.getSwitchType())) {
                        switchDetail.setTaskProgressList(buildInitTaskProgressInfo(DCDR_SWITCH_STEP_ARR_SMOOTH));
                    }

                    if (DcdrSwithTypeEnum.FORCE.getCode().equals(switchDetail.getSwitchType())) {
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
        return dcdrConcurrent > runingTaskSize
                && (runingTaskSize / dcdrConcurrent) == 0
                && dcdrFaultTolerant >= failedTaskSize;
    }
}
