import { addChartTitle, unitMap } from "../config";

const indexConfigClassifyList: string[] = ["项目性能指标"];

export const indexConfigData = {
  queryAppTotalCost: {
    name: "appid查询耗时平均值",
    unit: unitMap.ms,
    title: function () {
      return this.name + "(ms)";
    },
    classify: indexConfigClassifyList[0],
  },
  queryAppCount: {
    name: "gateway各appid查询量",
    unit: unitMap.mins,
    title: function () {
      return this.name + "(次/min)";
    },
    classify: indexConfigClassifyList[0],
  },
  queryAppSearchCost: {
    name: "appid查询总耗时平均值",
    unit: unitMap.ms,
    title: function () {
      return this.name + "(ms)";
    },
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

export const defaultIndexConfigList: defaultIndexConfigItemPropsType[] =
  indexConfigClassifyList.map((item) => ({
    title: item,
    plainOptions: allCheckedData[item].map((item) => ({
      label: indexConfigData[item].name,
      value: item,
    })),
  }));
