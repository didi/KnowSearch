package com.didi.arius.gateway.core.service.connectioncontrl;

import com.didi.arius.gateway.common.connectioncontrl.ConnectionLimit;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.remote.response.ConnectionLimitInfoResponse;
import com.didi.arius.gateway.remote.response.ConnectionLimitResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InboundConnectionLimitHolder {
		/**
		 * 节点上的连接数上限配置
		 */
		protected              ConnectionLimit               nodeLimit       = null;
		/**
		 * 默认的实例的连接数上限配置
		 */
		protected              ConnectionLimit               defaultAppLimit = null;
		/**
		 * 单独配置过的实例的连接数上限配置
		 */
		protected              Map<Integer, ConnectionLimit> appLimitMap = new HashMap<>(0);
		protected static final Logger                        bootLogger  = LoggerFactory.getLogger(
						QueryConsts.BOOT_LOGGER);
		
		/**
		 * 获取节点的连接数上限
		 * @return
		 */
		public Long nodeLimit() {
				return Optional.ofNullable(nodeLimit).map(ConnectionLimit::getThreshold).orElse(Long.MAX_VALUE);
		}
		
		/**
		 * 获取实例的连接数上限 * * @return
		 */
		public Long appLimit(Integer appid) {
				ConnectionLimit resultAppLimit = appLimitMap.get(appid);
				if (Objects.isNull(resultAppLimit)) {
						resultAppLimit = defaultAppLimit;
				}
				return Optional.ofNullable(resultAppLimit).map(ConnectionLimit::getThreshold).orElse(Long.MAX_VALUE);
		}
		
		/**
		 * 重置连接数上限为最新
		 * @return
		 */
		public synchronized void reset(String id, ConnectionLimitInfoResponse connectionLimitInfoResponse) {
				long startTime = System.currentTimeMillis();
				if (bootLogger.isDebugEnabled()) {
						bootLogger.debug("重置节点的连接数上限 id: {} param: {} startTime: {}", id, connectionLimitInfoResponse, startTime);
				}
				if (Objects.isNull(connectionLimitInfoResponse)) {
						this.nodeLimit = null;
						this.appLimitMap = new HashMap<>(0);
						this.defaultAppLimit = null;
						return;
				}
				ConnectionLimitResponse nodeLimitResponse = Optional.ofNullable(connectionLimitInfoResponse.getNodeLimit())
								.orElse(connectionLimitInfoResponse.getDefaultNodeLimit());
				this.nodeLimit = convert2ConnectionLimit(nodeLimitResponse);
				List<ConnectionLimitResponse> appLimitResponse = connectionLimitInfoResponse.getAppLimit();
				if (CollectionUtils.isEmpty(appLimitResponse)) {
						this.appLimitMap = new HashMap<>(0);
				} else {
						Map<Integer, ConnectionLimit> appLimit = new HashMap<>(connectionLimitInfoResponse.getAppLimit().size());
						connectionLimitInfoResponse.getAppLimit().forEach(
										x -> appLimit.put(x.getAppid(), convert2ConnectionLimit(x)));
						this.appLimitMap = appLimit;
				}
				ConnectionLimitResponse defaultAppLimitResponse = connectionLimitInfoResponse.getDefaultAppLimit();
				this.defaultAppLimit = convert2ConnectionLimit(defaultAppLimitResponse);
				Long cost = System.currentTimeMillis() - startTime;
				if (bootLogger.isDebugEnabled()) {
						bootLogger.debug("重置节点的连接数上限完成 id: {} , cost {}", id, cost);
				}
		}
		
		private ConnectionLimit convert2ConnectionLimit(ConnectionLimitResponse nodeLimitResponse) {
				if (Objects.isNull(nodeLimitResponse)) {
						return null;
				}
				ConnectionLimit connectionLimit = new ConnectionLimit();
				connectionLimit.setId(nodeLimitResponse.getId());
				connectionLimit.setThreshold(nodeLimitResponse.getThreshold());
				return connectionLimit;
		}
}