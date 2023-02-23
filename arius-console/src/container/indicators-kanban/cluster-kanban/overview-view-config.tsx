import React from "react";
import { Tag, Popover, Button, Tooltip } from "antd";
import { getLineOption, unitMap } from "./config";
import { formatDecimalPoint } from "lib/utils";
import _ from "lodash";

// 指标配置 start
export const indexConfigClassifyList: string[] = ["系统指标", "性能指标", "状态指标"];

export const indexConfigData = {
  cpuUsage: {
    name: "CPU使用率",
    classify: indexConfigClassifyList[0],
    indicatorType: [1, 2, 3],
  },
  cpuLoad1M: {
    name: "CPU 1分钟负载",
    classify: indexConfigClassifyList[0],
    indicatorType: [2, 3],
  },
  diskUsage: {
    name: "磁盘使用率",
    classify: indexConfigClassifyList[0],
    indicatorType: [1, 4],
  },
  diskInfo: {
    name: "磁盘使用情况",
    classify: indexConfigClassifyList[0],
    indicatorType: [],
  },
  // 特殊的指标前端做合并
  networkFlow: {
    name: "网络流量",
    types: ["recvTransSize", "sendTransSize"],
    classify: indexConfigClassifyList[0],
    indicatorType: [],
  },
  // recvTransSize: {
  //   name: "网络入口流量",
  //   classify: indexConfigClassifyList[0],
  //   indicatorType: [],
  // },
  // sendTransSize: {
  //   name: "网络出口流量",
  //   classify: indexConfigClassifyList[0],
  //   indicatorType: [],
  // },
  readTps: {
    name: "查询QPS",
    classify: indexConfigClassifyList[1],
    indicatorType: [1],
  },
  writeTps: {
    name: "写入TPS",
    classify: indexConfigClassifyList[1],
    indicatorType: [1],
  },
  searchLatency: {
    name: "查询耗时",
    classify: indexConfigClassifyList[1],
    indicatorType: [1, 2],
  },
  indexingLatency: {
    name: "写入耗时",
    classify: indexConfigClassifyList[1],
    indicatorType: [1, 2],
  },
  taskCost: {
    name: "执行任务耗时",
    classify: indexConfigClassifyList[1],
    indicatorType: [1, 2, 3, 4],
  },
  taskCount: {
    name: "执行任务数量",
    classify: indexConfigClassifyList[1],
    indicatorType: [1, 2, 3, 4],
  },
  movingShards: {
    name: "迁移中shard列表",
    classify: indexConfigClassifyList[2],
    indicatorType: [1, 2, 3, 4],
  },
  unAssignShards: {
    name: "未分配Shard列表",
    classify: indexConfigClassifyList[2],
    indicatorType: [1, 2, 3, 4],
  },
  invalidNodes: {
    name: "Dead节点列表",
    classify: indexConfigClassifyList[2],
    indicatorType: [1, 2, 3, 4],
  },
  pendingTasks: {
    name: "PendingTask列表",
    classify: indexConfigClassifyList[2],
    indicatorType: [1, 2, 3, 4],
  },
  // elapsedTime: {
  //   name: "_cluster_stats 接口平均采集耗时",
  //   classify: indexConfigClassifyList[1],
  // }
};

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
    indicatorType: indexConfigData[item].indicatorType,
  })),
}));
// 指标配置 end

// 折线图
interface dataType {
  [key: string]: number;
}
interface lineDataType {
  [key: string]: {
    name: string;
    data: number[];
  };
}

export const getOverviewOption = (
  title: string,
  data: dataType[],
  lineData: lineDataType,
  lineSeriesKeys: string[],
  unitMap: {},
  isMoreDay: boolean = false,
  isMarkLine: boolean = false
) => {
  data.sort((a, b) => a.timeStamp - b.timeStamp);

  data.forEach((item) => {
    for (let key in item) {
      if (lineData[key]) {
        lineData[key].data.push(item[key]);
      }
    }
  });

  const xAxisData = [...new Set(lineData.timeStamp.data)];

  const series = lineSeriesKeys.map((item) => lineData[item]);

  return getLineOption({ title, xAxisData, series, unitMap, isMoreDay, isMarkLine });
};

const legendInfo = {
  aggType: {
    name: "平均分位值",
    data: [],
  },
  st99: {
    name: "99分位值",
    data: [],
  },
  st95: {
    name: "95分位值",
    data: [],
  },
  st75: {
    name: "75分位值",
    data: [],
  },
  st55: {
    name: "55分位值",
    data: [],
  },
  timeStamp: {
    name: "时间戳",
    data: [],
  },
};

const getObjKeys = (obj) => {
  return Object.keys(obj).filter((item) => item !== "timeStamp");
};

// cpu 使用率
export const cpuUsageData: lineDataType = _.cloneDeep(legendInfo);

export const cpuUsageList = getObjKeys(cpuUsageData);

export const cpuUsageObj = {
  title: "CPU 使用率(%)",
  data: cpuUsageData,
  list: cpuUsageList,
};

// cpu 1分钟负载
const cpuLoad1MData = _.cloneDeep(legendInfo);

const cpuLoad1MList = getObjKeys(cpuLoad1MData);

const cpuLoad1MObj = {
  title: "CPU 1分钟负载",
  data: cpuLoad1MData,
  list: cpuLoad1MList,
};

// 磁盘使用率
export const diskUsageData: lineDataType = _.cloneDeep(legendInfo);

export const diskUsageList = getObjKeys(diskUsageData);

export const diskUsageObj = {
  title: "磁盘使用率(%)",
  data: diskUsageData,
  list: diskUsageList,
};

// 磁盘使用情况(GB)
export const diskInfoData: lineDataType = {
  freeStoreSize: {
    name: "磁盘空闲量",
    data: [],
  },
  storeSize: {
    name: "磁盘使用量",
    data: [],
  },
  totalStoreSize: {
    name: "磁盘总量",
    data: [],
  },
  timeStamp: {
    name: "时间戳",
    data: [],
  },
};

export const diskInfoList = getObjKeys(diskInfoData);

export const diskInfoObj = {
  title: "磁盘使用情况(GB)",
  data: diskInfoData,
  list: diskInfoList,
};

// 写入TPS
export const writeTpsData: lineDataType = {
  writeTps: {
    name: "写入TPS",
    data: [],
  },
  timeStamp: {
    name: "时间戳",
    data: [],
  },
};

export const writeTpsList = getObjKeys(writeTpsData);

export const writeTpsObj = {
  title: "写入TPS(个/S)",
  data: writeTpsData,
  list: writeTpsList,
};

// 网络出口流量
export const networkFlowData = {
  recvTransSize: {
    name: "网络入口流量",
    data: [],
  },
  sendTransSize: {
    name: "网络出口流量",
    data: [],
  },
  timeStamp: {
    name: "时间戳",
    data: [],
  },
};

export const networkFlowList = getObjKeys(networkFlowData);

export const networkFlowObj = {
  title: "网络流量(MB/S)",
  data: networkFlowData,
  list: networkFlowList,
};

// // 网络入口流量
// export const recvTransSizeData = {
//   recvTransSize: {
//     name: "网络入口流量",
//     data: [],
//   },
//   timeStamp: {
//     name: "时间戳",
//     data: [],
//   },
// };

// export const recvTransSizeList = getObjKeys(recvTransSizeData);

// export const recvTransSizeObj = {
//   title: "网络入口流量(MB/s)",
//   data: recvTransSizeData,
//   list: recvTransSizeList,
// };

// 查询耗时
export const searchLatencyData = _.cloneDeep(legendInfo);

export const searchLatencyList = getObjKeys(searchLatencyData);

export const searchLatencyObj = {
  title: "查询耗时(MS)",
  data: searchLatencyData,
  list: searchLatencyList,
};

// task 耗时taskCost
export const taskCostData = _.cloneDeep(legendInfo);

export const taskCostList = getObjKeys(taskCostData);

export const taskCostObj = {
  title: "执行任务耗时(S)",
  data: taskCostData,
  list: taskCostList,
};

// 查询QPS
export const readTpsData = {
  readTps: {
    name: "查询QPS",
    data: [],
  },
  timeStamp: {
    name: "时间戳",
    data: [],
  },
};

export const readTpsList = getObjKeys(readTpsData);

export const readTpsObj = {
  title: "查询QPS(次/S)",
  data: readTpsData,
  list: readTpsList,
};

// taskCount task数量趋势
export const taskCountData = {
  taskCount: {
    name: "执行任务数量",
    data: [],
  },
  timeStamp: {
    name: "时间戳",
    data: [],
  },
};

export const taskCountList = getObjKeys(taskCountData);

export const taskCountObj = {
  title: "执行任务数量(个/S)",
  data: taskCountData,
  list: taskCountList,
};

// export const elapsedTimeData: lineDataType = {
//   elapsedTime: {
//     name: "采集耗时",
//     data: [],
//   },
//   timeStamp: {
//     name: "时间戳",
//     data: [],
//   },
// };

// export const elapsedTimeList = getObjKeys(elapsedTimeData);

// export const elapsedTimeObj = {
//   title: "_cluster_stats 接口平均采集耗时(ms)",
//   data: elapsedTimeData,
//   list: elapsedTimeList,
// };

// 写入耗时
export const indexingLatencyData = _.cloneDeep(legendInfo);

export const indexingLatencyList = getObjKeys(indexingLatencyData);

export const indexingLatencyObj = {
  title: "写入耗时(MS)",
  data: indexingLatencyData,
  list: indexingLatencyList,
};

// shard
export const movingShardColumns = [
  {
    title: "承载索引",
    dataIndex: "i",
    key: "i",
  },
  {
    title: "源节点IP",
    dataIndex: "shost",
    key: "shost",
  },
  {
    title: "目标节点IP",
    dataIndex: "thost",
    key: "thost",
  },
  {
    title: "恢复的字节数",
    dataIndex: "br",
    key: "br",
  },
  {
    title: "字节数占比",
    dataIndex: "bp",
    key: "bp",
  },
  {
    title: "转换日志操作占比",
    dataIndex: "top",
    key: "top",
  },
];

export const unassignShardColumns = [
  {
    title: "归属索引",
    dataIndex: "index",
    key: "index",
  },
  {
    title: "shard标识",
    dataIndex: "shard",
    key: "shard",
  },
  {
    title: "主/备",
    dataIndex: "prirep",
    key: "prirep",
  },
  {
    title: "状态",
    dataIndex: "state",
    key: "state",
  },
];

export const bigShardColumns = [
  {
    title: "Shard序号",
    dataIndex: "shard",
    key: "shard",
  },
  {
    title: "承载索引",
    dataIndex: "index",
    key: "index",
  },
  {
    title: "主/备",
    dataIndex: "prirep",
    key: "prirep",
  },
  {
    title: "所属节点",
    dataIndex: "node",
    key: "node",
  },
  {
    title: "所属Ip",
    dataIndex: "ip",
    key: "ip",
  },
  {
    title: "容量",
    dataIndex: "store",
    key: "store",
    render: (text) => formatDecimalPoint(text),
  },
];

export const invalidNodesColumns = [
  {
    title: "节点IP",
    dataIndex: "ip",
    key: "ip",
    render: (_, record) => {
      return record?.deadNode?.ip || "-";
    },
  },
  {
    title: "主机名",
    dataIndex: "hostname",
    key: "hostname",
    render: (_, record) => {
      return record?.deadNode?.hostname || "-";
    },
  },
  {
    title: "实例名",
    dataIndex: "cluster",
    key: "cluster",
    render: (_, record) => {
      return record?.deadNode?.cluster || "-";
    },
  },
];

export const pendingTasksColumns = [
  {
    title: "插入顺序",
    dataIndex: "insertOrder",
    key: "insertOrder",
  },
  {
    title: "优先级",
    dataIndex: "priority",
    key: "priority",
  },
  {
    title: "任务来源说明",
    dataIndex: "source",
    key: "source",
  },
  {
    title: "执行任务前等待时间",
    dataIndex: "timeInQueue",
    key: "timeInQueue",
  },
];

export const LINE = "line";
export const SHARD = "shard";
interface metricsDataType {
  [key: string]: {
    type: string;
    info?: any;
    title?: string;
    shardColumn?: any;
    mapFn?: any;
    unit?: any;
  };
}

const ellipsis = (str: string | number, num: number = 10) => {
  return String(str).length > num ? (
    <Tooltip placement="top" title={str}>
      {String(str).substring(0, num) + "..."}
    </Tooltip>
  ) : (
    str
  );
};

const shardColumnEllipsis = (map, num = 10) => {
  for (let key in map) {
    if (key !== "key") {
      map[key] = ellipsis(map[key], num);
    }
  }
  return map;
};

export const metricsDataType: metricsDataType = {
  cpuUsage: {
    type: LINE,
    info: cpuUsageObj,
    unit: unitMap.none,
  },
  cpuLoad1M: {
    type: LINE,
    info: cpuLoad1MObj,
    unit: unitMap.none,
  },
  diskUsage: {
    type: LINE,
    info: diskUsageObj,
    unit: unitMap.percent,
  },
  diskInfo: {
    type: LINE,
    info: diskInfoObj,
    unit: unitMap.GB,
  },
  writeTps: {
    type: LINE,
    info: writeTpsObj,
    unit: unitMap.countS,
  },
  // sendTransSize: {
  //   type: LINE,
  //   info: sendTransSizeObj,
  //   unit: unitMap.mbS,
  // },
  searchLatency: {
    type: LINE,
    info: searchLatencyObj,
    unit: unitMap.ms,
  },
  taskCost: {
    type: LINE,
    info: taskCostObj,
    unit: unitMap.s,
  },
  // elapsedTime: {
  //   type: LINE,
  //   info: elapsedTimeObj,
  //   unit: unitMap.ms,
  // },
  networkFlow: {
    type: LINE,
    info: networkFlowObj,
    unit: unitMap.mbS,
  },
  // recvTransSize: {
  //   type: LINE,
  //   info: recvTransSizeObj,
  //   unit: unitMap.mbS,
  // },
  readTps: {
    type: LINE,
    info: readTpsObj,
    unit: unitMap.countS,
  },
  taskCount: {
    type: LINE,
    info: taskCountObj,
    unit: unitMap.countS,
  },
  indexingLatency: {
    type: LINE,
    info: indexingLatencyObj,
    unit: unitMap.ms,
  },
  movingShards: {
    type: SHARD,
    title: "迁移中shard列表",
    shardColumn: movingShardColumns,
    mapFn: (item, index) =>
      shardColumnEllipsis(
        {
          key: index + "" + item.i + item.shost + item.thost,
          i: item.i,
          shost: item.shost,
          thost: item.thost,
          br: item.br,
          bp: item.bp,
          top: item.top,
        },
        6
      ),
  },
  unAssignShards: {
    type: SHARD,
    title: "未分配Shard列表",
    shardColumn: unassignShardColumns,
    mapFn: (item, index) =>
      shardColumnEllipsis(
        {
          key: index + "" + item.shard + item.prirep + item.state,
          index: item.index,
          prirep: item.prirep,
          shard: item.shard,
          state: item.state,
        },
        20
      ),
  },
  invalidNodes: {
    type: SHARD,
    title: "Dead节点列表",
    shardColumn: invalidNodesColumns,
    mapFn: (item, index) =>
      shardColumnEllipsis(
        {
          key: index,
          deadNode: item,
        },
        97
      ),
  },
  pendingTasks: {
    type: SHARD,
    title: "PendingTask列表",
    shardColumn: pendingTasksColumns,
    mapFn: (item, index) =>
      shardColumnEllipsis(
        {
          key: index,
          insertOrder: item.insertOrder,
          priority: item.priority,
          source: item.source,
          timeInQueue: item.timeInQueue,
        },
        20
      ),
  },
};
