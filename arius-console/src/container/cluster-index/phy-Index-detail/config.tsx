import { timeFormat } from 'constants/time';
import moment from 'moment';
import React from 'react';

export const DESC_LIST = [{
  label: '所属集群',
  key: 'cluster'
}, {
  label: '所属项目',
  key: 'appName'
}, {
  label: '所属项目ID',
  key: 'appId'
}, {
  label: '负责人',
  key: 'responsible',
  render: (value: any) => (
    <>
      <span>{value}</span>
    </>
  ),
}];

export const baseInfo = [
  [{
    key: 'rack',
    label: '所属rack',
  }, {
    key: 'role',
    label: 'role',
    render: (value: number) => (
      <>
        <span>{value}</span>
      </>
    ),
  }], [{
    key: 'shard',
    label: 'shard',
  }, {
    key: 'createTime',
    label: '创建时间',
    render: (value: string) => <>{moment(value).format(timeFormat)}</>,
  }]
];