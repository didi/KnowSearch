import * as React from 'react';
import CommonRoutePage from './common';
import { PhysicsIndex } from 'container/cluster-index/PhyscisIndex';
import { LogicIndexDetail } from 'container/cluster-index/logic-Index-detail/detail';
import { PhyIndexDetail } from 'container/cluster-index/phy-Index-detail/detail';
import { CreateIndex } from 'container/create-index';
import { FirstStep } from 'container/create-index/first-step';
import { SecondStep } from 'container/create-index/second-step';
import { ClearIndex } from 'container/cluster-index/logic-Index-detail/clear-index';

export const ClusterIndexPageRoutes = [
  {
    path: '/index/logic/detail',
    exact: true,
    component: LogicIndexDetail,
  },
  {
    path: '/index/physics',
    exact: true,
    component: PhysicsIndex,
  },
  {
    path: '/index/physics/detail',
    exact: true,
    component: PhyIndexDetail,
  },
  {
    path: '/index/create',
    exact: true,
    component: CreateIndex,
    cacheKey: 'index/create',
  },
  {
    path: '/index/modify',
    exact: true,
    component: FirstStep,
  },
  {
    path: '/index/modify/mapping',
    exact: true,
    component: SecondStep,
  },
  {
    path: '/index/clear',
    exact: true,
    component: ClearIndex,
  },
];