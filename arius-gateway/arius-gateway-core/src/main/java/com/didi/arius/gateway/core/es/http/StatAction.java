package com.didi.arius.gateway.core.es.http;

import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.google.common.collect.Lists;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

import java.util.List;

import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;

/**
* @author weizijun
* @date：2016年8月25日
* 
*/
public abstract class StatAction extends HttpRestHandler {

	@Override
	public void handleRequest(QueryContext queryContext) throws Exception {
		// checkToken(queryContext);

		ESClient client = esClusterService.getClient(queryContext);

		if (queryContext.getRequest().param("index") != null) {
			String index = queryContext.getRequest().param("index");
			String[] indicesArr = Strings.splitStringByCommaToArray(index);
			List<String> indices = Lists.newArrayList(indicesArr);
			queryContext.setIndices(indices);

			checkIndices(queryContext);

			if (isIndexType(queryContext)) {
				IndexTemplate indexTemplate = getTemplateByIndexTire(indices, queryContext);

				client = esClusterService.getClient(queryContext, indexTemplate);
			}
		}
		
		handleInterRequest(queryContext, queryContext.getRequest(), queryContext.getChannel(), client);
	}

	abstract protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception;
}
