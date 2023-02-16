import React from "react";
import { renderOperationBtns, NavRouterLink } from "container/custom-component";
import { InfoCircleOutlined } from "@ant-design/icons";
import { message, Tag, Modal, Tooltip, DatePicker } from "antd";
import { IOpLogicCluster, IOpPhysicsCluster } from "typesPath/cluster/cluster-types";
import { ClusterStatus, clusterTypeMap, logicClusterType, StatusMap } from "constants/status-map";
import { cellStyle } from "constants/table";
import { delPackage } from "api/cluster-api";
import { ITableBtn } from "component/dantd/dtable";
import { IVersions } from "typesPath/cluster/physics-type";
import store from "store";
import { IColumnsType } from "component/dantd/query-form/QueryForm";
import { isOpenUp, LEVEL_MAP } from "constants/common";
import { renderDiskRate } from "../custom-component";
import { PhyClusterPermissions, MyClusterPermissions, ClusterVersionPermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import { renderAttributes } from "container/custom-component";
import { regNonnegativeInteger } from "constants/reg";
import DRangeTime from "../../d1-packages/d-range-time";
import { transTimeFormat } from "lib/utils";
import { XNotification } from "component/x-notification";

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

  const options = arr.map((item) => ({
    title: item,
    value: item,
  }));

  return options;
};

export const getPhyClusterQueryXForm = (
  data: IOpPhysicsCluster[],
  packageHostList: any,
  phyClusterList = [],
  logiClusterList = [],
  onPhyClusterChange
) => {
  const formMap = [
    {
      dataIndex: "id",
      title: "集群ID:",
      type: "input",
      placeholder: "请输入集群ID",
      componentProps: {
        autoComplete: "off",
      },
      rules: [
        {
          required: false,
          validator: (rule: any, value: string) => {
            if (value && !new RegExp(regNonnegativeInteger).test(value)) {
              return Promise.reject(new Error("请输入正确格式"));
            }
            if (value?.length > 16) {
              return Promise.reject(new Error("请输入正确ID，0-16位字符"));
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      dataIndex: "cluster",
      title: "物理集群名称:",
      type: "select",
      placeholder: "请选择",
      options: phyClusterList,
      componentProps: {
        onChange: onPhyClusterChange,
      },
    },
    {
      dataIndex: "logicClusterName",
      title: "逻辑集群名称:",
      type: "select",
      placeholder: "请选择",
      options: logiClusterList,
    },
    {
      dataIndex: "health",
      title: "集群状态:",
      type: "select",
      options: ClusterStatus,
      placeholder: "请选择",
    },
    {
      dataIndex: "esVersion",
      title: "集群版本:",
      type: "select",
      options: packageHostList,
      placeholder: "请选择",
    },
    {
      dataIndex: "desc",
      title: "描述:",
      type: "input",
      placeholder: "请输入集群描述",
      componentProps: {
        autoComplete: "off",
      },
    },
  ] as IColumnsType[];
  return formMap;
};

export const getLogicClusterQueryXForm = (data: IOpLogicCluster[], logiClusterList = []) => {
  const formMap = [
    {
      dataIndex: "id",
      title: "集群ID",
      type: "input",
      placeholder: "请输入集群ID",
      rules: [
        {
          required: false,
          validator: (rule: any, value: string) => {
            if (value && !new RegExp(regNonnegativeInteger).test(value)) {
              return Promise.reject(new Error("请输入正确格式"));
            }
            if (value?.length > 16) {
              return Promise.reject(new Error("请输入正确ID，0-16位字符"));
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      dataIndex: "name",
      title: "集群名称",
      type: "select",
      options: logiClusterList,
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
      dataIndex: "type",
      title: "集群类型",
      type: "select",
      options: logicClusterType,
      placeholder: "请选择",
    },
    {
      dataIndex: "memo",
      title: "描述",
      type: "input",
      placeholder: "请输入集群描述",
    },
  ] as IColumnsType[];
  return formMap;
};

export const getServiceBtnList = (record: IOpPhysicsCluster, setModalId: any, setDrawerId: any, reloadDataFn): ITableBtn[] => {
  let btn = [
    {
      invisible: !hasOpPermission(PhyClusterPermissions.PAGE, PhyClusterPermissions.SHORTCUTS),
      label: "快捷命令",
      type: "primary",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        setDrawerId("physicsClusterTask", record, reloadDataFn);
      },
    },
    {
      invisible: !hasOpPermission(PhyClusterPermissions.PAGE, PhyClusterPermissions.REGION_SET),
      label: "Region划分",
      type: "primary",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        setModalId("regionDivide", record, reloadDataFn);
      },
    },
    {
      invisible: !hasOpPermission(PhyClusterPermissions.PAGE, PhyClusterPermissions.REGION_MANAGE),
      label: "Region管理",
      type: "primary",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        setModalId("regionAdmin", record, reloadDataFn);
      },
    },
    {
      invisible: !hasOpPermission(PhyClusterPermissions.PAGE, PhyClusterPermissions.EXPAND_SHRINK) || !record?.supportZeus,
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
      invisible: !hasOpPermission(PhyClusterPermissions.PAGE, PhyClusterPermissions.UPGRADE) || !record?.supportZeus,
      label: "升级",
      type: "primary",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        setDrawerId("upgradeCluster", record, reloadDataFn);
      },
    },
    {
      invisible: !hasOpPermission(PhyClusterPermissions.PAGE, PhyClusterPermissions.RESTART) || !record?.supportZeus,
      label: "重启",
      type: "primary",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        setDrawerId("restartCluster", record, reloadDataFn);
      },
    },
    {
      invisible: !hasOpPermission(PhyClusterPermissions.PAGE, PhyClusterPermissions.CONFIG_UPDATE) || !record?.supportZeus,
      label: "配置变更",
      type: "primary",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        setDrawerId("newConfigModal", record, reloadDataFn);
      },
    },
  ];
  return btn;
};

export const getPhysicsBtnList = (record: IOpPhysicsCluster, setModalId: any, setDrawerId: any, reloadDataFn): ITableBtn[] => {
  let btn = [
    {
      invisible: !hasOpPermission(PhyClusterPermissions.PAGE, PhyClusterPermissions.EDIT),
      label: "编辑",
      type: "primary",
      clickFunc: () => {
        let params = record;
        if (params?.password) {
          let password = params?.password.split(":");
          params.usename = password[0];
          params.password = password[1];
        }

        setModalId("editPhyCluster", params, reloadDataFn);
      },
    },
    //这个配置不要删除，下个版本还要继续开发
    // {
    //   invisible: !hasOpPermission(PhyClusterPermissions.PAGE, PhyClusterPermissions.BIND_GATEWAY),
    //   label: "绑定Gateway",
    //   type: "primary",
    //   clickFunc: () => {
    //     setModalId("bindGateway", record, reloadDataFn);
    //   },
    // },
    {
      invisible: !hasOpPermission(PhyClusterPermissions.PAGE, PhyClusterPermissions.OFFLINE),
      label: "下线",
      type: "primary",
      clickFunc: () => {
        if (record?.logicClusterAndRegionList) {
          XNotification({ type: "error", message: `下线失败，请解绑逻辑集群后重试` });
          return;
        }
        setModalId("deleteCluster", record, reloadDataFn);
      },
    },
  ];
  return btn;
};

export const getPhysicsColumns = (setModalId: any, setDrawerId: any, reloadDataFn: any, props: any) => {
  const columns = [
    {
      title: "集群ID",
      dataIndex: "id",
      key: "id",
      width: 80,
    },
    {
      title: "物理集群名称",
      dataIndex: "cluster",
      key: "cluster",
      width: 180,
      lineClampOne: true,
      render: (text: string, record: IOpPhysicsCluster) => {
        let permission = hasOpPermission(PhyClusterPermissions.PAGE, PhyClusterPermissions.LIST_DETAIL);
        if (!permission) return text;
        return (
          <NavRouterLink
            needToolTip={true}
            element={text}
            href={`/cluster/physics/detail?physicsCluster=${record.cluster}&physicsClusterId=${record.id}&type=${record.type}#info`}
          />
        );
      },
    },
    {
      title: "逻辑集群名称",
      dataIndex: "logicClusterAndRegionList",
      key: "logicClusterAndRegionList",
      width: 240,
      render: (list) => {
        if (!list || !list[0]?.v1) return "-";
        let limit = 2;
        if (list[0]?.v1?.name?.length > 20 || list[0]?.v1?.name?.length + list[1]?.v1?.name?.length > 25) {
          limit = 1;
        }
        return renderAttributes({ data: list?.map((item: any) => item && item?.v1?.name) || [], limit, placement: "bottomLeft" });
      },
    },
    {
      title: "集群状态",
      dataIndex: "health",
      key: "health",
      width: 100,
      render: (health: number) => {
        return (
          <div>
            <Tag className={`tag ${StatusMap[health]}`} color={StatusMap[health]} style={{ width: 64, textAlign: "center" }}>
              {StatusMap[health]}
            </Tag>
          </div>
        );
      },
    },
    {
      title: "集群类型",
      dataIndex: "resourceType",
      key: "resourceType",
      width: 100,
      render: (type: number) => {
        return <>{clusterTypeMap[type] || "-"}</>;
      },
    },
    {
      title: "集群版本",
      dataIndex: "esVersion",
      key: "esVersion",
      width: 100,
      render: (text: string) => text || "-",
    },
    {
      title: "磁盘使用率",
      dataIndex: "diskInfo",
      key: "diskInfo",
      width: 160,
      sorter: true,
      render: (_, diskInfo) => renderDiskRate(diskInfo),
    },
    {
      title: "活跃分片数",
      dataIndex: "activeShardNum",
      key: "activeShardNum",
      width: 120,
      sorter: true,
    },
    {
      title: "描述",
      dataIndex: "desc",
      key: "desc",
      width: 130,
      lineClampOne: true,
      onCell: () => ({
        style: { ...cellStyle, maxWidth: 100 },
      }),
      render: (text: string) => {
        return (
          <Tooltip placement="bottomLeft" title={text}>
            {text ? text : "-"}
          </Tooltip>
        );
      },
    },
    {
      title: "集群服务",
      dataIndex: "service",
      key: "service",
      width: 220,
      render: (id: number, records: IOpPhysicsCluster) => {
        const record = { ...records, ...props };
        const btns = getServiceBtnList(record, setModalId, setDrawerId, reloadDataFn);
        return renderOperationBtns(btns, record, 3);
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      width: 120,
      filterTitle: true, //开启表格自定义列
      fixed: "right",
      render: (id: number, records: IOpPhysicsCluster) => {
        const record = { ...records, ...props };
        const btns = getPhysicsBtnList(record, setModalId, setDrawerId, reloadDataFn);
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return columns;
};

const getLogicBtnList = (record: IOpLogicCluster | any, fn: any, reloadDataFn: any, indexTemFun: any): ITableBtn[] => {
  let btn = [
    {
      invisible: !hasOpPermission(MyClusterPermissions.PAGE, MyClusterPermissions.EDIT),
      label: "编辑",
      clickFunc: () => {
        fn("editCluster", record, reloadDataFn);
      },
    },
    {
      invisible: !hasOpPermission(MyClusterPermissions.PAGE, MyClusterPermissions.EXPAND_SHRINK),
      label: "扩缩容",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        fn("expandShrink", record, reloadDataFn);
      },
    },
    {
      invisible: !hasOpPermission(MyClusterPermissions.PAGE, MyClusterPermissions.OFFLINE),
      label: "下线",
      clickFunc: async () => {
        let countState = await indexTemFun(record);
        if (countState?.templateLogicAggregates || countState?.catIndexResults) {
          XNotification({
            type: "error",
            message: `逻辑集群${record.name}下线失败`,
            description: `该集群下还有${countState?.templateLogicAggregates || 0}项模板资源、${countState?.catIndexResults || 0}
          项索引资源，如需下线集群，请前往模板管理、索引管理下线掉对应的模板及索引！`,
            duration: 1000,
          });
        } else {
          fn("offlineCluster", record, reloadDataFn);
        }
      },
    },
  ];
  return btn as ITableBtn[];
};

export const getLogicColumns = (tableData: IOpLogicCluster[], fn: any, reloadDataFn: any, props: any, indexTemFun: any) => {
  const columns = [
    {
      title: "集群ID",
      dataIndex: "id",
      key: "id",
      width: 80,
    },
    {
      title: "集群名称",
      dataIndex: "name",
      key: "name",
      width: 150,
      render: (text: string, record: IOpLogicCluster) => {
        let permission = hasOpPermission(MyClusterPermissions.PAGE, MyClusterPermissions.LIST_DETAIL);
        if (!permission) return text;
        return (
          <NavRouterLink
            needToolTip={true}
            element={text}
            href={`/cluster/logic/detail?cluster=${text}&clusterId=${record.id}&type=${record.type}#info`}
          />
        );
      },
    },
    {
      title: "集群状态",
      dataIndex: "health",
      key: "status",
      width: 100,
      render: (health) => {
        return (
          <div>
            <Tag className={`tag ${StatusMap[health]}`} style={{ width: 64, textAlign: "center" }} color={StatusMap[health]}>
              {StatusMap[health]}
            </Tag>
          </div>
        );
      },
    },
    {
      title: "集群类型",
      dataIndex: "type",
      key: "type",
      width: 120,
      sorter: true,
      render: (type: number) => {
        return <>{clusterTypeMap[type] || "-"}</>;
      },
    },
    {
      title: "集群版本",
      dataIndex: "esClusterVersion",
      key: "esClusterVersion",
      width: 120,
      sorter: true,
      render: (text: string) => text || "-",
    },
    {
      title: "业务等级",
      dataIndex: "level",
      key: "level",
      width: 120,
      sorter: true,
      render: (text) => {
        return LEVEL_MAP[Number(text) - 1]?.label || "-";
      },
    },
    {
      title: "磁盘使用率",
      dataIndex: "diskInfo",
      key: "diskInfo",
      width: 150,
      sorter: true,
      render: (_, diskInfo) => renderDiskRate(diskInfo),
    },
    {
      title: "数据节点数",
      dataIndex: "dataNodeNum",
      key: "dataNodeNum",
      width: 130,
      sorter: true,
      render: (podNumber: string) => {
        return <>{podNumber != null ? podNumber : "-"}</>;
      },
    },
    {
      title: "描述",
      dataIndex: "memo",
      key: "memo",
      width: 150,
      ellipsis: true,
      onCell: () => ({
        style: { ...cellStyle, maxWidth: 150 },
      }),
      render: (text: string) => {
        return (
          <Tooltip placement="bottomLeft" title={text}>
            {text ? text : "-"}
          </Tooltip>
        );
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      width: 150,
      fixed: "right",
      filterTitle: true,
      render: (id: number, records: IOpLogicCluster) => {
        const record = { ...records, ...props };
        const btns = getLogicBtnList(record, fn, reloadDataFn, indexTemFun);
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return columns;
};

export const getVersionsColumns = (fn, reloadDataFn) => {
  const cols = [
    {
      title: "ID",
      dataIndex: "id",
      key: "ID",
      width: 80,
      sorter: (a: IVersions, b: IVersions) => a.id - b.id,
    },
    {
      title: "版本名称",
      dataIndex: "esVersion",
      key: "esVersion",
      width: 120,
    },
    {
      title: "版本标识",
      dataIndex: "packageType",
      key: "packageType",
      width: 120,
      render: (text: number) => {
        let str = "-";
        if (text == 1) {
          str = "滴滴内部版本";
        }
        if (text == 2) {
          str = "社区开源版本";
        }
        return str;
      },
    },
    {
      title: "url",
      dataIndex: "url",
      key: "url",
      width: 180,
      lineClampTwo: true,
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
      title: "描述",
      dataIndex: "desc",
      key: "desc",
      width: 150,
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
      width: 80,
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
      width: 150,
      sorter: (a: IVersions, b: IVersions) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime(),
      render: (t: number) => transTimeFormat(t),
    },
  ];

  return cols;
};

export const getEditionQueryXForm = (data, handleTimeChange) => {
  //export const getEditionQueryXForm = (data, handleTimeChange, resetAllValue: Function) => {
  const customTimeOptions = [
    {
      label: "最近 1 小时",
      value: 60 * 60 * 1000,
    },
    {
      label: "最近 1 天",
      value: 24 * 60 * 60 * 1000,
    },
    {
      label: "最近 7 天",
      value: 7 * 24 * 60 * 60 * 1000,
    },
  ];
  const formMap = [
    {
      dataIndex: "esVersion",
      title: "版本名称:",
      type: "select",
      options: getOptions(data, "esVersion"),
      placeholder: "请选择",
    },
    {
      dataIndex: "createTime",
      title: "创建时间:",
      type: "custom",
      //component: <RangePicker showTime={{ format: "HH:mm" }} format="YYYY-MM-DD HH:mm" />,
      component: (
        <DRangeTime
          timeChange={handleTimeChange}
          popoverClassName="dashborad-popover"
          //resetAllValue={resetAllValue}
          customTimeOptions={customTimeOptions}
        />
      ),
    },
  ] as IColumnsType[];
  return formMap;
};
