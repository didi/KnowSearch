package com.didi.arius.gateway.remote.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionLimitResponse {
		/**
		 * 配额项 id
		 */
		private Long    id;
		/**
		 * 0 节点 1 实例
		 */
		private Integer type;
		/**
		 * 实例 id
		 */
		private Integer appid;
		/**
		 * 连接数阈值
		 */
		private Long    threshold;
		
}