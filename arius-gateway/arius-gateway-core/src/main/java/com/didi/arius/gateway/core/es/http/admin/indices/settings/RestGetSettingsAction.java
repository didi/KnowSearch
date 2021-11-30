package com.didi.arius.gateway.core.es.http.admin.indices.settings;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.ESAction;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.springframework.stereotype.Component;

/**
 * @author zhaoqingrong
 * @date 2021/6/8
 * @desc 招行需求，开放 restGetSettingsAction 给普通账号
 */
@Component("restGetSettingsAction")
public class RestGetSettingsAction extends ESAction {

    @Override
    public String name() {
        return "restGetSettingsAction";
    }

    @Override
    protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel) throws Exception {
        String getSettingIndex = queryContext.getRequest().param("index");

        if (StringUtils.isNotBlank(getSettingIndex)) {
            indexAction(queryContext, getSettingIndex);
        } else {
            throw new IllegalArgumentException("index must not be null when arius gateway in index mode");
        }
    }
}
