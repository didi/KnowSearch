import React, { memo, useEffect, useRef, useState } from "react";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as echarts from "echarts/core";
import { Spin, Empty, Tooltip, Modal } from "antd";
import { throttle, cloneDeep } from "lodash";
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
import { useMouseInSide } from "../hooks/useMouseInSide";
import "../style/index";
import { MenuUnfoldOutlined, MenuFoldOutlined } from "@ant-design/icons";
import "./style.less";

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
echarts.use([TitleComponent, LegendComponent, TooltipComponent, GridComponent, BarChart, LineChart, CanvasRenderer, MarkLineComponent]);

/*
 *@ 教程 https://echarts.apache.org/zh/index.html
 *@ 配置按需引入
 */

export const overviewClassPrefix = "monitor";

export interface ILine {
  index: string;
  option: ECOption;
  setModalId?: (modalId: string, params?: any, cb?: Function) => any;
  bigPicture?: boolean;
  width?: number | string;
  height?: number | string;
  isLoading?: boolean;
  title?: string | React.ReactNode;
  tipSync?: boolean;
  cb?: Function;
  connectGroupName?: string;
  tooltip?: React.ReactNode;
}

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});
export const DrawLine = connect(
  null,
  mapDispatchToProps
)(
  memo(({ index, option, setModalId, bigPicture, width, height, tipSync, cb, connectGroupName, title, tooltip }: ILine) => {
    const chartBox = useRef(null);
    const myChart = useRef(null);
    const flag = useRef(true);
    const [isShowLegend, setIsShowLegend] = useState(false);

    const onRegisterConnect = ({ chartInstance }) => {
      // 关联图表
      chartInstance.group = connectGroupName;
      echarts.connect(connectGroupName);
    };

    useEffect(() => {
      if (myChart.current) {
        myChart.current?.dispose();
      }

      myChart.current = echarts.init(document.getElementById(index) as HTMLElement);
      connectGroupName &&
        onRegisterConnect({
          chartInstance: myChart.current,
        });

      // 监听点击事件
      myChart.current.getZr().on("click", function (params) {
        if (flag.current) {
          myChart.current.getZr().off("mousemove");
          flag.current = false;
        } else {
          hideTip();
        }
      });

      const resize = throttle(() => {
        const el: HTMLElement = document.getElementById(index);
        // 表示该dom未进入可视区
        if (!el?.getBoundingClientRect().width) {
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
      if (isShowLegend) {
        const copyOption = cloneDeep(option);
        (copyOption.legend as any) = null;
        (copyOption.grid as any).right = "20";
        copyOption && myChart.current?.setOption(copyOption, true);
      } else {
        option && myChart.current?.setOption(option, true);
      }
    }, [option, isShowLegend]);

    const showTip = (params) => {
      const { offsetX: x, offsetY: y } = params;
      if (tipSync) {
        if (x !== -999 || y !== -999) {
          (window as any).lineX = x;
          (window as any).lineY = y;
          (window as any).lineTag = index;
        } else if ((window as any).lineTag === index) {
          (window as any).lineX = x;
          (window as any).lineY = y;
        }
        myChart.current.dispatchAction({
          type: "showTip",
          x: x === -999 ? (window as any).lineX : x,
          y: y === -999 ? (window as any).lineY : y,
        });
      } else {
        myChart.current.dispatchAction({
          type: "showTip",
          x: x,
          y: y,
        });
      }
    };

    const chartFirstMousemove = () => {
      myChart.current.dispatchAction({
        type: "takeGlobalCursor",
        key: "dataZoomSelect",
        dataZoomSelectActive: true,
      });
    };

    useMouseInSide("mousemove", chartBox, chartFirstMousemove);

    const hideTip = () => {
      // 设置无效的 x, y 隐藏点击显示的 toolTip 和 线
      showTip({ offsetX: -999, offsetY: -999 });

      // 重新监听鼠标移动显示 toolTip
      chartMousemove();

      flag.current = true;
    };

    const chartMousemove = () => {
      myChart.current.getZr().on("mousemove", showTip);
    };

    const getBigPictureOption = (option: ECOption): ECOption => {
      option = _.cloneDeep(option);
      (option.title as any).show = false;
      (option.title as any).top = "0";
      (option.legend as any).right = "2%";
      (option.legend as any).itemWidth = 20;
      ((option.legend as any).textStyle = {
        width: 120,
        overflow: "truncate",
        ellipsis: "...",
      }),
        ((option.grid as any).right = "20%");
      (option.grid as any).top = "5";
      return option;
    };

    return (
      <>
        <div
          ref={chartBox}
          style={{ position: "relative" }}
          className={bigPicture ? "" : `${overviewClassPrefix}-overview-content-line-container`}
        >
          <div
            className={
              bigPicture ? `${overviewClassPrefix}-overview-content-line-bigPicture` : `${overviewClassPrefix}-overview-content-line-item`
            }
            id={index}
            key={index}
            style={{
              width: width ? width : "none",
              height: height ? height : "none",
            }}
          ></div>
          {cb ? (
            cb()
          ) : (
            <>
              <div className="line-title">
                {title}
                {tooltip}
              </div>
              <div
                style={bigPicture ? { display: "none" } : null}
                className={`${overviewClassPrefix}-overview-content-line-legend`}
                onClick={() => setIsShowLegend(!isShowLegend)}
              >
                <Tooltip title={isShowLegend ? "展开legend" : "收起legend"}>
                  {isShowLegend ? <MenuFoldOutlined /> : <MenuUnfoldOutlined />}
                </Tooltip>
              </div>
              <Tooltip title="查看大图">
                <div
                  style={bigPicture ? { display: "none" } : null}
                  onClick={(e) => {
                    setModalId("bigPicture", { ...getBigPictureOption(option), titleText: title });
                  }}
                  className={`${overviewClassPrefix}-overview-content-line-enlarge`}
                >
                  <img src={iconSrc} alt="全屏" />
                </div>
              </Tooltip>
            </>
          )}
        </div>
      </>
    );
  })
);

export const Line: React.FC<ILine> = ({ index, option, isLoading = false, width, height, title, tooltip, connectGroupName, cb }) => {
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
        <div className="line-title">
          {title}
          {tooltip}
        </div>
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} className="center-center-empty-box-content" />
      </div>
    );
  };
  const renderLine = () => {
    return (
      <DrawLine
        width={width}
        height={height}
        option={option}
        index={index}
        connectGroupName={connectGroupName}
        cb={cb}
        title={title}
        tooltip={tooltip}
      />
    );
  };
  return <>{isLoading ? renderLoading() : !option || Object.keys(option).length == 0 ? renderEmpty() : renderLine()}</>;
};
