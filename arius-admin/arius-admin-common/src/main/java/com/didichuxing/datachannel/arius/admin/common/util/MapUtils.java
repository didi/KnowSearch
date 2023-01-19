package com.didichuxing.datachannel.arius.admin.common.util;

import java.util.HashMap;
import java.util.Map;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

public class MapUtils {

    private static final ILog LOGGER = LogFactory.getLog(MapUtils.class);

    public static String findChanged(Map<String, String> src, Map<String, String> dest) {
        if (src == null || dest == null) {
            return "";
        }

        StringBuilder content = new StringBuilder("");
        try {
            for (String key : src.keySet()) {
                String destValue = dest.get(key);
                String srcValue = src.get(key);
                if (null != destValue) {
                    if (!destValue.equals(srcValue)) {
                        content.append("字段").append(key).append("的原值").append("【").append(srcValue).append("】")
                            .append("修改为").append("【").append(destValue).append("】").append("\r\n");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("class=MapUtils||method=findChanged||errMsg={}", e.getMessage(), e);
        }
        return content.toString();
    }

    public static Map<String, String> findChangedWithDestV(Map<String, String> src, Map<String, String> dest) {
        Map<String, String> result = new HashMap<>();
        if (src == null || dest == null) {
            return result;
        }
        try {
            for (String key : src.keySet()) {
                String destValue = dest.get(key);
                String srcValue = src.get(key);
                if (null != destValue) {
                    if (!destValue.equals(srcValue)) {
                        result.put(key, destValue);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("class=MapUtils||method=findChanged||errMsg={}", e.getMessage(), e);
        }
        return result;
    }

}
