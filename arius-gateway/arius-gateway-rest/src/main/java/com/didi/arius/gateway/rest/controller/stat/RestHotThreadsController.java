package com.didi.arius.gateway.rest.controller.stat;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.rest.controller.StatController;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.action.admin.cluster.node.hotthreads.NodesHotThreadsRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.monitor.jvm.HotThreads;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Controller;

/**
 * @author fitz
 * @date 2021/5/25 5:36 下午
 */
@Controller
public class RestHotThreadsController extends StatController {
    public static final String NAME = "hotThreads";

    @Override
    protected String name() {
        return NAME;
    }

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.GET, "/_gwstat/hotthreads", this);
        controller.registerHandler(RestRequest.Method.GET, "/_gwstat/hot_threads", this);
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
        NodesHotThreadsRequest nodesHotThreadsRequest = new NodesHotThreadsRequest();
        nodesHotThreadsRequest.threads(request.paramAsInt("threads", nodesHotThreadsRequest.threads()));
        nodesHotThreadsRequest.ignoreIdleThreads(request.paramAsBoolean("ignore_idle_threads", nodesHotThreadsRequest.ignoreIdleThreads()));
        nodesHotThreadsRequest.type(request.param("type", nodesHotThreadsRequest.type()));
        nodesHotThreadsRequest.interval(TimeValue.parseTimeValue(request.param("interval"), nodesHotThreadsRequest.interval(), "interval"));
        nodesHotThreadsRequest.snapshots(request.paramAsInt("snapshots", nodesHotThreadsRequest.snapshots()));
        nodesHotThreadsRequest.timeout(request.param("timeout"));

        HotThreads hotThreads = new HotThreads()
                .busiestThreads(nodesHotThreadsRequest.threads())
                .type(nodesHotThreadsRequest.type())
                .interval(nodesHotThreadsRequest.interval())
                .threadElementsSnapshotCount(nodesHotThreadsRequest.snapshots())
                .ignoreIdleThreads(nodesHotThreadsRequest.ignoreIdleThreads());

        sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, hotThreads.detect()));

    }
}
