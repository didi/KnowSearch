import * as React from "react";
import { IndexTplManagement, IndexTplService, IndexTplDetail } from "container/index-tpl-management";
import { CreateIndexTpl } from "container/index-tpl-management/create";
import { EditBaseInfo } from "container/index-tpl-management/edit/baseInfo";
import { JsonMapping } from "container/index-tpl-management/edit/jsonMapping";
import { JsonSetting } from "container/index-tpl-management/edit/jsonSetting";
import { TempletPermissions, TempletServicePermissions } from "constants/permission";

export const IndexTplManagementPageRoutes = [
  {
    path: "/index-tpl",
    exact: true,
    component: IndexTplManagement,
    permissionPoint: TempletPermissions.PAGE,
  },
  {
    path: "/index-tpl/management",
    exact: true,
    component: IndexTplManagement,
    needCache: true,
    permissionPoint: TempletPermissions.PAGE,
  },
  {
    path: "/index-tpl/management/detail",
    exact: true,
    component: IndexTplDetail,
  },
  // {
  //   path: "/index-tpl/management/create",
  //   exact: true,
  //   component: CreateIndexTpl,
  //   needCache: true,
  // },
  // {
  //   path: "/index-tpl/management/modify",
  //   exact: true,
  //   component: EditBaseInfo,
  // },
  {
    path: "/index-tpl/management/modify/mapping",
    exact: true,
    component: JsonMapping,
  },
  {
    path: "/index-tpl/management/modify/setting",
    exact: true,
    component: JsonSetting,
  },
  {
    path: "/index-tpl/service",
    exact: true,
    component: IndexTplService,
    needCache: true,
    permissionPoint: TempletServicePermissions.PAGE,
  },
  {
    path: "/index-tpl/service/detail",
    exact: true,
    component: IndexTplDetail,
  },
];
