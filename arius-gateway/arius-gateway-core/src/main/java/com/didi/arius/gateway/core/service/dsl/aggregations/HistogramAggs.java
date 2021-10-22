package com.didi.arius.gateway.core.service.dsl.aggregations;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.AggsBukcetInfo;
import com.didi.arius.gateway.common.metadata.FieldInfo;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component("histogramAggs")
public class HistogramAggs extends BucketAggsType {
	private String name = "histogram";

	@Autowired
	private AggsTypes aggsTypes;

	@PostConstruct
	public void init() {
		aggsTypes.putAggsType(name, this);
	}

	@Override
	public AggsBukcetInfo computeAggsType(JsonObject item, Map<String, FieldInfo> mergedMappings) {
		JsonElement field = item.get("field");
		if (field == null) {
			return AggsBukcetInfo.createSingleBucket();
		}

		String strField = field.getAsString();

		JsonElement interval = item.get("interval");
		if (interval == null) {
			return AggsBukcetInfo.createSingleBucket();
		}

		int iInterval = interval.getAsInt();

		if (iInterval <= 0) {
			return AggsBukcetInfo.createSingleBucket();
		}
		
		if (mergedMappings.containsKey(strField)) {
			FieldInfo fieldInfo = mergedMappings.get(strField);
			if (fieldInfo.getCardinality() > 0) {
				AggsBukcetInfo aggsBukcetInfo = new AggsBukcetInfo();
				int bucket = (int) Math.ceil((double)fieldInfo.getCardinality() / iInterval);
				aggsBukcetInfo.setBucketNumber(bucket);
				aggsBukcetInfo.setLastBucketNumber(bucket);
				aggsBukcetInfo.setMemUsed(bucket * QueryConsts.AGGS_BUCKET_MEM_UNIT);
				
				return aggsBukcetInfo;
			}
		}
		
		return AggsBukcetInfo.createSingleBucket();
	}
}
