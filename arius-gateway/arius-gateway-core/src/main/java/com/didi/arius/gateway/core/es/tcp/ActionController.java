package com.didi.arius.gateway.core.es.tcp;

import org.elasticsearch.transport.ActionNotFoundTransportException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


/**
* @author weizijun
* @date：2016年9月19日
* 
*/
@Component("actionController")
public class ActionController {
	private Map<String, ActionHandler> actionMap = new HashMap<>();
	
	public void registerHandler(String action, ActionHandler handler) {
		actionMap.put(action, handler);
	}
	
	public ActionHandler getHandler(String action) {
		ActionHandler handler = actionMap.get(action);
		if (handler == null) {
			throw new ActionNotFoundTransportException(action);
		}
		
		return handler;
	}
}
