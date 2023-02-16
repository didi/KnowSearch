import React from "react";
import Cluster from "./cluster";
import Node from "./node";
import IndexView from "./index-view";
import { IMenuItem } from "typesPath/base-types";
import { unitMap, addChartTitle, colorList } from "../indicators-kanban/config";
import moment from "moment";
import { toFixedNum, bytesUnitFormatter, formatTime, formatDecimalPoint } from "lib/utils";
import { ECOption } from "../indicators-kanban/components/line";
import { NavRouterLink } from "container/custom-component";
import { Tooltip } from "antd";

export enum TAB_LIST_KEY {
  Operation = "operation",
  operate = "operate",
}

export enum OPERATION_TAB_LIST_KEY {
  cluster = "cluster",
  index = "index",
  node = "node",
}

interface seriesType {
  name: string;
  data: number[] | { timestamp: number; value: number }[];
}
export interface metricsContentsType {
  metricsContentCells: { timeStamp: number; value: number }[];
  name: string;
  cluster?: string;
}

export interface metricsType {
  metricsContents: metricsContentsType[];
  type: string;
}

export const operationTabs = [
  {
    name: "集群",
    key: OPERATION_TAB_LIST_KEY.cluster,
    content: () => <Cluster />,
  },
  {
    name: "节点",
    key: OPERATION_TAB_LIST_KEY.node,
    content: () => <Node />,
  },
  {
    name: "索引",
    key: OPERATION_TAB_LIST_KEY.index,
    content: () => <IndexView />,
  },
];

const menuMap = new Map<string, IMenuItem>();

operationTabs.forEach((d) => {
  menuMap.set(d.key, d);
});

export const OPERATION_MENU_MAP = menuMap;

interface lineOptionType {
  title: string;
  xAxisData: number[];
  series: seriesType[];
  unitMap?: { [key: string]: any };
  isMoreDay?: boolean;
  isMarkLine?: boolean;
  color?: string[];
  // isShowTooltipModal?: boolean,
  // isShowTaskTooltipModal?: boolean,
  isClusterLink?: boolean;
  isNodeLink?: boolean;
  isGatewayLink?: boolean;
  metricsType?: string;
  showLegend?: boolean;
  clusterPhyName?: string[];
  tooltip?: string;
  linkWithSeriesName?: boolean;
  cluster?: string;
}

interface OptionType {
  metrics: metricsType;
  configData: any;
  isMoreDay?: boolean;
  isMarkLine?: boolean;
  isClusterLink?: boolean;
  showLegend?: boolean;
  isGatewayLink?: boolean;
  clusterPhyName?: string[];
  linkWithSeriesName?: boolean;
  isNodeLink?: boolean;
}

// 判断 ms 是否需要转换成 s，只判断峰值
const isConversion = (series) => {
  return series.some((item) => {
    return item.data.some((item) => {
      if (typeof item == "object") {
        if (item.value > 1000) {
          return true;
        }
      }
      return item > 1000;
    });
  });
};

// 图表 tooltip 展示位置
const tooltipPosition = (pos, params, dom, rect, size, showLegend) => {
  const [x, y] = pos;
  const [width, height] = size.viewSize;

  let domHeight = (dom as any).offsetHeight;

  if (domHeight > 350) {
    domHeight = 250;
  }

  const domWidth = (dom as any).offsetWidth || 390;

  const obj = { top: y - domHeight - 10 };

  // showLegend 是否展示左侧 legend
  const chartPosition = showLegend ? width - 40 : width / 2 - 20;
  if (x > chartPosition) {
    // 在鼠标左侧展示
    obj["left"] = x - domWidth - 10;
  } else if (x + domWidth > width) {
    // 右侧超出，设置固定位置
    obj["right"] = -50;
  } else {
    // 在鼠标右侧展示
    obj["left"] = x + 10;
  }
  return obj;
};

// 图表 tooltip 展示的样式
const tooltipFormatter = (params) => {
  let { date, arr, unit, isClusterLink, metricsType, isGatewayLink, clusterPhyName, linkWithSeriesName, isNodeLink, cluster } = params;
  // 新增从大到小排序
  arr = arr.sort((a, b) => b.value - a.value);
  const str = arr
    .map((item: any, idx: number) => {
      return `<div style="margin: 3px 0;line-height:1;">
        <div style="margin: 0px 0 0;line-height:1;">
        ${item.marker}
        <span style="font-size:14px;color:#666;pointer-events: auto;font-weight:400;margin-left:2px;${
          isClusterLink || isGatewayLink || isNodeLink ? "cursor: pointer" : ""
        }" 
          ${isClusterLink ? `onclick="window.clusterlink('${linkWithSeriesName ? item.seriesName : ""}','${item?.data?.cluster}')"` : ""}
          ${isNodeLink ? `onclick="window.nodelink('${linkWithSeriesName ? item.seriesName : ""}','${cluster}')"` : ""}
          ${isGatewayLink ? `onclick="window.gatewaylink('${linkWithSeriesName ? item.seriesName : ""}')"}` : ""}>
          <span title=所属集群：${item?.data?.cluster}>${item.seriesName}</span>
          </span>
          <span style="float:right;margin-left:20px;font-size:14px;color:#666;font-weight:900">
            ${item.value > 10000 ? toFixedNum(item.value / 10000, 2) + "W" : item.value} ${unit || ""}
          </span>
          <div style="clear:both"></div>
        </div>
        <div style="clear:both"></div>
      </div>
    <div style="clear:both"></div>`;
    })
    .join("");

  return `<div style="margin: 0px 0 0;line-height:1; position: relative; z-index: 99;">
    <div style="margin: 0px 0 0;line-height:1;">
      <div style="font-size:14px;color:#666;font-weight:400;line-height:1;">
        ${date}
      </div>
      <div style="margin: 10px 0 0;line-height:1;max-height: 252px; overflow: scroll">
        ${str}
      </div>
      <div style="clear:both"></div>
    </div>
  </div>`;
};

export const getLineOption = ({
  title,
  xAxisData,
  series,
  // 单位名称，单位格式化
  unitMap,
  // 是否大于一天
  isMoreDay = false,
  // 是否展示警戒线
  isMarkLine = false,
  color = colorList,
  // 是否跳转到集群看板
  isClusterLink = false,
  // 是否跳转到节点视图
  isNodeLink = false,
  // 是否跳转到网关看板
  isGatewayLink = false,
  // 后端图表指标名称，帮助弹窗获取数据
  metricsType,
  // 是否显示左侧 legend
  showLegend = true,
  clusterPhyName = [],
  // 跳转是否需要精确到具体维度，比如节点/索引
  linkWithSeriesName,
  cluster,
}: lineOptionType) => {
  let seriesData;
  let unitFormatter = unitMap?.formatter;
  let unitName = unitMap?.name;

  if (unitFormatter) {
    // 根据数值大小判断单位是否需要进行转换
    if (unitName === "ms" && isConversion(series)) {
      unitFormatter = (num) => {
        let val = Number(num);
        return toFixedNum(val / 1000, 3);
      };
      unitName = "s";
      title = title.replace("ms", "s");
    }
    seriesData = series.map((item: any) => ({
      name: item?.name || "",
      data: item?.data.map((el) => {
        if (typeof el == "object") {
          return {
            timestamp: el.timestamp,
            value: unitFormatter(el.value),
            cluster: el?.cluster || "",
          };
        }
        return unitFormatter(el);
      }),
      type: "line",
      // markLine: isMarkLine ? markLine() : {},
      showSymbol: false,
    }));
  } else {
    seriesData = series.map((item) => ({
      ...item,
      type: "line",
      // markLine: isMarkLine ? markLine() : {},
      showSymbol: false,
    }));
  }
  return {
    color: color,
    title: {
      text: title,
      show: false,
    },
    tooltip: {
      trigger: "axis",
      enterable: true,
      position: (pos, params, dom, rect, size) => {
        return tooltipPosition(pos, params, dom, rect, size, showLegend);
      },
      extraCssText: "z-index: 101",
      formatter: (params: any) => {
        let res = "";
        if (params != null && params.length > 0) {
          let formatterParams = {
            date: moment(Number(params[0].name)).format("YYYY-MM-DD HH:mm"),
            arr: params,
            unit: unitName,
            isClusterLink,
            metricsType,
            isGatewayLink,
            clusterPhyName,
            linkWithSeriesName,
            isNodeLink,
            cluster,
          };
          res += tooltipFormatter(formatterParams);
        }
        return res;
      },
    },
    legend: showLegend
      ? {
          type: "scroll",
          left: "45",
          bottom: "5",
          icon: "rect",
          itemHeight: 2,
          itemWidth: 14,
          textStyle: {
            width: 85,
            overflow: "truncate",
            ellipsis: "...",
            fontSize: 11,
            color: "#74788D",
          },
          padding: [
            8, // 上
            20, // 右
            6, // 下
            5, // 左
          ],
          pageIcons: {
            horizontal: [
              "path://M474.496 512l151.616 151.616a9.6 9.6 0 0 1 0 13.568l-31.68 31.68a9.6 9.6 0 0 1-13.568 0l-190.08-190.08a9.6 9.6 0 0 1 0-13.568l190.08-190.08a9.6 9.6 0 0 1 13.568 0l31.68 31.68a9.6 9.6 0 0 1 0 13.568L474.496 512z",
              "path://M549.504 512L397.888 360.384a9.6 9.6 0 0 1 0-13.568l31.68-31.68a9.6 9.6 0 0 1 13.568 0l190.08 190.08a9.6 9.6 0 0 1 0 13.568l-190.08 190.08a9.6 9.6 0 0 1-13.568 0l-31.68-31.68a9.6 9.6 0 0 1 0-13.568L549.504 512z",
            ],
            pageIconColor: "#495057",
            pageIconInactiveColor: "#ADB5BC",
          },
          pageTextStyle: {
            width: 8,
            color: "#495057",
            fontSize: 11,
          },
          pageIconSize: [4, 7],
          pageButtonItemGap: 10,
          pageFormatter: "{current} / {total}",
          tooltip: {
            show: true,
          },
        }
      : null,
    grid: {
      left: 16,
      right: 16,
      bottom: showLegend ? 37 : 10,
      containLabel: true,
    },
    xAxis: {
      type: "category",
      boundaryGap: true,
      data: [...xAxisData],
      axisTick: {
        alignWithLabel: true,
        lineStyle: {
          color: "#e9e9ea",
        },
      },
      axisLine: {
        lineStyle: {
          color: "#e9e9ea",
        },
      },
      fontFamily: "HelveticaNeue",
      axisLabel: {
        color: "#495057",
        formatter: (value: number) => {
          value = Number(value);
          return "{a|" + moment(value).format("MM-DD") + "}\n" + "{b|" + moment(value).format("HH:mm") + "}";
        },
        rich: {
          a: {
            lineHeight: 16,
            color: "#495057",
          },
          b: {
            color: "#ADB5BC",
          },
        },
      },
    },
    yAxis: {
      type: "value",
      splitLine: {
        lineStyle: {
          type: "dashed",
          color: "#e9e9ea",
        },
      },
      axisLabel: {
        showMaxLabel: true,
        formatter: function (value) {
          if (value < 10000) {
            return value;
          }
          return (value / 10000).toFixed(1) + "W";
        },
      },
    },
    series: [...seriesData],
    animation: false,
  } as ECOption;
};

export const getOption = ({
  metrics,
  configData,
  isMoreDay = false,
  isMarkLine = false,
  isClusterLink = false,
  showLegend = true,
  isGatewayLink = false,
  clusterPhyName = [],
  linkWithSeriesName = true,
  isNodeLink = false,
}: OptionType) => {
  if (!metrics || !metrics.type) {
    return {};
  }

  const title = (configData[metrics.type] && configData[metrics.type].title()) || metrics.type;

  const xAxisData = [];
  const series = metrics.metricsContents.map((content, index) => ({
    name: content.name,
    data: content.metricsContentCells
      .sort((a, b) => a.timeStamp - b.timeStamp)
      .map((item) => {
        if (index === 0) {
          xAxisData.push(item.timeStamp);
        }
        return {
          value: item.value,
          timestamp: item.timeStamp,
          cluster: content?.cluster || "",
        };
      }),
  }));
  return getLineOption({
    title,
    xAxisData,
    series,
    unitMap: configData[metrics.type]?.unit,
    isMoreDay,
    isMarkLine,
    isClusterLink,
    metricsType: metrics.type,
    showLegend,
    clusterPhyName,
    isGatewayLink,
    linkWithSeriesName,
    isNodeLink,
    cluster: metrics?.metricsContents[0]?.cluster,
  });
};

export const clusterMetrics = {
  health: {
    name: "集群健康状态",
    unit: unitMap.none,
    type: "pie",
    fixed: true,
  },
  searchLatency: {
    name: "查询耗时",
    unit: unitMap.ms,
  },
  indexingLatency: {
    name: "写入耗时",
    unit: unitMap.ms,
  },
  shardNum: {
    name: "shard个数大于10000集群列表",
    width: "33%",
    columns: [
      {
        title: "集群名称",
        dataIndex: "clusterPhyName",
        render: (text, record) => {
          return (
            <div
              style={{
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis",
                height: 26,
                lineHeight: "26px",
                width: "90%",
              }}
            >
              <NavRouterLink
                needToolTip
                style={{
                  fontFamily: "PingFangSC-Regular",
                  fontSize: 12,
                  color: "#495057",
                  letterSpacing: 0,
                  textAlign: "justify",
                  lineHeight: "20px",
                }}
                element={text}
                href={`/indicators/cluster?cluster=${record.clusterPhyName}&overview=${text}&#overview`}
              />
            </div>
          );
        },
      },
      {
        title: "shard个数",
        dataIndex: "value",
        width: "33%",
        render: (val: number) => val || "-",
      },
      {
        title: "索引个数",
        dataIndex: "indexCount",
        width: "33%",
        render: (val: number) => val || "-",
      },
    ],
  },
  pendingTaskNum: {
    name: "集群pending task数",
    unit: unitMap.count,
  },
  gatewayFailedPer: {
    name: "网关失败率",
    unit: unitMap.percent,
  },
  nodeElapsedTime: {
    name: "nodes_stats 接口平均采集耗时",
    unit: unitMap.s,
  },
  clusterElapsedTimeGte5Min: {
    name: "指标采集延时大于5分钟集群列表",
    unit: unitMap.none,
    columns: [
      {
        title: "集群名称",
        dataIndex: "clusterPhyName",
        width: "50%",
        render: (text, record) => {
          return (
            <div
              style={{
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis",
                height: 26,
                lineHeight: "26px",
                width: "90%",
              }}
            >
              <NavRouterLink
                needToolTip
                style={{
                  fontFamily: "PingFangSC-Regular",
                  fontSize: 12,
                  color: "#495057",
                  letterSpacing: 0,
                  textAlign: "justify",
                  lineHeight: "20px",
                }}
                element={text}
                href={`/indicators/cluster?cluster=${record.clusterPhyName}&overview=${text}&#overview`}
              />
            </div>
          );
        },
      },
      {
        title: "采集延时",
        dataIndex: "value",
        width: "50%",
        render: (val: number) => formatTime(val) || "-",
      },
    ],
  },
};

addChartTitle(clusterMetrics);

const typeMap = {
  index: "索引",
  node: "节点",
  template: "索引模板",
};
export const columns = (width, type: string = "index", disabled: boolean = false) => [
  {
    title: `${typeMap[type]}名称`,
    dataIndex: "name",
    key: "clusterPhynameName",
    ellipsis: true,
    width,
    render: (text, record) => {
      return (
        <div
          style={{
            whiteSpace: "nowrap",
            overflow: "hidden",
            textOverflow: "ellipsis",
            height: 26,
            lineHeight: "26px",
            width: "90%",
          }}
        >
          {disabled ? (
            text
          ) : (
            <NavRouterLink
              // maxShowLength={20}
              needToolTip
              style={{
                fontFamily: "PingFangSC-Regular",
                fontSize: 12,
                color: "#495057",
                letterSpacing: 0,
                textAlign: "justify",
                lineHeight: "20px",
              }}
              element={text}
              href={`/indicators/cluster?cluster=${record.clusterPhyName}&${type}=${text}&#${type}`}
            />
          )}
        </div>
      );
    },
  },
  {
    title: "所属集群",
    dataIndex: "clusterPhyName",
    key: "clusterPhyName",
    ellipsis: true,
    width,
    render: (text, record) => {
      return (
        <div
          style={{
            whiteSpace: "nowrap",
            overflow: "hidden",
            textOverflow: "ellipsis",
            height: 26,
            lineHeight: "26px",
            width: "90%",
          }}
        >
          <Tooltip title={typeof text == "number" ? text.toFixed(2) : text} placement="bottomLeft">
            {typeof text == "number" ? text.toFixed(2) : text}
          </Tooltip>
        </div>
      );
    },
  },
];

export const indexViewMetrics = {
  segmentNum: {
    name: "索引Segments个数",
    //tooltip: "索引Segments个数超过阀值100才显示",
    unit: unitMap.ms,
    columns: [
      ...columns("33%"),
      {
        title: "Segments个数",
        dataIndex: "value",
        width: "33%",
        render: (text, record) => {
          return (
            <div
              style={{
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis",
                height: 26,
                lineHeight: "26px",
                width: "90%",
              }}
            >
              {text}
            </div>
          );
        },
      },
    ],
  },
  tplSegmentMemSize: {
    name: "索引模板Segments内存大小（MB）",
    //tooltip: "索引模版Segments内存大小超过阀值才显示",
    unit: unitMap.ms,
    as: "segmentMemSize",
    columns: [
      ...columns("33%", "template"),
      {
        title: "内存大小",
        dataIndex: "value",
        width: "33%",
        render: (text, record) => {
          return (
            <div
              style={{
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis",
                height: 26,
                lineHeight: "26px",
                width: "90%",
              }}
            >
              {bytesUnitFormatter(text) || ""}
            </div>
          );
        },
      },
    ],
  },
  unassignedShard: {
    name: "未分配shard索引列表",
    unit: unitMap.ms,
    columns: columns("50%"),
  },
  tplSegmentNum: {
    name: "索引模板Segments个数",
    //tooltip: "索引模版Segments个数超过阀值1000才显示",
    unit: unitMap.ms,
    as: "segmentNum",
    columns: [
      ...columns("33%", "template"),
      {
        title: "Segments个数",
        dataIndex: "value",
        width: "33%",
        render: (text, record) => {
          return (
            <div
              style={{
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis",
                height: 26,
                lineHeight: "26px",
                width: "90%",
              }}
            >
              {text}
            </div>
          );
        },
      },
    ],
  },
  mappingNum: {
    name: "索引Mapping字段个数",
    //tooltip: "索引Mapping个数超过100再显示",
    unit: unitMap.ms,
    columns: [
      ...columns("33%"),
      {
        title: "Mapping字段个数",
        dataIndex: "value",
        width: "33%",
        render: (text, record) => {
          return (
            <div
              style={{
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis",
                height: 26,
                lineHeight: "26px",
                width: "90%",
              }}
            >
              {text}
            </div>
          );
        },
      },
    ],
  },
  bigShard: {
    name: "单个shard大于50G索引列表",
    unit: unitMap.ms,
    //tooltip: "大于50G为大shard",
    columns: [
      ...columns("33%"),
      {
        title: "索引存储大小",
        dataIndex: "value",
        key: "value",
        width: "33%",
        render: (val) => {
          return bytesUnitFormatter(val);
        },
      },
    ],
  },
  smallShard: {
    name: "单个shard小于500MB索引列表",
    //name: "小shard索引列表",
    unit: unitMap.ms,
    // tooltip: "小shard索引列表，shard个数大于1才显示",
    columns: [
      ...columns("33%"),
      {
        title: "索引存储大小",
        dataIndex: "value",
        width: "33%",
        render: (val) => {
          return bytesUnitFormatter(val);
        },
      },
    ],
  },
  segmentMemSize: {
    name: "索引Segments内存大小（MB）",
    // tooltip: "索引Segments内存大小超过阀值才显示",
    unit: unitMap.ms,
    columns: [
      ...columns("33%"),
      {
        title: "内存大小",
        dataIndex: "value",
        width: "33%",
        render: (text, record) => {
          return (
            <div
              style={{
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis",
                height: 26,
                lineHeight: "26px",
                width: "90%",
              }}
            >
              {bytesUnitFormatter(text)}
            </div>
          );
        },
      },
    ],
  },
  singReplicate: {
    name: "无副本索引列表",
    unit: unitMap.ms,
    columns: columns("50%"),
  },
  red: {
    name: "RED索引列表",
    unit: unitMap.none,
    columns: columns("50%"),
  },
};

addChartTitle(indexViewMetrics);

export const nodeMetrics = {
  taskConsuming: {
    name: "节点执行任务耗时",
    unit: unitMap.s,
    as: "taskConsuming",
  },
  shardNum: {
    name: "节点分片个数列表",
    //tooltip: "节点分片个数>500才显示",
    unit: unitMap.ms,
    columns: [
      ...columns("33%", "node"),
      {
        title: "分片个数",
        dataIndex: "value",
        width: "33%",
        render: (text, record) => {
          return (
            <div
              style={{
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis",
                height: 26,
                lineHeight: "26px",
                width: "90%",
              }}
            >
              {text}
            </div>
          );
        },
      },
    ],
  },
  largeDiskUsage: {
    name: "磁盘利用率超红线节点列表",
    unit: unitMap.ms,
    tooltip: "磁盘利用率超85%为超红线",
    columns: [
      ...columns("33%", "node"),
      {
        title: "磁盘利用率",
        dataIndex: "value",
        width: "33%",
        render: (text, record) => {
          return (
            <div
              style={{
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis",
                height: 26,
                lineHeight: "26px",
                width: "90%",
              }}
            >
              {formatDecimalPoint(text)}%
            </div>
          );
        },
      },
    ],
  },
  largeHead: {
    name: "堆内存利用率超红线节点列表",
    unit: unitMap.ms,
    tooltip: "堆内存利用率超80%且持续5min为超红线",
    columns: [
      ...columns("33%", "node"),
      {
        title: "堆内存利用率",
        dataIndex: "value",
        width: "33%",
        render: (text, record) => {
          return (
            <div
              style={{
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis",
                height: 26,
                lineHeight: "26px",
                width: "90%",
              }}
            >
              {formatDecimalPoint(text)}%
            </div>
          );
        },
      },
    ],
  },
  writeRejectedNum: {
    name: "WriteRejected节点列表",
    unit: unitMap.ms,
    columns: columns("50%", "node"),
  },
  searchRejectedNum: {
    name: "SearchRejected节点列表",
    unit: unitMap.ms,
    columns: columns("50%", "node"),
  },
  largeCpuUsage: {
    name: "CPU利用率超红线节点列表",
    unit: unitMap.ms,
    tooltip: "CPU利用率超80%且持续30min为超红线",
    columns: [
      ...columns("33%", "node"),
      {
        title: "CPU利用率",
        dataIndex: "value",
        width: "33%",
        render: (text, record) => {
          return (
            <div
              style={{
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis",
                height: 26,
                lineHeight: "26px",
                width: "90%",
              }}
            >
              {formatDecimalPoint(text)}%
            </div>
          );
        },
      },
    ],
  },
};

addChartTitle(nodeMetrics);
