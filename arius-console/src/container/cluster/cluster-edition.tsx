import React, { useRef, useState } from "react";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getEditionQueryXForm, getVersionsColumns } from "./config";
import { getPackageList } from "api/cluster-api";
import { DTable, ITableBtn } from "component/dantd/dtable";
import { RenderTitle } from "component/render-title";
import QueryForm from "component/dantd/query-form";
import moment from "moment";
import { queryFormText } from "constants/status-map";
import { isOpenUp } from "constants/common";
import { ClusterVersionPermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import { ProTable } from "knowdesign";
const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params, cb)),
});

export const EditionCluster = connect(
  null,
  mapDispatchToProps
)((props: any) => {
  const department: string = localStorage.getItem("current-project");
  const [loading, setloading] = useState(false);
  const [queryFormObject, setqueryFormObject] = useState([]);
  const [data, setData] = useState([]);
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
        } else {
          (d[k] + "")?.toLowerCase().includes(queryFormObject[k]) ? "" : (b = false);
        }
      });
      return b;
    });
    return filterData;
  };

  const reloadData = () => {
    setloading(true);
    getPackageList()
      .then((res) => {
        if (res) {
          setData(res);
        }
      })
      .finally(() => {
        setloading(false);
      });
  };

  const renderTitleContent = () => {
    return {
      title: "集群版本",
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
    copyResult.createTime = isCustomTime;
    for (var key in copyResult) {
      if (copyResult[key] === "" || copyResult[key] === undefined) {
        delete copyResult[key];
      }
    }
    setqueryFormObject(copyResult);
  };
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

  const clientHeight = document.querySelector("#d1-layout-main")?.clientHeight;

  return (
    <div className="table-layout-style">
      <ProTable
        showQueryForm={true}
        queryFormProps={{
          // layout: "inline", //没有label的查询条件
          // colMode: "style", //col默认设计样式
          totalNumber: data.length || 0, //传入总条数
          defaultCollapse: true,
          columns: getEditionQueryXForm(data, handleTimeChange),
          // onChange={() => null}
          onReset: resetSubmit,
          onSearch: handleSubmit,
          // initialValues={{}}
          isResetClearAll: true,
        }}
        tableProps={{
          tableId: "cluster_edition_list", //开启表格自定义列
          isCustomPg: false,
          loading,
          rowKey: "id",
          dataSource: getData(),
          columns: getVersionsColumns(props.setDrawerId, reloadData),
          isDividerHide: false,
          reloadData,
          customRenderSearch: () => <RenderTitle {...renderTitleContent()} />,
          attrs: {
            scroll: { x: "max-content" },
          },
        }}
      />
    </div>
  );
});
