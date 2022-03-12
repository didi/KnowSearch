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

interface ContrastFigurePropsType {
  id: string | number;
  name: string;
  text: string;
  subtext: string;
  eChartsData: { value: number; name: string }[];
  colors: string[];
  legendVal: { [key: string]: { value: number | string; percent: number } };
  unit?: string;
}

export const ContrastFigure: React.FC<ContrastFigurePropsType> = memo(
  ({ id, name, text, subtext, eChartsData, colors, legendVal, unit }) => {
    useEffect(() => {
      // 基于准备好的dom，初始化echarts实例
      const myChart = echarts.init(
        document.getElementById(`${classPrefix}-box-content-${id}`)
      );
      // 指定图表的配置项和数据
      const option = {
        legend: {
          orient: "vertical",
          top: "center",
          align: "left",
          right: 0,
          icon: "circle",
          itemGap: 20,
          itemWidth: 12,
          itemHeight: 12,
          textStyle: {
            fontFamily: "PingFangSC-Regular",
            fontSize: 12,
            color: "#303A51",
            letterSpacing: 0,
            textAlign: "justify",
            width: 100,
            overflow: "break",
            lineHeight: 18,
            // ellipsis: "...",
          },
          formatter: (name) => {
            return (
              name +
              ":\n" +
              legendVal[name].value +
              "  |  " +
              legendVal[name].percent +
              "%"
            );
          },
        },
        //backgroundColor: '#031f2d',
        tooltip: {
          show: true,
          trigger: "item",
          formatter: `{b} : {c} ${unit ? unit : ""} ({d}%)`,
        },
        title: {
          show: true,
          text: text,
          left: "34%",
          top: "45%",
          // subtext: subtext,
          textAlign: "center",
          textVerticalAlign: "center",
          textStyle: {
            fontFamily: "PingFangSC-Regular",
            color: "#505568",
            fontSize: 14,
          },
          subtextStyle: {
            fontFamily: "PingFangSC-Medium",
            color: "#303A51",
            fontSize: 19,
            width: 100,
            overflow: "truncate",
            ellipsis: "...",
          },
        },
        color: colors,
        animation: false,
        series: [
          {
            name: name,
            type: "pie",
            radius: ["60%", "90%"],
            center: ["35%", "53%"],
            hoverAnimation: true,
            itemStyle: {
              borderColor: "#fff",
              borderWidth: 1,
            },
            label: {
              show: false,
            },
            data: eChartsData,
          },
        ],
      };

      // 使用刚指定的配置项和数据显示图表。
      myChart.setOption(option);
      const resize = _.throttle(() => {
        myChart.resize();
      }, 300);
      window.addEventListener("resize", resize);
      return () => {
        window.removeEventListener("resize", resize);
      };
    }, [id, name, text, subtext, eChartsData, colors]);
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
