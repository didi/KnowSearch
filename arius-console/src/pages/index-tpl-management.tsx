import * as React from 'react';
import CommonRoutePage from './common';
import { IndexTplManagement } from 'container/index-tpl-management/index';

export const IndexTplManagementPageRoutes = [
  {
    path: '/index-tpl-management',
    exact: true,
    component: IndexTplManagement,
    cacheKey: 'index-tpl-management',
  },
];