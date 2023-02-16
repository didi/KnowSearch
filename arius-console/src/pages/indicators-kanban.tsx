import { ClusterPanelPermissions, GatewayPanelPermissions } from "constants/permission";
import * as React from "react";
import { ClusterKanban, GatewayKanban } from "../container/indicators-kanban";

export const IndicatorsKanbanPageRoutes = [
  {
    path: "/indicators",
    exact: true,
    component: ClusterKanban,
    permissionPoint: ClusterPanelPermissions.PAGE,
  },
  {
    path: "/indicators/cluster",
    exact: true,
    component: ClusterKanban,
    permissionPoint: ClusterPanelPermissions.PAGE,
  },
  {
    path: "/indicators/gateway",
    exact: true,
    needCache: true,
    component: GatewayKanban,
    permissionPoint: GatewayPanelPermissions.PAGE,
  },
];
