import React, { useState, useEffect, useRef } from 'react';
import DRangeTime from './../../d1-packages/d-range-time';
import { SyncOutlined } from '@ant-design/icons'
import { MetricsConfig } from "./components/metricsConfig";
import { getOption, nodeMetrics } from "./config";
import { ILineParams, clusterThreadPoolQueue, nodeLine, nodeList, setCheckedList, getCheckedList } from 'api/dashboard';
import TableCard from './components/table'
import './index.less'
import { cloneDeep } from 'lodash';
import DragGroup from './../../packages/drag-group/DragGroup';
import { arrayMoveImmutable } from 'array-move';
import { Line } from './components/line';
import { withRouter } from "react-router-dom"
const ONE_HOUR = 1000 * 60 * 60;
const Node = withRouter((props: any) => {
  const currentTime = new Date().getTime();
  const [startTime, setStartTime] = useState(currentTime - ONE_HOUR);
  const [endTime, setEndTime] = useState(currentTime);
  const [loading, setLoading] = useState([]);
  const [metrics, setMetrics] = useState(Object.keys(nodeMetrics));
  const [data, setData] = useState({})
  const [listdata, setListData] = useState({})
  const [isLoading, setIsLoading] = useState(false);
  const topN = useRef({});

  const handleTimeChange = (times) => {
    if (times) {
      setStartTime(times[0]);
      setEndTime(times[1]);
    }
  }

  const reload = () => {
    getLineData();
    getListData();
    // getTplListData();
  }

  const setIndexConfigCheckedData = (checkvalue) => {
    setMetrics(checkvalue);
    setCheckedList('node', checkvalue);
  };

  const sortMetrics = () => {
    const left = metrics.map(item => ({ name: nodeMetrics[item]?.name, value: item }))
    const right = [];
    Object.keys(nodeMetrics).forEach(item => {
      if (!metrics.includes(item)) {
        right.push({ name: nodeMetrics[item]?.name, value: item })
      }
    })
    return [...left, ...right]
  }

  const handleReload = () => {
    const time = endTime - startTime;
    const currentTime = new Date().getTime();
    setStartTime(currentTime - time);
    setEndTime(currentTime);
  }

  const renderConfig = () => {
    return (
      <div className="dashboard-config">
        <SyncOutlined spin={isLoading} className="dashboard-config-icon" onClick={() => handleReload()} />
        <span className="dashboard-config-line"></span>
        <DRangeTime timeChange={handleTimeChange} popoverClassName="dashborad-popover" />
        <MetricsConfig title="节点" value={metrics} optionList={sortMetrics()} defaultCheckedData={[]} cb={setIndexConfigCheckedData} />
      </div>
    )
  }

  const injectOptions = (row: any) => {
    // 不需要注入clusterPhyName白名单
    const whiteList: string[] = ['refresh', 'merge', 'write', 'management', 'search', 'flush']
    let options: any = {
      clusterPhyName: (row.metricsContents || []).map(v => v.cluster)
    }
    if (whiteList.includes(row.type)) options.linkWithSeriesName = false
    return options
  }

  const getLineData = async (topNu?: number, metricsTypes?: string[]) => {
    const lineMetrics = [];
    const asLineMetrics = [];
    metrics.forEach(item => {
      if (nodeMetrics && nodeMetrics[item].columns) {
        return
      }
      if (nodeMetrics && nodeMetrics[item].as) {
        asLineMetrics.push(item)
        return
      }
      lineMetrics.push(item);
    })
    const Params: ILineParams = {
      metricsTypes: metricsTypes || lineMetrics,
      // 目前固定为avg
      aggType: 'avg',
      startTime,
      endTime,
      topNu: topNu || 5
    }
    const asParams = {
      metricsTypes: metricsTypes || asLineMetrics,
      // 目前固定为avg
      aggType: 'avg',
      startTime,
      endTime,
      topNu: topNu || 5
    }
    if (!Params.metricsTypes.length && !asParams.metricsTypes.length) {
      return
    }
    try {
      setIsLoading(true);
      setLoading((state) => [...state, ...(metricsTypes || lineMetrics)]);
      const metricsList = JSON.stringify(metricsTypes) === JSON.stringify(asLineMetrics) ? await nodeLine(asParams) : Params.metricsTypes.length ? await clusterThreadPoolQueue(Params) : [];
      let asList = [];
      if (asLineMetrics.length && !metricsTypes) {
        asList = await nodeLine(asParams);
      }
      setIsLoading(false);
      setLoading((state) => state.filter(item => !(metricsTypes || lineMetrics).includes(item)));
      if (metricsTypes) {
        // 如果传入指标就不初始化data直接在这里return
        const cloneData = cloneDeep(data);
        metricsList.forEach(item => {
          cloneData[item.type] = getOption({ metrics: item, configData: nodeMetrics, isClusterLink: true, ...injectOptions(item) });
        });
        setData(cloneData);
        return;
      }
      const datas = {};
      metricsList.forEach(item => {
        datas[item.type] = getOption({ metrics: item, configData: nodeMetrics, isClusterLink: true, ...injectOptions(item) });
      });
      asList.forEach(item => {
        datas[item.type] = getOption({ metrics: item, configData: nodeMetrics, isClusterLink: true, ...injectOptions(item) });
      });
      setData(datas);
    } catch (error) {
      setIsLoading(false);
      setLoading([]);
      console.log("render line", error);
    } finally {
      // setIsLoading(false);
    }
  }

  const getListData = async () => {
    const ListMetrics = [];
    metrics.forEach(item => {
      if (nodeMetrics && nodeMetrics[item].columns) {
        ListMetrics.push(item);
      }
    })
    if (!ListMetrics.length) return
    const Params: any = {
      metricsTypes: ListMetrics,
      aggType: 'avg',
      orderByDesc: true,
    }

    try {
      setIsLoading(true);
      setLoading((state) => [...state, ...ListMetrics]);
      const datas = {};
      const metricsList = await nodeList(Params);
      if (
        !metricsList ||
        metricsList.length === 0 ||
        metricsList[0]?.metricsContents?.length === 0
      ) {
        ListMetrics.forEach(item => {
          data[item];
        })
        setListData({});
        return;
      }
      metricsList.forEach(item => {
        datas[item.type] = item
      });
      setListData(datas);
      setIsLoading(false);
      setLoading((state) => state.filter(item => !ListMetrics.includes(item)));
    } catch (error) {
      setIsLoading(false);
      setLoading([]);
      console.log("render line", error);
    } finally {
      // setIsLoading(false);
    }
  }

  const sortEnd = ({ oldIndex, newIndex }) => {
    const listsNew = arrayMoveImmutable(metrics, oldIndex, newIndex)
    setCheckedList('node', listsNew);
    setMetrics(listsNew)
  };

  const renderContent = () => {
    return (
      <DragGroup
        dragContainerProps={{
          onSortEnd: (args) => sortEnd({ ...args }),
          axis: "xy",
          distance: 100
        }}
        containerProps={{
          grid: 8,
          gutter: [12, 12],
        }}
      >
        {
          metrics.map((item: any, i) =>
            nodeMetrics[item]?.columns ? <TableCard
              columns={nodeMetrics[item]?.columns}
              dataSource={listdata[item]?.metricListContents}
              title={nodeMetrics[item]?.name}
              tooltip={nodeMetrics[item]?.tooltip}
              // unit={item.unit}
              isLoading={loading.includes(item)}
              key={item}
            />
              :
              <Line
                title={data[item]?.title?.text || nodeMetrics[item]?.title()}
                index={item}
                key={item}
                option={data[item] || {}}
                isLoading={loading.includes(item)}
                cb={getLineData}
                tooltip={nodeMetrics[item]?.tooltip || ''}
                topN={topN}
              />
          )
        }
      </DragGroup>
    )
  }

  const getAsyncCheckedList = async () => {
    try {
      const checkedList = await getCheckedList('node');
      if (!checkedList || checkedList.length === 0) {
        setMetrics(metrics);
      } else {
        setMetrics(checkedList);
      }
    } catch (error) {
      setMetrics(metrics);
      console.log("cluster-kanban node-view 获取配置下项失败", error);
    }
  };
  const linkToClusterPage = () => {
    window['clusterlink'] = (node: string, clusterName: string) => {
      if (node) {
        props.history.push(`/indicators/cluster?cluster=${clusterName}&node=${node}#node`)
      } else {
        props.history.push(`/indicators/cluster?cluster=${clusterName}#node`)
      }
    };
  }
  // 加载选中配置
  useEffect(() => {
    linkToClusterPage()
    if ((window as any).setProjectList === true) {
      getAsyncCheckedList();
    }
  }, []);

  useEffect(() => {
    if ((window as any).setProjectList === true) {
      reload();
    }
  }, [startTime, endTime, metrics])

  return (
    <div className="dashboard-index-view">
      {renderConfig()}
      {renderContent()}
    </div>
  )
})

export default Node;