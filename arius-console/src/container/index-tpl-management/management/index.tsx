import { getAllIndexList, IAllIndexList, getClusterPerApp, getIndexDataType } from "api/cluster-index-api";
import { RenderTitle } from "component/render-title";
import React, { useState } from "react";
import { getLogicIndexColumns, getQueryFormConfig } from "./config";
import { LOGIC_INDEX_TITLE } from "./constants";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { ProTable, Button, Spin } from "knowdesign";
import { initPaginationProps } from "constants/table";
import { TempletPermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import { RenderEmpty } from "component/LogClusterEmpty";
import { isSuperApp } from "lib/utils";
import { uuid } from "lib/utils";
import "./index.less";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (drawerId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(drawerId, params, cb)),
});

export const IndexTplManagement = connect(
  null,
  mapDispatchToProps
)((props: { setModalId: Function; setDrawerId: Function; history: any }) => {
  const department: string = localStorage.getItem("current-project");
  const [loading, setloading] = useState(false);
  const superApp = isSuperApp();
  const [queryFormObject, setqueryFormObject]: any = useState({
    page: 1,
    size: 10,
    showMetadata: !superApp,
  });
  const [tableData, setTableData] = useState([]);
  const [paginationProps, setPaginationProps] = useState(initPaginationProps());
  const [cluster, setCluster] = useState([]);
  const [dataTypeList, setDataTypeList] = useState([]);
  const [pageLoad, setPageLoad] = useState(false);
  const [sorter, setSorter] = useState({});

  React.useEffect(() => {
    reloadData();
  }, [department, queryFormObject]);

  React.useEffect(() => {
    // 逻辑集群
    setPageLoad(true);
    getClusterPerApp()
      .then((res = []) => {
        setCluster(res);
      })
      .finally(() => {
        setPageLoad(false);
      });
  }, [department]);

  React.useEffect(() => {
    getIndexDataType().then((res = {}) => {
      const dataTypeList = Object.keys(res).map((key) => {
        return {
          title: res[key],
          label: res[key],
          value: Number(key),
        };
      });
      setDataTypeList(dataTypeList);
    });
  }, []);

  const handleSubmit = (result) => {
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setqueryFormObject({ ...result, ...sorter, page: 1, size: paginationProps.pageSize, showMetadata: queryFormObject.showMetadata });
  };

  const reloadData = () => {
    setloading(true);
    const Params: IAllIndexList = {
      page: queryFormObject.page,
      size: queryFormObject.size,
      id: queryFormObject.id !== undefined ? +queryFormObject.id : undefined,
      name: queryFormObject.name,
      dataType: queryFormObject.dataType,
      desc: queryFormObject.desc,
      resourceId: queryFormObject.cluster,
      sortTerm: queryFormObject.sortTerm,
      orderByDesc: queryFormObject.orderByDesc,
      showMetadata: queryFormObject.showMetadata,
    };
    getAllIndexList(Params)
      .then((res) => {
        if (res) {
          setTableData(res?.bizData);
          const { pageNo = 1, pageSize = 10 } = res.pagination;
          setPaginationProps({
            ...paginationProps,
            total: res?.pagination?.total,
            current: pageNo,
            pageSize: pageSize,
          });
        }
      })
      .finally(() => {
        setloading(false);
      });
  };

  const getOpBtns = () => {
    return (
      <>
        {hasOpPermission(TempletPermissions.PAGE, TempletPermissions.APPLY) ? (
          <Button type="primary" onClick={() => props.setDrawerId("createTemplate", { dataTypeList }, reloadData)}>
            新建模板
          </Button>
        ) : (
          ""
        )}
      </>
    );
  };

  const pushHistory = (url) => {
    props.history.push(url);
  };

  const handleChange = (pagination, filters, sorter) => {
    // 条件过滤请求在这里处理
    const sorterObject: { [key: string]: any } = {};
    // 排序
    if (sorter.field && sorter.order) {
      switch (sorter.field) {
        case "blockRead":
          sorterObject.sortTerm = "block_read";
          break;
        case "blockWrite":
          sorterObject.sortTerm = "block_write";
          break;
        default:
          sorterObject.sortTerm = sorter.field;
          break;
      }
      sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
    }
    let filterObj = {} as { showMetadata: boolean };
    filterObj["showMetadata"] = filters.name?.length ? true : !superApp;
    setSorter(sorterObject);
    setqueryFormObject((state) => {
      if (!sorter.order) {
        delete state.sortTerm;
        delete state.orderByDesc;
      }
      return {
        ...state,
        ...sorterObject,
        ...filterObj,
        page: pagination.current,
        size: pagination.pageSize,
      };
    });
  };

  const clientHeight = document.querySelector("#d1-layout-main")?.clientHeight;
  const renderNode = () => {
    return (
      <>
        <div className="table-layout-style">
          <ProTable
            showQueryForm={true}
            queryFormProps={{
              // layout: "inline",
              // colMode: "style",
              totalNumber: paginationProps?.total,
              defaultCollapse: true,
              columns: getQueryFormConfig(cluster, dataTypeList),
              onReset: handleSubmit,
              onSearch: handleSubmit,
              isResetClearAll: true,
            }}
            tableProps={{
              tableId: "template_manage_list",
              isCustomPg: false,
              loading,
              rowKey: "id",
              dataSource: tableData,
              columns: getLogicIndexColumns(dataTypeList, props.setDrawerId, reloadData, pushHistory, props.history),
              reloadData,
              getJsxElement: getOpBtns,
              customRenderSearch: () => <RenderTitle {...LOGIC_INDEX_TITLE} />,
              paginationProps,
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

  return (
    <Spin className="temp-spin-name" spinning={pageLoad}>
      {cluster.length
        ? !pageLoad && renderNode()
        : !pageLoad && (
            <div>
              <RenderEmpty {...props} href={`/cluster/logic?needApplyCluster=${uuid()}`} />
            </div>
          )}
    </Spin>
  );
});
