import { Tag, Tooltip, Modal, message, Progress } from "antd";
import { renderOperationBtns, NavRouterLink } from "container/custom-component";
import { GatewayPermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import React from "react";
import { cellStyle } from "constants/table";
import { LEVEL_MAP } from "constants/common";
import { deleteGateway } from "api/gateway-manage";
import { IMenuItem } from "typesPath/base-types";
import { NodeList } from "./nodeList";
import { ConfigList } from "./ConfigList";
import "./index.less";

const renderText = (text) => {
  return <div className="dsl-overflow-auto">{text}</div>;
};

export const columnsRender = (item: string) => {
  return (
    <Tooltip placement="right" title={renderText(item)}>
      <div
        className="row-ellipsis pointer"
        style={{
          maxWidth: "100%",
          display: "inline-block",
        }}
      >
        {item || "-"}
      </div>
    </Tooltip>
  );
};

export const getQueryFormConfig = () => {
  return [
    {
      type: "select",
      title: "Gateway集群状态:",
      dataIndex: "health",
      options: [
        {
          title: "green",
          value: "0",
        },
        {
          title: "yellow",
          value: "1",
        },
        {
          title: "red",
          value: "2",
        },
        {
          title: "unknown",
          value: "-1",
        },
      ],
      placeholder: "请选择",
    },
    {
      type: "input",
      title: "Gateway集群名称:",
      dataIndex: "clusterName",
      placeholder: "请输入Gateway集群名称",
      rules: [
        {
          required: false,
          validator: async (rule: any, value: string) => {
            if (value && value.length > 128) {
              return Promise.reject("最大限制128字符");
            }
            return Promise.resolve();
          },
        },
      ],
    },
  ];
};

export const getBtnList = (record, setModalId?: any, setDrawerId?: any, reloadDataFn?: (del?: boolean) => void) => {
  const isOpenUp = false;

  let btn = [
    {
      label: "升级",
      type: "primary",
      isOpenUp: isOpenUp,
      invisible: !hasOpPermission(GatewayPermissions.PAGE, GatewayPermissions.UPGRADE) || !record?.ecmAccess,
      clickFunc: () => {
        setModalId("gatewayVersion", { type: "upgrade", record }, reloadDataFn);
      },
    },
    {
      label: "重启",
      type: "primary",
      isOpenUp: isOpenUp,
      invisible: !hasOpPermission(GatewayPermissions.PAGE, GatewayPermissions.RESTART) || !record?.ecmAccess,
      clickFunc: () => {
        setDrawerId("gatewayReset", record, reloadDataFn);
      },
    },
    {
      label: "扩缩容",
      type: "primary",
      isOpenUp: isOpenUp,
      invisible: !hasOpPermission(GatewayPermissions.PAGE, GatewayPermissions.EXPAND_SHRINK) || !record?.ecmAccess,
      clickFunc: () => {
        setDrawerId("expandShrinkGatewayCluster", record, reloadDataFn);
      },
    },
    {
      label: "回滚",
      type: "primary",
      isOpenUp: isOpenUp,
      invisible: !hasOpPermission(GatewayPermissions.PAGE, GatewayPermissions.ROLLBACK) || !record?.ecmAccess,
      clickFunc: () => {
        setModalId("gatewayVersion", { type: "rollback", record }, reloadDataFn);
      },
    },
    {
      label: "编辑",
      type: "primary",
      isOpenUp: isOpenUp,
      invisible: !hasOpPermission(GatewayPermissions.PAGE, GatewayPermissions.EDIT),
      clickFunc: () => {
        setModalId("gatewayEdit", record, reloadDataFn);
      },
    },
    {
      label: "下线",
      type: "primary",
      invisible: !hasOpPermission(GatewayPermissions.PAGE, GatewayPermissions.OFFLINE),
      clickFunc: () => {
        Modal.confirm({
          title: `确定下线 ${record.clusterName} 吗？`,
          content: (
            <>
              {record.bindClusterPhyList?.length ? (
                <>
                  <div>该Gateway目前还绑定如下几个集群：</div>
                  <div className="gateway-cluster-box">{record.bindClusterPhyList.map((item) => item.name)?.join("、")}</div>
                </>
              ) : null}
            </>
          ),
          width: 500,
          okText: "确定",
          cancelText: "取消",
          onOk() {
            deleteGateway(record.id).then(() => {
              message.success(`操作成功`);
              reloadDataFn();
            });
          },
        });
      },
    },
  ];
  return btn;
};

const statusTag = (item) => {
  switch (item) {
    case "red":
    case 2:
      return <Tag color="error">red</Tag>;
    case "yellow":
    case 1:
      return <Tag color="warning">yellow</Tag>;
    case "green":
    case 0:
      return <Tag color="success">green</Tag>;
    case "unknown":
    case -1:
      return <Tag>unknown</Tag>;
    default:
      return "-";
  }
};

export const getColumns = (setModalId?: any, setDrawerId?: any, reloadDataFn?: any, props?: any) => {
  return [
    {
      title: "Gateway集群名称",
      dataIndex: "clusterName",
      fixed: "clusterName",
      width: 230,
      render: (item, record) => {
        return (
          <div className="two-row-ellipsis">
            <NavRouterLink
              needToolTip
              maxShowLength={50}
              element={item}
              href={`/cluster/gateway/detail?name=${record.clusterName}&id=${record.id}&componentId=${record.componentId}`}
            />
          </div>
        );
      },
    },
    {
      title: "Gateway集群状态",
      dataIndex: "health",
      width: 150,
      render: (item) => <div style={{ width: "2vw" }}>{statusTag(item)}</div>,
    },
    {
      title: "版本",
      dataIndex: "version",
      key: "version",
      width: 120,
    },
    {
      title: "描述",
      dataIndex: "memo",
      key: "memo",
      width: 230,
      lineClampOne: true,
      onCell: () => ({
        style: { ...cellStyle, maxWidth: 200 },
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
      fixed: "right",
      filterTitle: true,
      width: 250,
      render: (index: number, record) => {
        const records = { ...record, ...props };
        const btns = getBtnList(records, setModalId, setDrawerId, reloadDataFn);
        return renderOperationBtns(btns, record, 7);
      },
    },
  ];
};

export enum TAB_LIST_KEY {
  node = "node",
  config = "config",
}

export const TAB_LIST = [
  {
    name: "节点列表",
    key: TAB_LIST_KEY.node,
    content: (baseInfo: any) => <NodeList />,
  },
  {
    name: "配置列表",
    key: TAB_LIST_KEY.config,
    content: ({ history }) => <ConfigList type="gateway" history={history} />,
  },
];

const menuMap = new Map<string, IMenuItem>();
TAB_LIST.forEach((d) => {
  menuMap.set(d.key, d);
});

export const DETAIL_MENU_MAP = menuMap;

export const GATEWAY_DESC_LIST = [
  {
    label: "数据中心",
    key: "dataCenter",
  },
  {
    label: "代理地址",
    key: "proxyAddress",
  },
  // {
  //   label: "业务等级",
  //   key: "level",
  //   render: (value: number) => LEVEL_MAP[value - 1]?.label || "-",
  // },
];

export const getNodeColumns = () => {
  const columns = [
    {
      title: "节点IP",
      dataIndex: "hostName",
      width: 150,
      render: (val: string, record: any) => {
        return <Tooltip title={`${val}:${record.port}`}>{val && record.port ? `${val}:${record.port}` : "-"}</Tooltip>;
      },
    },
    {
      title: "实例名称",
      dataIndex: "hostName",
      width: 200,
      render: (val: string) => {
        return <Tooltip title={val}>{val ? val : "-"}</Tooltip>;
      },
    },
    {
      title: "接入网关连接数",
      dataIndex: "httpConnectionNum",
      width: 150,
    },
    {
      title: "CPU使用率",
      dataIndex: "cpuUsage",
      width: 200,
      render: (num) => {
        let strokeColor: string;
        let yellow = "#F4A838";
        let red = "#df6d62";
        if (num > 90) {
          strokeColor = red;
        } else if (num > 70) {
          strokeColor = yellow;
        } else {
          strokeColor = "#1473FF";
        }
        return (
          <>
            <Progress className="gateway-node-process" percent={num || 0} size="small" strokeColor={strokeColor} showInfo={false} />
            <span>{`${num || 0}%`}</span>
          </>
        );
      },
    },
  ];
  return columns;
};

const getConfigBtnList = (record: any, reloadDataFn: any, setModalId, setDrawerId, clusterId, componentId, history) => {
  const view = {
    label: "查看",
    clickFunc: () => {
      setDrawerId("editConfigGroup", { type: "view", cluster: "gateway", record: { ...record, clusterId, componentId } }, reloadDataFn);
    },
  };

  const edit = {
    label: "编辑",
    invisible: !record?.supportEditAndRollback,
    clickFunc: () => {
      setDrawerId(
        "editConfigGroup",
        { type: "edit", record: { ...record, clusterId, componentId, cluster: "gateway", history } },
        reloadDataFn
      );
    },
  };

  const rollback = {
    label: "回滚",
    invisible: !record?.supportEditAndRollback,
    clickFunc: () => {
      setModalId("rollbackConfig", { cluster: "gateway", record: { ...record, clusterId, componentId }, history }, reloadDataFn);
    },
  };

  const btnList = [view, edit, rollback];
  return btnList;
};

export const getConfigListColumns = ({ reloadData, setModalId, setDrawerId, clusterId, componentId, history }) => {
  const columns = [
    {
      title: "配置组名称",
      dataIndex: "groupName",
      key: "groupName",
      width: 200,
    },
    {
      title: "实例名称",
      dataIndex: "configName",
      key: "configName",
      render: (item, record) => {
        if (!record.nodes.length) return "-";
        return (record.nodes || []).map((item) => `${item.hostName || "-"}（${item.hostName || "-"}:${item.port ?? "-"}）`).join("，");
      },
    },
    {
      title: "版本",
      dataIndex: "version",
      key: "version",
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      render: (id: number, record: any) => {
        const btns = getConfigBtnList(record, reloadData, setModalId, setDrawerId, clusterId, componentId, history);
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return columns;
};
