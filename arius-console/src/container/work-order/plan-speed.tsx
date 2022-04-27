import * as React from 'react';
import { carryTask, getPlanSpeedColumns } from './config';
import { SPIT_STYLE_MAP, TASK_STATUS_TYPE_MAP } from 'constants/status-map';
import { tableFilter } from 'lib/utils';
import Url from 'lib/url-parser';
import './index.less';
import  { Table, Button, Progress, Popconfirm, Tooltip, Collapse, Modal } from 'antd';
import { INodeTask, ITask, ITaskDetail, ITaskNodes } from 'typesPath/task-types';
import { connect } from "react-redux";
import * as actions from "actions";
import { TaskState } from "store/type";
import { cancalTask, continueTask, getTaskDetail, pauseTask } from "api/task-api";
import { Dispatch } from "redux";

const { Panel } = Collapse;

const mapStateToProps = (state) => ({
  task: state.task,
});

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

const connects: Function = connect;
@connects(mapStateToProps, mapDispatchToProps)
export class PlanSpeed extends React.Component<{ task: TaskState; setModalId?: Function }> {
  public timer = null as any;

  public state = {
    searchKey: "",
    taskDetail: null as ITaskDetail,
    loading: false,
    taskNodes: [] as ITaskNodes[],
  };
  public id: number = null;
  public taskId: number = null;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.id = Number(url.search.id);
    this.taskId = Number(url.search.taskid);
  }

  public getData = (origin?: any[]) => {
    let { searchKey } = this.state;
    searchKey = (searchKey + "").trim().toLowerCase();
    const data = searchKey
      ? origin.filter(
          (d) => (d.id + "").toLowerCase().includes(searchKey as string) || d.hostname?.toLowerCase().includes(searchKey as string)
        )
      : origin;

    return data;
  };

  public componentDidMount() {
    this.reloadData();
  }

  public reloadData = () => {
    this.refreshData(this.id);
  };

  public refreshData(id: number) {
    this.getTaskDetail(id)
      .then((data: { taskDetail: ITaskDetail; taskNodes: ITaskNodes[] }) => {
        this.setState({
          taskDetail: data.taskDetail,
          taskNodes: data.taskNodes,
          loading: false,
        });
      })
      .catch(() => {
        clearInterval(this.timer);
      });
    setTimeout(() => this.iTimer(id), 0);
  }

  public iTimer = (id: number) => {
    this.timer = setInterval(() => {
      if (this.state.taskDetail && (this.state.taskDetail?.status === 'running' || this.state.taskDetail?.status === 'unknown')) {
      this.getTaskDetail(id, 'update').then((data: {taskDetail: ITaskDetail, taskNodes: ITaskNodes[]}) => {
        this.setState({
          taskDetail: data.taskDetail,
          taskNodes: data.taskNodes,
          loading: false
        });
      }).catch(() => { clearInterval(this.timer); });
      } else { clearInterval(this.timer); }
    }, 5 * 1 * 1000);
  }

  public getTaskDetail = (id: number, text?: string) => {
    if (text) {
      this.setState({ loading: false });
    } else {
      this.setState({ loading: true });
    }
    return getTaskDetail(id).then(this.setTaskDetail);
  };

  public setTaskDetail(data: ITaskDetail) {
    const obj = data.roleNameTaskDetailMap;
    const nodes = Object.keys(obj)
      .sort()
      .map((key) => key);
    const arr = nodes.map((ele) => {
      if (ele === "masternode") {
        return {
          header: "master-node",
          data: obj?.masternode || [],
          key: "master",
        };
      } else if (ele === "clientnode") {
        return {
          header: "client-node",
          data: obj?.clientnode || [],
          key: "client",
        };
      } else if (ele === "datanode") {
        return {
          header: "data-node",
          data: obj?.datanode || [],
          key: "data",
        };
      } else if (ele === "coldnode") {
        return {
          header: "cold-node",
          data: obj?.coldnode || [],
          key: "cold",
        };
      }
    });
    return {
      taskDetail: data,
      taskNodes: arr,
      key: "data",
    };
  }

  public getOpBtns = () => {
    return null;
  };

  // 组件清除时清除定时器
  public componentWillUnmount() {
    clearInterval(this.timer);
  }

  public confirmFn = (text: string, taskId: number, id: number, fn: Function) => {
    Modal.confirm({
      title: `确定${text}任务${taskId}?`,
      // icon: <DeleteOutlined style={{ color: "red" }} />,
      width: 500,
      okText: "确定",
      cancelText: "取消",
      onOk() {
        fn(id)
      },
    });
  }

  public pauseTask = (id: number) => {
    pauseTask(id).then(() => this.refreshData(id));
  };

  public continueTask = (id: number) => {
    continueTask(id).then(() => this.refreshData(id));
  };

  public cancalTask = (id: number) => {
    cancalTask(id).then(() => this.refreshData(id));
  };

  public renderInnerOperation = (): JSX.Element => {
    return <></>;
  };

  public renderOperationPanel = (): JSX.Element => {
    const taskObj = this.state.taskDetail;
    let pattern = null as any;
    SPIT_STYLE_MAP.forEach((ele, index) => {
      if (ele.type === taskObj?.status) {
        pattern = ele;
      }
    });
    return (
      <>
        <div className="plan-speed-head">
          <div className="speed-head-left">
            <span className="head-left-top">
              <Progress percent={taskObj?.percent / 100} strokeColor={pattern?.color} className="left-top-pro" />
              <i className={`left-top-text ${pattern?.back}`}>{pattern?.text}</i>
            </span>
            <ul className="head-left-ul">
              <li>
                <span>
                  <div className="spot running" />
                  总数：{taskObj?.sum}
                </span>
                <span>
                  <div className="spot success" />
                  成功：{taskObj?.success}
                </span>
                <span>
                  <div className="spot failed" />
                  失败：{taskObj?.failed}
                </span>
                <span>
                  <div className="spot creating" />
                  执行中：{taskObj?.creating}
                </span>
                {taskObj?.status === "cancel" ? (
                  <span>
                    <div className="spot waiting" />
                    已取消：{taskObj?.cancel}
                  </span>
                ) : (
                  <span>
                    <div className="spot waiting" />
                    待执行：{taskObj?.waiting}
                  </span>
                )}
                <span>
                  <div className="spot ignore" />
                  已忽略：{taskObj?.ignore}
                </span>
              </li>
            </ul>
          </div>
          <div>
            {taskObj?.status === 'waiting' &&
                <Button type="primary" className="mr-10" onClick={() => carryTask(this.id, taskObj?.orderType, this.reloadData, this.taskId)}>执行</Button>
            }
            {taskObj?.status === 'running' &&
                <Button type="primary" className="mr-10" onClick={() => this.confirmFn('暂停', this.taskId, this.id, this.pauseTask)}>暂停</Button>
            }
            {taskObj?.status === 'pause' &&
                <Button type="primary" className="mr-10"  onClick={() => this.confirmFn('继续', this.taskId, this.id, this.continueTask)}>继续</Button>
            }
            {(taskObj?.status === 'waiting' || taskObj?.status === 'running' || taskObj?.status === 'pause' || taskObj?.status === 'failed') &&
                <Button type="primary" className="mr-10" onClick={() => this.confirmFn('取消', this.taskId, this.id, this.cancalTask)}>取消</Button>
            }
          </div>
        </div>
      </>
    );
  };

  public getColumns(data: INodeTask[], type: string) {
    const columns = getPlanSpeedColumns(this.props.setModalId);

    const statusType = Object.assign({
      title: "节点状态",
      dataIndex: "status",
      key: "status",
      width: "10%",
      onFilter: (value: string, record: ITask) => record.status === value,
      filters: tableFilter<INodeTask>(data, "status", TASK_STATUS_TYPE_MAP),
      render: (text: any) => {
        return (
          <Tooltip placement="bottomLeft" title={TASK_STATUS_TYPE_MAP[text]}>
            {TASK_STATUS_TYPE_MAP[text]}
          </Tooltip>
        );
      },
    });

    columns.splice(3, 0, statusType);
    return columns;
  }

  public renderNodeTable = (data: INodeTask[], type: string) => {
    return (
      <>
        <Table rowKey="id" loading={this.state.loading} dataSource={this.getData(data)} columns={this.getColumns(data, type)} />
      </>
    );
  };

  public renderTable = () => {
    return (
      <>
        <Collapse bordered={false} defaultActiveKey={this.state.taskNodes?.map((row) => row.key)}>
          {this.state.taskNodes?.map((item) => {
            return (
              <Panel header={item.header} key={item.key}>
                {this.renderNodeTable(item.data, item.key)}
              </Panel>
            );
          })}
        </Collapse>
      </>
    );
  };

  public render() {
    return (
      <>
        {this.renderOperationPanel()}
        <div className="table-wrapper no-padding">{this.state.taskNodes?.length ? this.renderTable() : ""}</div>
      </>
    );
  }
}
