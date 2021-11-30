package com.didi.arius.gateway.common.metadata;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RateLimitStat {
	/**
	 * dsl模板数量
	 */
	private Integer dslCount = 0;

	/**
	 * 禁止的dsl模板数量
	 */
	private Integer dslForbiddenCount = 0;

	/**
	 * 新产生的dsl模板数量
	 */
	private Integer newDslCount = 0;

	/**
	 * 新产生的禁止的dsl模板数量
	 */
	private Integer newDslForbiddenCount = 0;

}
