package com.didi.arius.gateway.core.es.http.sql;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.HttpRestHandler;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestStatus;
import org.nlpcn.es4sql.query.SqlElasticRequestBuilder;
import org.springframework.stereotype.Component;

/**
* @author weizijun
* @date：2016年8月28日
* 
*/
@Component("sqlExplainAction")
public class SQLExplainAction extends HttpRestHandler {
	
	@Override
	public String name() {
		return "sqlExplain";
	}
    
	@Override
	public void handleRequest(QueryContext queryContext) throws Exception {
		SqlElasticRequestBuilder requestBuilder = buildRequest(queryContext.getPostBody());

		String queryDSL = requestBuilder.explain();

		sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, XContentType.JSON.restContentType(), queryDSL));
	}
}
