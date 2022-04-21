package com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySettings;
import com.didichuxing.datachannel.arius.admin.common.constant.TemplateOperateRecordEnum;
import com.didichuxing.datachannel.arius.admin.common.util.MapUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * @author liuchengxiang
 * @date 2022/2/17
 */
@Data
@NoArgsConstructor
public class TemplateSettingOperateRecord extends TemplateOperateRecord {

    public TemplateSettingOperateRecord(IndexTemplatePhySettings oldConfig, IndexTemplatePhySettings newConfig ) {
        this.operateType = TemplateOperateRecordEnum.SETTING.getCode();

        StringBuilder settingChange = new StringBuilder();
        JSONObject oldSetting = oldConfig.getSettings().getJSONObject("index");
        JSONObject newSetting = newConfig.getSettings().getJSONObject("index");

        Integer newSettingShardNum = newSetting.getInteger("number_of_replicas");
        Integer oldSettingShardNum = oldSetting.getInteger("number_of_replicas");
        if (oldSettingShardNum != newSettingShardNum) {
            settingChange.append(newSettingShardNum > 0 ? "关闭取消副本": "开启取消副本").append("，");
        }

        String newSettingTranslog = newSetting.getJSONObject("translog").getString("durability");
        String oldSettingTranslog = oldSetting.getJSONObject("translog").getString("durability");
        if (!oldSettingTranslog.equals(newSettingTranslog)) {
            settingChange.append(newSettingTranslog.equals("async") ? "开启异步translog" : "关闭异步translog");
        }
        this.operateDesc = settingChange.toString();
    }

}
