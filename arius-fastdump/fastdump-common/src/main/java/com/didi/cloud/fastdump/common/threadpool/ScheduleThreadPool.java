package com.didi.cloud.fastdump.common.threadpool;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ScheduleThreadPool {
    protected static final Logger    LOGGER              = LoggerFactory.getLogger(ScheduleThreadPool.class);
    private ScheduledExecutorService scheduleThreadPool;
    private static final int         SCHEDULE_THREAD_NUM = 1 << 5;

    @PostConstruct
    public void init() {
        scheduleThreadPool = new ScheduledThreadPoolExecutor(SCHEDULE_THREAD_NUM,
            new DesmondThreadFactory("scheduleThreadPool"));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduleThreadPool.shutdown();
            try {
                if (!scheduleThreadPool.awaitTermination(20, TimeUnit.SECONDS)) {
                    LOGGER.warn(
                            "class=ScheduleThreadPool||method=init||errMsg=still some task running, force to shutdown!");
                    List<Runnable> shutDownList = scheduleThreadPool.shutdownNow();
                    shutDownList.forEach(
                            e -> LOGGER.info("class=ScheduleThreadPool||method=init||msg=Runnable forced shutdown"));
                }
            } catch (InterruptedException e) {
                scheduleThreadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOGGER.info("class=ScheduleThreadPool||method=init||{} shutdown finished", "scheduleThreadPool");
        }));
        LOGGER.info("class=ScheduleThreadPool||method=init||ScheduleThreadPool init finished.");
    }

    public void submitScheduleAtFixedRateTask(Runnable runnable, long initialDelay, long period) {
        scheduleThreadPool.scheduleAtFixedRate(runnable, initialDelay, period, TimeUnit.SECONDS);
    }

    public void submitScheduleAtFixedDelayTask(Runnable runnable, long initialDelay, long delay) {
        scheduleThreadPool.scheduleWithFixedDelay(runnable, initialDelay, delay, TimeUnit.SECONDS);
    }

    static class DesmondThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber   = new AtomicInteger(1);
        final AtomicInteger        threadNumber = new AtomicInteger(1);
        final String               namePrefix;

        DesmondThreadFactory(String prefix) {
            namePrefix = prefix + "-pool-" + poolNumber.getAndIncrement() + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            if (!t.isDaemon()) {
                t.setDaemon(true);
            }

            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}