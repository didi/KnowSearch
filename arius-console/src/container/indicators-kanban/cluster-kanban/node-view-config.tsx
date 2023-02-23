import { unitMap, addChartTitle } from "../config";

// 指标分类
export const indexConfigClassifyList: string[] = ["系统指标", "基本性能指标", "高级性能指标", "JVM指标", "Breaker指标", "内存大小指标"];

// 所有指标项
export const indexConfigData = {
  "os-cpu-percent": {
    // 指标配置选项，中文映射
    name: "CPU利用率",
    // 单位名称和单位数据格式化函数
    unit: unitMap.percent,
    // 分类
    classify: indexConfigClassifyList[0],
    indicatorType: [1, 2, 3],
  },
  "os-cpu-load_average-1m": {
    name: "CPU近1分钟负载",
    unit: unitMap.none,
    classify: indexConfigClassifyList[0],
    indicatorType: [2, 3],
  },
  "fs-total-disk_free_percent": {
    name: "磁盘空闲率",
    unit: unitMap.percent,
    classify: indexConfigClassifyList[0],
    indicatorType: [1, 3],
  },
  "transport-tx_size_in_bytes_rate": {
    name: "网络发送流量",
    unit: unitMap.mbS,
    classify: indexConfigClassifyList[0],
    indicatorType: [3],
  },
  "transport-rx_size_in_bytes_rate": {
    name: "网络接收流量",
    unit: unitMap.mbS,
    classify: indexConfigClassifyList[0],
    indicatorType: [3],
  },
  "indices-indexing-index_total_rate": {
    name: "写入TPS",
    unit: unitMap.countS,
    classify: indexConfigClassifyList[1],
    indicatorType: [1, 3],
  },
  "indices-indexing-index_time_per_doc": {
    name: "写入耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
    indicatorType: [1, 2],
  },
  "thread_pool-bulk-rejected": {
    name: "Write Rejected",
    unit: unitMap.count,
    classify: indexConfigClassifyList[1],
    indicatorType: [1, 2],
  },
  "thread_pool-bulk-queue": {
    name: "Write Queue",
    unit: unitMap.count,
    classify: indexConfigClassifyList[1],
    indicatorType: [1, 2],
  },
  "indices-search-query_total_rate": {
    name: "Query QPS",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[1],
    indicatorType: [1, 3],
  },
  "indices-search-fetch_total_rate": {
    name: "Fetch QPS",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[1],
    indicatorType: [3],
  },
  "indices-search-query_time_per_query": {
    name: "Query耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
    indicatorType: [1, 2],
  },
  "indices-search-fetch_time_per_fetch": {
    name: "Fetch耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
    indicatorType: [2],
  },
  "thread_pool-search-queue": {
    name: "Search Queue",
    unit: unitMap.count,
    classify: indexConfigClassifyList[1],
    indicatorType: [1, 2],
  },
  "thread_pool-search-rejected": {
    name: "Search Rejected",
    unit: unitMap.count,
    classify: indexConfigClassifyList[1],
    indicatorType: [1, 2],
  },
  "indices-search-scroll_current": {
    name: "Scroll当下请求量",
    unit: unitMap.count,
    classify: indexConfigClassifyList[1],
    indicatorType: [],
  },
  "indices-search-scroll_avg_time": {
    name: "Scroll请求耗时",
    unit: unitMap.s,
    classify: indexConfigClassifyList[1],
    indicatorType: [2],
  },
  "indices-merges_avg_time": {
    name: "Merge操作耗时",
    unit: unitMap.s,
    classify: indexConfigClassifyList[1],
    indicatorType: [2],
  },
  "indices-merges-total": {
    name: "Merge次数",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[1],
    indicatorType: [],
  },
  "indices-refresh_avg_time": {
    name: "Refresh操作耗时",
    unit: unitMap.s,
    classify: indexConfigClassifyList[1],
    indicatorType: [2],
  },
  "indices-refresh-total": {
    name: "Refresh次数",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[1],
    indicatorType: [],
  },
  "indices-flush_avg_time": {
    name: "Flush操作耗时",
    unit: unitMap.s,
    classify: indexConfigClassifyList[1],
    indicatorType: [2],
  },
  "indices-flush-total": {
    name: "Flush次数",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[1],
    indicatorType: [],
  },
  "indices-query_cache-hit_rate": {
    name: "Query Cache内存命中率",
    unit: unitMap.percent,
    classify: indexConfigClassifyList[1],
    indicatorType: [2],
  },
  "indices-request_cache-hit_rate": {
    name: "Request Cache内存命中率",
    unit: unitMap.percent,
    classify: indexConfigClassifyList[1],
    indicatorType: [],
  },
  "indices-query_cache-evictions": {
    name: "Query Cache evictions",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[1],
    indicatorType: [],
  },
  "indices-request_cache-evictions": {
    name: "Request Cache eviction",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[1],
    indicatorType: [],
  },
  "http-current_open": {
    name: "Http活跃连接数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[2],
    indicatorType: [1, 3],
  },
  "indices-segments-count": {
    name: "Segment数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[2],
    indicatorType: [3, 4],
  },
  "indices-docs-count": {
    name: "文档总数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[2],
    indicatorType: [3],
  },
  "indices-store-size_in_bytes": {
    name: "总存储大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
    indicatorType: [],
  },
  "indices-translog-uncommitted_size_in_bytes": {
    name: "未提交的Translog大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
    indicatorType: [],
  },
  runningTime: {
    name: "执行任务耗时",
    unit: unitMap.s,
    classify: indexConfigClassifyList[2],
    newquota: "avg",
    indicatorType: [1, 2],
  },
  taskId: {
    name: "执行任务数量",
    unit: unitMap.count,
    classify: indexConfigClassifyList[2],
    newquota: "cardinality",
    indicatorType: [1, 2],
  },
  "thread_pool-write-queue": {
    name: "写入线程池queue数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[2],
    indicatorType: [2],
  },
  "thread_pool-refresh-queue": {
    name: "刷新线程池queue数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[2],
    indicatorType: [],
  },
  "thread_pool-rollup_indexing-queue": {
    name: "落盘线程池queue数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[2],
    indicatorType: [],
  },
  "thread_pool-management-queue": {
    name: "管理线程池queue数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[2],
    indicatorType: [],
  },
  "thread_pool-force_merge-queue": {
    name: "合并线程池queue数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[2],
    indicatorType: [],
  },
  "jvm-gc-young-collection_count_rate": {
    name: "Young GC次数",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[3],
    indicatorType: [1, 2],
  },
  "jvm-gc-old-collection_count_rate": {
    name: "Old GC次数",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[3],
    indicatorType: [1, 2],
  },
  "jvm-gc-young-collection_avg_time": {
    name: "Young GC耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[3],
    indicatorType: [],
  },
  "jvm-gc-old-collection_avg_time": {
    name: "Old GC耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[3],
    indicatorType: [],
  },
  "jvm-mem-heap_used_in_bytes": {
    name: "JVM堆内存使用量",
    unit: unitMap.GB,
    classify: indexConfigClassifyList[3],
    indicatorType: [],
  },
  "jvm-mem-non_heap_used_in_bytes": {
    name: "JVM堆外存使用量",
    unit: unitMap.GB,
    classify: indexConfigClassifyList[3],
    indicatorType: [],
  },
  "jvm-mem-heap_used_percent": {
    name: "JVM堆使用率",
    unit: unitMap.percent,
    classify: indexConfigClassifyList[3],
    indicatorType: [1, 2],
  },
  "jvm-mem-pools-young-used_in_bytes": {
    name: "堆内存young区使用空间",
    unit: unitMap.GB,
    classify: indexConfigClassifyList[3],
    indicatorType: [],
  },
  "jvm-mem-pools-old-used_in_bytes": {
    name: "堆内存old区使用空间",
    unit: unitMap.GB,
    classify: indexConfigClassifyList[3],
    indicatorType: [],
  },
  "breakers-fielddata-limit_size_in_bytes": {
    name: "Field data circuit breaker 内存占用",
    unit: unitMap.GB,
    classify: indexConfigClassifyList[4],
    indicatorType: [],
  },
  "breakers-request-limit_size_in_bytes": {
    name: "Request circuit breaker 内存占用",
    unit: unitMap.GB,
    classify: indexConfigClassifyList[4],
    indicatorType: [],
  },
  "breakers-in_flight_requests-limit_size_in_bytes": {
    name: "Inflight requests circuit breaker 内存占用",
    unit: unitMap.GB,
    classify: indexConfigClassifyList[4],
    indicatorType: [1, 4],
  },
  "breakers-in_flight_http_requests-limit_size_in_bytes": {
    name: "Inflight http requests circuit breaker 内存占用",
    unit: unitMap.GB,
    classify: indexConfigClassifyList[4],
    indicatorType: [1, 4],
  },
  "breakers-accounting-limit_size_in_bytes": {
    name: "Accounting requests circuit breaker 内存占用",
    unit: unitMap.GB,
    classify: indexConfigClassifyList[4],
    indicatorType: [],
  },
  // "script-compilations": {
  //   name: "Script compilation circuit breaker 编译次数",
  //   unit: unitMap.mins,
  //   classify: indexConfigClassifyList[4],
  //   indicatorType: [],
  // },
  "breakers-parent-limit_size_in_bytes": {
    name: "Parent circuit breaker JVM真实内存占用",
    unit: unitMap.GB,
    classify: indexConfigClassifyList[4],
    indicatorType: [1, 4],
  },
  "indices-segments-memory_in_bytes": {
    name: "Segment内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[5],
    indicatorType: [1, 4],
  },
  "indices-segments-term_vectors_memory_in_bytes": {
    name: "Terms内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[5],
    indicatorType: [4],
  },
  "indices-segments-points_memory_in_bytes": {
    name: "Points内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[5],
    indicatorType: [],
  },
  "indices-segments-doc_values_memory_in_bytes": {
    name: "Doc Values内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[5],
    indicatorType: [4],
  },
  "indices-segments-index_writer_memory_in_bytes": {
    name: "Index Writer内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[5],
    indicatorType: [4],
  },
  "indices-query_cache-memory_size_in_bytes": {
    name: "Query Cache内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[5],
    indicatorType: [4],
  },
  "indices-request_cache-memory_size_in_bytes": {
    name: "Request Cache内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[5],
    indicatorType: [4],
  },
  "indices-segments-stored_fields_memory_in_bytes": {
    name: "Stored Fields大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[5],
    indicatorType: [4],
  },
  "indices-segments-norms_memory_in_bytes": {
    name: "Norms内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[5],
    indicatorType: [],
  },
  "indices-segments-version_map_memory_in_bytes": {
    name: "Version Map内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[5],
    indicatorType: [],
  },
  "indices-segments-fixed_bit_set_memory_in_bytes": {
    name: "Fixed Bitset内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[5],
    indicatorType: [],
  },
  "breakers-fielddata-estimated_size_in_bytes": {
    name: "Fielddata内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[5],
    indicatorType: [4],
  },
};

addChartTitle(indexConfigData);

export const getCheckedData = (checkedList) => {
  const defaultClassifyData = {};

  indexConfigClassifyList.forEach((item) => {
    defaultClassifyData[item] = [];
  });

  checkedList.forEach((item) => {
    if (indexConfigData[item]) {
      defaultClassifyData[indexConfigData[item].classify].push(item);
    }
  });

  return defaultClassifyData;
};

//
export const allCheckedData = getCheckedData(Object.keys(indexConfigData));

interface defaultIndexConfigItemPropsType {
  title: string;
  plainOptions: { label: string; value: string }[];
}

export const defaultIndexConfigList: defaultIndexConfigItemPropsType[] = indexConfigClassifyList.map((item) => ({
  title: item,
  plainOptions: allCheckedData[item].map((item) => ({
    label: indexConfigData[item].name,
    value: item,
    indicatorType: indexConfigData[item].indicatorType,
  })),
}));

export const parseLineParams = (data) => {
  const title = data.nodeMetricsType;
  const xAxis = data.nodesSubMetrics[0].nodeMetricsContentCells.map((item) => item.timeStamp);
  const series = data.nodesSubMetrics.map((item) => {
    return {
      name: item.nodeIp,
      data: item.nodeMetricsContentCells.map((item) => ({
        value: item.nodeMetricsValue,
        timeStamp: item.timeStamp,
      })),
    };
  });
  return { title, xAxis, series };
};
