import React from "react";
import { Tag, Tooltip } from "antd";
import store from "store";
import moment from "moment";
import { timeFormat } from "constants/time";
import { BaseInfo } from "./base-info";
import { cellStyle } from "constants/table";
import { IPartition } from "@types/index-types";
import { IndexPartition } from "./partition";
import { IOpRecord, ITemplateLogic } from "@types/cluster/physics-type";
import { OperatingRecord } from "./record";
import { SecondStep } from "container/create-index/second-step";
import { NavRouterLink } from "container/custom-component";

export const DESC_LIST = [
  {
    label: "所属集群",
    key: "cluster",
    render: (cluster: string, record: ITemplateLogic) => (
      <>
        {/* <NavRouterLink
        needToolTip={true}
        element={cluster}
        href={`/cluster/logic/detail?clusterId=${record.id}&type=${record.type}#info`}
      /> */}
        {cluster}
      </>
    ),
  },
  {
    label: "所属项目",
    key: "appName",
  },
  {
    label: "所属项目ID",
    key: "appId",
  },
  {
    label: "负责人",
    key: "responsible",
    render: (value: string[]) => (
      <>
        <span>{value?.join(",")}</span>
      </>
    ),
  },
];

export enum TAB_LIST_KEY {
  info = "info",
  mapping = "mapping",
  statistic = "statistic",
  monitor = "monitor",
  source = "source",
  partition = "partition",
  record = "record",
}

export const INDEX_DETAIL_NEED_APP_TAB_LIST = [
  TAB_LIST_KEY.mapping,
  TAB_LIST_KEY.source,
  TAB_LIST_KEY.monitor,
  TAB_LIST_KEY.partition,
  TAB_LIST_KEY.record,
] as string[];

export const INDEX_TAB_LIST = [
  {
    name: "基本信息",
    key: TAB_LIST_KEY.info,
    content: (data) => <BaseInfo data={data} />,
  },
  {
    name: "Mapping",
    key: TAB_LIST_KEY.mapping,
    content: (data) => <SecondStep isShowPlaceholder={false}/>,
  },
  // {
  //   name: "实时监控",
  //   key: TAB_LIST_KEY.monitor,
  //   content: (data) => <div>实时监控</div>,
  // },
  {
    name: "分区详情",
    key: TAB_LIST_KEY.partition,
    content: (data) => <IndexPartition />,
  },
  {
    name: "操作记录",
    key: TAB_LIST_KEY.record,
    content: (data) => <OperatingRecord />,
  },
];

const menuMap = new Map();
INDEX_TAB_LIST.forEach((d) => {
  menuMap.set(d.key, d);
});
export const DETAIL_MENU_MAP = menuMap;

export const baseInfo: any = [
  [
    {
      key: "dateFormat",
      label: "滚动格式",
      render: (text) => {
        return text || '-';
      },
    },
    {
      key: "dateField",
      label: "分区字段",
      render: (text) => {
        return text || '-';
      },
    },
  ],
  [
    {
      key: "expireTime",
      label: "生命周期",
      render: (text) => {
        return text || '-';
      },
    },
    {
      key: "quota",
      label: "数据大小(GB)",
      render: (text: number) => (
        <>
          <span className="text-value">{text}</span>
        </>
      ),
    },
  ],
  [
    {
      key: "routingField",
      label: "路由字段",
      render: (text) => {
        return text || '-';
      },
    },
    {
      key: "first",
      label: "最早分区",
      render: (text) => {
        return text || '-';
      },
    },
  ],
  [
    {
      key: "createTime",
      label: "创建时间",
      render: (value: string) => <>{moment(value).format(timeFormat)}</>,
    },
    {
      key: "desc",
      label: "描述",
      render: (desc: string) => (
        <>
          <Tooltip placement="bottomLeft" title={desc}>
            {desc?.length > 20 ? desc.slice(0, 20) + "..." : desc}
          </Tooltip>
        </>
      ),
    },
  ],
];

export const getIndexPartitionColumns = () => {
  const cols = [
    {
      title: "分区名称",
      dataIndex: "index",
      key: "index",
      width: "12%",
      onCell: () => ({
        style: cellStyle,
      }),
      render: (text: string, record: IPartition) => {
        return (
          <Tooltip placement="bottomLeft" title={text}>
            {text}
          </Tooltip>
        );
      },
    },
    {
      title: "分区状态",
      dataIndex: "status",
      key: "status",
      width: "10%",
    },
    {
      title: "健康状态",
      dataIndex: "health",
      key: "health",
      width: "8%",
      render: (text: string) => {
        return <Tag color={text}>{text}</Tag>;
      },
    },
    {
      title: "shard个数",
      dataIndex: "pri",
      key: "pri",
      width: "10%",
      sorter: (a: IPartition, b: IPartition) => a.pri - b.pri,
    },
    {
      title: "副本个数",
      dataIndex: "rep",
      key: "rep",
      width: "10%",
      sorter: (a: IPartition, b: IPartition) => a.rep - b.rep,
    },
    {
      title: "文档个数",
      dataIndex: "docsCount",
      key: "docsCount",
      width: "10%",
      sorter: (a: IPartition, b: IPartition) => a.docsCount - b.docsCount,
    },
    {
      title: "删除文档个数",
      dataIndex: "docsDeleted",
      key: "docsDeleted",
      width: "12%",
      sorter: (a: IPartition, b: IPartition) => a.docsDeleted - b.docsDeleted,
    },
    {
      title: "主分片存储大小",
      dataIndex: "priStoreSize",
      key: "priStoreSize",
      width: "10%",
    },
    {
      title: "存储大小",
      dataIndex: "storeSize",
      key: "storeSize",
      width: "10%",
    },
  ];
  return cols;
};

export const getOperationColumns = () => {
  let cols = [
    {
      title: "业务ID",
      dataIndex: "bizId",
      key: "bizId",
      width: "10%",
      sorter: (a: IOpRecord, b: IOpRecord) => b.id - a.id,
    },
    {
      title: "操作时间",
      dataIndex: "operateTime",
      key: "operateTime",
      width: "15%",
      sorter: (a: IOpRecord, b: IOpRecord) =>
        new Date(b.operateTime).getTime() - new Date(a.operateTime).getTime(),
      render: (t: number) => moment(t).format(timeFormat),
    },
    {
      title: "模块",
      dataIndex: "module",
      key: "module",
      width: "10%",
    },
    {
      title: "操作内容",
      dataIndex: "content",
      key: "content",
      width: "30%",
    },
    {
      title: "行为",
      dataIndex: "operate",
      key: "operate",
      width: "10%",
    },
    {
      title: "操作人",
      dataIndex: "operator",
      key: "operator",
      width: "10%",
    },
  ];
  return cols;
};

export const getApplyOnlineColumns = () => {
  return [
    {
      title: "name",
      dataIndex: "name",
      key: "name",
    },
    {
      title: "id",
      dataIndex: "id",
      key: "id",
    },
  ];
};
