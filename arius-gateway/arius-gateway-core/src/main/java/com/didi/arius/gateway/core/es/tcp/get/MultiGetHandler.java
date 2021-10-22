package com.didi.arius.gateway.core.es.tcp.get;

import com.didi.arius.gateway.core.es.tcp.ActionHandler;
import com.didi.arius.gateway.core.es.tcp.ActionListenerImpl;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.common.metadata.FetchFields;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.MultiGetAction;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetRequest.Item;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.transport.TransportRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
* @author weizijun
* @date：2016年9月21日
* 
*/
@Component("multiGetHandler")
public class MultiGetHandler extends ActionHandler {

	@Override
	public String name() {
		return MultiGetAction.NAME;
	}
	
    @PostConstruct
    public void init() {
        controller.registerHandler(MultiGetAction.NAME, this);
    }	
	
	@Override
	public void handleInterRequest(ActionContext actionContext, int retryTimes) {
		MultiGetRequest multiGetRequest = (MultiGetRequest)actionContext.getRequest();
		
		List<String> indices = new ArrayList<String>();
		for (Item item : multiGetRequest.getItems()) {
			indices.add(item.index());
		}
		appService.checkIndices(actionContext, indices);
		
		final List<FetchFields> fetchFieldsList = new ArrayList<>(multiGetRequest.getItems().size());
        for (Item item : multiGetRequest.getItems()) {
        	FetchFields fetchFields = new FetchFields();
        	fetchFields.setFetchSourceContext(item.fetchSourceContext());
        	fetchFields.setFields(item.fields());
            if (fetchFields.getFields() != null) {
                for (String field : fetchFields.getFields()) {
        			if (field.equals(QueryConsts.MESSAGE_FIELD)) {
        				fetchFields.setHasMessageField(true);
        				break;
        			}
        		}
            }

            fetchFieldsList.add(fetchFields);
        }

        boolean needGetVersion = false;
        MultiGetRequest newMultiGetRequest = new MultiGetRequest();
        final List<Integer> versionPos = new ArrayList<>();
        final List<Integer> versionValue = new ArrayList<>();
        int pos = 0;
        for (Item item : multiGetRequest.getItems()) {
        	String index = item.index();
        	int indexVersion = indexTemplateService.getIndexVersion(index, actionContext.getCluster());
        	if (indexVersion > 0) {
        		versionPos.add(pos);
        		versionValue.add(indexVersion);
        		needGetVersion = true;
        		for (int i = indexVersion; i >= 0; i--) {
                	String inIndex = item.index();
                	if (i > 0) {
                		inIndex = index+"_v"+i;
                	}

        			Item newItem = new Item(inIndex, item.type(), item.id());

        			newItem.version(item.version());
        			newItem.fetchSourceContext(item.fetchSourceContext());
        			newItem.fields(item.fields());
        			newItem.routing(item.routing());
        			newItem.versionType(item.versionType());
        			
        			newMultiGetRequest.add(newItem);
        			
        			++pos;
        		}
        	} else {
        		newMultiGetRequest.add(item);
        		
        		++pos;
        	}
        }
        
        if (needGetVersion) {
        	ActionListener<MultiGetResponse> listener = new ActionListenerImpl<MultiGetResponse>(actionContext) {
        		@Override
        		public void onResponse(MultiGetResponse response) {
                	List<MultiGetItemResponse> itemList = new ArrayList<>();
                	Iterator<Integer> posIter = versionPos.iterator();
                	Iterator<Integer> versionIter = versionValue.iterator();
                	int itemPos = 0;
                	int currentVersion = 0;
                	if (posIter.hasNext()) {
                		itemPos = posIter.next();
                		currentVersion = versionIter.next();
                	}
                	
                	for (int i = 0; i < response.getResponses().length; ++i) {
                		MultiGetItemResponse itemResponse = response.getResponses()[i];
                		if (i == itemPos) {
                			MultiGetItemResponse newGetItemResponse = null;
                			for (int p = 0; p <= currentVersion; p++) {
                				newGetItemResponse = response.getResponses()[i + p];
                				if (newGetItemResponse.getResponse() != null && newGetItemResponse.getResponse().isExists()) {
                					break;
                				}
                			}
                			
                			itemList.add(newGetItemResponse);
                			
                			for (int p = 1; p <= currentVersion; p++) {
                				i++;
                			}
                			
                			if (posIter.hasNext()) {
                        		itemPos = posIter.next();
                        		currentVersion = versionIter.next();
                			}
                		} else {
                			itemList.add(itemResponse);
                		}
                	}
                	
                	MultiGetResponse newResponse = new MultiGetResponse(itemList.toArray(new MultiGetItemResponse[itemList.size()]));
					super.onResponse(newResponse);
        		}
        	};
        	
        	esTcpClientService.getClient(actionContext.getCluster()).multiGet(newMultiGetRequest, new RetryListener<>(this, actionContext, listener, retryTimes));
        } else {
        	ActionListener<MultiGetResponse> listener = new ActionListenerImpl<MultiGetResponse>(actionContext);
            esTcpClientService.getClient(actionContext.getCluster()).multiGet(multiGetRequest, new RetryListener<>(this, actionContext, listener, retryTimes));
        }
	}

	@Override
	protected Class<? extends TransportRequest> getRequestClass() {
		return MultiGetRequest.class;
	}
}
