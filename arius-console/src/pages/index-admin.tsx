import * as React from 'react';
import CommonRoutePage from './common';
import { IndexAdmin } from '../container/index-admin';
import { IndexAdminDetail } from '../container/index-admin/detail';

export const IndexAdminPagePageRoutes = [
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
