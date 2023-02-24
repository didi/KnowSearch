import React, { useEffect, useState, useRef } from "react";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getTaskQueryXForm, getTaskColumns } from "./config";
import { getTaskList, getTaskType } from "api/task-api";
import { ITask } from "typesPath/task-types";
import { ProTable } from "knowdesign";
import { RenderTitle } from "component/render-title";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

export const TaskList = connect(
  null,
  mapDispatchToProps
)(() => {
  const department: string = localStorage.getItem("current-project");
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([] as ITask[]);
  const [startAndEnd, setStartAndEnd] = useState([]);
  const [queryFormObject, setQueryFormObject]: any = useState({ current: 1, size: 10 });
  const [total, setTotal] = useState(0);
  const [typeList, setTypeList] = useState([]);

  const buttonTime = useRef(null);

  useEffect(() => {
    _getTaskType();
  }, []);

  useEffect(() => {
    reloadData();
  }, [department, queryFormObject]);

  const _getTaskType = async () => {
    let typeList = await getTaskType();
    setTypeList(typeList);
  };

  const reloadData = () => {
    setLoading(true);
    const params = {
      page: queryFormObject.current,
      size: queryFormObject.size,
      startTime: startAndEnd.length ? startAndEnd[0] : undefined,
      endTime: startAndEnd.length ? startAndEnd[1] : undefined,
      title: queryFormObject.title,
      sortTerm: queryFormObject.sortTerm,
      orderByDesc: queryFormObject.orderByDesc,
    };
    getTaskList(params)
      .then((res) => {
        if (res) {
          res.bizData =
            (res?.bizData || []).map((ele, index) => {
              return {
                ...ele,
                key: index,
              };
            }) || [];
          setData(res?.bizData);
          setTotal(res?.pagination?.total);
        }
      })
      .finally(() => {
        setLoading(false);
      });
  };

  const handleSubmit = (result) => {
    const copyResult = JSON.parse(JSON.stringify(result));
    // 增加点击刷新按钮先计算时间差 保持用户选择的事件范围
    // 判断此时是否是自定义时间情况，如果是则不需要实时更新时间戳，反之不需要。
    const time = startAndEnd[1] - startAndEnd[0];
    const currentTime = new Date().getTime();
    const isCustomTime = buttonTime.current ? [currentTime - time, currentTime] : startAndEnd;
    copyResult.createTime = isCustomTime;
    for (var key in copyResult) {
      if (copyResult[key] === "" || copyResult[key] == undefined) {
        delete copyResult[key];
      }
    }
    setQueryFormObject({ ...copyResult, size: queryFormObject.size, current: 1 });
  };

  const resetSubmit = (result) => {
    setStartAndEnd([]);
    buttonTime.current = false;
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setQueryFormObject({ ...result, size: queryFormObject.size, current: 1 });
  };
  const handleTimeChange = (times: number[], periodOrPicker: boolean) => {
    //periodOrPicker为true表示此时时间选择器选的是period，false表示Picker
    if (times) {
      setStartAndEnd(times);
      buttonTime.current = periodOrPicker;
    }
  };

  const handleChange = (pagination, filters, sorter) => {
    const sorterObject: { [key: string]: any } = {};
    // 排序
    if (sorter.columnKey && sorter.order) {
      switch (sorter.columnKey) {
        case "createTime":
          sorterObject.sortTerm = "create_time";
          sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
          break;
        case "updateTime":
          sorterObject.sortTerm = "update_time";
          sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
          break;
        default:
          break;
      }
    }
    setQueryFormObject((state) => {
      if (!sorter.order) {
        delete state.sortTerm;
        delete state.orderByDesc;
      }
      return {
        ...state,
        ...sorterObject,
        current: pagination.current,
        size: pagination.pageSize,
      };
    });
  };

  const renderTitleContent = () => {
    return {
      title: "任务中心",
      content: null,
    };
  };

  return (
    <div className="table-layout-style">
      <ProTable
        showQueryForm={true}
        queryFormProps={{
          defaultCollapse: true,
          columns: getTaskQueryXForm(handleTimeChange),
          // onChange={() => null}
          onReset: resetSubmit,
          onSearch: handleSubmit,
          // initialValues={{}}
          isResetClearAll: true,
        }}
        tableProps={{
          tableId: "work_order_table",
          isCustomPg: false,
          loading,
          rowKey: "id",
          dataSource: data,
          columns: getTaskColumns(reloadData, typeList),
          reloadData,
          isDividerHide: false,
          customRenderSearch: () => <RenderTitle {...renderTitleContent()} />,
          paginationProps: {
            position: "bottomRight",
            showQuickJumper: true,
            total: total,
            showSizeChanger: true,
            pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
            showTotal: (total: number) => `共 ${total} 条`,
            current: queryFormObject.current,
          },
          attrs: {
            onChange: handleChange,
            scroll: {
              x: "max-content",
            },
          },
        }}
      />
    </div>
  );
});
