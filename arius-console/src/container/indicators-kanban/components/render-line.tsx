import React, { memo, useEffect, useRef, useState } from "react";
import { Line } from "./line";
import { getOption, metricsType } from "../config";

interface propsType {
  metricsTypes: string[];
  width?: number | string;
  height?: number | string;
  configData?: {
    [key: string]: any;
  };
  isMoreDay?: boolean;
  getAsyncViewData: (metricsType: string[]) => any;
  reload?: boolean;
  endTime?: number;
  startTime?: number;
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
        const metricsList = await getAsyncViewData(metricsTypes);
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
          data[item.type] = getOption(item, configData, isMoreDay)
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
        {
          metricsTypes.map((item, i)=> 
            <Line
              title={configData[item]?.title()}
              index={item}
              key={item + i}
              option={option[item] || {}}
              isLoading={isLoading}
              width={width}
              height={height}
            />
          )
        }
      </>
    );
  }
);
