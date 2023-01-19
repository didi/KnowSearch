package com.didi.arius.gateway.remote.response;

import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionLimitInfoResponse {
		/**
		 * 主机标识
		 */
		private String hostName;
		/**
		 * 节点配置
		 */
		private ConnectionLimitResponse       nodeLimit;
		/**
		 * 默认节点配置
		 */
		private ConnectionLimitResponse       defaultNodeLimit;
		/**
		 * 实例配置
		 */
		private List<ConnectionLimitResponse> appLimit;
		/**
		 * 默认实例配置
		 */
		private ConnectionLimitResponse       defaultAppLimit;
		
		@Override
		public String toString() {
				return "hostName:" + hostName + "|" + "nodeLimit:" + (Objects.nonNull(nodeLimit) ? nodeLimit : "null") + "|"
				       + "appLimit:" + (Objects.nonNull(appLimit) ? StringUtils.join(appLimit, ",") : "null") + "|"
				       + "defaultNodeLimit:" + (Objects.nonNull(defaultNodeLimit) ? defaultNodeLimit : "null") + "|"
				       + "defaultAppLimit:" + (Objects.nonNull(defaultAppLimit) ? defaultAppLimit : "null");
		}
}