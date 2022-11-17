package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.didiglobal.knowframework.elasticsearch.client.response.cluster.getsetting.ESClusterGetSettingsResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ESClusterGetSettingsAllResponse extends ESClusterGetSettingsResponse {

    @JSONField(name = "defaults")
    private JSONObject defaults;

    public JSONObject getDefaults() {
        return defaults;
    }

    public void setDefaults(JSONObject defaults) {
        this.defaults = defaults;
    }

}
