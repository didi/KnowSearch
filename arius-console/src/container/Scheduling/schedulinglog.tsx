import React, { useState, useEffect, useMemo } from 'react';
import { getSchedulingLogQueryXForm, getSchedulingLogColumns, mockData } from './config';
import QueryForm from 'component/dantd/query-form';
import { getLogsList } from 'api/Scheduling';
// todo 接口好后增加类型判断
// import { ITask } from '@types/task-types';
import { queryFormText } from 'constants/status-map';
import { DTable } from 'component/dantd/dtable';
import { RenderTitle } from 'component/render-title';
import SchDulingDetail from './../drawer/scheduling-detail';
import SchDulingLog from './../drawer/scheduling-log';
import getUrlParams from 'lib/url-parser';

export const Schedulinglog = () => {
  const department: string = localStorage.getItem('current-project');
  const [loading, setloading] = useState(false);
  const [queryFormObject, setqueryFormObject]: any = useState({});
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10
  })
  const [data, setData] = useState([] as any[]);
  // 控制调度日志的状态
  const [visible, setVisible] = useState(false);
  const [record, setRecord]: any = useState({});
  // 控制执行日志的状态
  const [logVisible, setLogVisible] = useState(false);
  const [urlParams, setUrlParams]: any = useState(getUrlParams().search);
  const [total, setTotal] = useState(0)
  useEffect(() => {
    reloadData({});
  }, [department, urlParams, queryFormObject]);

  const reloadData = ({ page = pagination.current, size = pagination.pageSize }) => {
    setloading(true)
    const params = {
      ...urlParams,
      ...queryFormObject,
      page,
      size,
      beginTime: queryFormObject?.createTime?.length ? queryFormObject?.createTime[0]?.valueOf() : '',
      endTime: queryFormObject?.createTime?.length ? queryFormObject?.createTime[1]?.valueOf() : '',
    }
    getLogsList(params).then((res: any) => {
      if (res) {
        setData(res?.bizData);
        setTotal(res.pagination.total)
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
    setPagination({ ...pagination, current: 1 })
    setqueryFormObject({ ...result });
  };

  const renderTitleContent = () => {
    return {
      title: '调度日志',
      content: null
    }
  }

  const showDetail = (record) => {
    setRecord(record);
    setVisible(true);
  }

  const onCancel = () => {
    setVisible(false)
  }

  const showLog = (record) => {
    setRecord(record);
    setLogVisible(true);
  }

  const onLogCancel = () => {
    setLogVisible(false)
  }

  const handleChange = ({ current, pageSize }) => {
    setPagination({
      current,
      pageSize
    })
    reloadData({
      page: current,
      size: pageSize
    })
  }

  return (
    <>
      <div className="table-header">
        <RenderTitle {...renderTitleContent()} />
        <SchDulingDetail visible={visible} record={record} onCancel={onCancel} />
        <SchDulingLog visible={logVisible} error={record.result} onCancel={onLogCancel} />
        <QueryForm {...queryFormText} defaultCollapse columns={getSchedulingLogQueryXForm(urlParams.taskCode ? true : false)} onChange={() => null} onReset={handleSubmit} onSearch={handleSubmit} initialValues={{}} isResetClearAll />
      </div>
      <div>
        <div className="table-content">
          <DTable
            loading={loading}
            rowKey="jobCode"
            dataSource={data}
            paginationProps={{
              position: 'bottomRight',
              showQuickJumper: true,
              total: total,
              showSizeChanger: true,
              pageSizeOptions: ['10', '20', '50', '100', '200', '500'],
              showTotal: (total) => `共 ${total} 条`,
              current: pagination.current,
              pageSize: pagination.pageSize
            }}
            attrs={{
              onChange: handleChange
            }}
            columns={getSchedulingLogColumns(reloadData, showDetail, showLog)}
            reloadData={reloadData}
          />
        </div>
      </div>
    </>
  )
}
