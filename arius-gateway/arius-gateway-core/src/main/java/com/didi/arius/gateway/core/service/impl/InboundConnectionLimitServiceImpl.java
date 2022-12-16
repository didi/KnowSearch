package com.didi.arius.gateway.core.service.impl;

import com.didi.arius.gateway.common.connectioncontrl.ConnectionLimitResult;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.service.InboundConnectionLimitService;
import com.didi.arius.gateway.core.service.connectioncontrl.InboundConnectionHolder;
import com.didi.arius.gateway.core.service.connectioncontrl.InboundConnectionLimitHolder;
import org.jboss.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InboundConnectionLimitServiceImpl implements InboundConnectionLimitService {
		@Autowired
		private InboundConnectionHolder      inboundConnectionHolder;
		@Autowired
		private InboundConnectionLimitHolder inboundConnectionLimitHolder;
		
		@Override
		public void onConnect(Channel channel) {
				inboundConnectionHolder.add(channel);
		}
		
		@Override
		public void addIgnore(Channel channel) {
				inboundConnectionHolder.ignore(channel);
		}
		
		@Override
		public void onHandle(QueryContext queryContext) {
				inboundConnectionHolder.bindToApp(queryContext);
		}
		
		@Override
		public void onDisconnect(Channel channel) {
				inboundConnectionHolder.remove(channel);
		}
		
		@Override
		public ConnectionLimitResult nodeLimitResult() {
				return new ConnectionLimitResult((long) inboundConnectionHolder.connectionSize(),
				                                 inboundConnectionLimitHolder.nodeLimit());
		}
		
		@Override
		public ConnectionLimitResult appLimitResult(QueryContext queryContext) {
				final int appid = queryContext.getAppid();
				if (appid == QueryConsts.TOTAL_APPID_ID) {
						return new ConnectionLimitResult((long) inboundConnectionHolder.connectionSize());
				}
				return new ConnectionLimitResult((long) inboundConnectionHolder.connectionSize(appid),
				                                 inboundConnectionLimitHolder.appLimit(appid));
		}
		
		@Override
		public boolean isNew(QueryContext queryContext) {
				return inboundConnectionHolder.isNew(queryContext);
		}
		
		@Override
		public boolean isIgnore(QueryContext queryContext) {
				return inboundConnectionHolder.isIgnore(queryContext);
		}
}