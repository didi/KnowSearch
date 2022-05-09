import React, { useState, useEffect, useRef } from 'react';
import DRangeTime from './../../d1-packages/d-range-time';
import { SyncOutlined } from '@ant-design/icons'
import { MetricsConfig } from "./components/metricsConfig";
import { getOption, indexViewMetrics } from "./config";
import { ILineParams, indexLine, indexList, templateLine, setCheckedList, getCheckedList, templatelist } from 'api/dashboard';
import TableCard from './components/table'
import './index.less'
import { cloneDeep } from 'lodash';
import DragGroup from './../../packages/drag-group/DragGroup';
import { arrayMoveImmutable } from 'array-move';
import { Line } from './components/line';
import { withRouter } from 'react-router-dom';
const ONE_HOUR = 1000 * 60 * 60;
const IndexView = withRouter((props: any) => {
  const currentTime = new Date().getTime();
  const [startTime, setStartTime] = useState(currentTime - ONE_HOUR);
  const [endTime, setEndTime] = useState(currentTime);
  const [loading, setLoading] = useState(Object.keys(indexViewMetrics));
  const [metrics, setMetrics] = useState(Object.keys(indexViewMetrics));
  const [data, setData] = useState({})
  const [listData, setListData] = useState({});
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
    getTplListData();
  }

  const setIndexConfigCheckedData = (checkvalue) => {
    setMetrics(checkvalue);
    setCheckedList('index', checkvalue);
  };

  const sortMetrics = () => {
    const left = metrics.map(item => ({ name: indexViewMetrics[item]?.name, value: item }))
    const right = [];
    Object.keys(indexViewMetrics).forEach(item => {
      if (!metrics.includes(item)) {
        right.push({ name: indexViewMetrics[item]?.name, value: item })
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
        <MetricsConfig title="索引" value={metrics} optionList={sortMetrics()} defaultCheckedData={[]} cb={setIndexConfigCheckedData} />
      </div>
    )
  }

  const getLineData = async (topNu?: number, metricsTypes?: string[]) => {
    const lineMetrics = [];
    metrics.forEach(item => {
      if (indexViewMetrics && indexViewMetrics[item].columns) {
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
    if (!Params.metricsTypes.length) {
      return
    }
    try {
      setIsLoading(true);
      setLoading(state => [...state, ...(metricsTypes || lineMetrics)]);
      const metricsList = await indexLine(Params);
      if (
        !metricsList ||
        metricsList.length === 0
      ) {
        setData({});
        return;
      }
      if (metricsTypes) {
        // 如果传入指标就不初始化data直接在这里return
        const cloneData = cloneDeep(data);
        metricsList.forEach(item => {
          cloneData[item.type] = getOption({ metrics: item, configData: indexViewMetrics, isClusterLink: true, clusterPhyName: (item.metricsContents || []).map(v => v.cluster) });
        });
        setIsLoading(false);
        setLoading(state => state.filter(item => !(metricsTypes || lineMetrics).includes(item)));
        setData(cloneData);
        return;
      }
      const datas = {};
      metricsList.forEach(item => {
        datas[item.type] = getOption({ metrics: item, configData: indexViewMetrics, isClusterLink: true, clusterPhyName: (item.metricsContents || []).map(v => v.cluster) });
      });
      setIsLoading(false);
      setLoading(state => state.filter(item => !(metricsTypes || lineMetrics).includes(item)));
      setData(datas);
    } catch (error) {
      setIsLoading(false);
      setLoading([]);
      console.log("render line", error);
    } finally {
      // setIsLoading(false);
    }
  }

  const getTplListData = async () => {
    setIsLoading(true);
    const datas = {};
    const TplListMetrics = [];
    const loadingList = []
    metrics.forEach(item => {
      if (indexViewMetrics && indexViewMetrics[item].columns) {
        if (indexViewMetrics[item].as) {
          TplListMetrics.push(indexViewMetrics[item].as);
          loadingList.push(item)
        }
      }
    })
    if (TplListMetrics && TplListMetrics.length === 0) {
      return;
    }
    setLoading(state => [...state, ...loadingList]);
    const TplParams: any = {
      metricsTypes: TplListMetrics,
      aggType: 'avg',
      orderByDesc: true,
    }
    try {
      const tplMetricsList = await templatelist(TplParams);
      if (
        !tplMetricsList ||
        tplMetricsList.length === 0
      ) {
        // setListData({});
        setLoading(state => state.filter(item => !loadingList.includes(item)));
        return;
      }
      metrics.forEach(item => {
        tplMetricsList.forEach(v => {
          if (indexViewMetrics[item].as === v.type) {
            datas[item] = v
          }
        })
      });
      setListData((state) => ({ ...state, ...datas }));
      setIsLoading(false);
      setLoading(state => state.filter(item => !loadingList.includes(item)));
    } catch (err) {
      console.log(err)
    }
  }

  const getListData = async () => {
    const ListMetrics = [];
    metrics.forEach(item => {
      if (indexViewMetrics && indexViewMetrics[item].columns) {
        if (!indexViewMetrics[item].as) {
          ListMetrics.push(item);
        }
      }
    })
    if (ListMetrics && ListMetrics.length === 0) {
      return;
    }
    const Params: any = {
      metricsTypes: ListMetrics,
      aggType: 'avg',
      orderByDesc: true,
    }

    try {
      setIsLoading(true);
      setLoading((state) => [...state, ...ListMetrics]);
      const datas = {};
      const metricsList = await indexList(Params);
      if (
        !metricsList ||
        metricsList.length === 0
      ) {
        // setListData({});
        setLoading(state => state.filter(item => !ListMetrics.includes(item)));
        return;
      }
      metricsList.forEach(item => {
        datas[item.type] = item
      });
      setListData((state) => ({ ...state, ...datas }));
      setIsLoading(false);
      setLoading(state => state.filter(item => !ListMetrics.includes(item)));
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
    setCheckedList('index', listsNew);
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
            indexViewMetrics[item]?.columns ? <TableCard
              columns={indexViewMetrics[item]?.columns}
              dataSource={listData[item]?.metricListContents}
              title={indexViewMetrics[item]?.name}
              tooltip={indexViewMetrics[item]?.tooltip}
              // unit={item.unit}
              isLoading={loading.includes(item)}
              key={item}
            />
              :
              <Line
                title={data[item]?.title?.text || indexViewMetrics[item]?.title()}
                index={item}
                key={item}
                option={data[item] || {}}
                isLoading={loading.includes(item)}
                cb={getLineData}
                tooltip={indexViewMetrics[item]?.tooltip || ''}
                topN={topN}
              />
          )
        }
      </DragGroup>
    )
  }

  const getAsyncCheckedList = async () => {
    try {
      const checkedList = await getCheckedList('index');
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
    window['clusterlink'] = (index: string, clusterName: string) => {
      props.history.push(`/indicators/cluster?cluster=${clusterName}&index=${index}#index`)
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

export default IndexView;