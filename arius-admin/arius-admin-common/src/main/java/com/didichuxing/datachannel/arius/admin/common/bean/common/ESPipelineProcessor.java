package com.didichuxing.datachannel.arius.admin.common.bean.common;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019-09-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESPipelineProcessor {

    @JSONField(name = "index_template")
    private JSONObject indexTemplate;

    @JSONField(name = "throttle")
    private JSONObject throttle;

}
