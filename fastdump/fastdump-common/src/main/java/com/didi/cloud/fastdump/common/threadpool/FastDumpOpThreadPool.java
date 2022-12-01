package com.didi.cloud.fastdump.common.threadpool;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;

/**
 * 异步操作线程池, 异步执行事件
 */
@Component
@NoArgsConstructor
public class FastDumpOpThreadPool implements Executor {

    private static final String   THREAD_FACTORY_NAME = "FAST-DUMP-OP-POOL";

    protected static final Logger LOGGER              = LoggerFactory.getLogger(FastDumpOpThreadPool.class);

    @Value("${admin.thread.size.op:30}")
    public int                    poolSize;

    private ExecutorService       pool;

    private ThreadFactory         springThreadFactory = new NamedThreadFactory("FastDumpOpThreadPool");

    @PostConstruct
    public void init() {
        LOGGER.info("class=FastDumpOpThreadPool||method=init||FastDumpOpThreadPool init start..");
        pool = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1000), springThreadFactory);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(20, TimeUnit.SECONDS)) {
                    LOGGER.warn(
                        "class=FastDumpOpThreadPool||method=init||errMsg=still some task running, force to shutdown!");
                    List<Runnable> shutDownList = pool.shutdownNow();
                    shutDownList.forEach(
                        e -> LOGGER.info("class=FastDumpOpThreadPool||method=init||msg=Runnable forced shutdown"));
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOGGER.info("class=FastDumpOpThreadPool||method=init||{} shutdown finished", THREAD_FACTORY_NAME);
        }));
        LOGGER.info("class=FastDumpOpThreadPool||method=init||FastDumpOpThreadPool init finished.");
    }

    public <T> T submit(Callable<T> caller) throws InterruptedException, ExecutionException {
        Future<T> future = pool.submit(caller);
        return future.get();
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void run(Runnable runner) {
        pool.execute(runner);
    }

    @Override
    public void execute(Runnable command) {
        pool.execute(command);
    }
}
