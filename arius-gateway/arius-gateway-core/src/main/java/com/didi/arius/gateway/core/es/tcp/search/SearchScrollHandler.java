package com.didi.arius.gateway.core.es.tcp.search;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.common.metadata.FetchFields;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollAction;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.transport.TransportRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
* @author weizijun
* @date：2016年9月21日
* 
*/
@Component("searchScrollHandler")
public class SearchScrollHandler extends BaseSearchHandler {

	@Override
	public String name() {
		return SearchScrollAction.NAME;
	}
	
    @PostConstruct
    public void init() {
        controller.registerHandler(SearchScrollAction.NAME, this);
    }	
	
	@Override
	public void handleInterRequest(ActionContext actionContext, int retryTimes) {
		SearchScrollRequest searchScrollRequest = (SearchScrollRequest) actionContext.getRequest();
		
		StringBuilder sb = new StringBuilder();
		sb.append(QueryConsts.DLFLAG_PREFIX + "query_tcp_search_scroll_request||appid=");
		sb.append(actionContext.getAppid());
		sb.append("||requestId=");
		sb.append(actionContext.getRequestId()).append("||");

		sb.append("scorllId=").append(searchScrollRequest.scrollId());

		statLogger.info(sb.toString());
		
		ActionListener<SearchResponse> listener = newListener(actionContext);
		
        FetchFields fetchFields = new FetchFields();
        fetchFields.setHasMessageField(false);
        actionContext.setFetchFields(fetchFields);
		
		searchScrollRequest.putHeader("requestId", actionContext.getRequestId());
		searchScrollRequest.putHeader("Authorization", actionContext.getRequest().getHeader("Authorization"));
		
		esTcpClientService.getClient(actionContext.getCluster()).searchScroll(searchScrollRequest, new RetryListener<>(this, actionContext, listener, retryTimes));
	}

	@Override
	protected Class<? extends TransportRequest> getRequestClass() {
		return SearchScrollRequest.class;
	}
}
