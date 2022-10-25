package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author gyp
 * @version 1.0
 * @date 2022/5/9
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
     * 单位：MS
     */
    INDEXING_LATENCY("indexing-index_time_in_millis", "写入耗时"),
    /**
     * 单位：MS
     */
    INDEXING_INDEX_TIME_PER_DOC("indexing-index_time_per_doc", "单个文档写入耗时"),
    /**
     * 单位：个/S
     */
    QUERY_RATE("search-query_total_rate", "查询Query速率"),
    /**
     * 单位：个/S
     */
    FETCH_RATE("search-fetch_total_rate", "查询Fetch速率"),
    /**
     * 单位：MS
     */
    QUERY_LATENCY("cost-query_time_in_millis", "查询Query耗时"),
    /**
     * 单位：MS
     */
    INDEX_LATENCY("cost-index_time_in_millis", "写入耗时"),
    /**
     * 单位：MS
     */
    FETCH_LATENCY("cost-fetch_time_in_millis", "查询Fetch耗时"),
    /**
     * 单位：次/MIN
     */
    SCROLL_RATE("search-scroll_total_rate", "查询Scroll次数/分钟"),
    /**
     * 单位：MS
     */
    SCROLL_LATENCY("cost-scroll_time_in_millis", "查询Scroll耗时"),
    /**
     * 单位：MS
     */
    MERGE_LATENCY("cost-merges-total_time_in_millis", "Merge耗时"),
    /**
     * 单位：MS
     */
    REFRESH_LATENCY("cost-refresh-total_time_in_millis", "Refresh耗时"),
    /**
     * 单位：MS
     */
    FLUSH_LATENCY("cost-flush-total_time_in_millis", "Flush耗时"),
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
    DOC_VALUE_MEM_SIZE("segments-doc_values_memory_in_bytes","doc_values_memory大小"),
    /**
     * 单位：MB
     */
    INDEX_WRITE_MEM_SIZE("segments-index_writer_memory_in_bytes","index_writer_memory大小"),
    /**
     * 单位：MB
     */
    TRANSLOG_SIZE("translog-size_in_bytes", "translog大小"),
    /**
     * 单位：MS
     */
    INDEXING_TIME_PER_DOC("indices-indexing-index_time_per_doc", "索引单次操作文档耗时"),
    /**
     * 单位：MS
     */
    MERGES_AVG_TIME("merges_avg_time", "单次merges操作耗时"),
    /**
     * 单位：MS
     */
    REFRESH_AVG_TIME("refresh_avg_time", "单次refresh操作耗时"),
    /**
     * 单位：MS
     */
    FLUSH_AVG_TIME("flush_avg_time", "单次flush操作耗时"),
    /**
     * 单位：MB
     */
    SEGMENTS_STORED_FIELDS_MEM_SIZE("segments-stored_fields_memory_in_bytes","segments-stored_fields内存大小"),
    /**
     * 单位：MB
     */
    SEGMENTS_NORMS_MEM_SIZE("segments-norms_memory_in_bytes","Norms内存大小"),
    /**
     * 单位：MB
     */
    SEGMENTS_VERSION_MAP_MEM_SIZE("segments-version_map_memory_in_bytes","Version Map内存大小"),
    /**
     * 单位：MB
     */
    SEGMENTS_FIXED_BIT_SET_MEM_SIZE("segments-fixed_bit_set_memory_in_bytes","Fixed Bitsets内存大小"),
    /**
     * 单位：MB
     */
    FIELDDATA_MEM_SIZE("fielddata-memory_size_in_bytes","Fielddata内存大小"),
    /**
     * 单位：MB
     */
    SEGMENTS_REQUEST_CACHE_MEM_SIZE("segments-request_cache-memory_size_in_bytes","Request Cache内存大小");

    ClusterPhyIndicesMetricsEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    private String type;

    private String desc;

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
