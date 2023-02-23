import React, { useState, useEffect, useRef, useContext } from "react";
import { SyncOutlined } from "@ant-design/icons";
import { MetricsConfig } from "./components/metricsConfig";
import { clusterMetrics, getOption } from "./config";
import { PieCharts } from "./components/pieCharts";
import "./index.less";
import { ILineParams, clusterLine, setCheckedList, getCheckedList, clusterList, clusterThreadPoolQueue } from "api/dashboard";
import { Line } from "./components/line";
import { cloneDeep } from "lodash";
import DragGroup from "../../d1-packages/DragGroup";
import { arrayMoveImmutable } from "array-move";
import { withRouter } from "react-router-dom";
import TableCard from "./components/table";
import { connect } from "react-redux";
import { CustomTimeRangePicker } from "component/TimeRangePicker";
import { DashboardContext } from "./Operation";
import InfoTooltip from "component/infoTooltip";

const ONE_HOUR = 1000 * 60 * 60;

const mapStateToProps = (state) => ({
  dashboard: state.dashBoard,
});

const Cluster = withRouter((props: any) => {
  const dictionary = useContext(DashboardContext);

  const currentTime = new Date().getTime();
  const [rangeTime, setRangeTime] = useState([currentTime - ONE_HOUR, currentTime]);
  const [loading, setLoading] = useState([]);
  const [metricsLoading, setMetricsLoading] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [metrics, setMetrics] = useState(["health", ...Object.keys(clusterMetrics).filter((item) => item != "health")]);
  const [data, setData] = useState({});
  const ref: any = useRef();
  const topN = useRef({});
  const customTimeRef = useRef(null);

  const handleTimeChange = (times: number[], isCustomTime = false) => {
    setRangeTime(times);
  };

  const setIndexConfigCheckedData = (checkvalue) => {
    setMetrics(["health", ...checkvalue.filter((item) => item != "health")]);
    setCheckedList("cluster", ["health", ...checkvalue.filter((item) => item != "health")]);
  };

  const reload = () => {
    if (metricsLoading) return;
    ref.current?.getData();
    getLineData();
  };

  const sortMetrics = () => {
    const left = metrics.map((item) => ({ name: clusterMetrics[item]?.name, value: item, fixed: clusterMetrics[item]?.fixed || false }));
    const right = [];
    Object.keys(clusterMetrics).forEach((item) => {
      if (!metrics.includes(item)) {
        right.push({ name: clusterMetrics[item]?.name, value: item, fixed: clusterMetrics[item]?.fixed || false });
      }
    });
    return [...left, ...right];
  };

  // 增加点击刷新按钮先计算时间差 保持用户选择的事件范围
  const handleReload = () => {
    customTimeRef?.current?.refresh();
  };

  const renderConfig = () => {
    return (
      <div className="dashboard-config">
        <SyncOutlined spin={isLoading} className="dashboard-config-icon" onClick={() => handleReload()} />
        <span className="dashboard-config-line"></span>
        <CustomTimeRangePicker ref={customTimeRef} onChange={handleTimeChange} />
        <MetricsConfig title="集群" value={metrics} optionList={sortMetrics()} cb={setIndexConfigCheckedData} defaultCheckedData={[]} />
      </div>
    );
  };

  const getLineData = async (topNu?: number, metricsTypes?: string[]) => {
    const filterLine = ["health", "clusterElapsedTimeGte5Min", "shardNum"];
    const filterTable = ["clusterElapsedTimeGte5Min", "shardNum"];
    const whiteList: string[] = ["refresh", "merge", "write", "management", "search", "flush"];
    const lineMetrics = metrics.filter((item: any) => !filterLine.includes(item) && !whiteList.includes(item));
    const tableMetrics = metrics.filter((item: any) => filterTable.includes(item));
    const whiteMetrics = metrics.filter((item: any) => whiteList.includes(item));
    setIsLoading(true);
    setLoading(metricsTypes || metrics);
    const params: ILineParams = {
      metricsTypes: metricsTypes || lineMetrics,
      // 目前固定为avg
      aggType: "avg",
      startTime: rangeTime[0],
      endTime: rangeTime[1],
      topNu: topNu || 5,
    };
    try {
      let metricsList = [];
      if (params.metricsTypes.length) {
        metricsList = await clusterLine(params);
      }
      if (metrics.includes("clusterElapsedTimeGte5Min") || metrics.includes("shardNum")) {
        const list = await getListData(tableMetrics);
        metricsList.push(...list);
      }
      if (whiteMetrics.length) {
        params.metricsTypes = whiteMetrics;
        const list = await clusterThreadPoolQueue(params);
        metricsList.push(...list);
      }
      if (!metricsList || metricsList.length === 0 || metricsList[0]?.metricsContents?.length === 0) {
        setData({});
        setIsLoading(false);
        setLoading([]);
        return;
      }
      if (metricsTypes) {
        // 如果传入指标就不初始化data直接在这里return
        const cloneData = cloneDeep(data);
        metricsList.forEach((item) => {
          // 针对Gateway, clusterElapsedTimeGte5Min 做特殊处理
          if (item.type == "gatewaySucPer" || item.type == "gatewayFailedPer") {
            cloneData[item.type] = getOption({ metrics: item, configData: clusterMetrics, isGatewayLink: true });
          } else if (item.type === "clusterElapsedTimeGte5Min" || item.type === "shardNum") {
            cloneData[item.type] = item;
          } else if (whiteList.includes(item.type)) {
            cloneData[item.type] = getOption({ metrics: item, configData: clusterMetrics });
          } else {
            cloneData[item.type] = getOption({ metrics: item, configData: clusterMetrics, isClusterLink: true });
          }
        });
        setData(cloneData);
        return;
      }
      const datas = {};

      metricsList.forEach((item) => {
        // 针对Gateway, clusterElapsedTimeGte5Min 做特殊处理
        if (item.type == "gatewaySucPer" || item.type == "gatewayFailedPer") {
          datas[item.type] = getOption({ metrics: item, configData: clusterMetrics, isGatewayLink: true });
        } else if (item.type === "clusterElapsedTimeGte5Min" || item.type === "shardNum") {
          datas[item.type] = item;
        } else if (whiteList.includes(item.type)) {
          datas[item.type] = getOption({ metrics: item, configData: clusterMetrics });
        } else {
          datas[item.type] = getOption({ metrics: item, configData: clusterMetrics, isClusterLink: true });
        }
      });
      setData(datas);
    } catch (error) {
      setIsLoading(false);
      setLoading([]);
      console.log("render line", error);
    } finally {
      setIsLoading(false);
      setLoading([]);
    }
  };

  const getListData = async (metricsTypes) => {
    let listParams = {
      metricsTypes,
      aggType: "avg",
      orderByDesc: true,
    };
    let list = await clusterList(listParams);
    return list;
  };

  const sortEnd = ({ oldIndex, newIndex }) => {
    if (metrics[newIndex] === "health") {
      return;
    }
    if (newIndex === oldIndex) {
      return;
    }
    const listsNew = arrayMoveImmutable(metrics, oldIndex, newIndex);
    setCheckedList("cluster", listsNew);
    setMetrics(listsNew);
  };

  const renderDragItem = (item) => {
    if (item === "health") return <PieCharts ref={ref} key={"health"} dictionary={dictionary?.clusterData} />;
    if (item === "clusterElapsedTimeGte5Min" || item === "shardNum") {
      let list = data[item]?.metricListContents;
      let dataSource = (list || []).map((item) => {
        return {
          ...item,
          key: item.clusterPhyName,
        };
      });
      return (
        <TableCard
          columns={clusterMetrics[item]?.columns}
          dataSource={dataSource}
          title={clusterMetrics[item]?.name}
          dictionary={dictionary?.clusterData?.[item]}
          isLoading={loading.includes(item)}
          key={item}
        />
      );
    }
    return (
      <Line
        title={data?.[item]?.title?.text || clusterMetrics[item]?.title()}
        index={item}
        key={item}
        option={data[item] || {}}
        isLoading={loading.includes(item)}
        cb={getLineData}
        dictionary={dictionary?.clusterData?.[item]}
        topN={topN}
      />
    );
  };

  const renderContent = () => {
    const el = document.getElementsByClassName("dashboard-overview-content-line-container")?.[0];
    return (
      <DragGroup
        sortableContainerProps={{
          onSortEnd: (args) => sortEnd({ ...args }),
          axis: "xy",
          distance: el ? el.clientWidth - 80 : 150,
        }}
        gridProps={{
          grid: 8,
          gutter: [10, 10],
        }}
      >
        {metrics.map((item: any, i) => {
          return renderDragItem(item);
        })}
      </DragGroup>
    );
  };

  const getAsyncCheckedList = async () => {
    try {
      const checkedList = await getCheckedList("cluster");
      setMetricsLoading(false);
      if (!checkedList || checkedList.length === 0) {
        ref.current?.getData();
        getLineData();
      } else {
        // 以下需暂时过滤掉
        const filterList = [
          "reqUprushNum",
          "httpNum",
          "gatewaySucPer",
          "write",
          "search",
          "refresh",
          "flush",
          "management",
          "merge",
          "clusterElapsedTime",
          "indexReqNum",
          "docUprushNum",
        ];
        setMetrics([...(new Set(checkedList.filter((item) => filterList.indexOf(item) === -1)) as any)]);
      }
    } catch (error) {
      setMetricsLoading(false);
      setMetrics(metrics);
      console.log("cluster-kanban node-view 获取配置下项失败", error);
    }
  };

  const linkToClusterPage = (cluster: string) => {
    props.history.push(`/indicators/cluster?cluster=${cluster}`);
  };

  const linkToNodePage = (node: string, cluster: string) => {
    props.history.push(`/indicators/cluster?cluster=${cluster}&node=${node}&#node`);
  };

  const linkToGatewayPage = () => {
    props.history.push(`/indicators/gateway`);
  };

  useEffect(() => {
    window["clusterlink"] = (cluster: string) => {
      linkToClusterPage(cluster);
    };
    window["gatewaylink"] = () => {
      linkToGatewayPage();
    };
    window["nodelink"] = (node: string, cluster: string) => {
      linkToNodePage(node, cluster);
    };
  }, []);

  const setDymanicMetrics = () => {
    if (props.dashboard?.dymanicConfigMetrics?.length) {
      const res = props.dashboard?.dymanicConfigMetrics;
      res.forEach((item) => {
        try {
          const itemValue = JSON.parse(item?.value);
          switch (item.valueName) {
            case "cluster.shard.num_threshold":
              clusterMetrics.shardNum.name = `shard个数大于${itemValue.value ?? ""}集群列表`;
              break;
            case "cluster.metric.collector.delayed_threshold ":
              //指标采集延时大于5分钟集群列表
              clusterMetrics.clusterElapsedTimeGte5Min.name = `指标采集延时大于${itemValue.value ?? ""}${itemValue.unit ?? ""}集群列表`;
              break;
            default:
              break;
          }
        } catch (error) {
          console.log(error, `JSON.parse解析${item?.valueName}错误，请检查动态配置中配置项书写格式`);
        }
      });
    }
  };

  // 加载选中配置
  useEffect(() => {
    getAsyncCheckedList();
  }, []);

  useEffect(() => {
    setDymanicMetrics();
  }, [props.dashboard?.dymanicConfigMetrics]);

  useEffect(() => {
    reload();
  }, [rangeTime, metrics]);

  return (
    <div className="dashboard-index-view">
      {renderConfig()}
      {renderContent()}
    </div>
  );
});

export default connect(mapStateToProps, null)(Cluster);
