package com.didichuxing.datachannel.arius.admin.common.threadpool;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

/**
 *
 * 异步操作线程池
 *
 * @author d06679
 */
@Component
public class AriusOpThreadPool implements Executor {

    private static final String THREAD_FACTORY_NAME = "OP-POOL";

    private static final ILog   LOG                 = LogFactory.getLog(AriusOpThreadPool.class);

    @Value("${admin.thread.size.op:20}")
    public int                  poolSize;

    private ExecutorService     pool;

    private ThreadFactory springThreadFactory = new CustomizableThreadFactory("AriusOpThreadPool");

    @PostConstruct
    public void init() {
        LOG.info("class=AriusOpThreadPool||method=init||AriusOpThreadPool init start.");
        pool = Executors.newFixedThreadPool(poolSize, springThreadFactory);
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                pool.shutdown();
                try {
                    if (pool.awaitTermination(20, TimeUnit.SECONDS) == false) {
                        LOG.warn("still some task running, force to shutdown!");
                        List<Runnable> shutDownList = pool.shutdownNow();
                        shutDownList
                            .forEach(e -> LOG.info("Runnable forced shutdown||class={}", e.getClass().getName()));
                    }
                } catch (InterruptedException e) {
                    pool.shutdownNow();
                    e.printStackTrace();
                }
                LOG.info(THREAD_FACTORY_NAME + " shutdown finished");
            }
        });
        LOG.info("class=AriusOpThreadPool||method=init||AriusOpThreadPool init finished.");
    }

    public <T> T submit(Callable<T> caller) throws InterruptedException, ExecutionException {
        Future<T> future = pool.submit(caller);
        return future.get();
    }

    public int getPoolSize() {
        return poolSize;
    }

    @Override
    public void execute(Runnable command) {
        pool.execute(command);
    }
}
