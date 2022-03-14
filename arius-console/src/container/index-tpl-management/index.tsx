import { getAllIndexList, IAllIndexList, switchMasterSlave } from "api/cluster-index-api";
import { RenderTitle } from "component/render-title";
import React, { useState } from "react";
import { cherryList, getLogicIndexColumns, getQueryFormConfig } from "./config";
import { LOGIC_INDEX_TITLE } from "./constants";
import { connect } from "react-redux";
import { NavRouterLink, showSubmitOrderSuccessModal } from "container/custom-component";
import { Dispatch } from "redux";
import QueryForm from "component/dantd/query-form";
import * as actions from "actions";
import { queryFormText } from "constants/status-map";
import { DTable } from "component/dantd/dtable";
import { AppState } from "store/type";
import { Menu, Dropdown, Button, Modal, Tooltip } from "antd";
import FilterColumns from "component/filterColumns";
import { getCheckedList, setCheckedList } from "api/search-query";
import { isOpenUp } from "constants/common";
import { getClusterNameList } from "api/cluster-kanban";

const mapStateToProps = (state) => ({
  app: state.app,
});

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

export const IndexTplManagement = connect(
  mapStateToProps,
  mapDispatchToProps
)((props: { setModalId: Function; app: AppState; history: any }) => {
  const department: string = localStorage.getItem("current-project");
  const [loading, setloading] = useState(false);
  const [queryFromObject, setqueryFromObject]: any = useState({
    from: 0,
    size: 10,
  });
  const [tableData, setTableData] = useState([]);
  const [paginationProps, setPaginationProps] = useState({
    position: "bottomRight",
    showQuickJumper: true,
    total: 0,
    showSizeChanger: true,
    pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
    showTotal: (total) => `共 ${total} 条`,
  });
  const [selectedRows, setSelectedRows] = useState([]);
  const [columns, setColumns] = useState([]);
  const [cluster, setCluster] = useState([]);

  React.useEffect(() => {
    reloadData();
  }, [department, queryFromObject]);

  React.useEffect(() => {
    getClusterNameList().then((res) => {
      if (res && res?.length) {
        setCluster(res);
      }
    });
  }, []);

  // 开启定时器定时刷新
  React.useEffect(() => {
    const time = setInterval(() => {
      reloadData();
    }, 60 * 1000);
    return () => clearInterval(time);
  }, [department, queryFromObject]);

  const handleSubmit = (result) => {
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setqueryFromObject({ ...result, from: 0, size: 10 });
  };

  const reloadData = () => {
    setloading(true);
    const Params: IAllIndexList = {
      from: queryFromObject.from,
      size: queryFromObject.size,
      authType: queryFromObject.authType,
      dataType: queryFromObject.dataType,
      name: queryFromObject.name,
      sortTerm: queryFromObject.sortTerm,
      orderByDesc: queryFromObject.orderByDesc,
      hasDCDR: queryFromObject.hasDCDR,
    };
    if (queryFromObject.clusterPhies) {
      Params["clusterPhies"] = [queryFromObject.clusterPhies];
    }
    getAllIndexList(Params)
      .then((res) => {
        if (res) {
          setTableData(res?.bizData);
          setPaginationProps({
            position: "bottomRight",
            showQuickJumper: true,
            total: res?.pagination?.total,
            showSizeChanger: true,
            pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
            showTotal: (total) => `共 ${total} 条`,
          });
        }
      })
      .finally(() => {
        setloading(false);
      });
  };

  const onSwitchMasterSlave = (type) => {
    let flag = [];
    const keys = [];
    selectedRows.forEach((item) => {
      keys.push(item.id);
      if (!item.hasDCDR) {
        flag.push(item.name);
      }
    });
    if (flag.length && flag[0]) {
      Modal.confirm({
        title: "提示",
        content: (
          <>
            <div>
              {flag[0]}
              {flag.length > 1 ? "等" : ""}未建立DCDR链路，无法进行主从切换
            </div>
            <div>请点击模板名称进入模板详情-DCDR进行链路的创建</div>
          </>
        ),
      });
    } else {
      switchMasterSlave({
        type,
        templateIds: keys,
      }).then((res) => {
        Modal.success({
          onOk: () => reloadData(),
          okText: "确定",
          title: "提交成功！",
          content: (
            <>
              <div className="order-success">
                <span>{res.title}已提交！可至“工单任务” &gt; “我的申请”中查看工单详情</span>
                <br />
                <span>
                  工单标题（ID）：
                  <a href={`/work-order/task/dcdrdetail?taskid=${res.id}&title=${res.title}`}>
                    {res.title}（{res.id}）
                  </a>
                </span>
              </div>
            </>
          ),
        });
      });
    }
  };

  const getOpBtns = React.useCallback(() => {
    const menu = (
      <Menu>
        <Menu.Item disabled={(selectedRows && selectedRows.length === 0) || isOpenUp} onClick={() => onSwitchMasterSlave(1)}>
          {selectedRows && selectedRows.length === 0 ? (
            <Tooltip title={isOpenUp ? "该功能仅面向商业版客户开放" : "需选定索引模板才可以进行主从切换"}>平滑切换</Tooltip>
          ) : (
            "平滑切换"
          )}
        </Menu.Item>
        <Menu.Item disabled={(selectedRows && selectedRows.length === 0) || isOpenUp} onClick={() => onSwitchMasterSlave(2)}>
          {selectedRows && selectedRows.length === 0 ? (
            <Tooltip title={isOpenUp ? "该功能仅面向商业版客户开放" : "需选定索引模板才可以进行主从切换"}>强制切换</Tooltip>
          ) : (
            "强制切换"
          )}
        </Menu.Item>
      </Menu>
    );
    return (
      <>
        <FilterColumns
          columns={getLogicIndexColumns(tableData, props.setModalId, reloadData, null, props.app.appInfo()?.id, pushHistory)}
          setColumns={setColumns}
          checkArr={cherryList}
          getCheckFn={getCheckList}
          saveCheckFn={saveCheckFn}
        />
        <Button type="primary">
          <NavRouterLink element={"新建模板"} href={`/index/create`} />
        </Button>
        <Dropdown overlay={menu} placement="bottomCenter">
          <Tooltip title={isOpenUp ? "该功能仅面向商业版客户开放" : ""}>
            <Button type="primary" disabled={isOpenUp}>
              主从切换
            </Button>
          </Tooltip>
        </Dropdown>
      </>
    );
  }, [selectedRows]);

  const getCheckList = async () => {
    const checkListStr: string = await window.localStorage.getItem("templateAdmin");
    if (checkListStr) {
      try {
        return JSON.parse(checkListStr);
      } catch (err) {
        console.log(err);
      }
    } else {
      return [];
    }
  };

  const saveCheckFn = (list: string[]) => {
    window.localStorage.setItem("templateAdmin", JSON.stringify(list));
  };

  const pushHistory = (url) => {
    props.history.push(url);
  };

  const handleChange = (pagination, filters, sorter) => {
    // 条件过滤请求在这里处理
    const sorterObject: { [key: string]: any } = {};
    // 排序
    if (sorter.columnKey && sorter.order) {
      switch (sorter.columnKey) {
        case "checkPointDiff":
          sorterObject.sortTerm = "check_point_diff";
          sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
          break;
        case "level":
          sorterObject.sortTerm = "level";
          sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
          break;
        default:
          break;
      }
    }
    // 过滤
    if (filters.hasDCDR) {
      filters.hasDCDR?.length === 1 ? (sorterObject.hasDCDR = filters.hasDCDR[0]) : null;
    }
    setqueryFromObject((state) => {
      if (!sorter.order) {
        delete state.sortTerm;
        delete state.orderByDesc;
      }
      if ((filters && filters.hasDCDR?.length === 2) || !filters.hasDCDR) {
        delete state.hasDCDR;
      }
      return {
        ...state,
        ...sorterObject,
        from: (pagination.current - 1) * pagination.pageSize,
        size: pagination.pageSize,
      };
    });
  };

  const onSelectChange = (selectedRowKeys, selectedRows) => {
    console.log("selectedRowKeys changed: ", selectedRowKeys);
    setSelectedRows(selectedRows);
  };

  const rowSelection = {
    selectedRows,
    onChange: onSelectChange,
  };

  return (
    <>
      <div className="table-header">
        <RenderTitle {...LOGIC_INDEX_TITLE} />

        <QueryForm
          {...queryFormText}
          defaultCollapse
          columns={getQueryFormConfig(cluster)}
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
            dataSource={tableData}
            key={JSON.stringify({
              authType: queryFromObject.authType,
              dataType: queryFromObject.dataType,
              name: queryFromObject.name,
            })}
            attrs={{
              onChange: handleChange,
              rowSelection: rowSelection,
              scroll: { x: 1700 - (13 - columns.length) * 120 },
            }}
            paginationProps={paginationProps}
            columns={columns}
            reloadData={reloadData}
            renderInnerOperation={getOpBtns}
          />
        </div>
      </div>
    </>
  );
});
