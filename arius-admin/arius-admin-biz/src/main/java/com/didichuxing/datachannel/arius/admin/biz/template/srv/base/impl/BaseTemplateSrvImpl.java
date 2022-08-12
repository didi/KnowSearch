package com.didichuxing.datachannel.arius.admin.biz.template.srv.base.impl;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.common.BaseResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterConnectionStatusWithTemplateEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.SupportSrv;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleThree;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author chengxiang
 * @date 2022/5/11
 */
public abstract class BaseTemplateSrvImpl implements BaseTemplateSrv {
    protected static final ILog       LOGGER = LogFactory.getLog(BaseTemplateSrvImpl.class);

    @Autowired
    protected IndexTemplateService    indexTemplateService;

    @Autowired
    protected IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    protected TemplatePhyManager      templatePhyManager;
    
    @Autowired
    protected TemplateSrvManager templateSrvManager;
    
    @Autowired
    protected ClusterPhyManager      clusterPhyManager;
    @Autowired
    protected OperateRecordService   operateRecordService;
    @Autowired
    protected ProjectService         projectService;
    @Autowired
    protected AriusConfigInfoService ariusConfigInfoService;
    @Autowired
    protected ESClusterNodeService   esClusterNodeService;
    @Autowired
    protected ESClusterService       esClusterService;
    @Autowired
    protected ClusterRegionManager   clusterRegionManager;
   
    
    @Override
    public boolean isTemplateSrvOpen(Integer logicTemplateId) {
        return templateSrvManager.isTemplateSrvOpen(logicTemplateId, templateSrv().getCode());
    }

    @Override
    public String templateSrvName() {
        return templateSrv().getServiceName();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> openSrv(List<Integer> templateIdList, String operator,
                                Integer projectId) throws AdminOperateException {
        // 0.校验服务是否可以开启
        for (Integer templateId : templateIdList) {
            Result<Void> checkAvailableResult = checkSrvIsValid(templateId);
            if (checkAvailableResult.failed()) {
                return checkAvailableResult;
            }
        }

        // 1.更新DB服务开启状态
        Result<Void> updateResult = updateSrvStatus(templateIdList, Boolean.TRUE, operator, projectId);
        if (updateResult.failed()) {
            return updateResult;
        }

        return Result.buildSucc();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> closeSrv(List<Integer> templateIdList, String operator,
                                 Integer projectId) throws AdminOperateException {
        // 0.更新DB服务关闭状态
        Result<Void> updateResult = updateSrvStatus(templateIdList, Boolean.FALSE, operator, projectId);
        if (updateResult.failed()) {
            return updateResult;
        }

        return Result.buildSucc();
    }

    protected Result<Void> checkSrvIsValid(Integer logicTemplateId) {
        IndexTemplateLogicWithClusterAndMasterTemplate template = indexTemplateService
            .getLogicTemplateWithClusterAndMasterTemplate(logicTemplateId);
        if (null == template || null == template.getMasterTemplate()) {
            LOGGER.warn(
                "class=ColdManagerImpl||method=isTemplateSrvAvailable||templateId={}||errMsg=masterPhyTemplate is null",
                logicTemplateId);
            return Result.buildFail();
        }

        String masterCluster = template.getMasterTemplate().getCluster();
        Result<ClusterPhy> cluster = clusterPhyManager.getClusterByName(masterCluster);
        if (Objects.isNull(cluster.getData())) {
            LOGGER.warn(
                "class=ColdManagerImpl||method=isTemplateSrvAvailable||templateId={}||errMsg=clusterPhy of template is null",
                logicTemplateId);
            return Result.buildFail();
        }

        return Result.buildSucc();
    }

    /******************************************private************************************************/
    /**
     * 更新DB服务状态
     *
     * @param status,        true:开启, false:关闭
     * @param templateIdList
     * @param operator
     * @param projectId
     * @return
     */
    private Result<Void> updateSrvStatus(List<Integer> templateIdList, Boolean status, String operator,
                                         Integer projectId) throws AdminOperateException {
        String srvCode = templateSrv().getCode().toString();
        List<TupleTwo</*old*/IndexTemplate, /*change*/IndexTemplate>> tupleTwos = Lists.newArrayList();
        for (Integer templateId : templateIdList) {
            IndexTemplate indexTemplate = indexTemplateService.getLogicTemplateById(templateId);

            if (null == indexTemplate) {
                continue;
            }
            TupleTwo</*old*/IndexTemplate, /*change*/IndexTemplate> tupleTwo = Tuples.of(indexTemplate, null);
            if (status) {
                // 开启该项模版服务
                addSrvCode(indexTemplate, srvCode);
                // 如果是Rollover服务，则也要修改index_tmplate_config表中的disable_index_rollover字段
                if (TemplateServiceEnum.INDEX_PLAN.getCode().toString().equals(srvCode)){
                    updateTemplateConfigRollover(templateId, false, operator);
                }
            } else {
                // 关闭该项模版服务
                removeSrvCode(indexTemplate, srvCode);
                if (TemplateServiceEnum.INDEX_PLAN.getCode().toString().equals(srvCode)){
                    updateTemplateConfigRollover(templateId, true, operator);
                }
            }
            tupleTwo = tupleTwo.update2(indexTemplate);
            tupleTwos.add(tupleTwo);
        }
        //确认操作项目的合法性
        if (tupleTwos
            .stream().map(TupleTwo::v1).map(oldIndexTemplate -> ProjectUtils
                .checkProjectCorrectly(IndexTemplate::getProjectId, oldIndexTemplate, projectId))
            .allMatch(BaseResult::failed)) {
            return Result.buildFail("当前项目不属于超级项目或者持有该操作的项目");
        }
        for (TupleTwo<IndexTemplate, IndexTemplate> tupleTwo : tupleTwos) {
            final Result<Void> result = indexTemplateService
                .editTemplateInfoTODB(ConvertUtil.obj2Obj(tupleTwo.v2, IndexTemplateDTO.class));
            if (result.success()) {
                
                operateRecordService.save(new OperateRecord.Builder()
                    .operationTypeEnum(OperateTypeEnum.TEMPLATE_SERVICE).triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                    .bizId(tupleTwo.v2.getId()).project(projectService.getProjectBriefByProjectId(projectId))
                    .content(String.format("%s:【%s】", Boolean.TRUE.equals(status) ? "开启模板服务" : "关闭模板服务",templateSrvName())
                           
                           
                           ).userOperation(operator).build());
            }
        }

        return Result.buildSucc();
    }

    /**
     * 添加开启服务到对应模板实体中
     * @param indexTemplate
     * @param addSrvCode
     * @return true:有修改, false:无修改；根据返回值判断是否需要刷新到DB
     */
    private void addSrvCode(IndexTemplate indexTemplate, String addSrvCode) {
        String srvCodeStr = indexTemplate.getOpenSrv();
        List<String> srvCodeList = ListUtils.string2StrList(srvCodeStr);
        if (srvCodeList.isEmpty()) {
            indexTemplate.setOpenSrv(addSrvCode);
        } else {
            if (!srvCodeList.contains(addSrvCode)) {
                indexTemplate.setOpenSrv(srvCodeStr + "," + addSrvCode);
            }
        }
    }

    private void removeSrvCode(IndexTemplate indexTemplate, String removeSrvCode) {
        String srvCodeStr = indexTemplate.getOpenSrv();
        List<String> srvCodeList = ListUtils.string2StrList(srvCodeStr);
        if (srvCodeList.contains(removeSrvCode)) {
            srvCodeList.remove(removeSrvCode);
            indexTemplate.setOpenSrv(ListUtils.strList2String(srvCodeList));
        }
    }

    /**
     * 更新index_template_config表中的disable_index_rollover字段
     * @param templateId
     * @param state disable_index_rollover是true还是false
     * @param operator
     * @return
     */
    private Result<Void> updateTemplateConfigRollover(Integer templateId, Boolean state, String operator){
        IndexTemplateConfigDTO indexTemplateConfigDTO = new IndexTemplateConfigDTO();
        indexTemplateConfigDTO.setLogicId(templateId);
        indexTemplateConfigDTO.setDisableIndexRollover(state);
        Result<Void> result = indexTemplateService.updateTemplateConfig(indexTemplateConfigDTO, operator);
        if(result.failed()){
            LOGGER.warn("class=BaseTemplateSrvImpl||method=updateTemplateConfigRollover||msg={}", result.getMessage());
            return result;
        }
        return Result.buildSucc();
    }

    ///////////////////////////////////srv
  

    @Override
    public boolean isTemplateSrvOpen(List<IndexTemplatePhy> indexTemplatePhies) {
        for (IndexTemplatePhy indexTemplatePhy : indexTemplatePhies) {
            if (!isTemplateSrvOpen(indexTemplatePhy.getLogicId())) {
                return false;
            }
        }

        return true;
    }

    
  

    @Override
    public String templateServiceName() {
        return templateSrv().getServiceName();
    }
    
    @Override
    public SupportSrv getSupportSrvByLogicTemplateAndMasterClusterPhy(IndexTemplate logicIndexTemplate,
                                                                      String clusterPhy) {
        TupleThree</*dcdrExist*/Boolean,/*pipelineExist*/ Boolean,/*existColdRegion*/ Boolean> existDCDRAndPipelineModule = clusterPhyManager.getDCDRAndPipelineAndColdRegionTupleByClusterPhyWithCache(
                clusterPhy);
        List<TemplateServiceEnum> templateServiceEnums = TemplateServiceEnum.str2Srv(logicIndexTemplate.getOpenSrv());
        SupportSrv supportSrv = new SupportSrv();
        supportSrv.setPartition(StringUtils.endsWith(logicIndexTemplate.getExpression(), "*"));
        supportSrv.setDcdrModuleExists(
                Objects.equals(logicIndexTemplate.getHasDCDR(), Boolean.TRUE) || Boolean.TRUE.equals(
                        existDCDRAndPipelineModule.v1));
        supportSrv.setPipelineModuleExists(
                templateServiceEnums.contains(TemplateServiceEnum.TEMPLATE_PIPELINE) || Boolean.TRUE.equals(
                        existDCDRAndPipelineModule.v2));
        supportSrv.setColdRegionExists(
                templateServiceEnums.contains(TemplateServiceEnum.TEMPLATE_COLD) || Boolean.TRUE.equals(
                        existDCDRAndPipelineModule.v3));
    
        return supportSrv;
    }
    
    /**
     * 返回主集群连接的状态
     *
     * @param clusterPhy 集群的名称。
     * @return 主集群连接状态。
     */
    @Override
    public ClusterConnectionStatusWithTemplateEnum getMasterClusterConnectionStatus(String clusterPhy) {
        return esClusterService.syncConnectionStatus(clusterPhy)?ClusterConnectionStatusWithTemplateEnum.NORMAL:
                ClusterConnectionStatusWithTemplateEnum.DISCONNECTED;
    }
}