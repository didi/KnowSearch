import { Tooltip } from "antd";
import { bytesUnitFormatter } from "lib/utils";
import React from "react";


export const queryFormText: { searchText: string, resetText: string } = {
  searchText: '查询',
  resetText: '重置'
};


export const formColumns = [
  {
    type: 'input',
    title: '主机名称',
    dataIndex: 'ip',
    placeholder: "请输入",
  }
];


export const renderText = (text) => {
  return (
    <div className="dsl-overflow-auto">
      {text}
    </div>
  )
}
const columnsRender = (item: string, width: string = "none") => {
  return <div className="two-row-ellipsis pointer" style={{
    width: width
  }}>
    <Tooltip placement="right" title={renderText(item)}>{item}</Tooltip>
  </div>;
};

export const getColumns = (setModalId?: any,
  setDrawerId?: any,
  reloadDataFn?: any) => {
  return [
    {
      title: "主机名称",
      dataIndex: "ip",
      render: (item, row) =>  {
        return {
          children: columnsRender(item),
          props: {
            rowSpan: row.rowSpan
          },
        };
      }
    },
    {
      title: "Shard个数",
      dataIndex: "shardCount",
      render: (item, row) =>  {
        return {
          children: columnsRender(item),
          props: {
            rowSpan: row.rowSpan
          },
        };
      }
    },
    {
      title: "文档总个数",
      dataIndex: "totalDocs",
      render: (item, row) =>  {
        return {
          children: columnsRender(item),
          props: {
            rowSpan: row.rowSpan
          },
        };
      }
    },
    {
      title: "总存储大小",
      dataIndex: "totalStore",
      render: (item, row) =>  {
        return {
          children: bytesUnitFormatter(item, "float"),
          props: {
            rowSpan: row.rowSpan
          },
        };
      }
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
  ].map(item => ({
    ...item,
    align: 'center'
  }))
};

