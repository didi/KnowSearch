package com.didi.arius.gateway.common.enums;
/**
* @author weizijun
* @date：2016年8月22日
* 
*/
public enum FlowStatus {
	DOWN,/**低于下限*/
	KEEP,/**位于流控值的上限和下限之间*/
	UP;/**高于上限*/ 
}
