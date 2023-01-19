package org.elasticsearch.region;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.RestBuilderListener;


public class RestGetRegionAction extends BaseRestHandler {

    public RestGetRegionAction(RestController restController) {
        restController.registerHandler(RestRequest.Method.GET, "/_region/{name}", this);
    }

    @Override
    public String getName() {
        return "get region";
    }


    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) {
        GetRegionRequest regionRequest = new GetRegionRequest();
        regionRequest.setRegion(request.param("name", "all"));

        return channel -> client.executeLocally(GetRegionAction.INSTANCE, regionRequest, new RestBuilderListener<GetRegionResponse>(channel) {

            @Override
            public RestResponse buildResponse(GetRegionResponse response, XContentBuilder builder) throws Exception {
                return new BytesRestResponse(RestStatus.OK, response.toXContent(builder, ToXContent.EMPTY_PARAMS));
            }
        });
    }
}
