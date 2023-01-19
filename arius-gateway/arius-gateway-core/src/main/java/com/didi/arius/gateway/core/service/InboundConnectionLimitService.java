package com.didi.arius.gateway.core.service;

import com.didi.arius.gateway.common.connectioncontrl.ConnectionLimitResult;
import com.didi.arius.gateway.common.metadata.QueryContext;
import org.jboss.netty.channel.Channel;

public interface InboundConnectionLimitService {
		/**
		 * 收集 http 链接
		 *
		 * @param channel Netty 通道
		 */
		void onConnect(Channel channel);
		
		/**
		 * 将 http 链接 加入忽略列表
		 *
		 * @param channel Netty 通道
		 */
		void addIgnore(Channel channel);
		
		/**
		 * 绑定 http 链接和实例的关系
		 *
		 * @param queryContext 查询上下文
		 */
		void onHandle(QueryContext queryContext);
		
		/**
		 * 释放 http 链接
		 *
		 * @param channel Netty 通道
		 */
		void onDisconnect(Channel channel);
		
		/**
		 * 判断是否触发节点连接控制
		 *
		 * @return 节点的连接数限流情况
		 */
		ConnectionLimitResult nodeLimitResult();
		
		/**
		 * 判断是否触发实例连接控制
		 *
		 * @param queryContext 查询上下文
		 * @return 应用的连接数限流情况
		 */
		ConnectionLimitResult appLimitResult(QueryContext queryContext);
		
		/**
		 * 判断是不是可以处理的新连接
		 *
		 * @param queryContext 查询上下文
		 * @return 是否是新连接
		 */
		boolean isNew(QueryContext queryContext);
		
		/**
		 * 判断是不是可以忽略的连接
		 *
		 * @param queryContext 查询上下文
		 * @return 是不是可以忽略的连接
		 */
		boolean isIgnore(QueryContext queryContext);
}