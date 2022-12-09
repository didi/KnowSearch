package com.didi.arius.gateway.rest.tcp;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.observability.Observability;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.monitor.jvm.JvmInfo;
import org.elasticsearch.transport.netty.SizeHeaderFrameDecoder;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
* @author weizijun
* @date：2016年9月12日
* 
*/
@Component("nettyTransport")
public class NettyTransport {
	private static final ILog logger = LogFactory.getLog(QueryConsts.BOOT_LOGGER);
	
	@Value("${gateway.nettyTransport.port}")
	private short port;

	@Value("${gateway.nettyTransport.bossThreadCount}")
	private int bossThreadCount;

	@Value("${gateway.nettyTransport.workerCount}")
	private int workerCount;

	@Value("${gateway.nettyTransport.tcpNoDelay}")
	private boolean tcpNoDelay;

	@Value("${gateway.nettyTransport.keepAlive}")
	private boolean keepAlive;
	
	protected static final boolean COMPRESS = false;
	protected static final BigArrays bigArrays = BigArrays.NON_RECYCLING_INSTANCE;
	
    @Autowired
    private MessageChannelHandler messageChannelHandler;

	public NettyTransport() {
		// pass
	}

	public void init() {
		OpenChannelsHandler serverOpenChannels = new OpenChannelsHandler(logger);

		ExecutorService bossThreadPool = Observability.wrap(Executors
				.newCachedThreadPool(new DeamondThreadFactory(
						"boss")));
		ExecutorService workerThreadPool = Observability.wrap(Executors
				.newCachedThreadPool(new DeamondThreadFactory(
						"worker")));
		NioServerSocketChannelFactory serverNioFactory = new NioServerSocketChannelFactory(bossThreadPool,
				bossThreadCount, workerThreadPool,
				workerCount);

		ServerBootstrap serverBootstrap = new ServerBootstrap(serverNioFactory);
        serverBootstrap.setPipelineFactory(() -> {
			ChannelPipeline channelPipeline = Channels.pipeline();
			channelPipeline.addLast("openChannels", serverOpenChannels);
			SizeHeaderFrameDecoder sizeHeader = new SizeHeaderFrameDecoder();
			channelPipeline.addLast("size", sizeHeader);
			channelPipeline.addLast("dispatcher", messageChannelHandler);
			return channelPipeline;
		});
        
        serverBootstrap.setOption("child.tcpNoDelay", tcpNoDelay);
        serverBootstrap.setOption("child.keepAlive", keepAlive);

        long defaultReceiverPredictor = 512 * 1024L;
        if (JvmInfo.jvmInfo().getMem().getDirectMemoryMax().bytes() > 0) {
            // we can guess a better default...
            long l = (long) ((0.3 * JvmInfo.jvmInfo().getMem().getDirectMemoryMax().bytes()) / workerCount);
            defaultReceiverPredictor = Math.min(defaultReceiverPredictor, Math.max(l, 64 * 1024L));
        }
        
        // See AdaptiveReceiveBufferSizePredictor#DEFAULT_XXX for default values in netty..., we can use higher ones for us, even fixed one
		ByteSizeValue receivePredictorMin = new ByteSizeValue(defaultReceiverPredictor);
		ByteSizeValue receivePredictorMax = new ByteSizeValue(defaultReceiverPredictor);

		ReceiveBufferSizePredictorFactory receiveBufferSizePredictorFactory;

        if (receivePredictorMax.bytes() == receivePredictorMin.bytes()) {
            receiveBufferSizePredictorFactory = new FixedReceiveBufferSizePredictorFactory((int) receivePredictorMax.bytes());
        } else {
            receiveBufferSizePredictorFactory = new AdaptiveReceiveBufferSizePredictorFactory((int) receivePredictorMin.bytes(), (int) receivePredictorMin.bytes(), (int) receivePredictorMax.bytes());
        }
        
        boolean reuseAddress = NetworkUtils.defaultReuseAddress();
        
        serverBootstrap.setOption("receiveBufferSizePredictorFactory", receiveBufferSizePredictorFactory);
        serverBootstrap.setOption("child.receiveBufferSizePredictorFactory", receiveBufferSizePredictorFactory);
        serverBootstrap.setOption("reuseAddress", reuseAddress);
        serverBootstrap.setOption("child.reuseAddress", reuseAddress);

		try {
			// bind port
			serverBootstrap.bind(new InetSocketAddress(getPort()));
		} catch (ChannelException e) {
			logger.error("tcp port bind exception", e);
			System.exit(1);
		}

		logger.info("nettyTransport init done");
	}
	

    TransportAddress wrapAddress(SocketAddress socketAddress) {
        return new InetSocketTransportAddress((InetSocketAddress) socketAddress);
    }

	public short getPort() {
		return port;
	}
}


class DeamondThreadFactory implements ThreadFactory {
	static final AtomicInteger poolNumber = new AtomicInteger(1);
	final ThreadGroup group;
	final AtomicInteger threadNumber = new AtomicInteger(1);
	final String namePrefix;

	static {
		ThreadRenamingRunnable
				.setThreadNameDeterminer((currentThreadName, proposedThreadName) -> currentThreadName);
	}

	public DeamondThreadFactory(String prefix) {
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
				.getThreadGroup();
		namePrefix = prefix + "-pool-" + poolNumber.getAndIncrement() + "-";
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, namePrefix
				+ threadNumber.getAndIncrement(), 0);
		if (!t.isDaemon())
			t.setDaemon(true);
		if (t.getPriority() != Thread.NORM_PRIORITY)
			t.setPriority(Thread.NORM_PRIORITY);
		return t;
	}
}