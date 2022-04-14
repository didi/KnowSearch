package com.didi.arius.gateway.common.enums;
/**
* @author weizijun
* @date：2016年8月22日
* 
*/
public enum FlowStatus {
	/**
	 * 低于下限
	 */
	DOWN,
	/**
	 * 位于流控值的上限和下限之间
	 */
	KEEP,
	/**
	 * 高于上限
	 */
	UP;
}
