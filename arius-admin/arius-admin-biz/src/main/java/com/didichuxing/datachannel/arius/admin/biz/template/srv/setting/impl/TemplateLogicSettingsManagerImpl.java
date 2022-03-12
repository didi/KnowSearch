package com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.TemplatePreCreateManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplateLogicSettingsManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplatePhySettingsManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.ConsoleTemplateSettingDTO;
import com.didichuxing.datachannel.arius.admin.client.mapping.AriusIndexTemplateSetting;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySettings;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_SETTING;

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

        Result<Void> result = updateSettings(settingDTO.getLogicId(), settingDTO.getSetting());
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

    /**
     * 更新settings信息
     * @param logicId 逻辑ID
     * @param settings settings
     * @return
     */
    @Override
    public Result<Void> updateSettings(Integer logicId, AriusIndexTemplateSetting settings) {
        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService
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
                templatePhySettingsManager.mergeTemplateSettings(templatePhysical.getCluster(),
                    templatePhysical.getName(), settings.toJSON());
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
        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            return Result.buildNotExist("逻辑模板不存在, ID:" + logicId);
        }

        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildNotExist("物理模板不存在，ID:" + logicId);
        }

        IndexTemplatePhy indexTemplatePhy = templateLogicWithPhysical.getMasterPhyTemplate();
        if (indexTemplatePhy != null) {
            if (!isTemplateSrvOpen(indexTemplatePhy.getCluster())) {
                return Result.buildFail(indexTemplatePhy.getCluster() + "没有开启" + templateServiceName());
            }

            try {
                return Result.buildSucc( templatePhySettingsManager
                    .fetchTemplateSettings(indexTemplatePhy.getCluster(), indexTemplatePhy.getName()));
            } catch (ESOperateException e) {
                return Result.buildFail(e.getMessage());
            }
        }

        return Result.buildFail("不存在Master角色物理模板，ID：" + logicId);
    }
}
