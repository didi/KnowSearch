import { unitMap, addChartTitle } from "../config";

export const indexConfigClassifyList: string[] = ["索引基础指标", "索引性能指标", "索引内存指标"];

export const indexConfigData = {
  shardNu: {
    name: "索引Shard数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[0],
    indicatorType: [1],
  },
  "store-size_in_bytes": {
    name: "索引存储大小",
    unit: unitMap.GB,
    classify: indexConfigClassifyList[0],
    indicatorType: [],
  },
  "docs-count": {
    name: "文档总数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[0],
    indicatorType: [1],
  },
  "indexing-index_total_rate": {
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
  "search-query_total_rate": {
    name: "查询Query QPS",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[1],
    indicatorType: [1, 3],
  },
  "cost-query_time_in_millis": {
    name: "查询Query耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
    indicatorType: [1, 2],
  },
  "search-fetch_total_rate": {
    name: "查询Fetch QPS",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[1],
    indicatorType: [],
  },
  "cost-fetch_time_in_millis": {
    name: "查询Fetch耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
    indicatorType: [2],
  },
  "search-scroll_total_rate": {
    name: "查询Scroll量",
    unit: unitMap.countS,
    classify: indexConfigClassifyList[1],
    indicatorType: [],
  },
  "cost-scroll_time_in_millis": {
    name: "查询Scroll耗时",
    unit: unitMap.s,
    classify: indexConfigClassifyList[1],
    indicatorType: [2],
  },
  "merges-total_rate": {
    name: "Merge次数",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[1],
    indicatorType: [],
  },
  "cost-merges-total_time_in_millis": {
    name: "Merge耗时",
    unit: unitMap.s,
    classify: indexConfigClassifyList[1],
    indicatorType: [2],
  },
  // merges_avg_time: {
  //   name: "索引merge操作单次耗时",
  //   unit: unitMap.ms,
  //   classify: indexConfigClassifyList[1],
  // },
  "refresh-total_rate": {
    name: "Refresh次数",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[1],
    indicatorType: [],
  },
  "cost-refresh-total_time_in_millis": {
    name: "Refresh耗时",
    unit: unitMap.s,
    classify: indexConfigClassifyList[1],
    indicatorType: [2],
  },
  // refresh_avg_time: {
  //   name: "索引refresh操作单次耗时",
  //   unit: unitMap.ms,
  //   classify: indexConfigClassifyList[1],
  // },
  "flush-total_rate": {
    name: "Flush次数",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[1],
    indicatorType: [],
  },
  "cost-flush-total_time_in_millis": {
    name: "Flush耗时",
    unit: unitMap.s,
    classify: indexConfigClassifyList[1],
    indicatorType: [2],
  },
  // flush_avg_time: {
  //   name: "索引flush操作单次耗时",
  //   unit: unitMap.ms,
  //   classify: indexConfigClassifyList[1],
  // },
  // "indexing-time_per_doc": {
  //   name: "单个索引操作耗时",
  //   unit: unitMap.ms,
  //   classify: indexConfigClassifyList[1],
  // },
  "query_cache-memory_size_in_bytes": {
    name: "Query Cache内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
    indicatorType: [4],
  },
  "segments-memory_in_bytes": {
    name: "Segments大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
    indicatorType: [1, 4],
  },
  "segments-term_vectors_memory_in_bytes": {
    name: "Terms内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
    indicatorType: [4],
  },
  "segments-points_memory_in_bytes": {
    name: "Points内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
    indicatorType: [],
  },
  "segments-doc_values_memory_in_bytes": {
    name: "Doc Values内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
    indicatorType: [4],
  },
  "segments-index_writer_memory_in_bytes": {
    name: "Index Writer内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
    indicatorType: [4],
  },
  "translog-size_in_bytes": {
    name: "未提交的Translog大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
    indicatorType: [],
  },
  "segments-stored_fields_memory_in_bytes": {
    name: "Stored Fields大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
    indicatorType: [4],
  },
  "segments-norms_memory_in_bytes": {
    name: "Norms内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
    indicatorType: [],
  },
  "segments-version_map_memory_in_bytes": {
    name: "Version Map内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
    indicatorType: [],
  },
  "segments-fixed_bit_set_memory_in_bytes": {
    name: "Fixed Bitset内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
    indicatorType: [],
  },
  "fielddata-memory_size_in_bytes": {
    name: "Fielddata内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
    indicatorType: [4],
  },
  "request_cache-memory_size_in_bytes": {
    name: "Request Cache内存大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
    indicatorType: [4],
  },
};
// 给指标配置项添加标题
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
