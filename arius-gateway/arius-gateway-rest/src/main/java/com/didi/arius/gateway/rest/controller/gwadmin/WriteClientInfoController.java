package com.didi.arius.gateway.rest.controller.gwadmin;

import com.didi.arius.gateway.common.metadata.ESCluster;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.rest.controller.AdminController;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * @author didi
 * @date 2021-09-23 8:55 下午
 */
@Controller
public class WriteClientInfoController extends AdminController {

    public static final String NAME = "writeClientInfo";

    @Override
    protected String name() {
        return NAME;
    }

    protected static final String COMM = ",";

    @Override
    public void register() {
        controller.registerHandler(RestRequest.Method.GET, "/_gwadmin/writeAction", this);
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
        RestStatus status = RestStatus.OK;

        XContentBuilder builder = channel.newBuilder();

        builder.startArray();
        for (Map.Entry<String, ESCluster> entry : esRestClientService.getESClusterMap().entrySet()) {
            builder.startObject();
            builder.field("cluster_name", entry.getValue().getCluster());
            builder.field("write_action", String.join(COMM, entry.getValue().getWriteAction()));
            builder.field("run_mode", entry.getValue().getRunMode());
            builder.endObject();
        }

        builder.endArray();
        sendDirectResponse(queryContext, new BytesRestResponse(status, builder));
    }
}

