package com.didi.cloud.fastdump.rest.rest.action.template;

import static org.elasticsearch.rest.RestRequest.Method.GET;

import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.common.Result;
import com.didi.cloud.fastdump.common.bean.stats.TemplateMoveTaskStats;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.content.metadata.QueryContext;
import com.didi.cloud.fastdump.common.exception.BaseException;
import com.didi.cloud.fastdump.core.action.metadata.GetTemplateMoveStatsAction;
import com.didi.cloud.fastdump.rest.rest.BaseCommonHttpAction;

/**
 * Created by linyunan on 2022/9/5
 */
@Component
public class RestGetTemplateMoveStatsAction extends BaseCommonHttpAction {
    private final GetTemplateMoveStatsAction getTemplateMoveStatsAction;

    public RestGetTemplateMoveStatsAction(GetTemplateMoveStatsAction getTemplateMoveStatsAction) {
        this.getTemplateMoveStatsAction = getTemplateMoveStatsAction;
    }

    @Override
    protected void register() {
        restHandlerFactory.registerHandler(GET, "template-move/{taskId}/stats", this);
    }

    @Override
    protected String name() {
        return "get-template-move-stats";
    }

    @Override
    protected void handleRequest(QueryContext queryContext, RestChannel channel) throws Exception {
        RestRequest request = queryContext.getRequest();
        String taskId = request.param("taskId");
        if (null == taskId || "".equals(taskId)) {
            throw new BaseException(String.format("taskId[%s] is illegal", taskId), ResultType.ILLEGAL_PARAMS);
        }

        TemplateMoveTaskStats templateMoveTaskStats = getTemplateMoveStatsAction.doAction(taskId);
        sendResponse(channel, Result.build(true, templateMoveTaskStats));
    }
}
