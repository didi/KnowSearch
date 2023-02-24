package com.didi.cloud.fastdump.rest.rest.action.index;

import static org.elasticsearch.rest.RestRequest.Method.PUT;

import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.common.Result;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.content.metadata.QueryContext;
import com.didi.cloud.fastdump.common.exception.BaseException;
import com.didi.cloud.fastdump.core.action.movetask.PauseIndexNodeMoveAction;
import com.didi.cloud.fastdump.rest.rest.BaseCommonHttpAction;

/**
 * Created by linyunan on 2022/9/5
 */
@Component
public class RestPauseIndexNodeMoveAction extends BaseCommonHttpAction {
    private final PauseIndexNodeMoveAction pauseIndexNodeMoveAction;

    public RestPauseIndexNodeMoveAction(PauseIndexNodeMoveAction pauseIndexNodeMoveAction) {
        this.pauseIndexNodeMoveAction = pauseIndexNodeMoveAction;
    }

    @Override
    protected void register() {
        restHandlerFactory.registerHandler(PUT, "index-node-move/{taskId}/stop", this);
    }

    @Override
    protected String name() {
        return "stop-index-move-stats";
    }

    @Override
    protected void handleRequest(QueryContext queryContext, RestChannel channel) throws Exception {
        RestRequest request = queryContext.getRequest();
        String taskId = request.param("taskId");
        if (null == taskId || "".equals(taskId)) {
            throw new BaseException(String.format("taskId[%s] is illegal", taskId), ResultType.ILLEGAL_PARAMS);
        }

        pauseIndexNodeMoveAction.doAction(taskId);
        sendResponse(channel, Result.buildSucc());
    }
}
