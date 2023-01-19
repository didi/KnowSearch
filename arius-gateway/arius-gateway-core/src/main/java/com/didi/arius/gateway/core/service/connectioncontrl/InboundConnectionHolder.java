package com.didi.arius.gateway.core.service.connectioncontrl;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.elasticsearch.http.netty.NettyHttpRequest;
import org.elasticsearch.rest.RestRequest;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InboundConnectionHolder {
		/**
		 * 连接和实例的关系
		 */
		private final          ConcurrentHashMap<Channel, Integer>      connectionAppMap     = new ConcurrentHashMap<>();
		/**
		 * 应用和连接的关系
		 */
		private final          ConcurrentHashMap<Integer, Set<Channel>> appConnectionMap     = new ConcurrentHashMap<>();
		/**
		 * 因为节点连接数导致将被掐断的连接
		 */
		private final          Set<Channel>                             ignoredConnectionSet = Sets.newConcurrentHashSet();
		protected static final Logger                                   bootLogger           = LoggerFactory.getLogger(
						QueryConsts.BOOT_LOGGER);
		
		/**
		 * 缓存链接     *     * @param channel 连接
		 */
		public void add(Channel channel) {
				if (bootLogger.isDebugEnabled()) {
						bootLogger.debug("缓存新连接, channel {}, remoteAddress {}", channel.getId(), channel.getRemoteAddress());
				}
				connectionAppMap.put(channel, QueryConsts.TOTAL_APPID_ID);
		}
		
		/**
		 * 节点连接数超过上限时，需要提前的掐断这部分连接
		 * 但连接的建立实在在处理 http 数据之前，没办法在 channelConnected 里面判断请求协议，也就没办法给一个统一的返回体
		 * 解法是，维护一个
		 * ignoreSet，在处理请求的时候，直接掐断他们
		 *
		 * @param channel 连接
		 */
		public void ignore(Channel channel) {
				if (bootLogger.isDebugEnabled()) {
						bootLogger.debug("缓存待忽略的连接, channel {}, remoteAddress {}", channel.getId(),
						                 channel.getRemoteAddress());
				}
				ignoredConnectionSet.add(channel);
		}
		
		/**
		 * 摘除链接
		 *
		 * @param channel 连接
		 */
		public void remove(Channel channel) {
				if (bootLogger.isDebugEnabled()) {
						bootLogger.debug("断开连接, channel {}, remoteAddress {}", channel.getId(), channel.getRemoteAddress());
				}
				ignoredConnectionSet.remove(channel);
				Integer appid = connectionAppMap.remove(channel);
				if (Objects.isNull(appid)) {
						return;
				}
				if (bootLogger.isDebugEnabled()) {
						bootLogger.debug("断开连接, 解绑实例关系, channel {}, appid {}, remoteAddress {}", channel.getId(), appid,
						                 channel.getRemoteAddress());
				}
				removeAppConnection(appid, channel);
		}
		
		/**
		 * 缓存链接、应用关系
		 *
		 * @param queryContext 查询上下文
		 */
		public void bindToApp(QueryContext queryContext) {
				if (!(queryContext.getRequest() instanceof NettyHttpRequest)) {
						return;
				}
				NettyHttpRequest request = (NettyHttpRequest) queryContext.getRequest();
				Channel channel = request.getChannel();
				int appid = queryContext.getAppid();
				if (bootLogger.isDebugEnabled()) {
						bootLogger.debug("绑定实例关系, channel {}, appid {}, remoteAddress {}", channel.getId(), appid,
						                 channel.getRemoteAddress());
				}
				Integer bindAppid = connectionAppMap.get(channel);    // 解绑老的实例、连接关系  
				if (!Objects.equals(bindAppid, appid)) {
						if (Objects.nonNull(bindAppid)) {
								if (bootLogger.isDebugEnabled()) {
										bootLogger.debug("解绑实例关系, channel {}, appid {}, remoteAddress {}", channel.getId(), appid,
										                 channel.getRemoteAddress());
								}
								removeAppConnection(bindAppid, channel);
						}
						connectionAppMap.put(channel, appid);
						addAppConnection(appid, channel);
				}
		}
		
		public int connectionSize() {
				return connectionAppMap.size() + ignoredConnectionSet.size();
		}
		
		public int connectionSize(Integer appid) {
				return Optional.ofNullable(appConnectionMap.get(appid)).map(Set::size).orElse(0);
		}
		
		public Map<Integer, Integer> appConnectionSize() {
				Map<Integer, Integer> result = new HashMap<>(appConnectionMap.size());
				appConnectionMap.forEach((app, connectionSet) -> {
						if (app != QueryConsts.TOTAL_APPID_ID) {
								result.put(app, connectionSet.size());
						}
				});
				return result;
		}
		
		/**
		 * 没有发起过请求的连接，是新连接
		 * 更换了访问 appid 的连接不是新连接
		 *
		 * @param queryContext 查询上下文
		 * @return 是否为新连接
		 */
		public boolean isNew(QueryContext queryContext) {
				RestRequest restRequest = queryContext.getRequest();
				if (!(restRequest instanceof NettyHttpRequest)) {
						return false;
				}
				Channel channel = ((NettyHttpRequest) restRequest).getChannel();
				Integer appid = connectionAppMap.get(channel);
				if (Objects.isNull(appid) || Objects.equals(appid, QueryConsts.TOTAL_APPID_ID)) {
						return true;
				}
				return !Objects.equals(appid, queryContext.getAppid());
		}
		
		public boolean isIgnore(QueryContext queryContext) {
				RestRequest restRequest = queryContext.getRequest();
				if (!(restRequest instanceof NettyHttpRequest)) {
						return false;
				}
				Channel channel = ((NettyHttpRequest) restRequest).getChannel();
				return ignoredConnectionSet.contains(channel);
		}
		
		private void addAppConnection(int appid, Channel channel) {
				Set<Channel> appConnections = getAppConnections(appid);
				appConnections.add(channel);
		}
		
		private void removeAppConnection(int appid, Channel channel) {
				Set<Channel> appConnections = getAppConnections(appid);
				appConnections.remove(channel);
		}
		
		private Set<Channel> getAppConnections(int appid) {
				Set<Channel> appConnection = appConnectionMap.get(appid);
				if (Objects.isNull(appConnection)) {
						appConnection = appConnectionMap.computeIfAbsent(appid, ignore -> Sets.newConcurrentHashSet());
				}
				return appConnection;
		}
}