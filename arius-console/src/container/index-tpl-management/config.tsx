import { QuestionCircleOutlined } from "@ant-design/icons";
import { IColumnsType } from "component/dantd/query-form/QueryForm";
import { ITemplateLogic } from "@types/cluster/physics-type";
import { IIndex, IOpTemplateIndex } from "@types/index-types";
import { delTemplatePhysical, updateAppAuth, checkEditMapping } from "api/cluster-index-api";
import { Modal, notification, Tooltip } from "antd";
import { ITableBtn } from "component/dantd/dtable";
import { FormItemType, IFormItem } from "component/x-form";
import {
  authStatusMap,
  INDEX_AUTH_TYPE_ARR,
  INDEX_AUTH_TYPE_MAP,
  INDEX_DATA_TYPE_MAP,
  opTemplateIndexRoleMap,
} from "constants/status-map";
import { cellStyle } from "constants/table";
import { timeFormat } from "constants/time";
import { NavRouterLink, renderOperationBtns } from "container/custom-component";
import { ILogicIndex } from "interface/cluster-index";
import moment from "moment";
import React from "react";
import { getOptions } from "container/cluster/config";
import { isOpenUp } from "constants/common";

const { confirm } = Modal;

export const getQueryFormConfig = () => {
  return [
    {
      dataIndex: "authType",
      title: "项目权限",
      type: FormItemType.select,
      options: Object.keys(INDEX_AUTH_TYPE_MAP).map((item) => {
        return {
          title: INDEX_AUTH_TYPE_MAP[item],
          value: item,
        };
      }),
      placeholder: "请选择",
    },
    {
      dataIndex: "dataType",
      title: "模板类型",
      type: FormItemType.select,
      options: Object.keys(INDEX_DATA_TYPE_MAP).map((item) => {
        return {
          title: INDEX_DATA_TYPE_MAP[item],
          value: item,
        };
      }),
      placeholder: "请选择",
    },
    {
      dataIndex: "name",
      title: "模版名称",
      type: "input",
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
  ] as IColumnsType[];
};

export const getLogicIndexColumns = (
  data: ITemplateLogic[],
  setModalId: any,
  reloadData: any,
  clusterId?: number,
  appId?: number,
  pushHistory?: (url: string) => void,
) => {
  return [
    {
      title: "模板ID",
      dataIndex: "id",
      key: "id",
    },
    {
      title: "模板名称",
      dataIndex: "name",
      key: "name",
      onCell: () => ({
        style: cellStyle,
        width: 100,
      }),
      render: (text: string, record: IIndex) => {
        const href = `/index/logic/detail?id=${record.id}&authType=${record.authType}`;
        return (
          <div className="intro-step-8">
            <NavRouterLink needToolTip={true} element={text} href={href} />
          </div>
        );
      },
    },
    {
      title: "模板类型",
      dataIndex: "dataType",
      key: "dataType",
      // filters: tableFilter<ILogicIndex>(data, 'dataType', INDEX_DATA_TYPE_MAP),
      // onFilter: (text: number, record: ILogicIndex) => record.dataType === text,
      render: (val: number) => <>{INDEX_DATA_TYPE_MAP[val] || "未知"}</>,
    },
    {
      title: "项目权限",
      dataIndex: "authType",
      key: "authType",
      render: (val: number) => <>{INDEX_AUTH_TYPE_MAP[val] || "无权限"}</>,
    },
    {
      title: "所属集群",
      dataIndex: "clusterPhies",
      key: "clusterPhies",
      render: (text) => {
        return <>{text?.join(',')}</>;
      },
    },
    {
      title: "模板描述",
      dataIndex: "desc",
      onCell: () => ({
        style: { ...cellStyle, width: 100 },
      }),
      key: "desc",
      render: (text: string) => {
        return (
          <Tooltip placement="bottomLeft" title={text}>
            {text || '-'}
          </Tooltip>
        );
      },
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
      render: (time: number) => moment(time).format(timeFormat),
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      render: (text: number, record: ITemplateLogic) => {
        const btns = getBtnLogicIndexList(
          record,
          clusterId,
          setModalId,
          reloadData,
          appId,
          pushHistory,
        );
        return renderOperationBtns(btns, record);
      },
    },
  ];
};

export const getBtnLogicIndexList = (
  record: ITemplateLogic,
  clusterId: number,
  setModalId,
  reloadData,
  appId: number,
  pushHistory: (url: string) => void
): ITableBtn[] => {

  // 是否为系统数据
  const isSystemData = record.dataType === 0;
  const currentIsOpenUp = isSystemData && isOpenUp;

  let authBtns: any = [
    {
      label: "申请权限",
      type: "primary",
      isOpenUp: currentIsOpenUp,
      clickFunc: (record: ITemplateLogic) => {
        setModalId("logicApplyAuth", record, reloadData);
      },
    },
  ];

  if (record.authType === 2) {
    authBtns = [
      {
        label: "降级权限",
        type: "primary",
        isOpenUp: currentIsOpenUp,
        clickFunc: (record: ITemplateLogic) => {
          confirm({
            title: "降级权限",
            icon: <QuestionCircleOutlined style={{ color: "red" }} />,
            content: "是否权限确认下降为 “读” ？",
            width: 500,
            okText: "确认",
            cancelText: "取消",
            onOk() {
              const obj = {
                appId: appId,
                templateId: record.id,
                type: INDEX_AUTH_TYPE_ARR[3],
              };
              authChange(obj, reloadData);
            },
          });
          return;
        },
      },
      {
        label: "取消权限",
        type: "primary",
        isOpenUp: currentIsOpenUp,
        clickFunc: (record: ITemplateLogic) => {
          confirm({
            title: "取消权限",
            icon: <QuestionCircleOutlined style={{ color: "red" }} />,
            content: "是否取消权限 ？",
            width: 500,
            okText: "确认",
            cancelText: "取消",
            onOk() {
              const obj = {
                appId: appId,
                templateId: record.id,
                type: INDEX_AUTH_TYPE_ARR[0],
              };
              authChange(obj, reloadData);
            },
          });
          return;
        },
      },
    ];
  }

  if (record.authType === 3) {
    authBtns = [
      {
        label: "升级权限",
        type: "primary",
        isOpenUp: currentIsOpenUp,
        clickFunc: (record: ITemplateLogic) => {
          setModalId("logicApplyAuth", record, reloadData);
          return;
        },
      },
      {
        label: "取消权限",
        type: "primary",
        isOpenUp: currentIsOpenUp,
        clickFunc: (record: ITemplateLogic) => {
          confirm({
            title: "取消权限",
            icon: <QuestionCircleOutlined style={{ color: "red" }} />,
            content: "是否取消权限 ？",
            width: 500,
            okText: "确认",
            cancelText: "取消",
            onOk() {
              const obj = {
                appId: appId,
                templateId: record.id,
                type: INDEX_AUTH_TYPE_ARR[0],
              };
              authChange(obj, reloadData);
            },
          });
          return;
        },
      },
    ];
  }
  const href = `/index/modify?id=${record.id}`;
  const clearHref = `/index/clear?id=${record.id}`;

  const btns: any[] = [
    {
      label: currentIsOpenUp ? "编辑" : <NavRouterLink element={"编辑"} href={href} />,
      isOpenUp: currentIsOpenUp,
      isRouterNav: true,
    },
    // {
    //   label: '扩缩容',
    //   clickFunc: () => {
    //     setModalId('expandShrinkIndex', record.id, reloadData);
    //   },
    // },
    {
      label: "编辑Mapping",
      isOpenUp: currentIsOpenUp,
      clickFunc: () => {
        checkEditMapping(record.id)
          .then((res) => {
            if (res.code !== 0 && res.code !== 200) {
              Modal.confirm({
                title: "提示",
                content: res.message || '',
                okText: "确认",
                cancelText: "取消",
                onOk: () => {},
              });
            } else {
              Modal.confirm({
                title: "提示",
                content:
                  "Mapping修改，仅对修改后写入的数据生效，修改前写入的历史数据不会生效。是否确认修改？",
                okText: "确认",
                cancelText: "取消",
                onOk: () => {
                  const href = `/index/modify/mapping?id=${
                    record.id
                  }&history=${'/index-tpl-management'}`;
                  pushHistory(href);
                  // window.location.href = href;
                },
              });
            }
          })
      },
    },
    {
      label: "升版本",
      isOpenUp: currentIsOpenUp,
      clickFunc: () => {
        setModalId("phyUpgradeIndex", record.id, reloadData);
      },
    },
    {
      label: "配置",
      isOpenUp: currentIsOpenUp,
      clickFunc: () => {
        setModalId("phyModifyIndex", record.id, reloadData);
      },
    },
    {
      label: "转让",
      isOpenUp: currentIsOpenUp,
      clickFunc: () => {
        setModalId("transClusterIndex", record, reloadData);
      },
    },
    {
      isRouterNav: true,
      isOpenUp: currentIsOpenUp,
      label: currentIsOpenUp ? "清理" : <NavRouterLink element={"清理"} href={clearHref} />,
    },
    {
      label: "下线",
      isOpenUp: currentIsOpenUp,
      clickFunc: () => {
        setModalId("clearClusterIndex", record.id, reloadData);
      },
    },
  ];

  if (record.authType === 1) {
    return btns;
  }

  return authBtns;
};

const authChange = (req, reloadData) => {
  updateAppAuth(req).then((res) => {
    notification.success({ message: "操作成功！" });
    reloadData();
  });
};
