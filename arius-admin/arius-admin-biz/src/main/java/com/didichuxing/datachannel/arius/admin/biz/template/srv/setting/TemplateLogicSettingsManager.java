package com.didichuxing.datachannel.arius.admin.biz.template.srv.setting;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.ConsoleTemplateSettingDTO;
import com.didichuxing.datachannel.arius.admin.client.mapping.AriusIndexTemplateSetting;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;

/**
 * 逻辑模板settings service
 * @author wangshu
 * @date 2020/09/02
 */
public interface TemplateLogicSettingsManager {

    /**
     * 修改模板Setting
     * @param settingDTO Setting
     * @param operator 操作者
     * @return Result
     * @throws AdminOperateException
     */
    Result modifySetting(ConsoleTemplateSettingDTO settingDTO, String operator) throws AdminOperateException;

    /**
     * 获取逻辑模板settings
     * @param logicId 逻辑模板ID
     * @return
     * @throws AdminOperateException
     */
    Result getSettings(Integer logicId) throws AdminOperateException;

    /**
     * 更新settings信息
     * @param logicId 逻辑ID
     * @param settings settings
     * @return
     */
    Result updateSettings(Integer logicId, AriusIndexTemplateSetting settings);

    /**
     * 更加逻辑ID获取Settings
     * @param logicId 逻辑ID
     * @return
     */
    Result getTemplateSettings(Integer logicId);
}
