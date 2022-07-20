package com.didichuxing.datachannel.arius.admin.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.StringUtils;

public class IndexSettingsUtil {
    final static Set<String> ignoreSettings = Sets.newHashSet("index.verified_before_close", "index.version.created",
        "index.version", "index.uuid", "index.creation_date", "index.provided_name", "index.number_of_shards");

    /**
     * 排除忽视设置
     *
     * @param settings 设置
     * @return {@link Map}<{@link String}, {@link String}>
     */
    public static Map<String, String> excludeIgnoreSettings(Map<String, String> settings) {
        Map<String, String> result = new HashMap<>();
        if (null != settings && !settings.isEmpty()) {
            settings.forEach((key, val) -> {
                if (!ignoreSettings.contains(key)) {
                    result.put(key, val);
                }
            });
        }
        return result;
    }

    public static Map<String, String> getChangedSettings(Map<String, String> sourceSettings,
                                                         Map<String, String> changedSettings) {
        Map<String, String> result = new HashMap<>();
        if (null != changedSettings && !changedSettings.isEmpty()) {
            Map<String, String> finalSourceSettings = sourceSettings != null ? sourceSettings : new HashMap<>();
            changedSettings.forEach((key, val) -> {
                if (!StringUtils.equals(val, finalSourceSettings.get(key))) {
                    result.put(key, val);
                }
            });
        }
        return excludeIgnoreSettings(result);
    }

}
