import React from "react";
import { renderOperationBtns, NavRouterLink } from "container/custom-component";
import { transTimeFormat, transTimeStamp } from "lib/utils";
import moment from "moment";
import { cellStyle } from "constants/table";
import { Tooltip, DatePicker, Popconfirm, Modal, message } from "antd";
import { IColumnsType } from "component/dantd/query-form/QueryForm";
import { IBaseOrder, IOrderInfo, ITypeEnums } from "typesPath/cluster/order-types";
import {
  appTemplateAuthEnum,
  ClusterAuth,
  orderStatusMap,
  STAUS_TYPE_MAP,
  TASK_STATUS_TYPE_MAP,
  TASK_TYPE_MAP,
  TASK_TYPE_MAP_LIST,
  VERSION_MAINFEST_TYPE,
} from "constants/status-map";
import { ILabelValue, IMenuItem, IStringMap } from "interface/common";
import { DATA_TYPE_LIST, LEVEL_MAP } from "constants/common";
import { IRoleIpList } from "typesPath/cluster/cluster-types";
import { INodeTask, ITask } from "typesPath/task-types";
import { IBaseInfo } from "typesPath/base-types";
import { BaseInfo } from "./base-info";
import { PlanSpeed } from "./plan-speed";
import { timeFormat } from "constants/time";
import {
  cancalTask,
  createTask,
  pauseTask,
  restartTask,
  retryTask,
  scaleTask,
  upgradeTask,
} from "api/task-api";
import { cancelDcdr } from 'api/dcdr-api';

const { RangePicker } = DatePicker;
export const authCodeMap = {
  0: '超管',
  1: '配置管理',
  2: '访问',
  '-1': '无权限',
}

export const getMyApplicationColumns = (
  typeEnums: IStringMap,
  type?: string
) => {
  const orderColumns = [
    {
      title: "工单ID",
      dataIndex: "id",
      key: "id",
    },
    {
      title: "工单标题",
      dataIndex: "title",
      key: "title",
      render: (text: string, record: IBaseOrder) => {
        let href = `/work-order/my-application/detail?orderId=${record.id}`;
        if (type) {
          href = `/work-order/my-approval/detail?orderId=${record.id}`;
        }
        return <NavRouterLink needToolTip={true} element={text} href={href} />;
      },
    },
    {
      title: "工单状态",
      dataIndex: "status",
      key: "status",
      render: (t: any) => (
        <>
          <span className={t === 1 ? "success" : t === 2 ? "fail" : ""}>
            {orderStatusMap[t] || ""}
          </span>
        </>
      ),
    },
    {
      title: "申请时间",
      dataIndex: "createTime",
      key: "createTime",
      sorter: (a: IBaseOrder, b: IBaseOrder) =>
        transTimeStamp(b.createTime) - transTimeStamp(a.createTime),
      render: (t: string) => transTimeFormat(t),
    },
    {
      title: "工单类型",
      dataIndex: "type",
      key: "type",
      render: (t: string, record: IBaseOrder) => <>{typeEnums[t] || ""}</>,
    },
    {
      title: "申请原因",
      dataIndex: "description",
      key: "description",
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

export const getMyApplicationQueryXForm = (typeList: ITypeEnums[]) => {
  const formMap = [
    {
      dataIndex: "status",
      title: "工单状态",
      type: "select",
      options: Object.keys(orderStatusMap).map((key) => ({
        value: key,
        title: orderStatusMap[key],
      })),
      placeholder: "请选择",
    },
    {
      dataIndex: "title",
      title: "工单标题",
      type: "input",
      placeholder: "请输入",
    },
    {
      dataIndex: "type",
      title: "工单类型",
      type: "select",
      options: typeList,
      placeholder: "请选择",
    },
    {
      dataIndex: "createTime",
      title: "申请时间",
      type: "custom",
      component: (
        <RangePicker
          ranges={{
            近一天: [moment().subtract(1, "day"), moment()],
            近一周: [moment().subtract(7, "day"), moment()],
            近一月: [moment().subtract(1, "month"), moment()],
          }}
          showTime={{ format: "HH:mm" }}
          format="YYYY-MM-DD"
        />
      ),
    },
  ] as IColumnsType[];
  return formMap;
};

export const getTaskQueryXForm = () => {
  const formMap = [
    {
      dataIndex: "title",
      title: "任务标题",
      type: "input",
      placeholder: "请输入",
    },
    {
      dataIndex: "createTime",
      title: "创建时间",
      type: "custom",
      component: (
        <RangePicker
          ranges={{
            近一天: [moment().subtract(1, "day"), moment()],
            近一周: [moment().subtract(7, "day"), moment()],
            近一月: [moment().subtract(1, "month"), moment()],
          }}
          showTime={{ format: "HH:mm" }}
          format="YYYY-MM-DD"
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
        let href = `/work-order/task/detail?id=${record.businessKey}&taskid=${record.id}&type=${record?.taskType}&status=${record?.status}&dcdr_info=${str}`;
        if (record.taskType === 10) {
          href = `/work-order/task/dcdrdetail?taskid=${record.id}&title=${record.title}`;
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
      sorter: (a: ITask, b: ITask) =>
        transTimeStamp(b.createTime) - transTimeStamp(a.createTime),
      render: (t: string) => transTimeFormat(t),
    },
    {
      title: "更新时间",
      dataIndex: "updateTime",
      key: "updateTime",
      width: "10%",
      sorter: (a: ITask, b: ITask) =>
        transTimeStamp(b.updateTime) - transTimeStamp(a.updateTime),
      render: (t: string) => transTimeFormat(t),
    },
    {
      title: "操作",
      dataIndex: "operation",
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
}

const getTaskBtns = (record: ITask, reloadData: Function) => {
  let href = `/work-order/task/detail?id=${record.businessKey}&taskid=${record.id}#plan`;
  if (record.taskType === 10) {
    href = `/work-order/task/dcdrdetail?taskid=${record.id}&title=${record.title}`;
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
    label: (
      <span
        onClick={() => TaskApi.pauseTask(record.businessKey, reloadData, record.id)}
      >
        暂停
      </span>
    ),
  } as any;
  const restartBtn = {
    label: (
      <span
        onClick={() => TaskApi.retryTask(record.businessKey, reloadData, record.id)}
      >
        重试
      </span>
    ),
  } as any;
  const cancalBtn = {
    label: (
      <span
        onClick={() => TaskApi.cancalTask(record.businessKey, reloadData, record.id)}
      >
        取消
      </span>
    ),
  } as any;
  if (record.taskType === 10) {
    if (record.status == 'running') {
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
                    message.success('操作成功')
                  });
                },
              });
            }}
          >
            取消
          </span>
        ),
      })
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

export const carryTask = (
  id: number,
  orderType: number,
  reloadData: Function,
  taskId?: number
) => {
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
    return confirmFn('执行', taskId, id, createTask, reloadData);
  },
  scaleTask: (id: number, reloadData: Function, taskId: number) => {
    // return scaleTask(id).then(() => reloadData());
    return confirmFn('执行', taskId, id, scaleTask, reloadData);
  },
  restartTask: (id: number, reloadData: Function, taskId: number) => {
    // return restartTask(id).then(() => reloadData());
    return confirmFn('执行', taskId, id, restartTask, reloadData);
  },
  retryTask: (id: number, reloadData: Function, taskId: number) => {
    // return retryTask(id).then(() => reloadData());
    return confirmFn('重试', taskId, id, retryTask, reloadData);
  },
  upgradeTask: (id: number, reloadData: Function, taskId: number) => {
    // return upgradeTask(id).then(() => reloadData());
    return confirmFn('执行', taskId, id, upgradeTask, reloadData);
  },
  pauseTask: (id: number, reloadData: Function, taskId: number) => {
    return confirmFn('暂停', taskId, id, pauseTask, reloadData);
  },
  cancalTask: (id: number, reloadData: Function, taskId: number) => {
    return confirmFn('取消', taskId, id, cancalTask, reloadData);
  },
};

export const DESC_LIST = [
  {
    label: "逻辑索引模板",
    key: "logicTemplateName",
    render: (value: string) => (
      <>
        <span>{value || '-'}</span>
      </>
    ),
  },
  {
    label: "所属逻辑集群",
    key: "logicClusterName",
    render: (value: string) => (
      <>
        <span>{value || '-'}</span>
      </>
    ),
  },
  {
    label: "物理索引模板（主）",
    key: "masterPhysicalTemplateName",
    render: (value: string) => (
      <>
        <span>{value || '-'}</span>
      </>
    ),
  },
  {
    label: "所属物理集群（主）",
    key: "masterPhysicalClusterName",
    render: (value: string) => (
      <>
        <span>{value || '-'}</span>
      </>
    ),
  },
  {
    label: "物理索引模板（从）",
    key: "vicePhysicalTemplateName",
    render: (value: string) => (
      <>
        <span>{value || '-'}</span>
      </>
    ),
  },
  {
    label: "所属物理集群（从）",
    key: "vicePhysicalClusterName",
    render: (value: string) => (
      <>
        <span>{value || '-'}</span>
      </>
    ),
  },
  {
    label: "开始时间",
    key: "createTime",
    render: (value: string) => (
      <>
        <span>{moment(value).format(timeFormat) || '-'}</span>
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
    render: (text: string) => <>{transTimeFormat(text)}</>,
  },
  {
    key: "updateTime",
    label: "更新时间",
    render: (text: string) => <>{transTimeFormat(text)}</>,
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
      return t || '-';
    },
  },
  // {
  //   key: "nsTree",
  //   label: "服务节点",
    // render: (t: string) => {
    //   return (
    //   <>
    //     <Tooltip placement="bottomLeft" title={t}>
    //       <div style={{...cellStyle, marginBottom: '-4px'}}>{t}</div>
    //     </Tooltip>
    //   </>
    //   );
    // },
  // },
  // {
  //   key: "idc",
  //   label: "机房",
  // },
  // {
  //   key: "dept",
  //   label: "成本部门",
  // },
  {
    key: "esVersion",
    label: "ES 版本",
  },
  {
    key: "type",
    label: "资源类型",
    render: (value: string) => (
      <Tooltip
        placement="bottomLeft"
        title={VERSION_MAINFEST_TYPE[Number(value)]}
      >
        {VERSION_MAINFEST_TYPE[Number(value)]}
      </Tooltip>
    ),
  },
  // {
  //   key: "imageName",
  //   label: "镜像",
  //   render: (text: string) => (
  //     <Tooltip placement="bottomLeft" title={text}>
  //       {text}
  //     </Tooltip>
  //   ),
  // },
];

export const ORDER_INFO = (task) => {
  return [
    {
      key: "title",
      label: "工单标题",
      render: (text: number) => (
        <>
          <div style={{ float: 'left' }}>
            <span className="text-value">
              {text}（{task.taskBaseInfo?.workOrderId}）
            </span>
          </div>
          <div style={{ float: 'left' }}>
            <NavRouterLink element="查看" href={`/work-order/my-approval/detail?orderId=${task.taskBaseInfo?.workOrderId}`} />
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
      sorter: (a: INodeTask, b: INodeTask) =>
        transTimeStamp(b.updateTime) - transTimeStamp(a.updateTime),
      render: (t: string) => transTimeFormat(t),
    },
    {
      title: "操作",
      dataIndex: "operation",
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

export const getInfoRenderItem = (orderInfo: IOrderInfo, result: boolean) => {
  const detail = orderInfo?.detailInfo || {};
  const type = orderInfo.type;

  const expandText = detail.operationType === 2 ? "扩容" : "缩容";

  let workType = "";
  DATA_TYPE_LIST.forEach((ele) => {
    if (ele.value === Number(detail.dataType)) {
      workType = ele.label;
    }
  });

  let authCodeText = "";
  appTemplateAuthEnum.forEach((ele) => {
    if (ele.value === Number(detail.authCode)) {
      authCodeText = ele.label;
    }
  });

  // let clusterAuthCodeText = "";
  // ClusterAuth.forEach((ele) => {
  //   if (ele.value === Number(detail.authCode)) {
  //     clusterAuthCodeText = ele.label;
  //   }
  // });

  const appCreateList: ILabelValue[] = [
    {
      label: "租用名",
      value: detail.name  || '-',
    },
    {
      label: "业务负责人",
      value: detail.responsible  || '-',
    },
    // {
    //   label: "成本部门ID",
    //   value: detail.departmentId,
    // },
    // {
    //   label: "成本部门",
    //   value: detail.department,
    // },
  ];

  const templateTransferList: ILabelValue[] = [
    {
      label: "模版ID",
      value: detail.id || '-',
    },
    {
      label: "模版名称",
      value: detail.name || '-',
    },
    {
      label: "原应用",
      value: detail.sourceAppId || '-',
    },
    {
      label: "目标应用",
      value: detail.tgtAppId || '-',
    },
    {
      label: "责任人",
      value: detail.tgtResponsible || '-',
    },
  ];

  const templateCreateList: ILabelValue[] = [
    {
      label: "数据中心",
      value: detail.dataCenter || '-',
    },
    {
      label: "业务类型",
      value: workType || '-',
    },
    {
      label: "业务等级",
      value: LEVEL_MAP[Number(detail.level) - 1]?.label || '-',
    },
    {
      label: "分区字段",
      value: detail.dateField || '-',
    },
    {
      label: "描述",
      value: detail.desc || '-',
    },
    {
      label: "数据大小(GB)",
      value: detail.diskQuota || '-',
    },
    {
      label: "保存周期",
      value: detail.expireTime === -1 ? "永不过期" : detail.expireTime,
    },
    {
      label: "索引模板是否开启RollOver",
      value: !detail.disableIndexRollover ? '是' : '否',
    },
    {
      label: "热节点保存周期",
      value: detail.hotTime || '-',
    },
    {
      label: "主键字段",
      value: detail.idField || '-',
    },
    // {
    //   label: '成本部门ID',
    //   value: detail.libraDepartmentId,
    // }, {
    //   label: '成本部门',
    //   value: detail.libraDepartment,
    // },
    {
      label: "索引模板名称",
      value: detail.name || '-',
    },
    {
      label: "所属集群",
      value: detail.clusterLogicName || '-',
    },
    {
      label: "业务负责人",
      value: detail.responsible || '-',
    },
    {
      label: "routing字段",
      value: detail.routingField || '-',
    },
  ];

  const templateAuthList: ILabelValue[] = [
    {
      label: "模板ID",
      value: detail.id || '-',
    },
    {
      label: "模板名称",
      value: detail.name || '-',
    },
    {
      label: "权限类型",
      value: authCodeText || '-',
    },
    {
      label: "负责人",
      value: detail.responsible || '-',
    },
  ];

  const templateIndecreaseList: ILabelValue[] = [
    {
      label: "使用CPU",
      value: detail.actualCpuCount || '-',
    },
    {
      label: "磁盘使用容量（GB）",
      value: detail.actualDiskG || '-',
    },
    {
      label: "期望保存周期",
      value: detail.expectExpireTime || '-',
    },
    {
      label: "期望Quota",
      value: detail.expectQuota || '-',
    },
    {
      label: "保存周期（天）",
      value: detail.expireTime || '-',
    },
    // {
    //   label: 'force',
    //   value: detail.force,
    // },
    {
      label: "模版ID",
      value: detail.id || '-',
    },
    {
      label: "模版名称",
      value: detail.name || '-',
    },
    {
      label: "已有配额",
      value: detail.quota || '-',
    },
    {
      label: "CPU利用率(峰值)",
      value: detail.quotaCpuCount || '-',
    },
    {
      label: "磁盘配额",
      value: detail.quotaDiskG || '-',
    },
  ];

  const clusterList: ILabelValue[] = [
    {
      label: "物理集群ID",
      value: detail.phyClusterId || '-',
    },
    {
      label: "物理集群名称",
      value: detail.phyClusterName || '-',
    },
  ];

  const clusterOpUpdateList: ILabelValue[] = clusterList;

  const clusterOpRestartList: ILabelValue[] = clusterList;

  const clusterOpOfflineList: ILabelValue[] = [
    {
      label: "物理集群ID",
      value: detail.phyClusterId || '-',
    },
    {
      label: "物理集群名称",
      value: detail.phyClusterName || '-',
    },
  ];

  const masterNode = detail?.roleClusterHosts?.filter(
    (ele: IRoleIpList) => ele.role === "masternode"
  );
  const clientNode = detail?.roleClusterHosts?.filter(
    (ele: IRoleIpList) => ele.role === "clientnode"
  );
  const dataNode = detail?.roleClusterHosts?.filter(
    (ele: IRoleIpList) => ele.role === "datanode"
  );

  const clusterOpNewList: ILabelValue[] = [
    {
      label: "集群创建人",
      value: detail.creator || '-',
    },
    // {
    //   label: '数据中心',
    //   value: detail.dataCenter,
    // },
    {
      label: "Es版本",
      value: detail.esVersion || '-',
    },
    // {
    //   label: '机房',
    //   value: detail.idc,
    // },
    // {
    //   label: '机器节点',
    //   value: detail.machineSpec,
    // }, {
    //   label: '服务节点',
    //   value: detail.nsTree,
    // },
    {
      label: "物理集群名称",
      value: detail.phyClusterName || '-',
    },
    {
      label: "单节点实例数",
      value: detail.pidCount || '-',
    },
    // {
    //   label: '插件列表',
    //   value: detail.plugs,
    // },
    {
      label: "masterRole",
      value: (masterNode && masterNode[0]?.role) || '-',
    },
    {
      label: "master主机名称",
      value: (masterNode && masterNode[0]?.hostname) || '-',
    },
    {
      label: "clientRole",
      value: (clientNode && clientNode[0]?.role) || '-',
    },
    {
      label: "client主机名称",
      value: (clientNode && clientNode[0]?.hostname) || '-',
    },
    {
      label: "dataRole",
      value: (dataNode && dataNode[0]?.role) || '-',
    },
    {
      label: "data主机名称",
      value: (dataNode && dataNode[0]?.hostname) || '-',
    },
    {
      label: "备注",
      value: detail.desc || '-',
    },
  ];

  const opPidCount: ILabelValue[] = [
    {
      label: "单节点实例数",
      value: detail.pidCount || '-',
    },
  ];

  const clusterOpIndecreaseData: ILabelValue[] = [
    {
      label: "物理集群ID",
      value: detail.phyClusterId || '-',
    },
    {
      label: "物理集群名称",
      value: detail.phyClusterName || '-',
    },
    {
      label: "集群类型",
      value: VERSION_MAINFEST_TYPE[Number(detail.type)],
    },
    {
      label: "扩（缩）容",
      value: expandText || '-',
    },
  ];

  const clusterOpIndecreaseList =
    Number(detail.type) === 4
      ? clusterOpIndecreaseData.concat(opPidCount)
      : clusterOpIndecreaseData;

  const logicClusterAuthList: ILabelValue[] = [
    {
      label: "逻辑集群名称",
      value: detail.logicClusterName || '-',
    },
    {
      label: "逻辑集群ID",
      value: detail.logicClusterId || '-',
    },
    {
      label: "权限类型",
      value: authCodeMap[detail.authCode] || '-',
    },
  ];

  const logicClusterIndecreaseList: ILabelValue[] = [
    {
      label: "逻辑集群ID",
      value: detail.logicClusterId || '-',
    },
    {
      label: "data节点个数",
      value: detail.dataNodeNu || '-',
    },
    // {
    //   label: "data节点规格",
    //   value: detail.dataNodeSpec,
    // },
  ];

  const logicClusterPlugOperationList: ILabelValue[] = [
    {
      label: "逻辑集群ID",
      value: detail.logicClusterId || '-',
    },
    {
      label: "逻辑集群名称",
      value: detail.logicClusterName || '-',
    },
    {
      label: "扩缩容",
      value: detail.expandText || '-',
    },
    {
      label: "插件ID",
      value: detail.plugId || '-',
    },
    {
      label: "插件名称",
      value: detail.plugName || '-',
    },
    {
      label: "申请说明",
      value: detail.plugDesc || '-',
    },
  ];

  const logicClusterCreateList: ILabelValue[] = [
    // {
    //   label: "数据中心",
    //   value: detail.dataCenter || '-',
    // },
    {
      label: "集群名称",
      value: detail.name || '-',
    },
    {
      label: "业务等级",
      value: LEVEL_MAP[Number(detail.level) - 1]?.label || '-',
    },
    // {
    //   label: "插件ID列表",
    //   value: detail.plugins || '-',
    // },
    {
      label: "data节点个数",
      value: detail.dataNodeNu || '-',
    },
    // {
    //   label: "data节点规格",
    //   value: detail.dataNodeSpec,
    // },
    // {
    //   label: "成本部门ID",
    //   value: detail.libraDepartmentId,
    // },
    // {
    //   label: "成本部门",
    //   value: detail.libraDepartment,
    // },
    {
      label: "负责人",
      value: detail.responsible || '-',
    },
  ];

  const logicClusterTransfer: ILabelValue[] = [
    {
      label: "原所属项目ID",
      value: detail.sourceAppId || '-',
    },
    {
      label: "转让目标项目ID",
      value: detail.targetAppId || '-',
    },
  ];

  if (type === "appCreate") {
    return appCreateList;
  }

  if (type === "templateTransfer") {
    return templateTransferList;
  }

  if (type === "templateCreate") {
    return templateCreateList;
  }

  if (type === "templateAuth") {
    return templateAuthList;
  }

  if (type === "templateIndecrease") {
    return templateIndecreaseList;
  }

  if (type === "clusterOpUpdate") {
    return clusterOpUpdateList;
  }

  if (type === "clusterOpRestart") {
    return clusterOpRestartList;
  }

  if (type === "clusterOpOffline") {
    return clusterOpOfflineList;
  }

  if (type === "clusterOpNew") {
    return clusterOpNewList;
  }

  if (type === "clusterOpIndecrease") {
    return clusterOpIndecreaseList;
  }

  if (type === "logicClusterAuth") {
    return logicClusterAuthList;
  }

  if (type === "logicClusterIndecrease") {
    return logicClusterIndecreaseList;
  }

  if (type === "logicClusterPlugOperation") {
    return logicClusterPlugOperationList;
  }

  if (type === "logicClusterCreate") {
    return logicClusterCreateList;
  }

  if (type === "logicClusterTransfer") {
    return logicClusterTransfer;
  }

  return [] as ILabelValue[];
};

export const getAddConfigInfoColumns = (
  type: string,
  setDrawerId: Function
): any => {
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
