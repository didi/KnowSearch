import React, { memo, useCallback, useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { withRouter } from "react-router-dom";
import { Tooltip, Button, Spin } from "antd";
import { DTable } from "component/dantd/dtable";
import { RenderTitle } from "component/render-title";
import { getColumns, getQueryFormConfig, queryFormText } from "./config";
import QueryForm from "component/dantd/query-form";
import * as actions from "actions";
import "./index.less";
import { getIndexAdminData } from "api/index-admin";
import { getClusterPerApp, getPhyClusterPerApp } from "api/cluster-index-api";
import { initPaginationProps } from "constants/table";
import { isSuperApp } from "lib/utils";
import { IndexPermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import { RenderEmpty } from "component/LogClusterEmpty";
import { ProTable } from "knowdesign";

export const IndexAdmin = withRouter((props: { history: any }) => {
  const department: string = localStorage.getItem("current-project");
  // 超级应用展示物理集群，其他应用展示逻辑集群
  const superApp = isSuperApp();
  const totalLimit = 10000;
  const dispatch = useDispatch();
  const setModalId = (modalId: string, params?: any, cb?: Function) => {
    dispatch(actions.setModalId(modalId, params, cb));
  };
  const setDrawerId = (drawerId: string, params?: any, cb?: Function) => {
    dispatch(actions.setDrawerId(drawerId, params, cb));
  };

  const [data, setData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [paginationProps, setPaginationProps] = useState(initPaginationProps());
  const [realTotal, setRealTotal] = useState(0);
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);
  const [selectedRows, setSelectedRows] = useState([]);
  const [cluster, setCluster] = useState([]);
  const [queryData, setQueryData]: any = useState({
    page: 1,
    size: 10,
    showMetadata: false,
  });
  const [pageLoad, setPageLoad] = useState(false);

  const getAsyncData = async () => {
    setIsLoading(true);
    const Params: any = {
      page: queryData.page,
      size: queryData.size,
      index: queryData.index,
      cluster: queryData.cluster,
      health: queryData.health,
      sortTerm: queryData.sortTerm,
      showMetadata: queryData.showMetadata,
      orderByDesc: queryData.orderByDesc,
    };
    getIndexAdminData(Params)
      .then((res) => {
        if (res) {
          let data = (res?.bizData || []).map((item) => {
            return {
              ...item,
              cluster: superApp ? item.cluster : item.clusterLogic,
              rowKey: item.key,
            };
          });
          setData(data);
          clearSelect();
          const { pageNo = 1, pageSize = 10, total = 0 } = res?.pagination;
          setPaginationProps({
            ...paginationProps,
            total: total > totalLimit ? totalLimit : total,
            current: pageNo,
            pageSize: pageSize,
            showTotal: (total) => `共 ${res?.pagination?.total} 条`,
          });
          setRealTotal(res?.pagination?.total);
        }
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  const clearSelect = () => {
    setSelectedRowKeys([]);
    setSelectedRows([]);
  };

  const getAsyncClusterName = async () => {
    setPageLoad(true);
    try {
      const clusterNameList = superApp ? await getPhyClusterPerApp() : await getClusterPerApp();
      setPageLoad(false);
      setCluster(superApp ? clusterNameList || [] : clusterNameList.map((item) => item.v2));
    } catch (error) {
      console.log(error, "error");
      setPageLoad(false);
    }
  };

  const handleSubmit = (result) => {
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setQueryData({ ...result, page: 1, size: paginationProps.pageSize, showMetadata: queryData.showMetadata });
  };

  const pageChange = (pagination, filters, sorter) => {
    // 条件过滤请求在这里处理
    const sorterObject: { [key: string]: any } = {};
    // 排序
    if (sorter.field && sorter.order) {
      sorterObject.sortTerm = sorter.field;
      sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
    }
    let filterObj = {} as { showMetadata: boolean };
    filterObj["showMetadata"] = filters.index?.length ? true : false;
    setQueryData((state) => {
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

  const modalReloadData = function (del?: boolean) {
    // 下线操作，
    if (del) {
      setQueryData({
        ...queryData,
        page: 1,
        size: paginationProps.pageSize,
      });
      return;
    }
    setQueryData((state) => ({ ...state }));
  };

  useEffect(() => {
    getAsyncClusterName();
  }, [department]);

  useEffect(() => {
    getAsyncData();
  }, [queryData, department]);

  const renderTitleContent = () => {
    return {
      title: "索引管理",
      content: null,
    };
  };

  const getOpBtns = useCallback(() => {
    return (
      <>
        <Button type="primary" onClick={() => setDrawerId("createIndex", {}, modalReloadData)}>
          新建索引
        </Button>

        {hasOpPermission(IndexPermissions.PAGE, IndexPermissions.BAT_DELETE) ? (
          selectedRows && selectedRows.length > 0 ? (
            <Tooltip title={selectedRows && selectedRows.length ? "" : "需要选中后批量下线"}>
              <Button
                onClick={() => {
                  setModalId(
                    "deleteIndex",
                    {
                      delList: selectedRows.map((item) => ({
                        cluster: item.cluster,
                        index: item.index,
                      })),
                      title: `确定批量下线所选索引吗?`,
                    },
                    modalReloadData
                  );
                }}
                style={{ marginRight: 0 }}
                type={"primary"}
                disabled={selectedRows && selectedRows.length ? false : true}
              >
                批量下线
              </Button>
            </Tooltip>
          ) : (
            ""
          )
        ) : (
          ""
        )}
      </>
    );
  }, [selectedRows]);

  const clientHeight = document.querySelector("#d1-layout-main")?.clientHeight;

  const renderNode = () => {
    return (
      <div className="table-layout-style">
        <ProTable
          showQueryForm={true}
          queryFormProps={{
            defaultCollapse: true,
            columns: getQueryFormConfig(cluster),
            // onChange={() => null}
            onReset: handleSubmit,
            onSearch: handleSubmit,
            // initialValues={{}}
            isResetClearAll: true,
          }}
          tableProps={{
            tableId: "index_admin_manage_list", //开启表格自定义列
            isCustomPg: false,
            loading: isLoading,
            rowKey: "rowKey",
            dataSource: data,
            columns: getColumns(setModalId, setDrawerId, modalReloadData, superApp),
            reloadData: () => modalReloadData(),
            getJsxElement: getOpBtns,
            customRenderSearch: () => <RenderTitle {...renderTitleContent()} />,
            paginationProps: {
              ...paginationProps,
              itemRender: (page, type: "page" | "prev" | "next", originalElement) => {
                const lastPage = totalLimit / paginationProps?.pageSize;
                if (type === "page") {
                  if (realTotal > totalLimit && page === lastPage) {
                    return <Tooltip title={`考虑到性能问题，只展示${totalLimit}条数据`}>{page}</Tooltip>;
                  } else {
                    return page;
                  }
                } else {
                  return originalElement;
                }
              },
            },
            attrs: {
              onChange: pageChange,
              rowSelection: {
                selectedRowKeys,
                onChange: (selectedRowKeys, selectedRows) => {
                  setSelectedRowKeys(selectedRowKeys);
                  setSelectedRows(selectedRows);
                },
              },
              border: true,
              scroll: {
                x: "max-content",
              },
            },
          }}
        />
      </div>
    );
  };

  return (
    <div>
      <Spin spinning={pageLoad} className="index-spin-name">
        {superApp
          ? renderNode()
          : cluster.length
          ? !pageLoad && renderNode()
          : !pageLoad && (
              <div>
                <RenderEmpty {...props} />
              </div>
            )}
      </Spin>
    </div>
  );
});
