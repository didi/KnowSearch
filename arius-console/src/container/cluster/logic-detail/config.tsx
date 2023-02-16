import { IMenuItem, IBaseInfo } from "typesPath/base-types";
import React from "react";
import { DeleteOutlined } from "@ant-design/icons";
import { Modal, Tooltip } from "antd";
import { cellStyle } from "constants/table";
import { ROLE_TYPE, isOpenUp, LEVEL_MAP } from "constants/common";
import { IPlug } from "typesPath/plug-types";
import { submitWorkOrder } from "api/common-api";
import { IWorkOrder } from "typesPath/params-types";
import store from "store";
import { userDelPlug } from "api/plug-api";
import { clusterTypeMap } from "constants/status-map";
import { opNodeStatusMap } from "../physics-detail/constants";
import { renderDiskRate } from "../../custom-component";
import { transTimeFormat, getFormatJsonStr, isSuperApp } from "lib/utils";
import { ClusterInfo } from "./base-info";
import { LogicNodeList } from "./logic-node-list";
import "./index.less";

const appInfo = {
  app: store.getState().app.appInfo,
  user: store.getState().user.getName,
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
}

export const TAB_LIST = [
  {
    name: "集群概览",
    key: TAB_LIST_KEY.info,
    content: (params) => <ClusterInfo logicBaseInfo={params.clusterInfo} />,
  },
  {
    name: "节点列表",
    key: TAB_LIST_KEY.node,
    content: () => <LogicNodeList />,
  },
];

const menuMap = new Map<string, IMenuItem>();
TAB_LIST.forEach((d) => {
  menuMap.set(d.key, d);
});

export const DETAIL_MENU_MAP = menuMap;

export const baseInfo: any = [
  {
    label: "ES版本",
    key: "esClusterVersion",
  },
  {
    label: "Gateway地址",
    key: "gatewayAddress",
    render: (value: string) => <span>{value || "-"}</span>,
  },
  {
    label: "分片数",
    key: "activeShardNum",
  },
  {
    label: "数据节点数",
    key: "dataNodeNum",
  },
  {
    label: "创建时间",
    key: "createTime",
    render: (time: number) => transTimeFormat(time),
  },
  {
    label: "集群描述",
    key: "memo",
    render: (text: string) => (
      <>
        <Tooltip placement="bottomLeft" title={text}>
          {text ? (text.length > 20 ? text.slice(0, 20) + "..." : text) : "_"}
        </Tooltip>
      </>
    ),
  },
];
interface ICardInfo {
  label: string;
  configList: IBaseInfo[];
  btns?: JSX.Element[];
  col: number;
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

export const DESC_LIST = [
  {
    label: "集群类型",
    key: "type",
    render: (value: number) => (
      <>
        <span>{clusterTypeMap[value] || "-"}</span>
      </>
    ),
  },
  {
    label: "集群版本",
    key: "esClusterVersion",
    render: (val: number) => <span>{val || "-"}</span>,
  },
  {
    label: "业务等级",
    key: "level",
    render: (value: number) => (
      <>
        <span>{LEVEL_MAP[value - 1]?.label || "-"}</span>
      </>
    ),
  },
];

export const getLogicNodeColumns = (dataList, reloadData: Function, type: string) => {
  const columns = [
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
      width: 100,
      render: (val: string) => {
        return val || "-";
      },
    },
    {
      title: "磁盘使用率",
      dataIndex: "diskUsage",
      key: "diskUsage",
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
      width: 100,
      render: (t: any) => {
        let str;
        if (Array.isArray(t)) {
          str = t
            .map((index) => {
              return ROLE_TYPE[index].label || index;
            })
            .toString();
        } else {
          str = ROLE_TYPE[t].label || t;
        }
        return str;
      },
    },
    {
      title: "节点状态",
      dataIndex: "status",
      key: "status",
      width: 100,
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

const unintallPlugn = (data, reloadDataFn) => {
  Modal.confirm({
    title: `是否确定卸载该${data.name}插件`,
    content: `插件卸载、安装需要重启集群，点击确认后，将自动提交工单。`,
    width: 500,
    okText: "确定",
    cancelText: "取消",
    onOk() {
      const contentObj = {
        operationType: 4,
        logicClusterId: data.id,
        logicClusterName: data.name,
        plugIds: data.id,
        plugName: data.name,
        plugDesc: data.plugDesc,
        type: "6",
      };
      const params: IWorkOrder = {
        contentObj,
        submitorProjectId: appInfo.app()?.id,
        submitor: appInfo.user("userName") || "",
        description: "",
        type: "logicClusterPlugOperation",
      };
      return submitWorkOrder(params, () => {
        reloadDataFn();
      });
    },
  });
};

const intallPlugn = (data, reloadDataFn) => {
  Modal.confirm({
    title: `是否确定安装该${data.name}插件`,
    content: `插件卸载、安装需要重启集群，点击确认后，将自动提交工单。`,
    width: 500,
    okText: "确定",
    cancelText: "取消",
    onOk() {
      const contentObj = {
        operationType: 3,
        logicClusterId: data.id,
        logicClusterName: data.name,
        plugIds: data.id,
        plugName: data.name,
        plugDesc: data.plugDesc,
        type: "6",
      };
      const params: IWorkOrder = {
        contentObj,
        submitorProjectId: appInfo.app()?.id,
        submitor: appInfo.user("userName") || "",
        description: "",
        type: "logicClusterPlugOperation",
      };
      return submitWorkOrder(params, () => {
        reloadDataFn();
      });
    },
  });
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

export const getPlugnBtnList = (record: IPlug, reloadDataFn: any) => {
  const install = {
    label: "安装",
    clickFunc: () => {
      intallPlugn(record, reloadDataFn);
    },
  };

  const uninstall = {
    label: "卸载",
    isOpenUp: isOpenUp,
    clickFunc: () => {
      unintallPlugn(record, reloadDataFn);
    },
  };

  const edit = {
    label: "编辑",
    isOpenUp: isOpenUp,
    clickFunc: () => {
      delPlugn(record, reloadDataFn);
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
  } else if (record.pdefault) {
    btnList.push(install, edit);
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

export const getPluginListColumns = (fn: () => any) => {
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
      render: (value: number) => {
        const text = pDefaultMap[value] || "未知类型";
        return text;
      },
    },
    {
      title: "使用版本",
      dataIndex: "nodeSpec",
      key: "nodeSpec",
    },
    {
      title: "状态",
      dataIndex: "installed",
      key: "installed",
      render: (value: boolean) => {
        return <>{value ? "已安装" : "未安装"}</>;
      },
    },
    {
      title: "描述",
      dataIndex: "desc",
      key: "desc",
      render: (value: string) => {
        return value || "-";
      },
    },
  ];
  return columns;
};
