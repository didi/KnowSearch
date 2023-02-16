import {
  ClusterPanelPermissions,
  ClusterVersionPermissions,
  Dashboard,
  GatewayPanelPermissions,
  IndexPermissions,
  DslPermissions,
  SqlPermissions,
  KibanaPermissions,
  IndexServicePermissions,
  MyApplyPermissions,
  MyApprovalPermissions,
  MyClusterPermissions,
  OPRecordPermissions,
  PhyClusterPermissions,
  PlatformPermissions,
  ProjectPermissions,
  RolePermissions,
  SearchTemplatePermissions,
  SearchQueryPermissions,
  ShceduleLogPermissions,
  ShceduleTaskPermissions,
  TaskPermissions,
  TempletPermissions,
  TempletServicePermissions,
  UserPermissions,
  Grafana,
} from "./permission";

const pkgJson = require("../../package");
export const systemKey = pkgJson.ident;
export const urlPrefix = "/" + systemKey;

export const leftMenus = [
  {
    name: `${systemKey}`,
    path: "main",
    icon: "#",
    children: [
      {
        name: "dashboard",
        path: "dashboard",
        icon: "#iconDashboard",
        permissionPoint: Dashboard.PAGE,
      },
      {
        name: "cluster",
        path: "cluster",
        icon: "#iconjiqunguanli2",
        children: [
          {
            name: "physics",
            path: "physics",
            icon: "#icon-luoji",
            permissionPoint: PhyClusterPermissions.PAGE,
          },
          {
            name: "logic",
            path: "logic",
            icon: "#icon-jiqun1",
            permissionPoint: MyClusterPermissions.PAGE,
          },
          {
            name: "edition",
            path: "edition",
            icon: "#icon-jiqun1",
            permissionPoint: ClusterVersionPermissions.PAGE,
          },
        ],
      },
      {
        name: "index-tpl",
        path: "index-tpl",
        icon: "#iconmobanguanli1",
        children: [
          {
            name: "management",
            path: "management",
            icon: "#icon-luoji",
            permissionPoint: TempletPermissions.PAGE,
          },
          {
            name: "service",
            path: "service",
            icon: "#icon-luoji",
            permissionPoint: TempletServicePermissions.PAGE,
          },
        ],
      },
      {
        name: "index-admin",
        path: "index-admin",
        icon: "#iconsuoyinguanli1",
        children: [
          {
            name: "management",
            path: "management",
            icon: "#icon-luoji",
            permissionPoint: IndexPermissions.PAGE,
          },
          {
            name: "service",
            path: "service",
            icon: "#icon-luoji",
            permissionPoint: IndexServicePermissions.PAGE,
          },
        ],
      },
      {
        name: "search-query",
        path: "search-query",
        icon: "#iconjiansuochaxun2",
        children: [
          {
            name: "dsl",
            path: "dsl",
            icon: "#icon-luoji",
            permissionPoint: DslPermissions.PAGE,
          },
          {
            name: "sql",
            path: "sql",
            icon: "#icon-luoji",
            permissionPoint: SqlPermissions.PAGE,
          },
          {
            name: "search-template",
            path: "search-template",
            icon: "#icon-luoji",
            permissionPoint: SearchTemplatePermissions.PAGE,
          },
          {
            name: "dsl-tpl",
            path: "dsl-tpl",
            icon: "#icon-luoji",
            permissionPoint: SearchQueryPermissions.PAGE,
          },
          {
            name: "kibana",
            path: "kibana",
            icon: "#icon-luoji",
            permissionPoint: KibanaPermissions.PAGE,
          },
        ],
      },
      {
        name: "indicators",
        path: "indicators",
        icon: "#iconzhibiaokanban2",
        children: [
          {
            name: "cluster",
            path: "cluster",
            icon: "#icon-luoji",
            permissionPoint: ClusterPanelPermissions.PAGE,
          },
          {
            name: "gateway",
            path: "gateway",
            icon: "#icon-luoji",
            permissionPoint: GatewayPanelPermissions.PAGE,
          },
        ],
      },
    ],
  },
  {
    name: `${systemKey}`,
    path: "main",
    icon: "#",
    children: [
      {
        name: "scheduling",
        path: "scheduling",
        icon: "#icontiaodurenwu1",
        children: [
          {
            name: "task",
            path: "task",
            icon: "#icon-luoji",
            permissionPoint: ShceduleTaskPermissions.PAGE,
          },
          {
            name: "log",
            path: "log",
            icon: "#icon-luoji",
            permissionPoint: ShceduleLogPermissions.PAGE,
          },
        ],
      },
      {
        name: "system.config",
        path: "system/config",
        icon: "#iconpingtaipeizhi",
        permissionPoint: PlatformPermissions.PAGE,
      },
      {
        name: "system.operation",
        path: "system/operation",
        icon: "#iconcaozuojilu1",
        permissionPoint: OPRecordPermissions.PAGE,
      },
      {
        name: "work-order.task",
        path: "work-order/task",
        icon: "#iconrenwuzhongxin1",
        permissionPoint: TaskPermissions.PAGE,
      },
      {
        name: "work-order",
        path: "work-order",
        icon: "#icongongdanrenwu2",
        children: [
          {
            name: "my-application",
            path: "my-application",
            icon: "#icon-luoji",
            permissionPoint: MyApplyPermissions.PAGE,
          },
          {
            name: "my-approval",
            path: "my-approval",
            icon: "#icon-luoji",
            permissionPoint: MyApprovalPermissions.PAGE,
          },
        ],
      },
      {
        name: "system",
        path: "system",
        icon: "#iconzuhuguanli1",
        children: [
          {
            name: "users",
            path: "users",
            icon: "#icon-luoji",
            permissionPoint: UserPermissions.PAGE,
          },
          {
            name: "role",
            path: "role",
            icon: "#icon-luoji",
            permissionPoint: RolePermissions.PAGE,
          },
          {
            name: "project",
            path: "project",
            icon: "#icon-luoji",
            permissionPoint: ProjectPermissions.PAGE,
          },
        ],
      },
      {
        name: "grafana",
        icon: "#iconzhibiaokanban2",
        to: "jumpToGrafana",
        target: "_blank",
        permissionPoint: Grafana.PAGE,
      },
    ],
  },
];
