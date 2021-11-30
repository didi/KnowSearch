import * as React from 'react';
import CommonRoutePage from './common';
import {UserList} from 'container/user-management/user/user-management'
import {ProjectList} from 'container/user-management/project/project-management'
import { ProjectDetail } from 'container/user-management/project/detail';
import { RoleDetail } from 'container/user-management/role/detail';
import RoleList from 'container/user-management/role/role-management';


export const UserManagement = () => {
  const pageRoutes = [
    {
      path: '/',
      exact: true,
      component: UserList,
    },
    {
      path: '/user/users',
      exact: true,
      component: UserList,
    },
    {
      path: '/user/project',
      exact: true,
      component: ProjectList,
    },
    {
      path: '/user/project/detail',
      exact: true,
      component: ProjectDetail,
    },
    {
      path: '/user/role',
      exact: true,
      component: RoleList,
    },
    {
      path: '/user/role/detail',
      exact: true,
      component: RoleDetail,
    },
  ];

  return (
    <CommonRoutePage pageRoute={pageRoutes} active="cluster" />
  );
};
