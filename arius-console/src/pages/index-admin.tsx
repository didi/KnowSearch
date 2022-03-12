import * as React from 'react';
import CommonRoutePage from './common';
import { IndexAdmin } from '../container/index-admin';
import { IndexAdminDetail } from '../container/index-admin/detail';


export const IndexAdminPage = () => {
  const pageRoutes = [
    {
      path: '/index-admin',
      exact: true,
      component: IndexAdmin,
    },
    {
      path: '/index-admin/detail',
      exact: true,
      component: IndexAdminDetail,
    }
  ];

  return (
    <CommonRoutePage pageRoute={pageRoutes} active="cluster" />
  );
};
