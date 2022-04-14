package com.didi.arius.gateway.core.service;

/**
 * @author fitz
 * @date 2021/5/31 12:04 下午
 */
public interface MetricsService {
    /**
     * 统计appid query cost
     * @param appid
     * @param cost
     */
    void addQueryCost(int appid, long cost);

    /**
     * 统计slowlog cost
     * @param appid
     * @param cost
     */
    void addSlowlogCost(int appid, long cost);

    /**
     * 统计查询返回的指标计数
     * @param appid
     * @param tookInMillis
     * @param totalHits
     * @param totalShards
     * @param failedShards
     */
    void addSearchResponseMetrics(int appid, long tookInMillis, long totalHits, int totalShards, int failedShards);

    /**
     *
     * 统计requestLength
     * @param appid
     * @param length
     */
    void addReqeustLength(int appid, long length);

    /**
     *
     * 统计responseLength
     * @param appid
     * @param length
     */
    void addResponseLength(int appid, long length);

    /**
     *
     * 统计QueryMetrics requestLength, responseLength
     * @param appid
     * @param cost
     * @param requestLength
     * @param responseLength
     */
    void addQueryMetrics(int appid, long cost, long requestLength, long responseLength);

    /**
     *
     * IndexMetrics requestLength, responseLength
     * @param index
     * @param operation
     * @param cost
     * @param requestLength
     * @param responseLength
     */
    void addIndexMetrics(String index, String operation, long cost, long requestLength, long responseLength);

    /**
     * 统计聚合查询
     * @param appid
     */
    void incrQueryAggs(int appid);
}
