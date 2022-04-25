import React, { useState } from 'react';
import { connect } from "react-redux";
import { Dispatch } from 'redux';
import * as actions from 'actions';
import { getMyApplicationQueryXForm, getMyApplicationColumns } from './config';
import moment from 'moment';
import QueryForm from 'component/dantd/query-form';
import { getApplyOrderList, getTypeEnums } from 'api/order-api';
import { ITypeEnums } from 'typesPath/cluster/order-types';
import { IStringMap } from 'interface/common';
import { queryFormText } from 'constants/status-map';
import { DTable } from 'component/dantd/dtable';

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

export const MyApplication = connect(null, mapDispatchToProps)((props) => {
  const department: string = localStorage.getItem('current-project');
  const [loading, setloading] = useState(false);
  const [queryFormObject, setqueryFormObject] = useState(null);
  const [data, setData] = useState([]);
  const [typeList, setTypeList] = useState([] as ITypeEnums[]);
  const [typeEnums, setTypeEnums] = useState({} as IStringMap);

  React.useEffect(() => {
    reloadData();
  }, [department]);

  React.useEffect(() => {
    getTypeEnumsFn()
  }, [])

  const getTypeEnumsFn = () => {
    getTypeEnums().then((res) => {
      const arr = res.map((ele, index) => {
        return {
          ...ele,
          value: ele.type,
          title: ele.message,
          key: index + 1,
        };
      });
      const obj = {} as IStringMap;
      arr.map((e: ITypeEnums) => {
        obj[e.type] = e.message;
      });
      setTypeEnums(obj);
      setTypeList(arr);
    });
  }

  const getData = () => { // 查询项的key 要与 数据源的key  对应
    if (!queryFormObject) return data;
    const keys = Object.keys(queryFormObject);
    const filterData = data.filter(
      (d) => {
        let b = true;
        keys.forEach((k: string) => {
          if (k === 'createTime') {
            const time = moment(d[k]).unix();
            if ((queryFormObject[k][0] > time) || (time > queryFormObject[k][1])) {
              b = false
            }
          } else if (k === 'type') {
            d[k] === queryFormObject[k] ? '' : b = false;
          } else {
            (d[k] + '')?.includes(queryFormObject[k]) ? '' : b = false;
          }
        })
        return b;
      }
    )
    return filterData;
  }

  const reloadData = () => {
    setloading(true);
    getApplyOrderList(1).then((res) => {
      if (res) {
        setData(res);
      }
    }).finally(() => {
      setloading(false)
    })
  }

  const handleSubmit = (result) => {
    result.createTime = result.createTime?.map(item => {
      return item.unix()
    });
    for (var key in result) {
      if (result[key] === '' || result[key] === undefined) {
        delete result[key]
      }
    }
    setqueryFormObject(result);
  };

  return (
    <>
      <div className="table-header">
        <QueryForm {...queryFormText} defaultCollapse columns={getMyApplicationQueryXForm(typeList)} onChange={() => null} onReset={handleSubmit} onSearch={handleSubmit} initialValues={{}} isResetClearAll />
      </div>
      <div>
        <div className="table-content">
          <DTable
            loading={loading}
            rowKey="id"
            dataSource={getData()}
            columns={getMyApplicationColumns(typeEnums)}
            reloadData={reloadData}
          />
        </div>
      </div>
    </>
  )
})
