import React, { useEffect, useState, useRef } from 'react';
import ProTable from '../../ProTable';
import * as echarts from 'echarts';
import _ from 'lodash';
import classnames from 'classnames';
import './style.less';

interface Opts {
  width?: number;
  height?: number;
}

interface ITablePieChartProps {
  layout?: 'vertical' | 'horizontal';
  tableProps: any;
  chartProps: any;
  hightlightIndex: number;
  wrapClassName?: string;
  tableClassName?: string;
  chartClassName?: string;
  wrapStyle?: React.CSSProperties;
  tableStyle?: React.CSSProperties;
  chartStyle?: React.CSSProperties;
  updateHighlighItem?: (params?: any) => void;
  onResize?: (params?: any) => void;
  chartData?: any;
  initChartOpts?: Opts;
  resizeWait?: number;
}

const TablePieChart: React.FC<ITablePieChartProps> = ({
  tableProps,
  chartProps: option,
  layout = 'horizontal',
  updateHighlighItem,
  chartData,
  wrapClassName,
  tableClassName,
  chartClassName,
  wrapStyle,
  tableStyle,
  chartStyle,
  initChartOpts,
  onResize,
  hightlightIndex,
  resizeWait = 1000,
}) => {
  const chartRef = useRef(null);
  let chartInstance = null;

  const [dataSource, setDataSource] = useState(tableProps.dataSource);

  const renderChart = () => {
    if (!chartData) {
      return;
    }
    const renderedInstance = echarts.getInstanceByDom(chartRef.current);
    if (renderedInstance) {
      chartInstance = renderedInstance;
      chartInstance.setOption(option);
    } else {
      chartInstance = echarts.init(chartRef.current, null, {
        width: initChartOpts?.width || undefined,
        height: initChartOpts?.height || undefined,
      });
      chartInstance.setOption(option);

      chartInstance.off('click');
      chartInstance.on('click', (e) => {
        chartInstance.dispatchAction({
          type: 'downplay',
          seriesIndex: e.seriesIndex,
        });
        chartInstance.dispatchAction({
          type: 'highlight',
          dataIndex: e.dataIndex,
        });
        updateHighlighItem(e.data);
      });
      setTimeout(() => {
        chartInstance.dispatchAction({
          type: 'highlight',
          dataIndex: hightlightIndex,
        });
      }, 0);
    }
  };

  useEffect(() => {
    renderChart();
    const resize = _.throttle(() => {
      if (onResize) {
        onResize(chartInstance);
      } else {
        chartInstance.resize();
      }
    }, resizeWait);

    window.addEventListener('resize', resize);
    return () => {
      chartInstance && chartInstance.dispose();
      window.removeEventListener('resize', resize);
    };
  }, [chartData]);

  useEffect(() => {
    setDataSource(tableProps.dataSource);
  }, [tableProps.dataSource]);

  return (
    <div
      className={classnames('pie-table-box', {
        verticalLayout: layout === 'vertical',
        wrapClassName,
      })}
      style={wrapStyle}
    >
      {layout === 'vertical' ? (
        <>
          <div className={classnames('pie-chart-box', chartClassName)} ref={chartRef} style={chartStyle}></div>
          <div className={classnames('table-box', tableClassName)} style={tableStyle}>
            <ProTable
              tableProps={{
                ...tableProps,
                dataSource,
              }}
            />
          </div>
        </>
      ) : (
        <>
          <div className={classnames('table-box', tableClassName)} style={tableStyle}>
            <ProTable
              tableProps={{
                ...tableProps,
                dataSource,
              }}
            />
          </div>
          <div className={classnames('pie-chart-box', chartClassName)} ref={chartRef} style={chartStyle}></div>
        </>
      )}
    </div>
  );
};
export default TablePieChart;
