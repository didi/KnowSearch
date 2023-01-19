package org.elasticsearch.dcdr.rest;

import java.io.IOException;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.dcdr.action.ReplicationStatsAction;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

/**
 * author weizijun
 * date：2019-09-25
 */
public class RestReplicationStatsAction extends BaseRestHandler {
    public RestReplicationStatsAction(final Settings settings, final RestController controller) {
        controller.registerHandler(RestRequest.Method.GET, "/_dcdr/{index}/stats", this);
        controller.registerHandler(RestRequest.Method.GET, "/_dcdr/stats", this);
    }

    @Override
    public String getName() {
        return "dcdr_replication_stats";
    }

    @Override
    protected RestChannelConsumer prepareRequest(final RestRequest restRequest, final NodeClient client) throws IOException {
        final ReplicationStatsAction.Request request = new ReplicationStatsAction.Request();
        request.setIndices(Strings.splitStringByCommaToArray(restRequest.param("index")));
        return channel -> client.execute(ReplicationStatsAction.INSTANCE, request, new RestToXContentListener<>(channel));
    }

}
