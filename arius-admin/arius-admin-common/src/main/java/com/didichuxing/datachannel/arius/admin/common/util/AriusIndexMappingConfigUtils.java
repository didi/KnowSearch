package com.didichuxing.datachannel.arius.admin.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;

import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @author wangshu
 * @date 2020/06/14
 */
public class AriusIndexMappingConfigUtils {

    private AriusIndexMappingConfigUtils(){}

    private static final ILog LOGGER = LogFactory.getLog(AriusIndexMappingConfigUtils.class);

    private static final String MAPPING_STR  = "mapping";
    private static final String MAPPINGS_STR = "mappings";

    /**
     * 解析索引mapping config.
     * @param mappingConfig mapping config JSON序列化内容
     * @return
     */
    public static Result<MappingConfig> parseMappingConfig(String mappingConfig) {
        try {
            return Result.buildSucc(new MappingConfig(getMappingObj(JSON.parseObject(mappingConfig))));
        } catch (Exception t) {
            LOGGER.warn(
                    "class=AriusIndexMappingConfigUtils||method=parseMappingConfig||" +
                            "mappingConfig={}||exception={}", mappingConfig, t);
            if (t instanceof JSONException) {
                return Result.build(ResultType.FAIL.getCode(), "json解析失败");
            }
            return Result.build(ResultType.FAIL.getCode(), t.getMessage());
        }
    }

    private static JSONObject getMappingObj(JSONObject obj) {
        if (obj.containsKey(MAPPING_STR)) {
            return obj.getJSONObject(MAPPING_STR);
        }

        if (obj.containsKey(MAPPINGS_STR)) {
            return obj.getJSONObject(MAPPINGS_STR);
        }

        return obj;
    }
}
