package com.didi.arius.gateway.core.es.tcp.exists;

import com.didi.arius.gateway.core.es.tcp.ActionHandler;
import com.didi.arius.gateway.core.es.tcp.ActionListenerImpl;
import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.common.utils.Convert;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.exists.ExistsAction;
import org.elasticsearch.action.exists.ExistsRequest;
import org.elasticsearch.action.exists.ExistsResponse;
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
@Component("existsHandler")
public class ExistsHandler extends ActionHandler {

	@Override
	public String name() {
		return ExistsAction.NAME;
	}
	
    @PostConstruct
    public void init() {
        controller.registerHandler(ExistsAction.NAME, this);
    }	
	
	@Override
	public void handleInterRequest(ActionContext actionContext, int retryTimes) {
		ExistsRequest existsRequest = (ExistsRequest) actionContext.getRequest();
		
		List<String> indices = Arrays.asList(existsRequest.indices());
		appService.checkIndices(actionContext, indices);
		
		String[] newIndices = Convert.convertIndices(existsRequest.indices());
		existsRequest.indices(newIndices);
		
		ActionListener<ExistsResponse> listener = new ActionListenerImpl<>(actionContext);
		esTcpClientService.getClient(actionContext.getCluster()).exists(existsRequest, new RetryListener<>(this, actionContext, listener, retryTimes));
	}

	@Override
	protected Class<? extends TransportRequest> getRequestClass() {
		return ExistsRequest.class;
	}

}
