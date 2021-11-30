package com.didi.arius.gateway.core.es.http.admin.indices.settings;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.didi.arius.gateway.common.exception.SettingsForbiddenException;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.ESAction;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;

@Component("restPutIndexSettingsAction")
public class RestPutIndexSettingsAction extends ESAction {

    public static final String NAME = "restPutIndexSettings";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel) throws Exception {
        String index = queryContext.getRequest().param("index");
        if (Strings.hasText(index) == false) {
            throw new IllegalArgumentException("index must not be null");
        }

        String settings = queryContext.getPostBody();
        if (StringUtils.isBlank(settings)) {
            throw new IllegalArgumentException("index settings must not be null");
        }

        String forbiddenSettings = dynamicConfigService.getForbiddenSettings();
        if (StringUtils.isNotBlank(forbiddenSettings)) {
            String[] forbiddenSettingList = forbiddenSettings.split(",");

            JSONObject settingJSONObj = JSON.parseObject(settings);

            for (String strSetting : forbiddenSettingList) {
                if (null != settingJSONObj.get(strSetting) || JSONPath.contains(settingJSONObj, strSetting)) {
                    throw new SettingsForbiddenException(forbiddenSettings + "be forbiddened setting by arius gateway interface!");
                }
            }
        }

        ESClient client;

        String[] indicesArr = Strings.splitStringByCommaToArray(index);
        List<String> indicesList = Lists.newArrayList(indicesArr);
        queryContext.setIndices(indicesList);
        checkIndices(queryContext);
        if (isIndexType(queryContext)) {
            IndexTemplate indexTemplate = getTemplateByIndexTire(indicesList, queryContext);
            client = esClusterService.getClient(queryContext, indexTemplate, actionName);
        } else {
            client = esClusterService.getClient(queryContext, actionName);
        }

        directRequest(client, queryContext);
    }
}
