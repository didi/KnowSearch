package org.elasticsearch.dcdr.rest;

import java.io.IOException;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.dcdr.action.GetAutoReplicationAction;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

/**
 * author weizijun
 * date：2019-08-27
 */
public class RestGetAutoReplicationAction extends BaseRestHandler {
    public RestGetAutoReplicationAction(Settings settings, RestController controller) {
        controller.registerHandler(RestRequest.Method.GET, "/_dcdr/auto_replication/{name}", this);
        controller.registerHandler(RestRequest.Method.GET, "/_dcdr/auto_replication", this);
    }

    @Override
    public String getName() {
        return "dcdr_get_auto_replication_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) throws IOException {
        GetAutoReplicationAction.Request request = createRequest(restRequest);
        return channel -> client.execute(GetAutoReplicationAction.INSTANCE, request, new RestToXContentListener<>(channel));
    }

    static GetAutoReplicationAction.Request createRequest(RestRequest restRequest) throws IOException {
        GetAutoReplicationAction.Request request = new GetAutoReplicationAction.Request();
        request.setName(restRequest.param("name"));
        return request;
    }
}
