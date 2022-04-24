import React, { useState, useEffect, useRef } from 'react';
import DRangeTime from './../../d1-packages/d-range-time';
import { SyncOutlined } from '@ant-design/icons'
import { MetricsConfig } from "./components/metricsConfig";
import { clusterMetrics, getOption } from "./config";
import { PieCharts } from './components/pieCharts'
import "./index.less"
import { ILineParams, clusterLine, setCheckedList, getCheckedList } from 'api/dashboard';
import { Line } from './components/line';
import { cloneDeep } from 'lodash';
import DragGroup from './../../packages/drag-group/DragGroup';
import { arrayMoveImmutable } from 'array-move';
import { withRouter } from 'react-router-dom';

const ONE_HOUR = 1000 * 60 * 60;

const Cluster = withRouter((props: any) => {
  const currentTime = new Date().getTime();
  const [startTime, setStartTime] = useState(currentTime - ONE_HOUR);
  const [endTime, setEndTime] = useState(currentTime);
  const [loading, setLoading] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [metrics, setMetrics] = useState(['health', ...Object.keys(clusterMetrics).filter(item => item != 'health')]);
  const [data, setData] = useState({});
  const ref: any = useRef();
  const topN = useRef({});

  const handleTimeChange = (times: number[]) => {
    if (times) {
      setStartTime(times[0]);
      setEndTime(times[1]);
    }
  }

  const setIndexConfigCheckedData = (checkvalue) => {
    setMetrics(['health', ...checkvalue.filter(item => item != 'health')]);
    setCheckedList('cluster', ['health', ...checkvalue.filter(item => item != 'health')]);
  };

  const reload = () => {
    ref.current?.getData();
    getLineData();
  }

  const sortMetrics = () => {
    const left = metrics.map(item => ({ name: clusterMetrics[item]?.name, value: item, fixed: clusterMetrics[item]?.fixed || false }))
    const right = [];
    Object.keys(clusterMetrics).forEach(item => {
      if (!metrics.includes(item)) {
        right.push({ name: clusterMetrics[item]?.name, value: item, fixed: clusterMetrics[item]?.fixed || false })
      }
    })
    return [...left, ...right]
  }

  // 增加点击刷新按钮先计算时间差 保持用户选择的事件范围
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
        <MetricsConfig title="集群" value={metrics} optionList={sortMetrics()} cb={setIndexConfigCheckedData} defaultCheckedData={[]} />
      </div>
    )
  }

  const getLineData = async (topNu?: number, metricsTypes?: string[]) => {
    const lineMetrics = metrics.filter((item: any) => item !== 'health');
    setIsLoading(true);
    setLoading(metricsTypes || lineMetrics);
    const Params: ILineParams = {
      metricsTypes: metricsTypes || lineMetrics,
      // 目前固定为avg
      aggType: 'avg',
      startTime,
      endTime,
      topNu: topNu || 5
    }
    try {
      const metricsList = await clusterLine(Params);
      if (
        !metricsList ||
        metricsList.length === 0 ||
        metricsList[0]?.metricsContents?.length === 0
      ) {
        setData({});
        setIsLoading(false);
        setLoading([]);
        return;
      }
      if (metricsTypes) {
        // 如果传入指标就不初始化data直接在这里return
        const cloneData = cloneDeep(data);
        metricsList.forEach(item => {
          // 针对Gateway做特殊处理
          if (item.type == 'gatewaySucPer' || item.type == 'gatewayFailedPer') {
            cloneData[item.type] = getOption({ metrics: item, configData: clusterMetrics, isGatewayLink: true });
          } else {
            cloneData[item.type] = getOption({ metrics: item, configData: clusterMetrics, isClusterLink: true });
          }
        });
        setData(cloneData);
        return;
      }
      const datas = {};
      metricsList.forEach(item => {
        // 针对Gateway做特殊处理
        if (item.type == 'gatewaySucPer' || item.type == 'gatewayFailedPer') {
          datas[item.type] = getOption({ metrics: item, configData: clusterMetrics, isGatewayLink: true });
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
  }


  const sortEnd = ({ oldIndex, newIndex }) => {
    if (metrics[newIndex] === 'health') {
      return
    }
    const listsNew = arrayMoveImmutable(metrics, oldIndex, newIndex)
    setCheckedList('cluster', listsNew);
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
          gutter: [10, 10],
        }}
      >
        {
          metrics.map((item: any, i) =>
            item === 'health' ? <PieCharts ref={ref} key={'health'} /> :
              <Line
                title={data?.[item]?.title?.text || clusterMetrics[item]?.title()}
                index={item}
                key={item}
                option={data[item] || {}}
                isLoading={loading.includes(item)}
                cb={getLineData}
                tooltip={clusterMetrics[item]?.tooltip || ''}
                topN={topN}
              />
          )
        }
      </DragGroup>
    )
  }

  const getAsyncCheckedList = async () => {
    try {
      const checkedList = await getCheckedList('cluster');
      if (!checkedList || checkedList.length === 0) {
        setMetrics(metrics);
      } else {
        setMetrics([...(new Set(checkedList) as any)]);
      }
    } catch (error) {
      setMetrics(metrics);
      console.log("cluster-kanban node-view 获取配置下项失败", error);
    }
  };

  const linkToClusterPage = (cluster) => {
    props.history.push(`/indicators/cluster?cluster=${cluster}`)
  }

  const linkToGatewayPage = () => {
    props.history.push(`/indicators/gateway`)
  }

  useEffect(() => {
    window['clusterlink'] = (cluster) => {
      linkToClusterPage(cluster);
    };
    window['gatewaylink'] = (cluster) => {
      linkToGatewayPage();
    };
  }, []);

  // 加载选中配置
  useEffect(() => {
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
    <div>
      {renderConfig()}
      {renderContent()}
    </div>
  )
})

export default Cluster;