import * as React from 'react';
import CommonRoutePage from './common';
import { PhysicsCluster } from '../container/cluster';
import { LogicCluster } from '../container/cluster';
import { LogicClusterDetail } from 'container/cluster/logic-detail/detail';
import { PhyClusterDetail } from 'container/cluster/physics-detail/detail';
import { EditionCluster } from 'container/cluster/cluster-edition';


export const ClusterAdmin = () => {
  const pageRoutes = [
    {
      path: '/',
      exact: true,
      component: PhysicsCluster,
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

  return (
    <CommonRoutePage pageRoute={pageRoutes} active="cluster" />
  );
};
