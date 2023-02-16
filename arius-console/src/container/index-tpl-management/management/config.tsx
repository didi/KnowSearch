import { IColumnsType } from "component/dantd/query-form/QueryForm";
import { ITemplateLogic } from "typesPath/cluster/physics-type";
import { IIndex } from "typesPath/index-types";
import { deleteIndexInfo } from "api/cluster-index-api";
import { Modal, Tooltip, message } from "antd";
import { ITableBtn } from "component/dantd/dtable";
import { FormItemType } from "component/x-form";
import { regNonnegativeInteger } from "constants/reg";
import { NavRouterLink, renderOperationBtns } from "container/custom-component";
import React from "react";
import { isOpenUp, LEVEL_MAP } from "constants/common";
import { transTimeFormat } from "lib/utils";
import { TempletPermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import { XNotification } from "component/x-notification";
import { QuestionCircleOutlined } from "@ant-design/icons";
import { IWorkOrder } from "@types/params-types";
import { submitWorkOrder } from "api/common-api";
import store from "store";

const appInfo = {
  app: store.getState().app.appInfo,
  user: store.getState().user.getName,
};

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
        {item || (typeof item === "number" ? item : "-")}
      </div>
    </Tooltip>
  );
};

export const getQueryFormConfig = (cluster: any, dataTypeList = []) => {
  return [
    {
      dataIndex: "id",
      title: "模板ID",
      type: "input",
      placeholder: "请输入模板ID",
      componentProps: {
        autoComplete: "off",
      },
      rules: [
        {
          required: false,
          validator: (rule: any, value: string) => {
            if (value && !new RegExp(regNonnegativeInteger).test(value)) {
              return Promise.reject("请输入数字");
            }
            if (value?.length > 16) {
              return Promise.reject(new Error("请输入正确ID，0-16位字符"));
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      dataIndex: "name",
      title: "模板名称",
      type: "input",
      placeholder: "请输入模板名称",
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
      dataIndex: "dataType",
      title: "模板类型",
      type: FormItemType.select,
      options: dataTypeList,
      placeholder: "请选择",
    },
    {
      dataIndex: "cluster",
      title: "所属集群",
      type: FormItemType.select,
      options: cluster.map((item) => ({
        title: item.v2,
        value: item.v1,
      })),
      placeholder: "请选择",
    },
    {
      dataIndex: "desc",
      title: "模板描述",
      type: "input",
      placeholder: "请输入模板描述",
      componentProps: {
        autoComplete: "off",
      },
    },
  ] as IColumnsType[];
};

export const getLogicIndexColumns = (
  dataTypeList = [],
  setDrawerId: any,
  reloadData: any,
  pushHistory?: (url: string) => void,
  history?: any
) => {
  return [
    {
      title: "模板ID",
      dataIndex: "id",
      width: 80,
      fixed: "left",
    },
    {
      title: "模板名称",
      dataIndex: "name",
      width: 180,
      lineClampOne: true,
      render: (text: string, record: IIndex) => {
        const href = `/index-tpl/management/detail?index=${text}&id=${record.id}&authType=${record.authType}`;
        return <NavRouterLink needToolTip={true} element={text} href={href} />;
      },
    },
    {
      title: "业务等级",
      dataIndex: "level",
      width: 120,
      sorter: true,
      render: (text) => LEVEL_MAP[Number(text) - 1]?.label || "-",
    },
    {
      title: "模板类型",
      dataIndex: "dataType",
      width: 140,
      render: (val: number) => dataTypeList.find((item) => item.value === val)?.label || "未知",
    },
    {
      title: "所属集群",
      dataIndex: "cluster",
      width: 180,
      render: (item) => columnsRender(item),
    },
    {
      title: "模板描述",
      dataIndex: "desc",
      width: 160,
      render: (item) => columnsRender(item),
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      width: 160,
      render: (time) => transTimeFormat(time),
    },
    {
      title: "读",
      width: 60,
      dataIndex: "blockRead",
      sorter: true,
      render: (item) => (item ? "禁用" : "启用"),
    },
    {
      title: "写",
      width: 60,
      dataIndex: "blockWrite",
      sorter: true,
      render: (item) => (item ? "禁用" : "启用"),
    },
    {
      title: "操作",
      dataIndex: "operation",
      filterTitle: true,
      width: 220,
      fixed: "right",
      render: (text: number, record: ITemplateLogic) => {
        const btns = getBtnLogicIndexList(record, setDrawerId, reloadData, pushHistory, history, dataTypeList);
        return renderOperationBtns(btns, record);
      },
    },
  ];
};

export const getBtnLogicIndexList = (
  record: ITemplateLogic,
  setDrawerId,
  reloadData,
  pushHistory: (url: string) => void,
  history,
  dataTypeList
): ITableBtn[] => {
  // 是否为系统数据
  const isSystemData = record.dataType === 0;
  const currentIsOpenUp = isSystemData && isOpenUp;

  const btns: any[] = [
    {
      label: "编辑",
      isOpenUp: currentIsOpenUp,
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
      invisible: !hasOpPermission(TempletPermissions.PAGE, TempletPermissions.EDIT),
      clickFunc: () => {
        setDrawerId("editTemplate", { record, dataTypeList }, reloadData);
      },
    },
    {
      label: "编辑Mapping",
      isOpenUp: currentIsOpenUp || !record.isPartition,
      tip: currentIsOpenUp ? "预置系统数据，不支持操作" : !record.isPartition ? "非分区模板禁止编辑Mapping" : "",
      invisible: !hasOpPermission(TempletPermissions.PAGE, TempletPermissions.EDIT_MAPPING),
      clickFunc: () => {
        const href = `/index-tpl/management/modify/mapping?id=${record.id}&history=${"/index-tpl/management"}`;
        pushHistory(href);
      },
    },
    {
      label: "编辑Setting",
      isOpenUp: currentIsOpenUp,
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
      invisible: !hasOpPermission(TempletPermissions.PAGE, TempletPermissions.EDIT_SETTING),
      clickFunc: () => {
        const href = `/index-tpl/management/modify/setting?id=${record.id}&history=${"/index-tpl/management"}`;
        pushHistory(href);
      },
    },
    {
      label: !record.blockRead ? "禁用读" : "启用读",
      isOpenUp: currentIsOpenUp,
      clickFunc: () => {
        Modal.confirm({
          icon: <QuestionCircleOutlined />,
          content: `确定${record.blockRead ? "启用" : "禁用"}模版 ${record.name} 的读操作？`,
          okText: "确认",
          cancelText: "取消",
          onOk: async () => {
            const contentObj = {
              templateId: record.id,
              name: record.name,
              status: Number(!record.blockRead),
              operator: appInfo.user("userName") || "",
              projectId: appInfo.app()?.id,
            };
            const params: IWorkOrder = {
              contentObj,
              submitorProjectId: appInfo.app()?.id,
              submitor: appInfo.user("userName") || "",
              description: "",
              type: "templateLogicBlockRead",
            };
            return submitWorkOrder(params, history, reloadData);
          },
        });
      },
    },
    {
      label: !record.blockWrite ? "禁用写" : "启用写",
      isOpenUp: currentIsOpenUp,
      clickFunc: () => {
        Modal.confirm({
          icon: <QuestionCircleOutlined />,
          content: `确定${record.blockWrite ? "启用" : "禁用"}模版 ${record.name} 的写操作？`,
          okText: "确认",
          cancelText: "取消",
          onOk: async () => {
            const contentObj = {
              templateId: record.id,
              name: record.name,
              status: Number(!record.blockWrite),
              operator: appInfo.user("userName") || "",
              projectId: appInfo.app()?.id,
            };
            const params: IWorkOrder = {
              contentObj,
              submitorProjectId: appInfo.app()?.id,
              submitor: appInfo.user("userName") || "",
              description: "",
              type: "templateLogicBlockWrite",
            };
            return submitWorkOrder(params, history, reloadData);
          },
        });
      },
    },
    {
      label: "下线",
      isOpenUp: currentIsOpenUp,
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
      invisible: !hasOpPermission(TempletPermissions.PAGE, TempletPermissions.OFFLINE),
      clickFunc: () => {
        Modal.confirm({
          title: "提示",
          content: `索引模板 ${record.name}（${record.id}）下线后数据无法恢复，请确认影响后继续`,
          width: 500,
          okText: "确定",
          cancelText: "取消",
          onOk() {
            deleteIndexInfo(record.id).then((res) => {
              XNotification({ type: "success", message: `下线模板${record.name}（${record.id}）成功` });
              reloadData();
            });
          },
        });
      },
    },
  ];

  return btns;
};
