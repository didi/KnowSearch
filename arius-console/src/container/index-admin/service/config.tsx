import { Modal, message, Tooltip } from "antd";
import { NavRouterLink, renderOperationBtns } from "container/custom-component";
import React from "react";
import { columnsRender } from "../management/config";
import { QuestionCircleOutlined } from "@ant-design/icons";
import { openOrCloseReadOrWrite, indicesOpen, indicesClose } from "api/index-admin";
import { CodeType } from "constants/common";
import { renderPriority, renderSwitch } from "container/index-tpl-management/service/config";
import { hasOpPermission } from "lib/permission";
import { IndexServicePermissions } from "constants/permission";

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
  ];
};

const statusText = (item) => {
  let status = item ? "禁用" : "启用";
  return <div style={{ width: "2vw" }}>{status}</div>;
};

export const getBtnList = (record, setModalId?: any, setDrawerId?: any, reloadDataFn?: (del?: boolean) => void) => {
  const isOpenUp = false;
  const isRead = !record.readFlag; //  record.readFlag 为 true 禁用，false 启用
  const isWrite = !record.writeFlag;
  const clusterName = record.cluster;
  const indexName = record.index;

  const clickReadOrWrite = (props: { clusterName: string; indexName: string; type: "write" | "read"; value: boolean }) => {
    const { clusterName, indexName, type, value } = props;
    const info = type === "write" ? "写" : "读";
    const operate = value ? "禁用" : "启用";

    Modal.confirm({
      title: "提示",
      icon: <QuestionCircleOutlined />,
      content: `确定${operate}索引 ${indexName} 的${info}操作？`,
      okText: "确认",
      cancelText: "取消",
      onOk: async () => {
        try {
          const res = await openOrCloseReadOrWrite([
            {
              cluster: clusterName,
              index: indexName,
              type: type,
              value: value,
            },
          ]);
          res ? message.success(`${operate}${info}成功`) : message.error(`${operate}${info}失败`);
          reloadDataFn && reloadDataFn();
        } catch (error) {
          message.error(`${operate}${info}失败`);
        }
      },
    });
  };

  const clickOpenOrClose = (props: { clusterName: string; indexName: string; type: "open" | "close" | any }) => {
    const { clusterName, indexName, type } = props;
    const operate = type == "open" ? "关闭" : "开启";

    Modal.confirm({
      title: "提示",
      icon: <QuestionCircleOutlined />,
      content: `确定${operate}索引 ${indexName} 吗？`,
      okText: "确认",
      cancelText: "取消",
      onOk: async () => {
        try {
          const params = [
            {
              cluster: clusterName,
              index: indexName,
            },
          ];
          const res = type == "open" ? await indicesClose(params) : await indicesOpen(params);
          res ? message.success(`${operate}成功`) : message.error(`${operate}失败`);
          reloadDataFn && reloadDataFn();
        } catch (error) {
          message.error(`${operate}失败`);
        }
      },
    });
  };

  let btn = [
    hasOpPermission(IndexServicePermissions.PAGE, IndexServicePermissions.DISABLE_READ) && {
      label: isRead ? "禁用读" : "启用读",
      type: "primary",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        clickReadOrWrite({
          clusterName: clusterName,
          indexName: indexName,
          type: "read",
          value: isRead,
        });
      },
    },
    hasOpPermission(IndexServicePermissions.PAGE, IndexServicePermissions.DISABLE_WRITE) && {
      label: isWrite ? "禁用写" : "启用写",
      type: "primary",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        clickReadOrWrite({
          clusterName: clusterName,
          indexName: indexName,
          type: "write",
          value: isWrite,
        });
      },
    },
    hasOpPermission(IndexServicePermissions.PAGE, IndexServicePermissions.CLOSE_INDEX) && {
      label: record.status == "open" ? "关闭索引" : "开启索引",
      type: "primary",
      isOpenUp: isOpenUp,
      clickFunc: () => {
        clickOpenOrClose({
          clusterName: clusterName,
          indexName: indexName,
          type: record.status,
        });
      },
    },
  ].filter(Boolean);
  return btn;
};

export const getColumns = (setModalId?: any, setDrawerId?: any, reloadDataFn?: any) => {
  return [
    {
      title: "索引名称",
      dataIndex: "index",
      fixed: "left",
      width: 180,
      filters: [
        {
          text: "展示元数据集群索引",
          value: true,
        },
      ],
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
    // {
    //   title: "是否关联模板",
    //   dataIndex: "templateId",
    //   width: 100,
    //   render: (item) => (item ? "是" : "否"),
    // },
    {
      title: "所属集群",
      dataIndex: "cluster",
      width: 150,
      render: (item) => columnsRender(item),
    },
    {
      title: "Shard数",
      dataIndex: "pri",
      width: 120,
      sorter: true,
    },
    {
      title: "Segment数",
      dataIndex: "totalSegmentCount",
      width: 120,
      sorter: true,
    },
    {
      title: "读",
      width: 60,
      dataIndex: "readFlag",
      render: (item) => statusText(item),
    },
    {
      title: "写",
      width: 60,
      dataIndex: "writeFlag",
      render: (item) => statusText(item),
    },
    hasOpPermission(IndexServicePermissions.PAGE, IndexServicePermissions.ROLLOVER) && {
      title: "Rollover",
      dataIndex: "rollover",
      width: 80,
      render: (item, record) =>
        record.templateId ? (
          <Tooltip title="该索引具备模板，无法进行索引Rollover">
            <a style={{ color: "#bfbfbf" }}>执行</a>
          </Tooltip>
        ) : (
          <a
            onClick={() => {
              if (record.aliases?.length) {
                setDrawerId("indexSrvRollover", [record], reloadDataFn);
              } else {
                Modal.confirm({
                  title: "提示",
                  content: (
                    <>
                      <div style={{ marginBottom: "10px" }}>以下索引未设置别名，无法进行Rollover操作！</div>
                      <div className="btn-labels-box">
                        <div className="btn-labels">{record.index}</div>
                      </div>
                    </>
                  ),
                  okText: "确认",
                  cancelText: "取消",
                });
              }
            }}
          >
            执行
          </a>
        ),
    },
    hasOpPermission(IndexServicePermissions.PAGE, IndexServicePermissions.SHRINK) && {
      title: "Shrink",
      dataIndex: "shrink",
      width: 70,
      render: (item, record) =>
        record.templateId ? (
          <Tooltip title="该索引具备模板，无法进行索引shrink">
            <a style={{ color: "#bfbfbf" }}>执行</a>
          </Tooltip>
        ) : (
          <a onClick={() => setDrawerId("indexSrvShrinkSplit", { type: "shrink", indexs: record }, reloadDataFn)}>执行</a>
        ),
    },
    hasOpPermission(IndexServicePermissions.PAGE, IndexServicePermissions.SPLIT) && {
      title: "Split",
      dataIndex: "split",
      width: 70,
      render: (item, record) =>
        record.templateId ? (
          <Tooltip title="该索引具备模板，无法进行索引split">
            <a style={{ color: "#bfbfbf" }}>执行</a>
          </Tooltip>
        ) : (
          <a onClick={() => setDrawerId("indexSrvShrinkSplit", { type: "split", indexs: record }, reloadDataFn)}>执行</a>
        ),
    },
    hasOpPermission(IndexServicePermissions.PAGE, IndexServicePermissions.ForceMerge) && {
      title: "ForceMerge",
      dataIndex: "ForceMerge",
      width: 100,
      render: (item, record) => <a onClick={() => setDrawerId("indexSrvForceMerge", [record], reloadDataFn)}>执行</a>,
    },
    {
      title: "异步Translog",
      dataIndex: "translogAsync",
      key: "translogAsync",
      width: 120,
      render: (text, record) =>
        renderSwitch({ code: CodeType.Translog, from: "index", value: text, record, reloadData: reloadDataFn, setModalId }),
    },
    {
      title: "恢复优先级",
      dataIndex: "priorityLevel",
      key: "priorityLevel",
      width: 110,
      render: (text) => renderPriority(text),
    },
    {
      title: "操作",
      dataIndex: "operation",
      filterTitle: true,
      fixed: "right",
      width: 200,
      render: (index: number, record) => {
        const btns = getBtnList(record, setModalId, setDrawerId, reloadDataFn);
        return renderOperationBtns(btns, record);
      },
    },
  ].filter(Boolean);
};

export const getBatchBtnService = (setModalId, setDrawerId, selectedRows, reloadData) => {
  return [
    hasOpPermission(IndexServicePermissions.PAGE, IndexServicePermissions.ROLLOVER) && {
      label: "Rollover",
      onClick: () => setDrawerId("indexSrvRollover", selectedRows, reloadData),
    },
    hasOpPermission(IndexServicePermissions.PAGE, IndexServicePermissions.ForceMerge) && {
      label: "ForceMerge",
      onClick: () => setDrawerId("indexSrvForceMerge", selectedRows, reloadData),
    },
    {
      label: "异步Translog",
      onClick: () => setModalId("batchUpdate", { code: CodeType.Translog, datas: selectedRows, type: "index" }, reloadData),
    },
    {
      label: "恢复优先级",
      onClick: () => setModalId("batchUpdate", { code: CodeType.Priority, datas: selectedRows, type: "index" }, reloadData),
    },
    hasOpPermission(IndexServicePermissions.PAGE, IndexServicePermissions.DISABLE_READ) && {
      label: "启用/禁用读",
      onClick: () => setModalId("batchExecute", { type: "read", datas: selectedRows }, reloadData),
    },
    hasOpPermission(IndexServicePermissions.PAGE, IndexServicePermissions.DISABLE_WRITE) && {
      label: "启用/禁用写",
      onClick: () => setModalId("batchExecute", { type: "write", datas: selectedRows }, reloadData),
    },
    hasOpPermission(IndexServicePermissions.PAGE, IndexServicePermissions.CLOSE_INDEX) && {
      label: "开启/关闭索引",
      onClick: () => setModalId("batchExecute", { type: "status", datas: selectedRows }, reloadData),
    },
  ].filter(Boolean);
};
