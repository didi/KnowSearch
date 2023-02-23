import React, { useState } from "react";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getPhysicsColumns, getPhyClusterQueryXForm, getBatchBtn } from "./config";
import {
  getOpPhysicsClusterList,
  getNodeSpecification,
  IClusterList,
  getPackageList,
  getZeusUrl,
  getSuperPhyClusterList,
  getSuperLogiClusterList,
} from "api/cluster-api";
import { ITableBtn } from "component/dantd/dtable";
import { INodeListObjet } from "container/modal/physics-cluster/apply-cluster";
import { Button, Tag, Menu, Dropdown, Tooltip } from "knowdesign";
import { RenderTitle } from "component/render-title";
import { isOpenUp } from "constants/common";
import { PhyClusterPermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import { XNotification } from "component/x-notification";
import { ProTable } from "knowdesign";
import "./index.less";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params, cb)),
});

const PhysicsClusterBox = (props) => {
  const department: string = localStorage.getItem("current-project");
  const [loading, setLoading] = useState(false);
  const [queryFormObject, setQueryFormObject]: any = useState({
    current: 1,
    size: 10,
  });
  const [data, setData] = useState([]);
  const [total, setTotal] = useState(0);
  const [url, setUrl] = useState("");
  const [packageHostList, setPackageHostList] = useState([]);
  const [packageDockerList, setPackageDockerList] = useState([]);
  const [phyClusterList, setPhyClusterList] = useState([]);
  const [logiClusterList, setLogiClusterList] = useState([]);
  const [form, setForm] = useState<any>();
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);
  const [selectedRows, setSelectedRows] = useState([]);

  React.useEffect(() => {
    _getPackageList();
    getPhyClusterList();
    getLogiClusterList();
  }, []);

  React.useEffect(() => {
    reloadData();
  }, [department, queryFormObject]);

  const reloadData = () => {
    const app = JSON.parse(localStorage.getItem("current-project"));
    if (!app?.name) {
      return;
    }
    setLoading(true);
    getPhyClusterList();
    const params: IClusterList = {
      page: queryFormObject.current,
      size: queryFormObject.size,
      authType: queryFormObject.currentAppAuth,
      health: queryFormObject.health,
      cluster: queryFormObject.cluster,
      esVersion: queryFormObject.esVersion,
      sortTerm: queryFormObject.sortTerm,
      orderByDesc: queryFormObject.orderByDesc,
      id: queryFormObject.id !== undefined ? +queryFormObject.id : undefined,
      desc: queryFormObject.desc,
      logicClusterName: queryFormObject.logicClusterName,
    };
    getOpPhysicsClusterList(params)
      .then((res) => {
        if (res) {
          setData(
            res?.bizData?.map((item) => ({
              ...item,
              diskInfo: {
                diskTotal: item.diskTotal,
                diskUsage: item.diskUsage,
                diskUsagePercent: item.diskUsagePercent,
              },
            }))
          );
          setTotal(res?.pagination?.total);
        }
      })
      .finally(() => {
        setLoading(false);
      });
    getZeusUrl().then((res) => {
      setUrl(res);
    });
  };

  const _getPackageList = async () => {
    let data = await getPackageList();
    const packageList = data.map((ele, index) => {
      return {
        ...ele,
        key: ele.id,
        value: ele.esVersion,
        title: ele.esVersion,
      };
    });
    let dockerList = packageList.filter((ele) => ele.manifest === 3);
    let hostList = packageList.filter((ele) => ele.manifest === 4);
    setPackageHostList(hostList);
    setPackageDockerList(dockerList);
  };

  const getPhyClusterList = () => {
    getSuperPhyClusterList().then((res = []) => {
      const list = res.map((item) => ({ title: item, value: item }));
      setPhyClusterList(list);
    });
  };

  const getLogiClusterList = (phyClusterName?: string) => {
    getSuperLogiClusterList(phyClusterName).then((res = []) => {
      const list = res.map((item) => ({ title: item, value: item }));
      setLogiClusterList(list);
    });
  };

  const onPhyClusterChange = (val) => {
    form.setFieldsValue({ logicClusterName: undefined });
    getLogiClusterList(val);
  };

  const renderTitleContent = () => {
    return {
      title: "物理集群",
      content: null,
    };
  };

  const handleReset = () => {
    // 重置重新请求逻辑集群接口
    getLogiClusterList();
    setQueryFormObject({ size: queryFormObject.size, current: 1 });
  };

  const handleSubmit = (result) => {
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setQueryFormObject({ ...result, size: queryFormObject.size, current: 1 });
  };

  const getModalData = () => {
    const nodeList = {} as INodeListObjet;
    let machineList = [] as { value: string }[];

    getNodeSpecification()
      .then((data) => {
        machineList = data.map((item) => {
          return {
            value: item,
            label: item,
          };
        });
        props.setModalId("applyPhyCluster", { loading: false, nodeList, machineList, history: props.history }, reloadData);
      })
      .catch((err) => {
        XNotification({ type: "error", message: "网络错误！" });
        props.setModalId("");
      });
  };

  const getOpBtns = (): ITableBtn[] => {
    return [
      hasOpPermission(PhyClusterPermissions.PAGE, PhyClusterPermissions.ACCESS) && {
        label: "接入集群",
        type: "default",
        clickFunc: () => props.setModalId("accessCluster", {}, reloadData),
      },
      {
        type: "primary",
        label: "新建集群",
        isOpenUp: isOpenUp,
        clickFunc: () => getModalData(),
      },
      hasOpPermission(PhyClusterPermissions.PAGE, PhyClusterPermissions.FAST_INDEX) && {
        className: "ant-btn-primary",
        label: "数据迁移",
        clickFunc: () => props.setDrawerId("fastIndex", { history: props.history }, reloadData),
      },
    ].filter(Boolean);
  };

  const getBatchBtns = React.useCallback(() => {
    const menu = (
      <Menu>
        {getBatchBtn(props.setModalId, reloadData, selectedRows).map((item) => (
          <Menu.Item disabled={(selectedRows && selectedRows.length === 0) || isOpenUp} key={item.label} onClick={() => item.onClick()}>
            {selectedRows && selectedRows.length === 0 ? (
              <Tooltip title={isOpenUp ? "该功能仅面向商业版客户开放" : "需选定集群"}>{item.label}</Tooltip>
            ) : (
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

  const handleChange = (pagination, filters, sorter) => {
    // 条件过滤请求在这里处理
    const sorterObject: { [key: string]: any } = {};
    if (sorter.columnKey && sorter.order) {
      switch (sorter.columnKey) {
        case "diskInfo":
          sorterObject.sortTerm = "disk_usage_percent";
          sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
          break;
        case "activeShardNum":
          sorterObject.sortTerm = "active_shard_num";
          sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
          break;
        default:
          break;
      }
    }
    setQueryFormObject((state) => {
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

  const clientHeight = document.querySelector("#d1-layout-main")?.clientHeight;
  return (
    <div className="table-layout-style">
      <ProTable
        showQueryForm={true}
        queryFormProps={{
          // layout: "inline", //没有label的查询条件
          // colMode: "style", //col默认设计样式
          totalNumber: total, //传入总条数
          defaultCollapse: true,
          columns: getPhyClusterQueryXForm(data, packageHostList, phyClusterList, logiClusterList, onPhyClusterChange),
          // onChange={() => null}
          onReset: handleReset,
          onSearch: handleSubmit,
          initialValues: queryFormObject,
          isResetClearAll: true,
          getFormInstance: (form) => setForm(form),
        }}
        tableProps={{
          tableId: "physics_cluster_list", //开启表格自定义列
          key: JSON.stringify({
            authType: queryFormObject.authType,
            health: queryFormObject.health,
            cluster: queryFormObject.cluster,
            esVersion: queryFormObject.esVersion,
          }),
          isCustomPg: false,
          loading,
          rowKey: "id",
          dataSource: data,
          columns: getPhysicsColumns(props.setModalId, props.setDrawerId, reloadData, props),
          reloadData,
          getOpBtns: getOpBtns,
          getJsxElement: getBatchBtns,
          customRenderSearch: () => (
            <div className="zeus-url">
              <RenderTitle {...renderTitleContent()} />{" "}
              {url && (
                <Tag className="zeus-url-tag" onClick={() => (window.open("about:blank").location.href = url)}>
                  Zeus管控
                </Tag>
              )}
            </div>
          ),
          paginationProps: {
            position: "bottomRight",
            showQuickJumper: true,
            total: total,
            showSizeChanger: true,
            pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
            showTotal: (total) => `共 ${total} 条`,
            current: queryFormObject.current,
          },
          attrs: {
            onChange: handleChange,
            scroll: { x: "max-content" },
            rowSelection: {
              selectedRowKeys,
              onChange: (selectedRowKeys, selectedRows) => {
                setSelectedRowKeys(selectedRowKeys);
                setSelectedRows(selectedRows);
              },
            },
          },
          ...props,
        }}
      />
    </div>
  );
};
export const PhysicsCluster = connect(null, mapDispatchToProps)(PhysicsClusterBox);
