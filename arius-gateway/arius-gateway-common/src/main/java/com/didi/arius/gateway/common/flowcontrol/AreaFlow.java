package com.didi.arius.gateway.common.flowcontrol;

import com.didi.arius.gateway.common.enums.FlowStatus;
import lombok.Data;

/**
 * @author weizijun 
 * @date：2016年8月22日
 * 
 */
@Data
public class AreaFlow {
	private Flow in;

	private Flow out;

	private Flow ops;

	private FlowStatus status;

}
