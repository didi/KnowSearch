import { getServiceList, IAllIndexList, switchMasterSlave, getPhyClusterPerApp } from "api/cluster-index-api";
import { getProjectListByUserId } from "api/app-api";
import { checkClusterBindGateway } from "api/gateway-manage";
import { getCookie } from "lib/utils";
import { RenderTitle } from "component/render-title";
import React, { useState } from "react";
import { getServiceColumns, getQueryFormConfig, getBatchBtnService } from "./config";
import { LOGIC_INDEX_TITLE } from "./constants";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { AppState } from "store/type";
import { ProTable, Menu, Dropdown, Button, Modal, Tooltip } from "knowdesign";
import { InfoCircleFilled } from "@ant-design/icons";
import { isOpenUp, CodeType, GATEWAY_UNABLE_TIP, CONFIRM_BUTTON_TEXT } from "constants/common";
import { initPaginationProps } from "constants/table";
import store from "store";
import "./index.less";

const mapStateToProps = (state) => ({
  app: state.app,
});

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (drawerId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(drawerId, params, cb)),
});

export const IndexTplService = connect(
  mapStateToProps,
  mapDispatchToProps
)((props: { setModalId: Function; setDrawerId: Function; app: AppState; history: any }) => {
  const department: string = localStorage.getItem("current-project");
  const [loading, setLoading] = useState(false);
  const [queryFormObject, setQueryFormObject]: any = useState({
    page: 1,
    size: 10,
    showMetadata: false, // 默认不展示元数据集群模板
  });
  const [tableData, setTableData] = useState([]);
  const [paginationProps, setPaginationProps] = useState(initPaginationProps());
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);
  const [selectedRows, setSelectedRows] = useState([]);
  const [cluster, setCluster] = useState([]);
  const [projectList, setProjectList] = useState([]);
  const [sorter, setSorter] = useState({});
  const [bindGateway, setBindGateway] = useState(false);

  React.useEffect(() => {
    reloadData();
    _checkClusterBindGateway();
  }, [department, queryFormObject]);

  React.useEffect(() => {
    getPhyClusterPerApp().then((res = []) => {
      setCluster(res);
    });
  }, [department]);

  React.useEffect(() => {
    const projectList = props.app.projectList;
    if (!projectList?.length) {
      const userId = getCookie("userId");
      getProjectListByUserId(+userId).then((res = []) => {
        setProjectList(res);
        store.dispatch(actions.setProjectList(res));
      });
    } else {
      setProjectList(projectList);
    }
  }, []);

  const _checkClusterBindGateway = async () => {
    let res = await checkClusterBindGateway();
    setBindGateway(res);
  };

  const handleSubmit = (result) => {
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setQueryFormObject({ ...result, ...sorter, page: 1, size: paginationProps.pageSize, showMetadata: queryFormObject.showMetadata });
  };

  const reloadData = () => {
    setLoading(true);
    const params: IAllIndexList = {
      page: queryFormObject.page,
      size: queryFormObject.size,
      id: queryFormObject.id !== undefined ? +queryFormObject.id : undefined,
      name: queryFormObject.name,
      health: queryFormObject.health,
      projectId: queryFormObject.projectId,
      cluster: queryFormObject.cluster,
      sortTerm: queryFormObject.sortTerm,
      orderByDesc: queryFormObject.orderByDesc,
      hasDCDR: queryFormObject.hasDCDR,
      openSrv: queryFormObject.openSrv,
      // TODO 元数据筛选字段尚未定义，联调时更新
      showMetadata: queryFormObject.showMetadata,
    };
    getServiceList(params)
      .then((res) => {
        if (res) {
          setTableData(res?.bizData);
          clearSelect();
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
        setLoading(false);
      });
  };

  const clearSelect = () => {
    setSelectedRowKeys([]);
    setSelectedRows([]);
  };

  const switchDCDR = (type) => {
    const unavailableSrv = [];
    const opend = [];
    const closed = [];
    selectedRows.forEach((item) => {
      if (item.unavailableSrv?.find((srv) => srv.srvCode === CodeType.DCDR)) {
        unavailableSrv.push({
          name: item.name,
          id: item.id,
        });
      } else if (item.hasDCDR) {
        opend.push({
          name: item.name,
          id: item.id,
        });
      } else {
        closed.push({
          name: item.name,
          id: item.id,
        });
      }
    });
    if (unavailableSrv.length === selectedRows.length) {
      Modal.confirm({
        ...CONFIRM_BUTTON_TEXT,
        icon: <InfoCircleFilled className="confirm-icon" />,
        title: "提示",
        content: `索引模板${
          unavailableSrv.length > 1 ? `${unavailableSrv[0].name}等` : unavailableSrv[0].name
        }不具备DCDR主从切换条件，无法切换。`,
      });
    } else if (closed.length) {
      Modal.confirm({
        ...CONFIRM_BUTTON_TEXT,
        icon: <InfoCircleFilled className="confirm-icon" />,
        title: "提示",
        content: (
          <>
            <div>{`索引模板${closed.length > 1 ? `${closed[0].name}等` : closed[0].name}未建立DCDR链路，无法进行主从切换。`}</div>
            <div>请点击“创建链路”进行链路的创建。</div>
          </>
        ),
      });
    } else {
      if (!opend.length) return;
      if (type === 2) {
        Modal.confirm({
          ...CONFIRM_BUTTON_TEXT,
          icon: <InfoCircleFilled className="confirm-icon" />,
          title: "提示",
          content: `强制切换后，无法确保数据一致，待切换前主集群正常后会自动清理链路，且不会反向构建新链路，底层数据不会删除。`,
          onOk: () => {
            confirmSwitch(type, opend);
          },
        });
      } else {
        props.setModalId("dcdrTimeout", { onSubmit: confirmSwitch, type, opend });
      }
    }
  };

  const confirmSwitch = (type, ids, timeout?: number) => {
    switchMasterSlave({
      type,
      templateIds: ids.map((item) => item.id),
      timeout,
    }).then((res) => {
      Modal.success({
        ...CONFIRM_BUTTON_TEXT,
        onOk: () => reloadData(),
        title: "提交成功！",
        content: (
          <>
            <div className="order-success">
              <span>{res.title}已提交！可至“任务中心”查看任务详情</span>
              <br />
              <span>
                任务标题（ID）：
                <Button
                  type="link"
                  style={{ padding: 0 }}
                  onClick={() => {
                    Modal.destroyAll();
                    props.history.push(`/work-order/task/dcdrdetail?taskid=${res.id}&title=${res.title}`);
                  }}
                >
                  {res.title}（{res.id}）
                </Button>
              </span>
            </div>
          </>
        ),
      });
    });
  };

  const getOpBtns = React.useCallback(() => {
    const menu = (
      <Menu>
        {getBatchBtnService(props.setModalId, reloadData, selectedRows, switchDCDR, props.app.gatewayStatus).map((item) => (
          <Menu.Item
            disabled={(selectedRows && selectedRows.length === 0) || isOpenUp || item.gatewayStatus === false}
            key={item.label}
            onClick={() => item.onClick()}
          >
            {selectedRows && selectedRows.length === 0 ? (
              <Tooltip title={isOpenUp ? "该功能仅面向商业版客户开放" : "需选定索引模板"}>{item.label}</Tooltip>
            ) : (
              // ) : item.gatewayStatus === false ? (
              //   <Tooltip title={GATEWAY_UNABLE_TIP}>{item.label}</Tooltip>
              item.label
            )}
          </Menu.Item>
        ))}
      </Menu>
    );
    return (
      <>
        <Dropdown overlay={menu} placement="bottomCenter">
          <Tooltip title={isOpenUp ? "该功能仅面向商业版客户开放" : ""}>
            {selectedRows && selectedRows.length <= 0 ? (
              ""
            ) : (
              <Button type="primary" disabled={isOpenUp}>
                批量操作
              </Button>
            )}
          </Tooltip>
        </Dropdown>
      </>
    );
  }, [selectedRows]);

  const pushHistory = (url) => {
    props.history.push(url);
  };

  const handleChange = (pagination, filters, sorter) => {
    // 条件过滤请求在这里处理
    const sorterObject: { [key: string]: any } = {};
    // 排序
    if (sorter.field && sorter.order) {
      switch (sorter.columnKey) {
        case "checkPointDiff":
          sorterObject.sortTerm = "check_point_diff";
          sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
          break;
        default:
          sorterObject.sortTerm = sorter.field;
          sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
          break;
      }
    }
    let filterArr = filters.hasDCDR;
    let filterObj = {} as { openSrv: number; hasDCDR: boolean; showMetadata: boolean };
    (filterArr || []).forEach((item) => {
      item ? (filterObj["hasDCDR"] = true) : (filterObj["openSrv"] = 10);
    });
    filterObj["showMetadata"] = filters.name?.length ? true : false;
    setSorter({ ...sorterObject, ...filterObj });
    setQueryFormObject((state) => {
      if (!sorter.order) {
        delete state.sortTerm;
        delete state.orderByDesc;
      }
      if (!filterObj.hasDCDR) {
        delete state.hasDCDR;
      }
      if (!filterObj.openSrv) {
        delete state.openSrv;
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
  return (
    <div className="table-layout-style">
      <ProTable
        showQueryForm={true}
        queryFormProps={{
          // layout: "inline",
          // colMode: "style",
          totalNumber: paginationProps?.total,
          defaultCollapse: true,
          columns: getQueryFormConfig(cluster, projectList),
          onReset: handleSubmit,
          onSearch: handleSubmit,
          isResetClearAll: true,
        }}
        tableProps={{
          tableId: "template_service_list",
          isCustomPg: false,
          loading,
          rowKey: "id",
          dataSource: tableData,
          columns: getServiceColumns(props.setModalId, props.setDrawerId, reloadData, pushHistory, props.app.gatewayStatus, bindGateway),
          reloadData,
          // isDividerHide: selectedRows?.length > 0,
          getJsxElement: getOpBtns,
          customRenderSearch: () => <RenderTitle {...LOGIC_INDEX_TITLE} />,
          paginationProps,
          attrs: {
            onChange: handleChange,
            rowSelection: {
              selectedRowKeys,
              onChange: (selectedRowKeys, selectedRows) => {
                setSelectedRowKeys(selectedRowKeys);
                setSelectedRows(selectedRows);
              },
            },
            scroll: {
              x: "max-content",
            },
          },
        }}
      />
    </div>
  );
});
