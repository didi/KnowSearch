package com.didi.cloud.fastdump.rest.http;

import org.jboss.netty.util.ThreadRenamingRunnable;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by linyunan on 2022/8/4
 */
public class HttpDeamondThreadFactory implements ThreadFactory {
    static final AtomicInteger poolNumber   = new AtomicInteger(1);
    final AtomicInteger        threadNumber = new AtomicInteger(1);
    final String               namePrefix;
    final ThreadGroup          group;

    static {
        ThreadRenamingRunnable.setThreadNameDeterminer((currentThreadName, proposedThreadName) -> currentThreadName);
    }

    public HttpDeamondThreadFactory(String prefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = prefix + "-pool-" + poolNumber.getAndIncrement() + "-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (!t.isDaemon())
            t.setDaemon(true);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}