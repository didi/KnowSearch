import React, { memo, useEffect, useRef, useState } from "react";
import { Line } from "./line";
import { getOption, metricsType } from "../config";
import { indexConfigData } from 'container/indicators-kanban/cluster-kanban/node-view-config';
import DragGroup from './../../../packages/drag-group/DragGroup';
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
    aggType
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
      try {
        const metricsList = await getAsyncViewData(metricsTypes, aggType);
        if (
          !metricsList ||
          metricsList.length === 0 ||
          metricsList[0]?.metricsContents?.length === 0
        ) {
          setOption({});
          return;
        }
        const data = {};
        metricsList.forEach(item => {
          data[item.type] = getOption(item, configData, isMoreDay, false, false, true, !!indexConfigData[item.type]?.newquota ,clusterPhyName)
        });
        setOption(data);
      } catch (error) {
        console.log("render line", error);
      } finally {
        setIsLoading(false);
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
    }, [metricsTypes, configData, isMoreDay, getAsyncViewData, reload]);

    return (
      <>
        <DragGroup
          dragContainerProps={{
            onSortEnd: (args) => sortEnd(item, { ...args }),
            axis: "xy",
            distance: 100
          }}
          containerProps={{
            grid: 12,
            gutter: [10, 10],
          }}
        >
          {
            metricsTypes.map((item, i)=> 
              <Line
                title={configData[item]?.title()}
                index={item}
                key={item}
                option={option[item] || {}}
                isLoading={isLoading}
                width={width}
                height={height}
              />
            )
          }
        </DragGroup>
      </>
    );
  }
);
