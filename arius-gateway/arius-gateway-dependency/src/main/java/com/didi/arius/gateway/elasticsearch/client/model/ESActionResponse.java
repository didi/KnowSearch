package com.didi.arius.gateway.elasticsearch.client.model;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class ESActionResponse extends ActionResponse {
    private RestStatus restStatus;

    private Map<String, Object> otherFields = new HashMap<>();

    public RestStatus getRestStatus() {
        return restStatus;
    }

    public void setRestStatus(RestStatus restStatus) {
        this.restStatus = restStatus;
    }

    public Map<String, Object> getOtherFields() {
        return otherFields;
    }

    public void setOtherFields(Map<String, Object> otherFields) {
        this.otherFields = otherFields;
    }

    public void putOtherFields(String key, Object value) {
        otherFields.put(key, value);
    }

    public org.elasticsearch.rest.RestResponse buildRestResponse(RestChannel channel) {
        if (this instanceof ToXContent) {
            try {
                XContentBuilder builder = channel.newBuilder();
                ((ToXContent)this).toXContent(builder, channel.request());
                return new BytesRestResponse(getRestStatus(), builder);
            } catch (IOException e) {
                return new BytesRestResponse(getRestStatus(), XContentType.JSON.restContentType(), toString());
            }
        } else {
            return new BytesRestResponse(getRestStatus(), XContentType.JSON.restContentType(), toString());
        }

    }
}
