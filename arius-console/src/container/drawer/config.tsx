import React from 'react';
import  { Modal } from 'antd';
import { IRegionTaskItem } from '@types/cluster/physics-type';
import { renderOperationBtns } from 'container/custom-component';
import { TaskItemModal } from 'container/cluster/physics-detail/region-task-item-modal';


export const getPhysicsRegionTaskItemColumns = () => {
  const operationList = [
    {
      label: 'detail',
      clickFunc: (record: IRegionTaskItem) => {
        Modal.confirm({
          title: '详情',
          icon: 'none',
          content: <TaskItemModal {...record} />,
          width: 800,
          okText: '确认',
          cancelText: '取消',
          onOk() {
            return;
          },
        });
      },
    }];
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      sorter: (a: IRegionTaskItem, b: IRegionTaskItem) => a.id - b.id,
    }, {
      title: '物理模版ID',
      dataIndex: 'physicalId',
      key: 'physicalId',
    }, {
      title: '模版名字',
      dataIndex: 'templateName',
      key: 'templateName',
    }, {
      title: 'Quota',
      dataIndex: 'quota',
      key: 'quota',
    }, {
      title: '磁盘消耗（G）',
      dataIndex: 'sumIndexSizeG',
      key: 'sumIndexSizeG',
    }, {
      title: 'cpu消耗',
      dataIndex: 'combinedCpuCount',
      key: 'combinedCpuCount',
    }, {
      title: 'tps峰值(w/s)',
      dataIndex: 'maxTps',
      key: 'maxTps',
    }, {
      title: '操作',
      dataIndex: 'operation',
      key: 'operation',
      width: '15%',
      render: (text: number, record: IRegionTaskItem) => {
        const btns = operationList;
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return columns;
};