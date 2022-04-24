import React, { useState } from "react";
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

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setModalId(modalId, params, cb)),
});

export const EditionCluster = connect(
  null,
  mapDispatchToProps
)((props: any) => {
  const department: string = localStorage.getItem("current-project");
  const [loading, setloading] = useState(false);
  const [queryFormObject, setqueryFormObject] = useState([]);
  const [data, setData] = useState([]);

  React.useEffect(() => {
    reloadData();
  }, [department]);

  const getData = () => {
    // 查询项的key 要与 数据源的key  对应
    if (!queryFormObject) return data;
    const keys = Object.keys(queryFormObject);
    const filterData = data.filter((d) => {
      let b = true;
      keys.forEach((k: string) => {
        if (k === "createTime") {
          const time = moment(d[k]).unix();
          if (queryFormObject[k][0] > time || time > queryFormObject[k][1]) {
            b = false;
          }
        } else {
          (d[k] + "")?.toLowerCase().includes(queryFormObject[k])
            ? ""
            : (b = false);
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
    result.createTime = result.createTime?.map((item) => {
      return item.unix();
    });
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setqueryFormObject(result);
  };

  const getOpBtns = (): ITableBtn[] => {
    return [
      {
        label: "新增版本",
        isOpenUp: isOpenUp,
        className: "ant-btn-primary",
        clickFunc: () => props.setModalId("addPackageModal", { manifest: 4, addPackage: true }, reloadData),
      },
    ];
  };

  return (
    <>
      <div className="table-header">
        <RenderTitle {...renderTitleContent()} />

        <QueryForm
          {...queryFormText}
          defaultCollapse
          columns={getEditionQueryXForm(data)}
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
            dataSource={getData()}
            columns={getVersionsColumns(props.setModalId, reloadData)}
            reloadData={reloadData}
            getOpBtns={getOpBtns}
          // tableHeaderSearchInput={{submit: handleSubmit}}
          />
        </div>
      </div>
    </>
  );
});
