package com.didi.cloud.fastdump.rest.bootstrap;

import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.content.Version;
import com.didi.cloud.fastdump.rest.http.NettyHttpServerTransport;

/**
 * Created by linyunan on 2022/8/2
 */
@Component
public class Bootstrap {
    private static final Logger      LOGGER         = LoggerFactory.getLogger(Bootstrap.class);
    private final CountDownLatch     keepAliveLatch = new CountDownLatch(1);
    private final Thread             keepAliveThread;
    @Autowired
    private NettyHttpServerTransport nettyHttpServerTransport;

    Bootstrap() {
        keepAliveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    keepAliveLatch.await();
                } catch (InterruptedException e) {
                    // bail out
                }
            }
        }, "fast-dump[keepAlive/" + Version.CURRENT + "]");
        keepAliveThread.setDaemon(false);
        // keep this thread alive (non daemon thread) until we shutdown
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                keepAliveLatch.countDown();
            }
        });
    }

    @PostConstruct
    public void init() {
        try {
            nettyHttpServerTransport.init();
        } catch (Exception e) {
            LOGGER.error("FastDump HttpNettyServer failed to run , errMsg: {}", e.getCause(), e);
            return;
        }
        keepAliveThread.start();
        LOGGER.info("FastDump has been bound http(netty) port: {}", nettyHttpServerTransport.getHttpPort());
    }
}
