// 控制开源环境下部分功能的隐藏和禁用
export const isOpenUp = true;

// 索引模板服务，开源环境下展示分类
// 分类: 0 隐藏 1 开源正常展示 2 能力开发中 3 该功能仅面向商业版客户开放
export const showTag = {
  "Pipeline": 0,
  "索引别名": 0,
  "Shard调整": 0,
  "预创建": 1,
  "过期删除": 1,
  "Mapping设置": 1,
  "安全管控": 1,
  "冷热分离": 2,
  "Setting设置": 2,
  "资源管控": 3,
  "写入限流": 3,
  "容量规划": 3,
  "跨集群同步(DCDR)": 3,
} 

export const oneDayMillims = 24 * 60 * 60 * 1000;

export const urlPrefix = '';

export const colorTheme = '#526ecc';

export const REGION_LIST = [{
  label: '中国机房',
  value: 'cn',
}];

export const WIKI_LIST = [
  {
    label: '操作手册',
    href: '',
    key: 'w1',
  },
  {
    label: '用户指南',
    href: '',
    key: 'w2',
  },
  {
    label: '常见问答',
    href: '',
    key: 'w3',
  },
];

export const RESOURCE_TYPE_LIST = [
  { value: 1, label: '共享集群' },
  { value: 2, label: '独立集群' },
  { value: 3, label: '独享集群' }
];

export const LEVEL_LIST = [{
  label: 'normal',
  value: 1,
}, {
  label: 'important',
  value: 2,
}, {
  label: 'vip',
  value: 3,
}];

export const DATA_TYPE_LIST = [
  { value: 1, label: '日志数据' },
  { value: 2, label: '用户上报数据' },
  { value: 3, label: 'RDS数据' },
  { value: 6, label: '离线导入数据' },
];

export const IS_ROOT = [{
  label: '不是 ',
  value: 0,
}, {
  label: '是',
  value: 1,
}];

export const SEARCH_TYPE = [{
  label: '索引模式 ',
  value: 1,
}, {
  label: '集群模式',
  value: 2,
}];

export const ROLE_TYPE = [
  { value: -1, label: 'unknown' },
  { value: 1, label: 'data_node' },
  { value: 2, label: 'client_node' },
  { value: 3, label: 'master_node' },
  { value: 4, label: 'tribe_node' },
];

export const ROLE_TYPE_NO = [
  { value: -1, label: 'unknown' },
  { value: 1, label: 'data' },
  { value: 2, label: 'client' },
  { value: 3, label: 'master' },
  { value: 4, label: 'tribe' },
];