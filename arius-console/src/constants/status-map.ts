import { IStatusMap, IStringMap, ILabelValue, IStringArray } from 'typesPath/base-types';

export const selectAppAuth = [
  // {
  //   label: '管理',
  //   value: 1,
  // },
  {
    label: '读写',
    value: 2,
  }, {
    label: '读',
    value: 3,
  }] as ILabelValue[];

export const ClusterAuth = [
  {
    title: '无权限',
    value: -1,
  },
  {
    title: '管理',
    value: 1,
  }, {
    title: '访问',
    value: 2,
  }] as ILabelValue[];

export const ClusterAuthMaps = {
  [-1]: '无权限',
  0: '超级管理员',
  1: '管理',
  2: '访问',
} as IStatusMap;

export const StatusMap = {
  '-1': 'unknown',
  0: 'green',
  1: 'yellow',
  2: 'red',
}

export const ClusterStatus = [
  {
    title: 'green',
    value: '0',
  },
  {
    title: 'yellow',
    value: '1',
  }, {
    title: 'red',
    value: '2',
  }, {
    title: 'unknown',
    value: '-1'
  }] as ILabelValue[];

export const brokerMetrics = {
  bytesIn: 'Bytes In（MB/ 秒)',
  bytesOut: 'Bytes Out（MB/ 秒)',
  messagesIn: 'Messages In（条)',
  totalFetchRequests: 'Total Fetch Requests（QPS)',
  totalProduceRequests: 'Total Produce Requests（QPS)',
};

export const roleMap = {
  0: '普通用户',
  1: '研发人员',
  2: '运维人员',
} as IStatusMap;

export const roleModel = [{
  text: roleMap[0],
  label: roleMap[0],
  value: 0,
}, {
  text: roleMap[1],
  label: roleMap[1],
  value: 1,
}, {
  text: roleMap[2],
  label: roleMap[2],
  value: 2,
}] as ILabelValue[];

export const deployStatus = {
  1: '正常',
  2: '禁用',
} as IStatusMap;

export const selectModel = [{
  text: '集群模式',
  value: 0,
}, {
  text: '索引模式',
  value: 1,
}] as ILabelValue[];

export const PLUG_RADIO_LIST = [{
  label: '默认插件',
  value: 1,
}, {
  label: '自定义插件',
  value: 2,
}] as ILabelValue[];

export const weekOptions = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 0 },
];

export const orderStatusMap = {
  0: '待审批',
  1: '已通过',
  2: '已驳回',
  3: '已撤回',
} as IStatusMap;

export const authStatusMap = {
  0: '无权限',
  1: '消费',
  2: '发送',
  3: '发送、消费',
  4: '管理',
} as IStatusMap;

export const clusterTypeMap = {
  1: '共享集群',
  2: '独立集群',
  3: '独享集群',
} as IStatusMap;

export const logicClusterType = [
  { value: 1, title: '共享集群' },
  { value: 2, title: '独立集群' },
  { value: 3, title: '独享集群' }
]

//1:日志；2:上报；3:rds数据；4:离线导入数据
export const INDEX_DATA_TYPE_MAP = {
  0: '系统数据',
  1: '日志数据',
  2: '用户上报数据',
  3: 'RDS数据',
  // 4: '安全部数据',
  // 5: 'SODA数据',
  4: '离线导入数据',
} as IStatusMap;

export const INDEX_AUTH_TYPE_MAP = {
  [-1]: '无权限',
  1: '管理',
  2: '读/写',
  3: '读',
} as IStatusMap;

export const INDEX_AUTH_TYPE_ARR = [-1, 1, 2, 3];

export const VERSION_MAINFEST_TYPE = {
  3: 'docker',
  4: 'host',
  5: 'vm',
} as IStatusMap;

export const TASK_STATUS_TYPE_MAP = {
  waiting: '待执行',
  running: '执行中',
  pause: '已暂停',
  cancel: '已取消',
  ignore: '忽略',
  success: '成功',
  failed: '失败',
  timeout: '超时',
  unknown: 'unknown',
} as IStringMap;

export const DCDR_TASK_STATUS_TYPE_MAP = {
  0: 'success', // 执行成功
  1: 'failed', // 执行失败
  2: 'running', // 执行中
  3: 'waiting', // 等待
  4: 'pause', // 暂停
  5: 'cancel', // 取消
  99: '未知',
} as IStatusMap;

export const TASK_TYPE_MAP = {
  1: '集群新增',
  2: '集群扩容',
  3: '集群缩容',
  4: '集群重启',
  5: '集群升级',
  6: '集群插件操作',
  10: '模版DCDR任务',
} as IStatusMap;
export const appTemplateAuthEnum = [
  // { label: '管理', value: 1 },
  { label: '读/写', value: 2 },
  { label: '读', value: 3 },
];

export const equalList = [
  { label: '大于', value: '>' },
  { label: '小于', value: '<' },
  { label: '等于', value: '=' },
  { label: '大于等于', value: '>=' },
  { label: '小于等于', value: '<=' },
  { label: '不等于', value: '!=' },
];

export const funcList = [
  { label: '周期发生-happen', value: 'happen' },
  { label: '连续发生-all', value: 'all' },
  { label: '同比变化率-c_avg_rate_abs', value: 'c_avg_rate_abs' },
  { label: '突增突降值-diff', value: 'diff' },
  { label: '突增突降率-pdiff', value: 'pdiff' },
  { label: '求和-sum', value: 'sum' },
];

export const funcKeyMap = {
  happen: ['period', 'count'],
  ndiff: ['period', 'count'],
  c_avg_rate_abs: ['period', 'day'],
  all: ['period'],
  diff: ['period'],
  pdiff: ['period'],
  sum: ['period'],
} as IStringArray;

export const filterList = [
  { label: '集群', value: 'clusterName' },
  { label: 'Topic', value: 'topic' },
  { label: 'Location', value: 'loaction' },
  { label: '消费组', value: 'consumerGroup' },
] as ILabelValue[];

export const TASK_TYPE_MAP_LIST = [
  { value: 0, text: '全部', label: '全部' },
  { value: 1, text: '集群新增', label: '集群新增' },
  { value: 2, text: '集群扩容', label: '集群扩容' },
  { value: 3, text: '集群缩容', label: '集群缩容' },
  { value: 4, text: '集群重启', label: '集群重启' },
  { value: 5, text: '集群升级', label: '集群升级' },
] as ILabelValue[];

export const NODE_TYPE_MAP = [
  { value: 'masternode', text: 'master-node' },
  { value: 'clientnode', text: 'client-node' },
  { value: 'datanode', text: 'data-node' },
  // { value: 'datanode-ceph', text: 'datanode-ceph' },
] as ILabelValue[];

export const PHY_NODE_TYPE = ['masternode', 'clientnode', 'datanode', 'coldnode'];

export const STAUS_TYPE_MAP = [
  { value: 'waiting', text: '待执行' },
  { value: 'running', text: '执行中' },
  { value: 'pause', text: '已暂停' },
  { value: 'cancel', text: '已取消' },
  { value: 'ignore', text: '忽略' },
  { value: 'success', text: '成功' },
  { value: 'failed', text: '失败' },
] as ILabelValue[];

export const FORCED_EXPANSION_MAP = [
  { label: '强制', value: 1 },
  { label: '不强制', value: 0 },
] as ILabelValue[];

export const CONTROL_TYPE = [
  { label: '集群', value: 'cluster' },
  { label: '索引', value: 'template' },
] as ILabelValue[];

export const PHY_CLUSTER_TYPE = [
  { value: 4, label: 'host', title: 'host' },
  { value: 3, label: 'docker', title: 'docker' },
] as ILabelValue[];

export const CLUSTER_INDECREASE_TYPE = [
  { value: 2, label: '扩容' },
  { value: 3, label: '缩容' },
] as ILabelValue[];

export const filterKeys = ['cluster', 'index'];

export const SPIT_STYLE_MAP = [
  {
    type: 'success',
    color: '#10C038',
    back: 'success-back',
    text: '成功',
  }, {
    type: 'failed',
    color: '#F04134',
    back: 'failed-back',
    text: '失败',
  }, {
    type: 'pause',
    color: '#FF931D',
    back: 'pause-back',
    text: '已暂停',
  }, {
    type: 'running',
    color: '#337DFF',
    back: 'running-back',
    text: '执行中',
  }, {
    type: 'waiting',
    color: '#BFBFBF',
    back: 'waiting-back',
    text: '待执行',
  }, {
    type: 'cancel',
    color: '#333',
    back: 'cancel-back',
    text: '已取消',
  },
];

export const NODE_NUMBER_MAP = [
  { value: 2 },
  { value: 4 },
  { value: 6 },
  { value: 8 },
  { value: 10 },
  { value: 20 },
];

export const opTemplateIndexRoleMap: IStatusMap = {
  0: 'unknown',
  1: 'master',
  2: 'slave',
  3: '故障',
};

export const openSourceTip: string = '该功能仅面向商业版客户开放';

export const queryFormText: { searchText: string, resetText: string } = {
  searchText: '查询',
  resetText: '重置'
};

export const TOP_MAP = [
  { value: 5, label: 'Top5' },
  { value: 10, label: 'Top10' },
  { value: 15, label: 'Top15' },
  { value: 20, label: 'Top20' },
];

export const DCDR_STATE_MAP = {
  0: 'cancel',
  1: 'success',
  2: 'running',
  3: 'failed',
  4: 'waiting',
}

export const filtersHasDCDR = [
  { text: "是", value: true },
  { text: "否", value: false },
];

export const RESOURCE_TYPE_MAP = {
  5: '信创(tce)',
  4: 'vmware',
  3: 'acs',
} as IStatusMap;

export const INPUT_RULE_MAP = {
  '0': '自动获取',
  '1': '全量录入',
} as IStatusMap;