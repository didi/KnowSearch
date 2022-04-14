package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Created by linyunan on 2021-07-30
 */
public enum ClusterPhyIndicesMetricsEnum {
                                          /*** 未知*/
                                          UNKNOWN("", "未知"),

                                          /*************************************1.索引性能指标**************************************/

                                          SHARDS_TOTAL("shardNu", "Shard数"),

                                          INDEX_SIZE("store-size_in_bytes", "索引大小"),

                                          DOCS_TOTAL("docs-count", "文档总数"),

                                          INDEXING_TOTAL_RATE("indexing-index_total_rate", "写入速率"),

                                          INDEXING_TOTAL_CONSUME("indexing-index_time_in_millis", "写入耗时"),

                                          QUERY_RATE("search-query_total_rate", "查询Query数"),

                                          FETCH_RATE("search-fetch_total_rate", "查询Fetch数"),

                                          QUERY_CONSUME("search-query_time_in_millis", "查询Query耗时"),

                                          FETCH_CONSUME("search-fetch_time_in_millis", "查询Fetch耗时"),

                                          SCROLL_COUNT("search-scroll_total_rate", "查询Scroll数/分钟"),

                                          SCROLL_CONSUME("search-scroll_time_in_millis", "查询Scroll耗时"),

                                          MERGE_CONSUME("merges-total_time_in_millis", "Merge耗时"),

                                          REFRESH_CONSUME("refresh-total_time_in_millis", "Refresh耗时"),

                                          FLUSH_CONSUME("flush-total_time_in_millis", "Flush耗时"),

                                          QUERY_CACHE_SIZE("query_cache-memory_size_in_bytes", "query_cache大小"),

                                          MERGE_COUNT("merges-total_rate", "Merge次/s"),

                                          REFRESH_COUNT("refresh-total_rate", "Refresh次/s"),

                                          FLUSH_COUNT("flush-total_rate", "Flush次数"),

                                          SEGMENT_SIZE("segments-memory_in_bytes", "Segments大小"),

                                          TERMS_MEM_SIZE("segments-term_vectors_memory_in_bytes", "terms_memory大小"),

                                          POINT_MEM_SIZE("segments-points_memory_in_bytes", "points_memory大小"),

                                          DOC_VALUE_MEM_SIZE("segments-doc_values_memory_in_bytes",
                                                             "doc_values_memory大小"),

                                          INDEX_WRITE_MEM_SIZE("segments-index_writer_memory_in_bytes",
                                                               "index_writer_memory大小"),

                                          TRANSLOG_SIZE("translog-size_in_bytes", "translog大小"),

                                          INDEXING_TIME_PER_DOC("indexing-time_per_doc", "索引单次操作文档耗时"),

                                          MERGES_AVG_TIME("merges_avg_time", "单次merges操作耗时"),

                                          REFRESH_AVG_TIME("refresh_avg_time", "单次refresh操作耗时"),

                                          FLUSH_AVG_TIME("flush_avg_time", "单次flush操作耗时"),

                                          SEGMENTS_STORED_FIELDS("segments-stored_fields_memory_in_bytes", "segments-stored_fields内存大小");


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
