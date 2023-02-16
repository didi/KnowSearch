import React from "react";
import LineChart from "../echarts";
import { NavRouterLink } from "container/custom-component";
import { createOption } from "./constants";
import InfoTooltip from "component/infoTooltip";
import "./index.less";

interface IProps {
  type: string;
  dataSource: {
    greenNum: number;
    greenPercent: number;
    greenClusterList: string[];
    redClusterList: string[];
    redNum: number;
    redPercent: number;
    timestamp: number;
    totalNum: number;
    unknownClusterList: string[];
    unknownNum: number;
    unknownPercent: number;
    yellowClusterList: string[];
    yellowNum: number;
    yellowPercent: number;
  };
  loading?: boolean;
  dictionary?: any;
}

const imgSrc = require("./../../../../assets/empty.png");

export class PieChart extends React.Component<IProps> {
  className: string;
  textStyle: any;
  public constructor(props: IProps) {
    super(props);
    this.className = `${this.props.type}-piechart`;
    this.textStyle = {
      fontFamily: "PingFangSC-Regular",
      fontSize: 12,
      color: "#495057",
      letterSpacing: 0,
      textAlign: "justify",
      lineHeight: "20px",
    };
  }

  public state = {
    chartKey: "red",
  };

  public clickChart = (key: string) => {
    this.setState({ chartKey: key });
  };

  public renderChart = () => {
    const { dataSource } = this.props;
    let data = [
      { value: dataSource?.greenNum || 0, name: "健康集群", prrcent: dataSource?.greenPercent, key: "green" },
      { value: dataSource?.yellowNum || 0, name: "预警集群", prrcent: dataSource?.yellowPercent, key: "yellow" },
      { value: dataSource?.redNum || 0, name: "故障集群", prrcent: dataSource?.redPercent, key: "red" },
      { value: dataSource?.unknownNum || 0, name: "未知集群", prrcent: dataSource?.unknownPercent, key: "unknown" },
    ];
    const options = createOption(data, this.state.chartKey);
    return (
      <div style={{ margin: "31px 30px 0 0", float: "left" }}>
        <LineChart isResize={true} width={162} height={162} options={options} key="chart" onClick={this.clickChart} />
      </div>
    );
  };

  public renderContent = (list: any) => {
    let empty =
      list.length === 0 ? (
        <div className={`${this.className}-faulttask-content-empty`}>
          <div className="img">
            <img src={imgSrc} />
          </div>
          <div>
            <span>数据为空</span>
          </div>
        </div>
      ) : null;
    let content = (
      <ul>
        {(list || []).map((item: any, index: any) => {
          return (
            <li className={`${this.className}-faulttask-content-li`} key={index}>
              <NavRouterLink
                maxShowLength={28}
                needToolTip
                style={this.textStyle}
                element={item}
                href={`/indicators/cluster?cluster=${item}#overview`}
              />
            </li>
          );
        })}
      </ul>
    );
    return (
      <>
        {!this.props.loading && empty}
        {content}
      </>
    );
  };

  public renderGreenClusterList = () => {
    const { dataSource } = this.props;
    const list = dataSource?.greenClusterList || [];
    return (
      <div className={`${this.className}-faulttask`}>
        <div className={`${this.className}-faulttask-icon`}>
          <span className="icon iconfont icon-lv" style={{ margin: "0px 4px 0px 0px", color: "#34c28f" }}></span>
          <span className={`${this.className}-faulttask-icon-title`}>健康集群</span>
        </div>
        <div className={`${this.className}-faulttask-content`}>{this.renderContent(list)}</div>
      </div>
    );
  };

  public renderRedClusterList = () => {
    const { dataSource } = this.props;
    const list = dataSource?.redClusterList || [];
    return (
      <div className={`${this.className}-faulttask`}>
        <div className={`${this.className}-faulttask-icon`}>
          <svg className={`d1-layout-left-menus-icon`} style={{ margin: "0px 4px 0px 0px" }} aria-hidden="true">
            <use xlinkHref="#iconyijigaojing"></use>
          </svg>
          <span className={`${this.className}-faulttask-icon-title`}>故障集群</span>
        </div>
        <div className={`${this.className}-faulttask-content`}>{this.renderContent(list)}</div>
      </div>
    );
  };

  public renderYellowClusterList = () => {
    const { dataSource } = this.props;
    const list = dataSource?.yellowClusterList || [];
    return (
      <div className={`${this.className}-faulttask`}>
        <div className={`${this.className}-faulttask-icon`}>
          <svg className={`d1-layout-left-menus-icon`} style={{ margin: "0px 4px 0px 0px" }} aria-hidden="true">
            <use xlinkHref="#iconerjigaojing"></use>
          </svg>
          <span className={`${this.className}-faulttask-icon-title`}>预警集群</span>
        </div>
        <div className={`${this.className}-faulttask-content`}>{this.renderContent(list)}</div>
      </div>
    );
  };

  public renderUnknownClusterList = () => {
    const { dataSource } = this.props;
    const list = dataSource?.unknownClusterList || [];
    return (
      <div className={`${this.className}-faulttask`}>
        <div className={`${this.className}-faulttask-icon`}>
          <svg className={`d1-layout-left-menus-icon`} style={{ margin: "0px 4px 0px 0px" }} aria-hidden="true">
            <use xlinkHref="#iconweizhi"></use>
          </svg>
          <span className={`${this.className}-faulttask-icon-title`}>未知集群</span>
        </div>
        <div className={`${this.className}-faulttask-content`}>{this.renderContent(list)}</div>
      </div>
    );
  };

  public renderList = () => {
    let { chartKey } = this.state;
    if (chartKey === "unknown") {
      return this.renderUnknownClusterList();
    } else if (chartKey === "green") {
      return this.renderGreenClusterList();
    } else if (chartKey === "yellow") {
      return this.renderYellowClusterList();
    }
    return this.renderRedClusterList();
  };

  public renderTooltip = () => {
    let { price, currentCalLogic, threshold } = this.props?.dictionary || {};
    return <InfoTooltip className="health-tooltip" price={price} currentCalLogic={currentCalLogic} threshold={threshold}></InfoTooltip>;
  };

  public render() {
    return (
      <div className={this.className}>
        <div className={`${this.className}-header`}>
          <div className={`${this.className}-header-title`}>集群健康状态{this.renderTooltip()}</div>
        </div>
        <div className={`${this.className}-header-content`} style={{ display: "flex" }}>
          {this.renderChart()}
          {this.renderList()}
        </div>
      </div>
    );
  }
}
