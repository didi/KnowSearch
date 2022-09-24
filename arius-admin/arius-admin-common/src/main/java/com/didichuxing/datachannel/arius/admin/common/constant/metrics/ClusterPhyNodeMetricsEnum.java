package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author gyp
 * @date 2022/5/9
 * @version 1.0
 */
public enum ClusterPhyNodeMetricsEnum {
                                       /*** 未知*/
                                       UNKNOWN("", "未知"),

                                       /*************************************1.系统指标**************************************/
                                       /**
                                        * 单位：%
                                        */
                                       CPU_USAGE_PERCENT("os-cpu-percent", "CPU利用率"),
                                       /**
                                        * 单位：无
                                        */
                                       CPU_LOAD_AVERAGE_1M("os-cpu-load_average-1m", "cpu近1分钟内负载"),
                                       /**
                                        * 单位：无
                                        */
                                       CPU_LOAD_AVERAGE_5M("os-cpu-load_average-5m", "cpu近5分钟内负载"),
                                       /**
                                        * 单位：无
                                        */
                                       CPU_LOAD_AVERAGE_15M("os-cpu-load_average-15m", "cpu近15分钟内负载"),
                                       /**
                                        * 单位：%
                                        */

                                       DISK_FREE_PERCENT("fs-total-disk_free_percent", "磁盘空闲率"),
                                       /**
                                        * 单位：MB/S
                                        */

                                       TRANS_TX_RATE("transport-tx_count_rate", "网络发送速率"),
                                       /**
                                        * 单位：MB/S
                                        */

                                       TRANS_RX_RATE("transport-rx_count_rate", "网络接收速率"),
                                       /**
                                        * 单位：MB/S
                                        */

                                       TRANS_TX_SIZE("transport-tx_size_in_bytes_rate", "网络发送流量"),
                                       /**
                                        * 单位：MB/S
                                        */

                                       TRANS_RX_SIZE("transport-rx_size_in_bytes_rate", "网络接收流量"),

                                       /*************************************2.节点-索引基本信息******************************************/
                                       /**
                                        * 单位：个/s
                                        */

                                       INDICES_INDEXING_RATE("indices-indexing-index_total_rate", "索引写入速率"),
                                       /**
                                        * 单位：ms
                                        */

                                       INDICES_INDEXING_LATENCY("indices-indexing-index_time_in_millis", "索引写入耗时"),
                                       /**
                                        * 单位：个/s
                                        */

                                       INDICES_QUERY_RATE("indices-search-query_total_rate", "索引Query速率"),
                                       /**
                                        * 单位：个/s
                                        */

                                       INDICES_FETCH_RATE("indices-search-fetch_total_rate", "索引Fetch速率"),
                                       /**
                                        * 单位：ms
                                        */

                                       INDICES_QUERY_LATENCY("indices-search-query_time_in_millis", "索引Query耗时"),

                                       /**
                                        * 单位：个
                                        */
                                       INDICES_QUERY_TOTAL("indices-search-query_total", "索引Query总数"),

                                        /**
                                        * 单位：ms
                                        */

                                       INDICES_FETCH_LATENCY("indices-search-fetch_time_in_millis", "索引Fetch耗时"),
                                       /**
                                        * 单位：个
                                        */

                                       INDICES_CUREENT_SCROLL_NUM("indices-search-scroll_current", "Scroll当下请求量"),
                                       /**
                                        * 单位：ms
                                        */

                                       INDICES_SCROLL_LATENCY("indices-search-scroll_time_in_millis", "Scroll请求耗时"),
                                       /**
                                        * 单位：ms
                                        */

                                       INDICES_MERGE_LATENCY("indices-merges-total_time_in_millis", "Merge操作耗时"),
                                       /**
                                        * 单位：次/分钟
                                        */

                                       INDICES_MERGES_NUM_PER_MIN("indices-merges-total", "每分钟merges操作次数"),
                                       /**
                                        * 单位：次/分钟
                                        */

                                       INDICES_FLUSH_NUM_PER_MIN("indices-flush-total", "每分钟indices操作次数"),
                                       /**
                                        * 单位：次/分钟
                                        */

                                       INDICES_REFRESH_NUM_PER_MIN("indices-refresh-total", "每分钟refresh操作次数"),
                                       /**
                                        * 单位：ms
                                        */

                                       INDICES_REFRESH_LATENCY("indices-refresh-total_time_in_millis", "Refresh操作耗时"),
                                       /**
                                        * 单位：ms
                                        */

                                       INDICES_FLUSH_LATENCY("indices-flush-total_time_in_millis", "Flush操作耗时"),
                                       /**
                                        * 单位：MB
                                        */

                                       INDICES_UNCOMMITTED_TRANSLOG_SIZE("indices-translog-uncommitted_size_in_bytes",
                                                                         "未提交Translog大小"),
                                       /**
                                        * 单位：MB
                                        */

                                       INDICES_QUERY_CACHE_MEM_SIZE("indices-query_cache-memory_size_in_bytes",
                                                                    "QueryCache内存占用大小"),
                                       /**
                                        * 单位：MB
                                        */

                                       INDICES_REQUEST_CACHE_MEM_SIZE("indices-request_cache-memory_size_in_bytes",
                                                                      "RequestCache内存占用大小"),
                                       /**
                                        * 单位：次/分钟
                                        */

                                       INDICES_QUERY_CACHE_HIT_NUM("indices-query_cache-hit_count", "QueryCache内存命中次数"),
                                       /**
                                        * 单位：次/分钟
                                        */

                                       INDICES_REQUEST_CACHE_HIT_NUM("indices-request_cache-hit_count",
                                                                     "RequestCache内存命中次数"),
                                       /**
                                        * 单位：次/分钟
                                        */

                                       INDICES_QUERY_CACHE_MISS_NUM("indices-query_cache-miss_count",
                                                                    "QueryCache内存未命中次数"),
                                       /**
                                        * 单位：次/分钟
                                        */

                                       INDICES_REQUEST_CACHE_MISS_NUM("indices-request_cache-miss_count",
                                                                      "RequestCache内存未命中次数"),

                                       /*************************************3.节点-索引高级指标**************************************/
                                       /**
                                        * 单位：个
                                        */
                                       HTTP_OPEN_NUM("http-current_open", "Http活跃连接数"),
                                       /**
                                        * 单位：个
                                        */
                                       BUIK_QUEUE_SIZE("thread_pool-bulk-queue", "BulkQueue大小"),
                                       /**
                                        * 单位：个
                                        */
                                       BUIK_REJECTED_NUM("thread_pool-bulk-rejected", "BulkRejected个数"),
                                       /**
                                        * 单位：个
                                        */
                                       SEARCH_QUEUE_NUM("thread_pool-search-queue", "SearchQueue大小"),
                                       /**
                                        * 单位：个
                                        */
                                       SEARCH_REJECTED_NUM("thread_pool-search-rejected", "SearchRejected个数"),
                                       /**
                                        * 单位：个
                                        */
                                       WRITE_REJECTED_NUM("thread_pool-write-rejected", "WriteRejected个数"),
                                       /**
                                        * 单位：个
                                        */
                                       WRITE_QUEUE_SIZE("thread_pool-write-queue", "WriteQueue大小"),
                                       /**
                                        * 单位：个
                                        */
                                       INDICES_SEGMENT_NUM("indices-segments-count", "索引Segement数 "),
                                       /**
                                        * 单位：MB
                                        */
                                       INDICES_SEGMENT_MEM_SIZE("indices-segments-memory_in_bytes", "索引Segement内存大小"),
                                       /**
                                        * 单位：MB
                                        */
                                       INDICES_TERM_VECTORS_MEM_SIZE("indices-segments-term_vectors_memory_in_bytes",
                                                                     "索引term_vectors内存大小"),
                                       /**
                                        * 单位：MB
                                        */
                                       INDICES_POINT_MEM_SIZE("indices-segments-points_memory_in_bytes",
                                                              "索引points内存大小"),
                                       /**
                                        * 单位：MB
                                        */
                                       INDICES_DOC_VALUES_MEM_SIZE("indices-segments-doc_values_memory_in_bytes",
                                                                   "索引doc_values内存大小"),
                                       /**
                                        * 单位：MB
                                        */
                                       INDICES_WRITE_MEM_SIZE("indices-segments-index_writer_memory_in_bytes",
                                                              "索引index_writer内存大小"),
                                       /**
                                        * 单位：个
                                        */
                                       INDICES_NUM("indices-docs-count", "索引文档总数"),

                                       /**
                                        * 单位：个
                                        */
                                       INDICES_NUM_DIFF("indices-docs-count_diff", "间隔时间索引文档差值"),

                                       /**
                                        * 单位：MB
                                        */
                                       INDICES_SIZE("indices-store-size_in_bytes", "索引总存储大小"),

                                       /*************************************4.JVM指标******************************************/
                                       /**
                                        * 单位：次/s
                                        */
                                       YOUNG_GC_NUM("jvm-gc-young-collection_count_rate", "young-gc次数/s"),
                                       /**
                                        * 单位：次/s
                                        */
                                       OLD_GC_NUM("jvm-gc-old-collection_count_rate", "old-gc次数/s"),
                                       /**
                                        * 单位：ms
                                        */
                                       YOUNG_GC_LATENCY("jvm-gc-young-collection_time_in_millis", "young-gc耗时"),
                                       /**
                                        * 单位：ms
                                        */
                                       OLD_GC_LATENCY("jvm-gc-old-collection_time_in_millis", "old-gc耗时"),
                                       /**
                                        * 单位：MB
                                        */
                                       JVM_MEM_HEAP_USED("jvm-mem-heap_used_in_bytes", "JVM堆内存使用量"),
                                       /**
                                        * 单位：MB
                                        */
                                       JVM_MEM_NON_HEAP_USED("jvm-mem-non_heap_used_in_bytes", "JVM堆外存使用量"),
                                       /**
                                        * 单位：%
                                        */
                                       JVM_MEM_HEAP_PERCENT("jvm-mem-heap_used_percent", "JVM堆使用率"),
                                       /**
                                        * 单位：MB
                                        */
                                       JVM_MEM_POOLS_YOUNG_USED_IN_BYTES("jvm-mem-pools-young-used_in_bytes",
                                                                         "jvm堆内存young区使用空间"),
                                       /**
                                        * 单位：MB
                                        */
                                       JVM_MEM_POOLS_OLD_USED_IN_BYTES("jvm-mem-pools-old-used_in_bytes",
                                                                       "jvm堆内存old区使用空间"),
                                       /**
                                        * 单位：MB
                                        */
                                       STORED_FIELDS_MEMORY_IN_BYTES("indices-segments-stored_fields_memory_in_bytes",
                                                                     "indices-segments-stored_fields内存大小"),

                                       /*************************************5.TASK指标******************************************/
                                       TASK_COUNT("taskId", "task id"), TASK_COST("runningTime", "task耗时");

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
