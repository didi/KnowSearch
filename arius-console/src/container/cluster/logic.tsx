import React, { useState } from "react";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getLogicClusterQueryXForm, getLogicColumns } from "./config";
import { getOpLogicClusterList, ILogicLike, getCount, getMyLogiClusterList } from "api/cluster-api";
import { DTable, ITableBtn } from "component/dantd/dtable";
import { RenderTitle } from "component/render-title";
import QueryForm from "component/dantd/query-form";
import { queryFormText } from "constants/status-map";
import { isOpenUp } from "constants/common";
import { MyClusterPermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import Url from "lib/url-parser";
import { ProTable } from "knowdesign";
const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

const LogicClusterBox = (props) => {
  const department: string = localStorage.getItem("current-project");
  const [loading, setloading] = useState(false);
  const [queryFormObject, setqueryFormObject]: any = useState({
    current: 1,
    size: 10,
  });
  const [data, setData] = useState([]);
  const [total, setTotal] = useState(0);
  const [logiClusterList, setLogiClusterList] = useState([]);

  const indexTemFun = async (record) => {
    let indexCount = null;
    await getCount(record.id).then((res) => {
      if (res) {
        indexCount = res;
      }
    });
    return indexCount;
  };
  React.useEffect(() => {
    reloadData();
  }, [department, queryFormObject, Url().search.needApplyCluster]);

  React.useEffect(() => {
    getLogiClusterList();
  }, [department]);

  const getLogiClusterList = () => {
    getMyLogiClusterList().then((res = []) => {
      const list = res.map((item) => ({ title: item, value: item }));
      setLogiClusterList(list);
    });
  };

  // const getData = () => {
  //   // 查询项的key 要与 数据源的key  对应
  //   if (!queryFormObject) return data;
  //   const keys = Object.keys(queryFormObject);
  //   const filterData = data.filter((d) => {
  //     let b = true;
  //     keys.forEach((k: string) => {
  //       if (k === "clusterStatus") {
  //         (d[k].status + "")?.toLowerCase().includes(queryFormObject[k])
  //           ? ""
  //           : (b = false);
  //       } else if (k === "authType") {
  //         d[k] === queryFormObject[k] ? "" : (b = false);
  //       } else {
  //         (d[k] + "")?.toLowerCase().includes(queryFormObject[k])
  //           ? ""
  //           : (b = false);
  //       }
  //     });
  //     return b;
  //   });
  //   return filterData;
  // };

  const reloadData = () => {
    setloading(true);
    const Params: ILogicLike = {
      page: queryFormObject.current,
      size: queryFormObject.size,
      authType: queryFormObject.authType,
      name: queryFormObject.name,
      health: queryFormObject.health,
      type: queryFormObject.type,
      sortTerm: queryFormObject.sortTerm,
      orderByDesc: queryFormObject.orderByDesc,
      id: queryFormObject.id !== undefined ? +queryFormObject.id : undefined,
      memo: queryFormObject.memo,
    };
    getOpLogicClusterList(Params)
      .then((res) => {
        if (res) {
          if (Url().search.hasOwnProperty("needApplyCluster") && !res.bizData?.length) {
            props.setModalId("applyCluster", { history: props.history }, reloadData);
          }
          res.bizData = res?.bizData?.map((item) => {
            item.esClusterVersions = item.esClusterVersions?.join(",") || "_";
            return item;
          });
          setData(res?.bizData);
          setTotal(res?.pagination?.total);
        }
      })
      .finally(() => {
        setloading(false);
      });
  };

  const renderTitleContent = () => {
    return {
      title: "我的集群",
      content: null,
    };
  };

  const handleSubmit = (result) => {
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setqueryFormObject({ ...result, size: queryFormObject.size, current: 1 });
  };

  const getOpBtns = (): ITableBtn[] => {
    return [
      hasOpPermission(MyClusterPermissions.PAGE, MyClusterPermissions.APPLY) && {
        className: "ant-btn-primary",
        label: "申请集群",
        isOpenUp: isOpenUp,
        clickFunc: () => props.setModalId("applyCluster", { history: props.history }, reloadData),
      },
    ].filter(Boolean);
  };

  const handleChange = (pagination, filters, sorter) => {
    const sorterObject: { [key: string]: any } = {};
    // 排序
    if (sorter.columnKey && sorter.order) {
      switch (sorter.columnKey) {
        case "type":
          sorterObject.sortTerm = "type";
          sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
          break;
        case "esClusterVersion":
          sorterObject.sortTerm = "es_cluster_version";
          sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
          break;
        case "level":
          sorterObject.sortTerm = "level";
          sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
          break;
        case "diskInfo":
          sorterObject.sortTerm = "disk_usage_percent";
          sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
          break;
        case "dataNodeNum":
          sorterObject.sortTerm = "data_node_num";
          sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
          break;
        default:
          break;
      }
    }
    setqueryFormObject((state) => {
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

  const clientHeight = document.querySelector("#d1-layout-main")?.clientHeight;

  return (
    <>
      <div className="table-layout-style">
        <ProTable
          showQueryForm={true}
          queryFormProps={{
            defaultCollapse: true,
            columns: getLogicClusterQueryXForm(data, logiClusterList),
            onReset: handleSubmit,
            onSearch: handleSubmit,
            isResetClearAll: true,
            showCollapseButton: false,
          }}
          tableProps={{
            tableId: "logic_cluster_list", //开启表格自定义列
            isCustomPg: false,
            loading,
            rowKey: "id",
            dataSource: data,
            columns: getLogicColumns(data, props.setModalId, reloadData, props, indexTemFun),
            reloadData,
            getOpBtns: getOpBtns,
            customRenderSearch: () => <RenderTitle {...renderTitleContent()} />,
            paginationProps: {
              position: "bottomRight",
              showQuickJumper: true,
              total: total,
              showSizeChanger: true,
              pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
              showTotal: (total) => `共 ${total} 条`,
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
    </>
  );
};
export const LogicCluster = connect(null, mapDispatchToProps)(LogicClusterBox);
