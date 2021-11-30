import React, { useState } from 'react';
import { getTaskListQueryXForm, mockData, getTaskListColumns } from './config';
import QueryForm from 'component/dantd/query-form';
import { getTaskList } from 'api/Scheduling';
// todo 接口好后增加类型判断
// import { ITask } from '@types/task-types';
import { queryFormText } from 'constants/status-map';
import { DTable } from 'component/dantd/dtable';
import { RenderTitle } from 'component/render-title';
import TaskListDetail from './../drawer/tasklist-detail'

export const TaskList = () => {
  const department: string = localStorage.getItem('current-project');
  const [loading, setloading] = useState(false);
  const [queryFromObject, setqueryFromObject] = useState(null);
  const [data, setData] = useState([] as any[]);
  // 控制抽屉的状态
  const [visible, setVisible] = useState(false);
  const [detailData, setDetailData] = useState({});
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

  React.useEffect(() => {
    reloadData();
  }, [department, queryFromObject, page]);

  const reloadData = () => {
    setloading(true);
    const params = {
      ...page,
      ...queryFromObject,
    }
    getTaskList(params).then((res: any) => {
      if (res) {
        setData(res.bizData);
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
      title: '任务列表',
      content: null
    }
  }

  const showDetail = (record) => {
    setVisible(true);
    setDetailData(record);
  }

  const onCancel = () => {
    setVisible(false)
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
        <TaskListDetail visible={visible} onCancel={onCancel} detailData={detailData}/>
        <QueryForm {...queryFormText} defaultCollapse columns={getTaskListQueryXForm()} onChange={() => null} onReset={handleSubmit} onSearch={handleSubmit} initialValues={{}} isResetClearAll />
      </div>
      <div>
        <div className="table-content">
          <DTable
            loading={loading}
            rowKey="id"
            dataSource={data}
            paginationProps={pagination}
            attrs={{
              onChange: handleChange,
              scroll: { x: 1170 }
            }}
            columns={getTaskListColumns(reloadData, showDetail)}
            reloadData={reloadData}
          />
        </div>
      </div>
    </>
  )
}
