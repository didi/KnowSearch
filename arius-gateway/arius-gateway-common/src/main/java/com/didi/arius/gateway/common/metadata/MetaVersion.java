package com.didi.arius.gateway.common.metadata;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MetaVersion {
	/**
	 * 大基数字段的version
	 */
	private String largeFieldVersion = "-1"; 
	
}
