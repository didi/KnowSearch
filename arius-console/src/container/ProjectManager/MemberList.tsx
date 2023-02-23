import React from 'react';
import _ from 'lodash';
import { getMemberListColumns } from './config';
import './index.less'
import { DTable } from 'component/dantd/dtable';

export const MemberList = (props) => {
  return (
    <>
      <DTable
        rowKey="id"
        dataSource={props.list}
        columns={getMemberListColumns()}
        reloadData={null}
      />
    </>
  );
};
