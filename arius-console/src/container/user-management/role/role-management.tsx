import React from 'react';
import { connect } from "react-redux";
import _ from 'lodash';
import { getRoleColumns, getRoleQueryXForm } from './config';
import { Dispatch } from 'redux';
import * as actions from '../../../actions';
import { DTable, ITableBtn } from 'component/dantd/dtable';
import { RenderTitle } from 'component/render-title';
import QueryForm from 'component/dantd/query-form';
import { queryFormText } from 'constants/status-map';

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
  const [tableData, setTableData] = React.useState([]);
  const [queryFromObject, setqueryFromObject] = React.useState([]);
  const [loading, setLoading] = React.useState(false);
  React.useEffect(() => {
    reloadData();
  }, [department]);

  const getData = () => { // 查询项的key 要与 数据源的key  对应
    if (!queryFromObject) return tableData;
    const keys = Object.keys(queryFromObject);
    const filterData = tableData.filter(
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
    setTableData([{
      id: 1,
      name: 'ccc',
      user: 'oooo',
      ssh: 123456,
      principal: 'es-open',
      member: 12,
      date: '2021-01-02'
    }]);
    // setloading(true)
    // getOpPhysicsClusterList().then((res) => {
    //   if(res){
    //     setData(res);
    //   }
    // }).finally(() => {
    //   setloading(false)
    // })
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
    setqueryFromObject(result);
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
            dataSource={getData()}
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
