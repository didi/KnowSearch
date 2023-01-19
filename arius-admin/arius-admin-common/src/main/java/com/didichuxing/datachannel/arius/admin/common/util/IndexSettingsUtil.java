package com.didichuxing.datachannel.arius.admin.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONValidator;
import com.alibaba.fastjson.JSONValidator.Type;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.ESSettingConstant;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didiglobal.knowframework.elasticsearch.client.utils.JsonUtils;

public class IndexSettingsUtil {
    final static Set<String> ignoreSettings = Sets.newHashSet(
            ESSettingConstant.INDEX_VERIFIED_BEFORE_CLOSE,
            ESSettingConstant.INDEX_VERSION_CREATED,
            ESSettingConstant.INDEX_VERSION,
            ESSettingConstant.INDEX_UUID,
            ESSettingConstant.INDEX_CREATION_DATE,
            ESSettingConstant.INDEX_PROVIDED_NAME);

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
    
    /**
     * > 检查设置是否不可变且正确
     *
     * @param setting 指标设置
     * @param projectId 项目编号
     */
    public static void checkImmutableSettingAndCorrectSetting(String setting, IndexSettingVO indexSettingVO, Integer projectId) throws ESOperateException {
        final JSONValidator from = JSONValidator.from(setting);
        if (!(from.validate() && from.getType().equals(Type.Object))) {
            throw new ESOperateException("不是正确的 setting");
        }
        final Map<String, String> stringMap = JsonUtils.flat(JSON.parseObject(setting));
        for (String key : stringMap.keySet()) {
            if (StringUtils.startsWith(key, ESSettingConstant.INDEX_ROUTING_ALLOCATION_PREFIX) &&
                    !AuthConstant.SUPER_PROJECT_ID.equals(projectId) &&
                    !stringMap.get(key).equals(JsonUtils.flat(JSON.parseObject(indexSettingVO.getProperties().toJSONString())).get(key))
            ) {
                throw new ESOperateException(String.format("{%s}不允许修改", key));
            }
        }
    }
}