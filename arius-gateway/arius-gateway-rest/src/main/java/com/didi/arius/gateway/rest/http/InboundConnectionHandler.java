package com.didi.arius.gateway.rest.http;

import com.didi.arius.gateway.common.connectioncontrl.ConnectionLimitResult;
import com.didi.arius.gateway.core.service.InboundConnectionLimitService;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class InboundConnectionHandler extends SimpleChannelHandler {
		protected final InboundConnectionLimitService inboundConnectionLimitService;
		
		public InboundConnectionHandler(InboundConnectionLimitService inboundConnectionLimitService) {
				this.inboundConnectionLimitService = inboundConnectionLimitService;
		}
		
		@Override
		public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
				ConnectionLimitResult connectionLimit = inboundConnectionLimitService.nodeLimitResult();
				if (connectionLimit.isOverConnected()) {
						inboundConnectionLimitService.addIgnore(e.getChannel());
				} else {
						inboundConnectionLimitService.onConnect(e.getChannel());
				}
				super.channelConnected(ctx, e);
		}
		
		@Override
		public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
				inboundConnectionLimitService.onDisconnect(e.getChannel());
				super.channelDisconnected(ctx, e);
		}
}