package org.elasticsearch.dcdr.rest;

import java.io.IOException;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.dcdr.action.SwitchReplicationAction;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

/**
 * author zhz
 * date：2019-08-27
 */
public class RestResumeReplicationAction extends BaseRestHandler {
    public RestResumeReplicationAction(Settings settings, RestController controller) {
        controller.registerHandler(RestRequest.Method.POST, "/_dcdr/{index}/replication/resume", this);
    }

    @Override
    public String getName() {
        return "dcdr_resume_replication_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) throws IOException {
        SwitchReplicationAction.Request request = createRequest(restRequest);
        return channel -> client.execute(
            SwitchReplicationAction.INSTANCE,
            request,
            new RestToXContentListener<>(channel)
        );
    }

    static SwitchReplicationAction.Request createRequest(RestRequest restRequest) throws IOException {
        try (XContentParser parser = restRequest.contentOrSourceParamParser()) {
            return SwitchReplicationAction.Request.fromXContent(parser, restRequest.param("index"), true);
        }
    }
}
