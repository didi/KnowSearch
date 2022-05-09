package com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplateLogicMappingManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.TemplatePreCreateManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplateLogicSettingsManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplatePhySettingsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateSettingVO;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusTypeProperty;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfoWithMapping;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfoWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySettings;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_SETTING;
import static com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting.*;

/**
 * 索引setting服务实现
 * @author zqr
 * @date 2020-09-09
 */
@Service
public class TemplateLogicSettingsManagerImpl extends BaseTemplateSrv implements TemplateLogicSettingsManager {

    @Autowired
    private TemplatePhySettingsManager templatePhySettingsManager;

    @Autowired
    private TemplateLogicMappingManager templateLogicMappingManager;

    @Autowired
    private TemplatePreCreateManager templatePreCreateManager;

    @Override
    public TemplateServiceEnum templateService() {
        return TEMPLATE_SETTING;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> modifySetting(ConsoleTemplateSettingDTO settingDTO, String operator) throws AdminOperateException {

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
    public Result<IndexTemplatePhySettings> getSettings(Integer logicId) {
        return getTemplateSettings(logicId);
    }

    @Override
    public Result<TemplateSettingVO> buildTemplateSettingVO(Integer logicId) {
        // 从es引擎中获取逻辑模板对应的settings设置
        Result<IndexTemplatePhySettings> indexTemplateSettingsResult = getSettings(logicId);
        if (indexTemplateSettingsResult.failed()) {
            return Result.buildFrom(indexTemplateSettingsResult);
        }

        IndexTemplatePhySettings indexTemplatePhySettings = indexTemplateSettingsResult.getData();

        //  获取模板setting的扁平化结构
        Map<String, String> flatIndexTemplateMap = indexTemplatePhySettings.flatSettings();

        //模板索引setting视图构建,当副本为零时,cancelCopy为true,当translog为异步时，asyncTranslog为true
        TemplateSettingVO templateSettingVO = new TemplateSettingVO();

        // translog异步设置默认是request同步的
        templateSettingVO.setAsyncTranslog(flatIndexTemplateMap.containsKey(TRANSLOG_DURABILITY_KEY)
                && flatIndexTemplateMap.get(TRANSLOG_DURABILITY_KEY).equals(ASYNC));

        // 获取当前模板的副本数目设置
        templateSettingVO.setCancelCopy(flatIndexTemplateMap.containsKey(NUMBER_OF_REPLICAS_KEY)
                && Integer.parseInt(flatIndexTemplateMap.get(NUMBER_OF_REPLICAS_KEY)) == 0);

        // 获取当前模板设置的分词器，获取index.analysis下的自定义分词器设置
        templateSettingVO.setAnalysis(getAnalysisFromTemplateSettings(indexTemplatePhySettings));

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
        IndexTemplateInfoWithPhyTemplates templateLogicWithPhysical = indexTemplateInfoService
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
                templatePhySettingsManager.mergeTemplateSettings(logicId, templatePhysical.getCluster(),
                    templatePhysical.getName(), operator, settings.toJSON());
            } catch (AdminOperateException adminOperateException) {
                return Result.buildFail(adminOperateException.getMessage());
            }
        }

        return Result.buildSucc();
    }

    /**
     * 通过逻辑ID获取Settings
     * @param logicId 逻辑ID
     * @return
     */
    @Override
    public Result<IndexTemplatePhySettings> getTemplateSettings(Integer logicId) {
        IndexTemplateInfoWithPhyTemplates templateLogicWithPhysical = indexTemplateInfoService
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
                return Result.buildSucc( templatePhySettingsManager
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
        Result<IndexTemplateInfoWithMapping> templateWithMapping = templateLogicMappingManager.getTemplateWithMapping(logicId);
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

    private JSONObject getAnalysisFromTemplateSettings(IndexTemplatePhySettings indexTemplatePhySettings) {
        JSONObject indexSettings = indexTemplatePhySettings.getSettings().getJSONObject("index");
        if (AriusObjUtils.isNull(indexSettings)) {
            LOGGER.info("class=TemplateLogicServiceImpl||method=getAnalysisFromTemplateSettings||settings={}||msg= no index settings",
                    indexTemplatePhySettings);
            return null;
        }

        return indexSettings.getJSONObject("analysis");
    }
}
