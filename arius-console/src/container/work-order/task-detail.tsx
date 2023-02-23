import * as React from "react";
import { InfoItem } from "component/info-item";
import { TASK_MENU_MAP, TASK_TAB_LIST, DESC_LIST } from "./config";
import Url from "lib/url-parser";
import { DcdrPlanSpeed } from "./dcdr-plan-speed";
import { PageHeader, Tag } from "antd";
import { Menu } from "knowdesign";
import { IDcdrStepDetailInfo } from "typesPath/task-types";
import { DCDR_TASK_STATUS_TYPE_MAP } from "constants/status-map";
import { connect } from "react-redux";
import * as actions from "actions";
import { TaskState } from "store/type";
import { getTaskBaseInfo } from "api/task-api";
import "./index.less";

const mapStateToProps = (state) => ({
  task: state.task,
});

const connects: Function = connect;
@connects(mapStateToProps)
export class TaskDetail extends React.Component<{
  dispatch: any;
  task: TaskState;
}> {
  public id: number;
  public taskType: number;
  public index: number;
  public dcdrInfo: IDcdrStepDetailInfo;
  public status: string;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.id = Number(url.search.taskid);
    this.taskType = Number(url.search.type);
    this.status = url.search.status;
    if (this.taskType === 10) {
      const str = decodeURI(url.search?.dcdr_info);
      this.dcdrInfo = JSON.parse(str || "{}") as IDcdrStepDetailInfo;
      this.props.dispatch(actions.setDcdrStepDetail(this.dcdrInfo));
    } else {
      this.props.dispatch(actions.setTaskBaseLoading(true));
      getTaskBaseInfo(this.id).then((res) => {
        this.props.dispatch(actions.setTaskBaseInfo(res));
        this.props.dispatch(actions.setTaskBaseLoading(false));
      });
    }
    this.judgeHashValue();
    window.addEventListener("hashchange", () => {
      this.props.dispatch(actions.setTaskMenu());
    });
  }

  public judgeHashValue = () => {
    const { task } = this.props;
    const menu = task.menu;
    const currHash = window.location.hash;

    if (currHash !== menu) {
      this.props.dispatch(actions.setTaskMenu());
    }
  };

  public renderContent = () => {
    const { task } = this.props;
    const currentHash = window.location.hash.replace("#", "") || "base";
    const menu = task.menu !== currentHash ? currentHash : task.menu;
    return TASK_MENU_MAP.get(menu)?.content;
  };

  public renderPageHeader() {
    // 1：执行中 2：执行成功 3：执行失败
    const tagArr = [
      {
        color: "blue",
        text: "执行中",
      },
      {
        color: "green",
        text: "成功",
      },
      {
        color: "red",
        text: "失败",
      },
    ];
    let status = 0;
    switch (this.status) {
      case DCDR_TASK_STATUS_TYPE_MAP[0]:
        status = 1;
        break;
      case DCDR_TASK_STATUS_TYPE_MAP[1]:
        status = 3;
        break;
      case DCDR_TASK_STATUS_TYPE_MAP[2]:
        status = 0;
        break;
      default:
        return;
    }
    const { task } = this.props;
    return (
      <PageHeader
        className="detail-header"
        backIcon={false}
        tags={<Tag color={tagArr[status].color}>{tagArr[status].text}</Tag>}
        title={task.dcdrStepDetail.logicTemplateName + "主从切换"}
      >
        {DESC_LIST.map((row, index) => (
          <InfoItem
            key={index}
            label={row.label}
            value={row.render ? row.render(task.dcdrStepDetail?.[row.key]) : `${task.dcdrStepDetail?.[row.key] || ""}`}
            width={250}
          />
        ))}
      </PageHeader>
    );
  }

  public render() {
    const { task } = this.props;
    return (
      <>
        {this.taskType !== 10 ? (
          <div className="hash-menu-container menu-container task-detail">
            <Menu selectedKeys={[task.menu]} mode="horizontal" onClick={(e: any) => (window.location.hash = e.key)}>
              {TASK_TAB_LIST.map((d) => (
                <Menu.Item key={d.key}>{d.name}</Menu.Item>
              ))}
            </Menu>
          </div>
        ) : (
          this.renderPageHeader()
        )}
        <div className="detail-wrapper task-detail-wrapper">{this.taskType !== 10 ? this.renderContent() : <DcdrPlanSpeed />}</div>
      </>
    );
  }
}
