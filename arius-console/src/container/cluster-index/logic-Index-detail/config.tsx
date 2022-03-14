import React from "react";
import { Tag, Tooltip } from "antd";
import store from "store";
import moment from "moment";
import { timeFormat } from "constants/time";
import { BaseInfo } from "./base-info";
import { cellStyle } from "constants/table";
import { IPartition } from "typesPath/index-types";
import { IndexPartition } from "./partition";
import { IOpRecord, ITemplateLogic } from "typesPath/cluster/physics-type";
import { OperatingRecord } from "./record";
import { SecondStep } from "container/create-index/second-step";
import { Setting } from "container/create-index/setting";
import { NavRouterLink } from "container/custom-component";
import { DcdrInfo } from './dcdr';
import { LEVEL_MAP } from "constants/common";

export const DESC_LIST = [
  {
    label: "所属集群",
    key: "cluster",
    width: 250,
    render: (cluster: string, record: ITemplateLogic) => (
      <div style={{ 
        width: 150,
        overflow: "hidden",
        whiteSpace: 'nowrap',
        textOverflow: 'ellipsis',
       }}>
        <Tooltip title={cluster}>
          {cluster}
        </Tooltip>
        {/* <NavRouterLink
        needToolTip={true}
        element={cluster}
        href={`/cluster/logic/detail?clusterId=${record.id}&type=${record.type}#info`}
      /> */}
      </div>
    ),
  },
  {
    label: "业务等级",
    key: "level",
    render: (value: any) => (
      <>
        <span>{LEVEL_MAP[value - 1]?.label || "-"}</span>
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
  setting = "setting",
  statistic = "statistic",
  monitor = "monitor",
  source = "source",
  partition = "partition",
  record = "record",
  dcdr = "dcdr",
}

export const INDEX_DETAIL_NEED_APP_TAB_LIST = [
  TAB_LIST_KEY.mapping,
  TAB_LIST_KEY.setting,
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
  {
    name: "Setting",
    key: TAB_LIST_KEY.setting,
    content: (data) => <Setting />,
  },
  {
    name: "DCDR",
    key: TAB_LIST_KEY.dcdr,
    content: () => <DcdrInfo />,
  },
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
            {desc?.length > 20 ? desc.slice(0, 20) + "..." : (desc || '-')}
          </Tooltip>
        </>
      ),
    },
  ],
  [
    {
      key: "disableIndexRollover",
      label: "Rollover",
      render: (value: string) => <>{value ? '否' : '是'}</>,
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

export const getOperationColumns = (setDrawerId) => {
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
      render: (text: string, record: any) => {
        let diffContext: any = '' 
        try {
          if (record?.moduleId === 1 && record?.operateId === 3) {
            const data = text ? JSON.parse(text) : {};
            diffContext = (<span>编辑Mapping <a href="JavaScript:;" onClick={() => setDrawerId('mappingDiff', data)}>查看</a></span>)
          } else if (record?.moduleId === 13 && record?.operateId === 3) {
            const data = text ? JSON.parse(text) : {};
            for (const key in data.diffContext) {
              if (diffContext && diffContext.length) {
                // 如果已经有了的情况加一个分号
                diffContext += '; ';
              } else {
                // 编辑setting文案统一处理 只出现一次
                diffContext = '编辑setting，';
              }
              if (key == 'index.number_of_replicas') {
                // 0 开启 1关闭
                diffContext += data.diffContext[key] == "1" ? '关闭取消副本' : '开启取消副本'
              } else {
                // request 关闭  anync 开启
                diffContext += data.diffContext[key] == "async" ? '开启异步translog' : '关闭异步translog'
              }
            }
            // diffContext = data.diffContext && data.diffContext.index.number_of_replica ?  === 1 
          } else {
            diffContext = text
          }
        } catch(err) {
          console.log(err)
        }
        return <>{diffContext || '-'}</>
      }
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

export const dcdrInfo: any = [
  [
    {
      key: "master",
      label: "主集群信息: ",
      render: (text) => {
        return text || '';
      },
    },
    {
      key: "slave",
      label: "从集群信息: ",
      render: (text) => {
        return text || '';
      },
    },
  ],
  [
    {
      key: "masterClusterName",
      label: "集群名称",
      render: (text) => {
        return text || '-';
      },
    },
    {
      key: "slaveClusterName",
      label: "集群名称",
      render: (text) => {
        return text || '-';
      },
    },
  ],
  [
    {
      key: "masterTemplateCheckPoint",
      label: "位点信息",
      render: (text) => {
        return <>{text || typeof text == "number" ? text : "-"}</>;
      },
    },
    {
      key: "slaveTemplateCheckPoint",
      label: "位点信息",
      render: (text) => {
        return <>{text || typeof text == "number" ? text : "-"}</>;
      },
    },
  ],
  [
    {
      key: "templateCheckPointDiff",
      label: "位点差",
      render: (text) => {
        return <span style={{ color: '#2F81F9', background: 'rgb(243, 249, 255)', padding: '0px 4px' }}>{text || typeof text == "number" ? text : "-"}</span>
      },
    },
  ],
]