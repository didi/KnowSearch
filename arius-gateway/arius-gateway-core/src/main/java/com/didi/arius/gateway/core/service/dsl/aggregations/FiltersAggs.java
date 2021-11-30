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

@Component("filtersAggs")
public class FiltersAggs extends BucketAggsType {

	private String name = "filters";
	
	@Autowired
	private AggsTypes aggsTypes;

	public FiltersAggs() {
		// pass
	}

	@PostConstruct
	public void init() {
		aggsTypes.putAggsType(name, this);
	}
	
	@Override
	public AggsBukcetInfo computeAggsType(JsonObject item, Map<String, FieldInfo> mergedMappings) {
		JsonElement filters = item.get("filters");
		if (filters != null) {
			AggsBukcetInfo aggsBukcetInfo = new AggsBukcetInfo();
			
			if (filters.isJsonObject()) {
				JsonObject jsonFilters = filters.getAsJsonObject();
				int size = jsonFilters.entrySet().size();
				aggsBukcetInfo.setBucketNumber(size);
				aggsBukcetInfo.setLastBucketNumber(size);
				aggsBukcetInfo.setMemUsed((long)size * QueryConsts.AGGS_BUCKET_MEM_UNIT);
				
				return aggsBukcetInfo;
			} else if (filters.isJsonArray()) {
				JsonArray jsonRanges = filters.getAsJsonArray();
				int size = jsonRanges.size();
				aggsBukcetInfo.setBucketNumber(size);
				aggsBukcetInfo.setLastBucketNumber(size);
				aggsBukcetInfo.setMemUsed((long)size * QueryConsts.AGGS_BUCKET_MEM_UNIT);
				
				return aggsBukcetInfo;
			} else {
				return AggsBukcetInfo.createSingleBucket();
			}
		} else {
			return AggsBukcetInfo.createSingleBucket();
		}
		
	}

}
