import * as React from 'react';
import CommonRoutePage from './common';
import { ClusterConfig } from 'container/system/cluster-config';
import { OperatingList } from 'container/system/operating-list';


export const ClusterSystem = () => {
  const pageRoutes = [
    {
      path: '/',
      exact: true,
      component: ClusterConfig,
    },
    {
      path: '/system',
      exact: true,
      component: ClusterConfig,
    },
    {
      path: '/system/config',
      exact: true,
      component: ClusterConfig,
    },
    {
      path: '/system/operation',
      exact: true,
      component: OperatingList,
    },
  ];

  return (
    <CommonRoutePage pageRoute={pageRoutes} active="cluster" />
  );
};
