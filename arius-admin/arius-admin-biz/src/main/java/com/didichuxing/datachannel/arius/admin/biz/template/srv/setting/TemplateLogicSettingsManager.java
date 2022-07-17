package com.didichuxing.datachannel.arius.admin.biz.template.srv.setting;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySetting;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateSettingVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting;

/**
 * 逻辑模板settings service
 * @author wangshu
 * @date 2020/09/02
 */
public interface TemplateLogicSettingsManager {

    ///**
    // * 修改模板Setting
    // *
    // * @param settingDTO Setting
    // * @param operator   操作者
    // * @param projectId
    // * @return Result
    // * @throws AdminOperateException
    // */
    //Result<Void> modifySetting(ConsoleTemplateSettingDTO settingDTO, String operator,
    //                           Integer projectId) throws AdminOperateException;

    /**
     * 修改模板Setting(仅开放对于副本设置和异步translog落盘方式的设置)
     * @param settingDTO 模板seting修改类
     * @param operator 操作者
     * @throws AdminOperateException
     */
    Result<Void> customizeSetting(TemplateSettingDTO settingDTO, String operator) throws AdminOperateException;

    /**
     * 获取逻辑模板settings
     * @param logicId 逻辑模板ID
     * @return
     * @throws AdminOperateException
     */
    Result<IndexTemplatePhySetting> getSettings(Integer logicId) throws AdminOperateException;

    /**
     * 创建逻辑模板settings视图
     * @param logicId 逻辑模板ID
     * @return 索引模板视图信息
     */
    Result<TemplateSettingVO> buildTemplateSettingVO(Integer logicId);

    /**
     * 更新settings信息
     * @param logicId 逻辑ID
     * @param settings settings
     * @return
     */
    @Deprecated
    Result<Void> updateSettings(Integer logicId, String operator, AriusIndexTemplateSetting settings);

    /**
     * 更新settings信息
     *
     * @param logicId   逻辑ID
     * @param settings  settings
     * @param operator
     * @param projectId
     * @return
     */
    Result<Void> updateSettings(Integer logicId, IndexTemplatePhySetting settings, String operator, Integer projectId);

    /**
     * 更加逻辑ID获取Settings
     * @param logicId 逻辑ID
     * @return
     */
    Result<IndexTemplatePhySetting> getTemplateSettings(Integer logicId);
}