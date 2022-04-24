import { unitMap, addChartTitle } from "../config";

// 指标分类
export const indexConfigClassifyList: string[] = [
  "系统指标",
  "基本性能指标",
  "高级性能指标",
  "JVM指标",
];

//黄金配置
export const goldConfig = {
  "系统指标": [
    'os-cpu-percent',
    'fs-total-disk_free_percent',
  ],
  "基本性能指标": [
    'indices-indexing-index_total_rate',
    'indices-indexing-index_time_in_millis',
    'thread_pool-bulk-rejected',
    'thread_pool-bulk-queue',
    'indices-search-query_total_rate',
    'indices-search-query_time_in_millis',
    'thread_pool-search-queue',
    'thread_pool-search-rejected',
    'indices-merges-total_time_in_millis',
    'indices-refresh-total_time_in_millis',
    'indices-flush-total_time_in_millis',
  ],
  "高级性能指标": [
    'http-current_open',
    'indices-segments-count',
    'indices-segments-memory_in_bytes',
    'indices-docs-count',
  ],
  "JVM指标": [
    'jvm-gc-young-collection_count_rate',
    'jvm-gc-old-collection_count_rate',
    'jvm-mem-heap_used_percent',
  ],
}

// 所有指标项
export const indexConfigData = {
  "os-cpu-percent": {
    // 指标配置选项，中文映射
    name: "CPU利用率",
    // 单位名称和单位数据格式化函数
    unit: unitMap.percent,
    // 分类
    classify: indexConfigClassifyList[0],
  },
  "os-cpu-load_average-1m": {
    name: "CPU近1分钟内负载",
    unit: unitMap.none,
    classify: indexConfigClassifyList[0],
  },
  "os-cpu-load_average-5m": {
    name: "CPU近5分钟内负载",
    unit: unitMap.none,
    classify: indexConfigClassifyList[0],
  },
  "os-cpu-load_average-15m": {
    name:  "CPU近15分钟内负载",
    unit: unitMap.none,
    classify: indexConfigClassifyList[0],
  },
  "fs-total-disk_free_percent": {
    name: "磁盘空闲率",
    unit: unitMap.percent,
    classify: indexConfigClassifyList[0],
  },
  "transport-tx_count_rate": {
    name: "网络发送速率",
    unit: unitMap.countS,
    classify: indexConfigClassifyList[0],
  },
  "transport-rx_count_rate": {
    name: "网络接收速率",
    unit: unitMap.countS,
    classify: indexConfigClassifyList[0],
  },
  "transport-tx_size_in_bytes_rate": {
    name: "网络发送流量",
    unit: unitMap.mbS,
    classify: indexConfigClassifyList[0],
  },
  "transport-rx_size_in_bytes_rate": {
    name: "网络接收流量",
    unit: unitMap.mbS,
    classify: indexConfigClassifyList[0],
  },
  "indices-indexing-index_total_rate": {
    name: "索引写入速率",
    unit: unitMap.countS,
    classify: indexConfigClassifyList[1],
  },
  "indices-indexing-index_time_in_millis": {
    name: "索引写入耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
  },
  "thread_pool-bulk-rejected": {
    name: "WriteRejected个数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[1],
  },
  "thread_pool-bulk-queue": {
    name: "WriteQueue个数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[1],
  },
  "indices-search-query_total_rate": {
    name: "索引Query速率",
    unit: unitMap.countS,
    classify: indexConfigClassifyList[1],
  },
  "indices-search-fetch_total_rate": {
    name: "索引Fetch速率",
    unit: unitMap.countS,
    classify: indexConfigClassifyList[1],
  },
  "indices-search-query_time_in_millis": {
    name: "索引Query耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
  },
  "indices-search-fetch_time_in_millis": {
    name: "索引Fetch耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
  },
  "thread_pool-search-queue": {
    name: "SearchQueue个数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[1],
  },
  "thread_pool-search-rejected": {
    name: "SearchRejected个数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[1],
  },
  "indices-search-scroll_current": {
    name: "Scroll当下请求量",
    unit: unitMap.countS,
    classify: indexConfigClassifyList[1],
  },
  "indices-search-scroll_time_in_millis": {
    name: "Scroll请求耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
  },
  "indices-merges-total_time_in_millis": {
    name: "Merge操作耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
  },
  "indices-merges-total": {
    name: "每分钟merge次数",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[1],
  },
  "indices-refresh-total_time_in_millis": {
    name: "Refresh操作耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
  },
  "indices-refresh-total": {
    name: "每分钟refresh次数",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[1],
  },
  "indices-flush-total_time_in_millis": {
    name: "Flush操作耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
  },
  "indices-flush-total": {
    name: "每分钟flush次数",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[1],
  },
  "indices-query_cache-hit_count": {
    name: "QueryCache内存命中次数",
    unit: unitMap.numS,
    classify: indexConfigClassifyList[1],
  },
  "indices-query_cache-miss_count": {
    name: "QueryCache内存未命中次数",
    unit: unitMap.numS,
    classify: indexConfigClassifyList[1],
  },
  "indices-request_cache-hit_count": {
    name: "RequestCache内存命中次数",
    unit: unitMap.numS,
    classify: indexConfigClassifyList[1],
  },
  "indices-request_cache-miss_count": {
    name: "RequestCache内存未命中次数",
    unit: unitMap.numS,
    classify: indexConfigClassifyList[1],
  },
  "http-current_open": {
    name: "Http活跃连接数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[2],
  },
  // "thread_pool-bulk-queue": {
  //   name: "BulkQueue大小",
  //   unit: unitMap.count,
  //   classify: indexConfigClassifyList[2],
  // },
  // "thread_pool-bulk-rejected": {
  //   name: "BulkRejected个数",
  //   unit: unitMap.count,
  //   classify: indexConfigClassifyList[2],
  // },
  "indices-segments-count": {
    name: "索引Segement数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[2],
  },
  "indices-segments-memory_in_bytes": {
    name: "索引Segement内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "indices-segments-term_vectors_memory_in_bytes": {
    name: "索引term_vectors内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "indices-segments-points_memory_in_bytes": {
    name: "索引points内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "indices-segments-doc_values_memory_in_bytes": {
    name: "索引doc_values内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "indices-segments-index_writer_memory_in_bytes": {
    name: "索引index_writer内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "indices-docs-count": {
    name: "索引文档总数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[2],
  },
  "indices-store-size_in_bytes": {
    name: "索引总存储大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "indices-translog-uncommitted_size_in_bytes": {
    name: "未提交的 Translog 大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "indices-query_cache-memory_size_in_bytes": {
    name: "QueryCache内存占用大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "indices-request_cache-memory_size_in_bytes": {
    name: "RequestCache内存占用大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "runningTime": {
    name: "节点执行任务数耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[2],
    newquota: 'avg',
  },
  "taskId": {
    name: "节点执行任务数量",
    unit: unitMap.count,
    classify: indexConfigClassifyList[2],
    newquota: 'cardinality',
  },
  "indices-segments-stored_fields_memory_in_bytes": {
    name: "stored_fields_memory大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "jvm-gc-young-collection_count_rate": {
    name: "young-gc次数",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[3],
  },
  "jvm-gc-old-collection_count_rate": {
    name: "old-gc次数",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[3],
  },
  "jvm-gc-young-collection_time_in_millis": {
    name: "young-gc耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[3],
  },
  "jvm-gc-old-collection_time_in_millis": {
    name: "old-gc耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[3],
  },
  "jvm-mem-heap_used_in_bytes": {
    name: "JVM堆内存使用量",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[3],
  },
  "jvm-mem-non_heap_used_in_bytes": {
    name: "JVM堆外存使用量",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[3],
  },
  "jvm-mem-heap_used_percent": {
    name: "JVM堆使用率",
    unit: unitMap.percent,
    classify: indexConfigClassifyList[3],
  },
  "jvm-mem-pools-young-used_in_bytes": {
    name: "堆内存young区使用空间",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[3],
  },
  "jvm-mem-pools-old-used_in_bytes": {
    name: "堆内存old区使用空间",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[3],
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

export const defaultIndexConfigList: defaultIndexConfigItemPropsType[] =
  indexConfigClassifyList.map((item) => ({
    title: item,
    plainOptions: allCheckedData[item].map((item) => ({
      label: indexConfigData[item].name,
      value: item,
    })),
  }));

export const parseLineParams = (data) => {
  const title = data.nodeMetricsType;
  const xAxis = data.nodesSubMetrics[0].nodeMetricsContentCells.map(
    (item) => item.timeStamp
  );
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
