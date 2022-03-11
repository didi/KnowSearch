import React from "react";
import { Tag, Popover, Button, Tooltip } from "antd";
import { getLineOption, unitMap } from "./config";
import _ from "lodash";

// 指标配置 start
export const indexConfigClassifyList: string[] = [
  "系统指标",
  "性能指标",
  "状态指标",
];

//黄金配置
export const goldConfig = {
  "系统指标": [
    'cpuUsage',
    'nodesForDiskUsageGte75Percent',
  ],
  "性能指标": [
    'readTps',
    'writeTps',
    'searchLatency',
    'indexingLatency',
  ],
  "状态指标": [
    'shardNu',
    'bigShards',
  ],
}

export const indexConfigData = {
  cpuUsage: {
    name: "CPU使用率",
    classify: indexConfigClassifyList[0],
  },
  cpuLoad1M: {
    name: "CPU 1分钟负载",
    classify: indexConfigClassifyList[0],
  },
  cpuLoad5M: {
    name: "CPU 5分钟负载",
    classify: indexConfigClassifyList[0],
  },
  cpuLoad15M: {
    name: "CPU 15分钟负载",
    classify: indexConfigClassifyList[0],
  },
  diskUsage: {
    name: "磁盘使用率",
    classify: indexConfigClassifyList[0],
  },
  diskInfo: {
    name: "磁盘使用情况",
    classify: indexConfigClassifyList[0],
  },
  nodesForDiskUsageGte75Percent: {
    name: "磁盘利用率大于75%节点列表",
    classify: indexConfigClassifyList[0],
  },
  // 特殊的指标前端做合并
  networkFlow: {
    name: "网络流量",
    types: ['recvTransSize', 'sendTransSize'],
    classify: indexConfigClassifyList[0],
  },
  // recvTransSize: {
  //   name: "网络入口流量",
  //   classify: indexConfigClassifyList[0],
  // },
  // sendTransSize: {
  //   name: "网络出口流量",
  //   classify: indexConfigClassifyList[0],
  // },
  readTps: {
    name: "查询QPS",
    classify: indexConfigClassifyList[1],
  },
  writeTps: {
    name: "写入TPS",
    classify: indexConfigClassifyList[1],
  },
  searchLatency: {
    name: "查询耗时",
    classify: indexConfigClassifyList[1],
  },
  indexingLatency: {
    name: "写入耗时",
    classify: indexConfigClassifyList[1],
  },
  taskCost: {
    name: "执行任务耗时",
    classify: indexConfigClassifyList[1],
  },
  taskCount: {
    name: "执行任务数量",
    classify: indexConfigClassifyList[1],
  },
  shardNu: {
    name: "shard数量",
    classify: indexConfigClassifyList[2],
  },
  movingShards: {
    name: "迁移中shard列表",
    classify: indexConfigClassifyList[2],
  },
  bigShards: {
    name: "大小超过50GB的shard列表",
    classify: indexConfigClassifyList[2],
  },
  bigIndices: {
    name: "大于20亿文档数索引列表",
    classify: indexConfigClassifyList[2],
  },
  invalidNodes: {
    name: "Dead节点列表",
    classify: indexConfigClassifyList[2],
  },
  pendingTasks: {
    name: "PendingTask列表",
    classify: indexConfigClassifyList[2],
  },
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

export const defaultIndexConfigList: defaultIndexConfigItemPropsType[] =
  indexConfigClassifyList.map((item) => ({
    title: item,
    plainOptions: allCheckedData[item].map((item) => ({
      label: indexConfigData[item].name,
      value: item,
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
  isMarkLine: boolean = true
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

  return getLineOption({title, xAxisData, series, unitMap, isMoreDay, isMarkLine});
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
  return Object.keys(obj).filter(item => item !== 'timeStamp');
}

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

// cpu 5分钟负载
const cpuLoad5MData = _.cloneDeep(legendInfo);

const cpuLoad5MList = getObjKeys(cpuLoad5MData);

const cpuLoad5MObj = {
  title: "CPU 5分钟负载",
  data: cpuLoad5MData,
  list: cpuLoad5MList,
};

// cpu 15分钟负载
const cpuLoad15MData = _.cloneDeep(legendInfo);

const cpuLoad15MList = getObjKeys(cpuLoad15MData);

const cpuLoad15MObj = {
  title: "CPU 15分钟负载",
  data: cpuLoad15MData,
  list: cpuLoad15MList,
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

// 集群shard数
export const shardNuData: lineDataType = {
  shardNu: {
    name: "Shard总数",
    data: [],
  },
  unAssignedShards: {
    name: "未分配Shard总数",
    data: [],
  },
  timeStamp: {
    name: "时间戳",
    data: [],
  },
};

export const shardNuList = getObjKeys(shardNuData);

export const shardNuObj = {
  title: "shard数量(个)",
  data: shardNuData,
  list: shardNuList,
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
  title: "写入TPS(个/s)",
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
  title: "网络流量(MB/s)",
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
  title: "查询耗时(ms)",
  data: searchLatencyData,
  list: searchLatencyList,
};

// task 耗时taskCost
export const taskCostData = _.cloneDeep(legendInfo);

export const taskCostList = getObjKeys(taskCostData);

export const taskCostObj = {
  title: "执行任务耗时(ms)",
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
  title: "查询QPS(个/s)",
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
  title: "执行任务数量(个/s)",
  data: taskCountData,
  list: taskCountList,
};

// 写入耗时
export const indexingLatencyData = _.cloneDeep(legendInfo);

export const indexingLatencyList = getObjKeys(indexingLatencyData);

export const indexingLatencyObj = {
  title: "写入耗时(ms)",
  data: indexingLatencyData,
  list: indexingLatencyList,
};

// shard
export const movingShardColumns = [
  {
    title: "Shard序号",
    dataIndex: "s",
    key: "s",
  },
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
    title: "状态",
    dataIndex: "st",
    key: "st",
  },
  {
    title: "耗时",
    dataIndex: "t",
    key: "t",
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
  },
];

const columnTagShow = (nums) => {
  if (!nums || !Array.isArray(nums)) {
    return "---";
  }
  return (
    <>
      {nums.slice(0, 3).map((item) => (
        <Tag key={item.node}>{item.node}</Tag>
      ))}
      <Popover
        placement="bottom"
        content={
          <div className={"table-popover-content"}>
            {nums.map((item, index) => (
              <Tag key={index}>{item.node}</Tag>
            ))}
          </div>
        }
        trigger="hover"
      >
        <Button size="small" type="dashed">
          共{nums.length}个
        </Button>
      </Popover>
    </>
  );
};

export const bigIndicesColumns = [
  {
    title: "索引名",
    dataIndex: "indexName",
    key: "indexName",
  },
  {
    title: "分布的节点",
    dataIndex: "belongNodeIp",
    key: "belongNodeIp",
    render: function (item) {
      return columnTagShow(item);
    },
  },
];

export const invalidNodesColumns = [
  {
    title: "Dead的节点",
    dataIndex: "deadNode",
    key: "deadNode",
  },
];

export const nodesForDiskUsageGte75PercentColumns = [
  {
    title: "节点名称",
    dataIndex: "nodeName",
    key: "nodeName",
  },
  {
    title: "节点IP",
    dataIndex: "nodeIp",
    key: "nodeIp",
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
  cpuLoad5M: {
    type: LINE,
    info: cpuLoad5MObj,
    unit: unitMap.none,
  },
  cpuLoad15M: {
    type: LINE,
    info: cpuLoad15MObj,
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
  shardNu: {
    type: LINE,
    info: shardNuObj,
    unit: unitMap.count,
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
    unit: unitMap.ms,
  },
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
      shardColumnEllipsis({
        key: index + "" + item.shardNu + item.belongIndex + item.sourceNodeIp,
        s: item.s,
        i: item.i,
        shost: item.shost,
        thost: item.thost,
        st: item.st,
        t: item.t,
      }),
  },
  bigShards: {
    type: SHARD,
    title: "大小超过50GB的shard列表",
    shardColumn: bigShardColumns,
    mapFn: (item, index) =>
      shardColumnEllipsis({
        key: index + "" + item.shardNu + item.belongIndex,
        shard: item.shard,
        index: item.index,
        prirep: item.prirep,
        node: item.node,
        ip: item.ip,
        store: item.store,
      }),
  },
  bigIndices: {
    type: SHARD,
    title: "大于10亿文档数索引列表",
    shardColumn: bigIndicesColumns,
    mapFn: (item, index) =>
      shardColumnEllipsis(
        {
          key: index,
          indexName: item.indexName,
          belongNodeIp: item.belongNodeInfo,
        },
        45
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
  nodesForDiskUsageGte75Percent: {
    type: SHARD,
    title: "磁盘利用率大于75%节点列表",
    shardColumn: nodesForDiskUsageGte75PercentColumns,
    mapFn: (item, index) =>
      shardColumnEllipsis(
        {
          key: index,
          nodeIp: item.nodeIp,
          nodeName: item.nodeName,
        },
        45
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
