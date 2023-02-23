import React, { useState } from 'react';
import { connect } from "react-redux";
import _ from 'lodash';
import { getRoleColumns, getRoleQueryXForm } from './config';
import { Dispatch } from 'redux';
import * as actions from '../../actions';
import { DTable, ITableBtn } from 'component/dantd/dtable';
import { RenderTitle } from 'component/render-title';
import QueryForm from 'component/dantd/query-form';
import { queryFormText } from 'constants/status-map';
import { getRoleList } from 'api/logi-security';

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});
type Props = ReturnType<typeof mapDispatchToProps>;

interface IQueryParams {
  id: number;
  taskName: string;
  projectName: string;
  status: string;
  owner: string;
}

const RoleList: React.FC<Props> = (props) => {
  const department: string = localStorage.getItem('current-project');
  const [loading, setLoading] = React.useState(false);
  const [queryFormObject, setqueryFormObject] = useState({});
  const [data, setData] = useState([]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  React.useEffect(() => {
    reloadData();
  }, [department]);

  const reloadData = () => {
    const { current, pageSize } = pagination;
    const params = {
      ...queryFormObject,
      page: current,
      size: pageSize,
    };
    setLoading(true)
    getRoleList(params).then((res) => {
      if (res) {
        setData(res.bizData)
        setPagination({
          current: res.pagination.pageNo,
          pageSize: res.pagination.pageSize,
          total: res.pagination.total,
        });
      }
    }).finally(() => {
      setLoading(false)
    })
  }

  const renderTitleContent = () => {
    return {
      title: '角色',
      content: null
    }
  }

  const handleSubmit = (result) => {
    for (var key in result) {
      if (result[key] === '' || result[key] === undefined) {
        delete result[key]
      }
    }
    setqueryFormObject(result);
  };

  const getOpBtns = (): ITableBtn[] => {
    return [{
      label: '新增角色',
      className: 'ant-btn-primary',
      clickFunc: () => props.setModalId('addOrEditRole', null, reloadData),
    }];
  }

  return (
    <>
      <div className="table-header">
        <RenderTitle {...renderTitleContent()} />
        <QueryForm {...queryFormText} defaultCollapse columns={getRoleQueryXForm()} onChange={() => null} onReset={handleSubmit} onSearch={handleSubmit} initialValues={{}} isResetClearAll />
      </div>
      <div>
        <div className="table-content">
          <DTable
            loading={loading}
            rowKey="id"
            dataSource={data}
            columns={getRoleColumns(props.setModalId, reloadData)}
            reloadData={reloadData}
            getOpBtns={getOpBtns}
          />
        </div>
      </div>
    </>
  )
};

export default connect(null, mapDispatchToProps)(RoleList);
