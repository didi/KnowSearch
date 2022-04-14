package com.didi.arius.gateway.common.metadata;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
* @author weizijun
* @date：2016年8月23日
* 
*/
@Data
@NoArgsConstructor
public class FlowThreshold {

	/**
	 * 30MB/s
	 */
	private static final int SEARCHID_FLOW_LIMIT_IN_UPPER = 30 * 1024 * 1024;
	/**
	 * 15MB/s
	 */
	private static final int SEARCHID_FLOW_LIMIT_IN_LOWER = 15 * 1024 * 1024;
	/**
	 * 100MB/s
	 */
	private static final int SEARCHID_FLOW_LIMIT_OUT_UPPER = 100 * 1024 * 1024;
	/**
	 * 80MB/s
	 */
	private static final int SEARCHID_FLOW_LIMIT_OUT_LOWER = 80 * 1024 * 1024;
	
	/**
	 * 触发ops限流的值
	 */
	int opsUpper;

	/**
	 * 解除ops限流的值
	 */
	int opsLower;

	/**
	 * 触发request流量限流的值
	 */
	int inUpper = SEARCHID_FLOW_LIMIT_IN_UPPER;

	/**
	 * 解除request流量限流的值
	 */
	int inLower = SEARCHID_FLOW_LIMIT_IN_LOWER;

	/**
	 * 触发response流量限流的值
	 */
	int outUpper = SEARCHID_FLOW_LIMIT_OUT_UPPER;

	/**
	 * 解除response流量限流的值
	 */
	int outLower = SEARCHID_FLOW_LIMIT_OUT_LOWER;
	
}
