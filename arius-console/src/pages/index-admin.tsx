import { IndexPermissions, IndexServicePermissions } from "constants/permission";
import * as React from "react";
import { IndexAdmin, IndexService } from "../container/index-admin";
import { IndexAdminDetail } from "../container/index-admin/detail";

export const IndexAdminPagePageRoutes = [
  {
    path: "/index-admin",
    exact: true,
    component: IndexAdmin,
    permissionPoint: IndexPermissions.PAGE,
  },
  {
    path: "/index-admin/management",
    exact: true,
    component: IndexAdmin,
    needCache: true,
    permissionPoint: IndexPermissions.PAGE,
  },
  {
    path: "/index-admin/management/detail",
    exact: true,
    component: IndexAdminDetail,
    permissionPoint: IndexPermissions.PAGE,
  },
  {
    path: "/index-admin/service",
    exact: true,
    needCache: true,
    component: IndexService,
    permissionPoint: IndexServicePermissions.PAGE,
  },
];
