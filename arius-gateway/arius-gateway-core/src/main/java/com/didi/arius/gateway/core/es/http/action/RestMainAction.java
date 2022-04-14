package com.didi.arius.gateway.core.es.http.action;

import com.didi.arius.gateway.common.consts.RestConsts;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.StatAction;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.Build;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.elasticsearch.rest.RestRequest.Method.HEAD;

/**
* @author weizijun
* @date：2016年8月28日
* 
*/
@Component("restMainAction")
public class RestMainAction extends StatAction {

	@Override
	public String name() {
		return "main";
	}

	@Value("${gateway.cluster.name}")
	private String gatewayClusterName;

	public RestMainAction() {
		// pass
	}

	@Override
	public void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
		
        RestStatus status = RestStatus.OK;

		if (request.method() == HEAD) {
			sendDirectResponse(queryContext, new BytesRestResponse(status));
		} else {
			try (XContentBuilder builder = channel.newBuilder()) {
				// Default to pretty printing, but allow ?pretty=false to disable
				if (!request.hasParam("pretty")) {
					builder.prettyPrint().lfAtEnd();
				}

				builder.startObject();
				builder.field("name", "elasticsearch gateway " + RestConsts.GATEWAY_VERSION);
				builder.field("cluster_name", null != client ? client.getClusterName() : gatewayClusterName);
				builder.startObject("version")
						.field("number", null != client ? client.getEsVersion() : "6.6.1")
						.field("build_hash", Build.CURRENT.hash())
						.field("build_timestamp", Build.CURRENT.timestamp())
						.field("build_snapshot", false)
						.field("lucene_version", "7.6.0")
						.endObject();
				builder.field("tagline", "You Know, for Search");
				builder.endObject();

				sendDirectResponse(queryContext, new BytesRestResponse(status, builder));
			}
		}
        
	}

}
