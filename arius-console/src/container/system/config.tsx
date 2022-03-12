import React from 'react';
import { IDeploy, IOpRecord } from "@types/cluster/physics-type";
import { Tooltip, Modal, notification } from 'antd';
import { cellStyle } from "constants/table";
import { deployStatus } from 'constants/status-map';
import { renderOperationBtns } from 'container/custom-component';
import { InfoCircleOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { deleteDeploy, switchDeploy } from 'api/cluster-api';
import moment from 'moment';
import { timeFormat } from 'constants/time';

const { confirm } = Modal;

export const getClusterCongigQueryXForm = () => {
  const formMap = [
    {
      dataIndex: 'valueGroup',
      title: '配置组',
      type: 'input',
      placeholder: "请输入",
    }, {
      dataIndex: 'valueName',
      title: '名称',
      type: 'input',
      placeholder: "请输入",
    }
  ];
  return formMap;
}

export const getOperatingListQueryXForm = (modules) => {
  const formMap = [
    {
      dataIndex: 'moduleId',
      title: '模块',
      type: "select",
      options: modules,
      placeholder: "请选择",
    }, {
      dataIndex: 'operator',
      title: '操作人',
      type: 'input',
      placeholder: "请输入",
    }
  ];
  return formMap;
};

export const getClusterCongigColumns = (data: IDeploy[], fn: any, reloadDataFn: any) => {
  const getClusterCongigBtnList = (record: IDeploy, fn: any, reloadDataFn: any) => {
    return [{
      label: `${record.status === 1 ? '禁用' : '开启'}`,
      clickFunc: () => {
        const { id, status } = record;
        confirm({
          title: '提示',
          icon: <QuestionCircleOutlined style={{ color: 'red' }} />,
          content: `您确定要执行${record.status === 1 ? '禁用' : '开启'}操作吗？`,
          width: 500,
          okText: '确认',
          cancelText: '取消',
          onOk() {
            switchDeploy({ id, status: status === 1 ? 2 : 1 }).then((data => {
              notification.success({ message: `${status === 1 ? '关闭成功' : '打开成功'}` });
              reloadDataFn();
            }));
          },
        });
      },
    },
    {
      label: '编辑',
      clickFunc: () => {
        fn('clusterConfigModal', record, reloadDataFn)
      }
    },
    {
      label: '删除',
      clickFunc: (record: IDeploy) => {
        confirm({
          title: '提示',
          icon: <QuestionCircleOutlined style={{ color: 'red' }} />,
          content: '您确定要执行删除操作吗？',
          width: 500,
          okText: '确认',
          cancelText: '取消',
          onOk() {
            const { id } = record;
            deleteDeploy({ id }).then(data => {
              notification.success({ message: '删除成功' });
              reloadDataFn();
            });
          },
        });
        return;
      },
    },];
  }
  const columns = [
      {
        title: 'ID',
        dataIndex: 'id',
        key: 'id',
        width: '8%',
        sorter: (a: IDeploy, b: IDeploy) => a.id - b.id,
      }, {
        title: '配置组',
        dataIndex: 'valueGroup',
        key: 'valueGroup',
        width: '10%',
        onCell: () => ({
          style: {
            maxWidth: 250,
            ...cellStyle,
          },
        }),
        render: (value: string) => {
          return (
            <Tooltip placement="bottomLeft" title={value} >
              {value}
            </Tooltip>);
        },
      }, {
        title: '名称',
        dataIndex: 'valueName',
        key: 'valueName',
        width: '18%',
        onCell: () => ({
          style: {
            maxWidth: 200,
            ...cellStyle,
          },
        }),
        render: (value: string) => {
          return (
            <Tooltip placement="bottomLeft" title={value} >
              {value}
            </Tooltip>);
        },
      }, {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: '10%',
        render: (value: number) => {
          return deployStatus[value] || '';
        },
      }, {
        title: '值',
        dataIndex: 'value',
        key: 'value',
        width: '20%',
        onCell: () => ({
          style: {
            maxWidth: 250,
            ...cellStyle,
          },
        }),
        render: (desc: string) => {
          return (
            <Tooltip placement="bottomLeft" title={desc} >
              {desc || '-'}
            </Tooltip>);
        },
      }, {
        title: '描述',
        dataIndex: 'memo',
        key: 'memo',
        width: '20%',
        onCell: () => ({
          style: {
            maxWidth: 250,
            ...cellStyle,
          },
        }),
        render: (desc: string) => {
          return (
            <Tooltip placement="bottomLeft" title={desc} >
              {desc || '-'}
            </Tooltip>);
        },
      },
      {
        title: '操作',
        dataIndex: 'operation',
        key: 'operation',
        width: '15%',
        render: (id: number, record: IDeploy) => {
          const btns = getClusterCongigBtnList(record, fn, reloadDataFn);
          return renderOperationBtns(btns, record);
        },
      },
  ]
  return columns;
};

export const getOperationColumns = () => {
  let cols = [
    {
      title: '业务ID',
      dataIndex: 'bizId',
      key: 'bizId',
      width: '10%',
      sorter: (a: IOpRecord, b: IOpRecord) => b.id - a.id,
    },
    {
      title: '操作时间',
      dataIndex: 'operateTime',
      key: 'operateTime',
      width: '15%',
      sorter: (a: IOpRecord, b: IOpRecord) => new Date(b.operateTime).getTime() - new Date(a.operateTime).getTime(),
      render: (t: number) => moment(t).format(timeFormat),
    }, {
      title: '模块',
      dataIndex: 'module',
      key: 'module',
      width: '10%',
    }, {
      title: '操作内容',
      dataIndex: 'content',
      key: 'content',
      width: '30%',
    }, {
      title: '操作人',
      dataIndex: 'operator',
      key: 'operator',
      width: '10%',
    },
  ];
  return cols;
};