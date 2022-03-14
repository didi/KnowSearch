import { addChartTitle, unitMap } from "../config";

const indexConfigClassifyList: string[] = ["查询模板性能配置"];

export const indexConfigData = {
  queryDslCount: {
    name: "查询模版访问量",
    unit: unitMap.countM,
    title: function () {
      return this.name + "(个/min)";
    },
    classify: indexConfigClassifyList[0],
  },
  queryDslTotalCost: {
    name: " 查询模版访问耗时",
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
