package com.didichuxing.datachannel.arius.admin.common.bean.entity.index.setting;

import com.didiglobal.logi.elasticsearch.client.model.RestRequest;
import com.didiglobal.logi.elasticsearch.client.request.index.getindex.ESIndicesGetIndexRequest;

/**
 * @author lyn
 * @date 2021/10/03
 **/
public class ESIndicesGetAllSettingRequest extends ESIndicesGetIndexRequest {
    private boolean defaultSettingFlag = false;

    public void setDefaultSettingFlag(boolean defaultSettingFlag) {
        this.defaultSettingFlag = defaultSettingFlag;
    }

    @Override
    public RestRequest toRequest() throws Exception {
        RestRequest restRequest = super.toRequest();
        String endPoint = "";
        if (defaultSettingFlag) {
            endPoint = restRequest.getEndpoint() + "?include_defaults=true&pretty";
        }
        return new RestRequest("GET", endPoint, null);
    }
}
