package com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.limit.TemplateLimitManager;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.WorkTaskDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.WorkTaskProcessDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplatePhysicalDCDRDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.task.WorkTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.task.WorkTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.template.TemplateDCDRStepEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.WorkTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.DCDRTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.BatchProcessor;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateLabelService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESDCDRDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.dcdr.DCDRTemplate;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.TEMPLATE;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.CREATE_DCDR;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.DELETE_DCDR;
import static com.didichuxing.datachannel.arius.admin.client.constant.template.TemplateDeployRoleEnum.MASTER;
import static com.didichuxing.datachannel.arius.admin.client.constant.template.TemplateDeployRoleEnum.SLAVE;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_DCDR;

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

    private static final String[]      DCDR_SWITCH_STEP_ARR      = new String[] { TemplateDCDRStepEnum.STEP_1
        .getValue(), TemplateDCDRStepEnum.STEP_2.getValue(), TemplateDCDRStepEnum.STEP_3.getValue(),
                                                                                  TemplateDCDRStepEnum.STEP_4
                                                                                      .getValue(),
                                                                                  TemplateDCDRStepEnum.STEP_5
                                                                                      .getValue(),
                                                                                  TemplateDCDRStepEnum.STEP_6
                                                                                      .getValue(),
                                                                                  TemplateDCDRStepEnum.STEP_7
                                                                                      .getValue(),
                                                                                  TemplateDCDRStepEnum.STEP_8
                                                                                      .getValue(),
                                                                                  TemplateDCDRStepEnum.STEP_9
                                                                                      .getValue() };

    private static final String        DCDR_SWITCH_RETRY_FORMAT  = "请回平台重试";

    @Autowired
    private ESDCDRDAO                   esdcdrDAO;

    @Autowired
    private ESIndexService              esIndexService;

    @Autowired
    private ESTemplateService           esTemplateService;

    @Autowired
    private TemplateLimitManager templateLimitManager;

    @Autowired
    private TemplateLabelService        templateLabelService;

    @Autowired
    private WorkTaskManager workTaskManager;

    @Override
    public TemplateServiceEnum templateService() {
        return TEMPLATE_DCDR;
    }

    /**
     * 创建dcdr
     *
     * @param logicId  模板ID
     * @param operator 操作人
     * @return result
     * @throws ESOperateException
     */
    @Override
    public Result createDcdr(Integer logicId, String operator) throws ESOperateException {
        Result checkResult = checkDCDRParam(logicId);
        if (checkResult.failed()) {
            return checkResult;
        }

        Result result = createPhyDcdr(createDCDRMeta(logicId), operator);
        if (result.success()) {
            templateLabelService.updateTemplateLabel(logicId,
                Sets.newHashSet(TemplateLabelService.TEMPLATE_HAVE_DCDR), null, operator);
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
    public Result deleteDcdr(Integer logicId, String operator) throws ESOperateException {
        Result checkResult = checkDCDRParam(logicId);

        if (checkResult.failed()) {
            return checkResult;
        }

        TemplatePhysicalDCDRDTO dcdrdto = createDCDRMeta(logicId);

        Result result = deletePhyDcdr(dcdrdto, operator);
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
    public Result createPhyDcdr(TemplatePhysicalDCDRDTO param, String operator) throws ESOperateException {

        Result checkDCDRResult = checkDCDRParam(param);

        if (checkDCDRResult.failed()) {
            return checkDCDRResult;
        }

        for (int i = 0; i < param.getPhysicalIds().size(); ++i) {
            IndexTemplatePhy templatePhysicalPO = templatePhyService
                .getTemplateById(param.getPhysicalIds().get(i));

            // 判断集群与从集群是否配置了
            if (!esClusterPhyService.ensureDcdrRemoteCluster(templatePhysicalPO.getCluster(),
                param.getReplicaClusters().get(i))) {
                return Result.buildFail("创建remote-cluster失败");
            }

            if (!syncCreateTemplateDCDR(param.getPhysicalIds().get(i), param.getReplicaClusters().get(i), 3)) {

                operateRecordService.save(TEMPLATE, CREATE_DCDR, templatePhysicalPO.getLogicId(),
                    "replicaCluster:" + param.getReplicaClusters(), operator);

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
    public Result deletePhyDcdr(TemplatePhysicalDCDRDTO param, String operator) throws ESOperateException {
        Result checkDCDRResult = checkDCDRParam(param);

        if (checkDCDRResult.failed()) {
            return checkDCDRResult;
        }

        for (int i = 0; i < param.getPhysicalIds().size(); ++i) {
            if (syncDeleteTemplateDCDR(param.getPhysicalIds().get(i), param.getReplicaClusters().get(i), 3)) {
                IndexTemplatePhy templatePhysicalPO = templatePhyService
                    .getTemplateById(param.getPhysicalIds().get(i));

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
    public Result dcdrSwitchMasterSlave(Integer logicId, Long expectMasterPhysicalId, int step, String operator) {

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
                Result result = templatePhyManager.switchMasterSlave(masterTemplate.getLogicId(),
                    slaveTemplate.getId(), operator);
                if (result.success()) {
                    return Result.buildSucc("switch");
                }
                return result;
            }
            return Result.buildParamIllegal("DCDR链路不存在");
        }

        //记录dcdr任务开始
        if (step == TemplateDCDRStepEnum.STEP_1.getStep()) {
            WorkTaskDTO workTaskDTO = new WorkTaskDTO();
            workTaskDTO.setBusinessKey(logicId);
            workTaskDTO.setTaskType(WorkTaskTypeEnum.TEMPLATE_DCDR.getType());
            workTaskDTO.setCreator(operator);
            workTaskManager.addTask(workTaskDTO);
        }
        Result result = executeDcdr(logicId, expectMasterPhysicalId, step, operator, masterTemplate, slaveTemplate);
        processDcdrTask(logicId, result, step);
        return result;
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

    /**************************************** private method ****************************************************/
    private Result checkDCDRParam(Integer logicId) {
        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService
            .getLogicTemplateWithPhysicalsById(logicId);
        if (templateLogicWithPhysical == null) {
            return Result.buildNotExist("模板不存在");
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

    private Result checkDCDRParam(TemplatePhysicalDCDRDTO param) {
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

            if (!esClusterPhyService.isClusterExists(param.getReplicaClusters().get(i))) {
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
                // TODO: should think more.
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

    private Result changeDcdrConfig(String cluster, List<String> indices,
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

    private Result deleteSrcDcdr(IndexTemplatePhy masterTemplate, IndexTemplatePhy slaveTemplate,

                                 List<String> matchNoVersionIndexNames, String operator) throws ESOperateException {

        TemplatePhysicalDCDRDTO dcdrDTO = new TemplatePhysicalDCDRDTO();

        dcdrDTO.setPhysicalIds(Arrays.asList(masterTemplate.getId()));
        dcdrDTO.setReplicaClusters(Arrays.asList(slaveTemplate.getCluster()));
        dcdrDTO.setDeleteIndexDcdr(false);

        Result delTemDCDRResult = deletePhyDcdr(dcdrDTO, operator);
        boolean delIndexDCDRResult = syncDeleteIndexDCDR(masterTemplate.getCluster(), slaveTemplate.getCluster(),
            matchNoVersionIndexNames, 3);

        return Result.build(delTemDCDRResult.success() && delIndexDCDRResult);
    }

    private Result executeDcdr(Integer logicId, Long expectMasterPhysicalId, int step, String operator,
                               IndexTemplatePhy masterTemplate, IndexTemplatePhy slaveTemplate) {
        List<String> matchIndexNames = templatePhyService.getMatchIndexNames(masterTemplate.getId());

        List<String> stepMsgList = Lists.newArrayList();
        for (String stepMsgFormat : DCDR_SWITCH_STEP_ARR) {
            stepMsgList.add(String.format(stepMsgFormat, DCDR_SWITCH_TODO));
        }
        try {
            switch (step) {
                case DCDR_SWITCH_STEP_1:
                    // 停止索引写入
                    Result stopMasterIndexResult = templateLimitManager.blockIndexWrite(masterTemplate.getCluster(),
                        matchIndexNames, true);
                    Result step1Result = buildStepMsg(stopMasterIndexResult, logicId, expectMasterPhysicalId,
                        DCDR_SWITCH_STEP_1, operator, stepMsgList);
                    if (step1Result.failed()) {
                        return step1Result;
                    }
                case DCDR_SWITCH_STEP_2:
                    // 确保主从数据同步完成
                    Result checkDataResult = Result.buildSucc();
                    if (!esIndexService.ensureDateSame(masterTemplate.getCluster(), slaveTemplate.getCluster(),
                        matchIndexNames)) {
                        checkDataResult = Result.buildFail("校验索引数据不一致!");
                        // 恢复实时数据写入
                        Result sttartMasterIndexResult = templateLimitManager
                            .blockIndexWrite(masterTemplate.getCluster(), matchIndexNames, false);
                        if (sttartMasterIndexResult.failed()) {
                            checkDataResult
                                .setMessage(checkDataResult.getMessage() + "|" + sttartMasterIndexResult.getMessage());
                        }
                    }
                    Result step2Result = buildStepMsg(checkDataResult, logicId, expectMasterPhysicalId,
                        DCDR_SWITCH_STEP_2, operator, stepMsgList);
                    if (step2Result.failed()) {
                        return step2Result;
                    }
                case DCDR_SWITCH_STEP_3:
                    // 删除dcdr链路（模板和索引）
                    Result deleteSrcDcdrResult = deleteSrcDcdr(masterTemplate, slaveTemplate, matchIndexNames,
                        operator);
                    Result step3Result = buildStepMsg(deleteSrcDcdrResult, logicId, expectMasterPhysicalId,
                        DCDR_SWITCH_STEP_3, operator, stepMsgList);
                    if (step3Result.failed()) {
                        return step3Result;
                    }
                case DCDR_SWITCH_STEP_4:
                    Result copyResult = Result.buildSucc();
                    // 拷贝主模板到从模板
                    if (!esTemplateService.syncCopyMappingAndAlias(masterTemplate.getCluster(),
                        masterTemplate.getName(), slaveTemplate.getCluster(), slaveTemplate.getName(), 3)) {
                        copyResult = Result.buildFail();
                    }
                    Result step4Result = buildStepMsg(copyResult, logicId, expectMasterPhysicalId, DCDR_SWITCH_STEP_4,
                        operator, stepMsgList);
                    if (step4Result.failed()) {
                        return step4Result;
                    }
                case DCDR_SWITCH_STEP_5:
                    // 修改dcdr索引配置 index.dcdr.replica_index = true/false
                    // 然后还需要reopen索引，配置才能生效
                    Result changeMasterDCDRConfig = changeDcdrConfig(masterTemplate.getCluster(), matchIndexNames,
                        true);
                    Result changeSlaveDCDRConfig = changeDcdrConfig(slaveTemplate.getCluster(), matchIndexNames, false);
                    Result setSettingResult = Result.buildSucc();
                    if (changeMasterDCDRConfig.failed() || changeSlaveDCDRConfig.failed()) {
                        setSettingResult = Result
                            .buildFail(changeMasterDCDRConfig.getMessage() + "|" + changeSlaveDCDRConfig.getMessage());
                    }
                    Result step5Result = buildStepMsg(setSettingResult, logicId, expectMasterPhysicalId,
                        DCDR_SWITCH_STEP_5, operator, stepMsgList);
                    if (step5Result.failed()) {
                        return step5Result;
                    }
                case DCDR_SWITCH_STEP_6:
                    // 停止索引写入
                    Result stopSlaveIndexResult = templateLimitManager.blockIndexWrite(slaveTemplate.getCluster(),
                        matchIndexNames, true);
                    Result step6Result = buildStepMsg(stopSlaveIndexResult, logicId, expectMasterPhysicalId,
                        DCDR_SWITCH_STEP_6, operator, stepMsgList);
                    if (step6Result.failed()) {
                        return step6Result;
                    }
                case DCDR_SWITCH_STEP_7:
                    // 创建新的主从链路
                    Result createDCDRResult = createPhyDcdr(buildCreateDCDRParam(slaveTemplate, masterTemplate),
                        operator);
                    Result step7Result = buildStepMsg(createDCDRResult, logicId, expectMasterPhysicalId,
                        DCDR_SWITCH_STEP_7, operator, stepMsgList);
                    if (step7Result.failed()) {
                        return step7Result;
                    }
                case DCDR_SWITCH_STEP_8:
                    // 恢复实时写入
                    Result startMasterIndexResult = templateLimitManager.blockIndexWrite(masterTemplate.getCluster(),
                        matchIndexNames, false);
                    Result startSlaveIndexResult = templateLimitManager.blockIndexWrite(slaveTemplate.getCluster(),
                        matchIndexNames, false);
                    Result startIndexResult = Result.buildSucc();
                    if (startMasterIndexResult.failed() || startSlaveIndexResult.failed()) {
                        startIndexResult = Result.buildFail(startMasterIndexResult.getMessage() + "|" + startSlaveIndexResult.getMessage());
                    }
                    Result step8Result = buildStepMsg(startIndexResult, logicId, expectMasterPhysicalId,
                        DCDR_SWITCH_STEP_8, operator, stepMsgList);
                    if (step8Result.failed()) {
                        return step8Result;
                    }
                case DCDR_SWITCH_STEP_9:
                    // 主从角色切换
                    Result switchMasterSlave = templatePhyManager.switchMasterSlave(masterTemplate.getLogicId(),
                        slaveTemplate.getId(), operator);
                    Result step9Result = buildStepMsg(switchMasterSlave, logicId, expectMasterPhysicalId,
                        DCDR_SWITCH_STEP_9, operator, stepMsgList);
                    if (step9Result.failed()) {
                        return step9Result;
                    }
            }
        } catch (Exception e) {
            LOGGER.warn("method=dcdrSwitchMasterSlave||logicId={}||errMsg={}", logicId, e.getMessage(), e);
            return buildStepMsg(Result.buildFail(e.getMessage()), logicId, expectMasterPhysicalId, DCDR_SWITCH_STEP_9,
                operator, stepMsgList);
        }
        return Result.build(ResultType.SUCCESS.getCode(), String.join("\n", stepMsgList));
    }

    private Result buildStepMsg(Result result, Integer logicId, Long expectMasterPhysicalId, int step, String operator,
                                List<String> stepMsgList) {
        if (result.failed()) {
            stepMsgList.set(step - 1, String.format(TemplateDCDRStepEnum.valueOfStep(step).getValue(), DCDR_SWITCH_FAIL)
                                      + ". 异常信息：" + result.getMessage());
            stepMsgList.add(DCDR_SWITCH_RETRY_FORMAT);
            return Result.buildFail(String.join("\n", stepMsgList));
        }
        stepMsgList.set(step - 1, String.format(TemplateDCDRStepEnum.valueOfStep(step).getValue(), DCDR_SWITCH_DONE));
        LOGGER.info("method=dcdrSwitchMasterSlave||logicId={}||msg=step {} succ", logicId, step);
        step++;
        return Result.buildSucc();
    }

    private Result processDcdrTask(Integer logicId, Result dcdrResult, int step) {
        Result<WorkTask> result = workTaskManager.getLatestTask(logicId, WorkTaskTypeEnum.TEMPLATE_DCDR.getType());
        if (result.failed()) {
            return result;
        }
        WorkTaskProcessDTO processDTO = new WorkTaskProcessDTO();
        processDTO.setStatus(
            dcdrResult.success() ? WorkTaskStatusEnum.SUCCESS.getStatus() : WorkTaskStatusEnum.FAILED.getStatus());
        processDTO.setTaskId(result.getData().getId());
        processDTO.setTaskProgress(step);

        if (dcdrResult.failed()) {
            DCDRTaskDetail detail = new DCDRTaskDetail();
            detail.setComment(result.getMessage());
            processDTO.setExpandData(JSONObject.toJSONString(detail));
        }
        return workTaskManager.processTask(processDTO);
    }
}
