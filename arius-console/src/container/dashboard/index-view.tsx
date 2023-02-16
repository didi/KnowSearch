import React, { useState, useEffect, useRef, useContext } from "react";
import { SyncOutlined } from "@ant-design/icons";
import { MetricsConfig } from "./components/metricsConfig";
import { getOption, indexViewMetrics } from "./config";
import { ILineParams, indexLine, indexList, setCheckedList, getCheckedList, templatelist } from "api/dashboard";
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

const IndexView = withRouter((props: any) => {
  const dictionary = useContext(DashboardContext);

  const currentTime = new Date().getTime();
  const [rangeTime, setRangeTime] = useState([currentTime - ONE_HOUR, currentTime]);

  const [loading, setLoading] = useState(Object.keys(indexViewMetrics));
  const [metrics, setMetrics] = useState(Object.keys(indexViewMetrics));
  const [data, setData] = useState({});
  const [listData, setListData] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [metricsLoading, setMetricsLoading] = useState(false);
  const [indexViewInfo, setIndexViewInfo] = useState(null);
  const topN = useRef({});
  const customTimeRef = useRef(null);

  const handleTimeChange = (times: number[], isCustomTime = false) => {
    setRangeTime(times);
  };

  const reload = () => {
    if (metricsLoading) return;

    getLineData();
    getListData();
    getTplListData();
  };

  const setIndexConfigCheckedData = (checkvalue) => {
    setMetrics(checkvalue);
    setCheckedList("index", checkvalue);
  };

  const sortMetrics = () => {
    const left = metrics.map((item) => ({ name: indexViewMetrics[item]?.name, value: item }));
    const right = [];
    Object.keys(indexViewMetrics).forEach((item) => {
      if (!metrics.includes(item)) {
        right.push({ name: indexViewMetrics[item]?.name, value: item });
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
        <MetricsConfig title="索引" value={metrics} optionList={sortMetrics()} defaultCheckedData={[]} cb={setIndexConfigCheckedData} />
      </div>
    );
  };

  const getLineData = async (topNu?: number, metricsTypes?: string[]) => {
    //获取线性图标数据，通过columns，有columns代表是表格
    const lineMetrics = [];
    metrics.forEach((item) => {
      if (indexViewMetrics && indexViewMetrics[item].columns) {
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
    if (!params.metricsTypes.length) {
      return;
    }
    try {
      setIsLoading(true);
      setLoading((state) => [...state, ...(metricsTypes || lineMetrics)]);
      const metricsList = await indexLine(params);
      if (!metricsList || metricsList.length === 0) {
        setData({});
        return;
      }
      if (metricsTypes) {
        // 如果传入指标就不初始化data直接在这里return
        const cloneData = cloneDeep(data);
        metricsList.forEach((item) => {
          cloneData[item.type] = getOption({
            metrics: item,
            configData: indexViewMetrics,
            isClusterLink: true,
            clusterPhyName: (item.metricsContents || []).map((v) => v.cluster),
          });
        });
        setIsLoading(false);
        setLoading((state) => state.filter((item) => !(metricsTypes || lineMetrics).includes(item)));
        setData(cloneData);
        return;
      }
      const datas = {};
      metricsList.forEach((item) => {
        datas[item.type] = getOption({
          metrics: item,
          configData: indexViewMetrics,
          isClusterLink: true,
          clusterPhyName: (item.metricsContents || []).map((v) => v.cluster),
        });
      });
      setIsLoading(false);
      setLoading((state) => state.filter((item) => !(metricsTypes || lineMetrics).includes(item)));
      setData(datas);
    } catch (error) {
      setIsLoading(false);
      setLoading([]);
      console.log("render line", error);
    } finally {
      // setIsLoading(false);
    }
  };

  const getTplListData = async () => {
    setIsLoading(true);
    const datas = {};
    const TplListMetrics = [];
    const loadingList = [];
    metrics.forEach((item) => {
      if (indexViewMetrics && indexViewMetrics[item].columns) {
        if (indexViewMetrics[item].as) {
          TplListMetrics.push(indexViewMetrics[item].as);
          loadingList.push(item);
        }
      }
    });
    if (TplListMetrics && TplListMetrics.length === 0) {
      return;
    }
    setLoading((state) => [...state, ...loadingList]);
    const TplParams: any = {
      metricsTypes: TplListMetrics,
      aggType: "avg",
      orderByDesc: true,
    };
    try {
      const tplMetricsList = await templatelist(TplParams);
      if (!tplMetricsList || tplMetricsList.length === 0) {
        // setListData({});
        setLoading((state) => state.filter((item) => !loadingList.includes(item)));
        return;
      }
      metrics.forEach((item) => {
        tplMetricsList.forEach((v) => {
          if (indexViewMetrics[item].as === v.type) {
            datas[item] = v;
          }
        });
      });
      setListData((state) => ({ ...state, ...datas }));
      setIsLoading(false);
      setLoading((state) => state.filter((item) => !loadingList.includes(item)));
    } catch (err) {
      console.log(err);
    }
  };

  const getListData = async () => {
    const ListMetrics = [];
    metrics.forEach((item) => {
      if (indexViewMetrics && indexViewMetrics[item].columns) {
        if (!indexViewMetrics[item].as) {
          if (item != "smallShard") {
            ListMetrics.push(item);
          }
        }
      }
    });
    if (ListMetrics && ListMetrics.length === 0) {
      return;
    }
    const params: any = {
      metricsTypes: ListMetrics,
      aggType: "avg",
      orderByDesc: true,
    };
    const smallShardParams: any = {
      metricsTypes: ["smallShard"],
      aggType: "avg",
      orderByDesc: false,
    };

    try {
      setIsLoading(true);
      setLoading((state) => [...state, ...ListMetrics]);
      const datas = {};
      const metricsList = await indexList(params);
      if (metrics.includes("smallShard")) {
        const smallShardMetricsList = await indexList(smallShardParams);
        metricsList.push(...smallShardMetricsList);
        ListMetrics.push("smallShard");
      }
      if (!metricsList || metricsList.length === 0) {
        // setListData({});
        setLoading((state) => state.filter((item) => !ListMetrics.includes(item)));
        return;
      }
      metricsList.forEach((item) => {
        datas[item.type] = item;
      });
      setListData((state) => ({ ...state, ...datas }));
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
    const listsNew = arrayMoveImmutable(metrics, oldIndex, newIndex);
    setCheckedList("index", listsNew);
    setMetrics(listsNew);
  };

  const renderContent = () => {
    return (
      <DragGroup
        dragContainerProps={{
          onSortEnd: (args) => sortEnd({ ...args }),
          axis: "xy",
          distance: 100,
        }}
        containerProps={{
          grid: 8,
          gutter: [12, 12],
        }}
      >
        {metrics.map((item: any, i) => {
          // console.log("--dictionary?.indexData?.[item]", dictionary?.indexData, item);
          if (indexViewMetrics[item]?.columns) {
            let list = listData[item]?.metricListContents;
            let dataSource = (list || []).map((item) => {
              return {
                ...item,
                key: `${item.clusterPhyName}-${item.name}`,
              };
            });
            return (
              <TableCard
                columns={indexViewMetrics[item]?.columns}
                dataSource={dataSource}
                title={indexViewMetrics[item]?.name}
                // unit={item.unit}
                isLoading={loading.includes(item)}
                key={item}
                dictionary={dictionary?.indexData?.[item]}
              />
            );
          }
          return (
            <Line
              title={data[item]?.title?.text || indexViewMetrics[item]?.title()}
              index={item}
              key={item}
              option={data[item] || {}}
              isLoading={loading.includes(item)}
              cb={getLineData}
              topN={topN}
              dictionary={dictionary?.indexData?.[item]}
            />
          );
        })}
      </DragGroup>
    );
  };

  const getAsyncCheckedList = async () => {
    try {
      setMetricsLoading(true);
      const checkedList = await getCheckedList("index");
      if (!checkedList || checkedList.length === 0) {
        setMetrics(metrics);
      } else {
        // 以下需暂时过滤掉
        const filterList = ["reqUprushNum", "docUprushNum"];
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
    window["clusterlink"] = (index: string, clusterName: string) => {
      props.history.push(`/indicators/cluster?cluster=${clusterName}&index=${index}#index`);
    };
  };

  const setDymanicMetrics = () => {
    if (props.dashboard?.dymanicConfigMetrics?.length) {
      const res = props.dashboard?.dymanicConfigMetrics;
      res.forEach((item) => {
        try {
          const itemValue = JSON.parse(item.value);
          switch (item.valueName) {
            case "index.segment.num_threshold":
              //name: "索引Segments个数",
              indexViewMetrics.segmentNum.name = `segments个数大于${itemValue.value ?? ""}索引列表`;
              break;
            case "index.mapping.num_threshold":
              //name: "索引Mapping字段个数",
              indexViewMetrics.mappingNum.name = `mapping字段个数大于${itemValue.value ?? ""}索引列表`;
              break;
            case "index.template.segment_num_threshold":
              //name: "索引模板Segments个数",
              indexViewMetrics.tplSegmentNum.name = `segments个数大于${itemValue.value ?? ""}索引模版列表`;
              break;
            case "index.segment.memory_size_threshold":
              //name: "索引Segments内存大小（MB）",
              indexViewMetrics.segmentMemSize.name = `segments内存大于${itemValue.value ?? ""}${itemValue.unit ?? ""}索引列表`;
              break;
            case "index.template.segment_memory_size_threshold":
              //name: "索引模板Segments内存大小（MB）",
              indexViewMetrics.tplSegmentMemSize.name = `segments内存大于${itemValue.value ?? ""}${itemValue.unit ?? ""}索引模版列表`;
              break;
            case "index.shard.small_threshold":
              //小Shard列表阈值定义
              indexViewMetrics.smallShard.name = `单个shard小于${itemValue.value ?? ""}${itemValue.unit ?? ""}索引列表`;
              break;
            case "index.shard.big_threshold":
              //index.shard.big_threshold
              indexViewMetrics.bigShard.name = `单个shard大于${itemValue.value ?? ""}${itemValue.unit ?? ""}索引列表`;
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
    setDymanicMetrics();
    setIndexViewInfo(indexViewMetrics);
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

export default connect(mapStateToProps, null)(IndexView);
