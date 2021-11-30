import * as React from 'react';
import CommonRoutePage from './common';
import { ClusterKanban, GatewayKanban } from '../container/indicators-kanban';


export const IndicatorsKanban = () => {
  const pageRoutes = [
    {
      path: '/indicators',
      exact: true,
      component: ClusterKanban,
    },
    {
      path: '/indicators/cluster',
      exact: true,
      component: ClusterKanban,
    },
    {
      path: '/indicators/gateway',
      exact: true,
      component: GatewayKanban,
    }
  ];

  return (
    <CommonRoutePage pageRoute={pageRoutes} active="cluster" />
  );
};
