import React from 'react';
import { connect } from "react-redux";
import _ from 'lodash';
import { getResourcesAssociatedListColumns } from './config';
import './index.less'
import { Dispatch } from 'redux';
import * as actions from '../../actions';
import { DTable, ITableBtn } from 'component/dantd/dtable';


const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

export const ResourcesAssociatedList = (props) => {
  const [tableData, setTableData] = React.useState([]);
  const [loading, setLoading] = React.useState(false);
  const [searchKey, setSearchKey] = React.useState(null);


  const reloadData = () => {
    setLoading(true)
    const res = {
      data: [
        {
          id: '1',
          name: 'es-001',
          type: 'es',
          auth: '读写'
        }
      ]
    }
    setLoading(false);
    setTableData(res.data);
  }

  const getData = (origin?: any[]) => {
    if (!searchKey) return origin;
    const searchKeys = (searchKey + '').trim().toLowerCase();
    const data = searchKeys ? origin.filter(
      (d) => {
        let flat = false;
        Object.keys(d).forEach((key) => {
          if (typeof (key) === 'string' || typeof (key) === 'number') {
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

  React.useEffect(() => {
    reloadData();
  }, []);

  const handleSubmit = (value) => {
    setSearchKey(value);
  }

  const getOpBtns = (): ITableBtn[] => {
    return [{
      label: '关联资源',
      className: 'ant-btn-primary',
      clickFunc: () => props.setModalId('resourcesAssociated', null, reloadData),
    }];
  }

  return (
    <>
      <DTable
        loading={loading}
        rowKey="id"
        dataSource={getData(tableData)}
        columns={getResourcesAssociatedListColumns(props.setModalId, reloadData)}
        reloadData={reloadData}
        tableHeaderSearchInput={{ submit: handleSubmit }}
        getOpBtns={getOpBtns}
      />
    </>
  );
};

export default connect(null, mapDispatchToProps)(ResourcesAssociatedList);
