package com.didi.arius.gateway.core.es.tcp.get;

import com.didi.arius.gateway.core.es.tcp.ActionHandler;
import com.didi.arius.gateway.core.es.tcp.ActionListenerImpl;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.common.metadata.FetchFields;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.*;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.transport.TransportRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
* @author weizijun
* @date：2016年9月18日
* 
*/
@Component("getHandler")
public class GetHandler extends ActionHandler {
	
	@Override
	public String name() {
		return GetAction.NAME;
	}
	
    @PostConstruct
    public void init() {
        controller.registerHandler(GetAction.NAME, this);
    }
	
	@Override
	public void handleInterRequest(ActionContext actionContext, int retryTimes) {
		final GetRequest getRequest = (GetRequest)actionContext.getRequest();
		
		List<String> indices = Arrays.asList(getRequest.index());
		appService.checkIndices(actionContext, indices);
		
		final FetchFields fetchFields = new FetchFields();
		fetchFields.setFields(getRequest.fields());
		fetchFields.setFetchSourceContext(getRequest.fetchSourceContext());
		if (fetchFields.getFields() != null) {
			for (String field : fetchFields.getFields()) {
				if (field.equals(QueryConsts.MESSAGE_FIELD)) {
					fetchFields.setHasMessageField(true);
					break;
				}
			}	
		}
		
    	String index = getRequest.index();
    	final int indexVersion = indexTemplateService.getIndexVersion(index, actionContext.getCluster());
    	if (indexVersion > 0) {
    		MultiGetRequest multiGetRequest = getVersionRequest(indexVersion, getRequest);

			ActionListener<MultiGetResponse> listener = getListener(actionContext, getRequest);

			esTcpClientService.getClient(actionContext.getCluster()).multiGet(multiGetRequest, listener);
    	} else {
    		ActionListener<GetResponse> listener = new ActionListenerImpl<>(actionContext);
            esTcpClientService.getClient(actionContext.getCluster()).get(getRequest, new RetryListener<>(this, actionContext, listener, retryTimes));
    	}
	}

	private ActionListener<MultiGetResponse> getListener(ActionContext actionContext, GetRequest getRequest) {
		return new ActionListenerImpl<MultiGetResponse>(actionContext) {
			@Override
			public void onResponse(MultiGetResponse response) {
				GetResponse getResponse = null;
				for (MultiGetItemResponse itemResponse : response.getResponses()) {
					if (itemResponse == null) {
						continue;
					}

					if (itemResponse.getResponse() != null && itemResponse.getResponse().isExists()) {
						getResponse = itemResponse.getResponse();
						break;
					}

					getResponse = itemResponse.getResponse();
				}

				if (getResponse == null) {
					GetResult getResult = new GetResult(getRequest.index(), getRequest.type(), getRequest.id(), -1, false, null, null);
					getResponse = new GetResponse(getResult);
				}
				ActionListener<GetResponse> listener = new ActionListenerImpl<>(actionContext);
				listener.onResponse(getResponse);
			}
		};
	}

	@Override
	protected Class<? extends TransportRequest> getRequestClass() {
		return GetRequest.class;
	}

}


