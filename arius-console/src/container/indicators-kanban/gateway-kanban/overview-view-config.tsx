import { getLineOption } from "./config";
import { unitMap, addChartTitle } from "../config";

// 指标配置 start
export const indexConfigClassifyList: string[] = ["总览性能指标"];

export const indexConfigData = {
  writeDocCount: {
    name: "写入量",
    unit: unitMap.itemMin,
    classify: indexConfigClassifyList[0],
  },
  writeTotalCost: {
    name: "写入平均耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[0],
  },
  writeResponseLen: {
    name: "写入响应长度平均值",
    unit: unitMap.byte,
    classify: indexConfigClassifyList[0],
  },
  queryTotalHitsAvgCount: {
    name: "查询平均命中量",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[0],
  },
  readDocCount: {
    name: "查询量",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[0],
  },
  querySearchType: {
    name: "dsl/sql分布",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[0],
  },
  queryCostAvg: {
    name: "查询平均响应时间",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[0],
  },
  queryTotalShardsAvg: {
    name: "查询shard平均数",
    unit: unitMap.countM,
    classify: indexConfigClassifyList[0],
  },
  queryFailedShardsAvg: {
    name: "查询失败shard平均数",
    unit: unitMap.countM,
    classify: indexConfigClassifyList[0],
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
export const defaultIndexConfigList: defaultIndexConfigItemPropsType[] =
  indexConfigClassifyList.map((item) => ({
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

export const getOverviewOption = (
  metrics: metricsType,
  configData,
  isMoreDay: boolean = false,
  isMarkLine: boolean = false
) => {
  if (!metrics || !metrics.type || !configData[metrics.type]) {
    console.log(metrics.type);
    return {};
  }
  const title =
    (configData[metrics.type] && configData[metrics.type].title()) ||
    metrics.type;
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
    unitMap: configData[metrics.type].unit || {},
    isMoreDay,
    isMarkLine
  });
};
