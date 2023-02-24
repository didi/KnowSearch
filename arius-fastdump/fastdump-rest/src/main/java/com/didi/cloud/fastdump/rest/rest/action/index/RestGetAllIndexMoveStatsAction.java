package com.didi.cloud.fastdump.rest.rest.action.index;

import static org.elasticsearch.rest.RestRequest.Method.GET;

import java.util.List;

import org.elasticsearch.rest.RestChannel;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.bean.common.Result;
import com.didi.cloud.fastdump.common.bean.stats.IndexMoveTaskStats;
import com.didi.cloud.fastdump.common.content.metadata.QueryContext;
import com.didi.cloud.fastdump.core.action.metadata.GetAllIndexMoveStatsAction;
import com.didi.cloud.fastdump.rest.rest.BaseCommonHttpAction;

/**
 * Created by linyunan on 2022/9/5
 */
@Component
public class RestGetAllIndexMoveStatsAction extends BaseCommonHttpAction {
    private final GetAllIndexMoveStatsAction getAllIndexMoveStatsAction;

    public RestGetAllIndexMoveStatsAction(GetAllIndexMoveStatsAction getAllIndexMoveStatsAction) {
        this.getAllIndexMoveStatsAction = getAllIndexMoveStatsAction;
    }

    @Override
    protected void register() {
        restHandlerFactory.registerHandler(GET, "index-move/all/stats", this);
    }

    @Override
    protected String name() {
        return "get-index-move-all-stats";
    }

    @Override
    protected void handleRequest(QueryContext queryContext, RestChannel channel) throws Exception {
        List<IndexMoveTaskStats> indexMoveTaskStatsList = getAllIndexMoveStatsAction.doAction(null);
        sendResponse(channel, Result.build(true, indexMoveTaskStatsList));
    }
}
