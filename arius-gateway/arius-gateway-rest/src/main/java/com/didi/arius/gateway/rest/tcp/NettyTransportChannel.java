package com.didi.arius.gateway.rest.tcp;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.ActionContext;
import org.elasticsearch.Version;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.bytes.ReleasablePagedBytesReference;
import org.elasticsearch.common.compress.CompressorFactory;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.ReleasableBytesStreamOutput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.lease.Releasables;
import org.elasticsearch.common.netty.ReleaseChannelFutureListener;
import org.elasticsearch.transport.*;
import org.elasticsearch.transport.netty.NettyHeader;
import org.elasticsearch.transport.support.TransportStatus;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
* @author weizijun
* @date：2016年9月18日
* 
*/
public class NettyTransportChannel implements TransportChannel {
	protected static final Logger statLogger = LoggerFactory.getLogger(QueryConsts.STAT_LOGGER);

    private final NettyTransport transport;
    private final TransportServiceAdapter transportServiceAdapter;
    private final Version version;
    private final String action;
    private final Channel channel;
    private final long requestId;
    private final String profileName;
    private ActionContext actionContext;

    public NettyTransportChannel(NettyTransport transport, TransportServiceAdapter transportServiceAdapter, String action, Channel channel, long requestId, Version version, String profileName) {
        this.transportServiceAdapter = transportServiceAdapter;
        this.version = version;
        this.transport = transport;
        this.action = action;
        this.channel = channel;
        this.requestId = requestId;
        this.profileName = profileName;
    }

    @Override
    public String getProfileName() {
        return profileName;
    }

    @Override
    public String action() {
        return this.action;
    }

    @Override
    public void sendResponse(TransportResponse response) throws IOException {
        sendResponse(response, TransportResponseOptions.EMPTY);
    }

	@Override
    public void sendResponse(TransportResponse response, TransportResponseOptions options) throws IOException {
		if (NettyTransport.COMPRESS) {
            options = TransportResponseOptions.builder(options).withCompress(transport.COMPRESS ).build();
        }

        byte status = 0;
        status = TransportStatus.setResponse(status);

        ReleasableBytesStreamOutput bStream = new ReleasableBytesStreamOutput(NettyTransport.bigArrays);
        boolean addedReleaseListener = false;
        try {
            bStream.skip(NettyHeader.HEADER_SIZE);
            StreamOutput stream = bStream;
            if (options.compress()) {
                status = TransportStatus.setCompress(status);
                stream = CompressorFactory.defaultCompressor().streamOutput(stream);
            }
            stream.setVersion(version);
            response.writeTo(stream);
            stream.close();

            ReleasablePagedBytesReference bytes = bStream.bytes();
            
            if (actionContext != null) {
            	actionContext.setResponseLength(bytes.length());
            	statLogger.info(QueryConsts.DLFLAG_PREFIX + "query_tcp_response_length||type=response||requestId={}||appid={}||responseLen={}", actionContext.getRequestId(), actionContext.getAppid(), actionContext.getResponseLength());
            }

            ChannelBuffer buffer = bytes.toChannelBuffer();
            NettyHeader.writeHeader(buffer, requestId, status, version);
            ChannelFuture future = channel.write(buffer);
            ReleaseChannelFutureListener listener = new ReleaseChannelFutureListener(bytes);
            future.addListener(listener);
            addedReleaseListener = true;
            transportServiceAdapter.onResponseSent(requestId, action, response, options);
        } finally {
            if (!addedReleaseListener) {
                Releasables.close(bStream.bytes());
            }
        }
    }

    @Override
    public void sendResponse(Throwable error) throws IOException {
        try (BytesStreamOutput stream = new BytesStreamOutput()){
            stream.skip(NettyHeader.HEADER_SIZE);
            RemoteTransportException tx = new RemoteTransportException("", transport.wrapAddress(channel.getLocalAddress()), action, error);
            stream.writeThrowable(tx);
            byte status = 0;
            status = TransportStatus.setResponse(status);
            status = TransportStatus.setError(status);

            BytesReference bytes = stream.bytes();

            if (actionContext != null) {
                statLogger.info(QueryConsts.DLFLAG_PREFIX + "query_tcp_response_length||type=exception||requestId={}||appid={}||responseLen={}", actionContext.getRequestId(), actionContext.getAppid(), bytes.length());
            }

            ChannelBuffer buffer = bytes.toChannelBuffer();
            NettyHeader.writeHeader(buffer, requestId, status, version);
            channel.write(buffer);
            transportServiceAdapter.onResponseSent(requestId, action, error);
        }
    }

    @Override
    public long getRequestId() {
        return requestId;
    }

    @Override
    public String getChannelType() {
        return "netty";
    }

    /**
     * Returns the underlying netty channel. This method is intended be used for access to netty to get additional
     * details when processing the request and may be used by plugins. Responses should be sent using the methods
     * defined in this class and not directly on the channel.
     * @return underlying netty channel
     */
    public Channel getChannel() {
        return channel;
    }

	public ActionContext getActionContext() {
		return actionContext;
	}

	public void setActionContext(ActionContext actionContext) {
		this.actionContext = actionContext;
	}
}