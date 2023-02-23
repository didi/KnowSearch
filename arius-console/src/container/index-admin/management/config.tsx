import React from "react";
import { Tag, Tooltip } from "antd";
import { BaseDetail } from "component/dantd/base-detail";
import { renderOperationBtns, NavRouterLink } from "container/custom-component";
import { IndexPermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import { Mapping, Setting } from "../component";
import { OperatingRecord } from "container/index-tpl-management/detail/record";
import { ShardList } from "container/index-tpl-management/detail/shard";
import { CopyOutlined } from "@ant-design/icons";
import { copyString, formatDecimalPoint } from "lib/utils";
import { renderPriority } from "container/index-tpl-management/service/config";

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

export const queryFormText: { searchText: string; resetText: string } = {
  searchText: "查询",
  resetText: "重置",
};

export const getQueryFormConfig = (cluster: any) => {
  return [
    {
      type: "input",
      title: "索引名称:",
      dataIndex: "index",
      placeholder: "请输入索引名称",
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
    {
      type: "select",
      title: "所属集群:",
      dataIndex: "cluster",
      options: cluster.map((item) => ({
        title: item,
        value: item,
      })),
      placeholder: "请选择",
    },
    {
      type: "select",
      title: "健康状态:",
      dataIndex: "health",
      options: [
        {
          title: "green",
          value: "green",
        },
        {
          title: "yellow",
          value: "yellow",
        },
        {
          title: "red",
          value: "red",
        },
      ],
      placeholder: "请选择",
    },
  ];
};

export const getBtnList = (record, setModalId?: any, setDrawerId?: any, reloadDataFn?: (del?: boolean) => void) => {
  const isOpenUp = false;
  const clusterName = record.cluster;
  const indexName = record.index;

  let btn = [
    {
      label: "编辑Mapping",
      type: "primary",
      isOpenUp: isOpenUp,
      invisible: !hasOpPermission(IndexPermissions.PAGE, IndexPermissions.EDIT_MAPPING),
      clickFunc: () => {
        setDrawerId("editIndexMapping", record, reloadDataFn);
      },
    },
    {
      label: "编辑Setting",
      type: "primary",
      isOpenUp: isOpenUp,
      invisible: !hasOpPermission(IndexPermissions.PAGE, IndexPermissions.EDIT_SETTING),
      clickFunc: () => {
        setDrawerId("editIndexSetting", record, reloadDataFn);
      },
    },
    {
      label: "设置别名",
      type: "primary",
      isOpenUp: isOpenUp,
      invisible: !hasOpPermission(IndexPermissions.PAGE, IndexPermissions.SET_ALIAS),
      clickFunc: () => {
        setModalId("setAlias", record, reloadDataFn);
      },
    },
    {
      label: "删除别名",
      type: "primary",
      isOpenUp: isOpenUp,
      invisible: !hasOpPermission(IndexPermissions.PAGE, IndexPermissions.DELETE_ALIAS) || !record.aliases?.length,
      clickFunc: () => {
        setModalId("deleteAlias", record, reloadDataFn);
      },
    },
    {
      label: "下线",
      type: "primary",
      invisible: !hasOpPermission(IndexPermissions.PAGE, IndexPermissions.OFFLINE),
      clickFunc: () => {
        setModalId(
          "deleteIndex",
          {
            delList: [
              {
                cluster: clusterName,
                index: indexName,
              },
            ],
            title: `确定下线索引 ${indexName} ?`,
          },
          () => {
            reloadDataFn && reloadDataFn(true);
          }
        );
      },
    },
  ];
  return btn;
};

const statusTag = (item) => {
  switch (item) {
    case "red":
      return <Tag color="error">red</Tag>;
    case "yellow":
      return <Tag color="warning">yellow</Tag>;
    case "green":
      return <Tag color="success">green</Tag>;
    default:
      return "-";
  }
};

export const getColumns = (setModalId?: any, setDrawerId?: any, reloadDataFn?: any, superApp?: boolean) => {
  return [
    {
      title: "索引名称",
      dataIndex: "index",
      fixed: "left",
      width: 180,
      filters: superApp
        ? [
            {
              text: "展示元数据集群索引",
              value: true,
            },
          ]
        : null,
      render: (item, record) => {
        return (
          <div className="two-row-ellipsis pointer index-name-cell">
            <NavRouterLink
              needToolTip
              maxShowLength={50}
              element={item}
              href={`/index-admin/management/detail?index=${record.index}&cluster=${record.cluster}`}
            />
          </div>
        );
      },
    },
    {
      title: "所属集群",
      dataIndex: "cluster",
      width: 150,
      render: (item) => columnsRender(item),
    },
    {
      title: "健康状态",
      dataIndex: "health",
      width: 80,
      render: (item) => <div style={{ width: "2vw" }}>{statusTag(item)}</div>,
    },
    // {
    //   title: "索引状态",
    //   dataIndex: "status",
    //   render: (text) => {
    //     return text == 'open' ? '开启' : '关闭'
    //   },
    // },
    {
      title: "Shard个数",
      dataIndex: "pri",
      width: 110,
      sorter: true,
    },
    {
      title: "副本个数",
      dataIndex: "rep",
      width: 100,
      sorter: true,
    },
    {
      title: "文档个数",
      dataIndex: "docsCount",
      width: 100,
      sorter: true,
    },
    {
      title: "删除文档个数",
      dataIndex: "docsDeleted",
      width: 130,
      sorter: true,
    },
    {
      title: "主分片存储大小",
      dataIndex: "priStoreSize",
      width: 130,
      sorter: true,
      render: (item) => columnsRender(formatDecimalPoint(item) + ""),
    },
    {
      title: "存储大小",
      dataIndex: "storeSize",
      width: 80,
      sorter: true,
      render: (item) => columnsRender(formatDecimalPoint(item) + ""),
    },
    {
      title: "索引别名",
      dataIndex: "aliases",
      width: 120,
      render: (item) => columnsRender(item?.join()),
    },
    {
      title: "操作",
      dataIndex: "operation",
      filterTitle: true,
      fixed: "right",
      width: 250,
      render: (index: number, record) => {
        const btns = getBtnList(record, setModalId, setDrawerId, reloadDataFn);
        return renderOperationBtns(btns, record);
      },
    },
  ];
};

export enum TAB_LIST_KEY {
  baseInfo = "baseInfo",
  mapping = "mapping",
  setting = "setting",
  shard = "shard",
  record = "record",
}

const baseInfo = [
  {
    label: "Shard个数",
    key: "pri",
  },
  {
    label: "副本个数",
    key: "rep",
  },
  {
    label: "文档个数",
    key: "docsCount",
  },
  {
    label: "删除文档个数",
    key: "docsDeleted",
  },
  {
    label: "主分片存储大小",
    key: "priStoreSize",
    render: (text: string) => formatDecimalPoint(text) + "",
  },
  {
    label: "存储大小",
    key: "storeSize",
    render: (text: string) => formatDecimalPoint(text) + "",
  },
  {
    key: "priorityLevel",
    label: "恢复优先级",
    render: (value: number) => renderPriority(value),
  },
];

export const TAB_LIST = [
  {
    name: "基本信息",
    key: TAB_LIST_KEY.baseInfo,
    content: (data) => (
      <div className="base-info">
        <BaseDetail columns={baseInfo} baseDetail={data?.indexBaseInfo || {}} />
      </div>
    ),
  },
  {
    name: "Mapping",
    key: TAB_LIST_KEY.mapping,
    content: (data) => <Mapping data={data} />,
  },
  {
    name: "Setting",
    key: TAB_LIST_KEY.setting,
    content: (data) => <Setting data={data} />,
  },
  {
    name: "Shard",
    key: TAB_LIST_KEY.shard,
    content: (data) => <ShardList dataInfo={data} />,
  },
  {
    name: "操作记录",
    key: TAB_LIST_KEY.record,
    content: (data) => <OperatingRecord recordType="indexName" />,
  },
];

const menuMap = new Map();

TAB_LIST.forEach((d) => {
  return menuMap.set(d.key, d);
});

export const MENU_MAP = menuMap;
