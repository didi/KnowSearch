import React from "react";
import { EChartOption } from "echarts/lib/echarts";
import * as echart from "echarts/lib/echarts";
// import 'echarts/lib/chart/pie';
// import 'echarts/lib/chart/bar';
// import 'echarts/lib/chart/line';
// import 'echarts/lib/component/legend';
// import 'echarts/lib/component/tooltip';
// import 'echarts/lib/component/title';
// import 'echarts/lib/component/axis';
import "./index.less";
import { throttle } from "lodash";

export interface IEchartsProps {
  width?: number;
  height?: number;
  options?: EChartOption;
  isResize?: boolean;
  onClick?: (key: string) => void;
}

export const hasData = (options: EChartOption) => {
  if (options?.series?.length) return true;
  return false;
};

export default class Echarts extends React.Component<IEchartsProps> {
  public id = null as HTMLDivElement;

  public myChart = null as unknown as echart.ECharts;

  public componentDidMount() {
    const { options } = this.props;
    this.myChart = echart.init(this.id);
    this.myChart.setOption(options as EChartOption);
    this.props.onClick &&
      this.myChart.on("click", (params) => {
        this.props.onClick(params?.data?.key);
      });

    const resize = throttle(() => {
      // 表示该dom未进入可视区
      if (!this.id?.getBoundingClientRect().width) {
        return;
      }
      this.resize();
    }, 300);
    window.addEventListener("resize", resize);
  }

  public componentWillUnmount() {
    window.removeEventListener("resize", this.resize);
  }

  public componentDidUpdate() {
    this.refresh();
  }

  public refresh = () => {
    const { options } = this.props;
    this.myChart.setOption(options as EChartOption);
  };

  public resize = () => {
    this.props.isResize ? console.log("已设置不执行resize") : this.myChart.resize();
  };

  public render() {
    const { height, width } = this.props;
    return <div ref={(id: HTMLDivElement) => (this.id = id)} style={{ width: `${width}px`, height: `${height}px` }} />;
  }
}
