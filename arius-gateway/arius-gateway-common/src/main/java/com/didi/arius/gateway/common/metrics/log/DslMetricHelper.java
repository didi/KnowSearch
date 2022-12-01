package com.didi.arius.gateway.common.metrics.log;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.consts.QueryConsts;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.util.concurrent.ConcurrentCollections;

/**
 * @author didi
 * @date 2021-09-16 10:44 下午
 */
public class DslMetricHelper {

    private DslMetricHelper() {
    }

    private static final ConcurrentMap<String, KeyLock> map = ConcurrentCollections.newConcurrentMapWithAggressiveConcurrency();

    protected static final ILog bootLogger = LogFactory.getLog(QueryConsts.BOOT_LOGGER);

    //这个限制是个大概值，map大小在指定值左右值默认1000
    private static int dslMapSize;
    private static int threadSize;
    private static int queueSize;

    private static final String ERROR = "error";

    private static Map<String, DslLogEntity> dslLogMap = new ConcurrentHashMap<>();
    private static ExecutorService es;

    public static void init(int dslMapSize, int threadSize, int queueSize) {
        DslMetricHelper.dslMapSize = dslMapSize;
        DslMetricHelper.threadSize = threadSize;
        DslMetricHelper.queueSize = queueSize;
        es = newFixedThreadPool();
    }

    public static void putDslLog(String logString) {
        DslLogEntity dslLogEntity = JSON.parseObject(logString, DslLogEntity.class);
        if (!filter(dslLogEntity)) {
            es.submit(() -> {
                String key = String.format("%d_%s", dslLogEntity.getProjectId(), dslLogEntity.getDslTemplateMd5());
                dslLogEntity.setProjectIdDslTemplateMd5(key);

                //这里由于涉及到累加所以得加锁，但是全局加锁会导致性能低，所以这里采用对单个key加锁，不同key互不影响
                try {
                    KeyLock lock;
                    while (true) {
                        KeyLock perNodeLock = map.get(key);
                        if (perNodeLock == null) {
                            KeyLock newLock = new KeyLock(false);
                            newLock.lock();
                            KeyLock keyLock = map.putIfAbsent(key, newLock);
                            if (keyLock == null) {
                                lock = newLock;
                                break;
                            }
                        } else {
                            assert perNodeLock != null;
                            int i = perNodeLock.count.get();
                            if (i > 0 && perNodeLock.count.compareAndSet(i, i + 1)) {
                                perNodeLock.lock();
                                lock = perNodeLock;
                                break;
                            }
                        }
                    }

                    DslLogEntity value = dslLogMap.get(key);
                    if (null == value) {
                        value = dslLogEntity;
                        dslLogMap.put(key, value);
                    } else {
                        calculateTotal(dslLogEntity, value);
                    }

                    //释放锁
                    int decrementAndGet = lock.count.decrementAndGet();
                    lock.unlock();
                    if (decrementAndGet == 0) {
                        map.remove(key, lock);
                    }
                } catch (Exception e) {
                    bootLogger.warn("deal dsl log error", e);
                }
            });

        }
    }

    private static boolean filter(DslLogEntity dslLogEntity) {
        //空模板过滤
        if (Strings.isEmpty(dslLogEntity.getDslTemplateMd5()) ||
                Strings.isEmpty(dslLogEntity.getDslTemplate())) {
            return true;
        }

        if (!dslLogEntity.isQueryRequest()) {
            return true;
        }

        //error日志过滤
        if (!Strings.isEmpty(dslLogEntity.getAriusType()) && dslLogEntity.getAriusType().equals(ERROR)) {
            return true;
        }

        if (!dslLogMap.containsKey(String.format("%d_%s",
                dslLogEntity.getProjectId(), dslLogEntity.getDslTemplateMd5())) &&
                dslLogMap.size() >= dslMapSize) {
            return true;
        }

        return false;
    }


    private static ExecutorService newFixedThreadPool() {
        return new ThreadPoolExecutor(threadSize, threadSize, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueSize), (runnable, threadPoolExecutor) ->
                //拒绝了就直接丢弃
                bootLogger.warn("deal log busy so discard log"));
    }

    public static void resetMap() {
        dslLogMap = new ConcurrentHashMap<>();
    }

    public static Map<String, DslLogEntity> getDslLogMap() {
        return dslLogMap;
    }

    private static void calculateTotal(DslLogEntity dslLogEntity, DslLogEntity value) {
        value.setSearchCount(value.getSearchCount() + dslLogEntity.getSearchCount());
        value.setDslLen(value.getDslLen() + dslLogEntity.getDslLen());
        value.setResponseLen(value.getResponseLen() + dslLogEntity.getResponseLen());
        value.setBeforeCost(value.getBeforeCost() + dslLogEntity.getBeforeCost());
        value.setEsCost(value.getEsCost() + dslLogEntity.getEsCost());
        value.setTotalCost(value.getTotalCost() + dslLogEntity.getTotalCost());
        value.setTotalShards(value.getTotalShards() + dslLogEntity.getTotalShards());
        value.setTotalHits(value.getTotalHits() + dslLogEntity.getTotalHits());
        value.setFailedShards(value.getFailedShards() + value.getFailedShards());
    }

    private static final class KeyLock extends ReentrantLock {
        KeyLock(boolean fair) {
            super(fair);
        }

        private final AtomicInteger count = new AtomicInteger(1);
    }
}