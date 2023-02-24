package com.didi.cloud.fastdump.rest.rest.action.index;

import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didi.cloud.fastdump.common.bean.common.Result;
import com.didi.cloud.fastdump.common.bean.taskcontext.es.ESIndexMoveTaskActionContext;
import com.didi.cloud.fastdump.common.content.metadata.QueryContext;
import com.didi.cloud.fastdump.core.action.movetask.ESIndexMoveTaskSubmitAction;
import com.didi.cloud.fastdump.rest.rest.BaseCommonHttpAction;

/**
 * Created by linyunan on 2022/8/24
 */
@Component
public class RestESIndexMoveTaskSubmitAction extends BaseCommonHttpAction {
    @Autowired
    private ESIndexMoveTaskSubmitAction esIndexMoveTaskSubmitAction;

    @Override
    protected void register() {
        restHandlerFactory.registerHandler(RestRequest.Method.POST, "/index-move/submit", this);
    }

    @Override
    protected String name() {
        return "index-move-submit";
    }

    @Override
    protected void handleRequest(QueryContext queryContext, RestChannel channel) throws Exception {
        ESIndexMoveTaskActionContext esIndexMoveTaskActionContext =
                JSON.parseObject(queryContext.getPostBody(), ESIndexMoveTaskActionContext.class);

        String taskId = esIndexMoveTaskSubmitAction.doAction(esIndexMoveTaskActionContext);
        sendResponse(channel, Result.build(true, taskId));
    }
}
