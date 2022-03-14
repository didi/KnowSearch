import * as React from 'react';
import CommonRoutePage from './common';
import {MyApplication} from 'container/work-order/my-application'
import { OrderDetail } from 'container/work-order/order-detail';
import { MyApproval } from 'container/work-order/my-approval';
import { TaskList } from 'container/work-order/task';
import { TaskDetail } from 'container/work-order/task-detail';
import { DcdrTaskList } from 'container/work-order/dcdr-detail';

export const workOrderPageRouter = [
  {
    path: '/work-order',
    exact: true,
    component: MyApplication,
  },
  {
    path: '/work-order/my-application',
    exact: true,
    component: MyApplication,
  },
  {
    path: '/work-order/my-application/detail',
    exact: true,
    component: OrderDetail,
  },
  {
    path: '/work-order/my-approval',
    exact: true,
    component: MyApproval,
  },
  {
    path: '/work-order/my-approval/detail',
    exact: true,
    component: OrderDetail,
  },
  {
    path: '/work-order/task',
    exact: true,
    component: TaskList,
  },
  {
    path: '/work-order/task/detail',
    exact: true,
    component: TaskDetail,
  },
  {
    path: '/work-order/task/dcdrdetail',
    exact: true,
    component: DcdrTaskList,
  },
];