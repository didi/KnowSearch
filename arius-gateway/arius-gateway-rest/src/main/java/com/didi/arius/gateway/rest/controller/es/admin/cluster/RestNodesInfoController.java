package com.didi.arius.gateway.rest.controller.es.admin.cluster;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.action.admin.cluster.node.info.RestNodesInfoAction;
import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static org.elasticsearch.rest.RestRequest.Method.GET;

/**
 * @author fitz
 * @date 2021/5/26 3:07 下午
 */
@Controller
public class RestNodesInfoController extends BaseHttpRestController {

    @Autowired
    private RestNodesInfoAction restNodesInfoAction;

    @Override
    protected void register() {
        controller.registerHandler(GET, "/_nodes", this);
        controller.registerHandler(GET, "/_nodesclean", this);
        // this endpoint is used for metrics, not for nodeIds, like /_nodes/fs
        controller.registerHandler(GET, "/_nodes/{nodeId}", this);
        controller.registerHandler(GET, "/_nodes/{nodeId}/{metrics}", this);
        // added this endpoint to be aligned with stats
        controller.registerHandler(GET, "/_nodes/{nodeId}/info/{metrics}", this);

    }

    @Override
    protected String name() {
        return restNodesInfoAction.name();
    }

    @Override
    protected void handleRequest(QueryContext queryContext) throws Exception {
        restNodesInfoAction.handleRequest(queryContext);

    }
}
