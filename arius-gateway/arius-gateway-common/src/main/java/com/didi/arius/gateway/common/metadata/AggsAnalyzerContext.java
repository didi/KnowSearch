package com.didi.arius.gateway.common.metadata;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * aggs请求的上下文类
 */
@Data
@NoArgsConstructor
public class AggsAnalyzerContext {

	/**
	 * aggs的层数
	 */
	private int maxLevel;

}
