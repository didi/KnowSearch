import React, { useState, useEffect } from "react";
import { TaskApi, getPlanSpeedColumns } from "./config";
import { SPIT_STYLE_MAP, TASK_STATUS_TYPE_MAP, TASK_STATUS_NUMBER_MAP } from "constants/status-map";
import { Table, Button, Progress, Tooltip, Collapse, Modal } from "antd";
import { INodeTask, ITaskDetail } from "typesPath/task-types";
import { getTaskDetail, actionTask, getTaskBaseInfo } from "api/task-api";
import { connect } from "react-redux";
import * as actions from "actions";
import { Dispatch } from "redux";
import Url from "lib/url-parser";
import "./index.less";

const { Panel } = Collapse;

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

export const PlanSpeed = connect(
  null,
  mapDispatchToProps
)((props: any) => {
  const [loading, setLoading] = useState(false);
  const [taskNodes, setTaskNodes] = useState([]);
  const [taskDetail, setTaskDetail] = useState({} as ITaskDetail);
  const [status, setStatus] = useState(Url().search.status);

  const taskId = Number(Url().search.taskid);

  let timer = null;

  useEffect(() => {
    reloadData();
    return () => {
      clearInterval(timer);
    };
  }, []);

  const reloadData = async () => {
    setLoading(true);
    let detail = await getTaskBaseInfo(taskId);
    let res = await getTaskDetail(taskId);
    let taskNodes = [];
    let taskDetail = { sum: res?.length || 0, success: 0, failed: 0, running: 0, waiting: 0, cancel: 0, ignore: 0 };
    (res || []).forEach((item) => {
      let node = { header: item?.groupName, data: [{ ...item }], key: item?.groupName };
      let status = TASK_STATUS_NUMBER_MAP[item?.status];
      taskDetail[status] += 1;
      let index = -1;
      for (let i = 0; i < taskNodes.length; i++) {
        if (taskNodes[i]?.header === item?.groupName) {
          index = i;
          break;
        }
      }
      if (index === -1) {
        taskNodes.push(node);
      } else {
        taskNodes[index].data.push(item);
      }
    });
    setStatus(detail?.status);
    setTaskNodes(taskNodes);
    setTaskDetail(taskDetail);
    setLoading(false);
    if (detail?.status === "running") {
      timer = setTimeout(() => reloadData(), 5000);
    } else {
      clearInterval(timer);
    }
  };

  const confirmFn = (text: string, taskId: number, fn: Function) => {
    let title = decodeURI(Url()?.search?.title);
    Modal.confirm({
      title: `确定${text}任务${title}?`,
      width: 500,
      okText: "确定",
      cancelText: "取消",
      onOk() {
        fn(taskId);
      },
    });
  };

  const pauseTask = (id: number) => {
    actionTask("pause", id).then(() => reloadData());
  };

  const continueTask = (id: number) => {
    actionTask("start", id).then(() => reloadData());
  };

  const cancalTask = (id: number) => {
    actionTask("cancel", id).then(() => reloadData());
  };

  const getColumns = () => {
    const columns = getPlanSpeedColumns(props.setModalId, reloadData);
    const statusType = Object.assign({
      title: "节点状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (val: any) => {
        let text = TASK_STATUS_NUMBER_MAP[val];
        return (
          <Tooltip placement="bottomLeft" title={TASK_STATUS_TYPE_MAP[text]}>
            {TASK_STATUS_TYPE_MAP[text]}
          </Tooltip>
        );
      },
    });
    columns.splice(2, 0, statusType);
    return columns;
  };

  const renderNodeTable = (data: INodeTask[], type: string) => {
    return <Table rowKey="host" loading={loading} dataSource={data} columns={getColumns()} />;
  };

  const renderTable = () => {
    return (
      <>
        <Collapse bordered={false} defaultActiveKey={taskNodes?.map((row) => row.key)}>
          {taskNodes?.map((item) => {
            return (
              <Panel header={item.header} key={item.key}>
                {renderNodeTable(item.data, item.key)}
              </Panel>
            );
          })}
        </Collapse>
      </>
    );
  };

  const renderOperationPanel = (): JSX.Element => {
    let pattern = null as any;
    SPIT_STYLE_MAP.forEach((ele) => {
      if (ele.type === status) {
        pattern = ele;
      }
    });
    return (
      <>
        <div className="plan-speed-head">
          <div className="speed-head-left">
            <span className="head-left-top">
              <Progress
                percent={
                  status === "running"
                    ? Math.floor(((taskDetail?.success + taskDetail?.failed) / taskDetail?.sum) * 10000) / 100
                    : status === "success"
                    ? 100
                    : 0
                }
                strokeColor={pattern?.color}
                className="left-top-pro"
              />
              <i className={`left-top-text ${pattern?.back}`}>{pattern?.text}</i>
            </span>
            <ul className="head-left-ul">
              <li>
                <span>
                  <div className="spot running" />
                  总数：{taskDetail?.sum || 0}
                </span>
                <span>
                  <div className="spot success" />
                  成功：{taskDetail?.success || 0}
                </span>
                <span>
                  <div className="spot failed" />
                  失败：{taskDetail?.failed || 0}
                </span>
                <span>
                  <div className="spot creating" />
                  执行中：{taskDetail?.running || 0}
                </span>
                {status === "cancel" ? (
                  <span>
                    <div className="spot waiting" />
                    已取消：{taskDetail?.cancel || 0}
                  </span>
                ) : (
                  <span>
                    <div className="spot waiting" />
                    待执行：{taskDetail?.waiting || 0}
                  </span>
                )}
              </li>
            </ul>
          </div>
          <div>
            {status === "waiting" && (
              <Button
                type="primary"
                className="mr-10 button-styles"
                onClick={() => TaskApi.executeTask(reloadData, taskId, decodeURI(Url()?.search?.title))}
              >
                执行
              </Button>
            )}
            {status === "running" && (
              <Button type="primary" className="mr-10 button-styles" onClick={() => confirmFn("暂停", taskId, pauseTask)}>
                暂停
              </Button>
            )}
            {status === "pause" && (
              <Button type="primary" className="mr-10 button-styles" onClick={() => confirmFn("继续", taskId, continueTask)}>
                继续
              </Button>
            )}
            {(status === "waiting" || status === "running" || status === "pause" || status === "failed") && (
              <Button type="primary" className="mr-10 button-styles" onClick={() => confirmFn("取消", taskId, cancalTask)}>
                取消
              </Button>
            )}
          </div>
        </div>
      </>
    );
  };
  return (
    <>
      {renderOperationPanel()}
      <div className="table-wrapper no-padding">{taskNodes?.length ? renderTable() : ""}</div>
    </>
  );
});
