
import { ContrastFigurePropsType  } from './contrast-figure';

export const getPieOption = ({
  name,
  text,
  eChartsData,
  colors,
  legendVal,
  unit,
  tooltipDirection = 'right'
}: ContrastFigurePropsType) => {
  return {
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
    tooltip: {
      show: true,
      trigger: "item",
      formatter: `{b} : {c} ${unit ? unit : ""} ({d}%)`,
      position: (pos, params, dom, rect, size) => {
        const [x, y] = pos;

        let domHeight = (dom as any).offsetHeight;

        if (domHeight > 350) {
          domHeight = 250;
        }

        const domWidth = (dom as any).offsetWidth || 390;

        const obj = { top: y - domHeight - 10 };

        if (tooltipDirection === 'left') {
          // 在鼠标左侧展示
          obj["left"] = x - domWidth - 10;
        } else {
          // 在鼠标右侧展示
          obj["left"] = x + 10;
        }

        return obj;
      }
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
}