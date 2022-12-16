package com.didi.arius.gateway.common.connectioncontrl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionLimit {
		/**
		 * 对应的配置项 id
		 */
		private Long id;
		/**
		 * 上限阈值
		 */
		private Long threshold;
}