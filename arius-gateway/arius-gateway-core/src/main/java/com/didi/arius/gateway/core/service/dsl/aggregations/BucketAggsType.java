package com.didi.arius.gateway.core.service.dsl.aggregations;

import com.didi.arius.gateway.common.enums.AggsTypeEnum;

public abstract class BucketAggsType implements AggsType {
	protected AggsTypeEnum type = AggsTypeEnum.BUCKET;
	
	public AggsTypeEnum getType() {
		return type;
	}
}
