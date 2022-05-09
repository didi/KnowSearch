import * as React from 'react';
// import { users } from 'store/users';
import moment, { Moment } from 'moment';
import { IMenuItem } from 'interface/common';
import { MemberList } from './member-list';
import ResourcesList from './resources';
import ResourcesAssociatedList from './resources-associated';
import {Select, DatePicker} from 'antd';
import { NavRouterLink, renderOperationBtns } from 'container/custom-component';

const { Option } = Select;
const { RangePicker } = DatePicker;

export const getProjectColumns = (setModalId: Function, reloadList?: Function) => {
  const columns = [
    {
      title: '项目ID',
      dataIndex: 'id',
      key: 'id',
    }, {
      title: '项目名',
      dataIndex: 'name',
      key: 'name',
      render: (text: number, record: any) => <NavRouterLink key={`1Q`} element={text} href={`/user/project/detail?syncTaskId=${record.id}`} />,
    }, {
      title: '项目负责人',
      dataIndex: 'principal',
      key: 'principal',
    }, {
      title: '成员数',
      dataIndex: 'member',
      key: 'member',
      render: (text: number, record: any) => <NavRouterLink key={`1Q`} element={text} href={`/system/project/detail?syncTaskId=${record.id}`} />,
    }, {
      title: '关联资源数',
      dataIndex: 'respAppIds',
      key: 'respAppIds',
      render: (text: number, record: any) => <NavRouterLink key={`1Q`} element={text} href={`/system/project/detail?syncTaskId=${record.id}#resourcesAssociated`} />,
    }, {
      title: '密钥',
      dataIndex: 'ssh',
      key: 'ssh',
      render: (text: number) => <span>{text}</span>,
    }, {
      title: '创建时间',
      dataIndex: 'date',
      key: 'date',
    }, {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
    }, {
      title: '操作',
      dataIndex: 'operation',
      key: 'operation',
      render: (text: string, record: any) => {
        const btns = [
        {
          clickFunc: () => setModalId('addOrEditProjectModal', record, reloadList),
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

export const getMemberListColumns = () => {
  const columns = [
    {
      title: '用户名',
      dataIndex: 'name',
      key: 'name',
    }, {
      title: '邮箱',
      dataIndex: 'mailbox',
      key: 'mailbox',
    }, {
      title: '电话',
      dataIndex: 'phone',
      key: 'phone',
    }, {
      title: '角色',
      dataIndex: 'role',
      key: 'role',
    }
  ];
  return columns;
};

export const getResourcesListColumns = (setModalId: Function, reloadList?: Function) => {
  const columns = [
    {
      title: '资源名',
      dataIndex: 'name',
      key: 'name',
    }, {
      title: '资源类型',
      dataIndex: 'type',
      key: 'type',
    }, {
      title: '操作',
      dataIndex: 'operation',
      key: 'operation',
      render: (text: string, record: any) => {
        const btns = [
        {
          clickFunc: () => setModalId('transferOfResources', record, reloadList),
          label: '转让'
        }]
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return columns;
};

export const getResourcesAssociatedListColumns = (setModalId: Function, reloadList?: Function) => {
  const columns = [
    {
      title: '资源名',
      dataIndex: 'name',
      key: 'name',
    }, {
      title: '资源类型',
      dataIndex: 'type',
      key: 'type',
    }, {
      title: '细分权限',
      dataIndex: 'auth',
      key: 'auth',
    }, {
      title: '操作',
      dataIndex: 'operation',
      key: 'operation',
      render: (text: string, record: any) => {
        const btns = [
        {
          clickFunc: () => setModalId('resourcesAssociated', record, reloadList),
          label: '编辑细分权限'
        }, {
          clickFunc: () => setModalId('transferOfResources', record, reloadList),
          label: '解绑'
        }]
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return columns;
};

export const getProjectQueryXForm = () => {
  return [
  {
    type: 'select',
    title: '所属项目',
    dataIndex: 'id',
    options: [],
  }, {
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
  },
  ];
}

export const baseInfo = [{
  label: '项目名',
  key: 'name',
  render: (text: string) => (
    <>
      <span>{text}</span>
    </>
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
  members = 'members',
  resources = 'resources',
  resourcesAssociated = 'resourcesAssociated',
}

export const DETAIL_MENU = [
  {
    label: '成员列表',
    key: DETAIL_TAB_KEY.members,
    render: (value: any) => (<MemberList />)
  },
  {
    label: '纳管资源',
    key: DETAIL_TAB_KEY.resources,
    render: (value: any) => (<ResourcesList />)
  },
  {
    label: '关联资源',
    key: DETAIL_TAB_KEY.resourcesAssociated,
    render: (value: any) => (<ResourcesAssociatedList />)
  }
] as IMenuItem[];

const menuMap = new Map<string, IMenuItem>();
DETAIL_MENU.forEach(d => {
  menuMap.set(d.key, d);
});

export const DETAIL_MENU_MAP = menuMap;
