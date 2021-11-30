import React, { useState } from 'react';
import { connect } from "react-redux";
import { Dispatch } from 'redux';
import * as actions from 'actions';
import { getUserColumns, getUserQueryXForm } from './config';
import { DTable, ITableBtn } from 'component/dantd/dtable';
import { RenderTitle } from 'component/render-title';
import QueryForm from 'component/dantd/query-form';
import { queryFormText } from 'constants/status-map';


const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

export const UserList = connect(null, mapDispatchToProps)((props: { setModalId: Function }) => {
  const department: string = localStorage.getItem('current-project');
  const [loading, setloading] = useState(false);
  const [queryFromObject, setqueryFromObject] = useState([]);
  const [data, setData] = useState([]);

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
          d[k]?.toLowerCase().includes(queryFromObject[k]) ? '' : b = false;
        })
        return b;
      }
    )
    return filterData;
  }

  const reloadData = () => {
    // setloading(true)
    // getOpPhysicsClusterList().then((res) => {
    //   if (res) {
    //     setData(res);
    //   }
    // }).finally(() => {
    //   setloading(false)
    // })
  }

  const renderTitleContent = () => {
    return {
      title: '用户',
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

  const getOpBtns = (): ITableBtn[] => {
    return [{
      label: '新增用户',
      className: 'ant-btn-primary',
      clickFunc: () => props.setModalId('addOrEditUserModal', null, reloadData),
    }];
  }


  return (
    <>
      <div className="table-header">
        <RenderTitle {...renderTitleContent()} />

        <QueryForm {...queryFormText} defaultCollapse columns={getUserQueryXForm()} onChange={() => null} onReset={handleSubmit} onSearch={handleSubmit} initialValues={{}} isResetClearAll />
      </div>
      <div>
        <div className="table-content">
          <DTable
            loading={loading}
            rowKey="id"
            dataSource={getData()}
            columns={getUserColumns(props.setModalId, reloadData)}
            reloadData={reloadData}
            getOpBtns={getOpBtns}
          // tableHeaderSearchInput={{submit: handleSubmit}}
          />
        </div>
      </div>
    </>
  )
})










