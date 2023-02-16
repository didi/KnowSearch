import { addChartTitle, unitMap } from "../config";
export const indexConfigClassifyList: string[] = ["节点性能指标"];

export const indexConfigData = {
  writeGatewayNode: {
    name: "gatewayNode写入请求量",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[0],
    price: "Gateway节点级写入请求速率",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为false时相应节点对于gatewayNode字段的指定时间间隔的写入次数/指定时间间隔min",
  },
  dslLen: {
    name: "gatewayNode写入吞吐量",
    unit: unitMap.characterMin,
    classify: indexConfigClassifyList[0],
    price: "Gateway节点级写入请求吞吐量",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为false时相应节点对于gatewayNode字段的指定时间间隔的dslLen的平均长度",
  },
  queryGatewayNode: {
    name: "gatewayNode查询",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[0],
    price: "Gateway节点级查询请求速率",
    currentCalLogic:
      "[平均值] 聚合查询arius_gateway_join索引获取指定时间queryRequest字段为true时相应节点对于gatewayNode字段的指定时间间隔的查询次数/指定时间隔间min",
  },
  // queryClientNode: {
  //   name: "gateway各clientnode查询分布",
  //   unit: unitMap.numS,
  //   classify: indexConfigClassifyList[0],
  // },
  // writeClientNode: {
  //   name: "gateway各clientnode写入分布",
  //   unit: unitMap.itemMin,
  //   classify: indexConfigClassifyList[0],
  // },
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
