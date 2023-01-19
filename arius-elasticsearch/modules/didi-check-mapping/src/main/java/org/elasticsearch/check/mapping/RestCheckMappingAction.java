package org.elasticsearch.check.mapping;

import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestRequest.Method.PUT;

import java.io.IOException;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

/**
 * author weizijun
 * date：2019-08-27
 */
public class RestCheckMappingAction extends BaseRestHandler {
    public RestCheckMappingAction(Settings settings, RestController controller) {
        controller.registerHandler(PUT, "/_mapping/check", this);
        controller.registerHandler(PUT, "/{index}/{type}/_mapping/check", this);

        controller.registerHandler(POST, "/_mapping/check", this);
        controller.registerHandler(POST, "/{index}/{type}/_mapping/check", this);
    }

    @Override
    public String getName() {
        return "check_mapping_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) throws IOException {
        CheckMappingAction.Request request = createRequest(restRequest);
        return channel -> client.execute(CheckMappingAction.INSTANCE, request, new RestToXContentListener<>(channel));
    }

    static CheckMappingAction.Request createRequest(RestRequest restRequest) throws IOException {
        CheckMappingAction.Request request = new CheckMappingAction.Request();
        request.setIndex(restRequest.param("index"));
        request.setType(restRequest.param("type"));
        request.source(restRequest.requiredContent(), restRequest.getXContentType());
        return request;
    }
}
