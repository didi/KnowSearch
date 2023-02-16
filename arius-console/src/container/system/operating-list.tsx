import React, { useState, useEffect, useRef } from "react";
import { getOperatingListQueryXForm, getOperationColumns } from "./config";
import { getlistModules, getUserRecordList, getTriggerWay, getOperationType } from "api/cluster-api";
import { DTable } from "component/dantd/dtable";
import { RenderTitle } from "component/render-title";
import QueryForm from "component/dantd/query-form";
import { queryFormText } from "constants/status-map";
import moment from "moment";
import { Dispatch } from "redux";
import * as actions from "actions";
import { connect } from "react-redux";
import { ProTable } from "knowdesign";
interface IOpRecord {
  value: number | string;
  title: string | number;
}
const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params)),
});

const Operating = (props) => {
  const department: string = localStorage.getItem("current-project");
  const [loading, setloading] = useState(false);
  const [modules, setModules] = useState([] as IOpRecord[]);
  const [triggerWay, setTriggerWay] = useState([] as IOpRecord[]);
  const [operationType, setOperationType] = useState([] as IOpRecord[]);
  const [data, setData] = useState([]);
  const [queryFormObject, setQueryFormObject] = useState({ page: 1, size: 10 } as any);
  const [total, setTotal] = useState(0);
  const [moduleId, setModuleId] = useState();
  const [form, setForm] = useState<any>();
  const [startAndEnd, setStartAndEnd] = useState([]);
  //const [resetValue, setResetValue] = useState(null);//
  const buttonTime = useRef(null);

  useEffect(() => {
    reloadData();
    getModules();
    _getTriggerWay();
    _getOperationType();
  }, [department]);

  useEffect(() => {
    reloadData();
  }, [queryFormObject]);

  const getModules = () => {
    getlistModules().then((res) => {
      let keys = Object.keys(res || {});
      let modules = keys.map((item) => {
        return {
          title: item,
          value: res[item],
        };
      });
      setModules(modules);
    });
  };

  const _getTriggerWay = async () => {
    let res = await getTriggerWay();
    let keys = Object.keys(res || {});
    let triggerWay = keys.map((item) => {
      return {
        title: item,
        value: res[item],
      };
    });
    setTriggerWay(triggerWay);
  };

  const _getOperationType = async (code?: number) => {
    let res = await getOperationType(code);
    let keys = Object.keys(res || {});
    let operationType = keys.map((item) => {
      return {
        title: res[item],
        value: Number(item),
      };
    });
    setOperationType(operationType);
  };

  const reloadData = () => {
    setloading(true);
    getUserRecordList({
      ...queryFormObject,
      beginTime: queryFormObject?.operateTime?.length ? queryFormObject?.operateTime[0]?.valueOf() : "",
      endTime: queryFormObject?.operateTime?.length ? queryFormObject?.operateTime[1]?.valueOf() : "",
    })
      .then((res) => {
        if (res?.bizData) {
          setData(res.bizData);
        }
        setTotal(res?.pagination?.total);
      })
      .finally(() => {
        setloading(false);
      });
  };

  const renderTitleContent = () => {
    return {
      title: "操作记录",
      content: null,
    };
  };

  const handleSubmit = (result) => {
    const copyResult = JSON.parse(JSON.stringify(result));
    // 增加点击刷新按钮先计算时间差 保持用户选择的事件范围
    //判断此时是否是自定义时间情况，如果是则不需要实时更新时间戳，反之不需要。
    const time = startAndEnd[1] - startAndEnd[0];
    const currentTime = new Date().getTime();
    const isCustomTime = buttonTime.current ? [currentTime - time, currentTime] : startAndEnd;
    copyResult.operateTime = isCustomTime;
    for (var key in copyResult) {
      if (copyResult[key] === "" || copyResult[key] === undefined) {
        delete copyResult[key];
      }
    }
    setQueryFormObject({ ...copyResult, page: 1, size: queryFormObject.size });
  };
  const resetSubmit = (result) => {
    buttonTime.current = false;
    setStartAndEnd([]);
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setQueryFormObject({ ...result, page: 1, size: queryFormObject.size });
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

  const handleChange = (pagination) => {
    setQueryFormObject({ ...queryFormObject, page: pagination.current, size: pagination.pageSize });
  };

  const clientHeight = document.querySelector("#d1-layout-main")?.clientHeight;

  return (
    <>
      <div className="table-layout-style">
        <ProTable
          showQueryForm={true}
          queryFormProps={{
            defaultCollapse: true,
            columns: getOperatingListQueryXForm({ modules, triggerWay, operationType }, handleTimeChange),
            onChange: (data) => {
              if (data?.[1]?.value !== moduleId) {
                _getOperationType(data?.[1]?.value);
                setModuleId(data?.[1]?.value);
                form.setFieldsValue({ operateId: undefined });
              }
            },
            onReset: resetSubmit,
            onSearch: handleSubmit,
            // initialValues={{}}
            isResetClearAll: true,
            showCollapseButton: false,
            getFormInstance: (form) => setForm(form),
          }}
          tableProps={{
            tableId: "operation_list_table",
            isCustomPg: false,
            loading,
            rowKey: "id",
            dataSource: data,
            columns: getOperationColumns(props.setDrawerId),
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
              current: queryFormObject.page,
              pageSize: queryFormObject.size,
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
    </>
  );
};

export const OperatingList = connect(null, mapDispatchToProps)(Operating);
