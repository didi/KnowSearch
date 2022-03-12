import * as React from 'react';
import CommonRoutePage from './common';
import { IndexTplManagement } from 'container/index-tpl-management/index';

export const IndexTplManagementPage = () => {
  const pageRoutes = [
    {
      path: '/index-tpl-management',
      exact: true,
      component: IndexTplManagement,
    },
  ];

  return (
    <CommonRoutePage pageRoute={pageRoutes} active="cluster" />
  );
};
