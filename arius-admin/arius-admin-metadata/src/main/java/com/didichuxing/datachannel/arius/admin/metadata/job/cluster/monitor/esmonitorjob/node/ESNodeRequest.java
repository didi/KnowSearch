package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionRequestValidationException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didiglobal.knowframework.elasticsearch.client.model.ESActionResponse;
import com.didiglobal.knowframework.elasticsearch.client.model.RestRequest;
import com.didiglobal.knowframework.elasticsearch.client.model.RestResponse;

public class ESNodeRequest extends BaseTimeoutRequest<ESNodeRequest> {
    private String      nodeIds;
    private Set<String> flags = new HashSet<>();

    public ESNodeRequest clear() {
        flags.clear();
        return this;
    }

    public ESNodeRequest flag(String name) {
        flags.add(name);
        return this;
    }

    public ESNodeRequest nodeIds(String nodeIds) {
        this.nodeIds = nodeIds;
        return this;
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    @Override
    public RestRequest toRequest() throws Exception {
        String endpoint = buildEndPoint();
        RestRequest restRequest = new RestRequest("GET", endpoint, null);
        //加上超时时间
        restRequest.getParams().put("timeout", timeout);
        return restRequest;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        String respStr = response.getResponseContent();
        JSONObject obj = JSON.parseObject(respStr);
        Object nodes = obj.remove("_nodes");
        ESNodeResponse esNodeResponse = JSON.parseObject(obj.toJSONString(), ESNodeResponse.class);
        if (null != nodes) {
            esNodeResponse.setFailedNodes(((JSONObject) nodes).getInteger("failed"));
        }
        return JSON.parseObject(obj.toJSONString(), ESNodeResponse.class);
    }

    private String buildEndPoint() {
        String flagStr = flags.size() < 10 ? StringUtils.join(flags, ",").trim() : null;
        String nodeUrl = null == nodeIds ? "_nodes" : String.format("_nodes/%s", nodeIds);
        String finalUrl = null == flagStr ? nodeUrl : nodeUrl + "/" + flagStr;
        return finalUrl;
    }
}
