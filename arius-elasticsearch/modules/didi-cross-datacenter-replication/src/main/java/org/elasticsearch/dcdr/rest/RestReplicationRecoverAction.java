package org.elasticsearch.dcdr.rest;

import java.io.IOException;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.dcdr.action.ReplicationRecoverAction;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

/**
 * author weizijun
 * date：2019-10-24
 */
public class RestReplicationRecoverAction extends BaseRestHandler {
    public RestReplicationRecoverAction(final Settings settings, final RestController controller) {
        controller.registerHandler(RestRequest.Method.POST, "/_dcdr/{index}/recover", this);
        controller.registerHandler(RestRequest.Method.POST, "/_dcdr/{index}/recover/{shard}", this);
    }

    @Override
    public String getName() {
        return "dcdr_replication_recover";
    }

    @Override
    protected RestChannelConsumer prepareRequest(final RestRequest restRequest, final NodeClient client) throws IOException {
        final ReplicationRecoverAction.Request request = new ReplicationRecoverAction.Request();
        request.indices(Strings.splitStringByCommaToArray(restRequest.param("index")));
        request.setShardNum(restRequest.paramAsInt("shard", -1));
        return channel -> client.execute(ReplicationRecoverAction.INSTANCE, request, new RestToXContentListener<>(channel));
    }
}
