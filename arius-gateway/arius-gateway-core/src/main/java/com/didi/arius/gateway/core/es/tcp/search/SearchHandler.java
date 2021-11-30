package com.didi.arius.gateway.core.es.tcp.search;

import com.didi.arius.gateway.common.exception.QueryDslLengthException;
import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.common.metadata.FetchFields;
import com.didi.arius.gateway.common.utils.Convert;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.transport.TransportRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
* @author weizijun
* @date：2016年9月19日
* 
*/
@Component("searchHandler")
public class SearchHandler extends BaseSearchHandler {

	@Override
	public String name() {
		return SearchAction.NAME;
	}
	
    @PostConstruct
    public void init() {
        controller.registerHandler(SearchAction.NAME, this);
    }	

	@Override
	public void handleInterRequest(ActionContext actionContext, int retryTimes) {
		SearchRequest searchRequest = (SearchRequest) actionContext.getRequest();
		
		if (searchRequest.source() != null && searchRequest.source().length() > queryConfig.getDslMaxLength()) {
			throw new QueryDslLengthException(String.format("query length(%d) > %d exception", searchRequest.source().length(), queryConfig.getDslMaxLength()));
		}

		String log = buildSearchRequestLog(actionContext, searchRequest);
		statLogger.info(log);

		List<String> indices = Arrays.asList(searchRequest.indices());
		appService.checkIndices(actionContext, indices);
		
        ActionListener<SearchResponse> listener = newListener(actionContext);
        
        FetchFields fetchFields = formFetchFields(searchRequest);
        actionContext.setFetchFields(fetchFields);
        
        searchRequest.putHeader("requestId", actionContext.getRequestId());
        searchRequest.putHeader("Authorization", actionContext.getRequest().getHeader("Authorization"));
        
		// 日期索引加上*号后缀，支持异常索引修复方案
		Convert.convertIndices(searchRequest);
		
		// pre process		
		dslAuditService.auditDSL(actionContext, searchRequest.source(), searchRequest.indices());
		
		dslAggsAnalyzerService.analyzeAggs(actionContext, searchRequest.source(), searchRequest.indices());
		dslAggsAnalyzerService.analyzeAggs(actionContext, searchRequest.extraSource(), searchRequest.indices());
        
		esTcpClientService.getClient(actionContext.getCluster()).search(searchRequest, new RetryListener<>(this, actionContext, listener, retryTimes));
	}

	@Override
	protected Class<? extends TransportRequest> getRequestClass() {
		return SearchRequest.class;
	}
	
	
}
