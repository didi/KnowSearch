package com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySettings;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TemplateOperateRecordEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * @author liuchengxiang
 * @date 2022/2/17
 */
@Data
@NoArgsConstructor
public class TemplateSettingOperateRecord extends TemplateOperateRecord {

    public TemplateSettingOperateRecord(IndexTemplatePhySettings oldConfig, IndexTemplatePhySettings newConfig) {
        this.operateType = TemplateOperateRecordEnum.SETTING.getCode();

        StringBuilder settingChange = new StringBuilder();
        Optional<JSONObject> oldSetting = Optional.ofNullable(oldConfig.getSettings())
            .map(json -> json.getJSONObject("index"));
        Optional<JSONObject> newSetting = Optional.ofNullable(newConfig.getSettings())
            .map(json -> json.getJSONObject("index"));
        Optional<Integer> newSettingShardNum = newSetting.map(json -> json.getInteger("number_of_replicas"));
        Integer oldSettingShardNum = oldSetting.map(json -> json.getInteger("number_of_replicas")).orElse(null);
        newSettingShardNum.ifPresent(newShardNum -> {
            if (!Objects.equals(newShardNum, oldSettingShardNum)) {
                settingChange.append(newShardNum > 0 ? "关闭取消副本" : "开启取消副本").append("，");
            }
        });
        Optional<String> newSettingTranslog = newSetting.map(json -> json.getJSONObject("translog"))
            .map(json -> json.getString("durability"));
        String oldSettingTranslog = oldSetting.map(json -> json.getJSONObject("translog"))
            .map(json -> json.getString("durability")).orElse("");
        newSettingTranslog.ifPresent(newTranslog -> {
            if (!StringUtils.equals(oldSettingTranslog, newTranslog)) {
                settingChange.append(newTranslog.equals("async") ? "开启异步translog" : "关闭异步translog");
            }
        });
        this.operateDesc = settingChange.toString();
    }

}
