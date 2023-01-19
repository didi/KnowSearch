package org.elasticsearch.dcdr.rest;

import java.io.IOException;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.dcdr.action.DeleteReplicationAction;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

/**
 * author weizijun
 * date：2019-08-27
 */
public class RestDeleteReplicationAction extends BaseRestHandler {
    public RestDeleteReplicationAction(Settings settings, RestController controller) {
        controller.registerHandler(RestRequest.Method.DELETE, "/_dcdr/{index}/replication/delete", this);
    }

    @Override
    public String getName() {
        return "dcdr_delete_replication_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) throws IOException {
        DeleteReplicationAction.Request request = createRequest(restRequest);
        return channel -> client.execute(DeleteReplicationAction.INSTANCE, request, new RestToXContentListener<>(channel));
    }

    static DeleteReplicationAction.Request createRequest(RestRequest restRequest) throws IOException {
        try (XContentParser parser = restRequest.contentOrSourceParamParser()) {
            return DeleteReplicationAction.Request.fromXContent(parser, restRequest.param("index"));
        }
    }
}
