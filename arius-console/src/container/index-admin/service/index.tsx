import React, { useCallback, useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { withRouter } from "react-router-dom";
import { Menu, Dropdown, Tooltip, Button, Tag } from "antd";
import { DTable } from "component/dantd/dtable";
import { RenderTitle } from "component/render-title";
import { getColumns, getQueryFormConfig, queryFormText, getBatchBtnService } from "./config";
import QueryForm from "component/dantd/query-form";
import * as actions from "actions";
import "./index.less";
import { getIndexAdminData } from "api/index-admin";
import { getPhyClusterPerApp } from "api/cluster-index-api";
import { initPaginationProps } from "constants/table";
import { isSuperApp } from "lib/utils";
import { ProTable } from "knowdesign";
import { hasOpPermission } from "lib/permission";
import { IndexServicePermissions } from "constants/permission";

export const IndexService = withRouter((props: { history: any }) => {
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
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);
  const [selectedRows, setSelectedRows] = useState([]);
  const [cluster, setCluster] = useState([]);
  const [queryData, setQueryData]: any = useState({
    page: 1,
    size: 10,
    showMetadata: false, // 默认不展示元数据集群索引
  });
  const [paginationProps, setPaginationProps] = useState(initPaginationProps());
  const [realTotal, setRealTotal] = useState(0);

  const getAsyncData = async () => {
    setIsLoading(true);
    const Params: any = {
      page: queryData.page,
      size: queryData.size,
      index: queryData.index,
      cluster: queryData.cluster,
      sortTerm: queryData.sortTerm,
      orderByDesc: queryData.orderByDesc,
      // TODO 元数据筛选字段尚未定义，联调时更新
      showMetadata: queryData.showMetadata,
    };
    getIndexAdminData(Params)
      .then((res) => {
        if (res) {
          res?.bizData.forEach((item) => {
            item.cluster = superApp ? item.cluster : item.clusterLogic;
            item.rowKey = `${item.cluster}${item.index}`;
          });
          setData(res?.bizData);
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

  useEffect(() => {
    getPhyClusterPerApp().then((res = []) => {
      setCluster(res);
    });
  }, [department]);

  useEffect(() => {
    getAsyncData();
  }, [queryData, department]);

  const renderTitleContent = () => {
    return {
      title: "索引服务",
      content: null,
    };
  };

  const getOpBtns = useCallback(() => {
    if ((selectedRows && selectedRows.length < 1) || !hasOpPermission(IndexServicePermissions.PAGE, IndexServicePermissions.BAT)) {
      return null;
    }
    const menu = (
      <Menu>
        {getBatchBtnService(setModalId, setDrawerId, selectedRows, getAsyncData).map((item) => (
          <Menu.Item disabled={selectedRows && selectedRows.length === 0} key={item.label} onClick={() => item.onClick()}>
            {selectedRows && selectedRows.length === 0 ? <Tooltip title="需选定索引后批量执行">{item.label}</Tooltip> : item.label}
          </Menu.Item>
        ))}
      </Menu>
    );
    return (
      <>
        {selectedRows && selectedRows.length > 0 ? (
          <Dropdown overlay={menu} placement="bottomCenter">
            <Button type="primary">批量执行</Button>
          </Dropdown>
        ) : (
          ""
        )}
      </>
    );
  }, [selectedRows]);

  const clientHeight = document.querySelector("#d1-layout-main")?.clientHeight;

  return (
    <div className="table-layout-style">
      <ProTable
        showQueryForm={true}
        queryFormProps={{
          defaultCollapse: true,
          columns: getQueryFormConfig(cluster),
          onReset: handleSubmit,
          onSearch: handleSubmit,
          isResetClearAll: true,
        }}
        tableProps={{
          tableId: "index_admin_service_list",
          isCustomPg: false,
          loading: isLoading,
          rowKey: "rowKey",
          dataSource: data,
          columns: getColumns(setModalId, setDrawerId, getAsyncData),
          reloadData: () => getAsyncData(),
          isDividerHide: selectedRows?.length > 0,
          getJsxElement: getOpBtns,
          customRenderSearch: () => (
            <div className="zeus-url">
              <RenderTitle {...renderTitleContent()} />{" "}
              <Tag
                className="zeus-url-tag"
                onClick={() =>
                  (window.open("about:blank").location.href = "https://www.elastic.co/guide/en/elasticsearch/reference/7.10/indices.html")
                }
              >
                指导文档
              </Tag>
            </div>
          ),
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
          ...props,
        }}
      />
    </div>
  );
});
