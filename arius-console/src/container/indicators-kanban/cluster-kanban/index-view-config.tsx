import { unitMap, addChartTitle } from "../config";

export const indexConfigClassifyList: string[] = [
  "索引基础指标",
  "索引性能指标",
  "索引内存指标",
];

//黄金配置
export const goldConfig = {
  "索引基础指标": [
    'shardNu',
    'docs-count',
  ],
  "索引性能指标": [
    'indexing-index_total_rate',
    'indexing-index_time_in_millis',
    'search-query_time_in_millis',
  ],
  "索引内存指标": [
    'query_cache-memory_size_in_bytes',
    'segments-memory_in_bytes',
  ],
}

export const indexConfigData = {
  shardNu: {
    name: "Shard数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[0],
  },
  "store-size_in_bytes": {
    name: "索引大小",
    unit: unitMap.GB,
    classify: indexConfigClassifyList[0],
  },
  "docs-count": {
    name: "文档总数",
    unit: unitMap.count,
    classify: indexConfigClassifyList[0],
  },
  "indexing-index_total_rate": {
    name: "写入速率",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[1],
  },
  "indexing-index_time_in_millis": {
    name: "写入耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
  },
  "search-query_total_rate": {
    name: "查询Query速率",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[1],
  },
  "search-query_time_in_millis": {
    name: "查询Query耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
  },
  "search-fetch_total_rate": {
    name: "查询Fetch速率",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[1],
  },
  "search-fetch_time_in_millis": {
    name: "查询Fetch耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
  },
  "search-scroll_total_rate": {
    name: "查询Scroll速率",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[1],
  },
  "search-scroll_time_in_millis": {
    name: "查询Scroll耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
  },
  "merges-total_rate": {
    name: "Merge速率",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[1],
  },
  "merges-total_time_in_millis": {
    name: "Merge耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
  },
  "refresh-total_rate": {
    name: "Refresh速率",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[1],
  },
  "refresh-total_time_in_millis": {
    name: "Refresh耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
  },
  "flush-total_rate": {
    name: "Flush速率",
    unit: unitMap.ss,
    classify: indexConfigClassifyList[1],
  },
  "flush-total_time_in_millis": {
    name: "Flush耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[1],
  },
  "query_cache-memory_size_in_bytes": {
    name: "query_cache大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "segments-memory_in_bytes": {
    name: "Segments大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "segments-term_vectors_memory_in_bytes": {
    name: "terms_memory大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "segments-points_memory_in_bytes": {
    name: "points_memory大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "segments-doc_values_memory_in_bytes": {
    name: "doc_values_memory大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "segments-index_writer_memory_in_bytes": {
    name: "index_writer_memory大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
  },
  "translog-size_in_bytes": {
    name: "translog大小",
    unit: unitMap.MB,
    classify: indexConfigClassifyList[2],
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

export const defaultIndexConfigList: defaultIndexConfigItemPropsType[] =
  indexConfigClassifyList.map((item) => ({
    title: item,
    plainOptions: allCheckedData[item].map((item) => ({
      label: indexConfigData[item].name,
      value: item,
    })),
  }));
