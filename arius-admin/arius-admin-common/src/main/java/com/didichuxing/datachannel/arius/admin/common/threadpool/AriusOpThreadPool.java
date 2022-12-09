package com.didichuxing.datachannel.arius.admin.common.threadpool;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.observability.Observability;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.*;

/**
 * 异步操作线程池
 */
@Component
@NoArgsConstructor
public class AriusOpThreadPool implements Executor {

    private static final String THREAD_FACTORY_NAME = "OP-POOL";

    private static final ILog   LOG                 = LogFactory.getLog(AriusOpThreadPool.class);

    @Value("${admin.thread.size.op:20}")
    public int                  poolSize;

    private ExecutorService     pool;

    private ThreadFactory       springThreadFactory = new CustomizableThreadFactory("AriusOpThreadPool");

    @PostConstruct
    public void init() {
        LOG.info("class=AriusOpThreadPool||method=init||AriusOpThreadPool init start..");
        pool = Observability.wrap(new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), springThreadFactory));
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                pool.shutdown();
                try {
                    if (!pool.awaitTermination(20, TimeUnit.SECONDS)) {
                        LOG.warn(
                            "class=AriusOpThreadPool||method=init||errMsg=still some task running, force to shutdown!");
                        List<Runnable> shutDownList = pool.shutdownNow();
                        shutDownList.forEach(
                            e -> LOG.info("class=AriusOpThreadPool||method=init||msg=Runnable forced shutdown"));
                    }
                } catch (InterruptedException e) {
                    pool.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                LOG.info("class=AriusOpThreadPool||method=init||{}shutdown finished", THREAD_FACTORY_NAME);
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

    public void run(Runnable runner) {
        pool.execute(runner);
    }

    @Override
    public void execute(Runnable command) {
        pool.execute(command);
    }
}
