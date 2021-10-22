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

@Component("dateRangeAggs")
public class DateRangeAggs extends BucketAggsType {

	private String name = "date_range";
	
	@Autowired
	private AggsTypes aggsTypes;
	
	@PostConstruct
	public void init() {
		aggsTypes.putAggsType(name, this);
	}
	
	
	@Override
	public AggsBukcetInfo computeAggsType(JsonObject item, Map<String, FieldInfo> mergedMappings) {
		AggsBukcetInfo aggsBukcetInfo = new AggsBukcetInfo();
		
		JsonElement ranges = item.get("ranges");
		if (ranges != null) {
			JsonArray rangesArr = ranges.getAsJsonArray();
			aggsBukcetInfo.setBucketNumber(rangesArr.size());
			aggsBukcetInfo.setLastBucketNumber(rangesArr.size());
			aggsBukcetInfo.setMemUsed(rangesArr.size() * QueryConsts.AGGS_BUCKET_MEM_UNIT);
		}
		
		return aggsBukcetInfo;
	}

}
