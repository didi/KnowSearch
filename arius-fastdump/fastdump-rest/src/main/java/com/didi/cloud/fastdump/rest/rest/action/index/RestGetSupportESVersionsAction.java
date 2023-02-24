package com.didi.cloud.fastdump.rest.rest.action.index;

import com.didi.cloud.fastdump.common.bean.common.Result;
import com.didi.cloud.fastdump.common.content.metadata.QueryContext;
import com.didi.cloud.fastdump.core.action.metadata.GetSupportESVersionsAction;
import com.didi.cloud.fastdump.rest.rest.BaseCommonHttpAction;
import org.elasticsearch.rest.RestChannel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.elasticsearch.rest.RestRequest.Method.GET;

@Component
public class RestGetSupportESVersionsAction extends BaseCommonHttpAction {
    private final GetSupportESVersionsAction getSupportESVersionAction;

    public RestGetSupportESVersionsAction(GetSupportESVersionsAction getSupportESVersionAction) {
        this.getSupportESVersionAction = getSupportESVersionAction;
    }

    @Override
    protected void register() {
        restHandlerFactory.registerHandler(GET, "support/es-versions", this);
    }

    @Override
    protected String name() {
        return "get-support-es-versions";
    }

    @Override
    protected void handleRequest(QueryContext queryContext, RestChannel channel) throws Exception {
        List<String> supportESVersionList = getSupportESVersionAction.doAction(null);
        sendResponse(channel, Result.build(true, supportESVersionList));
    }
}
