import React, { useState } from 'react';
import { connect } from "react-redux";
import { Dispatch } from 'redux';
import * as actions from 'actions';
import { getClusterCongigQueryXForm, getClusterCongigColumns } from './config';
import { getDeployList, getOpLogicClusterList } from 'api/cluster-api';
import { DTable, ITableBtn } from 'component/dantd/dtable';
import { RenderTitle } from 'component/render-title';
import QueryForm from 'component/dantd/query-form';
import { queryFormText } from 'constants/status-map';
import { Button } from 'antd';
import { RiseOutlined } from '@ant-design/icons'



const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

export const ClusterConfig = connect(null, mapDispatchToProps)((props: any) => {
  const department: string = localStorage.getItem('current-project');
  const [loading, setloading] = useState(false);
  const [queryFromObject, setqueryFromObject] = useState(null);
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
          (d[k] + '')?.toLowerCase().includes(queryFromObject[k]) ? '' : b = false;
        })
        return b;
      }
    )
    return filterData;
  }

  const reloadData = () => {
    getDeployList({}).then((res) => {
      if (res) {
        setData(res);
      }
    }).finally(() => {
      setloading(false)
    })
  }

  const renderTitleContent = () => {
    return {
      title: '平台配置',
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


  const getOpBtns = () => {
    // return [{
    //   label: '新增配置',
    //   className: 'ant-btn-primary',
    //   clickFunc: () => props.setModalId('clusterConfigModal', {}, reloadData)
    // }];
    return (
      <>
        <Button type="primary" className="ant-btn-primary" onClick={() => props.setModalId('clusterConfigModal', {}, reloadData)}>
          新增配置
        </Button>
        <div style={{ display: 'inline-block', fontSize: 14, color: '#1473FF', letterSpacing: 0, textAlign: 'right', marginLeft: 4, cursor: "pointer" }} onClick={() => window.open('https://github.com/didi/LogiEM/blob/master/doc/LogiEM%E7%94%A8%E6%88%B7%E6%8C%87%E5%8D%97.md#38-%E5%B9%B3%E5%8F%B0%E9%85%8D%E7%BD%AE')}>指导文档<RiseOutlined /></div>
      </>
    )
  }



  return (
    <>
      <div className="table-header">
        <RenderTitle {...renderTitleContent()} />

        <QueryForm {...queryFormText} showCollapseButton={false} defaultCollapse columns={getClusterCongigQueryXForm()} onChange={() => null} onReset={handleSubmit} onSearch={handleSubmit} initialValues={{}} isResetClearAll />
      </div>
      <div>
        <div className="table-content">
          <DTable
            loading={loading}
            rowKey="id"
            dataSource={getData()}
            columns={getClusterCongigColumns(data, props.setModalId, reloadData)}
            reloadData={reloadData}
            renderInnerOperation={getOpBtns}
          />
        </div>
      </div>
    </>
  )
})