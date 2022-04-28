import { addChartTitle, unitMap } from "../config";

// 指标分类
const indexConfigClassifyList: string[] = ["索引性能指标"];

// 指标项
export const indexConfigData = {
  writeIndexCount: {
    name: "gateway对索引写入量",
    unit: unitMap.itemMin,
    classify: indexConfigClassifyList[0],
  },
  writeIndexTotalCost: {
    name: "gateway对索引写入耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[0],
  },
  searchIndexCount: {
    name: "索引查询量分布",
    unit: unitMap.itemMin,
    classify: indexConfigClassifyList[0],
  },
  searchIndexTotalCost: {
    name: "索引平均查询耗时",
    unit: unitMap.ms,
    classify: indexConfigClassifyList[0],
  }
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
