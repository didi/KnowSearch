import { addChartTitle, unitMap } from "../config";

const indexConfigClassifyList: string[] = ["查询模板性能配置"];

export const indexConfigData = {
  queryDslCount: {
    name: "查询模板访问量",
    unit: unitMap.mins,
    title: function () {
      return this.name + "(次/min)";
    },
    classify: indexConfigClassifyList[0],
    price: "Gateway视角查询模板级请求速率",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间相应应用对于dslTemplateMd5字段的指定时间间隔的查询次数/指定时间间隔min",
  },
  queryDslTotalCost: {
    name: "查询模板访问耗时",
    unit: unitMap.ms,
    title: function () {
      return this.name + "(ms)";
    },
    classify: indexConfigClassifyList[0],
    price: "Gateway视角查询模板级请求平均耗时",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间相应应用对于dslTemplateMd5字段的指定时间间隔的totalCost的总和/指定时间间隔min",
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
