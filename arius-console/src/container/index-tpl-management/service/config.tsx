import { IColumnsType } from "component/dantd/query-form/QueryForm";
import { ITemplateLogic } from "typesPath/cluster/physics-type";
import { IIndex } from "typesPath/index-types";
import { message } from "antd";
import { Modal, Tooltip, Switch, Tag } from "knowdesign";
import { ITableBtn } from "component/dantd/dtable";
import { FormItemType } from "component/x-form";
import { CodeType, isOpenUp, CONFIRM_BUTTON_TEXT } from "constants/common";
import { regNonnegativeInteger } from "constants/reg";
import { NavRouterLink, renderOperationBtns } from "container/custom-component";
import React from "react";
import { disableRead, disableWrite, updateTemplateSrv, updateVision } from "api/cluster-index-api";
import { columnsRender } from "../management/config";
import { XNotification } from "component/x-notification";
import { FilterModal } from "./filterModal";
import { InfoCircleFilled } from "@ant-design/icons";

const statusTag = (item) => {
  switch (item) {
    case 2:
      return <Tag color="error">Red</Tag>;
    case 1:
      return <Tag color="warning">Yellow</Tag>;
    case 0:
      return <Tag color="success">Green</Tag>;
    case -1:
      return <Tag className="ant-tag-dark">Unknown</Tag>;
    default:
      return "-";
  }
};

const healthList = [
  {
    title: "green",
    value: "0",
  },
  {
    title: "yellow",
    value: "1",
  },
  {
    title: "red",
    value: "2",
  },
  {
    title: "unknown",
    value: "-1",
  },
];

export const getQueryFormConfig = (cluster: any, projectList: any) => {
  return [
    {
      dataIndex: "id",
      title: "模板ID:",
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
      title: "模板名称:",
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
      type: "select",
      title: "健康状态:",
      dataIndex: "health",
      options: healthList,
      placeholder: "请选择",
    },
    {
      dataIndex: "projectId",
      title: "所属应用:",
      type: FormItemType.select,
      options: projectList.map((item) => ({
        title: item.projectName,
        value: item.id,
      })),
      placeholder: "请选择",
    },
    {
      dataIndex: "cluster",
      title: "所属集群:",
      type: FormItemType.select,
      options: cluster.map((item) => ({
        title: item,
        value: item,
      })),
      placeholder: "请选择",
    },
  ] as IColumnsType[];
};

export const getServiceColumns = (setModalId: any, setDrawerId: any, reloadData: any, pushHistory?: (url: string) => void) => {
  return [
    {
      title: "模板ID",
      dataIndex: "id",
      key: "id",
      width: 80,
      fixed: "left",
    },
    {
      title: "模板名称",
      dataIndex: "name",
      key: "name",
      width: 180,
      lineClampOne: true,
      render: (text: string, record: IIndex) => {
        const href = `/index-tpl/service/detail?name=${text}&id=${record.id}&authType=${record.authType}`;
        return <NavRouterLink needToolTip={true} element={text} href={href} />;
      },
    },
    {
      title: "健康状态",
      dataIndex: "health",
      key: "health",
      width: 100,
      sorter: true,
      render: (item) => <div>{statusTag(item)}</div>,
    },
    {
      title: "所属应用",
      dataIndex: "projectName",
      key: "projectName",
      width: 150,
      render: (text) => columnsRender(text),
    },
    {
      title: "所属集群",
      dataIndex: "cluster",
      key: "cluster",
      width: 180,
      render: (text) => columnsRender(text?.join(",")),
    },
    {
      title: "预创建",
      dataIndex: "preCreateFlags",
      key: "preCreateFlags",
      width: 80,
      render: (text, record) => renderSwitch(CodeType.PreCreate, record, reloadData, setModalId),
    },
    {
      title: "过期删除",
      dataIndex: "overdueDelete",
      key: "overdueDelete",
      width: 80,
      render: (text, record) => renderSwitch(CodeType.Delete, record, reloadData, setModalId),
    },
    {
      title: "冷热分离",
      dataIndex: "separate",
      key: "separate",
      width: 140,
      render: (text, record) => {
        const expireTime = record.expireTime !== -1 ? `${record.expireTime || "-"}天` : "永久";
        const hotTime =
          record.hotTime !== -1 ? `${record.hotTime || "-"}` : record.expireTime !== -1 ? `${record.expireTime || "-"}` : "永久";
        return (
          <>
            {renderSwitch(CodeType.Separate, record, reloadData, setModalId)}
            <span style={{ marginLeft: "3px" }}>
              {hotTime}/{expireTime}
            </span>
          </>
        );
      },
    },
    {
      title: "Pipeline",
      dataIndex: "pipeline",
      key: "pipeline",
      width: 80,
      render: (text, record) => renderSwitch(CodeType.Pipeline, record, reloadData, setModalId),
    },
    {
      title: "Rollover",
      dataIndex: "rollover",
      key: "rollover",
      width: 80,
      render: (text, record) => renderSwitch(CodeType.Rollover, record, reloadData, setModalId),
    },
    {
      title: "DCDR",
      dataIndex: "hasDCDR",
      key: "hasDCDR",
      filterDropdown: ({ setSelectedKeys, selectedKeys, confirm, clearFilters }) => {
        return <FilterModal setSelectedKeys={setSelectedKeys} selectedKeys={selectedKeys} confirm={confirm} clearFilters={clearFilters} />;
      },
      width: 90,
      render: (text, record) => renderSwitch(CodeType.DCDR, record, reloadData, setModalId, setDrawerId),
    },

    {
      title: "主从位点差",
      dataIndex: "checkPointDiff",
      key: "checkPointDiff",
      width: 110,
      sorter: true,
      render: (text) => {
        return <>{typeof text == "number" ? (text < 0 ? "-" : text) : text || "-"}</>;
      },
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
      key: "operation",
      width: 180,
      fixed: "right",
      render: (text: number, record: ITemplateLogic) => {
        const btns = getBtnServiceList(record, setModalId, setDrawerId, reloadData, pushHistory);
        return renderOperationBtns(btns, record);
      },
    },
  ];
};

const onClick = (code: number, status: boolean, record, reloadData, setModalId, setDrawerId?: any) => {
  let title = null;
  let content = null;
  switch (code) {
    case CodeType.PreCreate:
      title = `确定${status ? "关闭" : "开启"}预创建能力？`;
      break;
    case CodeType.Delete:
      title = `确定${status ? "关闭" : "开启"}过期删除能力？`;
      break;
    case CodeType.Separate:
      title = `确定${status ? "关闭" : "开启"}冷热分离能力？`;
      break;
    case CodeType.Pipeline:
      title = `确定${status ? "关闭" : "开启"}pipeline能力？`;
      break;
    case CodeType.Rollover:
      title = `确定${status ? "关闭" : "开启"}Rollover能力？`;
      if (!status) {
        content = (
          <>
            <div>开启后会影响索引Update和Delete能力以及指定id写入、更新、删除</div>
            <div>默认主分片到达50G后进行升版本，如需修改，请前往【平台配置】修改 index.rollover.threshold 值</div>
          </>
        );
      }
      break;
    default:
      break;
  }
  if (code === CodeType.Separate && !status) {
    setModalId("openSeparate", { id: record.id, expireTime: record.expireTime }, reloadData);
    return;
  }
  if (code === CodeType.DCDR && !status) {
    setModalId("createDCDR", record.id, reloadData);
    return;
  }
  if (code === CodeType.DCDR && status) {
    setDrawerId("dcdrDetail", record.id, reloadData);
    return;
  }
  Modal.confirm({
    ...CONFIRM_BUTTON_TEXT,
    title,
    content,
    icon: <InfoCircleFilled className="confirm-icon" />,
    onOk: () => {
      const params = {
        srvCode: code,
        templateIdList: [record.id],
      };
      updateTemplateSrv(params, status ? "DELETE" : "PUT").then(() => {
        message.success(`操作成功`);
        reloadData();
      });
    },
  });
};

export const renderSwitch = (code: number, record, reloadData, setModalId, setDrawerId?: any) => {
  const unavailableInfo = record?.unavailableSrv?.find((item) => item.srvCode === code);
  const openInfo = record?.openSrv?.find((item) => item.srvCode === code);
  const status = openInfo ? true : false;
  const hasDCDR = record?.hasDCDR;
  if (unavailableInfo) {
    return (
      <Tooltip title={unavailableInfo.unavailableReason}>
        {code === CodeType.DCDR ? <a style={{ color: "#bfbfbf" }}>创建链路</a> : <Switch size="small" disabled={true} checked={false} />}
      </Tooltip>
    );
  } else {
    return code === CodeType.DCDR ? (
      <a onClick={() => onClick(code, hasDCDR, record, reloadData, setModalId, setDrawerId)}>{hasDCDR ? "查看链路" : "创建链路"}</a>
    ) : (
      <Switch size="small" checked={status} onClick={() => onClick(code, status, record, reloadData, setModalId)} />
    );
  }
};

export const getBtnServiceList = (
  record: ITemplateLogic,
  setModalId,
  setDrawerId,
  reloadData,
  pushHistory: (url: string) => void
): ITableBtn[] => {
  // 是否为系统数据
  const isSystemData = record.dataType === 0;
  const currentIsOpenUp = isSystemData && isOpenUp;

  const btns: any[] = [
    {
      label: "清理",
      isOpenUp: currentIsOpenUp,
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
      clickFunc: () => {
        setDrawerId("clearModal", record.id, reloadData);
      },
    },
    {
      label: "扩缩容",
      isOpenUp: currentIsOpenUp,
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
      clickFunc: () => {
        let errorCluster = [];
        let errorDesc;
        if (record?.clusterConnectionStatus) {
          record?.clusterConnectionStatus.forEach((element) => {
            if (element.status === "DISCONNECTED") {
              errorCluster.push(element.cluster);
              errorDesc = element.desc;
            }
          });
          errorCluster.length
            ? XNotification({
                type: "error",
                message: "集群故障",
                description: `${errorCluster.join(",")}集群故障，请检查集群状态后重试`,
                duration: 1000,
              })
            : null;
        }
        if (!errorCluster.length) {
          setModalId("expandShrinkCapacity", record, reloadData);
        }
      },
    },
    {
      label: "升版本",
      isOpenUp: currentIsOpenUp,
      tip: isSystemData ? "预置系统数据，不支持操作" : "",
      clickFunc: () => {
        Modal.confirm({
          ...CONFIRM_BUTTON_TEXT,
          title: "确定进行升版本操作？",
          icon: <InfoCircleFilled className="confirm-icon" />,
          content: (
            <>
              <div>升级后版本号自动+1。</div>
              {record.partition ? null : <div>会影响当前模板指定id的写入、更新、删除。</div>}
            </>
          ),
          onOk: () => {
            updateVision(record.id).then(() => {
              message.success(`升版本成功`);
              reloadData();
            });
          },
        });
      },
    },
    {
      label: !record.blockRead ? "禁用读" : "启用读",
      isOpenUp: currentIsOpenUp,
      clickFunc: () => {
        Modal.confirm({
          ...CONFIRM_BUTTON_TEXT,
          title: `确定${record.blockRead ? "启用" : "禁用"}模版 ${record.name} 的读操作？`,
          icon: <InfoCircleFilled className="confirm-icon" />,
          onOk: async () => {
            disableRead(record.id, Number(!record.blockRead))
              .then(() => {
                message.success("操作成功");
              })
              .catch((error) => {
                message.error(`${record.blockRead ? "启用" : "禁用"}读失败`);
              })
              .finally(() => {
                reloadData();
              });
          },
        });
      },
    },
    {
      label: !record.blockWrite ? "禁用写" : "启用写",
      isOpenUp: currentIsOpenUp,
      clickFunc: () => {
        Modal.confirm({
          ...CONFIRM_BUTTON_TEXT,
          title: `确定${record.blockWrite ? "启用" : "禁用"}模版 ${record.name} 的写操作？`,
          icon: <InfoCircleFilled className="confirm-icon" />,
          onOk: async () => {
            disableWrite(record.id, Number(!record.blockWrite))
              .then(() => {
                message.success("操作成功");
              })
              .catch((error) => {
                message.error(`${record.blockWrite ? "启用" : "禁用"}写失败`);
              })
              .finally(() => {
                reloadData();
              });
          },
        });
      },
    },
  ];

  return btns;
};

export const getBatchBtnService = (setModalId, reloadData, selectedRows, switchDCDR) => {
  return [
    {
      label: "预创建",
      onClick: () => setModalId("batchUpdate", { code: CodeType.PreCreate, datas: selectedRows }, reloadData),
    },
    {
      label: "过期删除",
      onClick: () => setModalId("batchUpdate", { code: CodeType.Delete, datas: selectedRows }, reloadData),
    },
    {
      label: "冷热分离",
      onClick: () => setModalId("batchUpdate", { code: CodeType.Separate, datas: selectedRows }, reloadData),
    },
    {
      label: "pipeline",
      onClick: () => setModalId("batchUpdate", { code: CodeType.Pipeline, datas: selectedRows }, reloadData),
    },
    {
      label: "Rollover",
      onClick: () => setModalId("batchUpdate", { code: CodeType.Rollover, datas: selectedRows }, reloadData),
    },
    {
      label: "DCDR-平滑切换",
      onClick: () => switchDCDR(1),
    },
    {
      label: "DCDR-强制切换",
      onClick: () => switchDCDR(2),
    },
  ];
};
