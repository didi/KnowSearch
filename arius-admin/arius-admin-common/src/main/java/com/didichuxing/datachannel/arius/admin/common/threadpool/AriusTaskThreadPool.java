package com.didichuxing.datachannel.arius.admin.common.threadpool;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import lombok.NoArgsConstructor;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * admin执行任务的线程池
 */
@NoArgsConstructor
public class AriusTaskThreadPool {

    private static final String THREAD_FACTORY_NAME = "TASK-POOL";

    private static final ILog   LOGGER              = LogFactory.getLog(AriusTaskThreadPool.class);

    private int                 poolSize;

    private ExecutorService     pool;

    private ThreadFactory springThreadFactory = new CustomizableThreadFactory("AriusTaskThreadPool");

    public void init(int poolSize, String taskName) {
        this.poolSize   = poolSize;

        LOGGER.warn("class=AriusTaskThreadPool||method=init||poolSize={}||taskName={}", poolSize, taskName);

        pool = Executors.newFixedThreadPool(poolSize, springThreadFactory);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(20, TimeUnit.SECONDS)) {
                    LOGGER.warn("class=AriusTaskThreadPool||method=init||msg=still some task running, force to shutdown!");
                    List<Runnable> shutDownList = pool.shutdownNow();
                    shutDownList
                        .forEach(e -> LOGGER.info("class=AriusTaskThreadPool||method=init||msg=Runnable forced shutdown||class={}"));
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOGGER.info("class=AriusTaskThreadPool||method=init||msg={} shutdown finished", THREAD_FACTORY_NAME);
        } ) );

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
