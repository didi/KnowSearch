import React from "react";
import { transTimeFormat } from "lib/utils";
import { IColumnsType } from "component/dantd/query-form/QueryForm";
import { Modal, message } from "antd";
import { IStatusMap } from "typesPath/base-types";
import { renderOperationBtns } from "container/custom-component";
import { taskStatus, taskDo, jobStop, deleteTask } from "api/Scheduling";
import DRangeTime from "../../d1-packages/d-range-time";
import { regNonnegativeInteger } from "constants/reg";
import { XModal } from "component/x-modal";
import { hasOpPermission } from "lib/permission";
import { ShceduleLogPermissions } from "constants/permission";

const { confirm } = Modal;

export const StatusMap = {
  0: "Paused",
  1: "Normal",
} as IStatusMap;

export const logStatusMap = {
  0: "调度启动中",
  1: "调度运行中",
  2: "调度成功",
  3: "调度失败",
  4: "调度取消",
};

export const resultMap = {
  "-1": "失败",
  0: "运行中",
  1: "成功",
};

export const mockData = [
  {
    id: 1001,
    text: "平滑迁移",
    taskType: "10.166.189.142:9999",
    time: "2021-08-16 13:15:00",
    result: "成功",
    startTime: "2021-08-16 13:15:00",
    endTime: "2021-08-16 13:15:00",
  },
];

export const getSchedulingLogColumns = (reloadData: Function, showDetail: Function, showLog: Function) => {
  const getCongigBtnList = (reloadData: any, showDetail: Function, showLog: Function, record) => {
    return [
      hasOpPermission(ShceduleLogPermissions.PAGE, ShceduleLogPermissions.DETAIL) && {
        label: "调度详情",
        clickFunc: () => {
          showDetail(record);
        },
      },
      hasOpPermission(ShceduleLogPermissions.PAGE, ShceduleLogPermissions.LOG) && {
        label: "执行日志",
        clickFunc: (record: any) => {
          showLog(record);
        },
      },
      hasOpPermission(ShceduleLogPermissions.PAGE, ShceduleLogPermissions.END_MISSION) && {
        label: `${record.status === 2 && record.result.indexOf("0") !== -1 ? "终止任务" : ""}`,
        clickFunc: () => {
          showStop(record, reloadData);
        },
      },
    ];
  };
  const orderColumns = [
    {
      title: "任务ID",
      dataIndex: "taskId",
      key: "taskId",
      width: 70,
    },
    {
      title: "任务名称",
      dataIndex: "taskDesc",
      key: "taskDesc",
      width: 130,
      lineClampTwo: true,
      needTooltip: true,
    },
    {
      title: "调度地址",
      dataIndex: "workerIp",
      key: "workerIp",
      width: 120,
      lineClampOne: true,
      needTooltip: true,
    },
    {
      title: "调度时间",
      dataIndex: "createTime",
      key: "createTime",
      sorter: true,
      width: 150,
      render: (t: string) => transTimeFormat(t),
    },
    {
      title: "调度结果",
      dataIndex: "status",
      key: "status",
      width: 90,
      sorter: true,
      render: (text) => {
        return logStatusMap[text];
      },
    },
    {
      title: "执行开始时间",
      dataIndex: "startTime",
      key: "startTime",
      width: 150,
      sorter: true,
      render: (t: string) => transTimeFormat(t),
    },
    {
      title: "执行结束时间",
      dataIndex: "endTime",
      key: "endTime",
      width: 150,
      sorter: true,
      render: (t: string) => transTimeFormat(t),
    },
    {
      title: "执行结果",
      dataIndex: "result",
      key: "result",
      width: 90,
      sorter: true,
      render: (text) => {
        if (text) {
          const obj = JSON.parse(text);
          return resultMap[obj.code];
        }
        return "-";
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      filterTitle: true,
      fixed: "right",
      width: 220,
      render: (text: any, record: any) => {
        const btns: any = getCongigBtnList(reloadData, showDetail, showLog, record);
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return orderColumns;
};

export const getSchedulingLogQueryXForm = (isDetail, handleTimeChange) => {
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
  const log = [
    {
      dataIndex: "taskId",
      title: "任务ID",
      type: "input",
      placeholder: "请输入任务ID",
      rules: [
        {
          required: false,
          validator: (rule: any, value: string) => {
            if (value && !new RegExp(regNonnegativeInteger).test(value)) {
              return Promise.reject(new Error("请输入正确格式"));
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
      // todo： 字段确认
      dataIndex: "taskDesc",
      title: "任务名称",
      type: "input",
      placeholder: "请输入任务名称",
    },
  ] as IColumnsType[];
  let formMap = [
    {
      dataIndex: "taskStatus",
      title: "调度状态",
      type: "select",
      options: Object.keys(logStatusMap).map((key) => ({
        value: key,
        title: logStatusMap[key],
      })),
      placeholder: "请选择工单状态",
    },
    {
      dataIndex: "createTime",
      title: "调度时间",
      type: "custom",
      component: <DRangeTime timeChange={handleTimeChange} popoverClassName="dashborad-popover" customTimeOptions={customTimeOptions} />,
    },
  ] as IColumnsType[];
  if (!isDetail) {
    formMap = [...log, ...formMap];
  }
  return formMap;
};

export const getTaskListQueryXForm = () => {
  let formMap = [
    {
      dataIndex: "taskId",
      title: "任务ID",
      type: "input",
      placeholder: "请输入任务ID",
      rules: [
        {
          required: false,
          validator: (rule: any, value: string) => {
            if (value && !new RegExp(regNonnegativeInteger).test(value)) {
              return Promise.reject(new Error("请输入正确格式"));
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
      dataIndex: "taskDesc",
      title: "任务名称",
      type: "input",
      placeholder: "请输入任务名称",
    },
    {
      dataIndex: "className",
      title: "JobHandler",
      type: "input",
      placeholder: "请输入JobHandler",
    },
    {
      dataIndex: "taskStatus",
      title: "状态",
      type: "select",
      options: Object.keys(StatusMap).map((key) => ({
        value: Number(key),
        title: StatusMap[key],
      })),
      placeholder: "请选择",
    },
  ] as IColumnsType[];
  return formMap;
};

// 改变状态
export const showTaskStatus = (record, reloadData: Function) => {
  confirm({
    title: "提示",
    content: `确定${record.status ? "暂停" : "恢复"}${record.taskDesc}【${record.id}】？`,
    onOk() {
      taskStatus(record.taskCode, record.status ? 0 : 1).then((res) => {
        if (res) {
          message.success("操作成功");
        } else {
          message.warning("操作失败");
        }
        reloadData();
      });
    },
  });
};

// 执行操作
export const showTaskDo = (record, reloadData: Function) => {
  confirm({
    title: "提示",
    content: `确定执行${record.taskDesc}【${record.id}】？`,
    onOk() {
      taskDo(record.taskCode).then((res) => {
        message.success("操作成功");
        reloadData();
      });
    },
  });
};

// 执行操作
export const showStop = (record, reloadData: Function) => {
  confirm({
    title: "提示",
    content: `确定终止任务？`,
    onOk() {
      jobStop(record.jobCode).then((res) => {
        if (res) {
          message.success("操作成功");
        } else {
          message.warning("操作失败");
        }
        reloadData();
      });
    },
  });
};

export const getTaskListColumns = ({ reloadData, showDetail, setModalId, history, clusterList }) => {
  const getCongigBtnList = (reloadData: Function, record: any) => {
    let btnsList = [
      {
        label: "执行",
        clickFunc: () => {
          showTaskDo(record, reloadData);
        },
      },
      {
        label: `${record.status == 1 ? "暂停" : "恢复"}`,
        clickFunc: () => {
          showTaskStatus(record, reloadData);
        },
      },
      {
        label: "复制",
        clickFunc: () => {
          let params = { ...record };
          params.taskDesc = params.taskDesc + "-1";
          setModalId("copyTask", params, reloadData);
        },
      },
      {
        label: "编辑",
        clickFunc: () => {
          let params = { ...record, clusterList };
          setModalId("editTask", params, reloadData);
        },
      },
      {
        label: "查看日志",
        clickFunc: () => {
          history.push(`/scheduling/log/detail?taskId=${record.id}`);
        },
      },
    ];
    let deletebtn = {
      label: "删除",
      clickFunc: () => {
        XModal({
          type: "delete",
          title: `确定删除调度任务${record?.taskDesc || ""}？`,
          onOk: async () => {
            await deleteTask(record.taskCode);
            message.success("删除成功");
            reloadData();
          },
        });
      },
    };
    if (record.del) {
      btnsList.push(deletebtn);
    }
    return btnsList;
  };

  const orderColumns = [
    {
      title: "任务ID",
      dataIndex: "id",
      width: 80,
      render: (text: any, record: any) => {
        const btns: any = [
          {
            label: text,
            clickFunc: () => {
              showDetail(record);
            },
          },
        ];
        return renderOperationBtns(btns, record);
      },
    },
    {
      title: "任务名称",
      dataIndex: "taskDesc",
      width: 120,
      lineClampTwo: true,
      needTooltip: true,
      render: (text) => <div style={{ wordWrap: "break-word", wordBreak: "break-word" }}>{text}</div>,
    },
    {
      title: "JobHandler",
      dataIndex: "className",
      width: 130,
      lineClampTwo: true,
      needTooltip: true,
      render: (text) => <div style={{ wordWrap: "break-word", wordBreak: "break-word" }}>{text}</div>,
    },
    {
      title: "Corn",
      dataIndex: "cron",
      width: 130,
    },
    {
      title: "责任人",
      dataIndex: "owner",
      width: 120,
    },
    {
      title: "执行器",
      dataIndex: "workerIps",
      width: 130,
      lineClampTwo: true,
      needTooltip: true,
      render: (ips) => {
        return ips?.join("，") || "-";
      },
    },
    {
      title: "目标集群",
      dataIndex: "params",
      width: 120,
      lineClampTwo: true,
      needTooltip: true,
      render: (cluster) => {
        if (!cluster) return "-";
        let esClusterNamesList = JSON.parse(cluster)?.esClusterNames;
        let filterCluster = esClusterNamesList.filter((item) => !clusterList.includes(item));
        if (!filterCluster?.length && esClusterNamesList.length === clusterList.length) {
          return "ALL";
        }
        return esClusterNamesList?.join("，") || "-";
      },
    },
    {
      title: "状态",
      dataIndex: "status",
      width: 120,
      render: (t: number) => {
        return (
          <div>
            <svg className="icon" aria-hidden="true" style={{ marginRight: 5 }}>
              <use xlinkHref={`${StatusMap[t] === "Normal" ? "#iconwancheng" : "#iconzanting"}`}></use>
            </svg>
            {StatusMap[t]}
          </div>
        );
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      filterTitle: true,
      width: 190,
      render: (text: any, record: any) => {
        const btns: any = getCongigBtnList(reloadData, record);
        return <div>{renderOperationBtns(btns, record)}</div>;
      },
    },
  ];
  return orderColumns;
};
