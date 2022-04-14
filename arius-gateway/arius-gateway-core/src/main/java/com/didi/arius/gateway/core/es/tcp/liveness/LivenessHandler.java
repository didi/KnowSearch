package com.didi.arius.gateway.core.es.tcp.liveness;

import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.core.es.tcp.ActionHandler;
import com.didi.arius.gateway.core.es.tcp.ActionListenerImpl;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.node.liveness.LivenessRequest;
import org.elasticsearch.action.admin.cluster.node.liveness.LivenessResponse;
import org.elasticsearch.action.admin.cluster.node.liveness.TransportLivenessAction;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.transport.TransportRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
* @author weizijun
* @date：2016年9月19日
* 
*/
@Component("livenessHandler")
public class LivenessHandler extends ActionHandler {

	@Value("${gateway.cluster.name}")
	private String gatewayClusterName;
	private ClusterName clusterName;

	public LivenessHandler() {
		// pass
	}

	@Override
	public String name() {
		return TransportLivenessAction.NAME;
	}
	
    @PostConstruct
    public void init() {
        controller.registerHandler(TransportLivenessAction.NAME, this);
		this.clusterName = new ClusterName(gatewayClusterName);
    }	
	
	@Override
	public void handleInterRequest(ActionContext actionContext, int retryTimes) {
		ActionListener<LivenessResponse> listener = new ActionListenerImpl<>(actionContext);
		listener.onResponse(new LivenessResponse(this.clusterName, null));
	}

	@Override
	protected Class<? extends TransportRequest> getRequestClass() {
		return LivenessRequest.class;
	}

}
