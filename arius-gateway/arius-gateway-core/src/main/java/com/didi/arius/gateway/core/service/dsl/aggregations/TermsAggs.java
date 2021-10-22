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

@Component("termsAggs")
public class TermsAggs extends BucketAggsType {

	private String name = "terms";
	
	@Autowired
	private AggsTypes aggsTypes;
	
	@PostConstruct
	public void init() {
		aggsTypes.putAggsType(name, this);
	}

	@Override
	public AggsBukcetInfo computeAggsType(JsonObject item, Map<String, FieldInfo> mergedMappings) {
		AggsBukcetInfo aggsBukcetInfo = new AggsBukcetInfo();
		
		// check field
		JsonElement field = item.get("field");
		if (field != null) {
			String strField = field.getAsString();
			if (mergedMappings.containsKey(strField)) {
				FieldInfo fieldInfo = mergedMappings.get(strField);
				
				int bucketNumber = Math.max(1, fieldInfo.getCardinality());
				aggsBukcetInfo.setBucketNumber(bucketNumber);
				
				aggsBukcetInfo.setMemUsed(bucketNumber * QueryConsts.AGGS_BUCKET_MEM_UNIT);
			}
		}
		
		JsonElement size = item.get("size");
		if (size != null) {
			int iSize = size.getAsInt();
			if (iSize > 0) {
				aggsBukcetInfo.setLastBucketNumber(Math.min(iSize, aggsBukcetInfo.getBucketNumber()));
			} else {
				aggsBukcetInfo.setLastBucketNumber(aggsBukcetInfo.getBucketNumber());
			}
			
		} else {
			aggsBukcetInfo.setLastBucketNumber(Math.min(10, aggsBukcetInfo.getBucketNumber()));
		}

		return aggsBukcetInfo;
	}

}
