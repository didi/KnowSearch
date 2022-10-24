package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.metrics;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESDataTempBean;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESNodeToIndexTempBean;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;

/**
 * @author didi
 * @date 2022/09/13
 */
public class MetricsRegister {
    /**
     * 缓存普通的采集自es 发送给odin的数据
     */
    private final Cache<String, ESDataTempBean>        dataBeanRegisterCache  = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES).initialCapacity(10000).build();

    /**
     * 缓存采集自一个node上的索引的指标数据
     */
    private final Cache<String, ESNodeToIndexTempBean> nodeIndexRegisterCache = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES).initialCapacity(10000).build();

    /**
     * 缓存需要复合计算的值
     */
    private Map<String, Double>                computeValueRegister;

    private Map<String, Tuple<Long, Double>>   clusterNodeCpu;

    public MetricsRegister() {
        computeValueRegister = Maps.newConcurrentMap();
        clusterNodeCpu = Maps.newConcurrentMap();
    }

    public void putBeforeNodeToIndexData(String key, ESNodeToIndexTempBean data) {
        nodeIndexRegisterCache.put(key, data);
    }

    public ESNodeToIndexTempBean getBeforeNodeToIndexData(String key) {
        return nodeIndexRegisterCache.getIfPresent(key);
    }

    public void putBeforeEsData(String key, ESDataTempBean data) {
        dataBeanRegisterCache.put(key, data);
    }

    public ESDataTempBean getBeforeEsData(String key) {
        return dataBeanRegisterCache.getIfPresent(key);
    }

    public void putBeforeComputeData(String key, Double data) {
        computeValueRegister.put(key, data);
    }

    public Double getBeforeComputeData(String key) {
        return computeValueRegister.get(key);
    }

    public void clearComputeValueRegister() {
        computeValueRegister.clear();
    }

    public void putNodeCpu(String ip, Tuple<Long, Double> cpu) {
        clusterNodeCpu.put(ip, cpu);
    }

    public Tuple<Long, Double> getNodeCpu(String ip) {
        return clusterNodeCpu.get(ip);
    }
}
