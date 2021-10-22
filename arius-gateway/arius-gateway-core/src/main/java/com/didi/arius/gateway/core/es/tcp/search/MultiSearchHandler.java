package com.didi.arius.gateway.core.es.tcp.search;

import com.didi.arius.gateway.core.es.tcp.ActionListenerImpl;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.exception.QueryDslLengthException;
import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.common.metadata.FetchFields;
import com.didi.arius.gateway.common.utils.Convert;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.search.MultiSearchResponse.Item;
import org.elasticsearch.transport.TransportRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
* @author weizijun
* @date：2016年9月21日
* 
*/
@Component("multiSearchHandler")
public class MultiSearchHandler extends BaseSearchHandler {

	@Override
	public String name() {
		return MultiSearchAction.NAME;
	}
	
    @PostConstruct
    public void init() {
        controller.registerHandler(MultiSearchAction.NAME, this);
    }		
	
	@Override
	public void handleInterRequest(ActionContext actionContext, int retryTimes) {
		MultiSearchRequest multiSearchRequest = (MultiSearchRequest) actionContext.getRequest();
		final List<SearchRequest> requests = multiSearchRequest.requests();
		final List<FetchFields> fetchFieldsList = new ArrayList<>(multiSearchRequest.requests().size());
		List<String> indices = new ArrayList<String>();
		for (SearchRequest searchRequest : requests) {
			statLogger.info(buildSearchRequestLog(actionContext, searchRequest));
			
			if (searchRequest.source() != null && searchRequest.source().length() > queryConfig.getDslMaxLength()) {
				throw new QueryDslLengthException(String.format("query length(%d) > %d exception", searchRequest.source().length(), queryConfig.getDslMaxLength()));
			}
			
			List<String> inIndices = Arrays.asList(searchRequest.indices());
			indices.addAll(inIndices);

			// 日期索引加上*号后缀，支持异常索引修复方案
			Convert.convertIndices(searchRequest);
			
			// pre process			
			dslAuditService.auditDSL(actionContext, searchRequest.source(), searchRequest.indices());
			
			dslAggsAnalyzerService.analyzeAggs(actionContext, searchRequest.source(), searchRequest.indices());
			dslAggsAnalyzerService.analyzeAggs(actionContext, searchRequest.extraSource(), searchRequest.indices());
			
			FetchFields fetchFields = formFetchFields(searchRequest);
			fetchFieldsList.add(fetchFields);
		}
		appService.checkIndices(actionContext, indices);
		
		ActionListener<MultiSearchResponse> listener = new ActionListenerImpl<MultiSearchResponse>(actionContext){
        	@Override
        	public void onResponse(MultiSearchResponse multiSearchResponse) {
        		statLogger.info(QueryConsts.DLFLAG_PREFIX + "query_tcp_search_query||appid={}||requestId={}||cost={}", actionContext.getAppid(), actionContext.getRequestId(), (System.currentTimeMillis()-actionContext.getRequestTime()));
        		
				Item items[] = multiSearchResponse.getResponses();
				for (int i = 0; i < items.length; i++) {
					SearchResponse searchResponse = items[i].getResponse();
					
					if (searchResponse == null) {
						statLogger.info(QueryConsts.DLFLAG_PREFIX + "query_tcp_search_response||appid={}||requestId={}", actionContext.getAppid(), actionContext.getRequestId());
						continue;
					}
					
					statLogger.info(buildSearchResponseLog(actionContext, searchResponse));
					
					if (searchResponse.getTookInMillis() > queryConfig.getSearchSlowlogThresholdMills()) {
						SearchRequest searchRequest = requests.get(i);
						statLogger.warn(buildSearchSlowlog(actionContext, searchRequest, searchResponse));
					}
					
	    			if (searchResponse.getFailedShards() > 0) {
						StringBuilder stringBuilder = new StringBuilder("search response has some failed,appid="+actionContext.getAppid()+",requestId="+actionContext.getRequestId()+",number="+searchResponse.getFailedShards()+" reasons:\n");
						int count = 0;
						for (ShardSearchFailure shardSearchFailure : searchResponse.getShardFailures()) {
							stringBuilder.append(Convert.getPrefix(shardSearchFailure.reason()));
							stringBuilder.append("\n");
							count++;
							if (count > 2) {
								stringBuilder.append("...\n");
								break;
							}
						}
						logger.warn(stringBuilder.toString());
	    			}
	    			
				}
				super.onResponse(multiSearchResponse);
        	}
        };
        
        multiSearchRequest.putHeader("requestId", actionContext.getRequestId());
        multiSearchRequest.putHeader("Authorization", actionContext.getRequest().getHeader("Authorization"));
        
		esTcpClientService.getClient(actionContext.getCluster()).multiSearch(multiSearchRequest, new RetryListener<>(this, actionContext, listener, retryTimes));
	}

	@Override
	protected Class<? extends TransportRequest> getRequestClass() {
		return MultiSearchRequest.class;
	}
}
