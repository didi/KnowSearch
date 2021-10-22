package com.didi.arius.gateway.core.service.dsl.aggregations;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.AggsBukcetInfo;
import com.didi.arius.gateway.common.metadata.FieldInfo;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component("dateHistogramAggs")
public class DateHistogramAggs extends BucketAggsType {

	private String name = "date_histogram";
	
	public static final ImmutableMap<String, TimeValue> DATE_FIELD_UNITS;
	
    static {
        DATE_FIELD_UNITS = MapBuilder.<String, TimeValue>newMapBuilder()
                .put("year", new TimeValue(365 * 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS))
                .put("1y", new TimeValue(365 * 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS))
                .put("quarter", new TimeValue(90 * 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS))
                .put("1q", new TimeValue(90 * 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS))
                .put("month", new TimeValue(30 * 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS))
                .put("1M", new TimeValue(30 * 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS))
                .put("week", TimeValue.parseTimeValue("1w", null, "DateHistogramAggs.interval"))
                .put("1w", TimeValue.parseTimeValue("1w", null, "DateHistogramAggs.interval"))
                .put("day", TimeValue.parseTimeValue("1d", null, "DateHistogramAggs.interval"))
                .put("1d", TimeValue.parseTimeValue("1d", null, "DateHistogramAggs.interval"))
                .put("hour", TimeValue.parseTimeValue("1h", null, "DateHistogramAggs.interval"))
                .put("1h", TimeValue.parseTimeValue("1h", null, "DateHistogramAggs.interval"))
                .put("minute", TimeValue.parseTimeValue("1m", null, "DateHistogramAggs.interval"))
                .put("1m", TimeValue.parseTimeValue("1m", null, "DateHistogramAggs.interval"))
                .put("second", TimeValue.parseTimeValue("1s", null, "DateHistogramAggs.interval"))
                .put("1s", TimeValue.parseTimeValue("1s", null, "DateHistogramAggs.interval"))
                .immutableMap();
    }
	
	@Autowired
	private AggsTypes aggsTypes;
	
	@PostConstruct
	public void init() {
		aggsTypes.putAggsType(name, this);
	}
	
	@Override
	public AggsBukcetInfo computeAggsType(JsonObject item, Map<String, FieldInfo> mergedMappings) {
		JsonElement interval = item.get("interval");
		if (interval == null) {
			return AggsBukcetInfo.createSingleBucket();
		}

		String strInterval = interval.getAsString();

		if (strInterval == null) {
			return AggsBukcetInfo.createSingleBucket();
		}
		
		TimeValue timeValue = DATE_FIELD_UNITS.get(strInterval);
		if (timeValue == null) {
			timeValue = TimeValue.parseTimeValue(strInterval, null, getClass().getSimpleName() + ".interval");
		}

		long lInterval = timeValue.getMillis();
		
		if (lInterval <= 0) {
			return AggsBukcetInfo.createSingleBucket(); 
		}
		
		int bucket = (int) (QueryConsts.DATE_HISTOGRAM_DEFAUL_RANGE / lInterval);
		
		JsonElement extendedBounds = item.get("extended_bounds");
		if (extendedBounds != null) {
			long lMin = 0;
			long lMax = 0;
			JsonObject jsonExtended = extendedBounds.getAsJsonObject();
			JsonElement min = jsonExtended.get("min");
			if (min != null) {
				lMin = min.getAsLong();
			}
			
			JsonElement max = jsonExtended.get("max");
			if (max != null) {
				lMax = max.getAsLong();
			}
			
			if (lMin > 0 && lMax >= lMin) {
				bucket = (int) ((lMax + 1 - lMin) / lInterval);
			}
		}
		
		AggsBukcetInfo aggsBukcetInfo = new AggsBukcetInfo();
		
		aggsBukcetInfo.setBucketNumber(bucket);
		aggsBukcetInfo.setLastBucketNumber(bucket);
		aggsBukcetInfo.setMemUsed(bucket * QueryConsts.AGGS_BUCKET_MEM_UNIT);
		
		return aggsBukcetInfo;	
	}

}
