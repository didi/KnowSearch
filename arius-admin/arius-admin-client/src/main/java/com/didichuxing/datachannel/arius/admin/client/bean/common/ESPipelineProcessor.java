package com.didichuxing.datachannel.arius.admin.client.bean.common;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-09-03
 */
@Data
public class ESPipelineProcessor {

    @JSONField(name = "index_template")
    private JSONObject indexTemplate;

    @JSONField(name = "throttle")
    private JSONObject throttle;

}
