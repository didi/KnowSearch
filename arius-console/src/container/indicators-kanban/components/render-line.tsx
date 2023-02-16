import React, { memo, useEffect, useRef, useState } from "react";
import { Line } from "./line";
import { getOption, metricsType } from "../config";
import { indexConfigData } from "container/indicators-kanban/cluster-kanban/node-view-config";
import DragGroup from "../../../d1-packages/drag-group/DragGroup";
import { copyString } from "lib/utils";
import InfoTooltip from "component/infoTooltip";
import { unitMap } from "../config";
import "./style.less";

interface propsType {
  metricsTypes: string[];
  width?: number | string;
  height?: number | string;
  configData?: {
    [key: string]: any;
  };
  isMoreDay?: boolean;
  getAsyncViewData: (metricsType: string[], aggType?: string) => any;
  reload?: boolean;
  endTime?: number;
  startTime?: number;
  clusterPhyName?: string;
  sortEnd?: any;
  item?: string;
  aggType?: string;
  dictionary?: any;
  content?: string | string[];
}

export const RenderLine: React.FC<propsType> = memo(
  ({
    metricsTypes,
    width,
    height,
    configData,
    isMoreDay,
    getAsyncViewData,
    reload,
    startTime,
    endTime,
    clusterPhyName,
    sortEnd,
    item,
    aggType,
    dictionary,
    content,
  }) => {
    const [option, setOption] = useState({});
    const [isLoading, setIsLoading] = useState(true);
    const isFirst = useRef(true);
    const timeDiff = useRef(0);

    const getData = async () => {
      if (!getAsyncViewData) {
        setIsLoading(false);
        return;
      }
      setIsLoading(true);
      try {
        const metricsList = await getAsyncViewData(metricsTypes, aggType);
        if (!metricsList || metricsList.length === 0 || metricsList[0]?.metricsContents?.length === 0) {
          setOption({});
          return;
        }
        const data = {};
        metricsList.forEach((item) => {
          if (item?.type === "indices-store-size_in_bytes") {
            let series = item?.metricsContents;
            let unit = "MB";
            (series || []).forEach((ele) => {
              let number = ele?.metricsContentCells?.[0]?.value;
              if (number > 1024 * 1024 * 1024 * 1024) {
                unit = "TB";
              } else if (number > 1024 * 1024 * 1024) {
                unit = "GB";
              }
            });
            if (configData[item?.type]) {
              configData[item?.type].unit = unitMap[unit];
            }
          }
          let breakLimit = [
            "breakers-fielddata-limit_size_in_bytes",
            "breakers-accounting-limit_size_in_bytes",
            "breakers-parent-limit_size_in_bytes",
            "breakers-in_flight_requests-limit_size_in_bytes",
            "breakers-request-limit_size_in_bytes",
            "breakers-in_flight_http_requests-limit_size_in_bytes",
          ];
          // breaker指标需增加阈值线展示
          if (breakLimit.includes(item?.type)) {
            addLimit(item, metricsList);
          }
          data[item.type] = getOption({
            metrics: item,
            configData,
            isMoreDay,
            isShowTooltipModal: true,
            isShowTaskTooltipModal: !!indexConfigData[item.type]?.newquota,
            clusterPhyName,
            limit: item?.limit,
          });
        });
        setOption(data);
      } catch (error) {
        console.log("render line", error);
      } finally {
        setIsLoading(false);
      }
    };

    const addLimit = (metrics, metricsList) => {
      let length = metrics?.metricsContents?.[0]?.metricsContentCells?.length;
      let limit = metrics?.metricsContents?.[0]?.metricsContentCells[length - 1].value;
      // breaker指标返回的数据为阈值，需替换为真实数据
      switch (metrics?.type) {
        case "breakers-fielddata-limit_size_in_bytes":
          let fielddataEstimated = (metricsList || []).filter((item) => item?.type === "breakers-fielddata-estimated_size_in_bytes")?.[0];
          metrics.limit = limit;
          metrics.metricsContents = [...fielddataEstimated?.metricsContents];
          break;
        case "breakers-accounting-limit_size_in_bytes":
          let accountingEstimated = (metricsList || []).filter((item) => item?.type === "breakers-accounting-estimated_size_in_bytes")?.[0];
          metrics.limit = limit;
          metrics.metricsContents = [...accountingEstimated?.metricsContents];
          break;
        case "breakers-parent-limit_size_in_bytes":
          let parentEstimated = (metricsList || []).filter((item) => item?.type === "breakers-parent-estimated_size_in_bytes")?.[0];
          metrics.limit = limit;
          metrics.metricsContents = [...parentEstimated?.metricsContents];
          break;
        case "breakers-in_flight_requests-limit_size_in_bytes":
          let flightEstimated = (metricsList || []).filter(
            (item) => item?.type === "breakers-in_flight_requests-estimated_size_in_bytes"
          )?.[0];
          metrics.limit = limit;
          metrics.metricsContents = [...flightEstimated?.metricsContents];
          break;
        case "breakers-request-limit_size_in_bytes":
          let requestEstimated = (metricsList || []).filter((item) => item?.type === "breakers-request-estimated_size_in_bytes")?.[0];
          metrics.limit = limit;
          metrics.metricsContents = [...requestEstimated?.metricsContents];
          break;
        case "breakers-in_flight_http_requests-limit_size_in_bytes":
          let flightHttpEstimated = (metricsList || []).filter(
            (item) => item?.type === "breakers-in_flight_http_requests-estimated_size_in_bytes"
          )?.[0];
          metrics.limit = limit;
          metrics.metricsContents = [...flightHttpEstimated?.metricsContents];
          break;
        default:
          break;
      }
    };

    useEffect(() => {
      if (endTime && startTime) {
        if (isFirst.current || timeDiff.current !== endTime - startTime) {
          setIsLoading(true);
          timeDiff.current = endTime - startTime;
          isFirst.current = false;
        }
      } else {
        setIsLoading(true);
      }
      getData();
    }, [metricsTypes, configData, isMoreDay, getAsyncViewData, reload, content]);

    const showTooltipModal = (md5, metricsType) => {
      copyString(md5);
    };

    useEffect(() => {
      window["showTooltipModal"] = (md5, metricsType) => {
        showTooltipModal(md5, metricsType);
      };
    }, []);

    return (
      <>
        <DragGroup
          dragContainerProps={{
            onSortEnd: (args) => sortEnd(item, { ...args }),
            axis: "xy",
            distance: 100,
          }}
          containerProps={{
            grid: 12,
            gutter: [10, 10],
          }}
        >
          {metricsTypes.map((item, i) => {
            let dict = dictionary?.[item] || {};
            let title = configData[item]?.title() || "";
            let tooltip =
              dict?.currentCalLogic || dict?.price || dict?.threshold ? (
                <InfoTooltip
                  className="indicators-info"
                  currentCalLogic={dict?.currentCalLogic}
                  price={dict?.price}
                  threshold={dict?.threshold}
                ></InfoTooltip>
              ) : null;
            return (
              <Line
                title={title}
                index={item}
                key={item}
                option={option[item] || {}}
                isLoading={isLoading}
                width={width}
                height={height}
                tooltip={tooltip}
              />
            );
          })}
        </DragGroup>
      </>
    );
  }
);
