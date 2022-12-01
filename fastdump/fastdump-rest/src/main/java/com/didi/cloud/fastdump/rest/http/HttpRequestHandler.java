package com.didi.cloud.fastdump.rest.http;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.http.netty.NettyHttpRequest;
import org.elasticsearch.http.netty.pipelining.OrderedUpstreamMessageEvent;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * Created by linyunan on 2022/8/4
 */
@ChannelHandler.Sharable
public class HttpRequestHandler extends SimpleChannelUpstreamHandler {

    private final NettyHttpServerTransport serverTransport;
    private final boolean                  httpPipeliningEnabled;
    private final boolean                  detailedErrorsEnabled;

    protected final ESLogger               LOGGER = Loggers.getLogger(HttpRequestHandler.class);

    public HttpRequestHandler(NettyHttpServerTransport serverTransport, boolean detailedErrorsEnabled) {
        this.serverTransport = serverTransport;
        this.httpPipeliningEnabled = serverTransport.pipelining;
        this.detailedErrorsEnabled = detailedErrorsEnabled;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        long start = System.currentTimeMillis();

        HttpRequest request;
        OrderedUpstreamMessageEvent oue = null;
        if (this.httpPipeliningEnabled && e instanceof OrderedUpstreamMessageEvent) {
            oue = (OrderedUpstreamMessageEvent) e;
            request = (HttpRequest) oue.getMessage();
        } else {
            request = (HttpRequest) e.getMessage();
        }

        // the netty HTTP handling always copy over the buffer to its own buffer, either in NioWorker internally
        // when reading, or using a cumalation buffer
        NettyHttpRequest httpRequest = new NettyHttpRequest(request, e.getChannel());
        if (oue != null) {
            serverTransport.dispatchRequest(httpRequest,
                new NettyHttpChannel(serverTransport, httpRequest, oue, detailedErrorsEnabled));
        } else {
            serverTransport.dispatchRequest(httpRequest,
                new NettyHttpChannel(serverTransport, httpRequest, detailedErrorsEnabled));
        }
        super.messageReceived(ctx, e);

        long cost = System.currentTimeMillis() - start;
        if (cost > 1000) {
            LOGGER.warn("http messageReceived cost too long, cost={}, uri={}", cost, httpRequest.uri());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        serverTransport.exceptionCaught(ctx, e);
    }
}
