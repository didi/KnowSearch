export * from "./config";
import { addChartTitle, unitMap } from "../config";
export const indexConfigClassifyList: string[] = ["节点性能指标"];

export const indexConfigData = {
  writeGatewayNode: {
    name: "gatewayNode写入请求量",
    unit: unitMap.itemMin,
    classify: indexConfigClassifyList[0],
  },
  dslLen: {
    name: "gatewayNode写入吞吐量",
    unit: unitMap.characterMin,
    classify: indexConfigClassifyList[0],
  },
  queryGatewayNode: {
    name: "gatewayNode查询",
    unit: unitMap.mins,
    classify: indexConfigClassifyList[0],
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

export const defaultIndexConfigList: defaultIndexConfigItemPropsType[] =
  indexConfigClassifyList.map((item) => ({
    title: item,
    plainOptions: allCheckedData[item].map((item) => ({
      label: indexConfigData[item].name,
      value: item,
    })),
  }));

export const parseLineParams = (data) => {
  const title = data.nodeMetricsType;
  const xAxis = data.nodesSubMetrics[0].nodeMetricsContentCells.map(
    (item) => item.timeStamp
  );
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
