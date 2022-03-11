import React, { useState } from 'react';
import { connect } from "react-redux";
import { Dispatch } from 'redux';
import * as actions from 'actions';
import { getTaskQueryXForm, getTaskColumns } from './config';
import QueryForm from 'component/dantd/query-form';
import { getTaskList } from 'api/task-api';
import { ITask } from 'typesPath/task-types';
import { queryFormText } from 'constants/status-map';
import { DTable } from 'component/dantd/dtable';
import moment from 'moment';

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

export const TaskList = connect(null, mapDispatchToProps)(() => {
  const department: string = localStorage.getItem('current-project');
  const [loading, setloading] = useState(false);
  const [queryFromObject, setqueryFromObject] = useState(null);
  const [data, setData] = useState([] as ITask[]);

  React.useEffect(() => {
    reloadData();
  }, [department]);


  const getData = () => { // 查询项的key 要与 数据源的key  对应
    if (!queryFromObject) return data;
    const keys = Object.keys(queryFromObject);
    const filterData = data.filter(
      (d) => {
        let b = true;
        keys.forEach((k: string) => {
          if (k === 'createTime' && queryFromObject[k]) {
            const sT = moment(queryFromObject[k][0]).valueOf();
            const eT = moment(queryFromObject[k][1]).valueOf();
            const dT = moment(d[k]).valueOf();
            (dT >= sT && dT <= eT) ? '' : b = false;
          } else {
            (d[k] + '')?.toLowerCase().includes(queryFromObject[k]) ? '' : b = false;
          }
        })
        return b;
      }
    )
    return filterData;
  }

  const reloadData = () => {
    setloading(true)
    getTaskList().then((res: ITask[]) => {
      if (res) {
        res = res.map((ele, index) => {
          return {
            ...ele,
            key: index,
          };
        }) || [];
        setData(res);
      }
    }).finally(() => {
      setloading(false)
    })
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
        <QueryForm  showCollapseButton={false} {...queryFormText} defaultCollapse columns={getTaskQueryXForm()} onChange={() => null} onReset={handleSubmit} onSearch={handleSubmit} initialValues={{}} isResetClearAll />
      </div>
      <div>
        <div className="table-content">
          <DTable
            loading={loading}
            rowKey="id"
            dataSource={getData()}
            columns={getTaskColumns(reloadData)}
            reloadData={reloadData}
          />
        </div>
      </div>
    </>
  )
})
