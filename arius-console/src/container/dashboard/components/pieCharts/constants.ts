export const createOption = (data: any, key: string) => {
  let text = {
    green: "健康率",
    yellow: "预警率",
    red: "故障率",
    unknown: "未知率",
  };
  let subtext = (data || []).filter((item) => item.key === key);

  const options = {
    title: {
      text: text[key] || "故障率",
      subtext: parseInt(subtext[0]?.prrcent || "0") + "%",
      textAlign: "center",
      textVerticalAlign: "center",
      textStyle: {
        fontSize: 12,
        color: "#74788D",
        fontFamily: "PingFangSC-Regular",
      },
      subtextStyle: {
        fontSize: 20,
        color: "rgba(0,0,0,0.85)",
        fontFamily: "PingFangSC-Medium",
      },
      left: "47%",
      top: "40%",
    },
    tooltip: {
      trigger: "item",
      position: (pos, params, dom, rect, size) => {
        const [x, y] = pos;
        const [width, height] = size.viewSize;
        const domWidth = (dom as any).offsetWidth || 390;
        const obj = { top: y - 80 };
        const chartPosition = width / 2 + 20;
        if (x > chartPosition) {
          // 在鼠标左侧展示
          obj["left"] = x - domWidth - 10;
        } else {
          // 在鼠标右侧展示
          obj["left"] = x + 10;
        }
        return obj;
      },
      formatter: (params: any) => {
        let tip =
          params.marker +
          params.name +
          ": " +
          params?.data?.value +
          '个<br /> <span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color: #fff;"> </span>占比: ' +
          params?.data?.prrcent +
          "%";
        return tip;
      },
    },
    color: ["#6BD9C5", "#F9CC7E", "#FA8787", "#A8ADBD"],
    grid: {
      left: 10,
      top: 0,
    },
    series: [
      {
        type: "pie",
        radius: ["100%", "60%"],
        avoidLabelOverlap: false,
        hoverAnimation: false,
        label: {
          show: false,
          position: "center",
        },
        animation: false,
        labelLine: {
          show: false,
        },
        data: data,
      },
    ],
  };
  return options;
};
