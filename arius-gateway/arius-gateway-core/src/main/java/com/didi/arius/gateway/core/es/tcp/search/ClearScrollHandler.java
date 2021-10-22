package com.didi.arius.gateway.core.es.tcp.search;

import com.didi.arius.gateway.core.es.tcp.ActionHandler;
import com.didi.arius.gateway.core.es.tcp.ActionListenerImpl;
import com.didi.arius.gateway.common.metadata.ActionContext;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.ClearScrollAction;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.transport.TransportRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
* @author weizijun
* @date：2016年9月21日
* 
*/
@Component("clearScrollHandler")
public class ClearScrollHandler extends ActionHandler {
	@Override
	public String name() {
		return ClearScrollAction.NAME;
	}
	
    @PostConstruct
    public void init() {
        controller.registerHandler(ClearScrollAction.NAME, this);
    }	
    
	@Override
	public void handleInterRequest(ActionContext actionContext, int retryTimes) {
		ClearScrollRequest clearScrollRequest = (ClearScrollRequest) actionContext.getRequest();
		
		ActionListener<ClearScrollResponse> listener = new ActionListenerImpl<>(actionContext);
		esTcpClientService.getClient(actionContext.getCluster()).clearScroll(clearScrollRequest, new RetryListener<>(this, actionContext, listener, retryTimes));
	}

	@Override
	protected Class<? extends TransportRequest> getRequestClass() {
		return ClearScrollRequest.class;
	}

}
