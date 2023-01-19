package com.didi.arius.gateway.rest.http;

import org.elasticsearch.http.HttpChannel;
import org.elasticsearch.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didi.arius.gateway.rest.controller.es.RestCommonController;
import com.didiglobal.knowframework.log.LogFactory;

@Component("nettyHttpController")
public class NettyHttpController {

    @Autowired
    private RestController restController;

    @Autowired
    private RestCommonController restCommonController;

    public NettyHttpController() {
        // pass
    }
    public void dispatchRequest(HttpRequest request, HttpChannel channel) {
        try {
            LogFactory.setUniqueFlag();
            handleRequest(request, channel);
        } finally {
            LogFactory.removeFlag();
        }
    }

    private void handleRequest(HttpRequest request, HttpChannel channel) {
        IRestHandler restHandler;
        if (request.rawPath().startsWith("/_xpack")) {
            restHandler = restCommonController;
        } else {
            restHandler = restController.tryAllHandlers(request);
        }

        if (restHandler == null) {
            restHandler = restCommonController;
        }

        if (restHandler != null) {
            restHandler.dispatchRequest(request, channel);
        }
    }
}
