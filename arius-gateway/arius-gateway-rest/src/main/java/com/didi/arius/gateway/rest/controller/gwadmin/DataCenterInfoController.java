package com.didi.arius.gateway.rest.controller.gwadmin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.metadata.ESCluster;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.rest.controller.AdminController;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Set;

/**
 * @author fitz
 * @date 2021/5/25 2:40 下午
 */
@Controller
public class DataCenterInfoController extends AdminController {
    public static final String NAME = "dataCenterInfo";

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.GET, "/_gwadmin/datacenterinfo", this);
    }

    @Override
    protected String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
        Set<Map.Entry<String, ESCluster>> entries = esRestClientService.getESClusterMap().entrySet();
        JSONArray jr = new JSONArray(entries.size());
        for (Map.Entry<String, ESCluster> entry : entries) {
            JSONObject json = new JSONObject();
            ESCluster esCluster = entry.getValue();
            json.put("cluster", esCluster.getCluster());
            json.put("readAddress", esCluster.getHttpAddress());
            jr.add(json);
        }

        sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, jr.toJSONString()));

    }
}
