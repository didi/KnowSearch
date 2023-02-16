import React, { useState, useRef } from "react";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getMyApplicationQueryXForm, getMyApplicationColumns } from "./config";
import QueryForm from "component/dantd/query-form";
import { getApprovalOrderList, getTypeEnums } from "api/order-api";
import { ITypeEnums } from "typesPath/cluster/order-types";
import { IStringMap } from "interface/common";
import { queryFormText } from "constants/status-map";
import { DTable } from "component/dantd/dtable";
import moment from "moment";
import { ProTable } from "knowdesign";
import { RenderTitle } from "component/render-title";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

export const MyApproval = connect(
  null,
  mapDispatchToProps
)(() => {
  const department: string = localStorage.getItem("current-project");
  const [loading, setloading] = useState(false);
  const [queryFormObject, setqueryFormObject] = useState({ status: "0" });
  const [data, setData] = useState([]);
  const [typeList, setTypeList] = useState([] as ITypeEnums[]);
  const [typeEnums, setTypeEnums] = useState({} as IStringMap);
  const [startAndEnd, setStartAndEnd] = useState([]);
  //const [resetValue, setResetValue] = useState(null);
  const buttonTime = useRef(null);
  React.useEffect(() => {
    reloadData();
  }, [department]);
  // React.useEffect(() => {
  //   //setResetValue初始化只会执行一次
  //   if (resetValue) {
  //     for (var i in resetValue) {
  //       resetValue[i](undefined)
  //     }
  //   }
  // }, [resetValue]);
  React.useEffect(() => {
    getTypeEnumsFn();
  }, []);

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
  };

  const getData = () => {
    // 查询项的key 要与 数据源的key  对应
    if (!queryFormObject) return data;
    const keys = Object.keys(queryFormObject);
    const filterData = data.filter((d) => {
      let b = true;
      keys.forEach((k: string) => {
        if (k === "createTime") {
          const time = moment(d[k]).unix() * 1000;
          if (queryFormObject[k][0] > time || time > queryFormObject[k][1]) {
            b = false;
          }
        } else if (k === "type") {
          d[k] === queryFormObject[k] ? "" : (b = false);
        } else {
          (d[k] + "")?.includes(queryFormObject[k]) ? "" : (b = false);
        }
      });
      return b;
    });
    return filterData;
  };

  const reloadData = () => {
    setloading(true);
    getApprovalOrderList(1)
      .then((res) => {
        if (res) {
          setData(res);
        }
      })
      .finally(() => {
        setloading(false);
      });
  };

  const handleSubmit = (result) => {
    const copyResult = JSON.parse(JSON.stringify(result));
    // 增加点击刷新按钮先计算时间差 保持用户选择的事件范围
    //判断此时是否是自定义时间情况，如果是则不需要实时更新时间戳，反之不需要。
    const time = startAndEnd[1] - startAndEnd[0];
    const currentTime = new Date().getTime();
    const isCustomTime = buttonTime.current ? [currentTime - time, currentTime] : startAndEnd;
    copyResult.createTime = isCustomTime;
    for (var key in copyResult) {
      if (copyResult[key] === "" || copyResult[key] === undefined) {
        delete copyResult[key];
      }
    }
    setqueryFormObject(copyResult);
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
    setStartAndEnd([]);
    buttonTime.current = false;
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

  const initialValues = {
    status: ["0"], // 默认审批
  };
  const renderTitleContent = () => {
    return {
      title: "我的审批",
      content: null,
    };
  };

  return (
    <div className="table-layout-style">
      <ProTable
        showQueryForm={true}
        queryFormProps={{
          defaultCollapse: true,
          columns: getMyApplicationQueryXForm(typeList, handleTimeChange),
          // onChange={() => null}
          onReset: resetSubmit,
          onSearch: handleSubmit,
          initialValues: initialValues,
          isResetClearAll: true,
        }}
        tableProps={{
          tableId: "my_approval_table",
          isCustomPg: false,
          loading,
          rowKey: "id",
          dataSource: getData(),
          columns: getMyApplicationColumns(typeEnums, "approval"),
          customRenderSearch: () => <RenderTitle {...renderTitleContent()} />,
          reloadData,
          isDividerHide: false,
        }}
      />
    </div>
  );
});
