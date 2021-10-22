package com.didi.arius.gateway.core.es.http;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.google.common.collect.Lists;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

import java.util.List;

public abstract class ESAction extends HttpRestHandler {
	@Override
	public void handleRequest(QueryContext queryContext) throws Exception {
		RestRequest request = queryContext.getRequest();
		RestChannel channel = queryContext.getChannel();

		checkFlowLimit(queryContext);
		
		String[]  indicesArr = Strings.splitStringByCommaToArray(request.param("index"));
		List<String> indices = Lists.newArrayList(indicesArr);
		queryContext.setIndices(indices);
		queryContext.setTypeNames(request.param("type"));
		if (isNeededCheckIndices()) {
			checkIndices(queryContext);
		}

		if (queryContext.isDetailLog()) {
			statLogger.info(QueryConsts.DLFLAG_PREFIX + "query_request_before||requestId={}||before_cost={}", queryContext.getRequestId(), (System.currentTimeMillis()-queryContext.getRequestTime()));
		}

		handleInterRequest(queryContext, request, channel);
	}

	abstract protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel) throws Exception;
}
