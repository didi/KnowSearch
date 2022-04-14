package com.didi.arius.gateway.rest.tcp;

import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.metrics.MeanMetric;
import org.elasticsearch.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
* @author weizijun
* @date：2016年9月12日
* 
*/
@Component("adapter")
public class Adapter implements TransportServiceAdapter {
	protected static final Logger tracerLog = LoggerFactory.getLogger(Adapter.class);

    final MeanMetric rxMetric = new MeanMetric();
    final MeanMetric txMetric = new MeanMetric();	
	
	@Override
	public void received(long size) {
		rxMetric.inc(size);
	}

	@Override
	public void sent(long size) {
		txMetric.inc(size);
	}

	@Override
	public void onRequestSent(DiscoveryNode node, long requestId, String action, TransportRequest request,
			TransportRequestOptions options) {
        if (traceEnabled()) {
            traceRequestSent(node, requestId, action, options);
        }
	}
	
    protected boolean traceEnabled() {
        return tracerLog.isTraceEnabled();
    }

	@Override
	public void onResponseSent(long requestId, String action, TransportResponse response,
			TransportResponseOptions options) {
        if (traceEnabled()) {
            traceResponseSent(requestId, action);
        }
	}

	@Override
	public void onResponseSent(long requestId, String action, Throwable t) {
        if (traceEnabled()) {
            traceResponseSent(requestId, action, t);
        }
	}
	
    protected void traceResponseSent(long requestId, String action, Throwable t) {
        tracerLog.trace("[{}][{}] sent error response (error: [{}])", requestId, action, t.getMessage());
    }

	@Override
	public TransportResponseHandler onResponseReceived(long requestId) {
		return null;
	}

	@Override
	public void onRequestReceived(long requestId, String action) {
        if (traceEnabled()) {
            traceReceivedRequest(requestId, action);
        }
	}

	@Override
	public RequestHandlerRegistry getRequestHandler(String action) {
		return null;
	}

	@Override
	public void raiseNodeConnected(DiscoveryNode node) {
		//pass
	}

	@Override
	public void raiseNodeDisconnected(DiscoveryNode node) {
		//pass
	}
	
    protected void traceReceivedRequest(long requestId, String action) {
        tracerLog.trace("[{}][{}] received request", requestId, action);
    }

    protected void traceResponseSent(long requestId, String action) {
        tracerLog.trace("[{}][{}] sent response", requestId, action);
    }

    protected void traceUnresolvedResponse(long requestId) {
        tracerLog.trace("[{}] received response but can't resolve it to a request", requestId);
    }

    protected void traceRequestSent(DiscoveryNode node, long requestId, String action, TransportRequestOptions options) {
        tracerLog.trace("[{}][{}] sent to [{}] (timeout: [{}])", requestId, action, node, options.timeout());
    }	

}
