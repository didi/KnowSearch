package com.didi.arius.gateway.rest.init;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.rest.http.NettyHttpServerTransport;
import com.didi.arius.gateway.rest.tcp.NettyTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("initGateway")
public class InitGateway {
	protected static final Logger bootLogger = LoggerFactory.getLogger(QueryConsts.BOOT_LOGGER);

	@Autowired
	private NettyTransport nettyTransport;

	@Autowired
	private NettyHttpServerTransport nettyHttpServerTransport;


	@PostConstruct
	public void init() {
		//启动tcp server
		nettyTransport.init();
		//启动http server
		nettyHttpServerTransport.init();
		bootLogger.info("nettyTransport init done");
	}
}
