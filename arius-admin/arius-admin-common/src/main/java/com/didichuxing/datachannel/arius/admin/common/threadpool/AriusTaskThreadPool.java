package com.didichuxing.datachannel.arius.admin.common.threadpool;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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
 * admin执行任务的线程池
 *
 * @author d06679
 */
@Component
public class AriusTaskThreadPool {

    private static final String THREAD_FACTORY_NAME = "TASK-POOL";

    private static final ILog   LOGGER              = LogFactory.getLog(AriusTaskThreadPool.class);

    @Value("${admin.thread.size.task:20}")
    public int                  poolSize;

    private ExecutorService     pool;

    private ThreadFactory springThreadFactory = new CustomizableThreadFactory("AriusTaskThreadPool");

    @PostConstruct
    public void init() {
        LOGGER.info("class=AriusTaskThreadPool||method=init||AriusTaskThreadPool init start.");
        pool = Executors.newFixedThreadPool(poolSize, springThreadFactory);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                pool.shutdown();
                try {
                    if (!pool.awaitTermination(20, TimeUnit.SECONDS)) {
                        LOGGER.warn("still some task running, force to shutdown!");
                        List<Runnable> shutDownList = pool.shutdownNow();
                        shutDownList
                            .forEach(e -> LOGGER.info("Runnable forced shutdown||class={}", e.getClass().getName()));
                    }
                } catch (InterruptedException e) {
                    pool.shutdownNow();
                    e.printStackTrace();
                }
                LOGGER.info(THREAD_FACTORY_NAME + " shutdown finished");
            }
        });
        LOGGER.info("class=AriusTaskThreadPool||method=init||AriusTaskThreadPool init finished.");
    }

    public void run(Runnable runner) {
        pool.execute(runner);
    }

    public <T> T submit(Callable<T> caller) throws InterruptedException, ExecutionException {
        Future<T> future = pool.submit(caller);
        return future.get();
    }

    public int getPoolSize() {
        return poolSize;
    }
}
