package com.didi.cloud.fastdump.common.threadpool;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FutureUtil<T> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(FutureUtil.class);

    private ThreadPoolExecutor                            executor;
    private String                                        name;

    private Map<Long/*currentThreadId*/, List<Future<T>>> futuresMap;

    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public void setFuturesMap(Map<Long, List<Future<T>>> futuresMap) {
        this.futuresMap = futuresMap;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static <T> FutureUtil<T> init(String name, int corePoolSize, int maxPoolSize, int queueSize) {
        FutureUtil<T> futureUtil = new FutureUtil<>();

        ThreadPoolExecutor exe = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 3000, TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>(queueSize), new NamedThreadFactory("Fast-dump-FutureUtil-" + name),
            new ThreadPoolExecutor.AbortPolicy());// 抛出异常

        futureUtil.setExecutor(exe);
        futureUtil.setName(name);
        futureUtil.setFuturesMap(new ConcurrentHashMap<>());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            exe.shutdown();
            try {
                if (!exe.awaitTermination(20, TimeUnit.SECONDS)) {
                    LOGGER.warn("class=FutureUtil||method=init||msg=still some task running, force to shutdown!");
                    List<Runnable> shutDownList = exe.shutdownNow();
                    shutDownList.forEach(
                            e -> LOGGER.info("class=FutureUtil||method=init||msg=Runnable forced shutdown||class={}", e.getClass()));
                }
            } catch (InterruptedException e) {
                exe.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOGGER.info("class=FutureUtil||method=init||msg={} shutdown finished", name);
        }));
        return futureUtil;
    }

    /**
     * 根据系统处理器的数目创建线程池
     *
     * @param name
     * @param queueSize
     * @param <T>
     * @return
     */
    public static <T> FutureUtil<T> initBySystemAvailableProcessors(String name, int queueSize) {
        int processorsNum = Runtime.getRuntime().availableProcessors();
        if (processorsNum >= 3) {
            return init(name, processorsNum, processorsNum, queueSize);
        }
        return init(name, 3, 3, queueSize);
    }

    /**
     * 必须配合 waitResult使用 否则容易会撑爆内存
     * @param callable
     * @return
     */
    public FutureUtil<T> callableTask(Callable<T> callable) {
        Long currentThreadId = Thread.currentThread().getId();

        List<Future<T>> futures = futuresMap.get(currentThreadId);
        if (CollectionUtils.isEmpty(futures)) {
            futures = Lists.newCopyOnWriteArrayList();
        }

        futures.add(executor.submit(callable));
        futuresMap.put(currentThreadId, futures);
        return this;
    }

    /**
     * 必须配合 waitExecute使用 否则容易会撑爆内存
     * @param runnable
     * @return
     */
    public FutureUtil<T> runnableTask(Runnable runnable) {
        Long currentThreadId = Thread.currentThread().getId();

        List<Future<T>> futures = futuresMap.get(currentThreadId);
        if (CollectionUtils.isEmpty(futures)) {
            futures = Lists.newCopyOnWriteArrayList();
        }

        futures.add((Future<T>) executor.submit(runnable));
        futuresMap.put(currentThreadId, futures);
        return this;
    }

    public void waitExecute(long timeOutSeconds) {
        Long currentThreadId = Thread.currentThread().getId();

        List<Future<T>> currentFutures = futuresMap.get(currentThreadId);

        if (CollectionUtils.isEmpty(currentFutures)) {
            return;
        }

        //CopyOnWriteArrayList 不支持迭代器进行remove 会抛出java.lang.UnsupportedOperationException
        for (Future<T> f : currentFutures) {
            try {
                f.get(timeOutSeconds, TimeUnit.SECONDS);
            } catch (Exception e) {
                f.cancel(true);
                LOGGER.error("class=FutureUtil||method=waitExecute||msg=exception", e);
            } finally {
                currentFutures.remove(f);
            }
        }

        if (CollectionUtils.isEmpty(currentFutures)) {
            futuresMap.remove(currentThreadId);
        }
    }

    /**
     * 带有错误容忍的waitExecute，当连续有超过continuousFailThres 个任务失败时
     * 则认为这一堆任务不稳定，终止并清空
     * @param timeOutSeconds 单个任务超时上限
     * @param continuousFailThres 连续失败任务数量上限
     */
    public void waitExecuteWithErrorTolerance(long timeOutSeconds, int continuousFailThres) {
        Long currentThreadId = Thread.currentThread().getId();
        List<Future<T>> currentFutures = futuresMap.get(currentThreadId);
        boolean lastFail = false;
        int failCnt = 0;
        if (CollectionUtils.isEmpty(currentFutures)) {
            return;
        }
        for (Future<T> f : currentFutures) {
            try {
                if (lastFail && failCnt >= continuousFailThres) {
                    throw new Exception("exceed error tolerance upper bound");
                }
                f.get(timeOutSeconds, TimeUnit.SECONDS);
                lastFail = false;
                failCnt = 0;
            } catch (Exception e) {
                lastFail = true;
                failCnt += 1;
                f.cancel(true);
                LOGGER.error("class=FutureUtil||method=waitExecute||msg=exception", e);
            } finally {
                currentFutures.remove(f);
            }
        }

        if (CollectionUtils.isEmpty(currentFutures)) {
            futuresMap.remove(currentThreadId);
        }
    }

    public void waitExecute() {
        waitExecute(30);
    }

    public void waitExecuteUnlimited() {
        Long currentThreadId = Thread.currentThread().getId();

        List<Future<T>> currentFutures = futuresMap.get(currentThreadId);

        if (CollectionUtils.isEmpty(currentFutures)) {
            return;
        }

        //CopyOnWriteArrayList 不支持迭代器进行remove 会抛出java.lang.UnsupportedOperationException
        for (Future<T> f : currentFutures) {
            try {
                f.get();
            } catch (Exception e) {
                f.cancel(true);
                LOGGER.error("class=FutureUtil||method=waitExecute||msg=exception", e);
            } finally {
                currentFutures.remove(f);
            }
        }

        currentFutures.clear();
        if (CollectionUtils.isEmpty(currentFutures)) {
            futuresMap.remove(currentThreadId);
        }
    }

    public List<T> waitResult(long timeOutSeconds) {
        Long currentThreadId = Thread.currentThread().getId();

        List<Future<T>> currentFutures = futuresMap.get(currentThreadId);

        List<T> list = Lists.newCopyOnWriteArrayList();

        if (CollectionUtils.isEmpty(currentFutures)) {
            return list;
        }

        for (Future<T> f : currentFutures) {
            try {
                T t = f.get(timeOutSeconds, TimeUnit.SECONDS);
                if (t != null) {
                    list.add(t);
                }
            } catch (Exception e) {
                f.cancel(true);
                LOGGER.error("class=FutureUtil||method=waitResult||msg=exception", e);
            } finally {
                currentFutures.remove(f);
            }
        }

        if (CollectionUtils.isEmpty(currentFutures)) {
            futuresMap.remove(currentThreadId);
        }

        return list;
    }

    public void shutdownNow() {
        executor.shutdownNow();
    }

    public void shutdown() {
        executor.shutdown();
    }

    public List<T> waitResult() {
        return waitResult(30);
    }
}
