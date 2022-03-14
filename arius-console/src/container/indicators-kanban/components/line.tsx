import React, { memo, useEffect, useRef } from "react";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as echarts from "echarts/core";
import { Spin, Empty,  Tooltip, Modal } from "antd";
import _ from "lodash";
import {
  BarChart,
  // 系列类型的定义后缀都为 SeriesOption
  BarSeriesOption,
  LineChart,
  LineSeriesOption,
} from "echarts/charts";
import {
  TitleComponent,
  // 组件类型的定义后缀都为 ComponentOption
  TitleComponentOption,
  TooltipComponent,
  TooltipComponentOption,
  GridComponent,
  GridComponentOption,
  LegendComponent,
  LegendComponentOption,
  MarkLineComponent,
  MarkLineComponentOption,
} from "echarts/components";
import { CanvasRenderer } from "echarts/renderers";
import * as actions from "actions";
import { useMouseoutOutSide } from '../hooks/useMouseoutOutSide';
import "../style/index";

const iconSrc = require("../img/full-screen.png");


// 通过 ComposeOption 来组合出一个只有必须组件和图表的 Option 类型
export type ECOption = echarts.ComposeOption<
  | BarSeriesOption
  | LineSeriesOption
  | TitleComponentOption
  | TooltipComponentOption
  | GridComponentOption
  | LegendComponentOption
  | MarkLineComponentOption
>;

// 注册必须的组件
echarts.use([
  TitleComponent,
  LegendComponent,
  TooltipComponent,
  GridComponent,
  BarChart,
  LineChart,
  CanvasRenderer,
  MarkLineComponent,
]);

/*
 *@ 教程 https://echarts.apache.org/zh/index.html
 *@ 配置按需引入
 */

export const overviewClassPrefix = "rf-monitor";

export interface ILine {
  index: string;
  option: ECOption;
  setModalId?: (modalId: string, params?: any, cb?: Function) => any;
  bigPicture?: boolean;
  width?: number | string;
  height?: number | string;
  isLoading?: boolean;
  title?: string;
}

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setModalId(modalId, params, cb)),
});
export const DrawLine = connect(
  null,
  mapDispatchToProps
)(
  memo(({ index, option, setModalId, bigPicture, width, height }: ILine) => {
    const chartBox = useRef(null);
    const myChart = useRef(null);
    const flag = useRef(true);

    const showTip = (params) => {
      const {offsetX: x, offsetY: y} = params;

      myChart.current.dispatchAction({
          type: 'showTip',
          x,
          y,
      });
    }

    const hideTip = () => {
      // 设置无效的 x, y 隐藏点击显示的 toolTip 和 线
      showTip({offsetX: -999, offsetY: -999})
      
      // 重新监听鼠标移动显示 toolTip
      chartMousemove();

      flag.current = true;
    }

    const chartMousemove = () => {
      myChart.current.getZr().on('mousemove', showTip);
    }

    useMouseoutOutSide('mousemove', chartBox, hideTip);

    useEffect(() => {
      if (myChart.current) {
        myChart.current?.dispose();
      }

      myChart.current = echarts.init(
        document.getElementById(index) as HTMLElement
      );

      // 监听点击事件
      myChart.current.getZr().on('click', function(params) {
        if (flag.current) {
          myChart.current.getZr().off('mousemove');

          flag.current = false;
        } else {
          hideTip();
        }
      });

      // 监听鼠标移动显示 toolTip
      chartMousemove();

      const resize = _.throttle(() => {
        const el: HTMLElement = document.getElementById(index);
        // 表示该dom未进入可视区
        if (!el.getBoundingClientRect().width) {
          return;
        }
        myChart.current?.resize();
      }, 300);

      window.addEventListener("resize", resize);

      return () => {
        window.removeEventListener("resize", resize);
      };
    }, []);

    useEffect(() => {
      // 增加true不合并数据
      option && myChart.current?.setOption(option, true);
    }, [option]);

    const getBigPictureOption = (option: ECOption): ECOption => {
      option = _.cloneDeep(option);
      (option.title as any).show = false;
      (option.title as any).top = "0";
      (option.legend as any).right = "2%";
      (option.grid as any).right = "20%";
      (option.grid as any).top = "5";
      return option;
    };

    return (
      <>
        <div
          ref={chartBox}
          style={{ position: "relative" }}
          className={
            bigPicture
              ? ""
              : `${overviewClassPrefix}-overview-content-line-container`
          }
        >
          <div
            className={
              bigPicture
                ? `${overviewClassPrefix}-overview-content-line-bigPicture`
                : `${overviewClassPrefix}-overview-content-line-item`
            }
            id={index}
            key={index}
            style={{
              width: width ? width : "none",
              height: height ? height : "none",
            }}
          ></div>
          <Tooltip title="查看大图">
            <div
              style={bigPicture ? { display: "none" } : null}
              onClick={(e) => {
                hideTip();
                setModalId("bigPicture", getBigPictureOption(option));
              }}
              className={`${overviewClassPrefix}-overview-content-line-enlarge`}
            >
              <img src={iconSrc} alt="全屏" />
            </div>
          </Tooltip>
        </div>
      </>
    );
  })
);

export const Line: React.FC<ILine> = ({
  index,
  option,
  isLoading = false,
  width,
  height,
  title,
}) => {
  const renderLoading = () => {
    return (
      <div
        className={`common-loading-container indicators-kanban-loading-container ${overviewClassPrefix}-overview-content-line-container`}
      >
        <div className="center-center-loading">
          <Spin />
        </div>
      </div>
    );
  };
  const renderEmpty = () => {
    return (
      <div
        className={`common-loading-container indicators-kanban-loading-container ${overviewClassPrefix}-overview-content-line-container`}
      >
        {title ? (
          <h2 className="center-center-empty-box-title">{title}</h2>
        ) : (
          ""
        )}
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          className="center-center-empty-box-content"
        />
      </div>
    );
  };
  const renderLine = () => {
    return (
      <DrawLine width={width} height={height} option={option} index={index} />
    );
  };
  return (
    <>
      {isLoading
        ? renderLoading()
        : !option || Object.keys(option).length == 0
        ? renderEmpty()
        : renderLine()}
    </>
  );
};
