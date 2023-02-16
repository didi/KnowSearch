import * as React from "react";
import { ClusterConfig } from "container/system/cluster-config";
import { OperatingList } from "container/system/operating-list";
import { OPRecordPermissions, PlatformPermissions } from "constants/permission";

export const ClusterSystemPageRoutes = [
  {
    path: "/",
    exact: true,
    component: ClusterConfig,
    permissionPoint: PlatformPermissions.PAGE,
  },
  {
    path: "/system",
    exact: true,
    component: ClusterConfig,
    permissionPoint: PlatformPermissions.PAGE,
  },
  {
    path: "/system/config",
    exact: true,
    component: ClusterConfig,
    needCache: true,
    permissionPoint: PlatformPermissions.PAGE,
  },
  {
    path: "/system/operation",
    exact: true,
    component: OperatingList,
    needCache: true,
    permissionPoint: OPRecordPermissions.PAGE,
  },
];
