import { QuestionCircleOutlined } from "@ant-design/icons";
import { message, Modal, Tag, Tooltip } from "antd";
import { openOrCloseReadOrWrite, indicesOpen, indicesClose } from "api/index-admin";
import { BaseDetail } from "component/dantd/base-detail";
import { IMenuItem } from "component/hash-menu";
import { renderOperationBtns, NavRouterLink } from "container/custom-component";
import React from "react";

import { Mapping, Setting } from "./component";

export const renderText = (text) => {
  return (
    <div className="dsl-overflow-auto">
      {text}
    </div>
  )
}

const columnsRender = (item: string, width: string = "5vw") => {
  return <Tooltip placement="right" title={renderText(item)}>
    <div className="row-ellipsis  pointer" style={{
      maxWidth: width,
      display: 'inline-block'
    }}>
      {item}
    </div>
  </Tooltip>;
};

export const queryFormText: { searchText: string, resetText: string } = {
  searchText: '查询',
  resetText: '重置'
};

export const formColumns = [
  {
    type: 'input',
    title: '索引名称',
    dataIndex: 'index',
    placeholder: "请输入",
    rules: [
      {
        required: false,
        validator: async (rule: any, value: string) => {
          if (value && value.length > 128) {
            return Promise.reject('最大限制128字符');
          }
          return Promise.resolve();
        },
      },
    ],
  },
  {
    type: 'select',
    title: '所属集群',
    dataIndex: 'clusterPhyName',
    options: [],
    placeholder: "请选择",
  },
  {
    type: 'select',
    title: '健康状态',
    dataIndex: 'health',
    options: [
      {
        title: 'green',
        value: 'green',
      },
      {
        title: 'yellow',
        value: 'yellow',
      },
      {
        title: 'red',
        value: 'red',
      },
    ],
    placeholder: "请选择",
  },
];

export const getBtnList = (
  record,
  setModalId?: any,
  setDrawerId?: any,
  reloadDataFn?: (del?: boolean) => void
) => {
  const isOpenUp = false;
  const isRead = !record.readFlag; //  record.readFlag 为 true 禁用，false 启用
  const isWrite = !record.writeFlag;
  const clusterName = record.cluster;
  const indexName = record.index;

  const clickReadOrWrite = (props: {
    clusterName: string,
    indexName: string,
    type: 'write' | 'read',
    value: boolean
  }) => {
    const { clusterName, indexName, type, value } = props;
    let info = '读';

    if (type === 'write') {
      info = '写'
    }

    Modal.confirm({
      icon: <QuestionCircleOutlined />,
      content: `确定${value ? "禁用" : "启用"}索引 ${indexName} 的${info}操作？`,
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          const res = await openOrCloseReadOrWrite([
            {
              clusterPhyName: clusterName,
              index: indexName,
              type: type,
              value: value
            }
          ]);
          res ? message.success(`${value ? "禁用" : "启用"}${info}成功`) : message.error(`${value ? "禁用" : "启用"}${info}失败`);
        } catch (error) {
          message.error(`${value ? "禁用" : "启用"}${info}失败`);
        } finally {
          reloadDataFn && reloadDataFn();
        }
      }
    });
  }

  const clickOpenOrClose = (props: {
    clusterName: string,
    indexName: string,
    type: 'open' | 'close' | any,
  }) => {
    const { clusterName, indexName, type } = props;

    Modal.confirm({
      icon: <QuestionCircleOutlined />,
      content: `确定${type == 'open' ? "关闭" : "开启"}索引 ${indexName} 吗？`,
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          const params = [{
            clusterPhyName: clusterName,
            index: indexName,
            // type: type,
          }];
          const res = type == 'open' ? await indicesClose(params) : await indicesOpen(params);
          res ? message.success(`${type == 'open' ? "关闭" : "开启"}成功`) : message.error(`${type == 'open' ? "关闭" : "开启"}失败`);
          reloadDataFn && reloadDataFn();
        } catch (error) {
          message.error(`${type == 'open' ? "关闭" : "开启"}失败`);
        }
      }
    });
  }

  let btn = [
    {
      label: isRead ? "禁用读" : "启用读",
      type: "primary",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        clickReadOrWrite({
          clusterName: clusterName,
          indexName: indexName,
          type: 'read',
          value: isRead
        })
      },
    },
    {
      label: isWrite ? "禁用写" : "启用写",
      type: "primary",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        clickReadOrWrite({
          clusterName: clusterName,
          indexName: indexName,
          type: 'write',
          value: isWrite
        })
      },
    },
    {
      label: record.status == 'open' ? "关闭索引" : "开启索引",
      type: "primary",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        clickOpenOrClose({
          clusterName: clusterName,
          indexName: indexName,
          type: record.status,
        })
      },
    },
    {
      label: "删除",
      type: "primary",
      clickFunc: () => {
        setModalId("deleteIndex", {
          delList: [
            {
              "clusterPhyName": clusterName,
              "index": indexName
            }
          ],
          title: `确定删除索引 ${indexName} ?`
        }, () => {
          reloadDataFn && reloadDataFn(true);
        });
      },
    },
  ];
  return btn;
};

const statusText = (item) => {
  let status = "启用"
  if (item) {
    status = "禁用"
  }
  return <div style={{ width: '2vw' }}>{status}</div>;
}

const statusTag = (item) => {
  switch (item) {
    case 'red':
      return <Tag color="error">red</Tag>
    case 'yellow':
      return <Tag color="warning">yellow</Tag>
    case "green":
      return <Tag color="success">green</Tag>
  }
}

export const cherryList = [
  'rep',
  'docsCount',
  'docsDeleted',
  'priStoreSize',
  'storeSize',
]

export const getColumns = (
  setModalId?: any,
  setDrawerId?: any,
  reloadDataFn?: any
) => {
  return [
    {
      title: "索引名称",
      dataIndex: "index",
      sorter: true,
      fixed: "left",
      width: 180,
      render: (item, record) => {
        return <div
          className="two-row-ellipsis pointer"
          style={{
            color: "#526ecc",
            // width: "8vw"
          }}>
            <NavRouterLink 
              needToolTip
              element={item}
              href={`/index-admin/detail?index=${record.index}&cluster=${record.cluster}`}
            />
        </div>
      },
    },
    {
      title: "所属集群",
      dataIndex: "cluster",
      render: (item) => columnsRender(item),
    },
    {
      title: "健康状态",
      dataIndex: "health",
      render: (item) => <div style={{ width: "2vw" }}>{statusTag(item)}</div>,
    },
    {
      title: "索引状态",
      dataIndex: "status",
      render: (text) => {
        return text == 'open' ? '开启' : '关闭'
      },
    },
    {
      title: "Shard个数",
      dataIndex: "pri",
      sorter: true,
      render: (item, record) => <div
        className="two-row-ellipsis pointer"
        onClick={() => { setDrawerId('shardList', record, reloadDataFn) }}
        style={{
          color: "#526ecc",
          width: "2vw"
        }}>
        <Tooltip placement="right" title={renderText(item)}>
          {item}
        </Tooltip>
      </div>
    },
    {
      title: "副本个数",
      dataIndex: "rep",
      sorter: true,
      render: (item) => columnsRender(item, '1vw'),
    },
    {
      title: "文档个数",
      dataIndex: "docsCount",
      sorter: true,
      render: (item) => columnsRender(item),
    },
    {
      title: "删除文档个数",
      dataIndex: "docsDeleted",
      sorter: true,
      render: (item) => columnsRender(item),
    },
    {
      title: "主分片存储大小",
      dataIndex: "priStoreSize",
      sorter: true,
      render: (item) => columnsRender(item, '6vw'),
    },
    {
      title: "存储大小",
      dataIndex: "storeSize",
      sorter: true,
      render: (item) => columnsRender(item),
    },
    {
      title: "读",
      width: 70,
      dataIndex: "readFlag",
      render: (item) => statusText(item),
    },
    {
      title: "写",
      width: 70,
      dataIndex: "writeFlag",
      render: (item) => statusText(item),
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: 'operation',
      fixed: 'right',
      width: 180,
      render: (index: number, record) => {
        const btns = getBtnList(
          record,
          setModalId,
          setDrawerId,
          reloadDataFn
        );
        return renderOperationBtns(btns, record);
      },
    },
  ]
};


// detail
export const DESC_LIST = [
  {
    label: "所属集群",
    key: "cluster",
    render: (cluster: string,) => (
      <>
        {cluster}
      </>
    ),
  },
];

export enum TAB_LIST_KEY {
  baseInfo = "baseInfo",
  mapping = "mapping",
  setting = "setting",
}

const baseInfo = [
  [
    {
      label: "Shard个数",
      key: "pri",
    },
    {
      label: "副本个数",
      key: "rep",
    },
  ],
  [
    {
      label: "文档个数",
      key: "docsCount",
    },
    {
      label: "删除文档个数",
      key: "docsDeleted",
    },
  ],
  [    
    {
      label: "主分片存储大小",
      key: "priStoreSize",
    },
    {
      label: "存储大小",
      key: "storeSize",
    }
  ]
]

export const TAB_LIST = [
  {
    name: "基本信息",
    key: TAB_LIST_KEY.baseInfo,
    content: (data) => <BaseDetail
              columns={baseInfo}
              baseDetail={data}
            />,
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
];

const menuMap = new Map<string, IMenuItem>();

TAB_LIST.forEach((d) => {
  return menuMap.set(d.key, d);
});

export const MENU_MAP = menuMap;
