package org.elasticsearch.dcdr.rest;

import java.io.IOException;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.dcdr.action.CreateReplicationAction;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

/**
 * author weizijun
 * date：2019-08-12
 */
public class RestCreateReplicationAction extends BaseRestHandler {
    public RestCreateReplicationAction(Settings settings, RestController controller) {
        controller.registerHandler(RestRequest.Method.PUT, "/_dcdr/{index}/replication/create", this);
    }

    @Override
    public String getName() {
        return "dcdr_create_replication_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) throws IOException {
        CreateReplicationAction.Request request = createRequest(restRequest);
        return channel -> client.execute(CreateReplicationAction.INSTANCE, request, new RestToXContentListener<>(channel));
    }

    static CreateReplicationAction.Request createRequest(RestRequest restRequest) throws IOException {
        try (XContentParser parser = restRequest.contentOrSourceParamParser()) {
            return CreateReplicationAction.Request.fromXContent(parser, restRequest.param("index"));
        }
    }
}
