package com.didi.arius.gateway.core.service.dsl.aggregations;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.AggsBukcetInfo;
import com.didi.arius.gateway.common.metadata.FieldInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component("ipRangeAggs")
public class IpRangeAggs extends BucketAggsType {
	private String name = "ip_range";
	
	@Autowired
	private AggsTypes aggsTypes;

	public IpRangeAggs() {
		// pass
	}

	@PostConstruct
	public void init() {
		aggsTypes.putAggsType(name, this);
	}
	
	@Override
	public AggsBukcetInfo computeAggsType(JsonObject item, Map<String, FieldInfo> mergedMappings) {
		JsonElement ranges = item.get("ranges");
		if (ranges != null) {
			AggsBukcetInfo aggsBukcetInfo = new AggsBukcetInfo();
			
			JsonArray ipJsonRanges = ranges.getAsJsonArray();
			int size = ipJsonRanges.size();
			aggsBukcetInfo.setLastBucketNumber(size);
			aggsBukcetInfo.setBucketNumber(size);
			aggsBukcetInfo.setMemUsed((long)size * QueryConsts.AGGS_BUCKET_MEM_UNIT);
			
			return aggsBukcetInfo;
		} else {
			return AggsBukcetInfo.createSingleBucket();
		}
	}
}
