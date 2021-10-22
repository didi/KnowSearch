package com.didi.arius.gateway.common.metadata;

import lombok.Data;
import org.elasticsearch.search.fetch.source.FetchSourceContext;

/**
* @author weizijun
* @date：2017年2月28日
* 
*/
@Data
public class FetchFields {
	/**
	 * _source信息
	 */
	private FetchSourceContext fetchSourceContext;

	/**
	 * fileds信息
	 */
	private String[] fields;

	/**
	 * 是否包含message字段
	 */
	private boolean hasMessageField;
	
}
