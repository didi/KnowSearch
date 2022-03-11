package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by linyunan on 2021-07-30
 */
public enum ClusterPhyNodeMetricsEnum {
                                       /*** 未知*/
                                       UNKNOWN("", "未知"),

                                       /*************************************1.系统指标**************************************/

                                       CPU_USAGE_PERCENT("os-cpu-percent", "CPU利用率"),

                                       CPU_LOAD_AVERAGE_1M("os-cpu-load_average-1m", "cpu近1分钟内负载"),

                                       CPU_LOAD_AVERAGE_5M("os-cpu-load_average-5m", "cpu近5分钟内负载"),

                                       CPU_LOAD_AVERAGE_15M("os-cpu-load_average-15m", "cpu近15分钟内负载"),

                                       DISK_FREE_PERCENT("fs-total-disk_free_percent", "磁盘空闲率"),

                                       TRANS_TX_RATE("transport-tx_count_rate", "网络发送速率"),

                                       TRANS_RX_RATE("transport-rx_count_rate", "网络接收速率"),

                                       TRANS_TX_SIZE("transport-tx_size_in_bytes_rate", "网络发送流量"),

                                       TRANS_RX_SIZE("transport-rx_size_in_bytes_rate", "网络接收流量"),

                                       /*************************************2.节点-索引基本信息******************************************/

                                       INDICES_INDEXING_RATE("indices-indexing-index_total_rate", "索引写入速率"),

                                       INDICES_INDEXING_CONSUME("indices-indexing-index_time_in_millis", "索引写入耗时"),

                                       INDICES_QUERY_RATE("indices-search-query_total_rate", "索引Query速率"),

                                       INDICES_FETCH_RATE("indices-search-fetch_total_rate", "索引Fetch速率"),

                                       INDICES_QUERY_CONSUME("indices-search-query_time_in_millis", "索引Query耗时"),

                                       INDICES_FETCH_CONSUME("indices-search-fetch_time_in_millis", "索引Fetch耗时"),

                                       INDICES_SCROLL_COUNT("indices-search-scroll_current", "Scroll当下请求量"),

                                       INDICES_SCROLL_CONSUME("indices-search-scroll_time_in_millis",
                                                              "Scroll请求耗时"),

                                       INDICES_MERGE_CONSUME("indices-merges-total_time_in_millis", "Merge操作耗时"),

                                       INDICES_REFRESH_CONSUME("indices-refresh-total_time_in_millis", "Refresh操作耗时"),

                                       INDICES_FLUSH_CONSUME("indices-flush-total_time_in_millis", "Flush操作耗时"),

                                       INDICES_TRANSLOG_SIZE("indices-translog-uncommitted_size_in_bytes",
                                                             "未提交Translog大小"),

                                       INDICES_QUERY_CACHE_MEM_SIZE("indices-query_cache-memory_size_in_bytes",
                                                                    "QueryCache内存占用大小"),

                                       INDICES_REQUEST_CACHE_MEM_SIZE("indices-request_cache-memory_size_in_bytes",
                                                                      "RequestCache内存占用大小"),

                                       INDICES_QUERY_CACHE_HIT_COUNT("indices-query_cache-hit_count",
                                                                     "QueryCache内存命中次数"),

                                       INDICES_REQUEST_CACHE_HIT_COUNT("indices-request_cache-hit_count",
                                                                       "RequestCache内存命中次数"),

                                       INDICES_QUERY_CACHE_MISS_COUNT("indices-query_cache-miss_count",
                                                                      "QueryCache内存未命中次数"),

                                       INDICES_REQUEST_CACHE_MISS_COUNT("indices-request_cache-miss_count",
                                                                        "RequestCache内存未命中次数"),

                                       /*************************************3.节点-索引高级指标**************************************/

                                       HTTP_OPEN_COUNT("http-current_open", "Http活跃连接数"),

                                       BUIK_QUEUE_SIZE("thread_pool-bulk-queue", "BulkQueue大小"),

                                       BUIK_REJECTED_COUNT("thread_pool-bulk-rejected", "BulkRejected个数"),

                                       SEARCH_QUEUE_SIZE("thread_pool-search-queue", "SearchQueue大小"),

                                       SEARCH_REJECTED_COUNT("thread_pool-search-rejected", "SearchRejected个数"),

                                       WRITE_REJECTED_COUNT("thread_pool-write-rejected", "WriteRejected个数"),

                                       WRITE_QUEUE_SIZE("thread_pool-write-queue", "WriteQueue大小"),

                                       INDICES_SEGMENT_COUNT("indices-segments-count", "索引Segement数 "),

                                       INDICES_SEGMENT_MEM_SIZE("indices-segments-memory_in_bytes", "索引Segement内存大小"),

                                       INDICES_TERM_VECTORS_MEM_SIZE("indices-segments-term_vectors_memory_in_bytes",
                                                                     "索引term_vectors内存大小"),

                                       INDICES_POINT_MEM_SIZE("indices-segments-points_memory_in_bytes",
                                                              "索引points内存大小"),

                                       INDICES_DOC_VALUES_MEM_SIZE("indices-segments-doc_values_memory_in_bytes",
                                                                   "索引doc_values内存大小"),

                                       INDICES_WRITE_MEM_SIZE("indices-segments-index_writer_memory_in_bytes",
                                                              "索引index_writer内存大小"),

                                       INDICES_COUNT("indices-docs-count", "索引文档总数"),

                                       INDICES_SIZE("indices-store-size_in_bytes", "索引总存储大小"),

                                       /*************************************4.JVM指标******************************************/

                                       YOUNG_GC_COUNT("jvm-gc-young-collection_count_rate", "young-gc次数/s"),

                                       OLD_GC_COUNT("jvm-gc-old-collection_count_rate", "old-gc次数/s"),

                                       YOUNG_GC_CONSUME("jvm-gc-young-collection_time_in_millis", "young-gc耗时"),

                                       OLD_GC_CONSUME("jvm-gc-old-collection_time_in_millis", "old-gc耗时"),

                                       JVM_MEM_HEAP_USED("jvm-mem-heap_used_in_bytes", "JVM堆内存使用量"),

                                       JVM_MEM_NON_HEAP_USED("jvm-mem-non_heap_used_in_bytes", "JVM堆外存使用量"),

                                       JVM_MEM_HEAP_PERCENT("jvm-mem-heap_used_percent", "JVM堆使用率"),

                                       /*************************************5.TASK指标******************************************/
                                       TASK_COUNT("taskId", "task id"),
                                       TASK_COST("runningTime", "task耗时");

    ClusterPhyNodeMetricsEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;

    }

    private String type;

    private String desc;

    public String getDesc() {
        return desc;
    }

    public String getType() {
        return type;
    }

    public static boolean hasExist(String metricsType) {
        if (null == metricsType) {
            return false;
        }
        for (ClusterPhyNodeMetricsEnum typeEnum : ClusterPhyNodeMetricsEnum.values()) {
            if (metricsType.equals(typeEnum.getType())) {
                return true;
            }
        }

        return false;
    }

    public static List<String> getClusterPhyNodeMetricsType() {
        List<String> clusterPhyNodeMetricsTypes = Lists.newArrayList();
        for (ClusterPhyNodeMetricsEnum value : ClusterPhyNodeMetricsEnum.values()) {
            if (UNKNOWN.getType().equals(value.getType())) {
                continue;
            }

            clusterPhyNodeMetricsTypes.add(value.getType());
        }

        return clusterPhyNodeMetricsTypes;
    }

    public static List<String> getPercentMetricsType() {
        return Lists.newArrayList(DISK_FREE_PERCENT.getType());
    }
}
