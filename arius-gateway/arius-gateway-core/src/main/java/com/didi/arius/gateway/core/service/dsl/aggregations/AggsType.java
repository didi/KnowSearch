package com.didi.arius.gateway.core.service.dsl.aggregations;

import com.didi.arius.gateway.common.metadata.AggsBukcetInfo;
import com.didi.arius.gateway.common.enums.AggsTypeEnum;
import com.didi.arius.gateway.common.metadata.FieldInfo;
import com.google.gson.JsonObject;

import java.util.Map;

public interface AggsType {
	/**
	 *
	 * @param item
	 * @param mergedMappings
	 * @return
	 */
	AggsBukcetInfo computeAggsType(JsonObject item, Map<String, FieldInfo> mergedMappings);

	/**
	 *
	 * @return
	 */
	AggsTypeEnum getType();
}
