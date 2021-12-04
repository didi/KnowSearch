import * as React from 'react';
import { IUser } from './types';
// import { users } from 'store/users';
import moment, { Moment } from 'moment';
import { renderOperationBtns } from 'container/custom-component';
import { Select, DatePicker } from 'antd';


const { Option } = Select;
const { RangePicker } = DatePicker;

export const getUserColumns = (setModalId: Function, reloadList?: Function) => {
  const columns = [
    {
      title: '用户账号',
      dataIndex: 'user',
      key: 'user',
    }, {
      title: '用户名',
      dataIndex: 'userName',
      key: 'userName',
    }, {
      title: '邮箱',
      dataIndex: ' mailbox',
      key: ' mailbox',
      render: (text: string) => <span>{text}</span>,
    }, {
      title: '电话',
      dataIndex: 'phone',
      key: 'phone',
    }, {
      title: '所属项目',
      dataIndex: 'respAppIds',
      key: 'respAppIds',
      render: (text: number) => <a>{'xx'}</a>,
    }, {
      title: '角色',
      dataIndex: 'role',
      key: 'role',
      render: (text: number) => <a>{'xx'}</a>,
    }, {
      title: '新增时间',
      dataIndex: 'date',
      key: 'date',
    }, {
      title: '操作',
      dataIndex: 'operation',
      key: 'operation',
      render: (text: string, record: any) => {
        const btns = [
        {
          clickFunc: () => setModalId('addOrEditUserModal', record, reloadList),
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

export const getUserQueryXForm = () => {
  return [
    {
      dataIndex: 'project',
      title: '所属项目',
      type: 'select',
      options: [],
      placeholder: '请选择'
    }, {
    type: 'select',
    title: '角色',
    dataIndex: 'taskName',
    options: [],
    }, {
      type: 'input',
      title: '用户信息',
      dataIndex: 'projectName',
      placeholder: '请输入所属项目名称',
    }, {
      type: 'custom',
      title: '新增时间',
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


// const handleDeleteUser = (id: number) => {
//   users.deleteUser(id);
// };
