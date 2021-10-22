package com.didi.arius.gateway.common.flowcontrol;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
* @author weizijun
* @date：2016年8月23日
* 
*/
public class AreaFlowCache {
	private ConcurrentMap<String, AreaFlow> areaFlowMap = new ConcurrentHashMap<>();
	
	public static AreaFlowCache getInstance() {
		return instance;
	}

	private static final AreaFlowCache instance = new AreaFlowCache();
	
	public AreaFlow getAreaFlow(String areaId) {
		return areaFlowMap.get(areaId);
	}
	
	public void setAreaFlow(String areaId, AreaFlow areaFlow) {
		areaFlowMap.put(areaId, areaFlow);
	}
}
