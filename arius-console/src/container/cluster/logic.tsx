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
  const [queryFromObject, setqueryFromObject]: any = useState({
    from: 0,
    size: 10,
  });
  const [data, setData] = useState([]);
  const [total, setTotal] = useState(0);

  React.useEffect(() => {
    reloadData();
  }, [department, queryFromObject]);

  // const getData = () => {
  //   // 查询项的key 要与 数据源的key  对应
  //   if (!queryFromObject) return data;
  //   const keys = Object.keys(queryFromObject);
  //   const filterData = data.filter((d) => {
  //     let b = true;
  //     keys.forEach((k: string) => {
  //       if (k === "clusterStatus") {
  //         (d[k].status + "")?.toLowerCase().includes(queryFromObject[k])
  //           ? ""
  //           : (b = false);
  //       } else if (k === "authType") {
  //         d[k] === queryFromObject[k] ? "" : (b = false);
  //       } else {
  //         (d[k] + "")?.toLowerCase().includes(queryFromObject[k])
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
      from: queryFromObject.from,
      size: queryFromObject.size,
      authType: queryFromObject.authType,
      name: queryFromObject.name,
      health: queryFromObject.health,
      appId: queryFromObject.appId,
      type: queryFromObject.type,
      sortTerm: queryFromObject.sortTerm,
      orderByDesc: queryFromObject.orderByDesc,
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
    setqueryFromObject({ ...result, from: 0, size: 10 });
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
    setqueryFromObject((state) => {
      if (!sorter.order) {
        delete state.sortTerm;
        delete state.orderByDesc;
      }

      return {
        ...state,
        ...sorterObject,
        from: (pagination.current - 1) * pagination.pageSize,
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
            }}
            key={JSON.stringify({
              authType: queryFromObject.authType,
              name: queryFromObject.name,
              health: queryFromObject.health,
              appId: queryFromObject.appId,
              type: queryFromObject.type,
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
            }}
          />
        </div>
      </div>
    </>
  );
};
export const LogicCluster = connect(null, mapDispatchToProps)(LogicClusterBox);
