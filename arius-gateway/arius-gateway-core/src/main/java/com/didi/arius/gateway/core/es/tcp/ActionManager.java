package com.didi.arius.gateway.core.es.tcp;

import org.elasticsearch.action.admin.cluster.node.liveness.LivenessRequest;
import org.elasticsearch.action.admin.cluster.node.liveness.TransportLivenessAction;
import org.elasticsearch.action.get.GetAction;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.transport.TransportRequest;

import java.util.HashMap;

/**
 * @author weizijun @date：2016年9月18日
 * 
 */
public class ActionManager {
	private static HashMap<String, Class<? extends TransportRequest>> actionRequestMap = new HashMap<String, Class<? extends TransportRequest>>();

	private static HashMap<Class<? extends TransportRequest>, String> requestActionMap = new HashMap<Class<? extends TransportRequest>, String>();

	static {
		ActionManager.regist(GetAction.NAME, GetRequest.class);
		ActionManager.regist(SearchAction.NAME, SearchRequest.class);
		ActionManager.regist(TransportLivenessAction.NAME, LivenessRequest.class);
	}

	public static void regist(String action, Class<? extends TransportRequest> cls) {
		if (actionRequestMap.containsKey(action) || requestActionMap.containsKey(cls))
			throw new IllegalArgumentException("Action " + action + " already exists");
		actionRequestMap.put(action, cls);
		requestActionMap.put(cls, action);
	}

	public static String getAction(Class<? extends TransportRequest> cls) {
		return requestActionMap.get(cls);
	}

	public static Class<? extends TransportRequest> getRequestClass(String action) {
		return actionRequestMap.get(action);
	}

}
