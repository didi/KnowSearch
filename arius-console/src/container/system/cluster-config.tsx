import React, { useState } from "react";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getClusterCongigQueryXForm, getClusterCongigColumns } from "./config";
import { getDeployList } from "api/cluster-api";
import { DTable } from "component/dantd/dtable";
import { RenderTitle } from "component/render-title";
import QueryForm from "component/dantd/query-form";
import { queryFormText } from "constants/status-map";
import { Button, Tag } from "antd";
import { RiseOutlined } from "@ant-design/icons";
import { PlatformPermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import { ProTable } from "knowdesign";
const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

export const ClusterConfig = connect(
  null,
  mapDispatchToProps
)((props: any) => {
  const department: string = localStorage.getItem("current-project");
  const [loading, setloading] = useState(false);
  const [queryFormObject, setqueryFormObject] = useState(null);
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
        (d[k] + "").includes(queryFormObject[k]) ? "" : (b = false);
      });
      return b;
    });
    return filterData;
  };

  const reloadData = () => {
    setloading(true);
    getDeployList()
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
      title: "平台配置",
      content: null,
    };
  };

  const handleSubmit = (result) => {
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setqueryFormObject(result);
  };

  const getOpBtns = () => {
    return (
      <>
        {hasOpPermission(PlatformPermissions.PAGE, PlatformPermissions.ADD) && (
          <Button type="primary" className="ant-btn-primary" onClick={() => props.setModalId("clusterConfigModal", {}, reloadData)}>
            新增配置
          </Button>
        )}
        {/* <div
          style={{
            display: "inline-block",
            fontSize: 14,
            color: "#1473FF",
            letterSpacing: 0,
            textAlign: "right",
            marginLeft: 4,
            cursor: "pointer",
          }}
          onClick={() =>
            window.open(
              "https://github.com/didi/LogiEM/blob/master/doc/LogiEM%E7%94%A8%E6%88%B7%E6%8C%87%E5%8D%97.md#38-%E5%B9%B3%E5%8F%B0%E9%85%8D%E7%BD%AE"
            )
          }
        >
          指导文档
          <RiseOutlined />
        </div> */}
      </>
    );
  };

  const clientHeight = document.querySelector("#d1-layout-main")?.clientHeight;

  return (
    <>
      <div className="table-layout-style">
        <ProTable
          showQueryForm={true}
          queryFormProps={{
            defaultCollapse: true,
            columns: getClusterCongigQueryXForm(),
            onReset: handleSubmit,
            onSearch: handleSubmit,
            initialValues: queryFormObject,
            isResetClearAll: true,
          }}
          tableProps={{
            tableId: "cluster_config_table",
            isCustomPg: false,
            loading,
            rowKey: "id",
            dataSource: getData(),
            columns: getClusterCongigColumns(data, props.setModalId, reloadData),
            reloadData,
            getJsxElement: getOpBtns,
            customRenderSearch: () => (
              <div className="zeus-url">
                <RenderTitle {...renderTitleContent()} />{" "}
                <Tag
                  className="zeus-url-tag"
                  onClick={() =>
                    (window.open("about:blank").location.href =
                      "https://github.com/didi/LogiEM/blob/master/doc/LogiEM%E7%94%A8%E6%88%B7%E6%8C%87%E5%8D%97.md#38-%E5%B9%B3%E5%8F%B0%E9%85%8D%E7%BD%AE")
                  }
                >
                  指导文档
                </Tag>
              </div>
            ),
            attrs: {
              scroll: { x: "max-content" },
            },
          }}
        />
      </div>
      {/* <div className="table-header">
        <RenderTitle {...renderTitleContent()} />

        <QueryForm
          {...queryFormText}
          showCollapseButton={false}
          defaultCollapse
          columns={getClusterCongigQueryXForm()}
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
            attrs={{
              scroll: {
                x: 1700 - (13 - 6) * 120,
                y: clientHeight > 500 ? clientHeight - 200 : 300,
              },
            }}
            columns={getClusterCongigColumns(data, props.setModalId, reloadData)}
            reloadData={reloadData}
            renderInnerOperation={getOpBtns}
          />
        </div>
      </div> */}
    </>
  );
});
