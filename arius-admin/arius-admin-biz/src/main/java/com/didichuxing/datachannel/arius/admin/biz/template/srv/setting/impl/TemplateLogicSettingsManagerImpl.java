package com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.impl;

import static com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting.ASYNC;
import static com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting.NUMBER_OF_REPLICAS_KEY;
import static com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting.REQUEST;
import static com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting.TRANSLOG_DURABILITY_KEY;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplateLogicMappingManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.PreCreateManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplateLogicSettingsManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplatePhySettingManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template.TemplateSettingOperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySetting;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithMapping;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateSettingVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.event.index.ReBuildTomorrowIndexEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusTypeProperty;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didiglobal.logi.elasticsearch.client.utils.JsonUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 索引setting服务实现
 * @author zqr
 * @date 2020-09-09
 */
@Service
public class TemplateLogicSettingsManagerImpl extends BaseTemplateSrvImpl implements TemplateLogicSettingsManager {

    @Autowired
    private TemplatePhySettingManager   templatePhySettingManager;

    @Autowired
    private TemplateLogicMappingManager templateLogicMappingManager;

    @Autowired
    private PreCreateManager            templatePreCreateManager;
    @Autowired
    private ESTemplateService           esTemplateService;
    @Autowired
    private ESIndexService esIndexService;

    /**
     * @return
     */
    @Override
    public TemplateServiceEnum templateSrv() {
        return TemplateServiceEnum.TEMPLATE_SETTING;
    }
    

    @Override
    public Result<Void> customizeSetting(TemplateSettingDTO settingDTO, String operator) throws AdminOperateException {

        LOGGER.info("class=TemplateLogicServiceImpl||method=modifySetting||operator={}||setting={}", operator,
            JSON.toJSONString(settingDTO));

        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人为空");
        }

        // 根据传入setting的设置创建修改的模板setting
        AriusIndexTemplateSetting settings = new AriusIndexTemplateSetting();
        settings.setReplicasNum(settingDTO.isCancelCopy() ? 0 : 1);
        settings.setTranslogDurability(settingDTO.isAsyncTranslog() ? ASYNC : REQUEST);

        Result<Void> result = updateSettings(settingDTO.getLogicId(), operator, settings);
        if (result.success()) {
            SpringTool.publish(new ReBuildTomorrowIndexEvent(this, settingDTO.getLogicId()));
        }

        return result;
    }

    /**
     * 获取逻辑模板settings
     *
     * @param logicId 逻辑模板ID
     * @return
     * @throws AdminOperateException
     */
    @Override
    public Result<IndexTemplatePhySetting> getSettings(Integer logicId) {
        return getTemplateSettings(logicId);
    }

    @Override
    public Result<TemplateSettingVO> buildTemplateSettingVO(Integer logicId) {
        // 从es引擎中获取逻辑模板对应的settings设置
        Result<IndexTemplatePhySetting> indexTemplateSettingsResult = getSettings(logicId);
        if (indexTemplateSettingsResult.failed()) {
            return Result.buildFrom(indexTemplateSettingsResult);
        }

        IndexTemplatePhySetting indexTemplatePhySetting = indexTemplateSettingsResult.getData();

        //  获取模板setting的扁平化结构
        Map<String, String> flatIndexTemplateMap = indexTemplatePhySetting.flatSettings();

        //模板索引setting视图构建,当副本为零时,cancelCopy为true,当translog为异步时，asyncTranslog为true
        TemplateSettingVO templateSettingVO = new TemplateSettingVO();

        // translog异步设置默认是request同步的
        templateSettingVO.setAsyncTranslog(flatIndexTemplateMap.containsKey(TRANSLOG_DURABILITY_KEY)
                                           && flatIndexTemplateMap.get(TRANSLOG_DURABILITY_KEY).equals(ASYNC));

        // 获取当前模板的副本数目设置
        templateSettingVO.setCancelCopy(flatIndexTemplateMap.containsKey(NUMBER_OF_REPLICAS_KEY)
                                        && Integer.parseInt(flatIndexTemplateMap.get(NUMBER_OF_REPLICAS_KEY)) == 0);

        // 获取当前模板设置的分词器，获取index.analysis下的自定义分词器设置
        templateSettingVO.setAnalysis(getAnalysisFromTemplateSettings(indexTemplatePhySetting));

        // 获取当前模板的dynamic_templates
        templateSettingVO.setDynamicTemplates(getDynamicTemplatesByLogicTemplate(logicId));

        return Result.buildSucc(templateSettingVO);
    }

    /**
     * 更新settings信息
     * @param logicId 逻辑ID
     * @param settings settings
     * @return
     */
    @Override
    public Result<Void> updateSettings(Integer logicId, String operator, AriusIndexTemplateSetting settings) {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            return Result.buildNotExist("逻辑模板不存在, ID:" + logicId);
        }

        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildNotExist("物理模板不存在，ID:" + logicId);
        }

        List<IndexTemplatePhy> templatePhysicals = templateLogicWithPhysical.fetchMasterPhysicalTemplates();

        

        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            try {
                templatePhySettingManager.mergeTemplateSettings(logicId, templatePhysical.getCluster(),
                    templatePhysical.getName(), operator, settings.toJSON());
            } catch (AdminOperateException adminOperateException) {
                return Result.buildFail(adminOperateException.getMessage());
            }
        }

        return Result.buildSucc();
    }

    @Override
    public Result<Void> updateSettings(Integer logicId, IndexTemplatePhySetting settings, String operator,
                                       Integer projectId) throws AdminOperateException {

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            return Result.buildNotExist("逻辑模板不存在, ID:" + logicId);
        }

        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildNotExist("物理模板不存在，ID:" + logicId);
        }
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(IndexTemplateWithPhyTemplates::getProjectId,
            templateLogicWithPhysical, projectId);
        if (result.failed()) {
            return result;
        }

        List<IndexTemplatePhy> templatePhysicals = templateLogicWithPhysical.fetchMasterPhysicalTemplates();

        //获取变更前的setting
        final Result<IndexTemplatePhySetting> beforeSetting = getSettings(logicId);
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
        
            templatePhySettingManager.mergeTemplateSettingsCheckAllocationAndShard(logicId,
                    templatePhysical.getCluster(), templatePhysical.getName(), settings);
        }
        //分区索引会自动重建
        if (templateLogicWithPhysical.getMasterPhyTemplate().getExpression().endsWith("*")) {
            SpringTool.publish(new ReBuildTomorrowIndexEvent(this, logicId));
        } else {
            noPartitioningIndexSettingChanges(settings,templateLogicWithPhysical);
        }
        final Result<IndexTemplatePhySetting> afterSetting = getSettings(logicId);
        operateRecordService.save(new OperateRecord.Builder()
            .project(projectService.getProjectBriefByProjectId(projectId)).triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
            .userOperation(operator).operationTypeEnum(OperateTypeEnum.TEMPLATE_MANAGEMENT_EDIT_SETTING)
            .content(new TemplateSettingOperateRecord(beforeSetting.getData(), afterSetting.getData()).toString())
            .bizId(logicId).build());

        return Result.buildSucc();
    }

    /**
     * 通过逻辑ID获取Settings
     * @param logicId 逻辑ID
     * @return
     */
    @Override
    public Result<IndexTemplatePhySetting> getTemplateSettings(Integer logicId) {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            return Result.buildNotExist("逻辑模板不存在, ID:" + logicId);
        }

        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildNotExist("物理模板不存在，ID:" + logicId);
        }

        IndexTemplatePhy indexTemplatePhy = templateLogicWithPhysical.getMasterPhyTemplate();
        if (indexTemplatePhy != null) {
            try {
                return Result.buildSucc(templatePhySettingManager.fetchTemplateSettings(indexTemplatePhy.getCluster(),
                    indexTemplatePhy.getName()));
            } catch (ESOperateException e) {
                return Result.buildFail(e.getMessage());
            }
        }

        return Result.buildFail("不存在Master角色物理模板，ID：" + logicId);
    }

    /**************************************** private method ****************************************************/
    /**
     * 根据逻辑模板id获取设置的dynamic_templates设置
     * @param logicId 逻辑模板id
     * @return
     */
    private JSONArray getDynamicTemplatesByLogicTemplate(Integer logicId) {
        Result<IndexTemplateWithMapping> templateWithMapping = templateLogicMappingManager
            .getTemplateWithMapping(logicId);
        if (templateWithMapping.failed()) {
            LOGGER.warn(
                "class=TemplateLogicServiceImpl||method=getDynamicTemplatesByLogicTemplate||logicTemplateId={}||msg={}",
                logicId, templateWithMapping.getMessage());
            return null;
        }

        // 获取逻辑模板对应的物理模板的mapping设置
        List<AriusTypeProperty> typeProperties = templateWithMapping.getData().getTypeProperties();
        if (CollectionUtils.isEmpty(typeProperties)) {
            return null;
        }

        // 获取其中一个物理模板的mapping的中的dynamic_templates设置
        return typeProperties.get(0).getDynamicTemplates();
    }

    private JSONObject getAnalysisFromTemplateSettings(IndexTemplatePhySetting indexTemplatePhySetting) {
        JSONObject indexSettings = indexTemplatePhySetting.getSettings().getJSONObject("index");
        if (AriusObjUtils.isNull(indexSettings)) {
            LOGGER.info(
                "class=TemplateLogicServiceImpl||method=getAnalysisFromTemplateSettings||settings={}||msg= no index settings",
                indexTemplatePhySetting);
            return null;
        }

        return indexSettings.getJSONObject("analysis");
    }
    
    /**
     * 修改不分区索引的setting，尝试性修改，对错误结果不做返回
     *
     * @param settings 索引模板设置。
     * @param templateLogicWithPhysical 带有物理模板的逻辑索引模板
     */
    private void noPartitioningIndexSettingChanges(IndexTemplatePhySetting settings,
        IndexTemplateWithPhyTemplates templateLogicWithPhysical) {
        // 同步修改不分区索引
        final Map<String, String> settingMap = JsonUtils.flat(settings.getSettings());
        // 删除 index.routing.allocation.include._name 和 index.number_of_shards 因为会导致更新索引 setting 失效
        settingMap.remove("index.routing.allocation.include._name");
        settingMap.remove("index.number_of_shards");
        // 更新索引 setting
        for (IndexTemplatePhy physical : templateLogicWithPhysical.getPhysicals()) {
            try {
                esIndexService.syncPutIndexSettings(physical.getCluster(), Collections.singletonList(physical.getName()),
                    settingMap,3);
            } catch (ESOperateException e) {
               LOGGER.error(
                "class=TemplateLogicServiceImpl||method=noPartitioningIndexSettingChanges||IndexTemplate={}",
                templateLogicWithPhysical.getName(),e);
            }
        }
        
    }
}