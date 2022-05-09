import React from "react";
import { transTimeFormat, transTimeStamp } from "lib/utils";
import { Link } from "react-router-dom";
import { IColumnsType } from "component/dantd/query-form/QueryForm";
import { Modal, DatePicker, message } from "antd";
import { IStatusMap } from "typesPath/base-types";
import { renderOperationBtns } from "container/custom-component";
import { taskStatus, taskDo, jobStop } from "api/Scheduling";
import moment from "moment";
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
      {
        label: "调度详情",
        clickFunc: () => {
          showDetail(record);
        },
      },
      {
        label: "执行日志",
        clickFunc: (record: any) => {
          showLog(record);
        },
      },
      {
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
      width: 70,
    },
    {
      title: "任务描述",
      dataIndex: "taskDesc",
    },
    {
      title: "调度地址",
      dataIndex: "workerIp",
    },
    {
      title: "调度时间",
      dataIndex: "createTime",
      render: (t: string) => transTimeFormat(t),
    },
    {
      title: "调度结果",
      dataIndex: "status",
      width: 100,
      render: (text) => {
        return logStatusMap[text];
      },
    },
    {
      title: "执行开始时间",
      dataIndex: "startTime",
      render: (t: string) => (t ? transTimeFormat(t) : "-"),
    },
    {
      title: "执行结束时间",
      dataIndex: "endTime",
      render: (t: string) => (t ? transTimeFormat(t) : "-"),
    },
    {
      title: "执行结果",
      dataIndex: "result",
      width: 125,
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
      width: 200,
      render: (text: any, record: any) => {
        const btns: any = getCongigBtnList(reloadData, showDetail, showLog, record);
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return orderColumns;
};

export const getSchedulingLogQueryXForm = (isDetail) => {
  const log = [
    {
      dataIndex: "taskId",
      title: "任务ID",
      type: "input",
      placeholder: "请输入任务ID",
    },
    {
      // todo： 字段确认
      dataIndex: "taskDesc",
      title: "任务描述",
      type: "input",
      placeholder: "请输入任务描述",
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
      component: (
        <DatePicker.RangePicker
          style={{ width: "100%" }}
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
    },
    {
      dataIndex: "taskDesc",
      title: "任务描述",
      type: "input",
      placeholder: "请输入任务描述",
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
    content: `确定${record.status ? "暂停" : "恢复"}调度任务${record.id}？`,
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
    content: `确定执行调度任务${record.id}？`,
    onOk() {
      taskDo(record.taskCode).then((res) => {
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

export const getTaskListColumns = (reloadData: Function, showDetail: Function) => {
  const getCongigBtnList = (reloadData: Function, record: any) => {
    return [
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
    ];
  };
  const orderColumns = [
    {
      title: "任务ID",
      dataIndex: "id",
      width: 90,
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
      title: "任务描述",
      dataIndex: "taskDesc",
      width: "20vw",
      render: (text) => <div style={{ wordWrap: "break-word", wordBreak: "break-word" }}>{text}</div>,
    },
    {
      title: "JobHandler",
      dataIndex: "className",
      width: "20vw",
      render: (text) => <div style={{ wordWrap: "break-word", wordBreak: "break-word" }}>{text}</div>,
    },
    {
      title: "Corn",
      dataIndex: "cron",
    },
    {
      title: "负责人",
      dataIndex: "owner",
    },
    {
      title: "状态",
      dataIndex: "status",
      width: 90,
      render: (t: number) => {
        if (StatusMap[t] === "Normal") {
          return (
            <div>
              <svg className="icon" aria-hidden="true" style={{ marginRight: 5 }}>
                <use xlinkHref="#iconwancheng"></use>
              </svg>
              {StatusMap[t]}
            </div>
          );
        }
        return (
          <div>
            <svg className="icon" aria-hidden="true" style={{ marginRight: 5 }}>
              <use xlinkHref="#iconzanting"></use>
            </svg>
            {StatusMap[t]}
          </div>
        );
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      width: 160,
      render: (text: any, record: any) => {
        const btns: any = getCongigBtnList(reloadData, record);
        return (
          <div>
            {renderOperationBtns(btns, record)}
            <Link to={`/scheduling/log/detail?taskId=${record.id}`} style={{ marginLeft: 10 }}>
              查看日志
            </Link>
          </div>
        );
      },
    },
  ];
  return orderColumns;
};
