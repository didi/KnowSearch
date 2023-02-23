export enum Dashboard {
  PAGE = "Dashboard",
}

export enum Grafana {
  PAGE = "Grafana",
}

export enum PhyClusterPermissions {
  PAGE = "物理集群",
  LIST_DETAIL = "查看集群列表及详情",
  ACCESS = "接入集群",
  ADD = "新建集群",
  FAST_INDEX = "数据迁移",
  EDIT = "编辑",
  EXPAND_SHRINK = "扩缩容",
  UPGRADE = "升级",
  RESTART = "重启",
  CONFIG_UPDATE = "配置变更",
  REGION_SET = "Region划分",
  REGION_MANAGE = "Region管理",
  INSTALL_PLUGIN = "插件安装",
  SHORTCUTS = "快捷命令",
  BIND_GATEWAY = "绑定Gateway",
  OFFLINE = "下线",
  BAT = "批量操作",
}

export enum MyClusterPermissions {
  PAGE = "我的集群",
  LIST_DETAIL = "查看集群列表及详情",
  APPLY = "申请集群",
  EDIT = "编辑",
  EXPAND_SHRINK = "扩缩容",
  OFFLINE = "下线",
}

export enum ClusterVersionPermissions {
  PAGE = "集群版本",
  LIST_DETAIL = "查看版本列表",
  ADD = "新增版本",
  EDIT = "编辑",
  DELETE = "删除",
}

export enum GatewayPermissions {
  PAGE = "Gateway管理",
  LIST_DETAIL = "查看Gateway 集群列表",
  ACCESS = "接入gateway",
  ADD = "新建Gateway",
  EDIT = "编辑",
  OFFLINE = "下线",
  EXPAND_SHRINK = "扩缩容",
  UPGRADE = "升级",
  RESTART = "重启",
  ROLLBACK = "回滚",
}

export enum TempletPermissions {
  PAGE = "模板管理",
  LIST_DETAIL = "查看模板列表及详情",
  APPLY = "申请模板",
  EDIT = "编辑",
  EDIT_MAPPING = "编辑Mapping",
  EDIT_SETTING = "编辑Setting",
  OFFLINE = "下线",
}

export enum TempletServicePermissions {
  PAGE = "模板服务",
  LIST_DETAIL = "查看模板列表",
  PRE_CREATE = "开关：预创建",
  EXPIRE_DELETE = "开关：过期删除",
  HOT_COLD = "开关：冷热分离",
  PIPELINE = "开关：pipeline",
  ROLLOVER = "开关：Rollover",
  GET_DCDR = "查看DCDR链路",
  CREATE_DCDR = "创建DCDR链路",
  CLEAN = "清理",
  EXPAND_SHRINK = "扩缩容",
  UPGRADE_VERSION = "升版本",
  BAT = "批量操作",
}

export enum IndexPermissions {
  PAGE = "索引管理",
  LIST_DETAIL = "查看索引列表及详情",
  EDIT_MAPPING = "编辑Mapping",
  EDIT_SETTING = "编辑Setting",
  SET_ALIAS = "设置别名",
  DELETE_ALIAS = "删除别名",
  OFFLINE = "下线",
  BAT_DELETE = "批量删除",
  CREATE_INDEX = "新建索引",
}

export enum IndexServicePermissions {
  PAGE = "索引服务",
  LIST_DETAIL = "查看列表",
  ROLLOVER = "执行Rollover",
  SHRINK = "执行shrink",
  SPLIT = "执行split",
  BAT = "批量执行",
  DISABLE_READ = "禁用读",
  DISABLE_WRITE = "禁用写",
  CLOSE_INDEX = "关闭索引",
  ForceMerge = "执行ForceMerge",
}

export enum DslPermissions {
  PAGE = "DSL",
}

export enum SqlPermissions {
  PAGE = "SQL",
}

export enum KibanaPermissions {
  PAGE = "Kibana",
}

export enum SearchTemplatePermissions {
  PAGE = "查询模板",
  BAT_MODIFY = "批量修改限流值",
  DISABLE = "禁用",
  MODIFY_LIMIT = "修改限流值",
}

export enum SearchQueryPermissions {
  PAGE = "查询诊断",
  ERROR_LIST = "查看异常查询列表",
  SLOW_LIST = "查看慢查询列表",
}

export enum ClusterPanelPermissions {
  PAGE = "集群看板",
  LIST = "查看集群看板",
}

export enum GatewayPanelPermissions {
  PAGE = "网关看板",
  LIST = "查看网关看板",
}

export enum MyApplyPermissions {
  PAGE = "我的申请",
  LIST = "查看我的申请列表",
  CALLBACK = "撤回",
}

export enum MyApprovalPermissions {
  PAGE = "我的审批",
  LIST = "查看我的审批列表",
  CALLBACK = "驳回",
  DNOE = "通过",
}

export enum TaskPermissions {
  PAGE = "任务列表",
  LIST = "查看任务列表",
  DETAIL = "查看进度",
  EXE = "执行",
  STOP = "暂停",
  RETRY = "重试",
  CANCEL = "取消",
  LOG_CHILD = "查看日志（子任务）",
  RETRY_CHILD = "重试（子任务）",
  IGNORE_CHILD = "忽略（子任务）",
  DETAIL_DCDR = "查看详情（DCDR）",
  CANCEL_DCDR = "取消（DCDR）",
  RETRY_DCDR = "重试（DCDR）",
  CHANGE_DCDR = "强切（DCDR）",
  RETURN_DCDR = "返回（DCDR）",
}

export enum ScriptCenterPermissions {
  PAGE = "脚本中心",
  LIST_DETAIL = "查看脚本中心列表",
  ADD = "新建脚本",
  EDIT = "编辑",
  DELETE = "删除",
}

export enum SoftwareCenterPermissions {
  PAGE = "软件中心",
  LIST_DETAIL = "查看软件中心列表",
  ADD = "新建软件",
  EDIT = "编辑",
  DELETE = "删除",
}

export enum ShceduleTaskPermissions {
  PAGE = "调度任务列表",
  LIST = "查看任务列表",
  DETAIL = "查看日志",
  EXE = "执行",
  STOP = "暂停",
}

export enum ShceduleLogPermissions {
  PAGE = "调度日志",
  LIST = "查看调度日志列表",
  DETAIL = "调度详情",
  LOG = "执行日志",
  END_MISSION = "终止任务",
}

export enum UserPermissions {
  PAGE = "用户管理",
  LIST_DETAIL = "查看用户列表",
  ASSGIN = "分配角色",
}

export enum RolePermissions {
  PAGE = "角色管理",
  LIST_DETAIL = "查看角色列表",
  EDIT = "编辑",
  DELETE = "删除角色",
  BIND = "绑定用户",
  RECYCLE = "回收用户",
  ADD = "新增角色",
}

export enum ProjectPermissions {
  PAGE = "应用管理",
  LIST_DETAIL = "查看应用列表",
  ADD = "新建应用",
  EDIT = "编辑",
  DELETE = "删除",
  ACCESS = "访问设置",
}

export enum PlatformPermissions {
  PAGE = "平台配置",
  LIST_DETAIL = "查看平台配置列表",
  ADD = "新增平台配置",
  EDIT = "编辑平台配置",
  DELETE = "删除平台配置",
  DISABLE = "禁用平台配置",
}

export enum OPRecordPermissions {
  PAGE = "操作记录",
  LIST_DETAIL = "查看操作记录列表",
}
