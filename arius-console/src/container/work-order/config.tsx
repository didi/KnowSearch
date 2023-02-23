import React from "react";
import { renderOperationBtns, NavRouterLink } from "container/custom-component";
import { transTimeFormat, transTimeStamp } from "lib/utils";
import { cellStyle } from "constants/table";
import { Tooltip, Popconfirm, Modal, message } from "antd";
import { IColumnsType } from "component/dantd/query-form/QueryForm";
import { IBaseOrder, IOrderInfo, ITypeEnums } from "typesPath/cluster/order-types";
import { orderStatusMap, STAUS_TYPE_MAP, TASK_STATUS_TYPE_MAP, TASK_STATUS_NUMBER_MAP } from "constants/status-map";
import { ILabelValue, IMenuItem, IStringMap } from "interface/common";
import { LEVEL_MAP } from "constants/common";
import { INodeTask, ITask } from "typesPath/task-types";
import { IBaseInfo } from "typesPath/base-types";
import BaseInfo from "./base-info";
import { PlanSpeed } from "./plan-speed";
import { executeTask, retryTask, rollbackTask, actionTask, actionHostTask, rollbackClusterConfig } from "api/task-api";
import { cancelDcdr } from "api/dcdr-api";
import { rollbackFastIndexTask, retryFastIndexTask, cancelFastIndexTask } from "api/fastindex-api";
import { opRollbackPluginConfig, opRollbackUpdatePlugin } from "api/plug-api";
import { clusterRollback } from "api/cluster-api";
import { rollbackGateway, rollbackGatewayConfig } from "api/gateway-manage";
import DRangeTime from "../../d1-packages/d-range-time";
import { XModal } from "component/x-modal";
import Url from "lib/url-parser";
import "./index.less";

export const authCodeMap = {
  0: "超管",
  1: "配置管理",
  2: "访问",
  "-1": "无权限",
};

export const getMyApplicationColumns = (typeEnums: IStringMap, type?: string) => {
  const orderColumns = [
    {
      title: "工单ID",
      dataIndex: "id",
      width: 100,
    },
    {
      title: "工单标题",
      dataIndex: "title",
      width: 200,
      render: (text: string, record: IBaseOrder) => {
        let href = `/work-order/my-application/detail?title=${text}&orderId=${record.id}`;
        if (type) {
          href = `/work-order/my-approval/detail?title=${text}&orderId=${record.id}`;
        }
        return (
          <div className="two-row-ellipsis pointer">
            <NavRouterLink needToolTip={true} maxShowLength={50} element={text} href={href} />
          </div>
        );
      },
    },
    {
      title: "工单状态",
      dataIndex: "status",
      width: 100,
      render: (t: any) => (
        <>
          <span className={t === 1 ? "success" : t === 2 ? "fail" : ""}>{orderStatusMap[t] || ""}</span>
        </>
      ),
    },
    {
      title: "申请时间",
      dataIndex: "createTime",
      width: 160,
      // sorter: (a: IBaseOrder, b: IBaseOrder) => transTimeStamp(b.createTime) - transTimeStamp(a.createTime),
      render: (t: string) => transTimeFormat(t),
    },
    {
      title: "工单类型",
      dataIndex: "type",
      width: 120,
      render: (t: string, record: IBaseOrder) => <>{typeEnums[t] || "-"}</>,
    },
    {
      title: "申请原因",
      dataIndex: "description",
      width: 200,
      onCell: () => ({
        style: {
          maxWidth: 200,
          ...cellStyle,
        },
      }),
      render: (text: string) => {
        return (
          <Tooltip placement="bottomLeft" title={text}>
            {" "}
            {text || "_"}{" "}
          </Tooltip>
        );
      },
    },
  ];
  return orderColumns;
};

export const getMyApplicationQueryXForm = (typeList: ITypeEnums[], handleTimeChange) => {
  const customTimeOptions = [
    {
      label: "最近 1 天",
      value: 24 * 60 * 60 * 1000,
    },
    {
      label: "最近 7 天",
      value: 7 * 24 * 60 * 60 * 1000,
    },
    {
      label: "最近 1 月",
      value: 30 * 24 * 60 * 60 * 1000,
    },
  ];
  const formMap = [
    {
      dataIndex: "status",
      title: "工单状态:",
      type: "select",
      options: Object.keys(orderStatusMap).map((key) => ({
        value: key,
        title: orderStatusMap[key],
      })),
      placeholder: "请选择",
    },
    {
      dataIndex: "title",
      title: "工单标题:",
      type: "input",
      placeholder: "请输入工单标题",
    },
    {
      dataIndex: "type",
      title: "工单类型:",
      type: "select",
      options: typeList,
      placeholder: "请选择",
    },
    {
      dataIndex: "createTime",
      title: "申请时间:",
      type: "custom",
      component: <DRangeTime timeChange={handleTimeChange} popoverClassName="dashborad-popover" customTimeOptions={customTimeOptions} />,
    },
  ] as IColumnsType[];
  return formMap;
};

export const getTaskQueryXForm = (handleTimeChange) => {
  const customTimeOptions = [
    {
      label: "最近 1 天",
      value: 24 * 60 * 60 * 1000,
    },
    {
      label: "最近 7 天",
      value: 7 * 24 * 60 * 60 * 1000,
    },
    {
      label: "最近 1 月",
      value: 30 * 24 * 60 * 60 * 1000,
    },
  ];
  const formMap = [
    {
      dataIndex: "title",
      title: "任务标题:",
      type: "input",
      placeholder: "请输入",
    },
    {
      dataIndex: "createTime",
      title: "创建时间:",
      type: "custom",
      component: (
        <DRangeTime
          timeChange={handleTimeChange}
          popoverClassName="dashborad-popover"
          ///resetAllValue={resetAllValue}
          customTimeOptions={customTimeOptions}
        />
      ),
    },
  ] as IColumnsType[];
  return formMap;
};

export const getTaskColumns = (reloadData: Function, typeList: any) => {
  const orderColumns = [
    {
      title: "任务ID",
      dataIndex: "id",
      key: "id",
      width: 80,
    },
    {
      title: "任务标题",
      dataIndex: "title",
      key: "title",
      width: 200,
      lineClampTwo: true,
      render: (text: string, record: ITask) => {
        let type = typeList.filter((item) => item?.type === record.taskType)[0];
        let href = `/work-order/task/detail?taskid=${record.id}&type=${type?.message}&title=${record?.title}&status=${record?.status}`;
        if (record.taskType === 10) {
          href = `/work-order/task/dcdrdetail?title=${text}&taskid=${record.id}&title=${record.title}`;
        }
        if (record.taskType === 14) {
          let dataType = JSON.parse(record?.expandData)?.dataType;
          href = `/work-order/task/fastindex-detail?taskid=${record.id}&title=${record.title}&datatype=${dataType}`;
        }
        return <NavRouterLink needToolTip={true} element={text} href={href} />;
      },
    },
    {
      title: "任务类型",
      dataIndex: "taskType",
      key: "taskType",
      width: 100,
      render: (t: any) => {
        let type = typeList.filter((item) => item?.type === t)[0];
        return type?.message || "-";
      },
    },
    {
      title: "任务状态",
      dataIndex: "status",
      key: "status",
      width: 80,
      render: (t: any) => <>{TASK_STATUS_TYPE_MAP[t] || "_"}</>,
    },
    {
      title: "创建人",
      dataIndex: "creator",
      key: "creator",
      width: 80,
      render: (text: string) => {
        return (
          <Tooltip placement="bottomLeft" title={text}>
            {text}
          </Tooltip>
        );
      },
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
      width: "10%",
      sorter: true,
      render: (t: string) => transTimeFormat(t),
    },
    {
      title: "更新时间",
      dataIndex: "updateTime",
      key: "updateTime",
      width: 150,
      sorter: true,
      render: (t: string) => transTimeFormat(t),
    },
    {
      title: "操作",
      dataIndex: "operation",
      filterTitle: true,
      key: "operation",
      width: 150,
      render: (text: any, record: ITask) => {
        const btns = getTaskBtns(record, reloadData);
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return orderColumns;
};

const confirmFn = (text: string, fn: Function, reloadDataFn: Function, title: string) => {
  Modal.confirm({
    title: `确定${text}任务${title}?`,
    // icon: <DeleteOutlined style={{ color: "red" }} />,
    width: 500,
    okText: "确定",
    cancelText: "取消",
    onOk() {
      fn().then(() => {
        reloadDataFn();
      });
    },
  });
};

const getTaskBtns = (record: ITask, reloadData: Function) => {
  let href = `/work-order/task/detail?title=${record.title}&taskid=${record.id}#plan`;
  if (record.taskType === 10) {
    href = `/work-order/task/dcdrdetail?title=${record.title}&taskid=${record.id}&title=${record.title}`;
  }
  if (record.taskType === 14) {
    let dataType = JSON.parse(record?.expandData)?.dataType;
    href = `/work-order/task/fastindex-detail?taskid=${record.id}&title=${record.title}&datatype=${dataType}`;
  }
  let taskBtns = [
    {
      isRouterNav: true,
      label: <NavRouterLink element={"查看进度"} href={href} />,
    },
  ];
  const carryBtn = {
    label: <span onClick={() => TaskApi.executeTask(reloadData, record.id, record.title)}>执行 </span>,
  } as any;
  const pauseBtn = {
    label: <span onClick={() => TaskApi.pauseTask(reloadData, record.id, record.title)}>暂停</span>,
  } as any;
  const continueBtn = {
    label: <span onClick={() => TaskApi.continueTask(reloadData, record.id, record.title)}>继续 </span>,
  } as any;
  const restartBtn = {
    label: <span onClick={() => TaskApi.retryTask(reloadData, record.id, record.title)}>重试</span>,
  } as any;
  const cancelBtn = {
    label: <span onClick={() => TaskApi.cancalTask(reloadData, record.id, record.title)}>取消</span>,
  } as any;
  const rollbackBtn = {
    label: <span onClick={() => TaskApi.rollbackTask(reloadData, record.id, record.title)}>回滚</span>,
  } as any;
  const rollbackFastIndexBtn = {
    label: <span onClick={() => TaskApi.rollbackFastIndex(record.id, reloadData, record.title)}>回切</span>,
  } as any;
  const retryFastIndexBtn = {
    label: <span onClick={() => TaskApi.retryFastIndex(record, reloadData)}>重试</span>,
  } as any;
  const cancelFastIndexBtn = {
    label: <span onClick={() => TaskApi.cancelFastIndex(record.id, reloadData, record.title)}>取消</span>,
  } as any;
  const rollbackECM = (fn) => {
    let expandData = record.expandData ? JSON.parse(record.expandData) : {};
    let params = {
      componentId: expandData.componentId,
      taskId: record.id,
    };
    let jsonParams = { expandData: JSON.stringify(params) };
    let rollback = {
      isRouterNav: false,
      label: <span onClick={() => confirmFn("回滚", () => fn(jsonParams), reloadData, record.title)}>回滚</span>,
    };
    taskBtns = taskBtns.concat(restartBtn, rollback, cancelBtn);
  };
  if (record.taskType === 10) {
    if (record.status == "running") {
      taskBtns.push({
        isRouterNav: false,
        label: <span onClick={() => confirmFn("取消", () => cancelDcdr(record.id), reloadData, record.title)}>取消</span>,
      });
    }
    return taskBtns;
  }
  switch (record.status) {
    case "success":
      if (record.taskType === 14) {
        let expandData = JSON.parse(record?.expandData);
        let dataType = expandData.dataType;
        let transferStatus = expandData.transferStatus;
        if (dataType == 1 && transferStatus === 1) {
          taskBtns = taskBtns.concat(rollbackFastIndexBtn);
        }
      }
      break;
    case "cancel":
      if (record.taskType === 14) {
        taskBtns = taskBtns.concat(retryFastIndexBtn);
      }
      break;
    case "waiting":
      if (record.taskType === 14) {
        taskBtns = taskBtns.concat(cancelFastIndexBtn);
      } else {
        taskBtns = taskBtns.concat(carryBtn, cancelBtn);
      }
      break;
    case "running":
      if (record.taskType === 14) {
        taskBtns = taskBtns.concat(cancelFastIndexBtn);
      } else {
        taskBtns = taskBtns.concat(pauseBtn, cancelBtn);
      }
      break;
    case "failed":
      if (record.taskType === 11 || record.taskType === 12) {
        taskBtns = taskBtns.concat(restartBtn, rollbackBtn, cancelBtn);
      } else if (record.taskType === 14) {
        taskBtns = taskBtns.concat(retryFastIndexBtn);
      } else if (record.taskType === 25) {
        rollbackECM(rollbackGateway);
      } else if (record.taskType === 28) {
        rollbackECM(rollbackGatewayConfig);
      } else if (record.taskType === 36) {
        rollbackECM(clusterRollback);
      } else if (record.taskType === 37) {
        rollbackECM(rollbackClusterConfig);
      } else if (record.taskType === 40) {
        rollbackECM(opRollbackUpdatePlugin);
      } else if (record.taskType === 46) {
        let expandData = record.expandData ? JSON.parse(record.expandData) : {};
        let params = {
          componentId: expandData.componentId,
          pluginType: expandData.pluginType,
          dependComponentId: expandData.dependComponentId,
          taskId: record.id,
        };
        let jsonParams = { expandData: JSON.stringify(params) };
        let rollback = {
          isRouterNav: false,
          label: <span onClick={() => confirmFn("回滚", () => opRollbackPluginConfig(jsonParams), reloadData, record.title)}>回滚</span>,
        };
        taskBtns = taskBtns.concat(restartBtn, rollback, cancelBtn);
      } else {
        taskBtns = taskBtns.concat(restartBtn, cancelBtn);
      }
      break;
    case "pause":
      taskBtns = taskBtns.concat(continueBtn, cancelBtn);
      break;
  }
  return taskBtns;
};

export const TaskApi = {
  executeTask: (reloadData: Function, taskId: number, title) => {
    return confirmFn("执行", () => executeTask(taskId), reloadData, title);
  },
  retryTask: (reloadData: Function, taskId: number, title) => {
    return confirmFn("重试", () => retryTask(taskId), reloadData, title);
  },
  pauseTask: (reloadData: Function, taskId: number, title) => {
    return confirmFn("暂停", () => actionTask("pause", taskId), reloadData, title);
  },
  continueTask: (reloadData: Function, taskId: number, title) => {
    return confirmFn("继续", () => actionTask("start", taskId), reloadData, title);
  },
  cancalTask: (reloadData: Function, taskId: number, title) => {
    return confirmFn("取消", () => actionTask("cancel", taskId), reloadData, title);
  },
  rollbackTask: (reloadData: Function, taskId: number, title) => {
    return confirmFn("回滚", () => rollbackTask(taskId), reloadData, title);
  },
  rollbackFastIndex: (id: number, reloadData: Function, title: string) => {
    let rollbackTitle = `确定回切任务“${title}”？`;
    let content = <div className="content">回切后，已迁移的数据会全部回切到源集群中。</div>;
    let onOk = async () => {
      await rollbackFastIndexTask(id);
      message.success("操作成功");
      reloadData();
    };
    return XModal({ type: "warning", title: rollbackTitle, content, className: "rollback-fastindex-modal", onOk });
  },
  retryFastIndex: (record, reloadData) => {
    let title = `确定重试任务${record?.title}？`;
    let writeType = JSON.parse(record?.expandData)?.writeType;
    let content = (
      <div>
        <div>
          {writeType === 1
            ? " 指定ID+版本号的写入方式，ID冲突时，以高版本文档为主，进行覆盖或者丢弃"
            : writeType === 2
            ? "指定ID的写入方式，ID冲突时，进行覆盖写入"
            : "指定ID的写入方式，ID冲突时，丢弃当前写入"}
        </div>
        <div>重试会清除目标索引的全部数据，包括近期写入的数据，请谨慎操作。</div>
      </div>
    );
    let onOk = async () => {
      await retryFastIndexTask(record.id);
      message.success("操作成功");
      reloadData();
    };
    return XModal({ type: "warning", title, content, className: "retry-fastindex-modal", onOk });
  },
  cancelFastIndex: (id: number, reloadData: Function, title: string) => {
    return confirmFn("取消", () => cancelFastIndexTask(id), reloadData, title);
  },
};

export const DESC_LIST = [
  {
    label: "逻辑索引模板",
    key: "logicTemplateName",
    render: (value: string) => (
      <>
        <span>{value || "-"}</span>
      </>
    ),
  },
  {
    label: "所属逻辑集群",
    key: "logicClusterName",
    render: (value: string) => (
      <>
        <span>{value || "-"}</span>
      </>
    ),
  },
  {
    label: "物理索引模板（主）",
    key: "masterPhysicalTemplateName",
    render: (value: string) => (
      <>
        <span>{value || "-"}</span>
      </>
    ),
  },
  {
    label: "所属物理集群（主）",
    key: "masterPhysicalClusterName",
    render: (value: string) => (
      <>
        <span>{value || "-"}</span>
      </>
    ),
  },
  {
    label: "物理索引模板（从）",
    key: "vicePhysicalTemplateName",
    render: (value: string) => (
      <>
        <span>{value || "-"}</span>
      </>
    ),
  },
  {
    label: "所属物理集群（从）",
    key: "vicePhysicalClusterName",
    render: (value: string) => (
      <>
        <span>{value || "-"}</span>
      </>
    ),
  },
  {
    label: "开始时间",
    key: "createTime",
    render: (value: string) => (
      <>
        <span>{transTimeFormat(value)}</span>
      </>
    ),
  },
];

export enum TAB_LIST_KEY {
  base = "base",
  plan = "plan",
  dcdrPlan = "dcdrPlan",
}

export const BASE_INFO: IBaseInfo[] = [
  {
    key: "title",
    label: "任务名称",
  },
  {
    key: "status",
    label: "任务状态",
    render: ({ baseInfo }) => {
      let type = "";
      STAUS_TYPE_MAP.forEach((ele: ILabelValue) => {
        if (ele.value === baseInfo) {
          type = ele.text;
        }
      });
      return <>{type}</>;
    },
  },
  {
    key: "createTime",
    label: "创建时间",
    render: ({ baseInfo }) => transTimeFormat(baseInfo),
  },
  {
    key: "updateTime",
    label: "更新时间",
    render: ({ baseInfo }) => transTimeFormat(baseInfo),
  },
  {
    key: "orderType",
    label: "任务类型",
    render: ({ typeList }) => {
      let type = decodeURI(Url()?.search?.type);
      if (Number(type)) {
        type = typeList.filter((item) => item?.type === Number(type))[0]?.message;
      }
      return type || "-";
    },
  },
  {
    key: "desc",
    label: "集群描述",
    render: ({ task }) => {
      let taskBaseInfo = task?.taskBaseInfo;
      let memo = taskBaseInfo?.expandData ? JSON.parse(taskBaseInfo?.expandData)?.memo : "";
      return memo || "-";
    },
  },
];

export const ORDER_INFO = (task) => {
  return [
    {
      key: "title",
      label: "工单标题",
      render: (text: number) => (
        <>
          <div style={{ float: "left" }}>
            <span className="text-value">
              {text}（{task.taskBaseInfo?.workOrderId}）
            </span>
          </div>
          <div style={{ float: "left" }}>
            <NavRouterLink element="查看" href={`/work-order/my-approval/detail?title=${text}&orderId=${task.taskBaseInfo?.workOrderId}`} />
          </div>
        </>
      ),
    },
    {
      key: "creator",
      label: "申请人",
    },
  ];
};

export const getPlanSpeedColumns = (setModalId: Function, reloadData) => {
  const cols = [
    {
      title: "ID",
      dataIndex: "id",
      key: "id",
      width: 100,
    },
    {
      title: "节点名称",
      dataIndex: "host",
      key: "host",
      width: 120,
    },
    {
      title: "更新时间",
      dataIndex: "updateTime",
      key: "updateTime",
      width: 150,
      render: (t: string) => transTimeFormat(t),
    },
    {
      title: "操作",
      dataIndex: "operation",
      filterTitle: true,
      key: "operation",
      width: 150,
      render: (text: number, record: INodeTask) => {
        const btns = getPlanBtns(record, setModalId, reloadData);
        return renderOperationBtns(btns as any, record);
      },
    },
  ];
  return cols;
};

const getPlanBtns = (record: INodeTask, setModalId, reloadData) => {
  let status = TASK_STATUS_NUMBER_MAP[record?.status];
  let taskBtns = [
    {
      label: "查看日志",
      clickFunc: (record: INodeTask) => {
        setModalId("taskLogModal", record);
      },
    },
    {
      label: (
        <Popconfirm
          title="确定重试？"
          onConfirm={async () => {
            let params = { action: "redo", taskId: +Url()?.search?.taskid, host: record.host, groupName: record.groupName };
            await actionHostTask(params);
            message.success("操作成功");
            reloadData();
          }}
        >
          <a>重试</a>
        </Popconfirm>
      ),
    },
  ];
  if (status === "failed") {
    return taskBtns;
  }
  return taskBtns.slice(0, 1);
};

export const TASK_TAB_LIST = [
  {
    name: "基础信息",
    key: TAB_LIST_KEY.base,
    content: <BaseInfo task={{} as any} />,
  },
  {
    name: "执行进度",
    key: TAB_LIST_KEY.plan,
    content: <PlanSpeed task={{} as any} />,
  },
];

const menuMap = new Map<string, IMenuItem>();
TASK_TAB_LIST.forEach((d) => {
  menuMap.set(d.key, d);
});

export const TASK_MENU_MAP = menuMap;

export const DCDR_STEPS = [
  {
    title: "创建dcdr",
    description: "",
    key: 0,
  },
  {
    title: "停止向主索引写入数据",
    description: "",
    key: 1,
  },
  {
    title: "确保主从索引数据同步",
    description: "",
    key: 2,
  },
  {
    title: "删除源dcdr模板和索引链路",
    description: "",
    key: 3,
  },
  {
    title: "拷贝主模板的mapping信息到从模板",
    description: "",
    key: 4,
  },
  {
    title: "关闭从索引dcdr索引开关，并打开主索引dcdr索引开关",
    description: "",
    key: 5,
  },
  {
    title: "停止从索引写入",
    description: "",
    key: 6,
  },
  {
    title: "创建新的dcdr链路",
    description: "",
    key: 7,
  },
  {
    title: "恢复主从索引实时写入",
    description: "",
    key: 8,
  },
  {
    title: "主从模板角色切换",
    description: "",
    key: 9,
  },
];

export const getInfoRenderItem = (orderInfo: IOrderInfo) => {
  const { type, detailInfo, description } = orderInfo;
  const logicClusterIndecreaseList: ILabelValue[] = [
    {
      label: "集群名称",
      value: detailInfo?.logicClusterName,
    },
    {
      label: "data节点数",
      value: detailInfo?.oldDataNodeNu,
    },
    {
      label: "期望节点数",
      value: detailInfo?.dataNodeNu,
    },
    {
      label: "节点规格",
      value: detailInfo?.dataNodeSpec,
    },
    {
      label: "申请原因",
      value: description,
    },
  ];

  const logicClusterCreateList: ILabelValue[] = [
    {
      label: "集群名称",
      value: detailInfo?.name,
    },
    {
      label: "业务等级",
      value: LEVEL_MAP[Number(detailInfo?.level) - 1]?.label,
    },
    {
      label: "节点规格",
      value: detailInfo?.dataNodeSpec,
    },
    {
      label: "data节点数",
      value: detailInfo?.dataNodeNu,
    },
    {
      label: "申请原因",
      value: description,
    },
  ];

  if (type === "logicClusterIndecrease") {
    return logicClusterIndecreaseList;
  } else if (type === "logicClusterCreate") {
    return logicClusterCreateList;
  } else if (type === "dslTemplateStatusChange") {
    return [
      {
        label: "查询模版",
        value: detailInfo?.dslTemplateMd5,
      },
    ];
  } else if (type === "dslTemplateQueryLimit") {
    const { dslQueryLimitDTOList } = detailInfo;
    const dslTemplateQueryLimit: ILabelValue[] = [];
    for (let item of dslQueryLimitDTOList) {
      dslTemplateQueryLimit.push(
        ...[
          {
            label: "查询模版",
            value: item?.dslTemplateMd5,
          },
          {
            label: "原限流值",
            value: item?.queryLimitBefore,
          },
          {
            label: "修改后限流值",
            value: item?.queryLimit,
          },
        ]
      );
    }
    return dslTemplateQueryLimit;
  } else if (type === "templateLogicBlockWrite" || type === "templateLogicBlockRead") {
    return [
      {
        label: "索引模版",
        value: detailInfo?.name,
      },
    ];
  } else {
    return [] as ILabelValue[];
  }
};

export const getAddConfigInfoColumns = (type: string, setDrawerId: Function): any => {
  const columns = [
    {
      title: "节点角色",
      dataIndex: "enginName",
      key: "enginName",
    },
    {
      title: "配置类别",
      dataIndex: "typeName",
      key: "typeName",
      render: (value: string) => <span>{value || "-"}</span>,
    },
    {
      title: "配置内容",
      dataIndex: "configData",
      key: "configData",
      onCell: () => ({
        style: cellStyle,
      }),
      render: (text: string) => {
        return (
          <a
            onClick={() => {
              setDrawerId("configDetail", text);
            }}
          >
            {text}
          </a>
        );
      },
    },
  ];
  const columnsItem = {
    title: "原配置内容",
    dataIndex: "originalConfigData",
    key: "originalConfigData",
    onCell: () => ({
      style: cellStyle,
    }),
    render: (text: string) => {
      return (
        <a
          onClick={() => {
            setDrawerId("configDetail", text);
          }}
        >
          {text}
        </a>
      );
    },
  };
  const columnsItemUpdata = {
    title: "编辑后内容",
    dataIndex: "configData",
    key: "configData",
    onCell: () => ({
      style: cellStyle,
    }),
    render: (text: string) => {
      return (
        <a
          onClick={() => {
            setDrawerId("configDetail", text);
          }}
        >
          {text}
        </a>
      );
    },
  };
  if (type.indexOf("编辑") > -1) {
    columns.splice(2, 1, columnsItem, columnsItemUpdata);
  }
  return columns;
};
