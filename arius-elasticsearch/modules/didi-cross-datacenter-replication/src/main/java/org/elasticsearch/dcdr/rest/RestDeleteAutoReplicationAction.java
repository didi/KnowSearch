package org.elasticsearch.dcdr.rest;

import java.io.IOException;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.dcdr.action.DeleteAutoReplicationAction;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

/**
 * author weizijun
 * date：2019-08-27
 */
public class RestDeleteAutoReplicationAction extends BaseRestHandler {
    public RestDeleteAutoReplicationAction(Settings settings, RestController controller) {
        controller.registerHandler(RestRequest.Method.DELETE, "/_dcdr/auto_replication/{name}", this);
    }

    @Override
    public String getName() {
        return "dcdr_delete_auto_replication_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) throws IOException {
        DeleteAutoReplicationAction.Request request = createRequest(restRequest);
        return channel -> client.execute(DeleteAutoReplicationAction.INSTANCE, request, new RestToXContentListener<>(channel));
    }

    static DeleteAutoReplicationAction.Request createRequest(RestRequest restRequest) throws IOException {
        DeleteAutoReplicationAction.Request request = new DeleteAutoReplicationAction.Request();
        request.setName(restRequest.param("name"));
        return request;
    }
}
