import * as React from 'react';
import { PhysicsCluster } from '../container/cluster';
import { LogicCluster } from '../container/cluster';
import { LogicClusterDetail } from 'container/cluster/logic-detail/detail';
import { PhyClusterDetail } from 'container/cluster/physics-detail/detail';
import { EditionCluster } from 'container/cluster/cluster-edition';
import { DashBoard } from '../container/dashboard/index';

export const ClusterAdminPageRoutes = [
  {
    path: '/',
    exact: true,
    component: DashBoard,
  },
  {
    path: '/cluster',
    exact: true,
    component: PhysicsCluster,
  },
  {
    path: '/cluster/physics',
    exact: true,
    component: PhysicsCluster,
  },
  {
    path: '/cluster/logic',
    exact: true,
    component: LogicCluster,
  },
  {
    path: '/cluster/edition',
    exact: true,
    component: EditionCluster,
  },
  {
    path: '/cluster/logic/detail',
    exact: true,
    component: LogicClusterDetail,
  },
  {
    path: '/cluster/physics/detail',
    exact: true,
    component: PhyClusterDetail,
  },
];