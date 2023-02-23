import React from "react";
import { Tag, Tooltip } from "antd";
import { BaseInfo } from "./base-info";
import { cellStyle } from "constants/table";
import { IPartition } from "typesPath/index-types";
import { IndexPartition } from "./partition";
import { IOpRecord, ITemplateLogic } from "typesPath/cluster/physics-type";
import { OperatingRecord } from "./record";
import { JsonMapping } from "../edit/jsonMapping";
import { JsonSetting } from "../edit/jsonSetting";
import { LEVEL_MAP } from "constants/common";
import { transTimeFormat, bytesUnitFormatter, formatDecimalPoint } from "lib/utils";
import { columnsRender } from "../management/config";
import { renderPriority } from "../service/config";
export const formColumns = [
  {
    type: "input",
    title: "主机名称",
    dataIndex: "ip",
    placeholder: "请输入",
  },
];
export const queryFormText: { searchText: string; resetText: string } = {
  searchText: "查询",
  resetText: "重置",
};
export const getColumns = (setModalId?: any, setDrawerId?: any, reloadDataFn?: any) => {
  return [
    {
      title: "主机名称",
      dataIndex: "ip",
      render: (item, row) => {
        return {
          children: columnsRender(item),
          props: {
            rowSpan: row.rowSpan,
          },
        };
      },
    },
    {
      title: "Shard个数",
      dataIndex: "shardCount",
      render: (item, row) => {
        return {
          children: columnsRender(item),
          props: {
            rowSpan: row.rowSpan,
          },
        };
      },
    },
    {
      title: "文档总个数",
      dataIndex: "totalDocs",
      render: (item, row) => {
        return {
          children: columnsRender(item),
          props: {
            rowSpan: row.rowSpan,
          },
        };
      },
    },
    {
      title: "总存储大小",
      dataIndex: "totalStore",
      render: (item, row) => {
        return {
          children: bytesUnitFormatter(item, "float"),
          props: {
            rowSpan: row.rowSpan,
          },
        };
      },
    },
    {
      title: "Shard状态",
      dataIndex: "state",
      render: (item, row) => columnsRender(item),
    },
    {
      title: "shard序号",
      dataIndex: "shard",
      render: (item) => columnsRender(item),
    },
    {
      title: "进程名称",
      dataIndex: "node",
      render: (item) => columnsRender(item),
    },
    {
      title: "文档个数",
      dataIndex: "docs",
      render: (item) => columnsRender(item),
    },
    {
      title: "存储大小",
      dataIndex: "storeInByte",
      render: (item) => bytesUnitFormatter(item, "float"),
    },
  ].map((item) => ({
    ...item,
    align: "center",
  }));
};
export const DESC_LIST = [
  {
    label: "所属集群",
    key: "cluster",
    width: 250,
    render: (cluster: string) => cluster || "-",
  },
  {
    label: "业务等级",
    key: "level",
    render: (value: any) => <span>{LEVEL_MAP[value - 1]?.label || "-"}</span>,
  },
];

export enum TAB_LIST_KEY {
  info = "info",
  mapping = "mapping",
  setting = "setting",
  partition = "partition",
  record = "record",
}

export const INDEX_TAB_LIST = [
  {
    name: "基本信息",
    key: TAB_LIST_KEY.info,
    content: (data) => <BaseInfo data={data} />,
  },
  {
    name: "Mapping",
    key: TAB_LIST_KEY.mapping,
    content: (data) => <JsonMapping isShowPlaceholder={false} />,
  },
  {
    name: "Setting",
    key: TAB_LIST_KEY.setting,
    content: (data) => <JsonSetting isShowPlaceholder={false} />,
  },
  {
    name: "分区详情",
    key: TAB_LIST_KEY.partition,
    content: (data) => <IndexPartition />,
  },
  {
    name: "操作记录",
    key: TAB_LIST_KEY.record,
    content: (data) => <OperatingRecord recordType="bizId" />,
  },
];

const menuMap = new Map();
INDEX_TAB_LIST.forEach((d) => {
  menuMap.set(d.key, d);
});
export const DETAIL_MENU_MAP = menuMap;

export const baseInfo: any = [
  {
    key: "dateFormat",
    label: "滚动格式",
    render: (text) => {
      return text || "-";
    },
  },
  {
    key: "dateField",
    label: "分区字段",
    render: (text) => {
      return text || "-";
    },
  },
  {
    key: "dateFieldFormat",
    label: "时间格式",
    render: (text) => {
      return text || "-";
    },
  },
  {
    key: "expireTime",
    label: "生命周期",
    render: (text) => {
      return text || "-";
    },
  },

  {
    key: "quota",
    label: "数据大小(GB)",
    render: (text: number) => (
      <>
        <span className="text-value">{text || "-"}</span>
        {/* <a
            href="http://wiki.intra.xiaojukeji.com/pages/viewpage.action?pageId=235801075"
            target="_blank"
          >
            Arius Quota说明
          </a> */}
      </>
    ),
  },
  {
    key: "first",
    label: "最早分区",
    render: (text) => {
      return text || "-";
    },
  },

  {
    key: "createTime",
    label: "创建时间",
    render: (value: string) => transTimeFormat(value),
  },
  {
    key: "desc",
    label: "描述",
    render: (desc: string) => (
      <>
        <Tooltip placement="bottomLeft" title={desc}>
          {desc?.length > 20 ? desc.slice(0, 20) + "..." : desc || "-"}
        </Tooltip>
      </>
    ),
  },

  {
    key: "disableIndexRollover",
    label: "Rollover",
    render: (value: string) => <>{value === null || value === undefined ? "-" : value ? "否" : "是"}</>,
  },
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
      render: (text: string) => formatDecimalPoint(text),
    },
    {
      title: "存储大小",
      dataIndex: "storeSize",
      key: "storeSize",
      width: "10%",
      render: (text: string) => formatDecimalPoint(text),
    },
  ];
  return cols;
};

export const getOperationColumns = (setDrawerId) => {
  let cols = [
    {
      title: "业务ID",
      dataIndex: "id",
      sorter: true,
      width: 100,
    },
    {
      title: "操作时间",
      dataIndex: "operateTime",
      sorter: true,
      width: 160,
      render: (t) => transTimeFormat(t),
    },
    {
      title: "模块",
      dataIndex: "module",
      width: 100,
    },
    {
      title: "操作内容",
      dataIndex: "content",
      width: 360,
      render: (text: string, record: any) => {
        let diffArr = ["编辑MAPPING", "编辑SETTING", "配置文件变更"];
        if (diffArr.includes(record.operate)) {
          try {
            const data = text ? JSON.parse(text) : {};
            let operate: string;
            if (record.operate === "编辑MAPPING") {
              operate = "Mapping";
            } else if (record.operate === "编辑SETTING") {
              operate = "Setting";
            } else if (record.operate === "配置文件变更") {
              operate = "配置文件";
            }
            return (
              <>
                <span style={{ marginRight: "5px" }}>{record.operate}</span>
                <a href="JavaScript:;" onClick={() => setDrawerId("mappingSettingDiff", { operate, data })}>
                  查看
                </a>
              </>
            );
          } catch (err) {
            console.log(err);
          }
        } else {
          return columnsRender(text) || "-";
        }
      },
    },
    {
      title: "行为",
      dataIndex: "operate",
      width: 160,
    },
    {
      title: "操作人",
      dataIndex: "userOperation",
      width: 100,
    },
  ];
  return cols;
};
