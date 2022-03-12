import * as React from 'react';
import CommonRoutePage from './common';
import { Schedulinglog } from 'container/Scheduling/schedulinglog';
import { TaskList } from 'container/Scheduling/taskList';

export const Scheduling = () => {
  const pageRoutes = [
    {
      path: '/scheduling',
      exact: true,
      component: TaskList,
    },
    {
      path: '/scheduling/task',
      exact: true,
      component: TaskList,
    },
    {
      path: '/scheduling/log',
      exact: true,
      component: Schedulinglog,
    },
    {
      path: '/scheduling/log/detail',
      exact: true,
      component: Schedulinglog,
    },
  ];

  return (
    <CommonRoutePage pageRoute={pageRoutes} active="cluster" />
  );
};
