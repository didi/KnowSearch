package com.didi.arius.gateway.core.service.dsl.aggregations;

import com.didi.arius.gateway.common.metadata.AggsBukcetInfo;
import com.didi.arius.gateway.common.metadata.FieldInfo;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.elasticsearch.search.aggregations.metrics.cardinality.HyperLogLogPlusPlus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component("cardinalityAggs")
public class CardinalityAggs extends MetricsAggsType {

	private String name = "cardinality";
	
	@Autowired
	private AggsTypes aggsTypes;

	public CardinalityAggs() {
		// pass
	}

	@PostConstruct
	public void init() {
		aggsTypes.putAggsType(name, this);
	}
	
	@Override
	public AggsBukcetInfo computeAggsType(JsonObject item, Map<String, FieldInfo> mergedMappings) {
		int iMemPrecision = 4;
		JsonElement precision = item.get("precision_threshold");
		if (precision != null) {
			int iPrecision = precision.getAsInt();
			iMemPrecision = HyperLogLogPlusPlus.precisionFromThreshold(iPrecision);
		}

		long memUsed = HyperLogLogPlusPlus.memoryUsage(iMemPrecision);
		AggsBukcetInfo aggsBukcetInfo = AggsBukcetInfo.createSingleMetrics();
		aggsBukcetInfo.setMemUsed(memUsed);
	
		return aggsBukcetInfo;
	}
}
