import React from 'react';
import _ from 'lodash';
import { getMemberListColumns } from './config';
import './index.less'
import { DTable } from 'component/dantd/dtable';

export const MemberList = () => {
  const [tableData, setTableData] = React.useState([]);
  const [loading, setLoading] = React.useState(false);
  const [searchKey, setSearchKey] = React.useState(null);

  const getData = (origin?: any[]) => {
    if(!searchKey) return origin;
    const searchKeys = (searchKey + '').trim().toLowerCase();
    const data = searchKeys ? origin.filter(
      (d) => {
        let flat = false;
        Object.keys(d).forEach((key) => {
          if (typeof(key) === 'string' || typeof(key) === 'number') {
            if ((d[key] + '').toLowerCase().includes((searchKeys + '') as string)) {
              flat = true;
              return;
            }
          }
        });
        return flat;
      }
    ) : origin;
    return data;
  }

  const reloadData = () => {
    setLoading(true)
    const res = {
      data: [
        {
          id: '1',
          name: '曦',
          mailbox: '976987127@qq.com',
          phone: '3423820',
          role: '青铜'
        }
      ]
    }
    setLoading(false);
    setTableData(res.data);
  }

  React.useEffect(() => {
    reloadData();
  }, []);

  const handleSubmit = (value) => {
    setSearchKey(value);
  }

  return (
    <>
       <DTable
          loading={loading}
          rowKey="id"
          dataSource={getData(tableData)}
          columns={getMemberListColumns()}
          reloadData={reloadData}
          tableHeaderSearchInput={{submit: handleSubmit}}
      />
    </>
  );
};
