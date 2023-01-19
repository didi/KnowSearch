package com.didi.arius.gateway.core.component.log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.observability.Observability;

/**
 * @author didi
 * @date 2021-09-17 5:22 下午
 */
public abstract class AbstractAggLogManager {

    protected static final ILog bootLogger = LogFactory.getLog(QueryConsts.BOOT_LOGGER);

    protected ScheduledExecutorService scheduleThreadPool;

    /**
     * 定时线程池启动，以便后续其他日志聚合操作
     *
     * @param threadSize 池大小
     * @param runnable   处理类
     * @param interval   处理间隔
     */
    public void init(int threadSize, Runnable runnable, int interval) {
        scheduleThreadPool = Observability.wrap(Executors.newScheduledThreadPool(threadSize));
        scheduleThreadPool.scheduleWithFixedDelay(runnable, interval, interval, TimeUnit.MINUTES);
    }
}
