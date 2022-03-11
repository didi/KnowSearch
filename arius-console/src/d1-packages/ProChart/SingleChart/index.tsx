import React, { useRef, useEffect, useState } from 'react';
import _ from 'lodash';
import * as echarts from 'echarts';
import { getMergeOption } from './config';

interface Opts {
  width?: number;
  height?: number;
}

interface Props {
  option: any;
  wrapStyle: React.CSSProperties;
  wrapClassName?: string;
  initOpts?: Opts;
  onResize?: (params: any) => void;
  resizeWait?: number;
}

export const SingleChart = (props: Props) => {
  const { wrapStyle, option, wrapClassName = '', initOpts, onResize, resizeWait = 1000 } = props;
  const chartRef = useRef(null);
  let chartInstance = null;

  const renderChart = () => {
    if (option?.series?.[0]?.data?.length < 1) {
      return;
    }
    const chartType = option?.series?.[0]?.type;
    const chartOptons = getMergeOption(chartType, option);
    const renderedInstance = echarts.getInstanceByDom(chartRef.current);
    if (renderedInstance) {
      chartInstance = renderedInstance;
    } else {
      chartInstance = echarts.init(chartRef.current, null, { width: initOpts?.width || undefined, height: initOpts?.height || undefined });
    }
    chartInstance.setOption(chartOptons);
  };

  const handleResize = _.throttle(() => {
    if (onResize) {
      onResize(chartInstance);
    } else {
      chartInstance &&
        chartInstance.resize({
          width: initOpts?.width || undefined,
          height: initOpts?.height || undefined,
        });
    }
  }, resizeWait);

  useEffect(() => {
    renderChart();
    window.addEventListener('resize', handleResize);
    return () => {
      window.removeEventListener('resize', handleResize);
      chartInstance && chartInstance.dispose();
    };
  }, [option]);

  return <div ref={chartRef} className={wrapClassName} style={wrapStyle}></div>;
};

export default SingleChart;
