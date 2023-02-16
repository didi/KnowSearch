import React from "react";
import { renderOperationBtns, NavRouterLink } from "container/custom-component";
import { transTimeFormat, transTimeStamp } from "lib/utils";
import moment from "moment";
import { cellStyle } from "constants/table";
import { Tooltip, DatePicker, Popconfirm, Modal, message } from "antd";
import { IColumnsType } from "component/dantd/query-form/QueryForm";
import { IBaseOrder, IOrderInfo, ITypeEnums } from "typesPath/cluster/order-types";
import {
  orderStatusMap,
  STAUS_TYPE_MAP,
  TASK_STATUS_TYPE_MAP,
  TASK_TYPE_MAP,
  TASK_TYPE_MAP_LIST,
  VERSION_MAINFEST_TYPE,
} from "constants/status-map";
import { ILabelValue, IMenuItem, IStringMap } from "interface/common";
import { LEVEL_MAP } from "constants/common";
import { INodeTask, ITask } from "typesPath/task-types";
import { IBaseInfo } from "typesPath/base-types";
import { BaseInfo } from "./base-info";
import { PlanSpeed } from "./plan-speed";
import { cancalTask, createTask, pauseTask, restartTask, retryTask, scaleTask, upgradeTask } from "api/task-api";
import { cancelDcdr } from "api/dcdr-api";
import DRangeTime from "../../d1-packages/d-range-time";
const { RangePicker } = DatePicker;
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

export const getTaskColumns = (reloadData: Function) => {
  const orderColumns = [
    {
      title: "任务ID",
      dataIndex: "id",
      key: "id",
    },
    {
      title: "任务标题",
      dataIndex: "title",
      key: "title",
      render: (text: string, record: ITask) => {
        const str = encodeURI(record.expandData);
        let href = `/work-order/task/detail?title=${text}&taskid=${record.id}&id=${record.businessKey}&type=${record?.taskType}&status=${record?.status}&dcdr_info=${str}`;
        if (record.taskType === 10) {
          href = `/work-order/task/dcdrdetail?title=${text}&taskid=${record.id}&title=${record.title}`;
        }
        return <NavRouterLink needToolTip={true} element={text} href={href} />;
      },
    },
    {
      title: "任务类型",
      dataIndex: "taskType",
      key: "taskType",
      render: (t: any) => <>{TASK_TYPE_MAP[t] || "_"}</>,
    },
    {
      title: "任务状态",
      dataIndex: "status",
      key: "status",
      render: (t: any) => <>{TASK_STATUS_TYPE_MAP[t] || "_"}</>,
    },
    {
      title: "创建人",
      dataIndex: "creator",
      key: "creator",
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
      width: "10%",
      sorter: true,
      render: (t: string) => transTimeFormat(t),
    },
    {
      title: "操作",
      dataIndex: "operation",
      filterTitle: true,
      key: "operation",
      render: (text: any, record: ITask) => {
        const btns = getTaskBtns(record, reloadData);
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return orderColumns;
};

const confirmFn = (text: string, taskId: number, id: number, fn: Function, reloadDataFn: Function) => {
  Modal.confirm({
    title: `确定${text}任务${taskId}?`,
    // icon: <DeleteOutlined style={{ color: "red" }} />,
    width: 500,
    okText: "确定",
    cancelText: "取消",
    onOk() {
      fn(id).then(() => {
        reloadDataFn();
      });
    },
  });
};

const getTaskBtns = (record: ITask, reloadData: Function) => {
  let href = `/work-order/task/detail?title=${record.title}&taskid=${record.id}&id=${record.businessKey}#plan`;
  if (record.taskType === 10) {
    href = `/work-order/task/dcdrdetail?title=${record.title}&taskid=${record.id}&title=${record.title}`;
  }
  let taskBtns = [
    {
      isRouterNav: true,
      label: <NavRouterLink element={"查看进度"} href={href} />,
    },
  ];
  const carryBtn = {
    label: (
      <span
        onClick={() => {
          // 后端字段修改： record.orderType -> record.taskType
          return carryTask(record.businessKey, record.taskType, reloadData, record.id);
        }}
      >
        执行
      </span>
    ),
  } as any;
  const pauseBtn = {
    label: <span onClick={() => TaskApi.pauseTask(record.businessKey, reloadData, record.id)}>暂停</span>,
  } as any;
  const restartBtn = {
    label: <span onClick={() => TaskApi.retryTask(record.businessKey, reloadData, record.id)}>重试</span>,
  } as any;
  const cancalBtn = {
    label: <span onClick={() => TaskApi.cancalTask(record.businessKey, reloadData, record.id)}>取消</span>,
  } as any;
  if (record.taskType === 10) {
    if (record.status == "running") {
      taskBtns.push({
        isRouterNav: false,
        label: (
          <span
            onClick={() => {
              Modal.confirm({
                title: `提示`,
                content: `确定取消任务${record.title}?`,
                // icon: <DeleteOutlined style={{ color: "red" }} />,
                // width: 500,
                okText: "确定",
                cancelText: "取消",
                onOk() {
                  cancelDcdr(record.id).then(() => {
                    reloadData();
                    message.success("操作成功");
                  });
                },
              });
            }}
          >
            取消
          </span>
        ),
      });
    }
    return taskBtns;
  }
  switch (record.status) {
    case "waiting":
      taskBtns = taskBtns.concat(carryBtn, cancalBtn);
      break;
    case "running":
      taskBtns = taskBtns.concat(pauseBtn, cancalBtn);
      break;
    case "failed":
      taskBtns = taskBtns.concat(restartBtn, cancalBtn);
      break;
    case "pause":
      taskBtns = taskBtns.concat(carryBtn, cancalBtn);
      break;
  }
  return taskBtns;
};

export const carryTask = (id: number, orderType: number, reloadData: Function, taskId?: number) => {
  // 1 集群启动create 2 集群扩容scale 3 集群缩容scale 4 集群重启restart 5 集群升级upgrade
  switch (orderType) {
    case 1:
      TaskApi.createTask(id, reloadData, taskId);
      break;
    case 2:
    case 3:
      TaskApi.scaleTask(id, reloadData, taskId);
      break;
    case 4:
      TaskApi.restartTask(id, reloadData, taskId);
      break;
    default:
      TaskApi.upgradeTask(id, reloadData, taskId);
  }
};

const TaskApi = {
  createTask: (id: number, reloadData: Function, taskId: number) => {
    // return createTask(id).then(() => reloadData()); // this.getTaskList()
    return confirmFn("执行", taskId, id, createTask, reloadData);
  },
  scaleTask: (id: number, reloadData: Function, taskId: number) => {
    // return scaleTask(id).then(() => reloadData());
    return confirmFn("执行", taskId, id, scaleTask, reloadData);
  },
  restartTask: (id: number, reloadData: Function, taskId: number) => {
    // return restartTask(id).then(() => reloadData());
    return confirmFn("执行", taskId, id, restartTask, reloadData);
  },
  retryTask: (id: number, reloadData: Function, taskId: number) => {
    // return retryTask(id).then(() => reloadData());
    return confirmFn("重试", taskId, id, retryTask, reloadData);
  },
  upgradeTask: (id: number, reloadData: Function, taskId: number) => {
    // return upgradeTask(id).then(() => reloadData());
    return confirmFn("执行", taskId, id, upgradeTask, reloadData);
  },
  pauseTask: (id: number, reloadData: Function, taskId: number) => {
    return confirmFn("暂停", taskId, id, pauseTask, reloadData);
  },
  cancalTask: (id: number, reloadData: Function, taskId: number) => {
    return confirmFn("取消", taskId, id, cancalTask, reloadData);
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
    render: (text: string) => {
      let type = "";
      STAUS_TYPE_MAP.forEach((ele: ILabelValue) => {
        if (ele.value === text) {
          type = ele.text;
        }
      });
      return <>{type}</>;
    },
  },
  {
    key: "clusterNodeRole",
    label: "节点角色",
    render: (text: string) => {
      return <>{text}</>;
    },
  },
  {
    key: "createTime",
    label: "创建时间",
    render: (t: string) => transTimeFormat(t),
  },
  {
    key: "updateTime",
    label: "更新时间",
    render: (t: string) => transTimeFormat(t),
  },
  {
    key: "clusterName",
    label: "生效集群",
  },
  {
    key: "orderType",
    label: "集群类型",
    render: (t: number) => {
      let type = "";
      TASK_TYPE_MAP_LIST.forEach((ele: ILabelValue) => {
        if (ele.value === t) {
          type = ele.text;
        }
      });
      return <>{type}</>;
    },
  },
  {
    key: "desc",
    label: "集群描述",
    render: (t) => {
      return t || "-";
    },
  },
  {
    key: "esVersion",
    label: "ES 版本",
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

export const getPlanSpeedColumns = (setModalId: Function) => {
  const cols = [
    {
      title: "ID",
      dataIndex: "id",
      key: "id",
    },
    {
      title: "节点名称",
      dataIndex: "hostname",
      key: "hostname",
    },
    {
      title: "分组",
      dataIndex: "grp",
      key: "grp",
    },
    {
      title: "更新时间",
      dataIndex: "updateTime",
      key: "updateTime",
      sorter: (a: INodeTask, b: INodeTask) => transTimeStamp(b.updateTime) - transTimeStamp(a.updateTime),
      render: (t: string) => transTimeFormat(t),
    },
    {
      title: "操作",
      dataIndex: "operation",
      filterTitle: true,
      key: "operation",
      width: "15%",
      render: (text: number, record: INodeTask) => {
        const btns = getPlanBtns(record, setModalId);
        return renderOperationBtns(btns as any, record);
      },
    },
  ];
  return cols;
};

const getPlanBtns = (record: INodeTask, setModalId) => {
  const taskBtns = [
    {
      label: "查看日志",
      clickFunc: (record: INodeTask) => {
        // viewTaskLog(record.id);
        setModalId("taskLogModal", record.id);
      },
    },
    {
      label: (
        <Popconfirm title="确定重试？" onConfirm={() => null}>
          <a>重试</a>
        </Popconfirm>
      ),
    },
    {
      label: (
        <Popconfirm title="确定忽略？" onConfirm={() => null}>
          <a>忽略</a>
        </Popconfirm>
      ),
    },
  ];
  if (record.status === "failed") {
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
