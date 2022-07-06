package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import com.alibaba.fastjson.JSONObject;
import com.didiglobal.logi.elasticsearch.client.utils.JsonUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 物理模板Settings Entity.
 * @author wangshu
 * @date 2020/08/24
 */
@Data
@NoArgsConstructor
public class IndexTemplatePhySetting implements Serializable {
    private JSONObject settings;


    /**
     * 构造函数
     * @param settings JSON settings
     */
    public IndexTemplatePhySetting(JSONObject settings) {
        this.settings = settings;
    }

    public IndexTemplatePhySetting(Map<String, String> settings) {
        reset(settings);
    }

    /**
     * 重置settings信息
     * @param currentSettings 最新settings信息
     */
    public void reset(Map<String, String> currentSettings) {
        if (currentSettings != null) {
            settings = JsonUtils.reFlat(currentSettings);
        } else {
            settings = null;
        }
    }

    /**
     * 扁平化settings.
     * @return
     */
    public Map<String, String> flatSettings() {
        return JsonUtils.flat(settings);
    }

    /**
     * merge增量settings信息
     * @param incrementalSettings 增量settings.
     */
    public Map<String, String> merge(Map<String, String> incrementalSettings) {
        Map<String, String> finalSettings = flatSettings();
        if (incrementalSettings != null) {
            finalSettings.putAll(incrementalSettings);
            reset(finalSettings);
        }
        return finalSettings;
    }

}
