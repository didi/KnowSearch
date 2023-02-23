import React, { memo, useEffect } from "react";
import _ from "lodash";
// 引入 ECharts 主模块
import * as echarts from "echarts/core";
import {
  PieChart,
  // 系列类型的定义后缀都为 SeriesOption
} from "echarts/charts";
// 引入提示框，标题，直角坐标系组件，组件后缀都为 Component
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
} from "echarts/components";
// 引入 Canvas 渲染器，注意引入 CanvasRenderer 或者 SVGRenderer 是必须的一步
import { CanvasRenderer } from "echarts/renderers";
import { ellipsis } from "../config";
import { getPieOption } from './contrast-figure-config';

// 注册必须的组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  PieChart,
  LegendComponent,
  CanvasRenderer,
]);
import "../style";

const classPrefix = "contrast-figure";

export interface ContrastFigurePropsType {
  id: string | number;
  name: string;
  text: string;
  subtext: string;
  eChartsData: { value: number; name: string }[];
  colors: string[];
  legendVal: { [key: string]: { value: number | string; percent: number } };
  unit?: string;
  tooltipDirection?: 'left' | 'right';
}

export const ContrastFigure: React.FC<ContrastFigurePropsType> = memo((props) => {
  const { id, name, subtext, unit } = props;

  useEffect(() => {
    // 基于准备好的dom，初始化echarts实例
    const myChart = echarts.init(
      document.getElementById(`${classPrefix}-box-content-${id}`)
    );

    // 指定图表的配置项和数据
    const option = getPieOption(props);

    // 使用刚指定的配置项和数据显示图表。
    myChart.setOption(option);

    const resize = _.throttle(() => {
      const el: HTMLElement = document.getElementById(`${classPrefix}-box-content-${id}`);
      // 表示该dom未进入可视区
      if (!el.getBoundingClientRect().width) {
        return;
      }
      myChart.resize();
    }, 300);

    window.addEventListener("resize", resize);

    return () => {
      window.removeEventListener("resize", resize);
    };
  }, [props]);

  return (
    <div className={`${classPrefix}-box`}>
      <h4 className={`${classPrefix}-box-title`}>{name}</h4>
      <div className={`${classPrefix}-box-container`}>
        <span className={`${classPrefix}-box-subtext`}>
          {ellipsis(subtext, 6, unit)}
        </span>
        <div
          className={`${classPrefix}-box-content`}
          id={`${classPrefix}-box-content-${id}`}
        ></div>
      </div>
    </div>
  );
}
);
