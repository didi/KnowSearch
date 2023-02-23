import { getLineOption } from "./config";
import { unitMap, addChartTitle } from "../config";

// 指标配置 start
export const indexConfigClassifyList: string[] = ["总览性能指标"];

export const indexConfigData = {
  writeDocCount: {
    name: "写入请求量",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[0],
    price: "Gateway集群写入请求速率",
    currentCalLogic: "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为false时指定时间间隔的写入次数/指定时间min",
  },
  writeTotalCost: {
    name: "写入平均耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[0],
    price: "Gateway集群写入请求平均耗时",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为false时指定时间间隔的totalCost的总和/指定时间间隔min",
  },
  writeResponseLen: {
    name: "写入响应长度平均值",
    unit: unitMap.byte,
    classify: indexConfigClassifyList[0],
    price: "Gateway集群写入请求响应体平均大小",
    currentCalLogic: "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为false时指定时间间隔的responseLen的平均长度",
  },
  queryTotalHitsAvgCount: {
    name: "查询平均命中量",
    unit: unitMap.itemMin,
    classify: indexConfigClassifyList[0],
    price: "Gateway集群查询请求TotalHits平均值",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为true时指定时间间隔的totalHits的总和/指定时间间隔min",
  },
  readDocCount: {
    name: "查询量",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[0],
    price: "Gateway集群查询请求速率",
    currentCalLogic: "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为true时指定时间间隔的查询次数/指定时间min",
  },
  queryCostAvg: {
    name: "查询平均响应时间",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[0],
    price: "Gateway集群查询请求平均响应时间",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为true时指定时间间隔的totalCost的总和/指定时间间隔min",
  },
  queryTotalShardsAvg: {
    name: "查询shard平均数",
    unit: unitMap.countM,
    classify: indexConfigClassifyList[0],
    price: "Gateway集群查询请求平均命中Shard数",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为true时指定时间间隔的totalShards的总和/指定时间间隔min",
  },
  dslLen: {
    name: "写入吞吐量",
    unit: unitMap.characterMin,
    classify: indexConfigClassifyList[0],
    price: "Gateway集群写入请求吞吐量",
    currentCalLogic: "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为false时指定时间间隔的dslLen的平均长度",
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

// 指标配置项
export const defaultIndexConfigList: defaultIndexConfigItemPropsType[] = indexConfigClassifyList.map((item) => ({
  title: item,
  plainOptions: allCheckedData[item].map((item) => ({
    label: indexConfigData[item].name,
    value: item,
  })),
}));

export interface metricsType {
  metrics: { timeStamp: number; value: number }[];
  type: string;
}

export const getOverviewOption = (metrics: metricsType, configData, isMoreDay: boolean = false, isMarkLine: boolean = false) => {
  if (!metrics || !metrics.type || !configData[metrics.type]) {
    return {};
  }
  const title = (configData[metrics.type] && configData[metrics.type].title()) || metrics.type;
  const xAxisData = [];
  const series = [
    {
      name: configData[metrics.type].name || metrics.type,
      data: metrics.metrics.map((item) => {
        if (!xAxisData.includes(item.timeStamp)) {
          xAxisData.push(item.timeStamp);
        }
        return {
          value: item.value,
          timestamp: item.timeStamp,
        };
      }),
    },
  ];

  xAxisData.sort((a, b) => a - b);

  return getLineOption({
    title,
    xAxisData,
    series,
    unitMap: configData[metrics.type]?.unit || {},
    isMoreDay,
    isMarkLine,
  });
};
