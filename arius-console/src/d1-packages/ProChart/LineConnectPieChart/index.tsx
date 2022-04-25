import React, { useRef, useEffect, useState } from 'react';
import _ from 'lodash';
import * as echarts from 'echarts';
import './style.less';
import { getLineOption, getPieOption } from './config';

interface Opts {
  width?: number;
  height?: number;
}

interface Props {
  wrapClassName?: string;
  wrapStyle?: React.CSSProperties;
  lineClassName?: string;
  lineStyle?: React.CSSProperties;
  pieClassName?: string;
  pieStyle?: React.CSSProperties;
  lineOption: echarts.EChartsOption;
  pieOption: echarts.EChartsOption;
  lineInitOpts?: Opts;
  pieInitOpts?: Opts;
  chartData: any;
  resizeWait?: number;
  onResize?: (params: any) => void;
  onUpdateAxisPointer?: (params: any) => void;
}

export const LineConnectPieChart: React.FC<Props> = ({
  wrapClassName = '',
  wrapStyle,
  lineClassName = '',
  lineStyle,
  pieClassName = '',
  pieStyle,
  lineOption,
  pieOption,
  lineInitOpts,
  pieInitOpts,
  chartData,
  resizeWait = 200,
  onResize,
  onUpdateAxisPointer,
}) => {
  const lineChartRef = useRef(null);
  let lineChartInstance = null;

  const pieChartRef = useRef(null);
  let pieChartInstance = null;

  const renderLineChart = () => {
    if (!chartData) {
      return;
    }
    const renderedInstance = echarts.getInstanceByDom(lineChartRef.current);
    if (renderedInstance) {
      lineChartInstance = renderedInstance;
    } else {
      lineChartInstance = echarts.init(lineChartRef.current, null, {
        width: lineInitOpts?.width || undefined,
        height: lineInitOpts?.height || undefined,
      });
    }

    lineChartInstance.setOption(getLineOption(lineOption));
    lineChartInstance.on('updateAxisPointer', function (event: any) {
      if (onUpdateAxisPointer) {
        onUpdateAxisPointer({
          lineChartInstance,
          pieChartInstance,
        });
      } else {
        const lineData = lineOption.series[0].data;
        const pieData = lineData[event.dataIndex]?.list;
        if (pieData) {
          pieChartInstance.setOption({
            series: [
              {
                data: pieData,
              },
            ],
          });
        }
      }
    });
  };

  const renderPieChart = () => {
    if (!chartData) {
      return;
    }
    const renderedInstance = echarts.getInstanceByDom(pieChartRef.current);
    if (renderedInstance) {
      pieChartInstance = renderedInstance;
    } else {
      pieChartInstance = echarts.init(pieChartRef.current, null, {
        width: pieInitOpts?.width || undefined,
        height: pieInitOpts?.height || undefined,
      });
    }
    pieChartInstance.setOption(getPieOption(pieOption));
  };

  useEffect(() => {
    renderLineChart();
    renderPieChart();

    const resize = _.throttle(() => {
      if (onResize) {
        onResize({ lineChartInstance, pieChartInstance });
      } else {
        setTimeout(() => {
          lineChartInstance.resize({ width: lineInitOpts?.width || undefined, height: lineInitOpts?.height || undefined });
          pieChartInstance.resize({
            width: pieInitOpts?.width || undefined,
            height: pieInitOpts?.height || undefined,
          });
        }, 0);
      }
    }, resizeWait);

    window.addEventListener('resize', resize);
    return () => {
      window.removeEventListener('resize', resize);
      lineChartInstance && lineChartInstance.dispose();
      pieChartInstance && pieChartInstance.dispose();
    };
  }, [chartData]);

  return (
    <div className={`line-connect-pie-chart-box ${wrapClassName}`} style={wrapStyle}>
      <div ref={lineChartRef} className={`line-chart-box ${lineClassName}`} style={lineStyle}></div>
      <div ref={pieChartRef} className={`pie-chart-box ${pieClassName}`} style={pieStyle}></div>
    </div>
  );
};

export default LineConnectPieChart;
