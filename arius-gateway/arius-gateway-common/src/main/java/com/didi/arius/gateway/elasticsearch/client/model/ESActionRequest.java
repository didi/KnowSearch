package com.didi.arius.gateway.elasticsearch.client.model;

import com.didi.arius.gateway.common.utils.CommonUtil;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.action.ActionRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ESActionRequest<T extends ESActionRequest> extends ActionRequest {
    protected ESActionRequest() {
        super();
    }

    protected ESActionRequest(ESActionRequest request) {
        super(request);
    }

    public abstract RestRequest toRequest() throws Exception;

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

    public abstract ESActionResponse toResponse(RestResponse response) throws Exception;

    public boolean checkResponse(org.elasticsearch.client.Response response) {
        int status = response.getStatusLine().getStatusCode();
        boolean res = false;
        if (status == 200 || status == 202 || status == 201) {
            res = true;
        }
        return res;
    }

    public ESActionResponse buildResponse(RestResponse response) throws Exception {
        ESActionResponse esActionResponse = toResponse(response);
        esActionResponse.setRestStatus( CommonUtil.fromCode(response.getStatusCode()));
        esActionResponse.setHost(response.getResponse().getHost());

        return  esActionResponse;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }
}
