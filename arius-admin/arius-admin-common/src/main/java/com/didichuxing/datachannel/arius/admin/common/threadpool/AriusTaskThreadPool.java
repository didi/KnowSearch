package com.didichuxing.datachannel.arius.admin.common.threadpool;

import java.util.List;
import java.util.concurrent.*;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.observability.Observability;
import lombok.NoArgsConstructor;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

/**
 * admin执行任务的线程池
 */
@NoArgsConstructor
public class AriusTaskThreadPool {

    private static final String THREAD_FACTORY_NAME = "TASK-POOL";

    private static final ILog   LOGGER              = LogFactory.getLog(AriusTaskThreadPool.class);

    private int                 poolSize;

    private ExecutorService     pool;

    private ThreadFactory       springThreadFactory = new CustomizableThreadFactory("AriusTaskThreadPool");

    public void init(int poolSize, String taskName, int queueLen) {
        this.poolSize = poolSize;

        LOGGER.warn("class=AriusTaskThreadPool||method=init||poolSize={}||taskName={}", poolSize, taskName);

        // 这里注意需要 LinkedBlockingQueue 的容量大小设置，默认为接近无限大,
        // 随着worker数量积增，会导致内存压力持续上升，若部署在容器中, 服务会被容器主动kill掉
        pool = Observability.wrap(new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueLen), springThreadFactory));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(20, TimeUnit.SECONDS)) {
                    LOGGER.warn(
                        "class=AriusTaskThreadPool||method=init||msg=still some task running, force to shutdown!");
                    List<Runnable> shutDownList = pool.shutdownNow();
                    shutDownList.forEach(e -> LOGGER
                        .info("class=AriusTaskThreadPool||method=init||msg=Runnable forced shutdown||class={}"));
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOGGER.info("class=AriusTaskThreadPool||method=init||msg={} shutdown finished", THREAD_FACTORY_NAME);
        }));

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

    public int getCurrentQueueWorkerSize() {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) pool;
        return threadPoolExecutor.getQueue().size();
    }
}
