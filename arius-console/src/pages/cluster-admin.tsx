import * as React from "react";
import { PhysicsCluster } from "../container/cluster";
import { LogicCluster } from "../container/cluster";
import { LogicClusterDetail } from "container/cluster/logic-detail/detail";
import { PhyClusterDetail } from "container/cluster/physics-detail/detail";
import { EditionCluster } from "container/cluster/cluster-edition";
import { GatewayCluster } from "container/gateway-manage";
import { GatewayClusterDetail } from "container/gateway-manage/cluster/detail";
import { DashBoard } from "../container/dashboard/index";
import { getCookie } from "knowdesign/lib/utils/tools";
import { ClusterVersionPermissions, MyClusterPermissions, PhyClusterPermissions, GatewayPermissions } from "constants/permission";

import { isSuperApp } from "lib/utils";

export const ClusterAdminPageRoutes = [
  {
    path: "/",
    exact: true,
    component: isSuperApp() ? DashBoard : LogicCluster,
  },
  {
    path: "/cluster",
    exact: true,
    component: PhysicsCluster,
    permissionPoint: PhyClusterPermissions.PAGE,
  },
  {
    path: "/cluster/physics",
    exact: true,
    component: PhysicsCluster,
    needCache: true,
    permissionPoint: PhyClusterPermissions.PAGE,
  },
  {
    path: "/cluster/logic",
    exact: true,
    component: LogicCluster,
    needCache: true,
    permissionPoint: MyClusterPermissions.PAGE,
  },
  {
    path: "/cluster/edition",
    exact: true,
    component: EditionCluster,
    needCache: true,
    permissionPoint: ClusterVersionPermissions.PAGE,
  },
  {
    path: "/cluster/logic/detail",
    exact: true,
    component: LogicClusterDetail,
    permissionPoint: MyClusterPermissions.PAGE,
  },
  {
    path: "/cluster/physics/detail",
    exact: true,
    component: PhyClusterDetail,
    permissionPoint: PhyClusterPermissions.PAGE,
  },
  {
    path: "/cluster/gateway",
    exact: true,
    component: GatewayCluster,
    needCache: true,
    permissionPoint: GatewayPermissions.PAGE,
  },
  {
    path: "/cluster/gateway/detail",
    exact: true,
    component: GatewayClusterDetail,
    permissionPoint: GatewayPermissions.PAGE,
  },
];
