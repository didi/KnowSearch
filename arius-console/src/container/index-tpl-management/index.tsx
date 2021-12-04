import { getAllIndexList, IAllIndexList } from 'api/cluster-index-api';
import { RenderTitle } from 'component/render-title';
import React, { useState } from 'react';
import { getLogicIndexColumns, getQueryFormConfig } from './config';
import { LOGIC_INDEX_TITLE } from './constants';
import { connect } from "react-redux";
import { NavRouterLink } from 'container/custom-component';
import { Dispatch } from 'redux';
import QueryForm from 'component/dantd/query-form';
import * as actions from 'actions';
import { queryFormText } from 'constants/status-map';
import { DTable } from 'component/dantd/dtable';
import { AppState } from 'store/type';

const mapStateToProps = (state) => ({
  app: state.app,
});

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

export const IndexTplManagement = connect(mapStateToProps, mapDispatchToProps)((props: { setModalId: Function, app: AppState, history: any }) => {
  const department: string = localStorage.getItem('current-project');
  const [loading, setloading] = useState(false);
  const [queryFromObject, setqueryFromObject]: any = useState({
    from: 0,
    size: 10,
  });
  const [tableData, setTableData] = useState([]);
  const [paginationProps, setPaginationProps] = useState({
    position: 'bottomRight',
    showQuickJumper: true,
    total: 0,
    showSizeChanger: true,
    pageSizeOptions: ['10', '20', '50', '100', '200', '500'],
    showTotal: (total) => `共 ${total} 条`,
  })

  React.useEffect(() => {
    reloadData();
  }, [department, queryFromObject]);

  const handleSubmit = (result) => {
    for (var key in result) {
      if (result[key] === '' || result[key] === undefined) {
        delete result[key]
      }
    }
    setqueryFromObject({...result, from: 0, size: 10,});
  };

  const reloadData = () => {
    setloading(true)
    const Params: IAllIndexList = {
      from: queryFromObject.from,
      size: queryFromObject.size,
      authType: queryFromObject.authType,
      dataType: queryFromObject.dataType,
      name: queryFromObject.name,
    }
    getAllIndexList(Params).then((res) => {
      if (res) {
        setTableData(res?.bizData);
        setPaginationProps({
          position: 'bottomRight',
          showQuickJumper: true,
          total: res?.pagination?.total,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50', '100', '200', '500'],
          showTotal: (total) => `共 ${total} 条`,
        })
      }
    }).finally(() => {
      setloading(false)
    })
  }

  const getOpBtns = () => {
    return [{
      label: (<NavRouterLink element={'新建模板'} href={`/index/create`} />),
      className: 'ant-btn-primary',
      noRefresh: true,
    }];
  }

  const pushHistory = (url) => {
    props.history.push(url);
  }

  const handleChange = (pagination) => {
    setqueryFromObject((state) => ({
      ...state,
      from: (pagination.current - 1) * pagination.pageSize,
      size: pagination.pageSize,
    }));
  }
  return (
    <>
      <div className="table-header">
        <RenderTitle {...LOGIC_INDEX_TITLE} />

        <QueryForm {...queryFormText} defaultCollapse columns={getQueryFormConfig()} onChange={() => null} onReset={handleSubmit} onSearch={handleSubmit} initialValues={{}} isResetClearAll />
      </div>
      <div>
        <div className="table-content">
          <DTable
            loading={loading}
            rowKey="id"
            dataSource={tableData}
            key={JSON.stringify({
              authType: queryFromObject.authType,
              dataType: queryFromObject.dataType,
              name: queryFromObject.name,
            })}
            attrs={{
              onChange: handleChange
            }}
            paginationProps={paginationProps}
            columns={getLogicIndexColumns(tableData, props.setModalId, reloadData, null, props.app.appInfo()?.id, pushHistory)}
            reloadData={reloadData}
            getOpBtns={getOpBtns}
          />
        </div>
      </div>
    </>
  );
})