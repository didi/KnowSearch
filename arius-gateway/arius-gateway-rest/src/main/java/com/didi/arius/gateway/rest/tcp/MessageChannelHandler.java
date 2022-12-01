package com.didi.arius.gateway.rest.tcp;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.es.tcp.ActionController;
import com.didi.arius.gateway.core.es.tcp.ActionHandler;
import com.didi.arius.gateway.core.service.RequestStatsService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.apache.lucene.util.IOUtils;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.compress.Compressor;
import org.elasticsearch.common.compress.CompressorFactory;
import org.elasticsearch.common.compress.NotCompressedException;
import org.elasticsearch.common.io.stream.NamedWriteableAwareStreamInput;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.util.concurrent.KeyedLock;
import org.elasticsearch.transport.TransportChannel;
import org.elasticsearch.transport.TransportRequest;
import org.elasticsearch.transport.netty.ChannelBufferStreamInputFactory;
import org.elasticsearch.transport.netty.NettyHeader;
import org.elasticsearch.transport.netty.NettyTransport.NodeChannels;
import org.elasticsearch.transport.netty.SizeHeaderFrameDecoder;
import org.elasticsearch.transport.support.TransportStatus;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import static org.elasticsearch.common.transport.NetworkExceptionHelper.isCloseConnectionException;
import static org.elasticsearch.common.transport.NetworkExceptionHelper.isConnectException;
import static org.elasticsearch.common.util.concurrent.ConcurrentCollections.newConcurrentMap;

/**
 * @author weizijun @date：2016年9月12日
 * 
 */
@Component("messageChannelHandler")
public class MessageChannelHandler extends SimpleChannelUpstreamHandler {

	@Autowired
	protected NettyTransport transport;
	
	@Autowired
	protected Adapter transportServiceAdapter;

	@Autowired
	protected ActionController actionController;

	@Autowired
	protected RequestStatsService requestStatsService;

	private static final ILog logger = LogFactory.getLog(MessageChannelHandler.class);
	
	// node id to actual channel
    protected final ConcurrentMap<DiscoveryNode, NodeChannels> connectedNodes = newConcurrentMap();
    
    protected final KeyedLock<String> connectionLock = new KeyedLock<>();
    
    protected final NamedWriteableRegistry namedWriteableRegistry = new NamedWriteableRegistry();
    
	@Autowired
	protected QueryConfig queryConfig;

	@Override
	public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
		transportServiceAdapter.sent(e.getWrittenAmount());
		super.writeComplete(ctx, e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object m = e.getMessage();
		if (!(m instanceof ChannelBuffer)) {
			ctx.sendUpstream(e);
			return;
		}
		ChannelBuffer buffer = (ChannelBuffer) m;
		int size = buffer.getInt(buffer.readerIndex() - 4);
		transportServiceAdapter.received((long)size + 6);

		// we have additional bytes to read, outside of the header
		boolean hasMessageBytesToRead = (size - (NettyHeader.HEADER_SIZE - 6)) != 0;

		int markedReaderIndex = buffer.readerIndex();
		int expectedIndexReader = markedReaderIndex + size;

		// netty always copies a buffer, either in NioWorker in its read
		// handler, where it copies to a fresh
		// buffer, or in the cumlation buffer, which is cleaned each time
		StreamInput streamIn = ChannelBufferStreamInputFactory.create(buffer, size);
		try {
			long requestId = streamIn.readLong();
			byte status = streamIn.readByte();
			Version version = Version.fromId(streamIn.readInt());

			if (TransportStatus.isCompress(status) && hasMessageBytesToRead && buffer.readable()) {
				Compressor compressor;
				try {
					compressor = CompressorFactory.compressor(buffer);
				} catch (NotCompressedException ex) {
					int maxToRead = Math.min(buffer.readableBytes(), 10);
					int offset = buffer.readerIndex();
					StringBuilder sb = new StringBuilder(
							"stream marked as compressed, but no compressor found, first [").append(maxToRead)
									.append("] content bytes out of [").append(buffer.readableBytes())
									.append("] readable bytes with message size [").append(size).append("] ")
									.append("] are [");
					for (int i = 0; i < maxToRead; i++) {
						sb.append(buffer.getByte(offset + i)).append(",");
					}
					sb.append("]");
					throw new IllegalStateException(sb.toString());
				}
				streamIn = compressor.streamInput(streamIn);
			}
			streamIn.setVersion(version);

			if (TransportStatus.isRequest(status)) {
                String action = handleRequest(ctx.getChannel(), streamIn, requestId, version, size);

                // Chek the entire message has been read
                final int nextByte = streamIn.read();
                // calling read() is useful to make sure the message is fully read, even if there some kind of EOS marker
                if (nextByte != -1) {
                    throw new IllegalStateException("Message not fully read (request) for requestId [" + requestId + "], action ["
                            + action + "], readerIndex [" + buffer.readerIndex() + "] vs expected [" + expectedIndexReader + "]; resetting");
                }
                if (buffer.readerIndex() < expectedIndexReader) {
                    throw new IllegalStateException("Message is fully read (request), yet there are " + (expectedIndexReader - buffer.readerIndex()) + " remaining bytes; resetting");
                }
                if (buffer.readerIndex() > expectedIndexReader) {
                    throw new IllegalStateException("Message read past expected size (request) for requestId [" + requestId + "], action ["
                            + action + "], readerIndex [" + buffer.readerIndex() + "] vs expected [" + expectedIndexReader + "]; resetting");
                }
			}
		} finally {
			try {
				IOUtils.closeWhileHandlingException(streamIn);
			} finally {
				// Set the expected position of the buffer, no matter what
				// happened
				buffer.readerIndex(expectedIndexReader);
			}
		}
	}
	
    protected String handleRequest(Channel channel, StreamInput buffer, long requestId, Version version, int requestLength) throws Exception {
        buffer = new NamedWriteableAwareStreamInput(buffer, namedWriteableRegistry);
        final String action = buffer.readString();
        transportServiceAdapter.onRequestReceived(requestId, action);
        final NettyTransportChannel transportChannel = new NettyTransportChannel(transport, transportServiceAdapter, action, channel, requestId, version, "");        
        ActionHandler handler = actionController.getHandler(action);
        final TransportRequest request = handler.parseRequest(new InetSocketTransportAddress((InetSocketAddress) channel.getRemoteAddress()), buffer);
        
        ActionContext context = parseContext(action, request, channel, transportChannel, requestLength);
        
        transportChannel.setActionContext(context);
        
        handler.handleRequest(context);
        
        return action;
    }
    
    private ActionContext parseContext(String action, final TransportRequest request, Channel channel, TransportChannel transportChannel, int requestLength) {
    	ActionContext context = new ActionContext();
		String searchId = request.getHeader(QueryConsts.HEAD_SEARCH_ID);
		String clusterId = request.getHeader(QueryConsts.HEAD_CLUSTER_ID);
		String user = request.getHeader(QueryConsts.HEAD_USER);
		String authentication = request.getHeader(QueryConsts.HEAD_AUTHORIZATION);
		
		String traceid = request.getHeader(QueryConsts.TRACE_ID);
		String spanid = request.getHeader(QueryConsts.SPAN_ID);
		
		if (traceid == null) {
			traceid = "";
		}
		
		if (spanid == null) {
			spanid = "";
		}		
		
		String remoteAddr = ((InetSocketAddress)channel.getRemoteAddress()).getHostString();
		
		if (searchId == null) {
			searchId = QueryConsts.TOTAL_SEARCH_ID;
		}
		
		context.setTraceid(traceid);
		context.setRequestId(UUID.randomUUID().toString());
		context.setSearchId(searchId);
		context.setClusterId(clusterId);
		context.setUser(user);
		context.setRemoteAddr(remoteAddr);
		context.setAuthentication(authentication);
		
		context.setTraceid(traceid);
		context.setSpanid(spanid);

		context.setRequest(request);
		context.setChannel(transportChannel);		
		context.setActionName(action);
		
		context.setSemaphore(queryConfig.getTcpSemaphore());
		context.setRequestLength(requestLength);
		context.setDetailLog(true);

		requestStatsService.putActionContext(context.getRequestId(), context);
		
		return context;
    }
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		logger.info("client connect,client:{}", ctx.getChannel().getRemoteAddress());
	}
	
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		logger.info("client disconnect,clent:{}", ctx.getChannel().getRemoteAddress());
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        if (isCloseConnectionException(e.getCause())) {
            logger.trace("close connection exception caught on transport layer , disconnecting from relevant node", e.getCause());
            // close the channel, which will cause a node to be disconnected if relevant
            ctx.getChannel().close();
        } else if (isConnectException(e.getCause())) {
            logger.trace("connect exception caught on transport layer", e.getCause());
            // close the channel as safe measure, which will cause a node to be disconnected if relevant
            ctx.getChannel().close();
        } else if (e.getCause() instanceof CancelledKeyException) {
            logger.trace("cancelled key exception caught on transport layer, disconnecting from relevant node", e.getCause());
            // close the channel as safe measure, which will cause a node to be disconnected if relevant
            ctx.getChannel().close();
        } else if (e.getCause() instanceof SizeHeaderFrameDecoder.HttpOnTransportException) {
            // in case we are able to return data, serialize the exception content and sent it back to the client
            if (ctx.getChannel().isOpen()) {
                ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(e.getCause().getMessage().getBytes(StandardCharsets.UTF_8));
                ChannelFuture channelFuture = ctx.getChannel().write(buffer);
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        future.getChannel().close();
                    }
                });
            }
        } else {
            logger.warn("exception caught on transport layer, closing connection", e.getCause());
            // close the channel, which will cause a node to be disconnected if relevant
            ctx.getChannel().close();
        }
    }
}
