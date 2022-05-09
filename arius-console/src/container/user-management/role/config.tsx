import * as React from 'react';
import { Badge, Select, DatePicker } from 'antd';
import moment, { Moment } from 'moment';
import { IMenuItem } from 'interface/common';
import AuthorizedList from './authorized-list';
import JurisdictionList from './jurisdiction-list';
import { NavRouterLink, renderOperationBtns } from 'container/custom-component';

const { Option } = Select;
const { RangePicker } = DatePicker;

export const getRoleColumns = (setModalId: Function, reloadList?: Function) => {
  // 角色名、涉及模块、授权用户数、描述、创建时间、操作
  const columns = [
    {
      title: '角色名',
      dataIndex: 'user',
      key: 'user',
      render: (text: number, record: any) => <NavRouterLink element={text} href={`/es/user/role/detail?syncTaskId=${record.id}`} />,
    }, {
      title: '涉及模块',
      dataIndex: 'userName',
      key: 'userName',
    }, {
      title: '授权用户数',
      dataIndex: ' mailbox',
      key: ' mailbox',
      render: (text: string) => <span>{text}</span>,
    }, {
      title: '描述',
      dataIndex: 'phone',
      key: 'phone',
    }, {
      title: '创建时间',
      dataIndex: 'respAppIds',
      key: 'respAppIds',
      render: (text: number) => <a>{'xx'}</a>,
    }, {
      title: '操作',
      dataIndex: 'operation',
      key: 'operation',
      render: (text: string, record: any) => {
        const btns = [
        {
          clickFunc: () => setModalId('addOrEditRole', record, reloadList),
          label: '编辑'
        }, {
          clickFunc: () => setModalId('delTask'),
          label: '删除'
        }]
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return columns;
};

export const getRoleQueryXForm = () => {
  return [
    {
      type: 'custom',
      title: '创建时间',
      dataIndex: 'status',
      component: (
        <RangePicker
        ranges={{
          近一天: [moment().subtract(1, 'day'), moment()],
          近一周: [moment().subtract(7, 'day'), moment()],
          近一月: [moment().subtract(1, 'month'), moment()],
        }}
        format="YYYY/MM/DD"
      />
      ),
    }, {
      type: 'select',
      title: '涉及模块',
      dataIndex: 'id',
      option: []
    }, {
      type: 'input',
      title: '角色名',
      dataIndex: 'roleName',
      placeholder: '请输入角色名',
    }, 
  ];
}

export const baseInfo = [{
  label: '所属租户',
  key: 'name',
  render: (value: string) => (
    <>{value}</>
  )
}, {
  label: '成员数',
  key: 'members'
}, {
  label: '关联资源数',
  key: 'resources'
}, {
  label: '创建时间',
  key: 'startTime'
}, {
  label: '描述',
  key: 'desc'
}];

export enum DETAIL_TAB_KEY {
  authorizedUsers = 'authorizedUsers',
  jurisdiction = 'jurisdiction',
}

export const DETAIL_MENU = [
  {
    label: '授权用户',
    key: DETAIL_TAB_KEY.authorizedUsers,
    render: (value: any) => (<AuthorizedList />)
  },
  {
    label: '权限点列表',
    key: DETAIL_TAB_KEY.jurisdiction,
    render: (value: any) => (<JurisdictionList />)
  },
] as IMenuItem[];

const menuMap = new Map<string, IMenuItem>();
DETAIL_MENU.forEach(d => {
  menuMap.set(d.key, d);
});

export const DETAIL_MENU_MAP = menuMap;

export const roleDetailBreadcrumb = [{
  aHref: `/system/role`,
  label: '角色列表',
}, {
  label: '角色详情',
}];

export const getRoleJurisdictionListColumns = (setModalId: Function, reloadList?: Function) => {
  const columns = [
    {
      title: '模块名',
      dataIndex: 'name',
      key: 'name',
    }, {
      title: '菜单路径',
      dataIndex: 'type',
      key: 'type',
    }, {
      title: '权限点',
      dataIndex: 'auth',
      key: 'auth',
    },
  ];
  return columns;
};

export const getRoleAuthorizedListColumns = (setModalId: Function, reloadList?: Function) => {
  const columns = [
    {
      title: '用户账号',
      dataIndex: 'name',
      key: 'name',
    }, {
      title: '用户名',
      dataIndex: 'type',
      key: 'type',
    }, {
      title: '邮箱',
      dataIndex: 'auth',
      key: 'auth',
    }, {
      title: '电话',
      dataIndex: 'phone',
      key: 'phone',
    }
  ];
  return columns;
};