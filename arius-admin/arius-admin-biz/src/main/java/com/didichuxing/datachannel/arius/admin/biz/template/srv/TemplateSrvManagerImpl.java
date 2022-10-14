package com.didichuxing.datachannel.arius.admin.biz.template.srv;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.TEMPLATE_SRV;

import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.biz.page.TemplateSrvPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.cold.ColdManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplateLogicSettingsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesIncrementalSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.ColdSrvOpenDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateIncrementalSettingsDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.TemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.UnavailableTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.TemplateWithSrvVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.ESSettingConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.template.SupportSrv;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleThree;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

/**
 * @author chengxiang
 * @date 2022/5/9
 */
@Service()
@DependsOn("springTool")
public class TemplateSrvManagerImpl implements TemplateSrvManager {
    protected static final ILog                                         LOGGER                                                    = LogFactory
        .getLog(TemplateSrvManagerImpl.class);

    private static final String                                         NO_PERMISSION_CONTENT                                     = "只有运维或者研发才有权限操作";

    private static final String                                         CLUSTER_LOGIC_NOT_EXISTS                                  = "逻辑集群不存在";
    private static final String                                         PHYSICAL_CLUSTER_NOT_EXISTS                               = "物理集群不存在";

    private final Map<Integer, BaseTemplateSrv>                         BASE_TEMPLATE_SRV_MAP                                     = Maps
        .newConcurrentMap();


    @Autowired
    private IndexTemplateService                                        indexTemplateService;

    @Autowired
    private ClusterPhyService                                           clusterPhyService;

    @Autowired
    private HandleFactory                                               handleFactory;
    @Autowired
    private RoleTool                                                    roleTool;

    @Autowired
    private ColdManager          coldManager;

    @Autowired
    private TemplateLogicSettingsManager                                templateLogicSettingsManager;

    @Autowired
    private IndicesManager                                              indicesManager;

    @PostConstruct
    public void init() {
        Map<String, BaseTemplateSrv> strTemplateSrvHandleMap = SpringTool.getBeansOfType(BaseTemplateSrv.class);
        strTemplateSrvHandleMap.forEach((k, v) -> {
            try {
                TemplateServiceEnum srvEnum = v.templateSrv();
                BASE_TEMPLATE_SRV_MAP.put(srvEnum.getCode(), v);
            } catch (Exception e) {
                LOGGER.error("class=TemplateSrvManagerImpl||method=init||error=", e);
            }
        });
        LOGGER.info("class=TemplateSrvManagerImpl||method=init||init finish");
    }

    @Override
    public Result<List<TemplateSrv>> getTemplateOpenSrv(Integer logicTemplateId) {
        try {
            IndexTemplate template = indexTemplateService.getLogicTemplateById(logicTemplateId);
            if (null == template) {
                return Result.buildNotExist("逻辑模板不存在");
            }

            return Result.buildSucc(TemplateSrv.codeStr2SrvList(template.getOpenSrv()));
        } catch (Exception e) {
            LOGGER.error("class=TemplateSrvManagerImpl||method=getTemplateOpenSrv||logicTemplateId={}", logicTemplateId,
                e);
            return Result.buildFail("获取模板开启服务失败");
        }
    }

    @Override
    public boolean isTemplateSrvOpen(Integer logicTemplateId, Integer srvCode) {
        Result<List<TemplateSrv>> openSrvResult = getTemplateOpenSrv(logicTemplateId);
        if (openSrvResult.failed()) {
            return false;
        }

        List<TemplateSrv> openSrv = openSrvResult.getData();
        for (TemplateSrv srv : openSrv) {
            if (srvCode.equals(srv.getSrvCode())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<UnavailableTemplateSrv> getUnavailableSrvByTemplateAndMasterPhy(IndexTemplate template,
                                                                                TupleThree<Boolean, Boolean, Boolean> existDCDRAndPipelineModule) {
        List<UnavailableTemplateSrv> unavailableSrvList = Lists.newCopyOnWriteArrayList();
        List<TemplateServiceEnum> allSrvList = TemplateServiceEnum.allTemplateSrv();
        //默认给一个srv就可以了
        BaseTemplateSrv srvHandle = BASE_TEMPLATE_SRV_MAP.get(TemplateServiceEnum.TEMPLATE_COLD.getCode());
        SupportSrv supportSrv =srvHandle.getSupportSrvByLogicTemplateAndMasterClusterPhy(template,
                existDCDRAndPipelineModule);
        final boolean dcdrSupport = supportSrv.isDcdrModuleExists();
        final boolean pipelineSupport = supportSrv.isPipelineModuleExists();
        boolean coldRegionSupport = supportSrv.isColdRegionExists();
        //校验是否具备分区能力：冷热划分的能力、过期删除
        // isPartition为true代表能分区，false不能分区
        boolean isPartition =supportSrv.isPartition();
        /**
         * 预创建，过期删除（分区才可以操作），冷热分离（分区并且有冷region才能操作），dcdr和pipeline（es有对应module才能操作），rolloer没有限制但是产品侧有提示
         */
        for (TemplateServiceEnum srvEnum : allSrvList) {


            //1.非分区模版不支持：预创建、过期删除、冷热划分的能力
            if (Boolean.FALSE.equals(isPartition)&&TemplateServiceEnum.usePartitionService().contains(srvEnum)){
                final UnavailableTemplateSrv unavailableTemplateSrv = new UnavailableTemplateSrv(srvEnum.getCode(),
                        srvEnum.getServiceName(), srvEnum.getEsClusterVersion().getVersion(),
                        String.format("非分区模版不支持%s能力", srvEnum.getServiceName()));
                unavailableSrvList.add(unavailableTemplateSrv);
            } else
                // 2.必须存在冷region才支持 少了一步判断 等待cold支持
                if (Boolean.FALSE.equals(coldRegionSupport) && srvEnum.equals(TemplateServiceEnum.TEMPLATE_COLD)) {
                    final UnavailableTemplateSrv unavailableTemplateSrv = new UnavailableTemplateSrv(srvEnum.getCode(),
                            srvEnum.getServiceName(), srvEnum.getEsClusterVersion().getVersion(),
                            "集群没有冷region，不支持此能力");
                    unavailableSrvList.add(unavailableTemplateSrv);

                } else
                    //3.dcdr dcdrSupport==true支持
                    if (Boolean.FALSE.equals(dcdrSupport) && srvEnum.equals(TemplateServiceEnum.TEMPLATE_DCDR)) {
                        final UnavailableTemplateSrv unavailableTemplateSrv = new UnavailableTemplateSrv(
                                srvEnum.getCode(), srvEnum.getServiceName(), srvEnum.getEsClusterVersion().getVersion(),
                                "无DCDR插件，不支持此能力");
                        unavailableSrvList.add(unavailableTemplateSrv);
                    } else
            //4.pipeline pipelineSupport==true支持
            if (Boolean.FALSE.equals(pipelineSupport)&&srvEnum.equals(TemplateServiceEnum.TEMPLATE_PIPELINE)){
                final UnavailableTemplateSrv unavailableTemplateSrv = new UnavailableTemplateSrv(srvEnum.getCode(),
                        srvEnum.getServiceName(), srvEnum.getEsClusterVersion().getVersion(),
                        "集群中没有pipeline 插件，不支持此能力");
                unavailableSrvList.add(unavailableTemplateSrv);
            }
        }
        return unavailableSrvList;
    }

    @Override
    public PaginationResult<TemplateWithSrvVO> pageGetTemplateWithSrv(TemplateQueryDTO condition, Integer projectId) throws NotFindSubclassException {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(TEMPLATE_SRV.getPageSearchType());
        if (baseHandle instanceof TemplateSrvPageSearchHandle) {
            if (condition.getProjectId() == null && !AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
                condition.setProjectId(projectId);
            }
            TemplateSrvPageSearchHandle handler = (TemplateSrvPageSearchHandle) baseHandle;
            return handler.doPage(condition, projectId);
        }
        return PaginationResult.buildFail("没有找到对应的处理器");
    }

    @Override
    public Result<Void> openSrv(Integer srvCode, List<Integer> templateIdList, String operator, Integer projectId,
                                ColdSrvOpenDTO data) {
        BaseTemplateSrv srvHandle = BASE_TEMPLATE_SRV_MAP.get(srvCode);
        if (null == srvHandle) {
            return Result.buildParamIllegal("未找到对应的服务");
        }

        try {
            if (Objects.nonNull(data)) {
                Result<Integer> result = coldManager.batchChangeHotDay(data.getColdSaveDays(), operator, templateIdList,
                    projectId);
                if (result.failed()) {
                    return Result.buildFrom(result);
                }
            }
            return srvHandle.openSrv(templateIdList, operator, projectId);
        } catch (AdminOperateException e) {
            LOGGER.error("class=TemplateSrvManagerImpl||method=openSrv||templateIdList={}||srvCode={}"
                         + "||errMsg=failed to open template srv",
                ListUtils.intList2String(templateIdList), srvCode);
            return Result.buildFail(e.getMessage());
        }
    }

    @Override
    public Result<Void> closeSrv(Integer srvCode, List<Integer> templateIdList, String operator, Integer projectId) {
        BaseTemplateSrv srvHandle = BASE_TEMPLATE_SRV_MAP.get(srvCode);
        if (null == srvHandle) {
            return Result.buildParamIllegal("未找到对应服务");
        }

        try {
            return srvHandle.closeSrv(templateIdList, operator, projectId);
        } catch (AdminOperateException e) {
            LOGGER.error("class=TemplateSrvManagerImpl||method=closeSrv||templateIdList={}||srvCode={}"
                         + "||errMsg=failed to open template srv",
                ListUtils.intList2String(templateIdList), srvCode);
            return Result.buildFail(e.getMessage());
        }
    }











    /**
     * 查询开启了某个索引服务的物理集群列表
     *
     * @param srvId
     * @return
     */
    @Override
    public List<String> getPhyClusterByOpenTemplateSrv(int srvId) {

        return clusterPhyService.listAllClusters().stream().map(ClusterPhy::getCluster).collect(Collectors.toList());
    }

    /**
     * 查询开启了某个索引服务的索引模板列表
     *
     * @param srvId srvid
     * @return {@link List}<{@link String}>
     */
      @Override
    public List<String> getIndexTemplateContainsSrv(int srvId) {
       return indexTemplateService.listAllLogicTemplatesWithCache()
                .stream()
                .filter(indexTemplate -> TemplateServiceEnum.strContainsSrv( indexTemplate.getOpenSrv(),TemplateServiceEnum.TEMPLATE_PIPELINE))
                .map(IndexTemplate::getName)
                .collect(Collectors.toList());
    }



    /**
     * 通过更新模版settings和部分索引settings来实现模版服务(如异步translog、恢复优先级)
     * @param settings 模版增量settings
     * @param templateIdList  模版id列表
     * @param operator
     * @param projectId
     * @return
     */
    @Override
    public Result<Void> updateSrvStatusBySettings(TemplateIncrementalSettingsDTO settings, List<Integer> templateIdList, String operator, Integer projectId) throws AdminOperateException {

        Result<Void> checkResult = checkParam(projectId, operator, settings);
        if(checkResult.failed()){
            return Result.buildFail(checkResult.getMessage());
        }

        // 用于错误消息拼接
        boolean updateFail = false;
        StringBuilder updateFailTemplates = new StringBuilder();
        // 构造模版的增量settings
        Map<String, String> incrementalSettings = Maps.newHashMap();
        incrementalSettings.put(settings.getKey(), settings.getValue());

        // indicesIncrementalSettingList 存储需要更新settings的索引
        List<IndicesIncrementalSettingDTO> indicesIncrementalSettingList = Lists.newArrayList();

        for (Integer logicId : templateIdList) {
            // 增量方式修改每个模版的settings
            Result<Void> result = templateLogicSettingsManager.updateSettingsByMerge(logicId, incrementalSettings, operator, projectId);
            if(result.failed()){
                updateFail = true;
                updateFailTemplates.append(logicId).append(",");
                LOGGER.error("class=TemplateSrvManagerImpl||method=updateSrvStatusBySettings,templateId={}, errMsg={}",
                        logicId, "update settings failed");
            }

            // 更新服务状态到数据库中


            // 对于非分区模版，还要修改其对应的那一个索引的settings
            IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService.getLogicTemplateWithPhysicalsById(logicId);
            if(!templateLogicWithPhysical.getExpression().endsWith("*")){
                List<IndexTemplatePhy> physicalMasters = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
                for (IndexTemplatePhy physicalMaster : physicalMasters) {
                    CatIndexResult catIndexResult = indicesManager.listIndexCatInfoByTemplatePhyId(physicalMaster.getId())
                            .stream().findFirst().orElse(null);
                    if(AriusObjUtils.isNull(catIndexResult)){
                        continue;
                    }

                    IndicesIncrementalSettingDTO indicesIncrementalSettingDTO = new IndicesIncrementalSettingDTO();
                    indicesIncrementalSettingDTO.setCluster(physicalMaster.getCluster());
                    indicesIncrementalSettingDTO.setIndex(catIndexResult.getIndex());
                    indicesIncrementalSettingDTO.setKey(settings.getKey());
                    indicesIncrementalSettingDTO.setValue(settings.getValue());

                    indicesIncrementalSettingList.add(indicesIncrementalSettingDTO);
                }
            }
        }

        // 批量更新索引settings
        if(!indicesIncrementalSettingList.isEmpty()){
            Result<Void> result = indicesManager.updateIndexSettingsByMerge(indicesIncrementalSettingList, projectId, operator);
            if(result.failed()){
                return Result.buildFail(result.getMessage());
            }
        }

        if(updateFail){
            return Result.buildFail(updateFailTemplates.deleteCharAt(updateFailTemplates.length()-1) + "模版更新settings失败");
        }

        return Result.buildSucc();
    }

    /***************************************************private**********************************************************/

    private Result<Void> checkParam(Integer projectId, String operator, TemplateIncrementalSettingsDTO settings){
        final Result<Void> projectCheck = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (projectCheck.failed()) {
            return Result.buildFail(projectCheck.getMessage());
        }
        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人为空");
        }

        if(ESSettingConstant.INDEX_TRANSLOG_DURABILITY.equals(settings.getKey())){
            if(!(ESSettingConstant.ASYNC.equals(settings.getValue()) || ESSettingConstant.REQUEST.equals(settings.getValue()))) {
                return Result.buildParamIllegal("setting [index.translog.durability] must be 'request' or 'async'");
            }
        }else if (ESSettingConstant.INDEX_PRIORITY.equals(settings.getKey())){
            if(!StringUtils.isNumeric(settings.getValue())){
                return Result.buildParamIllegal("setting [index.priority] must be numeric");
            }
            Integer priorityLevel = Integer.valueOf(settings.getValue());
            if(priorityLevel < 0){
                return Result.buildParamIllegal("setting [index.priority] must be >= 0");
            }
            if(!(ESSettingConstant.HIGH_PRIORITY.equals(priorityLevel) || ESSettingConstant.MIDDLE_PRIORITY
                    .equals(priorityLevel) || ESSettingConstant.LOW_PRIORITY.equals(priorityLevel))){
                return Result.buildParamIllegal("setting [index.priority] must be 10,5,0");
            }
        }else {
            return Result.buildParamIllegal("模版settings的key取值有误");
        }

        return Result.buildSucc();
    }
}