import React from 'react';
import { connect } from "react-redux";
import _ from 'lodash';
import { getRoleJurisdictionListColumns } from './config';
import './index.less'
import { Dispatch } from 'redux';
import * as actions from '../../../actions';
import { DTable } from 'component/dantd/dtable';


const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

export const JurisdictionList = (props) => {
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
          name: 'es-001',
          type: 'es',
          auth: '读写'
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
          columns={getRoleJurisdictionListColumns(props.setModalId, reloadData)}
          reloadData={reloadData}
          tableHeaderSearchInput={{submit: handleSubmit}}
      />
    </>
  );
};

export default connect(null, mapDispatchToProps)(JurisdictionList);
