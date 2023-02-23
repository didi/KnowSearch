import { addChartTitle, unitMap } from "../config";

// 指标分类
const indexConfigClassifyList: string[] = ["索引性能指标"];

// 指标项
export const indexConfigData = {
  writeIndexCount: {
    name: "gateway对索引写入量",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[0],
    price: "Gateway视角索引级写入请求速率",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为false时相应索引对于destTemplateName字段的指定时间间隔的写入次数/指定时间间隔min",
  },
  writeIndexTotalCost: {
    name: "gateway对索引写入耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[0],
    price: "Gateway视角索引级写入请求平均耗时",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为false时相应索引对于destTemplateName字段的指定时间间隔的totalCost的总和/指定时间间隔min",
  },
  searchIndexCount: {
    name: "索引查询量分布",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[0],
    price: "Gateway视角索引级查询请求速率",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为true时相应索引对于destTemplateName字段的指定时间间隔的查询次数/指定时间间隔min",
  },
  searchIndexTotalCost: {
    name: "索引平均查询耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[0],
    price: "Gateway视角索引级查询请求平均耗时",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为true时相应索引对于destTemplateName字段的指定时间间隔的totalCost的总和/指定时间间隔min",
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
  })),
}));
