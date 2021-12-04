import React, { useState, useEffect } from 'react';
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
  const [queryFromObject, setqueryFromObject] = useState(null);
  const [data, setData] = useState([] as any[]);
  // 控制调度日志的状态
  const [visible, setVisible] = useState(false);
  const [record, setRecord] = useState({});
  // 控制执行日志的状态
  const [logVisible, setLogVisible] = useState(false);
  const [urlParams, setUrlParams]: any = useState(getUrlParams().search);
  const [pagination, setPagination] = useState({
    position: 'bottomRight',
    showQuickJumper: true,
    total: 0,
    showSizeChanger: true,
    pageSizeOptions: ['10', '20', '50', '100', '200', '500'],
    showTotal: (total) => `共 ${total} 条`,
  });
  const [page, setPage] = useState({
    page: 1,
    size: 10,
  })

  useEffect(() => {
    reloadData();
  }, [department, urlParams, page, queryFromObject]);

  const reloadData = () => {
    setloading(true)
    const params = {
      ...page,
      ...urlParams,
      ...queryFromObject,
      beginTime: queryFromObject?.createTime?.length ? queryFromObject?.createTime[0]?.valueOf() : '',
      endTime: queryFromObject?.createTime?.length ? queryFromObject?.createTime[1]?.valueOf() : '',
    }
    getLogsList(params).then((res: any) => {
      if (res) {
        setData(res?.bizData);
        setPagination({
          position: 'bottomRight',
          total: res.pagination.total,
          showQuickJumper: true,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50', '100', '200', '500'],
          showTotal: (total) => `共 ${total} 条`,
        });
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

  const handleChange = (pagination) => {
    setPage({
      page: pagination.current,
      size: pagination.pageSize,
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
            paginationProps={pagination}
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
