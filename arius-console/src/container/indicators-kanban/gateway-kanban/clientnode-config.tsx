import { addChartTitle, unitMap } from "../config";
export const indexConfigClassifyList: string[] = ["ClientNode性能指标"];

export const indexConfigData = {
  queryClientNode: {
    name: "clientNode查询",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[0],
    price: "Gateway视角ES ClientNode查询请求速率",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为true时相应节点对于clientNode字段的指定时间间隔的查询次数/指定时间间隔min",
  },
  writeClientNode: {
    name: "clientNode写入请求量",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[0],
    price: "Gateway视角ES ClientNode写入请求速率",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为false时相应节点对于clientNode字段的指定时间间隔的写入次数/指定时间间隔min",
  },
  dslLen: {
    name: "clientNode写入吞吐量",
    unit: unitMap.characterMin,
    classify: indexConfigClassifyList[0],
    price: "Gateway视角ES ClientNode写入吞吐量",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为false时相应节点对于clientNode字段的指定时间间隔的dslLen的平均长度",
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

export const parseLineParams = (data) => {
  const title = data.nodeMetricsType;
  const xAxis = data.nodesSubMetrics[0].nodeMetricsContentCells.map((item) => item.timeStamp);
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
