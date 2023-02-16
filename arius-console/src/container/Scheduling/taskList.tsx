import React, { useState, useEffect } from "react";
import { getTaskListQueryXForm, getTaskListColumns } from "./config";
import QueryForm from "component/dantd/query-form";
import { getTaskList } from "api/Scheduling";
// todo 接口好后增加类型判断
// import { ITask } from '@types/task-types';
import { queryFormText } from "constants/status-map";
import { DTable } from "component/dantd/dtable";
import { RenderTitle } from "component/render-title";
import TaskListDetail from "./../drawer/tasklist-detail";
import { ProTable } from "knowdesign";
import { cloneDeep } from "lodash";

export const TaskList = () => {
  const department: string = localStorage.getItem("current-project");
  const [loading, setloading] = useState(false);
  const [queryFormObject, setqueryFormObject]: any = useState({
    page: 1,
    size: 10,
    current: 1,
  });
  const [data, setData] = useState([] as any[]);
  // 控制抽屉的状态
  const [visible, setVisible] = useState(false);
  const [detailData, setDetailData] = useState({});
  const [total, setTotal] = useState(0);

  useEffect(() => {
    reloadData();
  }, [department, queryFormObject]);

  const reloadData = () => {
    setloading(true);
    const params = cloneDeep(queryFormObject);
    delete params.current;
    getTaskList(params)
      .then((res: any) => {
        if (res) {
          setData(res.bizData);
          setTotal(res.pagination.total);
        }
      })
      .finally(() => {
        setloading(false);
      });
  };

  const handleSubmit = (result) => {
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setqueryFormObject({ ...result, page: 1, size: queryFormObject.size, current: 1 });
  };

  const renderTitleContent = () => {
    return {
      title: "任务列表",
      content: null,
    };
  };

  const showDetail = (record) => {
    setVisible(true);
    setDetailData(record);
  };

  const onCancel = () => {
    setVisible(false);
  };

  const handleChange = (pagination) => {
    setqueryFormObject({
      ...queryFormObject,
      size: pagination.pageSize,
      page: pagination.current,
      current: pagination.current,
    });
  };

  return (
    <>
      <div className="table-layout-style">
        <ProTable
          showQueryForm={true}
          queryFormProps={{
            defaultCollapse: true,
            columns: getTaskListQueryXForm(),
            // onChange={() => null}
            onReset: handleSubmit,
            onSearch: handleSubmit,
            // initialValues={{}}
            isResetClearAll: true,
          }}
          tableProps={{
            tableId: "scheduling_task_table",
            isCustomPg: false,
            loading,
            rowKey: "id",
            dataSource: data,
            columns: getTaskListColumns(reloadData, showDetail),
            reloadData,
            isDividerHide: false,
            customRenderSearch: () => <RenderTitle {...renderTitleContent()} />,
            paginationProps: {
              total,
              pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
              showTotal: (total) => `共 ${total} 条`,
              current: queryFormObject.current,
              pageSize: queryFormObject.size,
            },
            attrs: {
              onChange: handleChange,
            },
          }}
        />
        <TaskListDetail visible={visible} onCancel={onCancel} detailData={detailData} />
      </div>
    </>
  );
};
