package org.elasticsearch.dcdr.rest;

import java.io.IOException;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.dcdr.action.FetchShardInfoAction;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

/**
 * author weizijun
 * date：2019-12-17
 */
public class RestFetchShardInfoAction extends BaseRestHandler {
    public RestFetchShardInfoAction(Settings settings, RestController controller) {
        controller.registerHandler(RestRequest.Method.GET, "/_dcdr/{index}/{shard}/fetch_shard_info", this);
    }

    @Override
    public String getName() {
        return "dcdr_fetch_shard_info_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) throws IOException {
        FetchShardInfoAction.Request request = new FetchShardInfoAction.Request(restRequest.param("index"), restRequest.paramAsInt("shard", -1));
        return channel -> client.execute(FetchShardInfoAction.INSTANCE, request, new RestToXContentListener<>(channel));
    }

}
