package com.didi.arius.gateway.elasticsearch.client.model;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;

import java.io.IOException;
import java.io.InputStream;

public class RestResponse {
    private Response response;

    public RestResponse(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public String getResponseContent() {
        String content = null;
        try {
            if (response.getEntity() == null) {
                return null;
            }

            content = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            content = "{}";
        }

        return content;
    }

    public InputStream getResponseStream() throws IOException {
        return response.getEntity().getContent();
    }

    public int getStatusCode() {
        return response.getStatusLine().getStatusCode();
    }
}
