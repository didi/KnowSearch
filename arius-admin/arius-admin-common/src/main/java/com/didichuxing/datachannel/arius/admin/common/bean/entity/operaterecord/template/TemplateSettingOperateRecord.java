package com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySetting;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TemplateOperateRecordEnum;
import com.didichuxing.datachannel.arius.admin.common.util.DiffUtil;
import com.didichuxing.datachannel.arius.admin.common.util.DiffUtil.Diff;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liuchengxiang
 * @date 2022/2/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSettingOperateRecord extends TemplateOperateRecord {
    /**
     * 旧的schema 设置
     */
    private JSONObject OldSettings;
    
    /**
     * 新的schema 设置
     */
    private   JSONObject newSetting;
    
    
    public TemplateSettingOperateRecord(IndexTemplatePhySetting oldConfig, IndexTemplatePhySetting newConfig) {
        this.operateType = TemplateOperateRecordEnum.SETTING.getCode();
        try {
            final List<Diff> diffs = DiffUtil.diffJson(
                    Optional.ofNullable(oldConfig).map(IndexTemplatePhySetting::getSettings)
                            .map(JSONAware::toJSONString)
                    
                            .orElse(""), Optional.ofNullable(newConfig).map(IndexTemplatePhySetting::getSettings)
                            .map(JSONAware::toJSONString)
                    
                            .orElse("")
    
            );
            
            diffResult.addAll(diffs);
        } catch (Exception ignore) {
        }
        OldSettings = oldConfig.getSettings();
        newSetting = newConfig.getSettings();
        
    }
    
     public TemplateSettingOperateRecord(IndexSettingVO oldSetting, IndexSettingVO newSettingVo) {
        OldSettings = oldSetting.getProperties();
        newSetting = newSettingVo.getProperties();
        try {
            final List<Diff> diffs = DiffUtil.diffJson(OldSettings.toJSONString(), newSetting.toJSONString());
            
            diffResult.addAll(diffs);
        } catch (Exception ignore) {
        }
        
        operateType = TemplateOperateRecordEnum.SETTING.getCode();
    }
    
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}