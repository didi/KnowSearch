import React, { useState, useEffect, useRef, useContext } from "react";
import { SyncOutlined } from "@ant-design/icons";
import { MetricsConfig } from "./components/metricsConfig";
import { getOption, nodeMetrics } from "./config";
import { ILineParams, nodeLine, nodeList, setCheckedList, getCheckedList } from "api/dashboard";
import TableCard from "./components/table";
import "./index.less";
import { cloneDeep } from "lodash";
import DragGroup from "../../d1-packages/drag-group/DragGroup";
import { arrayMoveImmutable } from "array-move";
import { Line } from "./components/line";
import { withRouter } from "react-router-dom";
import { connect } from "react-redux";
import { CustomTimeRangePicker } from "component/TimeRangePicker";
import { DashboardContext } from "./Operation";

const ONE_HOUR = 1000 * 60 * 60;

const mapStateToProps = (state) => ({
  dashboard: state.dashBoard,
});

const Node = withRouter((props: any) => {
  const dictionary = useContext(DashboardContext);

  const currentTime = new Date().getTime();
  const [rangeTime, setRangeTime] = useState([currentTime - ONE_HOUR, currentTime]);
  const [loading, setLoading] = useState([]);
  const [metrics, setMetrics] = useState(Object.keys(nodeMetrics));
  const [data, setData] = useState({});
  const [listdata, setListData] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [metricsLoading, setMetricsLoading] = useState(false);
  const [nodeInfo, setNodeInfo] = useState(null);
  const topN = useRef({});
  const customTimeRef = useRef(null);

  const handleTimeChange = (times, isCustomTime) => {
    setRangeTime(times);
  };

  const reload = () => {
    if (metricsLoading) return;

    getLineData();
    getListData();
    // getTplListData();
  };

  const setIndexConfigCheckedData = (checkvalue) => {
    setMetrics(checkvalue);
    setCheckedList("node", checkvalue);
  };

  const sortMetrics = () => {
    const left = metrics.map((item) => ({ name: nodeMetrics[item]?.name, value: item }));
    const right = [];
    Object.keys(nodeMetrics).forEach((item) => {
      if (!metrics.includes(item)) {
        right.push({ name: nodeMetrics[item]?.name, value: item });
      }
    });
    return [...left, ...right];
  };

  const handleReload = () => {
    customTimeRef?.current?.refresh();
  };

  const renderConfig = () => {
    return (
      <div className="dashboard-config">
        <SyncOutlined spin={isLoading} className="dashboard-config-icon" onClick={() => handleReload()} />
        <span className="dashboard-config-line"></span>
        <CustomTimeRangePicker ref={customTimeRef} onChange={handleTimeChange} />
        <MetricsConfig title="节点" value={metrics} optionList={sortMetrics()} defaultCheckedData={[]} cb={setIndexConfigCheckedData} />
      </div>
    );
  };

  const injectOptions = (row: any) => {
    // 不需要注入clusterPhyName白名单
    const whiteList: string[] = ["refresh", "merge", "write", "management", "search", "flush"];
    let options: any = {
      clusterPhyName: (row.metricsContents || []).map((v) => v.cluster),
    };
    if (whiteList.includes(row.type)) options.linkWithSeriesName = false;
    return options;
  };

  const getLineData = async (topNu?: number, metricsTypes?: string[]) => {
    const lineMetrics = [];
    const asLineMetrics = [];
    metrics.forEach((item) => {
      if (nodeMetrics && nodeMetrics[item].columns) {
        return;
      }
      if (nodeMetrics && nodeMetrics[item].as) {
        asLineMetrics.push(item);
        return;
      }
      lineMetrics.push(item);
    });
    const params: ILineParams = {
      metricsTypes: metricsTypes || lineMetrics,
      // 目前固定为avg
      aggType: "avg",
      startTime: rangeTime[0],
      endTime: rangeTime[1],
      topNu: topNu || 5,
    };
    const asParams = {
      metricsTypes: metricsTypes || asLineMetrics,
      // 目前固定为avg
      aggType: "avg",
      startTime: rangeTime[0],
      endTime: rangeTime[1],
      topNu: topNu || 5,
    };
    if (!params.metricsTypes.length && !asParams.metricsTypes.length) {
      return;
    }
    try {
      setIsLoading(true);
      setLoading((state) => [...state, ...(metricsTypes || lineMetrics), ...asLineMetrics]);
      const metricsList = JSON.stringify(metricsTypes) === JSON.stringify(asLineMetrics) ? await nodeLine(asParams) : [];
      let asList = [];
      if (asLineMetrics.length && !metricsTypes) {
        asList = await nodeLine(asParams);
      }
      setIsLoading(false);
      setLoading((state) => state.filter((item) => !(metricsTypes || lineMetrics).includes(item)));
      if (metricsTypes) {
        // 如果传入指标就不初始化data直接在这里return
        const cloneData = cloneDeep(data);
        metricsList.forEach((item) => {
          cloneData[item.type] = getOption({ metrics: item, configData: nodeMetrics, isClusterLink: true, ...injectOptions(item) });
        });
        setData(cloneData);
        return;
      }
      const datas = {};
      metricsList.forEach((item) => {
        datas[item.type] = getOption({ metrics: item, configData: nodeMetrics, isClusterLink: true, ...injectOptions(item) });
      });
      asList.forEach((item) => {
        datas[item.type] = getOption({ metrics: item, configData: nodeMetrics, isClusterLink: true, ...injectOptions(item) });
      });
      setData(datas);
    } catch (error) {
      setIsLoading(false);
      setLoading([]);
      console.log("render line", error);
    } finally {
      setLoading([]);
    }
  };

  const getListData = async () => {
    const ListMetrics = [];
    metrics.forEach((item) => {
      if (nodeMetrics && nodeMetrics[item].columns) {
        ListMetrics.push(item);
      }
    });
    if (!ListMetrics.length) return;
    const params: any = {
      metricsTypes: ListMetrics,
      aggType: "avg",
      orderByDesc: true,
    };

    try {
      setIsLoading(true);
      setLoading((state) => [...state, ...ListMetrics]);
      const datas = {};
      const metricsList = await nodeList(params);
      if (!metricsList || metricsList.length === 0 || metricsList[0]?.metricsContents?.length === 0) {
        ListMetrics.forEach((item) => {
          data[item];
        });
        setListData({});
        return;
      }
      metricsList.forEach((item) => {
        datas[item.type] = item;
      });
      setListData(datas);
      setIsLoading(false);
      setLoading((state) => state.filter((item) => !ListMetrics.includes(item)));
    } catch (error) {
      setIsLoading(false);
      setLoading([]);
      console.log("render line", error);
    } finally {
      // setIsLoading(false);
    }
  };

  const sortEnd = ({ oldIndex, newIndex }) => {
    if (newIndex === oldIndex) {
      return;
    }
    const listsNew = arrayMoveImmutable(metrics, oldIndex, newIndex);
    setCheckedList("node", listsNew);
    setMetrics(listsNew);
  };
  const renderDragItem = (item) => {
    if (nodeMetrics[item]?.columns) {
      let list = listdata[item]?.metricListContents;
      let dataSource = (list || []).map((item) => {
        return {
          ...item,
          key: `${item.clusterPhyName}-${item.name}`,
        };
      });
      return (
        <TableCard
          columns={nodeMetrics[item]?.columns}
          dataSource={dataSource}
          title={nodeMetrics[item]?.name}
          // unit={item.unit}
          isLoading={loading.includes(item)}
          key={item}
          dictionary={dictionary?.nodeData?.[item]}
        />
      );
    }
    return (
      <Line
        title={data[item]?.title?.text || nodeMetrics[item]?.title()}
        index={item}
        key={item}
        option={data[item] || {}}
        isLoading={loading.includes(item)}
        cb={getLineData}
        topN={topN}
        dictionary={dictionary?.nodeData?.[item]}
      />
    );
  };

  const renderContent = () => {
    const el = document.getElementsByClassName("dashboard-overview-content-line-container")?.[0];
    return (
      <DragGroup
        dragContainerProps={{
          onSortEnd: (args) => sortEnd({ ...args }),
          axis: "xy",
          distance: el ? el.clientWidth - 80 : 150,
        }}
        containerProps={{
          grid: 8,
          gutter: [12, 12],
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
      setMetricsLoading(true);
      const checkedList = await getCheckedList("node");
      if (!checkedList || checkedList.length === 0) {
        setMetrics(metrics);
      } else {
        // 以下需暂时过滤掉
        const filterList = ["dead"];
        setMetrics(checkedList.filter((item) => filterList.indexOf(item) === -1));
      }
    } catch (error) {
      setMetrics(metrics);
      console.log("cluster-kanban node-view 获取配置下项失败", error);
    } finally {
      setMetricsLoading(false);
    }
  };
  const linkToClusterPage = () => {
    window["clusterlink"] = (node: string, clusterName: string) => {
      if (node) {
        props.history.push(`/indicators/cluster?cluster=${clusterName}&node=${node}#node`);
      } else {
        props.history.push(`/indicators/cluster?cluster=${clusterName}#node`);
      }
    };
  };

  const setDymanicMetrics = () => {
    if (props.dashboard?.dymanicConfigMetrics?.length) {
      const res = props.dashboard?.dymanicConfigMetrics;
      res.forEach((item) => {
        try {
          const itemValue = JSON.parse(item.value);
          switch (item.valueName) {
            case "node.disk.used_percent_threshold":
              nodeMetrics.largeDiskUsage.name = `磁盘利用率超${itemValue.value ?? ""}${itemValue.unit ?? ""}节点列表`;
              if (dictionary?.nodeData?.largeDiskUsage) {
                dictionary.nodeData.largeDiskUsage.threshold = `磁盘利用率超${itemValue.value ?? ""}${itemValue.unit ?? ""}为超红线`;
              }
              break;
            case "node.jvm.heap.used_percent_threshold":
              // ex
              const target = res.find((row) => row.valueName === "node.jvm.heap.used_percent_time_duration_threshold") || {};
              const timeValue = target.value ? JSON.parse(target.value) : {};
              nodeMetrics.largeHead.name = `堆内存利用率超${itemValue.value ?? ""}${itemValue.unit ?? ""}节点列表`;
              if (dictionary?.nodeData?.largeHead) {
                dictionary.nodeData.largeHead.threshold = `堆内存利用率超${itemValue.value ?? ""}${itemValue.unit ?? ""}且持续${
                  timeValue?.value ?? ""
                }${timeValue?.unit ?? ""}为超红线`;
              }
              break;
            case "node.cpu.used_percent_threshold":
              // ex
              const target1 = res.find((row) => row.valueName === "node.cpu.used_percent_threshold_time_duration_threshold") || {};
              const time1Value = target1.value ? JSON.parse(target1.value) : {};
              nodeMetrics.largeCpuUsage.name = `CPU利用率超${itemValue.value ?? ""}${itemValue.unit ?? ""}节点列表`;
              if (dictionary?.nodeData?.largeCpuUsage) {
                dictionary.nodeData.largeCpuUsage.threshold = `CPU利用率超${itemValue.value ?? ""}${itemValue.unit ?? ""}且持续${
                  time1Value?.value ?? ""
                }${time1Value?.unit ?? ""}为超红线`;
              }
              break;
            case "node.shard.num_threshold":
              nodeMetrics.shardNum.name = `节点分片个数大于${itemValue.value ?? ""}节点列表`;
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
    linkToClusterPage();
    getAsyncCheckedList();
    setNodeInfo(nodeMetrics);
  }, []);

  useEffect(() => {
    setDymanicMetrics();
  }, [props.dashboard?.dymanicConfigMetrics, dictionary]);

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

export default connect(mapStateToProps, null)(Node);
