package com.didi.cloud.fastdump.common.threadpool;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import lombok.NoArgsConstructor;

/**
 * fast-dump 任务的线程池
 */
@NoArgsConstructor
public class TaskThreadPool {
    protected static final Logger LOGGER              = LoggerFactory.getLogger(TaskThreadPool.class);

    private ExecutorService       pool;

    private int                   poolSize;

    private ThreadFactory         threadFactory;

    public void init(int corePoolSize, int maximumPoolSize, String threadPoolName, int queueLen) {
        poolSize = maximumPoolSize;
        threadFactory = new NamedThreadFactory(threadPoolName);
        LOGGER.info("class=TaskThreadPool||method=init||poolSize={}||taskName={}", corePoolSize, threadPoolName);

        // 这里注意需要 LinkedBlockingQueue 的容量大小设置，默认为接近无限大,
        // 随着worker数量积增，会导致内存压力持续上升，若部署在容器中, 服务会被容器主动kill掉
        pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueLen), threadFactory, /*直接抛出异常*/new ThreadPoolExecutor.AbortPolicy());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(20, TimeUnit.SECONDS)) {
                    LOGGER.warn("class=TaskThreadPool||method=init||msg=still some task running, force to shutdown!");
                    List<Runnable> shutDownList = pool.shutdownNow();
                    shutDownList.forEach(
                        e -> LOGGER.info("class=TaskThreadPool||method=init||msg=Runnable forced shutdown||class={}", e.getClass()));
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOGGER.info("class=TaskThreadPool||method=init||msg={} shutdown finished", threadPoolName);
        }));

        LOGGER.info("class=TaskThreadPool||method=init||TaskThreadPool init finished.");
    }

    public void run(Runnable runner) {
        pool.execute(runner);
    }

    public <T> T submit(Callable<T> caller) throws InterruptedException, ExecutionException {
        Future<T> future = pool.submit(caller);
        return future.get();
    }

    public int getPoolSize() {
        return this.poolSize;
    }

    public int getCurrentQueueWorkerSize() {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) pool;
        return threadPoolExecutor.getQueue().size();
    }

    /**
     * 判断线程池是否空闲
     * @return
     */
    public boolean isIdleThreadPool() {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) pool;
        return 0 == threadPoolExecutor.getActiveCount();
    }
}
