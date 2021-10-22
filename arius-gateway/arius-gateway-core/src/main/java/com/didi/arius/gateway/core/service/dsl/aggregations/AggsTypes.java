package com.didi.arius.gateway.core.service.dsl.aggregations;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("aggsTypes")
public class AggsTypes {
	private Map<String, AggsType> aggsTypeMap = new HashMap<String, AggsType>();
	
	public void putAggsType(String name, AggsType aggsType) {
		aggsTypeMap.put(name, aggsType);
	}
	
	public AggsType getAggsType(String name) {
		return aggsTypeMap.get(name);
	}

}
