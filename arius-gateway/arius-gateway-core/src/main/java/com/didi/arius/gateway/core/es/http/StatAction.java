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

		ESClient client = esClusterService.getClient(queryContext, actionName);

		if (isOriginCluster(queryContext)) {
			handleOriginClusterRequest(queryContext);
		} else {
			if (queryContext.getRequest().param("index") != null) {
				String index = queryContext.getRequest().param("index");
				String[] indicesArr = Strings.splitStringByCommaToArray(index);
				List<String> indicesList = Lists.newArrayList(indicesArr);
				queryContext.setIndices(indicesList);

				checkIndices(queryContext);

				if (isIndexType(queryContext)) {
					IndexTemplate indexTemplate = getTemplateByIndexTire(indicesList, queryContext);

					client = esClusterService.getClient(queryContext, indexTemplate, actionName);
				}
			}
			handleInterRequest(queryContext, queryContext.getRequest(), queryContext.getChannel(), client);
		}
	}

	protected abstract void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception;
}
