package com.didi.cloud.fastdump.rest.http;

import com.didi.cloud.fastdump.rest.rest.RestHandler;
import org.elasticsearch.http.HttpChannel;
import org.elasticsearch.http.HttpRequest;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * Created by linyunan on 2022/8/4
 */
@Component
public class HttpRequestAllocator extends BaseRequestAllocator<HttpRequest, HttpChannel>{
    @Autowired
    private RestHandlerFactory restHandlerFactory;

    public void dispatchRequest(HttpRequest request, HttpChannel channel) {
        RestHandler restHandler = restHandlerFactory.tryAllHandlers(request);
        if (restHandler != null) {
            restHandler.dispatchRequest(request, channel);
        } else {
            channel.sendResponse(new BytesRestResponse(RestStatus.NOT_FOUND,
                    String.format("unable to find uri=%s, rawPath=%s", request.uri(), request.rawPath())));
        }
    }
}
