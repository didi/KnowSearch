import { ProjectList } from "container/ProjectManager";
import { ProjectDetail } from "container/ProjectManager/detail";
import { RoleManage } from "../d1-packages/CommonPages/RoleManage";
import { RoleDetail } from "container/RoleManager/detail";
import { UserManage } from "../d1-packages/CommonPages/UserManage";
import * as React from "react";
import { ProjectPermissions, RolePermissions, UserPermissions } from "constants/permission";

export const systemPageRoutes = [
  {
    path: "/",
    exact: true,
    component: UserManage,
    permissionPoint: UserPermissions.PAGE,
  },
  {
    path: "/system/users",
    exact: true,
    component: UserManage,
    permissionPoint: UserPermissions.PAGE,
  },
  {
    path: "/system/project",
    exact: true,
    component: ProjectList,
    permissionPoint: ProjectPermissions.PAGE,
  },
  {
    path: "/system/project/detail",
    exact: true,
    component: ProjectDetail,
    permissionPoint: ProjectPermissions.PAGE,
  },
  {
    path: "/system/role",
    exact: true,
    component: RoleManage,
    permissionPoint: RolePermissions.PAGE,
  },
  {
    path: "/system/role/detail",
    exact: true,
    component: RoleDetail,
    permissionPoint: RolePermissions.PAGE,
  },
];
