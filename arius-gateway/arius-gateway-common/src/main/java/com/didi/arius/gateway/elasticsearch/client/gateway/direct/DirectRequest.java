package com.didi.arius.gateway.elasticsearch.client.gateway.direct;

import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import org.elasticsearch.action.ActionRequestValidationException;

import java.util.HashMap;
import java.util.Map;

public class DirectRequest extends ESActionRequest<DirectRequest> {
    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    private String method;

    private String uri;

    private String postContent;

    private Map<String, String> params = new HashMap<>();

    public DirectRequest() {

    }

    public DirectRequest(String method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    @Override
    public RestRequest toRequest() throws Exception {
        RestRequest rr = new RestRequest(method, uri);
        rr.setParams(params);
        rr.setBody(postContent);
        return rr;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        String respStr = response.getResponseContent();
        return new DirectResponse(respStr);
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }
}
