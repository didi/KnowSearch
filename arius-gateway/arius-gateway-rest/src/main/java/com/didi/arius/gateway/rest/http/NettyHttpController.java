package com.didi.arius.gateway.rest.http;

import com.didi.arius.gateway.rest.controller.es.RestCommonController;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.elasticsearch.http.HttpChannel;
import org.elasticsearch.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("nettyHttpController")
public class NettyHttpController {

    @Autowired
    private RestController restController;

    @Autowired
    private RestCommonController restCommonController;

    public void dispatchRequest(HttpRequest request, HttpChannel channel) {
        try {
            LogFactory.setUniqueFlag();

            IRestHandler restHandler;
            if (request.rawPath().startsWith("/_xpack")) {
                restHandler = restCommonController;
            } else {
                restHandler = restController.tryAllHandlers(request);
            }

            if (restHandler == null) {
                restHandler = restCommonController;
                // throw new UriNotFoundException("No handler found for uri [" + request.rawPath() + "] and method [" + request.method() + "]");
            }

            restHandler.dispatchRequest(request, channel);
        } finally {
            LogFactory.removeFlag();
        }
    }
}
