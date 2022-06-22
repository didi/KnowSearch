package com.didi.arius.gateway.remote.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
* @author weizijun
* @date：2016年8月18日
* 
*/
@Data
@NoArgsConstructor
public class AppDetailResponse {
	private int id;
	private String name;
	private Integer projectId;
	private List<String> indexExp;
	private List<String> windexExp;
	private String verifyCode;
	private List<String> ip;
	private String cluster;
	private int queryThreshold;
	private int isRoot;

	/**
	 * 是否生效DSL分析查询限流值 1为生效DSL分析查询限流值，0不生效DSL分析查询限流值
	 */
	private int      dslAnalyzeEnable;
	
	/**
	 * 是否索引存储分离，1为分离，0为不分离
	 */
	private int      isSourceSeparated;

	/**
	 * 是否生效聚合查询分析，0为不生效，1为生效
	 */
	private int aggrAnalyzeEnable;

	/**
	 * 是否生效记录响应结果的索引列表, 0为不生效，1为生效
	 */
	private int analyzeResponseEnable;

	/**
	 * appid查询方式，0为集群模式，1为索引模式
	 */
	private int searchType;
	
}