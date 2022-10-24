package com.didichuxing.datachannel.arius.admin.common.threadpool;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AriusScheduleThreadPool {

    private ScheduledExecutorService scheduleThreadPool;
    private int                      scheduleThreadNum = 1 << 5;

    @PostConstruct
    public void init() {
        scheduleThreadPool = new ScheduledThreadPoolExecutor(scheduleThreadNum,
            new DesmondThreadFactory("scheduleThreadPool"));
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