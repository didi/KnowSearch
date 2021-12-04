import React, { useState } from 'react';
import { getOperatingListQueryXForm, getOperationColumns } from './config';
import { getlistModules, getUserRecordList } from 'api/cluster-api';
import { DTable } from 'component/dantd/dtable';
import { RenderTitle } from 'component/render-title';
import QueryForm from 'component/dantd/query-form';
import { queryFormText } from 'constants/status-map';

interface IOpRecordModules {
  code: number;
  desc: string;
}

export const OperatingList = (() => {
  const department: string = localStorage.getItem('current-project');
  const [loading, setloading] = useState(false);
  const [queryFromObject, setqueryFromObject] = useState(null);
  const [modules, setModules] = useState([]);
  const [data, setData] = useState([]);

  React.useEffect(() => {
    reloadData();
    getModules();
  }, [department]);

  const getData = () => { // 查询项的key 要与 数据源的key  对应
    if (!queryFromObject) return data;
    const keys = Object.keys(queryFromObject);
    const filterData = data.filter(
      (d) => {
        let b = true;
        keys.forEach((k: string) => {
          (d[k] + '')?.toLowerCase().includes(queryFromObject[k]) ? '' : b = false;
          if (k === 'moduleId') {
            d[k] === queryFromObject[k] ? '' : b = false;
          }
        })
        return b;
      }
    )
    return filterData;
  }

  const getModules = () => {
    getlistModules().then((res: IOpRecordModules[]) => {
      setModules((res || []).map(i => ({ title: i.desc, value: i.code })))
    })
  }

  const reloadData = () => {
    getUserRecordList({}).then((res) => {
      if (res) {
        setData(res);
      }
    }).finally(() => {
      setloading(false)
    })
  }

  const renderTitleContent = () => {
    return {
      title: '操作记录',
      content: null
    }
  }

  const handleSubmit = (result) => {
    for (var key in result) {
      if (result[key] === '' || result[key] === undefined) {
        delete result[key]
      }
    }
    setqueryFromObject(result);
  };


  return (
    <>
      <div className="table-header">
        <RenderTitle {...renderTitleContent()} />

        <QueryForm showCollapseButton={false} {...queryFormText} defaultCollapse columns={getOperatingListQueryXForm(modules)} onChange={() => null} onReset={handleSubmit} onSearch={handleSubmit} initialValues={{}} isResetClearAll />
      </div>
      <div>
        <div className="table-content">
          <DTable
            loading={loading}
            rowKey="id"
            dataSource={getData()}
            columns={getOperationColumns()}
            reloadData={reloadData}
          />
        </div>
      </div>
    </>
  )
});