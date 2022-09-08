package com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONAware;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySetting;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TemplateOperateRecordEnum;
import com.didichuxing.datachannel.arius.admin.common.util.DiffUtil;
import com.didichuxing.datachannel.arius.admin.common.util.DiffUtil.DiffJson;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author liuchengxiang
 * @date 2022/2/17
 */
@Data
public class TemplateSettingOperateRecord extends TemplateOperateRecord {

    public TemplateSettingOperateRecord(IndexTemplatePhySetting oldConfig, IndexTemplatePhySetting newConfig) {
        this.operateType = TemplateOperateRecordEnum.SETTING.getCode();
        source = oldConfig.getSettings();
        target = newConfig.getSettings();
        final List<DiffJson> diffs = DiffUtil.diffJsonByString(
            Optional.ofNullable(oldConfig).map(IndexTemplatePhySetting::getSettings).map(JSONAware::toJSONString)

                .orElse(""),
            Optional.ofNullable(newConfig).map(IndexTemplatePhySetting::getSettings).map(JSONAware::toJSONString)

                .orElse("")

        );

        diffResult.addAll(diffs);
        change = CollectionUtils.isNotEmpty(diffs);
        this.behavior="编辑SETTING";
        

    }

    public TemplateSettingOperateRecord(IndexSettingVO oldSetting, IndexSettingVO newSettingVo) {
        source = oldSetting.getProperties();
        target = newSettingVo.getProperties();
        final List<DiffJson> diffs = DiffUtil.diffJsonByString(source.toJSONString(), target.toJSONString());

        diffResult.addAll(diffs);
        operateType = TemplateOperateRecordEnum.SETTING.getCode();
        change = CollectionUtils.isNotEmpty(diffs);
        this.behavior="编辑SETTING";
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}