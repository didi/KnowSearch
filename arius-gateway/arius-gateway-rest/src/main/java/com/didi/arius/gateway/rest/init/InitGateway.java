package com.didi.arius.gateway.rest.init;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.rest.http.NettyHttpServerTransport;
import com.didi.arius.gateway.rest.tcp.NettyTransport;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("initGateway")
public class InitGateway {
	protected static final ILog bootLogger = LogFactory.getLog(QueryConsts.BOOT_LOGGER);

	@Autowired
	private NettyTransport nettyTransport;

	@Autowired
	private NettyHttpServerTransport nettyHttpServerTransport;

	public InitGateway() {
		// pass
	}

	@PostConstruct
	public void init() {
		//启动tcp server
		nettyTransport.init();
		//启动http server
		nettyHttpServerTransport.init();
		bootLogger.info("nettyTransport init done");
	}
}
