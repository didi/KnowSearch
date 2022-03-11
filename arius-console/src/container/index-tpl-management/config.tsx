import { QuestionCircleOutlined } from "@ant-design/icons";
import { IColumnsType } from "component/dantd/query-form/QueryForm";
import { ITemplateLogic } from "typesPath/cluster/physics-type";
import { IIndex } from "typesPath/index-types";
import { rolloverSwitch, updateAppAuth, checkEditMapping, disableRead, disableWrite } from "api/cluster-index-api";
import { message, Modal, notification, Tooltip } from "antd";
import { ITableBtn } from "component/dantd/dtable";
import { FormItemType, IFormItem } from "component/x-form";
import { filtersHasDCDR, INDEX_AUTH_TYPE_ARR, INDEX_AUTH_TYPE_MAP, INDEX_DATA_TYPE_MAP } from "constants/status-map";
import { cellStyle } from "constants/table";
import { timeFormat } from "constants/time";
import { NavRouterLink, renderOperationBtns } from "container/custom-component";
import moment from "moment";
import React from "react";
import { isOpenUp, LEVEL_MAP } from "constants/common";
import { checkEditTemplateSrv } from "api/cluster-api";

const { confirm } = Modal;
export const cherryList = ["authType", "clusterPhies", "hasDCDR", "checkPointDiff", "desc", "createTime"];

export const getQueryFormConfig = (cluster: any) => {
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
              return Promise.reject("最大限制128字符");
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      dataIndex: "clusterPhies",
      title: "集群名称",
      type: FormItemType.select,
      options: cluster.map((item) => {
        return {
          title: item,
          value: item,
        };
      }),
      placeholder: "请选择",
    },
  ] as IColumnsType[];
};

export const getLogicIndexColumns = (
  data: ITemplateLogic[],
  setModalId: any,
  reloadData: any,
  clusterId?: number,
  appId?: number,
  pushHistory?: (url: string) => void
) => {
  return [
    {
      title: "模板ID",
      dataIndex: "id",
      key: "id",
      fixed: "left",
    },
    {
      title: "模板名称",
      dataIndex: "name",
      key: "name",
      width: 150,
      // onCell: () => ({
      //   style: cellStyle,
      // }),
      render: (text: string, record: IIndex) => {
        const href = `/index/logic/detail?id=${record.id}&authType=${record.authType}`;
        return (
          <div className="intro-step-8 two-row-ellipsis pointer">
            <NavRouterLink needToolTip={true} element={text} href={href} />
          </div>
        );
      },
    },
    {
      title: "业务等级",
      dataIndex: "level",
      key: "level",
      sorter: true,
      render: (text) => {
        return LEVEL_MAP[Number(text) - 1]?.label || "-";
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
        return <>{text?.join(",")}</>;
      },
    },
    {
      title: "是否建立DCDR链路",
      dataIndex: "hasDCDR",
      key: "hasDCDR",
      width: 150,
      render: (text) => {
        return <>{text ? "是" : "否"}</>;
      },
      filters: filtersHasDCDR,
    },
    {
      title: "主从位点差",
      dataIndex: "checkPointDiff",
      key: "checkPointDiff",
      render: (text) => {
        return <>{text || typeof text == "number" ? text : "-"}</>;
      },
      sorter: true,
    },
    {
      title: "Rollover",
      dataIndex: "disableIndexRollover",
      key: "disableIndexRollover",
      render: (text) => {
        return <>{text ? "否" : "是"}</>;
      },
    },
    {
      title: "读",
      dataIndex: "blockRead",
      key: "blockRead",
      render: (text) => {
        return <>{!text ? "启用" : "禁用"}</>;
      },
    },
    {
      title: "写",
      dataIndex: "blockWrite",
      key: "blockWrite",
      render: (text) => {
        return <>{!text ? "启用" : "禁用"}</>;
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
            {text || "-"}
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
      width: 215,
      fixed: "right",
      render: (text: number, record: ITemplateLogic) => {
        const btns = getBtnLogicIndexList(record, clusterId, setModalId, reloadData, appId, pushHistory);
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
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
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
        tip: isSystemData ? "预置系统数据，不支持操作" : "",
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
        tip: isSystemData ? "预置系统数据，不支持操作" : "",
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
        tip: isSystemData ? "预置系统数据，不支持操作" : "",
        clickFunc: (record: ITemplateLogic) => {
          setModalId("logicApplyAuth", record, reloadData);
          return;
        },
      },
      {
        label: "取消权限",
        type: "primary",
        isOpenUp: currentIsOpenUp,
        tip: isSystemData ? "预置系统数据，不支持操作" : "",
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
      label: "编辑Mapping",
      isOpenUp: currentIsOpenUp,
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
      clickFunc: () => {
        checkEditMapping(record.id).then((res) => {
          if (res.code !== 0 && res.code !== 200) {
            Modal.confirm({
              title: "提示",
              content: res.message || "",
              okText: "确认",
              cancelText: "取消",
              onOk: () => {},
            });
          } else {
            Modal.confirm({
              title: "提示",
              content: "Mapping修改，仅对修改后写入的数据生效，修改前写入的历史数据不会生效。是否确认修改？",
              okText: "确认",
              cancelText: "取消",
              onOk: () => {
                const href = `/index/modify/mapping?id=${record.id}&history=${"/index-tpl-management"}`;
                pushHistory(href);
                // window.location.href = href;
              },
            });
          }
        });
      },
    },
    {
      label: "编辑Setting",
      isOpenUp: currentIsOpenUp,
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
      clickFunc: () => {
        checkEditTemplateSrv((record as any)?.id, 5).then((res) => {
          if (res.code !== 0 && res.code !== 200) {
            Modal.confirm({
              title: "提示",
              content: res.message || "",
              okText: "确认",
              cancelText: "取消",
              onOk: () => {},
            });
          } else {
            setModalId("editSetting", record.id, reloadData);
          }
        });
      },
    },
    {
      label: `${record.disableIndexRollover ? "开启" : "关闭"}Rollover`,
      isOpenUp: currentIsOpenUp,
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
      clickFunc: () => {
        Modal.confirm({
          title: "提示",
          width: 550,
          content: (
            <>
              <div>确定{record.disableIndexRollover ? "开启" : "关闭"}索引RollOver能力?</div>
              <div>{record.disableIndexRollover ? "开启后会影响索引Update和Delete能力以及指定id 写入，更新，删除" : null}</div>
            </>
          ),
          okText: "确认",
          cancelText: "取消",
          onOk: () => {
            rolloverSwitch(record.id, record.disableIndexRollover ? "1" : "0").then(() => {
              message.success("操作成功");
              reloadData();
            });
          },
        });
      },
    },
    {
      label: "升版本",
      isOpenUp: currentIsOpenUp,
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
      clickFunc: () => {
        setModalId("phyUpgradeIndex", record.id, reloadData);
      },
    },
    {
      label: "配置",
      isOpenUp: currentIsOpenUp,
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
      clickFunc: () => {
        setModalId("phyModifyIndex", record.id, reloadData);
      },
    },
    {
      label: !record.blockRead ? "禁用读" : "启用读",
      isOpenUp: currentIsOpenUp,
      clickFunc: () => {
        disableRead({ blockRead: !record.blockRead, id: record.id }).then(() => {
          message.success("操作成功");
          reloadData();
        });
      },
    },
    {
      label: !record.blockWrite ? "禁用写" : "启用写",
      isOpenUp: currentIsOpenUp,
      clickFunc: () => {
        disableWrite({ blockWrite: !record.blockWrite, id: record.id }).then(() => {
          message.success("操作成功");
          reloadData();
        });
      },
    },
    {
      label: "编辑",
      isOpenUp: currentIsOpenUp,
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
      clickFunc: () => {
        setTimeout(() => {
          pushHistory(href);
        }, 500);
      },
    },
    {
      label: "转让",
      isOpenUp: currentIsOpenUp,
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
      clickFunc: () => {
        setModalId("transClusterIndex", record, reloadData);
      },
    },
    {
      isOpenUp: currentIsOpenUp,
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
      label: "清理",
      clickFunc: () => {
        setTimeout(() => {
          pushHistory(clearHref);
        }, 500);
      },
    },
    {
      label: "下线",
      isOpenUp: currentIsOpenUp,
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
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
