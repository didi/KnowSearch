package com.didi.arius.gateway.elasticsearch.client.model;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.rest.RestStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ESActionRequest<T extends ESActionRequest> extends ActionRequest {
    public ESActionRequest() {
        super();
    }

    protected ESActionRequest(ESActionRequest request) {
        super(request);
    }

    abstract public RestRequest toRequest() throws Exception;

    private int socketTimeout;

    public RestRequest buildRequest(List<Header> headers) throws Exception {
        RestRequest restRequest = toRequest();
        List<Header> newHeaders = new ArrayList<>();
        newHeaders.addAll(headers);
        if (this.headers != null) {
            for (Map.Entry<String, Object> entry : this.headers.entrySet()) {
                Header header = new BasicHeader(entry.getKey(), entry.getValue().toString());
                newHeaders.add(header);
            }
        }

        if (socketTimeout > 0) {
            restRequest.setSocketTimeOut(socketTimeout);
        }

        restRequest.setHeaders(newHeaders);
        return restRequest;
    }

    abstract public ESActionResponse toResponse(RestResponse response) throws Exception;

    public boolean checkResponse(org.elasticsearch.client.Response response) {
        int status = response.getStatusLine().getStatusCode();

        if (status == 200 || status == 202 || status == 201) {
            return true;
        } else {
            return false;
        }
    }

    public ESActionResponse buildResponse(RestResponse response) throws Exception {
        ESActionResponse esActionResponse = toResponse(response);
        esActionResponse.setRestStatus(ESActionRequest.fromCode(response.getStatusCode()));

        return esActionResponse;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    private static RestStatus fromCode(int code) {
        for (RestStatus restStatus : RestStatus.values()) {
            if (code == restStatus.getStatus()) {
                return restStatus;
            }
        }

        return null;
    }
}
