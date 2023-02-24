package com.didi.cloud.fastdump.rest.rest.action;

import static org.elasticsearch.rest.RestRequest.Method.GET;

import org.elasticsearch.rest.RestChannel;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.common.Result;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.content.metadata.QueryContext;
import com.didi.cloud.fastdump.rest.rest.BaseCommonHttpAction;

/**
 * Created by linyunan on 2022/9/23
 */
@Component
public class RestFastDumpHealthCheckAction extends BaseCommonHttpAction {
    @Override
    protected void register() {
        restHandlerFactory.registerHandler(GET, "/check-health", this);
    }

    @Override
    protected String name() {
        return "check-health";
    }

    @Override
    protected void handleRequest(QueryContext queryContext, RestChannel channel) throws Exception {
        sendResponse(channel, Result.build(ResultType.FAST_DUMP_EXIST));
    }
}
