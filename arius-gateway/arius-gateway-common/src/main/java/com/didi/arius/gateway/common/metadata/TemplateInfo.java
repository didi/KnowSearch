package com.didi.arius.gateway.common.metadata;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class TemplateInfo {
	/**
	 * 是否需要message
	 */
	private boolean needSource;

	/**
	 * 索引模板版本号
	 */
	private int version = 0;

	/**
	 * 索引模板mapping列表
	 */
	private Map<String, FieldInfo> mappings = new HashMap<>();

}
