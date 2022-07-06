package com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.impl;

import static com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting.ASYNC;
import static com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting.NUMBER_OF_REPLICAS_KEY;
import static com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting.REQUEST;
import static com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting.TRANSLOG_DURABILITY_KEY;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.precreate.PreCreateManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplateLogicMappingManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplateLogicSettingsManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplatePhySettingsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySetting;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithMapping;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateSettingVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.NewTemplateSrvEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusTypeProperty;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didiglobal.logi.elasticsearch.client.response.setting.template.TemplateConfig;
import com.didiglobal.logi.elasticsearch.client.utils.JsonUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 索引setting服务实现
 * @author zqr
 * @date 2020-09-09
 */
@Service
public class TemplateLogicSettingsManagerImpl extends BaseTemplateSrvImpl implements TemplateLogicSettingsManager {

    @Autowired
    private TemplatePhySettingManager templatePhySettingManager;

    @Autowired
    private TemplateLogicMappingManager templateLogicMappingManager;

    @Autowired
    private PreCreateManager  templatePreCreateManager;
    @Autowired
    private ESTemplateService esTemplateService;
    
    
    
    /**
     * @return
     */
    @Override
    public NewTemplateSrvEnum templateSrv() {
        return NewTemplateSrvEnum.TEMPLATE_SETTING;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> modifySetting(ConsoleTemplateSettingDTO settingDTO, String operator, Integer projectId) throws AdminOperateException {

        LOGGER.info("class=TemplateLogicServiceImpl||method=modifySetting||operator={}||setting={}", operator,
            JSON.toJSONString(settingDTO));

        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人为空");
        }

        if (settingDTO.getSetting() == null || settingDTO.getSetting().getAnalysis() == null) {
            return Result.buildParamIllegal("setting信息不能为空");
        }

        Result<Void> result = updateSettings(settingDTO.getLogicId(), operator, settingDTO.getSetting());
        if (result.success()) {
            templatePreCreateManager.reBuildTomorrowIndex(settingDTO.getLogicId(), 3);
        }

        return result;
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
            templatePreCreateManager.reBuildTomorrowIndex(settingDTO.getLogicId(), 3);
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

        if (!isTemplateSrvOpen(templatePhysicals)) {
            return Result.buildFail("集群没有开启" + templateServiceName());
        }

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
                                       Integer projectId) {
        
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
        TemplateConfig templateConfig = Optional.ofNullable(esTemplateService.syncGetTemplateConfig(
                templateLogicWithPhysical.getMasterPhyTemplate().getCluster(),
                templateLogicWithPhysical.getMasterPhyTemplate().getName())).orElse(new TemplateConfig());
        JSONObject beforeSetting= MapUtils.isNotEmpty(templateConfig.getSetttings())?
                JsonUtils.reFlat(templateConfig.getSetttings()):new JSONObject();
        //变更后的setting
        JSONObject afterSetting=settings.getSettings();
        
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            try {
                templatePhySettingManager.mergeTemplateSettings(logicId, templatePhysical.getCluster(), templatePhysical.getName(),  settings);
            } catch (AdminOperateException adminOperateException) {
                return Result.buildFail(adminOperateException.getMessage());
            }
        }

        try {
            templatePreCreateManager.reBuildTomorrowIndex(logicId, 3);
        } catch (Exception e) {
            LOGGER.error("class=TemplateLogicServiceImpl||method=updateSettings||logicId:{}", logicId, e);
        }
        operateRecordService.save(
                new OperateRecord.Builder().project(projectService.getProjectBriefByProjectId(projectId))
                        .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).userOperation(operator)
                        .operationTypeEnum(OperateTypeEnum.INDEX_TEMPLATE_MANAGEMENT_EDIT_SETTING)
                        .content(ProjectUtils.getChangeByAfterAndBeforeJson(beforeSetting, afterSetting))
                        .bizId(logicId)
                        .build());

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
                return Result.buildSucc( templatePhySettingManager
                    .fetchTemplateSettings(indexTemplatePhy.getCluster(), indexTemplatePhy.getName()));
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
        Result<IndexTemplateWithMapping> templateWithMapping = templateLogicMappingManager.getTemplateWithMapping(logicId);
        if (templateWithMapping.failed()) {
            LOGGER.warn("class=TemplateLogicServiceImpl||method=getDynamicTemplatesByLogicTemplate||logicTemplateId={}||msg={}",
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
            LOGGER.info("class=TemplateLogicServiceImpl||method=getAnalysisFromTemplateSettings||settings={}||msg= no index settings",
                    indexTemplatePhySetting);
            return null;
        }

        return indexSettings.getJSONObject("analysis");
    }
}