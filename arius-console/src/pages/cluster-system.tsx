import * as React from 'react';
import CommonRoutePage from './common';
import { ClusterConfig } from 'container/system/cluster-config';
import { OperatingList } from 'container/system/operating-list';

export const ClusterSystemPageRoutes = [
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