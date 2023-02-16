import React, { useState, useEffect, useMemo, useRef } from "react";
import { getSchedulingLogQueryXForm, getSchedulingLogColumns, mockData } from "./config";
import QueryForm from "component/dantd/query-form";
import { getLogsList } from "api/Scheduling";
// todo 接口好后增加类型判断
// import { ITask } from '@types/task-types';
import { queryFormText } from "constants/status-map";
import { DTable } from "component/dantd/dtable";
import { RenderTitle } from "component/render-title";
import SchDulingDetail from "./../drawer/scheduling-detail";
import SchDulingLog from "./../drawer/scheduling-log";
import getUrlParams from "lib/url-parser";
import { ProTable } from "knowdesign";
export const Schedulinglog = () => {
  const department: string = localStorage.getItem("current-project");
  const [loading, setloading] = useState(false);
  const [queryFormObject, setqueryFormObject]: any = useState({});
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
  });
  const [data, setData] = useState([] as any[]);
  // 控制调度日志的状态
  const [visible, setVisible] = useState(false);
  const [record, setRecord]: any = useState({});
  // 控制执行日志的状态
  const [logVisible, setLogVisible] = useState(false);
  const [urlParams, setUrlParams]: any = useState(getUrlParams().search);
  const [total, setTotal] = useState(0);
  const [startAndEnd, setStartAndEnd] = useState([]);
  //const [resetValue, setResetValue] = useState(null);
  const buttonTime = useRef(null);
  useEffect(() => {
    reloadData({});
  }, [department, urlParams, queryFormObject]);
  // React.useEffect(() => {
  //   //setResetValue初始化只会执行一次
  //   if (resetValue) {
  //     for (var i in resetValue) {
  //       resetValue[i](undefined)
  //     }
  //   }
  // }, [resetValue]);
  const reloadData = ({ page = pagination.current, size = pagination.pageSize }) => {
    setloading(true);
    const params = {
      sortName: "create_time",
      sortAsc: "desc",
      ...urlParams,
      ...queryFormObject,
      page,
      size,
      beginTime: queryFormObject?.createTime?.length ? queryFormObject?.createTime[0]?.valueOf() : "",
      endTime: queryFormObject?.createTime?.length ? queryFormObject?.createTime[1]?.valueOf() : "",
    };
    getLogsList(params)
      .then((res: any) => {
        if (res) {
          setData(res?.bizData);
          setTotal(res.pagination.total);
        }
      })
      .finally(() => {
        setloading(false);
      });
  };

  const handleSubmit = (result) => {
    // 增加点击刷新按钮先计算时间差 保持用户选择的事件范围
    //判断此时是否是自定义时间情况，如果是则不需要实时更新时间戳，反之不需要。
    const copyResult = JSON.parse(JSON.stringify(result));
    const time = startAndEnd[1] - startAndEnd[0];
    const currentTime = new Date().getTime();
    const isCustomTime = buttonTime.current ? [currentTime - time, currentTime] : startAndEnd;
    copyResult.createTime = isCustomTime;
    for (var key in copyResult) {
      if (copyResult[key] === "" || copyResult[key] === undefined) {
        delete copyResult[key];
      }
    }
    setPagination({ ...pagination, current: 1 });
    setqueryFormObject({ ...copyResult });
  };

  const handleTimeChange = (times: number[], periodOrPicker: boolean) => {
    //periodOrPicker为true表示此时时间选择器选的是period，false表示Picker
    if (times) {
      setStartAndEnd(times);
      buttonTime.current = periodOrPicker;
    }
  };
  // const resetAllValue = (obj = {}) => {
  //   setResetValue({ ...obj })
  // }
  const resetSubmit = (result) => {
    buttonTime.current = false;
    setStartAndEnd([]);
    // if (resetValue) {
    //   for (var i in resetValue) {
    //     resetValue[i](undefined)
    //   }
    // }
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setqueryFormObject(result);
  };

  const renderTitleContent = () => {
    return {
      title: "调度日志",
      content: null,
    };
  };

  const showDetail = (record) => {
    setRecord(record);
    setVisible(true);
  };

  const onCancel = () => {
    setVisible(false);
  };

  const showLog = (record) => {
    setRecord(record);
    setLogVisible(true);
  };

  const onLogCancel = () => {
    setLogVisible(false);
  };

  const handleChange = ({ current, pageSize }, filters, sorter) => {
    setPagination({
      current,
      pageSize,
    });
    const sorterObject: { [key: string]: any } = {};
    if (sorter.columnKey && sorter.order) {
      switch (sorter.columnKey) {
        case "createTime":
          sorterObject.sortName = "create_time";
          sorterObject.sortAsc = sorter.order === "ascend" ? "asc" : "desc";
          break;
        case "status":
          sorterObject.sortName = "status";
          sorterObject.sortAsc = sorter.order === "ascend" ? "asc" : "desc";
          break;
        case "startTime":
          sorterObject.sortName = "start_time";
          sorterObject.sortAsc = sorter.order === "ascend" ? "asc" : "desc";
          break;
        case "endTime":
          sorterObject.sortName = "end_time";
          sorterObject.sortAsc = sorter.order === "ascend" ? "asc" : "desc";
          break;
        case "result":
          sorterObject.sortName = "result";
          sorterObject.sortAsc = sorter.order === "ascend" ? "asc" : "desc";
          break;
        default:
          break;
      }
    }
    setqueryFormObject((state) => {
      if (!sorter.order) {
        delete state.sortName;
        delete state.sortAsc;
      }
      return {
        ...state,
        ...sorterObject,
        page: current,
        size: pageSize,
      };
    });
  };

  return (
    <>
      <div className="table-layout-style">
        <ProTable
          showQueryForm={true}
          queryFormProps={{
            defaultCollapse: true,
            columns: getSchedulingLogQueryXForm(urlParams.taskCode ? true : false, handleTimeChange),
            // onChange={() => null}
            onReset: resetSubmit,
            onSearch: handleSubmit,
            // initialValues={{}}
            isResetClearAll: true,
          }}
          tableProps={{
            tableId: "scheduling_table",
            isCustomPg: false,
            loading,
            rowKey: "id",
            dataSource: data,
            columns: getSchedulingLogColumns(reloadData, showDetail, showLog),
            reloadData,
            isDividerHide: false,
            customRenderSearch: () => <RenderTitle {...renderTitleContent()} />,
            paginationProps: {
              position: "bottomRight",
              showQuickJumper: true,
              total: total,
              showSizeChanger: true,
              pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
              showTotal: (total) => `共 ${total} 条`,
              current: pagination.current,
              pageSize: pagination.pageSize,
            },
            attrs: {
              onChange: handleChange,
              scroll: { x: 1160, y: 600 },
            },
          }}
        />
        <SchDulingDetail visible={visible} record={record} onCancel={onCancel} />
        <SchDulingLog visible={logVisible} error={record.result} onCancel={onLogCancel} />
      </div>
    </>
  );
};
