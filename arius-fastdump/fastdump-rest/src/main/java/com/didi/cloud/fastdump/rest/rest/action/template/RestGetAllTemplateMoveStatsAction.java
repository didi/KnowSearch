package com.didi.cloud.fastdump.rest.rest.action.template;

import static org.elasticsearch.rest.RestRequest.Method.GET;

import java.util.List;

import org.elasticsearch.rest.RestChannel;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.common.Result;
import com.didi.cloud.fastdump.common.bean.stats.TemplateMoveTaskStats;
import com.didi.cloud.fastdump.common.content.metadata.QueryContext;
import com.didi.cloud.fastdump.core.action.metadata.GetAllTemplateMoveStatsAction;
import com.didi.cloud.fastdump.rest.rest.BaseCommonHttpAction;

/**
 * Created by linyunan on 2022/9/5
 */
@Component
public class RestGetAllTemplateMoveStatsAction extends BaseCommonHttpAction {
    private final GetAllTemplateMoveStatsAction getAllTemplateMoveStatsAction;

    public RestGetAllTemplateMoveStatsAction(GetAllTemplateMoveStatsAction getAllTemplateMoveStatsAction) {
        this.getAllTemplateMoveStatsAction = getAllTemplateMoveStatsAction;
    }


    @Override
    protected void register() {
        restHandlerFactory.registerHandler(GET, "template-move/all/stats", this);
    }

    @Override
    protected String name() {
        return "get-template-move-all-stats";
    }

    @Override
    protected void handleRequest(QueryContext queryContext, RestChannel channel) throws Exception {
        List<TemplateMoveTaskStats> templateMoveTaskStats = getAllTemplateMoveStatsAction.doAction(null);
        sendResponse(channel, Result.build(true, templateMoveTaskStats));
    }
}
