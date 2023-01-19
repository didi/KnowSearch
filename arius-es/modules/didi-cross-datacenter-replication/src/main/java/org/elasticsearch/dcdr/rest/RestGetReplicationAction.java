package org.elasticsearch.dcdr.rest;

import java.io.IOException;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.dcdr.action.GetReplicationAction;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

/**
 * author weizijun
 * date：2019-08-27
 */
public class RestGetReplicationAction extends BaseRestHandler {
    public RestGetReplicationAction(Settings settings, RestController controller) {
        controller.registerHandler(RestRequest.Method.GET, "/_dcdr/{index}/replication", this);
        controller.registerHandler(RestRequest.Method.GET, "/_dcdr/replication", this);
    }

    @Override
    public String getName() {
        return "dcdr_get_auto_replication_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) throws IOException {
        GetReplicationAction.Request request = createRequest(restRequest);
        return channel -> client.execute(GetReplicationAction.INSTANCE, request, new RestToXContentListener<>(channel));
    }

    static GetReplicationAction.Request createRequest(RestRequest restRequest) throws IOException {
        GetReplicationAction.Request request = new GetReplicationAction.Request();
        request.setPrimaryIndex(restRequest.param("index"));
        return request;
    }
}
