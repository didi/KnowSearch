package com.didi.arius.gateway.common.metadata;

import lombok.Data;

@Data
public class RateLimitDetail {
	private int appid;

	/**
	 * dsl md5值
	 */
	private String dslMd5;

	/**
	 * 限流值
	 */
	private double queryLimit;

	/**
	 * 是否禁止
	 */
	private boolean queryForbidden;

	/**
	 * es查询开销
	 */
	private double esCostAvg;

	/**
	 * 查询命中数
	 */
	private double totalHitsAvg;

	/**
	 * 查询totalShards
	 */
	private double totalShardsAvg;
}
