package com.didi.arius.gateway.common.metadata;

import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;

@Data
public class DSLTemplate {
	/**
	 * dsl模板限流类
	 */
	private RateLimiter rateLimiter = null;

	/**
	 * dsl模板总的限流值
	 */
	private double totalQueryLimit;

	/**
	 * 当前节点的dsl模板限流值
	 */
	private double queryLimit;

	/**
	 * 是否禁止查询
	 */
	private boolean queryForbidden;

	/**
	 * dsl模板es查询开销
	 */
	private double esCostAvg;

	/**
	 * dsl模板平均命中数量
	 */
	private double totalHitsAvg;

	/**
	 * dsl模板查询平均total_shards数
	 */
	private double totalShardsAvg;
	
	public DSLTemplate(double totalQueryLimit, double queryLimit, boolean queryForbidden) {
		this.totalQueryLimit = totalQueryLimit;
		this.queryLimit = queryLimit;
		this.queryForbidden = queryForbidden;
		rateLimiter = RateLimiter.create(queryLimit);
	}

	public void updateRateLimiter(double queryLimit) {
		rateLimiter.setRate(queryLimit);
	}
	
	public String toString() {
		return "queryForbidden=" + queryForbidden + "||queryLimit=" + queryLimit + "||qps=" + rateLimiter.getRate();
	}

}
