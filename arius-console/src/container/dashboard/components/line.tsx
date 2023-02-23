import React, { memo, useEffect, useRef, useState } from "react";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as echarts from "echarts/core";
import { Spin, Empty, Tooltip, Modal, Dropdown, Menu } from "antd";
import _, { cloneDeep } from "lodash";
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
import { useMouseoutOutSide } from "../../indicators-kanban//hooks/useMouseoutOutSide";
import "../index";
import { DownOutlined } from "@ant-design/icons";
import InfoTooltip from "component/infoTooltip";

const imgSrc = require("./../../../assets/empty.png");

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

export const overviewClassPrefix = "dashboard";

export interface ILine {
  index: string;
  option: ECOption;
  setModalId?: (modalId: string, params?: any, cb?: Function) => any;
  bigPicture?: boolean;
  width?: number | string;
  height?: number | string;
  isLoading?: boolean;
  title?: string | any;
  tipSync?: boolean;
  cb?: Function;
  tooltip?: string;
  topN?: any;
  dictionary?: any;
}

export const DrawLine = ({ index, option, setModalId, bigPicture, width, height, tipSync, cb, title, topN, dictionary }: ILine) => {
  const chartBox = useRef(null);
  const myChart = useRef(null);
  const flag = useRef(true);
  const [isShowLegend, setIsShowLegend] = useState(false);
  const topNMap = {
    5: "top 5",
    10: "top 10",
    20: "top 20",
    50: "top 50",
  };
  const handleMenuClick = (result) => {
    topN.current[index] = result.key;
    cb(result.key, [index]);
  };
  const getMenu = () => (
    <Menu onClick={handleMenuClick} selectedKeys={[topN.current[index] || "5"]}>
      {Object.keys(topNMap).map((item) => (
        <Menu.Item key={item}>{topNMap[item]}</Menu.Item>
      ))}
    </Menu>
  );

  const renderCheckTop = (topN) => {
    return (
      <div className={`dashboard-overview-content-line-enlarge`}>
        <Dropdown overlay={getMenu()} trigger={["click"]}>
          <div>
            {topNMap[topN.current && topN.current[index]] || topNMap[5]} <DownOutlined style={{ color: "#74788D", marginLeft: 6 }} />
          </div>
        </Dropdown>
      </div>
    );
  };

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
    myChart.current.dispatchAction({
      type: "takeGlobalCursor",
      key: "dataZoomSelect",
      dataZoomSelectActive: true,
    });
  };

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

  useMouseoutOutSide("mousemove", chartBox, hideTip);

  useEffect(() => {
    if (myChart.current) {
      myChart.current?.dispose();
    }

    myChart.current = echarts.init(document.getElementById(index) as HTMLElement);

    // 监听点击事件
    myChart.current.getZr().on("click", function (params) {
      if (flag.current) {
        myChart.current.getZr().off("mousemove");

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
    option = {
      ...option,
      toolbox: {
        feature: {
          dataZoom: {
            show: true,
            iconStyle: {
              opacity: 0,
            },
            yAxisIndex: "none",
          },
        },
      },
    };
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

  return (
    <>
      <div ref={chartBox} style={{ position: "relative" }} className={`${overviewClassPrefix}-overview-content-line-container`}>
        <div
          className={`${overviewClassPrefix}-overview-content-line-item`}
          id={index}
          key={index}
          style={{
            width: width ? width : "none",
            height: height ? height : "none",
          }}
        ></div>
        <div
          className="center-center-empty-box-title"
          style={{
            position: "absolute",
            top: 13,
            left: 16,
            height: 22,
            lineHeight: "22px",
            fontSize: "14px",
            color: "#212529",
            fontFamily: "HelveticaNeue-Medium",
            letterSpacing: "0.5px",
          }}
        >
          {title}
          {(dictionary?.price || dictionary?.currentCalLogic || dictionary?.threshold) && (
            <InfoTooltip
              price={dictionary?.price}
              currentCalLogic={dictionary?.currentCalLogic}
              threshold={dictionary?.threshold}
            ></InfoTooltip>
          )}
        </div>
        {renderCheckTop(topN)}
      </div>
    </>
  );
};

export const Line: React.FC<ILine> = ({
  index,
  option,
  isLoading = false,
  width,
  height,
  title,
  tipSync,
  cb,
  tooltip,
  topN,
  dictionary,
}) => {
  const renderLoading = () => {
    return (
      <div
        className={`common-loading-container indicators-kanban-loading-container ${overviewClassPrefix}-overview-content-line-container`}
        style={{ border: "none" }}
      >
        <div className="center-center-loading">
          <Spin />
        </div>
      </div>
    );
  };
  const renderEmpty = () => {
    const { price, currentCalLogic, threshold } = dictionary || {};
    return (
      <div
        className={`common-loading-container indicators-kanban-loading-container ${overviewClassPrefix}-overview-content-line-container`}
        style={{ border: "none" }}
      >
        {title ? (
          <div
            className="center-center-empty-box-title"
            style={{
              position: "relative",
              top: 13,
              left: 16,
              height: 22,
              lineHeight: "22px",
              fontSize: "14px",
              color: "#212529",
              fontFamily: "HelveticaNeue-Medium",
              letterSpacing: "0.5px",
              marginBottom: 100,
            }}
          >
            {title}
            {(price || currentCalLogic || threshold) && (
              <InfoTooltip price={price} currentCalLogic={currentCalLogic} threshold={threshold}></InfoTooltip>
            )}
          </div>
        ) : (
          ""
        )}
        <div className={"dashboard-line-container-empty"}>
          <div>
            <img src={imgSrc} />
          </div>
          <div>
            <span>数据为空</span>
          </div>
        </div>
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
        tipSync={tipSync}
        cb={cb}
        title={title}
        tooltip={tooltip}
        topN={topN}
        dictionary={dictionary}
      />
    );
  };
  return <>{isLoading ? renderLoading() : !option || Object.keys(option).length == 0 ? renderEmpty() : renderLine()}</>;
};
