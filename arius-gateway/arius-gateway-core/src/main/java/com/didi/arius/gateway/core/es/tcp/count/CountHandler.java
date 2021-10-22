package com.didi.arius.gateway.core.es.tcp.count;

import com.didi.arius.gateway.core.es.tcp.ActionHandler;
import com.didi.arius.gateway.core.es.tcp.ActionListenerImpl;
import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.common.utils.Convert;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.count.CountAction;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.transport.TransportRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
* @author weizijun
* @date：2016年9月21日
* 
*/
@Component("countHandler")
public class CountHandler extends ActionHandler {
	@Override
	public String name() {
		return CountAction.NAME;
	}
	
    @PostConstruct
    public void init() {
        controller.registerHandler(CountAction.NAME, this);
    }
    
	@Override
	public void handleInterRequest(ActionContext actionContext, int retryTimes) {
		CountRequest countRequest = (CountRequest)actionContext.getRequest();
		
		List<String> indices = Arrays.asList(countRequest.indices());
		appService.checkIndices(actionContext, indices);
		
		String[] newIndices = Convert.convertIndices(countRequest.indices());
		countRequest.indices(newIndices);

        ActionListener<CountResponse> listener = new ActionListenerImpl<CountResponse>(actionContext);	
        esTcpClientService.getClient(actionContext.getCluster()).count(countRequest, new RetryListener<>(this, actionContext, listener, retryTimes));
	}

	@Override
	protected Class<? extends TransportRequest> getRequestClass() {
		return CountRequest.class;
	}

}
