package com.didi.cloud.fastdump.rest.rest;

import javax.annotation.PostConstruct;

import com.alibaba.fastjson.JSON;
import com.didi.cloud.fastdump.common.bean.common.Result;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.didi.cloud.fastdump.common.content.metadata.QueryContext;
import com.didi.cloud.fastdump.common.utils.HostUtil;
import com.didi.cloud.fastdump.rest.http.RestHandlerFactory;

/**
 * Created by linyunan on 2022/8/4
 */
public abstract class BaseRestHttpAction implements RestHandler {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseRestHttpAction.class);
    @Value("${fastdump.httpTransport.port:8300}")
    protected int                 httpPort;
    @Autowired
    protected RestHandlerFactory  restHandlerFactory;

    @PostConstruct
    public void init() { register();}

    @Override
    public void dispatchRequest(RestRequest request, RestChannel channel) {
        QueryContext queryContext = parseContext(request, channel);
        try {
            checkToken(queryContext);

            preRequest(queryContext);

            handleRequest(queryContext, channel);

            postRequest(queryContext);
        } catch (Exception ex) {
            preException(queryContext, ex);
            channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR,
                    JSON.toJSONString(Result.buildFail(ex.getMessage()))));
        }
    }

    protected abstract void register();

    protected abstract void handleRequest(QueryContext queryContext, RestChannel channel) throws Exception;

    protected void preException(QueryContext queryContext, Exception ex) {

    }

    protected void postRequest(QueryContext queryContext) {
    }

    protected void preRequest(QueryContext queryContext) {
    }

    protected QueryContext parseContext(RestRequest request, RestChannel channel) {
        QueryContext context = new QueryContext();

        context.setRequest(request);
        context.setChannel(channel);
        context.setMethod(request.method());

        context.setRestName(name());

        String uri = request.uri();
        int pathEndPos = uri.indexOf('?');
        String queryStr = pathEndPos > 0 ? uri.substring(pathEndPos + 1) : "";
        context.setQueryString(queryStr);

        String postBody = request.content().toUtf8() == null ? "" : request.content().toUtf8();
        context.setPostBody(postBody);

        String remoteAddr = HostUtil.getClientIP(request);
        context.setRemoteAddr(remoteAddr);

        return context;
    }

    protected abstract String name();

    protected void checkToken(QueryContext queryContext) {
        // do nothing
    }
}
