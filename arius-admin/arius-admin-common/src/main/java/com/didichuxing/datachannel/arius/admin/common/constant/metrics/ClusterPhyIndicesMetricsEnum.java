package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * @author gyp
 * @date 2022/5/9
 * @version 1.0
 */
public enum ClusterPhyIndicesMetricsEnum {
    /*** 未知*/
    UNKNOWN("", "未知"),

/*************************************1.索引性能指标**************************************/
    /**
     * 单位：个
     */
    SHARDS_NUM("shardNu", "Shard数"),
    /**
     * 单位：GB
     */
    INDEX_STORE_SIZE("store-size_in_bytes", "索引大小"),
    /**
     * 单位：个
     */
    DOCS_NUM("docs-count", "文档总数"),
    /**
     * 单位：次/S
     */
    INDEXING_RATE("indexing-index_total_rate", "写入速率"),
    /**
     * 单位：ms
     */
    INDEXING_LATENCY("indexing-index_time_in_millis", "写入耗时"),
    /**
     * 单位：个/S
     */
    QUERY_RATE("search-query_total_rate", "查询Query速率"),
    /**
     * 单位：个/S
     */
    FETCH_RATE("search-fetch_total_rate", "查询Fetch速率"),
    /**
     * 单位：ms
     */
    QUERY_LATENCY("search-query_time_in_millis", "查询Query耗时"),
    /**
     * 单位：ms
     */
    FETCH_LATENCY("search-fetch_time_in_millis", "查询Fetch耗时"),
    /**
     * 单位：次/分钟
     */
    SCROLL_RATE("search-scroll_total_rate", "查询Scroll次数/分钟"),
    /**
     * 单位：ms
     */
    SCROLL_LATENCY("search-scroll_time_in_millis", "查询Scroll耗时"),
    /**
     * 单位：ms
     */
    MERGE_LATENCY("merges-total_time_in_millis", "Merge耗时"),
    /**
     * 单位：ms
     */
    REFRESH_LATENCY("refresh-total_time_in_millis", "Refresh耗时"),
    /**
     * 单位：ms
     */
    FLUSH_LATENCY("flush-total_time_in_millis", "Flush耗时"),
    /**
     * 单位：MB
     */
    QUERY_CACHE_SIZE("query_cache-memory_size_in_bytes", "query_cache大小"),
    /**
     * 单位：次/S
     */
    MERGE_RATE("merges-total_rate", "Merge次/S"),
    /**
     * 单位：次/S
     */
    REFRESH_RATE("refresh-total_rate", "Refresh次/S"),
    /**
     * 单位：次/S
     */
    FLUSH_RATE("flush-total_rate", "Flush次数/S"),
    /**
     * 单位：MB
     */
    SEGMENT_MEM_SIZE("segments-memory_in_bytes", "Segments大小"),
    /**
     * 单位：MB
     */
    TERMS_MEM_SIZE("segments-term_vectors_memory_in_bytes", "terms_memory大小"),
    /**
     * 单位：MB
     */
    POINT_MEM_SIZE("segments-points_memory_in_bytes", "points_memory大小"),
    /**
     * 单位：MB
     */
    DOC_VALUE_MEM_SIZE("segments-doc_values_memory_in_bytes",
            "doc_values_memory大小"),
    /**
     * 单位：MB
     */
    INDEX_WRITE_MEM_SIZE("segments-index_writer_memory_in_bytes",
            "index_writer_memory大小"),
    /**
     * 单位：MB
     */
    TRANSLOG_SIZE("translog-size_in_bytes", "translog大小"),
    /**
     * 单位：ms
     */
    INDEXING_TIME_PER_DOC("indexing-time_per_doc", "索引单次操作文档耗时"),
    /**
     * 单位：ms
     */
    MERGES_AVG_TIME("merges_avg_time", "单次merges操作耗时"),
    /**
     * 单位：ms
     */
    REFRESH_AVG_TIME("refresh_avg_time", "单次refresh操作耗时"),
    /**
     * 单位：ms
     */
    FLUSH_AVG_TIME("flush_avg_time", "单次flush操作耗时"),
    /**
     * 单位：MB
     */
    SEGMENTS_STORED_FIELDS_MEM_SIZE("segments-stored_fields_memory_in_bytes", "segments-stored_fields内存大小");


    ClusterPhyIndicesMetricsEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    private final String type;

    private final String desc;

    public static boolean hasExist(String metricsType) {
        if (null == metricsType) {
            return false;
        }
        for (ClusterPhyIndicesMetricsEnum typeEnum : ClusterPhyIndicesMetricsEnum.values()) {
            if (metricsType.equals(typeEnum.getType())) {
                return true;
            }
        }

        return false;
    }

    public String getDesc() {
        return desc;
    }

    public String getType() {
        return type;
    }

    public static List<String> getClusterPhyIndicesMetricsType() {
        List<String> clusterPhyIndicesMetricsTypes = Lists.newArrayList();
        for (ClusterPhyIndicesMetricsEnum value : ClusterPhyIndicesMetricsEnum.values()) {
            if (UNKNOWN.getType().equals(value.getType())) {
                continue;
            }

            clusterPhyIndicesMetricsTypes.add(value.getType());
        }

        return clusterPhyIndicesMetricsTypes;
    }
}
