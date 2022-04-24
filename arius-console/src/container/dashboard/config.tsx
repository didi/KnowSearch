import Cluster from "./cluster";
import Node from "./node";
import IndexView from "./index-view";
import { IMenuItem } from "typesPath/base-types";
import React from "react";
import { unitMap, addChartTitle, colorList } from "../indicators-kanban/config";
import moment from "moment";
import { toFixedNum } from "lib/utils";
import { ECOption } from "../indicators-kanban/components/line";
import { NavRouterLink } from "container/custom-component";
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
]

const menuMap = new Map<string, IMenuItem>();

operationTabs.forEach((d) => {
  menuMap.set(d.key, d);
});

export const OPERATION_MENU_MAP = menuMap;

interface lineOptionType {
  title: string,
  xAxisData: number[],
  series: seriesType[],
  unitMap?: { [key: string]: any },
  isMoreDay?: boolean,
  isMarkLine?: boolean,
  color?: string[],
  // isShowTooltipModal?: boolean,
  // isShowTaskTooltipModal?: boolean,
  isClusterLink?: boolean,
  isGatewayLink?: boolean,
  metricsType?: string,
  showLegend?: boolean,
  clusterPhyName?: string[],
  tooltip?: string,
  linkWithSeriesName?: boolean
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
  linkWithSeriesName?: boolean
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
    })
  });
}

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
  const chartPosition = showLegend ? width / 2 - 100 : width / 2 - 20;

  if (x > chartPosition) {
    // 在鼠标左侧展示
    obj["left"] = x - domWidth - 10;
  } else {
    // 在鼠标右侧展示
    obj["left"] = x + 10;
  }

  return obj;
};

// 图表 tooltip 展示的样式
const tooltipFormatter = (date, arr, unit, isClusterLink, metricsType, isGatewayLink, clusterPhyName, linkWithSeriesName) => {
  // 新增从大到小排序
  arr = arr.sort((a, b) => b.value - a.value);
  const str = arr
    .map(
      (item, idx) => `<div style="margin: 3px 0;line-height:1;">
          <div style="margin: 0px 0 0;line-height:1;">
          ${item.marker}
          <span style="font-size:14px;color:#666;pointer-events: auto;font-weight:400;margin-left:2px;${isClusterLink || isGatewayLink ? 'cursor: pointer' : ''}" 
            ${isClusterLink ? `onclick="window.clusterlink('${linkWithSeriesName ? item.seriesName : ''}','${clusterPhyName[idx]}')"` : ""}
            ${isGatewayLink ? `onclick="window.gatewaylink('${linkWithSeriesName ? item.seriesName : ''}')"}` : ""}>
              ${item.seriesName}
            </span>
            <span style="float:right;margin-left:20px;font-size:14px;color:#666;font-weight:900">
              ${item.value > 10000
          ? toFixedNum(item.value / 10000, 2) + "W"
          : item.value
        } ${unit || ""}
            </span>
            <div style="clear:both"></div>
          </div>
          <div style="clear:both"></div>
        </div>
      <div style="clear:both"></div>`
    )
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
  // 是否跳转到网关看板
  isGatewayLink = false,
  // 后端图表指标名称，帮助弹窗获取数据
  metricsType,
  // 是否显示左侧 legend
  showLegend = true,
  clusterPhyName = [],
  // 跳转是否需要精确到具体维度，比如节点/索引
  linkWithSeriesName
}: lineOptionType) => {
  let seriesData;
  let unitFormatter = unitMap.formatter;
  let unitName = unitMap.name;

  if (unitFormatter) {
    // 根据数值大小判断单位是否需要进行转换
    if (unitName === 'ms' && isConversion(series)) {
      unitFormatter = (num) => {
        let val = Number(num);
        return toFixedNum(val / 1000, 3);
      }
      unitName = 's';
      title = title.replace('ms', 's');
    }
    seriesData = series.map((item: any) => ({
      name: item?.name || "",
      data: item?.data.map((el) => {
        if (typeof el == "object") {
          return {
            timestamp: el.timestamp,
            value: unitFormatter(el.value),
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
      position: (pos, params, dom, rect, size,) => {
        return tooltipPosition(pos, params, dom, rect, size, showLegend)
      },
      formatter: (params: any) => {
        let res = "";
        if (params != null && params.length > 0) {
          res += tooltipFormatter(
            moment(Number(params[0].name)).format("YYYY-MM-DD HH:mm"),
            params,
            unitName,
            isClusterLink,
            metricsType,
            isGatewayLink,
            clusterPhyName,
            linkWithSeriesName
          );
        }
        return res;
      },
    },
    legend: showLegend ? {
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
        color: '#74788D',
      },
      padding: [
        8,  // 上
        20, // 右
        6,  // 下
        5, // 左
      ],
      pageIcons: {
        horizontal: ['path://M474.496 512l151.616 151.616a9.6 9.6 0 0 1 0 13.568l-31.68 31.68a9.6 9.6 0 0 1-13.568 0l-190.08-190.08a9.6 9.6 0 0 1 0-13.568l190.08-190.08a9.6 9.6 0 0 1 13.568 0l31.68 31.68a9.6 9.6 0 0 1 0 13.568L474.496 512z', 'path://M549.504 512L397.888 360.384a9.6 9.6 0 0 1 0-13.568l31.68-31.68a9.6 9.6 0 0 1 13.568 0l190.08 190.08a9.6 9.6 0 0 1 0 13.568l-190.08 190.08a9.6 9.6 0 0 1-13.568 0l-31.68-31.68a9.6 9.6 0 0 1 0-13.568L549.504 512z'],
        pageIconColor: '#495057',
        pageIconInactiveColor: '#ADB5BC',
      },
      pageTextStyle: {
        width: 8,
        color: "#495057",
        fontSize: 11
      },
      pageIconSize: [4, 7],
      pageButtonItemGap: 10,
      pageFormatter: '{current} / {total}',
      tooltip: {
        show: true,
      },
    } : null,
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
          color: '#e9e9ea'
        }
      },
      axisLine: {
        lineStyle: {
          color: '#e9e9ea'
        }
      },
      fontFamily: 'HelveticaNeue',
      axisLabel: {
        color: "#495057",
        formatter: (value: number) => {
          value = Number(value);
          return '{a|' + moment(value).format("MM-DD") + '}\n'
            + '{b|' + moment(value).format("HH:mm") + '}';
        },
        rich: {
          a: {
            lineHeight: 16,
            color: '#495057',
          },
          b: {
            color: '#ADB5BC',
          }
        }
      },
    },
    yAxis: {
      type: "value",
      splitLine: {
        lineStyle: {
          type: "dashed",
          color: "#e9e9ea"
        },
      },
      axisLabel: {
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
  linkWithSeriesName = true
}: OptionType) => {
  if (!metrics || !metrics.type) {
    return {};
  }

  const title =
    (configData[metrics.type] && configData[metrics.type].title()) ||
    metrics.type;

  const xAxisData = [];

  const series = metrics.metricsContents.map((item, index) => ({
    name: item.name,
    data: item.metricsContentCells.sort((a, b) => a.timeStamp - b.timeStamp).map((item) => {
      if (index === 0) {
        xAxisData.push(item.timeStamp);
      }
      return {
        value: item.value,
        timestamp: item.timeStamp,
      };
    }),
  }));
  return getLineOption({
    title,
    xAxisData,
    series,
    unitMap: configData[metrics.type].unit,
    isMoreDay,
    isMarkLine,
    isClusterLink,
    metricsType: metrics.type,
    showLegend,
    clusterPhyName,
    isGatewayLink,
    linkWithSeriesName
  });
};

export const clusterMetrics = {
  health: {
    name: "集群健康状态",
    unit: unitMap.none,
    type: 'pie',
    fixed: true,
  },
  indexingLatency: {
    name: "写入耗时",
    unit: unitMap.ms,
  },
  indexReqNum: {
    name: "写入文档数",
    unit: unitMap.count,
  },
  searchLatency: {
    name: "查询耗时",
    unit: unitMap.ms,
  },
  gatewaySucPer: {
    name: "网关成功率",
    unit: unitMap.percent,
  },
  gatewayFailedPer: {
    name: "网关失败率",
    unit: unitMap.percent,
  },
  pendingTaskNum: {
    name: "集群pending task数",
    unit: unitMap.count,
  },
  httpNum: {
    name: "集群http连接数",
    unit: unitMap.count,
  },
  docUprushNum: {
    name: "查询请求数突增集群",
    unit: unitMap.none,
    tooltip: '每秒查询请求数加倍为突增',
  },
  reqUprushNum: {
    name: "写入文档数突增集群",
    unit: unitMap.none,
    tooltip: '每秒写入文档数加倍为突增',
  },
  shardNum: {
    name: "集群shard个数",
    unit: unitMap.count,
  },
}

addChartTitle(clusterMetrics);

const typeMap = {
  index: '索引',
  node: '节点',
  template: '索引模板'
}
export const columns = (width, type: string = 'index', disabled: boolean = false) => [
  {
    title: `${typeMap[type]}名称`,
    dataIndex: 'name',
    key: 'clusterPhynameName',
    width: width,
    render: (text, record) => {
      return <div style={{
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        height: 26,
        lineHeight: '26px',
        width: width - 10,
      }}>
        {
          disabled ? text : <NavRouterLink
            // maxShowLength={20}
            needToolTip
            style={{
              fontFamily: 'PingFangSC-Regular',
              fontSize: 12,
              color: '#495057',
              letterSpacing: 0,
              textAlign: 'justify',
              lineHeight: '20px',
            }}
            element={text}
            href={`/indicators/cluster?cluster=${record.clusterPhyName}&${type}=${text}&#${type}`} />
        }
      </div>
    },
  },
  {
    title: '所属集群',
    dataIndex: 'clusterPhyName',
    key: 'clusterPhyName',
    width: width,
    render: (text, record) => {
      return <div style={{
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        height: 26,
        lineHeight: '26px',
        width: width - 10,
      }}>
        {typeof text == 'number' ? text.toFixed(2) : text}
      </div>
    },
  },
]

export const indexViewMetrics = {
  reqUprushNum: {
    name: "查询请求数突增索引",
    unit: unitMap.count,
    tooltip: '每秒查询请求数加倍为突增',
  },
  docUprushNum: {
    name: "写入文档数突增索引",
    unit: unitMap.count,
    tooltip: '每秒写入文档数加倍为突增',
  },
  red: {
    name: "RED索引列表",
    unit: unitMap.none,
    columns: columns(180),
  },
  singReplicate: {
    name: "单副本索引列表",
    unit: unitMap.ms,
    columns: columns(180),
  },
  unassignedShard: {
    name: "未分配shard索引列表",
    unit: unitMap.ms,
    columns: columns(180),
  },
  bigShard: {
    name: "大shard索引列表",
    unit: unitMap.ms,
    tooltip: '大于50G为大shard',
    columns: columns(180),
  },
  smallShard: {
    name: "小shard索引列表",
    unit: unitMap.ms,
    tooltip: '小于1G为小shard',
    columns: columns(180),
  },
  mappingNum: {
    name: "索引Mapping字段个数",
    unit: unitMap.ms,
    columns: [...columns(120), {
      title: 'Mapping字段个数',
      dataIndex: 'value',
      width: 120,
      render: (text, record) => {
        return <div style={{
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          height: 26,
          lineHeight: '26px',
          width: 110,
        }}>
          {text}
        </div>
      },
    }],
  },
  segmentNum: {
    name: "索引Segements个数",
    unit: unitMap.ms,
    columns: [...columns(120), {
      title: 'Segements个数',
      dataIndex: 'value',
      width: 120,
      render: (text, record) => {
        return <div style={{
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          height: 26,
          lineHeight: '26px',
          width: 110,
        }}>
          {text}
        </div>
      },
    }],
  },
  tplSegmentNum: {
    name: "索引模板Segements个数",
    unit: unitMap.ms,
    as: 'segmentNum',
    columns: [...columns(120, 'template'), {
      title: 'Segements个数',
      dataIndex: 'value',
      width: 120,
      render: (text, record) => {
        return <div style={{
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          height: 26,
          lineHeight: '26px',
          width: 110,
        }}>
          {text}
        </div>
      },
    }],
  },
  segmentMemSize: {
    name: "索引Segements内存大小（MB）",
    unit: unitMap.ms,
    columns: [...columns(120), {
      title: '内存大小',
      dataIndex: 'value',
      width: 120,
      render: (text, record) => {
        return <div style={{
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          height: 26,
          lineHeight: '26px',
          width: 110,
        }}>
          {text}
        </div>
      },
    }],
  },
  tplSegmentMemSize: {
    name: "索引模板Segements内存大小（MB）",
    unit: unitMap.ms,
    as: 'segmentMemSize',
    columns: [...columns(120, 'template'), {
      title: '内存大小',
      dataIndex: 'value',
      width: 120,
      render: (text, record) => {
        return <div style={{
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          height: 26,
          lineHeight: '26px',
          width: 110,
        }}>
          {text}
        </div>
      },
    }],
  },
}

addChartTitle(indexViewMetrics);

export const nodeMetrics = {
  dead: {
    name: "Dead节点列表",
    unit: unitMap.ms,
    columns: columns(180, 'node', true),
  },
  largeDiskUsage: {
    name: "磁盘利用率超红线节点列表",
    unit: unitMap.ms,
    tooltip: '磁盘利用率超85%为超红线',
    columns: [...columns(120, 'node'), {
      title: '磁盘利用率',
      dataIndex: 'value',
      width: 120,
      render: (text, record) => {
        return <div style={{
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          height: 26,
          lineHeight: '26px',
          width: 110,
        }}>
          {text}
        </div>
      },
    }],
  },
  largeHead: {
    name: "堆内存利用率超红线节点列表",
    unit: unitMap.ms,
    tooltip: '堆内存利用率超80%且持续5min为超红线',
    columns: [...columns(120, 'node'), {
      title: '堆内存利用率',
      dataIndex: 'value',
      width: 120,
      render: (text, record) => {
        return <div style={{
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          height: 26,
          lineHeight: '26px',
          width: 110,
        }}>
          {text}
        </div>
      },
    }],
  },
  largeCpuUsage: {
    name: "CPU利用率超红线节点列表",
    unit: unitMap.ms,
    tooltip: 'CPU利用率超80%且持续30min为超红线',
    columns: [...columns(120, 'node'), {
      title: 'CPU利用率',
      dataIndex: 'value',
      width: 120,
      render: (text, record) => {
        return <div style={{
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          height: 26,
          lineHeight: '26px',
          width: 110,
        }}>
          {text}
        </div>
      },
    }],
  },
  writeRejectedNum: {
    name: "WriteRejected节点列表",
    unit: unitMap.ms,
    columns: columns(180, 'node'),
  },
  searchRejectedNum: {
    name: "SearchRejected节点列表",
    unit: unitMap.ms,
    columns: columns(180, 'node'),
  },
  shardNum: {
    name: "节点分片个数列表",
    unit: unitMap.ms,
    columns: [...columns(120, 'node'), {
      title: '分片个数',
      dataIndex: 'value',
      width: 120,
      render: (text, record) => {
        return <div style={{
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          height: 26,
          lineHeight: '26px',
          width: 110,
        }}>
          {text}
        </div>
      },
    }],
  },
  taskConsuming: {
    name: "节点执行任务耗时",
    unit: unitMap.ms,
    as: 'taskConsuming',
  },
  refresh: {
    name: "刷新线程池queue数",
    unit: unitMap.count,
  },
  flush: {
    name: "落盘刷新线程池queue数",
    unit: unitMap.count,
  },
  merge: {
    name: "合并线程池queue数",
    unit: unitMap.count,
  },
  search: {
    name: "查询线程池queue数",
    unit: unitMap.count,
  },
  write: {
    name: "写入线程池queue数",
    unit: unitMap.count,
  },
  management: {
    name: "集群管理线程池queue数",
    unit: unitMap.count,
  },
}

addChartTitle(nodeMetrics)