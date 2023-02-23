import React, { memo, useCallback, useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { withRouter } from "react-router-dom";
import { Tooltip, Button, ProTable } from "knowdesign";
import { RenderTitle } from "component/render-title";
import { getColumns, getQueryFormConfig } from "./config";
import * as actions from "actions";
import { getGatewayList } from "api/gateway-manage";
import { initPaginationProps } from "constants/table";
import { GatewayPermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import { ITableBtn } from "component/dantd/dtable";

export const GatewayCluster = withRouter((props: { history: any }) => {
  const department: string = localStorage.getItem("current-project");
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
  const [queryData, setQueryData]: any = useState({
    page: 1,
    size: 10,
  });

  const getAsyncData = async () => {
    setIsLoading(true);
    const params: any = {
      page: queryData.page,
      size: queryData.size,
      clusterName: queryData.clusterName,
      health: queryData.health,
      sortTerm: queryData.sortTerm,
      orderByDesc: queryData.orderByDesc,
    };
    getGatewayList(params)
      .then((res) => {
        if (res) {
          setData(res?.bizData);
          const { pageNo = 1, pageSize = 10, total = 0 } = res?.pagination;
          setPaginationProps({
            ...paginationProps,
            total: total,
            current: pageNo,
            pageSize: pageSize,
          });
        }
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  const handleSubmit = (result) => {
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setQueryData({ ...result, page: 1, size: paginationProps.pageSize });
  };

  const pageChange = (pagination, filters, sorter) => {
    // 条件过滤请求在这里处理
    const sorterObject: { [key: string]: any } = {};
    // 排序
    if (sorter.field && sorter.order) {
      sorterObject.sortTerm = sorter.field;
      sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
    }
    setQueryData((state) => {
      if (!sorter.order) {
        delete state.sortTerm;
        delete state.orderByDesc;
      }
      return {
        ...state,
        ...sorterObject,
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
    getAsyncData();
  }, [queryData, department]);

  const renderTitleContent = () => {
    return {
      title: "Gateway管理",
      content: null,
    };
  };

  const getOpBtns = (): ITableBtn[] => {
    return [
      hasOpPermission(GatewayPermissions.PAGE, GatewayPermissions.ACCESS) && {
        label: "接入Gateway",
        type: "primary",
        clickFunc: () => setDrawerId("joinGateway", {}, modalReloadData),
      },
      hasOpPermission(GatewayPermissions.PAGE, GatewayPermissions.ADD) && {
        label: "新建Gateway",
        type: "primary",
        clickFunc: () => setDrawerId("addGateway", { history: props.history }, modalReloadData),
      },
    ].filter(Boolean);
  };

  const renderNode = () => {
    return (
      <div className="table-layout-style">
        <ProTable
          showQueryForm={true}
          queryFormProps={{
            defaultCollapse: true,
            columns: getQueryFormConfig(),
            onReset: handleSubmit,
            onSearch: handleSubmit,
            isResetClearAll: true,
          }}
          tableProps={{
            isCustomPg: false,
            loading: isLoading,
            rowKey: "id",
            dataSource: data,
            columns: getColumns(setModalId, setDrawerId, modalReloadData, props),
            reloadData: () => modalReloadData(),
            getOpBtns,
            customRenderSearch: () => <RenderTitle {...renderTitleContent()} />,
            paginationProps,
            attrs: {
              onChange: pageChange,
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

  return <>{renderNode()}</>;
});
