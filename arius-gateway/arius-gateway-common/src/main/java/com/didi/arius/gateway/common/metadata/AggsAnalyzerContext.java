package com.didi.arius.gateway.common.metadata;

import lombok.Data;

/**
 * aggs请求的上下文类
 */
@Data
public class AggsAnalyzerContext {

	/**
	 * aggs的层数
	 */
	private int maxLevel;

}
