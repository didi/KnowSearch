import { IMenuItem } from "typesPath/base-types";
import { IOpPhysicsClusterDetail, ITemplateSrvData } from "typesPath/cluster/cluster-types";
import React from "react";
import { DeleteOutlined, EditOutlined, QuestionCircleOutlined } from "@ant-design/icons";
import { Modal, Tooltip, message } from "antd";
import { cellStyle } from "constants/table";
import { ROLE_TYPE, ROLE_TYPE_NO, colorTheme, isOpenUp } from "constants/common";
import { ClusterInfo } from "./base-info";
import { NavRouterLink, renderOperationBtns } from "container/custom-component";
import { PlugnList } from "./plugn-list";
import { PHY_CLUSTER_TYPE, RESOURCE_TYPE_MAP, clusterTypeMap } from "constants/status-map";
import { IPlug } from "typesPath/plug-types";
import { userDelPlug } from "api/plug-api";
import store from "store";
import { IPhyConfig } from "typesPath/cluster/physics-type";
import { PhysicsConfigInfo } from "./physics-config-info.tsx";
import { IIndex, INodeDivide } from "typesPath/index-types";
import { NodeDivide } from "./node-divide";
import { opNodeStatusMap } from "./constants";
import { ITableBtn } from "component/dantd/dtable";
import { EditList } from "./edit-list";
import { Sense } from "./sense";
import { renderDiskRate } from "../../custom-component";
import { LogicList } from "./logic-list";
import { transTimeFormat } from "lib/utils";
import { SearchProfiler } from "./searchProfiler";

const appInfo = {
  app: store.getState().app,
  user: store.getState().user,
};

export enum TAB_LIST_KEY {
  info = "info",
  index = "index",
  indexTemplate = "indexTemplate",
  search = "search",
  monitor = "monitor",
  pluggin = "pluggin",
  node = "node",
  region = "region",
  diary = "diary",
  editList = "editList",
  sense = "sense",
  configList = "configList",
  logicList = "logicList",
  searchProfiler = "searchProfiler",
}

export const TAB_LIST = [
  {
    name: "集群概览",
    key: TAB_LIST_KEY.info,
    content: (params: { clusterInfo: IOpPhysicsClusterDetail; reloadData: Function; loading: boolean }) => (
      <ClusterInfo phyBaseInfo={params.clusterInfo} reloadData={params.reloadData} />
    ),
  },
  {
    name: "Sense查询",
    key: TAB_LIST_KEY.sense,
    content: () => <Sense />,
  },
  {
    name: "Search Profiler",
    key: TAB_LIST_KEY.searchProfiler,
    content: () => <SearchProfiler />,
  },
  {
    name: "节点列表",
    key: TAB_LIST_KEY.indexTemplate,
    content: (logicBaseInfo: IOpPhysicsClusterDetail) => <NodeDivide />,
  },
  {
    name: "逻辑集群列表",
    key: TAB_LIST_KEY.logicList,
    content: (params: { clusterInfo: IOpPhysicsClusterDetail; reloadData: Function; loading: boolean }) => (
      <LogicList logicList={params.clusterInfo?.logicClusterAndRegionList} reloadData={params.reloadData} loading={params.loading} />
    ),
  },
  // {
  //   name: "插件列表",
  //   key: TAB_LIST_KEY.pluggin,
  //   content: (logicBaseInfo: IOpPhysicsClusterDetail) => <PlugnList />,
  // },
  {
    name: "配置文件列表",
    key: TAB_LIST_KEY.configList,
    content: (logicBaseInfo: IOpPhysicsClusterDetail) => <PhysicsConfigInfo />,
  },
  {
    name: "动态配置",
    key: TAB_LIST_KEY.editList,
    content: () => <EditList />,
  },
];

const menuMap = new Map<string, IMenuItem>();
TAB_LIST.forEach((d) => {
  menuMap.set(d.key, d);
});

export const DETAIL_MENU_MAP = menuMap;

export const baseInfo = (setModalId?: Function, reloadData?: Function, phyBaseInfo?: any) => {
  const proxyAddressInfo = [
    {
      label: "代理地址",
      key: "proxyAddress",
      render: (value: string) => (
        <span>{value?.length > 56 ? <Tooltip title={value}>{value?.substring(0, 54) + "..."}</Tooltip> : value}</span>
      ),
    },
    {
      label: "分片数",
      key: "activeShardNum",
    },

    {
      label: "创建时间",
      key: "createTime",
      render: (time: number) => transTimeFormat(time),
    },
    {
      label: "备注",
      key: "desc",
      render: (value: string) => (
        <>
          <span>{value?.length > 56 ? <Tooltip title={value}>{value?.substring(0, 54) + "..."}</Tooltip> : value || "-"}</span>
        </>
      ),
    },
  ];

  const httpWriteAddressInfo = [
    {
      label: "读地址",
      key: "httpAddress",
      render: (value: string) => (
        <>
          <span>{value?.length > 56 ? <Tooltip title={value}>{value?.substring(0, 54) + "..."}</Tooltip> : value}</span>
        </>
      ),
    },
    {
      label: "分片数",
      key: "activeShardNum",
    },
    {
      label: "写地址",
      key: "httpWriteAddress",
      render: (value: string) => (
        <>
          <span>{value?.length > 56 ? <Tooltip title={value}>{value?.substring(0, 54) + "..."}</Tooltip> : value}</span>
        </>
      ),
    },
    {
      label: "创建时间",
      key: "createTime",
      render: (time: number) => transTimeFormat(time),
    },
    {
      label: "备注",
      key: "desc",
      render: (value: string) => (
        <>
          <span>{value?.length > 56 ? <Tooltip title={value}>{value?.substring(0, 54) + "..."}</Tooltip> : value || "-"}</span>
        </>
      ),
    },
  ];

  const baseConfigInfo = [
    {
      label: "ES版本",
      key: "esVersion",
    },
    {
      label: "Gateway地址",
      key: "gatewayAddress",
      render: () => {
        return (
          <div>
            <span>{phyBaseInfo.gatewayUrl || "-"}</span>
            {/* <EditOutlined onClick={() => setModalId("editGatewayUrl", phyBaseInfo, reloadData)} /> */}
          </div>
        );
      },
    },
  ];

  return phyBaseInfo?.proxyAddress
    ? [].concat(baseConfigInfo).concat(proxyAddressInfo)
    : [].concat(baseConfigInfo).concat(httpWriteAddressInfo);
};

const formatNodeInfo = (node: any, str: string) => {
  const esRoleClusterVOSItem = node?.find((item: any) => {
    return item?.role === str;
  });
  const machineSpecArr = esRoleClusterVOSItem?.machineSpec.split("-");
  return esRoleClusterVOSItem
    ? `${esRoleClusterVOSItem.podNumber} * CPU${machineSpecArr[0]}核-内存${machineSpecArr[1] || "(-)"}-磁盘${machineSpecArr[2] || "(-)"}`
    : "-";
};

export const configInfo: any = [
  [
    {
      label: "Masternode",
      key: "esRoleClusterVOS",
      render: (value: any) => (
        <>
          <span>{formatNodeInfo(value, "masternode")}</span>
        </>
      ),
    },
    {
      label: "Datanode",
      key: "esRoleClusterVOS",
      render: (value: any) => (
        <>
          <span>{formatNodeInfo(value, "datanode")}</span>
        </>
      ),
    },
  ],
  [
    {
      label: "Clientnode",
      key: "esRoleClusterVOS",
      render: (value: any) => (
        <>
          <span>{formatNodeInfo(value, "clientnode")}</span>
        </>
      ),
    },
  ],
];
interface ICardInfo {
  label: string;
  configList: () => [];
  btns?: JSX.Element[];
  col?: number;
}

export const cardInfo = [
  {
    label: "基本信息",
    configList: baseInfo,
  },
] as ICardInfo[];

export const getNodeColumns = () => {
  const cols = [
    {
      title: "节点名称",
      dataIndex: "hostname",
      key: "hostname",
      width: "15%",
    },
    {
      title: "节点ip",
      dataIndex: "ip",
      key: "ip",
      width: "20%",
      onCell: () => ({
        style: cellStyle,
      }),
      render: (text: string) => {
        return (
          <Tooltip placement="bottomLeft" title={text}>
            {text}
          </Tooltip>
        );
      },
    },
    {
      title: "节点规格",
      dataIndex: "nodeSpec",
      key: "nodeSpec",
      width: "20%",
    },
    {
      title: "节点角色",
      dataIndex: "role",
      key: "role",
      width: "15%",
      render: (role: number) => {
        return <>{ROLE_TYPE[role].label}</>;
      },
    },
  ];
  return cols;
};

export const arrToStr = (value, length = 10) => {
  let str = "";
  let tip = "";
  if (value && value?.length) {
    value.forEach((item, index) => {
      // 数组多个时最后一个没有逗号
      tip += item + (value.length - index == 1 ? "" : ",");
      // 识别到已经有胜率号后不在计算添加str
      if (str.includes("...")) {
        return;
      }
      // 超长会导致换行
      if (str.length > length || str.length + item.length > length) {
        str += "...";
      } else {
        str += item + (value.length - index == 1 ? "" : ",");
      }
    });
  }
  return (
    <>
      <Tooltip title={tip}>
        <span>{str || "-"}</span>
      </Tooltip>
    </>
  );
};

export const PHYSICE_DESC_LIST = [
  {
    label: "集群类型",
    key: "resourceType",
    render: (value: number) => clusterTypeMap[value] || "-",
  },
  {
    label: "IaaS平台类型",
    key: "platformType",
    render: (value: string) => value || "-",
  },
  {
    label: "数据中心",
    key: "dataCenter",
  },
];

export const getLogicNodeColumns = () => {
  const columns = [
    {
      title: "节点名称",
      dataIndex: "hostname",
      key: "hostname",
    },
    {
      title: "节点IP",
      dataIndex: "ip",
      key: "ip",
    },
    {
      title: "节点规格",
      dataIndex: "nodeSpec",
      key: "nodeSpec",
    },
    {
      title: "节点角色",
      dataIndex: "role",
      key: "role",
      render: (t: number) => ROLE_TYPE[t].label,
    },
    {
      title: "所属region",
      dataIndex: "regionId",
      key: "regionId",
    },
    {
      title: "所属物理集群",
      dataIndex: "cluster",
      key: "cluster",
    },
  ];
  return columns;
};

export const getLogicListColumns = () => {
  const columns = [
    {
      title: "逻辑集群ID",
      dataIndex: "logicClusterId",
      key: "logicClusterId",
      width: 200,
    },
    {
      title: "逻辑集群名称",
      dataIndex: "logicName",
      key: "logicName",
      width: 200,
    },
    {
      title: "所属应用",
      dataIndex: "projectNameList",
      key: "projectNameList",
      width: 200,
      ellipsis: true,
      render: (val) => {
        let text = val?.join("，");
        return <Tooltip title={text}>{text ? text : "-"}</Tooltip>;
      },
    },
    {
      title: "关联Region",
      dataIndex: "region",
      key: "region",
      width: 200,
    },
  ];
  return columns;
};

export const indexExplain = [
  {
    label: "预创建",
    content: "对于分区创建的索引，支持预创建，减轻集群负担，提高稳定性",
  },
  {
    label: "过期删除",
    content: "支持索引根据保存周期自动清理，避免磁盘过满",
  },
  {
    label: "Pipeline",
    content: "提供索引分区规则（索引模板到具体的物理索引的映射）和写入限流能力",
  },
  {
    label: "Mapping设置",
    content: "提供修改索引的 mapping 的信息的功能",
  },
  {
    label: "Setting设置",
    content: "提供修改索引 Setting 的信息的功能",
  },
  {
    label: "写入限流",
    content: "对索引写入量进行限制，避免过大影响集群稳定性",
  },
  {
    label: "跨集群同步(DCDR)",
    content: "跨集群数据复制功能，用于集群间的数据复制，类似ES官方的CCR能力",
  },
  {
    label: "索引别名",
    content: "支持通过接口来设置和修改索引别名",
  },
  {
    label: "资源管控",
    content: "支持对索引资源(磁盘)大小的管控，超过设定值会被限流",
  },
  {
    label: "安全管控",
    content: "提供了引擎原生的租户和安全管控能力，可以保证引擎层面的数据安全",
  },
  {
    label: "索引规划",
    content: "保障集群节点的容量均衡，避免索引在节点上的分布不合理问题",
  },
  {
    label: "冷热分离",
    content: "提供SSD和HDD两种类型的磁盘来保存索引，从而降低成本",
  },
  {
    label: "Shard调整",
    content: "根依据索引写入的历史数据来每天定时计算未来一天索引的 shard 个数，保障索引 shard 个数的合理性",
  },
];

export const getNodeDivideColumns = () => {
  const columns = [
    {
      title: "RegionID",
      dataIndex: "regionId",
      key: "regionId",
      width: 80,
      render: (val: number) => {
        if (val === -1) return "-";
        return val;
      },
    },
    {
      title: "Region名称",
      dataIndex: "regionName",
      key: "regionName",
      width: 110,
      onCell: () => ({
        style: { ...cellStyle, maxWidth: 100 },
      }),
      render: (val: string) => {
        return (
          <Tooltip placement="bottomLeft" title={val}>
            {val ? val : "-"}
          </Tooltip>
        );
      },
    },
    {
      title: "节点IP",
      dataIndex: "ip",
      key: "ip",
      width: 100,
      render: (val: string) => {
        return <Tooltip title={val}>{val ? val : "-"}</Tooltip>;
      },
    },
    {
      title: "主机名称",
      dataIndex: "hostname",
      key: "hostname",
      width: 100,
      render: (val: string) => {
        return <Tooltip title={val}>{val ? val : "-"}</Tooltip>;
      },
    },
    {
      title: "attribute",
      dataIndex: "attributes",
      key: "attributes",
      width: 180,
      onCell: () => ({
        style: { ...cellStyle, maxWidth: 180 },
      }),
      render: (val: string) => {
        return (
          <Tooltip placement="bottomLeft" title={val}>
            {val ? val : "-"}
          </Tooltip>
        );
      },
    },
    {
      title: "节点名称",
      dataIndex: "nodeSet",
      key: "nodeSet",
      width: 90,
      render: (val: string) => {
        return val || "-";
      },
    },
    {
      title: "磁盘使用率",
      dataIndex: "disk",
      key: "disk",
      width: 120,
      render: (_, record) => renderDiskRate(record),
    },
    {
      title: "节点规格",
      dataIndex: "machineSpec",
      key: "machineSpec",
      width: 110,
      onCell: () => ({
        style: { ...cellStyle, maxWidth: 110 },
      }),
      render: (val: string) => {
        return (
          <Tooltip placement="bottomLeft" title={val}>
            {val ? val : "-"}
          </Tooltip>
        );
      },
    },
    {
      title: "节点角色",
      dataIndex: "role",
      key: "role",
      width: 90,
      sorter: (a: any, b) => {
        const sortA = Array.isArray(a.role)
          ? a.role
              .map((index) => {
                return ROLE_TYPE_NO[index].label || index;
              })
              .sort()
              .toString()
          : a.role;
        const sortB = Array.isArray(b.role)
          ? b.role
              .map((index) => {
                return ROLE_TYPE_NO[index].label || index;
              })
              .sort()
              .toString()
          : b.role;
        return ROLE_TYPE_NO[sortA].label[0].localeCompare(ROLE_TYPE_NO[sortB].label[0]);
      },
      showSorterTooltip: false,
      render: (t: any) => {
        let str;
        if (Array.isArray(t)) {
          str = t
            .map((index) => {
              return ROLE_TYPE_NO[index].label || index;
            })
            .toString();
        } else {
          str = ROLE_TYPE_NO[t].label || t;
        }
        return str;
      },
    },
    {
      title: "节点状态",
      dataIndex: "status",
      key: "status",
      width: 90,
      sorter: (a, b) => a.status - b.status,
      showSorterTooltip: false,
      render: (status: number | number[]) => {
        let str;
        if (Array.isArray(status)) {
          str = status
            .map((index) => {
              return opNodeStatusMap[index];
            })
            .toString();
        } else {
          str = opNodeStatusMap[status];
        }
        return (
          <span className={status === 1 ? "success" : status === 2 ? "unline" : status === 3 ? "fail" : "unknown"}> {str || "-"}</span>
        );
      },
    },
  ];
  return columns;
};

const delPlugn = (data, reloadDataFn) => {
  Modal.confirm({
    title: `是否确定删除该${data.name}插件`,
    icon: <DeleteOutlined style={{ color: "red" }} />,
    content: `插件删除将永久在列表消失，请谨慎操作。`,
    width: 500,
    okText: "确定",
    cancelText: "取消",
    onOk() {
      userDelPlug(data.id).then((res) => {
        reloadDataFn();
      });
    },
  });
};

const getPlugnBtnList = (record: IPlug, reloadDataFn: any, setModalId) => {
  const install = {
    label: "安装",
    isOpenUp: isOpenUp,
    clickFunc: () => {
      setModalId("installplugin", record, reloadDataFn);
    },
  };

  const uninstall = {
    label: "卸载",
    isOpenUp: isOpenUp,
    clickFunc: () => {
      setModalId("uninstallPlugin", record, reloadDataFn);
    },
  };

  const edit = {
    label: "编辑",
    clickFunc: () => {
      setModalId("EditPluginDesc", { record }, reloadDataFn);
    },
  };

  const del = {
    label: "删除插件包",
    // needConfirm: true,
    isOpenUp: isOpenUp,
    // confirmText: "删除插件包",
    clickFunc: (record: any) => {
      delPlugn(record, reloadDataFn);
    },
  };

  const btnList = [];
  if (record.installed) {
    btnList.push(uninstall, edit);
  } else {
    btnList.push(install, edit, del);
  }
  return btnList;
};

export const pDefaultMap = {
  0: "系统默认",
  1: "ES能力",
  2: "平台能力",
};

export const getPluginListColumns = (fn: () => any, setModalId) => {
  const columns = [
    {
      title: "插件名称",
      dataIndex: "name",
      key: "name",
    },
    {
      title: "插件类型",
      dataIndex: "pdefault",
      key: "pdefault",
      sorter: true,
      render: (value: number) => {
        const text = pDefaultMap[value] || "未知类型";
        return text;
      },
    },
    {
      title: "使用版本",
      dataIndex: "version",
      sorter: true,
      key: "version",
    },
    {
      title: "状态",
      dataIndex: "installed",
      sorter: true,
      key: "installed",
      render: (value: boolean) => {
        return <>{value ? "已安装" : "未安装"}</>;
      },
    },
    {
      title: "描述",
      dataIndex: "desc",
      key: "desc",
      width: "25%",
      render: (value: string) => {
        return value || "-";
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      filterTitle: true,
      render: (id: number, record: IPlug) => {
        const btns = getPlugnBtnList(record, fn, setModalId);
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return columns;
};

export const getConfigInfoColumns = (setDrawerId: any) => {
  const operationList = [
    {
      label: "查看",
      isOpenUp: isOpenUp,
      clickFunc: (record: IPhyConfig) => {
        setDrawerId("configDetail", record.configData);
        return;
      },
    },
  ] as ITableBtn[];
  const columns = [
    {
      title: "节点角色",
      dataIndex: "enginName",
      key: "enginName",
    },
    {
      title: "配置类别",
      dataIndex: "typeName",
      key: "typeName",
      render: (value: string) => <span>{value || "-"}</span>,
    },
    {
      title: "配置内容",
      dataIndex: "configData",
      key: "configData",
      onCell: () => ({
        style: cellStyle,
      }),
    },
    {
      title: "操作",
      dataIndex: "id",
      key: "operation",
      width: "20%",
      render: (id: number, record: IPhyConfig) => {
        const btns = operationList;
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return columns;
};
