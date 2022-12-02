package com.didi.cloud.fastdump.rest.rest.action.template;

import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didi.cloud.fastdump.common.bean.common.Result;
import com.didi.cloud.fastdump.common.bean.taskcontext.es.ESTemplateMoveTaskActionContext;
import com.didi.cloud.fastdump.common.content.metadata.QueryContext;
import com.didi.cloud.fastdump.core.action.movetask.ESTemplateMoveTaskSubmitAction;
import com.didi.cloud.fastdump.rest.rest.BaseCommonHttpAction;

/**
 * Created by linyunan on 2022/8/24
 */
@Component
public class RestESTemplateMoveTaskStartAction extends BaseCommonHttpAction {
    private final ESTemplateMoveTaskSubmitAction esTemplateMoveTaskSubmitAction;

    public RestESTemplateMoveTaskStartAction(ESTemplateMoveTaskSubmitAction esTemplateMoveTaskSubmitAction) {
        this.esTemplateMoveTaskSubmitAction = esTemplateMoveTaskSubmitAction;
    }

    @Override
    protected void register() {
        restHandlerFactory.registerHandler(RestRequest.Method.POST, "/template-move/start", this);
    }

    @Override
    protected String name() {
        return "template-move-start";
    }

    @Override
    protected void handleRequest(QueryContext queryContext, RestChannel channel) throws Exception {
        ESTemplateMoveTaskActionContext esTemplateMoveTaskActionContext = JSON.parseObject(queryContext.getPostBody(),
                ESTemplateMoveTaskActionContext.class);

        String taskId = esTemplateMoveTaskSubmitAction.doAction(esTemplateMoveTaskActionContext);
        sendResponse(channel, Result.build(true, taskId));
    }
}
