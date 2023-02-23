// 控制开源环境下部分功能的隐藏和禁用 true 禁用 false 启用
export const isOpenUp = false;

export const CURRENT_PROJECT_KEY = "current-project";
export const PERMISSION_TREE = "permission-tree";

// 索引模板服务，开源环境下展示分类
// 分类: 0 隐藏 1 开源正常展示 2 能力开发中 3 该功能仅面向商业版客户开放
export const showTag = {
  Pipeline: 0,
  索引别名: 0,
  Shard调整: 0,
  预创建: 1,
  过期删除: 1,
  Mapping设置: 1,
  安全管控: 1,
  冷热分离: 2,
  Setting设置: 2,
  资源管控: 3,
  写入限流: 3,
  容量规划: 3,
  "跨集群同步(DCDR)": 3,
};

export const oneDayMillims = 24 * 60 * 60 * 1000;

export const urlPrefix = "";

export const colorTheme = "#526ecc";

export const REGION_LIST = [
  {
    label: "中国机房",
    value: "cn",
  },
];

export const WIKI_LIST = [
  {
    label: "操作手册",
    href: "http://wiki.intra.xiaojukeji.com/pages/viewpage.action?pageId=500193248",
    key: "w1",
  },
  {
    label: "用户指南",
    href: "http://wiki.intra.xiaojukeji.com/pages/viewpage.action?pageId=343934369",
    key: "w2",
  },
  {
    label: "常见问答",
    href: "http://wiki.intra.xiaojukeji.com/pages/viewpage.action?pageId=120871076",
    key: "w3",
  },
];

export const RESOURCE_TYPE_LIST = [
  { value: 1, label: "共享集群" },
  { value: 2, label: "独立集群" },
  { value: 3, label: "独享集群" },
];

export const LEVEL_MAP = [
  { value: 1, label: "核心" },
  { value: 2, label: "重要" },
  { value: 3, label: "一般" },
];

export const NEW_RESOURCE_TYPE_LIST = [
  { value: 1, label: "共享集群" },
  { value: 2, label: "独立集群" },
  // { value: 3, label: '独享集群' }
];

export const LEVEL_LIST = [
  {
    label: "normal",
    value: 1,
  },
  {
    label: "important",
    value: 2,
  },
  {
    label: "vip",
    value: 3,
  },
];

export const IS_ROOT = [
  {
    label: "不是 ",
    value: 0,
  },
  {
    label: "是",
    value: 1,
  },
];

export const SEARCH_TYPE = [
  {
    label: "索引模式 ",
    value: 1,
  },
  {
    label: "集群模式",
    value: 2,
  },
];

export const ROLE_TYPE = [
  { value: -1, label: "unknown" },
  { value: 1, label: "data_node" },
  { value: 2, label: "client_node" },
  { value: 3, label: "master_node" },
  { value: 4, label: "ml_node" },
];

export const ROLE_TYPE_NO = [
  { value: -1, label: "unknown" },
  { value: 1, label: "data" },
  { value: 2, label: "client" },
  { value: 3, label: "master" },
  { value: 4, label: "ml" },
];

export const BOOLEAN_LIST = [
  { value: "false", label: "是" },
  { value: "true", label: "否" },
];

export enum CodeType {
  PreCreate = 1,
  Pipeline = 2,
  Rollover = 3,
  Delete = 4,
  Separate = 5,
  Clear = 6,
  UpgradeVersion = 7,
  UpdateCapacity = 8,
  DCDR = 10,
  Translog = 17,
  // TODO 恢复优先级的code没定，先用英文代替，联调是根据实际接口再定
  Priority = "Priority",
}

export const CONFIRM_BUTTON_TEXT = {
  okText: "确定",
  cancelText: "取消",
};
export const DIVIDE_TYPE = [
  { value: "host", label: "nodename" },
  { value: "attribute", label: "attribute" },
];

export const ATTRIBUTE = [{ value: "rack", label: "rack" }];

export const GATEWAY_UNABLE_TIP = "未部署Gateway集群，不具备此能力";
export const DATA_TYPE_LIST = [
  { value: 1, label: "索引模板" },
  { value: 2, label: "索引" },
];

export const SOURCE_CLUSTER_TYPE = [{ value: 1, label: "ES" }];

export const TEMPLATE_RELATION_TYPE = [{ value: 2, label: "One To One" }];

export const INDEX_RELATION_TYPE = [
  { value: 1, label: "All To One" },
  { value: 2, label: "One To One" },
];

export const WRITE_TYPE = [
  { value: 1, label: "index_with_id" },
  { value: 2, label: "index" },
  { value: 3, label: "create" },
];

export const TRANSFER_TEMPLATE = [
  { value: "1", label: "是" },
  { value: "0", label: "否" },
];

export const PACKAGE_TYPE = [
  { value: 1, label: "es安装包" },
  { value: 2, label: "gateway安装包" },
  { value: 3, label: "es引擎插件" },
  { value: 4, label: "gateway引擎插件" },
  { value: 5, label: "es平台插件" },
  { value: 6, label: "gateway平台插件" },
];
