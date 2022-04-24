import React from 'react';
import LineChart from '../echarts';
import { NavRouterLink } from "container/custom-component";
import { createOption } from './constants';
import './index.less';

interface IProps {
  type: string;
  dataSource: {
    greenNum: number,
    greenPercent: number,
    redClusterList: string[],
    redNum: number,
    redPercent: number,
    timestamp: number,
    totalNum: number,
    unknownClusterList: string[],
    unknownNum: number,
    unknownPercent: number,
    yellowClusterList: string[],
    yellowNum: number,
    yellowPercent: number
  };
}

const imgSrc = require('./../../../../assets/empty.png');

export class PieChart extends React.Component<IProps> {
  className: string;
  textStyle: any;
  public constructor(props: IProps) {
    super(props);
    this.className = `${this.props.type}-piechart`;
    this.textStyle = {
      fontFamily: 'PingFangSC-Regular',
      fontSize: 12,
      color: '#495057',
      letterSpacing: 0,
      textAlign: 'justify',
      lineHeight: '20px',
    }
  }

  public renderChart = () => {
    const { dataSource } = this.props;
    let data = [
      { value: dataSource?.greenNum || 0, name: '健康集群', prrcent: dataSource?.greenPercent },
      { value: dataSource?.yellowNum || 0, name: '预警集群', prrcent: dataSource?.yellowPercent },
      { value: dataSource?.redNum || 0, name: '故障集群', prrcent: dataSource?.redPercent },
      { value: dataSource?.unknownNum || 0, name: '未知集群', prrcent: dataSource?.unknownPercent },
    ]
    const options = createOption(data);
    return (
      <div style={{ marginTop: 31, float: 'left' }}>
        <LineChart isResize={true} width={162} height={162} options={options} key="chart" />
      </div>
    )
  }

  public renderRedClusterList = () => {
    const { dataSource } = this.props;
    const list = dataSource?.redClusterList || [];
    return (
      <div className={`${this.className}-faulttask`} style={{ marginLeft: 59, flex: 1 }}>
        <div className={`${this.className}-faulttask-icon`}>
          <svg className={`d1-layout-left-menus-icon`} style={{ margin: '0px 4px 0px 0px' }} aria-hidden="true">
            <use xlinkHref="#iconyijigaojing"></use>
          </svg>
          <span className={`${this.className}-faulttask-icon-title`}>故障集群</span>
        </div>
        <div className={`${this.className}-faulttask-content`}>
          {
            list.length === 0 ? <div className={`${this.className}-faulttask-content-empty`}>
              <div className='img'>
                <img src={imgSrc} />
              </div>
              <div>
                <span>数据为空</span>
              </div>
            </div> : null
          }
          <ul>
            {list.map((item: any, index: any) => {
              return <li className={`${this.className}-faulttask-content-li`} key={index}>
                <NavRouterLink maxShowLength={20} needToolTip style={this.textStyle} element={item} href={`/indicators/cluster?cluster=${item}#overview`} />
              </li>
            })}
          </ul>
        </div>
      </div>
    )
  }

  public renderYellowClusterList = () => {
    const { dataSource } = this.props;
    const list = dataSource?.yellowClusterList || [];
    return (
      <div className={`${this.className}-faulttask`} style={{ flex: 1 }}>
        <div className={`${this.className}-faulttask-icon`}>
          <svg className={`d1-layout-left-menus-icon`} style={{ margin: '0px 4px 0px 0px' }} aria-hidden="true">
            <use xlinkHref="#iconerjigaojing"></use>
          </svg>
          <span className={`${this.className}-faulttask-icon-title`}>预警集群</span>
        </div>
        <div className={`${this.className}-faulttask-content`}>
          {
            list.length === 0 ? <div className={`${this.className}-faulttask-content-empty`}>
              <div>
                <img src={imgSrc} />
              </div>
              <div>
                <span>数据为空</span>
              </div>
            </div> : null
          }
          <ul>
            {list.map((item: any, index: any) => {
              return <li className={`${this.className}-faulttask-content-li`} key={index}>
                <NavRouterLink maxShowLength={20} needToolTip style={this.textStyle} element={item} href={`/indicators/cluster?cluster=${item}#overview`} />
              </li>
            })}
          </ul>
        </div>
      </div>
    )
  }

  public rendeUnknownClusterList = () => {
    const { dataSource } = this.props;
    const list = dataSource?.unknownClusterList || [];
    return (
      <div className={`${this.className}-faulttask`} style={{ flex: 1 }}>
        <div className={`${this.className}-faulttask-icon`}>
          <svg className={`d1-layout-left-menus-icon`} style={{ margin: '0px 4px 0px 0px' }} aria-hidden="true">
            <use xlinkHref="#iconweizhi"></use>
          </svg>
          <span className={`${this.className}-faulttask-icon-title`}>未知集群</span>
        </div>
        <div className={`${this.className}-faulttask-content`}>
          {
            list.length === 0 ? <div className={`${this.className}-faulttask-content-empty`}>
              <div>
                <img src={imgSrc} />
              </div>
              <div>
                <span>数据为空</span>
              </div>
            </div> : null
          }
          <ul>
            {list.map((item: any, index: any) => {
              return <li className={`${this.className}-faulttask-content-li`} key={index}>
                <NavRouterLink maxShowLength={20} needToolTip style={this.textStyle} element={item} href={`/indicators/cluster?cluster=${item}#overview`} />
              </li>
            })}
          </ul>
        </div>
      </div>
    )
  }

  public render() {
    return (
      <div className={this.className}>
        <div className={`${this.className}-header`}>
          <div className={`${this.className}-header-title`}>
            集群健康状态
          </div>
        </div>
        {this.renderChart()}
        <div style={{ display: 'flex'}}>
          {this.renderRedClusterList()}
          {this.renderYellowClusterList()}
          {this.rendeUnknownClusterList()}
        </div>
      </div>
    )
  }
}
