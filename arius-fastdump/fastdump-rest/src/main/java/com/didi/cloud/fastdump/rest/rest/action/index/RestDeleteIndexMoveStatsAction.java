package com.didi.cloud.fastdump.rest.rest.action.index;

import static org.elasticsearch.rest.RestRequest.Method.DELETE;

import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.common.Result;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.content.metadata.QueryContext;
import com.didi.cloud.fastdump.common.exception.BaseException;
import com.didi.cloud.fastdump.core.action.metadata.DeleteIndexMoveStatsAction;
import com.didi.cloud.fastdump.rest.rest.BaseCommonHttpAction;

/**
 * Created by linyunan on 2022/9/5
 */
@Component
public class RestDeleteIndexMoveStatsAction extends BaseCommonHttpAction {
    private final DeleteIndexMoveStatsAction deleteIndexMoveStatsAction;

    public RestDeleteIndexMoveStatsAction(DeleteIndexMoveStatsAction deleteIndexMoveStatsAction) {
        this.deleteIndexMoveStatsAction = deleteIndexMoveStatsAction;
    }

    @Override
    protected void register() {
        restHandlerFactory.registerHandler(DELETE, "index-move/{taskId}/stats", this);
    }

    @Override
    protected String name() {
        return "delete-index-move-stats";
    }

    @Override
    protected void handleRequest(QueryContext queryContext, RestChannel channel) throws Exception {
        RestRequest request = queryContext.getRequest();
        String taskId = request.param("taskId");
        if (null == taskId || "".equals(taskId)) {
            throw new BaseException(String.format("taskId[%s] is illegal", taskId), ResultType.ILLEGAL_PARAMS);
        }

        deleteIndexMoveStatsAction.doAction(taskId);
        sendResponse(channel, Result.buildSucc());
    }
}
