import React from "react";
import { renderOperationBtns, NavRouterLink } from "container/custom-component";
import { InfoCircleOutlined } from "@ant-design/icons";
import {
  message,
  Tag,
  Modal,
  Progress,
  Tooltip,
  notification,
  DatePicker,
} from "antd";
import {
  IClusterStatus,
  IOpLogicCluster,
  IOpPhysicsCluster,
} from "@types/cluster/cluster-types";
import { nounClusterType, nounClusterStatus } from "container/tooltip";
import {
  ClusterAuth,
  ClusterAuthMaps,
  ClusterStatus,
  clusterTypeMap,
  INDEX_AUTH_TYPE_MAP,
  logicClusterType,
  PHY_CLUSTER_TYPE,
  VERSION_MAINFEST_TYPE,
  StatusMap,
} from "constants/status-map";
import { cellStyle } from "constants/table";
import { delPackage } from "api/cluster-api";
import { ITableBtn } from "component/dantd/dtable";
import { IVersions } from "@types/cluster/physics-type";
import moment from "moment";
import { timeFormat } from "constants/time";
import { submitWorkOrder } from "api/common-api";
import { IWorkOrder } from "@types/params-types";

import store from "store";
import { updateBinCluster } from "api/cluster-index-api";
import { IColumnsType } from "component/dantd/query-form/QueryForm";
import { bytesUnitFormatter } from "../../lib/utils";
import { isOpenUp } from "constants/common";

const loginInfo = {
  userName: store.getState().user?.getName,
  app: store.getState().app,
};
const { RangePicker } = DatePicker;
const { confirm } = Modal;

export const getOptions = (data, type: string | number) => {
  if (!data) return [];
  const arr = Array.from(
    new Set(
      data.map((item) => {
        return item?.[type];
      })
    )
  );

  if (type === "appId") {
    const arr = [];
    data.forEach((element) => {
      let flat = false;
      if (arr.length) {
        arr.forEach((item) => {
          if (item.value === element.appId) {
            flat = true;
          }
        });
      }
      if (!flat) {
        arr.push({
          value: element.appId,
          title: element.appName,
        });
      }
    });
    return arr;
  }

  const options = arr.map((item) => ({
    title: item,
    value: item,
  }));

  return options;
};

export const getPhyClusterQueryXForm = (data: IOpPhysicsCluster[]) => {
  const formMap = [
    {
      dataIndex: "currentAppAuth",
      title: "项目权限",
      type: "select",
      options: ClusterAuth,
      placeholder: "请选择",
    },
    {
      dataIndex: "health",
      title: "集群状态",
      type: "select",
      options: ClusterStatus,
      placeholder: "请选择",
    },
    {
      dataIndex: "cluster",
      title: "集群名称",
      type: "input",
      placeholder: "请输入",
    },
    {
      dataIndex: "esVersion",
      title: "版本",
      type: "select",
      options: getOptions(data, "esVersion"),
      placeholder: "请选择",
    },
  ] as IColumnsType[];
  return formMap;
};

export const getLogicClusterQueryXForm = (data: IOpLogicCluster[]) => {
  const formMap = [
    {
      dataIndex: "authType",
      title: "项目权限",
      type: "select",
      options: ClusterAuth,
      placeholder: "请选择",
    },
    {
      dataIndex: "health",
      title: "集群状态",
      type: "select",
      options: ClusterStatus,
      placeholder: "请选择",
    },
    {
      dataIndex: "name",
      title: "集群名称",
      type: "input",
      placeholder: "请输入",
    },
    {
      dataIndex: "type",
      title: "集群类型",
      type: "select",
      options: logicClusterType,
      placeholder: "请选择",
    },
    // {
    //   dataIndex: "esClusterVersions",
    //   title: "集群版本",
    //   type: "select",
    //   options: getOptions(data, "esClusterVersions"),
    //   placeholder: "请选择",
    // },
    {
      dataIndex: "appId",
      title: "所属项目",
      type: "select",
      options: getOptions(data, "appId"),
      placeholder: "请选择",
    },
  ] as IColumnsType[];
  return formMap;
};


export const getPhysicsBtnList = (
  record: IOpPhysicsCluster,
  setModalId: any,
  setDrawerId: any,
  reloadDataFn
): ITableBtn[] => {
  let btn = [
    {
      label: "升级",
      type: "primary",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        setModalId("upgradeCluster", record, reloadDataFn);
      },
    },
    {
      label: "扩缩容",
      type: "primary",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        if (record.type === 3) {
          setModalId("dockerExpandShrinkCluster", record, reloadDataFn);
        } else if (record.type === 4) {
          setModalId("expandShrinkCluster", record, reloadDataFn);
        }
      },
    },
    {
      label: "重启",
      type: "primary",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        setModalId("restartCluster", record, reloadDataFn);
      },
    },
    // {
    //   label: "任务",
    //   type: "primary",
    //   isOpenUp: isOpenUp,
    //   clickFunc: () => {
    //     setDrawerId("physicsClusterTaskDrawer", record, reloadDataFn);
    //   },
    // },
    {
      label: "编辑",
      type: "primary",
      clickFunc: () => {
        setModalId("editPhyCluster", record, reloadDataFn);
      },
    },
    {
      label: "删除",
      needConfirm: true,
      confirmText: "删除",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        setModalId("deleteCluster", record, reloadDataFn);
      },
    },
  ];
  if (record.currentAppAuth !== 1 && record.currentAppAuth !== 0) {
    btn = [];
  }
  return btn;
};

export const getPhysicsColumns = (
  setModalId: any,
  setDrawerId: any,
  reloadDataFn: any
) => {
  const columns = [
    {
      title: "集群ID",
      dataIndex: "id",
      key: "id",
    },
    {
      title: "集群名称",
      dataIndex: "cluster",
      key: "cluster",
      sorter: (a, b) => a.cluster.length - b.cluster.length,
      render: (text: string, record: IOpPhysicsCluster) => {
        return (
          <NavRouterLink
            needToolTip={true}
            element={text}
            href={`/cluster/physics/detail?physicsCluster=${record.cluster}&physicsClusterId=${record.id}&type=${record.type}&auth=${record.currentAppAuth}#info`}
          />
        );
      },
    },
    {
      title: "集群状态",
      dataIndex: "health",
      key: "health",
      sorter: (a, b) => a.health - b.health,
      render: (health: string) => {
        return (
          <div>
            <Tag color={StatusMap[health]}>{StatusMap[health]}</Tag>
          </div>
        );
      },
    },
    {
      title: "集群类型",
      dataIndex: "type",
      key: "type",
      render: (type: number) => {
        return <div>{VERSION_MAINFEST_TYPE[type]}</div>;
      },
    },
    {
      title: "集群版本",
      dataIndex: "esVersion",
      key: "esVersion",
      sorter: (a, b) => a.esVersion.length - b.esVersion.length,
      render: (text: string) => text || "-",
    },
    {
      title: "磁盘使用率",
      dataIndex: "diskInfo",
      key: "diskInfo",
      sorter: (a, b) =>
        a.diskInfo.diskUsagePercent - b.diskInfo.diskUsagePercent,
      render: (diskInfo) => {
        const num = Number((diskInfo.diskUsagePercent * 100).toFixed(2));
        let strokeColor;
        let yellow = "#eaaa50";
        let red = "#df6d62";
        if (num > 90) {
          strokeColor = red;
        } else if (num > 70) {
          strokeColor = yellow;
        }

        return (
          <div style={{ position: "relative" }} className="process-box">
            <Progress
              percent={num}
              size="small"
              strokeColor={strokeColor}
              width={150}
            />
            <div style={{ position: "absolute", fontSize: "1em" }}>
              {bytesUnitFormatter(diskInfo.diskUsage || 0)}/
              {bytesUnitFormatter(diskInfo.diskTotal || 0)}
            </div>
          </div>
        );
      },
    },
    {
      title: "权限",
      dataIndex: "currentAppAuth",
      key: "currentAppAuth",
      render: (currentAppAuth: number) => {
        return <>{ClusterAuthMaps[currentAppAuth] || "无权限"}</>;
      },
    },
    {
      title: "描述",
      dataIndex: "desc",
      key: "desc",
      width: "8%",
      onCell: () => ({
        style: { ...cellStyle, maxWidth: 100 },
      }),
      render: (text: string) => {
        return (
          <Tooltip placement="bottomLeft" title={text}>
            {text ? text : '-'}
          </Tooltip>
        );
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      render: (id: number, record: IOpPhysicsCluster) => {
        const btns = getPhysicsBtnList(
          record,
          setModalId,
          setDrawerId,
          reloadDataFn
        );
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return columns;
};

export const delLogicCluster = (data: IOpLogicCluster, reloadDataFn, setModalId?: Function, url?,) => {
  setModalId("deleteLogicCluster", {...data, url: url}, reloadDataFn);
  // confirm({
  //   title: `是否确定删除集群${data.name}`,
  //   icon: <InfoCircleOutlined />,
  //   content: `集群删除后，集群所有相关数据也将被删除，请谨慎操作！`,
  //   width: 500,
  //   okText: "确定",
  //   cancelText: "取消",
  //   onOk() {
  //     const params: IWorkOrder = {
  //       contentObj: {
  //         id: data.id,
  //         name: data.name,
  //         type: data.type,
  //         responsible: data.responsible,
  //       },
  //       submitorAppid: loginInfo.app.appInfo()?.id,
  //       submitor: loginInfo.userName("domainAccount"),
  //       description: "",
  //       type: "logicClusterDelete",
  //     };
  //     return submitWorkOrder(params, () => {
  //       message.success("提交工单成功");
  //       if (url) {
  //         url();
  //       } else {
  //         reloadDataFn();
  //       }
  //     });
  //   },
  // });
};

const getLogicBtnList = (
  record: IOpLogicCluster | any,
  fn: any,
  reloadDataFn: any
): ITableBtn[] => {
  let btn = [
    {
      label: "编辑",
      clickFunc: () => {
        fn("editCluster", record, reloadDataFn);
      },
    },
    {
      label: "扩缩容",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        fn("expandShrink", record, reloadDataFn);
      },
    },
    {
      label: "转让",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        fn("transferCluster", record, reloadDataFn);
      },
    },
    {
      label: "删除",
      needConfirm: true,
      confirmText: "删除",
      clickFunc: (record: IOpLogicCluster) => {
        delLogicCluster(record, reloadDataFn, fn);
      },
    },
  ];
  if (ClusterAuthMaps[record?.authType] === "无权限") {
    btn = [
      {
        label: "申请权限",
        clickFunc: () => {
          fn("applyAauthority", record, reloadDataFn);
        },
      },
    ];
  }

  if (ClusterAuthMaps[record?.authType] === "访问") {
    btn = [
      {
        label: "取消权限",
        clickFunc: () => {
          confirm({
            title: `提示`,
            icon: <InfoCircleOutlined />,
            content: `是否确定取消权限?`,
            width: 500,
            okText: "确定",
            cancelText: "取消",
            onOk() {
              updateBinCluster(record.authId).then(() => {
                message.success("取消权限成功");
                reloadDataFn();
              });
            },
          });
        },
      },
    ];
  }

  return btn as ITableBtn[];
};

export const getLogicColumns = (
  tableData: IOpLogicCluster[],
  fn: any,
  reloadDataFn: any
) => {
  const columns = [
    {
      title: "集群名称",
      dataIndex: "name",
      key: "name",
      sorter: (a, b) => a.name.length - b.name.length,
      render: (text: string, record: IOpLogicCluster) => {
        return (
          <NavRouterLink
            needToolTip={true}
            element={text}
            href={`/cluster/logic/detail?clusterId=${record.id}&type=${record.type}#info`}
          />
        );
      },
    },
    {
      title: () => {
        return (
          <>
            {/* {nounClusterStatus} */}
            集群状态
          </>
        );
      },
      dataIndex: "health",
      key: "status",
      width: "12%",
      sorter: (a, b) => a.health - b.health,
      render: (health) => {
        return (
          <div>
            <Tag color={StatusMap[health]}>{StatusMap[health]}</Tag>
          </div>
        );
      },
    },
    {
      title: () => {
        return (
          <>
            {/* {nounClusterType}  */}
            集群类型
          </>
        );
      },
      dataIndex: "type",
      key: "type",
      width: "12%",
      render: (type: number) => {
        return <>{clusterTypeMap[type] || "-"}</>;
      },
    },
    // {
    //   title: "集群版本",
    //   dataIndex: "esClusterVersions",
    //   key: "esClusterVersions",
    //   width: "8%",
    //   onCell: () => ({
    //     style: { ...cellStyle, maxWidth: 100 },
    //   }),
    //   render: (t: string) => {
    //     return (
    //       <>
    //         <Tooltip placement="bottomLeft" title={t}>
    //           {t}
    //         </Tooltip>
    //       </>
    //     );
    //   },
    // },
    {
      title: "是否关联物理集群",
      dataIndex: "phyClusterAssociated",
      key: "phyClusterAssociated",
      render: (isBin: boolean) => {
        return <>{isBin ? "是" : "否"}</>;
      },
    },
    {
      title: "数据节点数",
      dataIndex: "dataNodesNumber",
      key: "dataNodesNumber",
      render: (podNumber: string) => {
        return <>{podNumber != null ? podNumber : "-"}</>;
      },
    },
    {
      title: "所属项目",
      dataIndex: "appName",
      key: "appName",
      render: (appId: string) => {
        return <>{appId ? appId : "-"}</>;
      },
    },
    {
      title: "描述",
      dataIndex: "memo",
      key: "memo",
      width: "8%",
      onCell: () => ({
        style: { ...cellStyle, maxWidth: 100 },
      }),
      render: (text: string) => {
        return (
          <Tooltip placement="bottomLeft" title={text}>
            {text ? text : '-'}
          </Tooltip>
        );
      },
    },
    {
      title: "权限",
      dataIndex: "authType",
      key: "authType",
      render: (podNumber: number) => {
        return <>{podNumber ? ClusterAuthMaps[podNumber] : "无权限"}</>;
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      width: "15%",
      render: (id: number, record: IOpLogicCluster) => {
        const btns = getLogicBtnList(record, fn, reloadDataFn);
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return columns;
};

export const getVersionsColumns = (fn, reloadDataFn) => {
  const getOperationList = (record, fn, reloadDataFn) => {
    return [
      {
        label: "编辑",
        isOpenUp: isOpenUp,
        clickFunc: () => {
          fn("addPackageModal", record, reloadDataFn);
        },
      },
      {
        label: "删除",
        isOpenUp: isOpenUp,
        clickFunc: () => {
          confirm({
            title: "确定删除？",
            icon: <InfoCircleOutlined />,
            content: "",
            width: 500,
            okText: "确认",
            cancelText: "取消",
            onOk() {
              delPackage(record.id).then((res) => {
                notification.success({ message: "删除成功" });
                reloadDataFn();
              });
            },
          });
        },
      },
    ];
  };

  const cols = [
    {
      title: "ID",
      dataIndex: "id",
      key: "ID",
      width: "8%",
      sorter: (a: IVersions, b: IVersions) => a.id - b.id,
    },
    {
      title: "版本名",
      dataIndex: "esVersion",
      key: "esVersion",
    },
    {
      title: "url",
      dataIndex: "url",
      key: "url",
      onCell: () => ({
        style: {
          maxWidth: 200,
          ...cellStyle,
        },
      }),
      render: (url: string) => {
        return (
          <Tooltip placement="bottomLeft" title={url}>
            {url}
          </Tooltip>
        );
      },
    },
    {
      title: "类型",
      dataIndex: "manifest",
      key: "manifest",
      width: "15%",
      render: (manifest: number) => {
        return <>{VERSION_MAINFEST_TYPE[manifest] || ""}</>;
      },
    },
    {
      title: "描述",
      dataIndex: "desc",
      key: "desc",
      onCell: () => ({
        style: {
          maxWidth: 200,
          ...cellStyle,
        },
      }),
      render: (desc: string) => {
        return (
          <>
            <Tooltip placement="bottomLeft" title={desc}>
              {desc || "_"}
            </Tooltip>
          </>
        );
      },
    },
    {
      title: "创建人",
      dataIndex: "creator",
      key: "creator",
      width: "15%",
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
      width: "15%",
      sorter: (a: IVersions, b: IVersions) =>
        new Date(b.createTime).getTime() - new Date(a.createTime).getTime(),
      render: (t: number) => moment(t).format(timeFormat),
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      render: (text: string, record: IVersions) => {
        const btns = getOperationList(record, fn, reloadDataFn);
        return renderOperationBtns(btns, record);
      },
    },
  ];

  return cols;
};

export const getEditionQueryXForm = (data) => {
  const formMap = [
    {
      dataIndex: "manifest",
      title: "版本类型",
      type: "select",
      options: PHY_CLUSTER_TYPE,
      placeholder: "请选择",
    },
    {
      dataIndex: "esVersion",
      title: "版本名",
      type: "select",
      options: getOptions(data, "esVersion"),
      placeholder: "请选择",
    },
    {
      dataIndex: "createTime",
      title: "创建时间",
      type: "custom",
      component: (
        <RangePicker showTime={{ format: "HH:mm" }} format="YYYY-MM-DD HH:mm" />
      ),
    },
  ] as IColumnsType[];
  return formMap;
};
