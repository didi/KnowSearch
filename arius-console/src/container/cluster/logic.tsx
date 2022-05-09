import React, { useState } from "react";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getLogicClusterQueryXForm, getLogicColumns } from "./config";
import { getOpLogicClusterList, ILogicLike } from "api/cluster-api";
import { DTable, ITableBtn } from "component/dantd/dtable";
import { RenderTitle } from "component/render-title";
import QueryForm from "component/dantd/query-form";
import { queryFormText } from "constants/status-map";
import { isOpenUp } from "constants/common";

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

  React.useEffect(() => {
    reloadData();
  }, [department, queryFormObject]);

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
      appId: queryFormObject.appId,
      type: queryFormObject.type,
      sortTerm: queryFormObject.sortTerm,
      orderByDesc: queryFormObject.orderByDesc,
    };
    getOpLogicClusterList(Params)
      .then((res) => {
        if (res) {
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
      title: "逻辑集群",
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
      {
        className: "ant-btn-primary",
        label: "申请集群",
        isOpenUp: isOpenUp,
        clickFunc: () => props.setModalId("applyCluster", {}, reloadData),
      },
    ];
  };

  const handleChange = (pagination, filters, sorter) => {
    const sorterObject: { [key: string]: any } = {};
    // 排序
    if (sorter.columnKey && sorter.order) {
      switch (sorter.columnKey) {
        case "level":
          sorterObject.sortTerm = "level";
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

  return (
    <>
      <div className="table-header">
        <RenderTitle {...renderTitleContent()} />

        <QueryForm
          {...queryFormText}
          defaultCollapse
          columns={getLogicClusterQueryXForm(data)}
          onChange={() => null}
          onReset={handleSubmit}
          onSearch={handleSubmit}
          initialValues={{}}
          isResetClearAll
        />
      </div>
      <div>
        <div className="table-content">
          <DTable
            loading={loading}
            rowKey="id"
            dataSource={data}
            attrs={{
              onChange: handleChange,
              scroll: {
                x: true
              }
            }}
            key={JSON.stringify({
              authType: queryFormObject.authType,
              name: queryFormObject.name,
              health: queryFormObject.health,
              appId: queryFormObject.appId,
              type: queryFormObject.type,
            })}
            columns={getLogicColumns(data, props.setModalId, reloadData)}
            reloadData={reloadData}
            getOpBtns={getOpBtns}
            paginationProps={{
              position: "bottomRight",
              showQuickJumper: true,
              total: total,
              showSizeChanger: true,
              pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
              showTotal: (total) => `共 ${total} 条`,
              current: queryFormObject.current
            }}
          />
        </div>
      </div>
    </>
  );
};
export const LogicCluster = connect(null, mapDispatchToProps)(LogicClusterBox);
