package com.didi.arius.gateway.core.service.dsl.aggregations;

import com.didi.arius.gateway.common.enums.AggsTypeEnum;

public abstract class MetricsAggsType implements AggsType {
	protected AggsTypeEnum type = AggsTypeEnum.METRICS;
	
	public AggsTypeEnum getType() {
		return type;
	}
}
