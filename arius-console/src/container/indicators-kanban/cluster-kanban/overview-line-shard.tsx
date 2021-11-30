import React, { memo, useEffect, useState, useRef } from "react";
import { useSelector, shallowEqual } from "react-redux";
import { Spin } from "antd";
import { Line, Shard } from "../components";
import { getOverviewOption } from "./overview-view-config";
import { LINE, SHARD, metricsDataType } from "./overview-view-config";
import { getOverviewData } from "../../../api/cluster-kanban";
import _ from "lodash";

interface propsType {
  metricsTypes: string[];
}

export const LineShard: React.FC<propsType> = memo(
  ({ metricsTypes }) => {
    const { clusterName, startTime, endTime, isMoreDay } = useSelector(
      (state) => ({
        clusterName: (state as any).clusterKanban.clusterName,
        startTime: (state as any).clusterKanban.startTime,
        endTime: (state as any).clusterKanban.endTime,
        isMoreDay: (state as any).clusterKanban.isMoreDay,
      }),
      shallowEqual
    );

    const [data, setData] = useState({});
    // const metricsTypes = [metricsType, "searchLatency"];
    const [currentTime, setCurrentTime] = useState("");
    const [isLoading, setIsLoading] = useState(true);
    const isFirst = useRef(true);
    const timeDiff = useRef(0);
    const oldClusterName = useRef(clusterName);

    const getLineOption = (lineConfig, data) => {
      if (!data) {
        return;
      }
      return getOverviewOption(
        lineConfig.info.title,
        data,
        lineConfig.info.data,
        lineConfig.info.list,
        lineConfig.unit,
        isMoreDay
      );
    };

    const getShardOption = (shardConfig, data) => {
      if (!data) {
        return;
      }
      return data.map(shardConfig.mapFn);
    };

    const getOption = (LineShardData, metricsType) => {
      const metricsConfig = _.cloneDeep(metricsDataType[metricsType]);

      const data = LineShardData[metricsType];
      // 判断是折线图还是表格
      if (metricsConfig.type === LINE) {
        return data && data.length ? getLineOption(metricsConfig, LineShardData[metricsType]) : {};
      } else {
        return data && data.length ? getShardOption(metricsConfig, LineShardData[metricsType]) : [];
      }
    }

    const getOverviewLineShardData = async () => {
      try {
        const LineShardData = await getOverviewData(
          metricsTypes,
          clusterName,
          startTime,
          endTime
        );
        const data = {};
        metricsTypes.forEach(item => {
          data[item] = getOption(LineShardData, item);
        });
        setData(data);
        setCurrentTime(LineShardData["currentTime"] || 0);
      } catch (error) {
        console.log(error);
      } finally {
        setIsLoading(false);
      }
    };

    useEffect(() => {
      if (isFirst.current || timeDiff.current !== endTime - startTime || oldClusterName.current !== clusterName) {
        setIsLoading(true);
        timeDiff.current = endTime - startTime;
        isFirst.current = false;
        oldClusterName.current = clusterName;
      }
      getOverviewLineShardData();
    }, [
      isMoreDay,
      metricsTypes,
      clusterName,
      startTime,
      endTime,
      getOverviewData
    ]);

    const renderLineShard = (metricsType) => {
      const metricsConfig = metricsDataType[metricsType];
      if (!metricsConfig) {
        return "";
      }
      if (metricsConfig.type === LINE) {
        return <Line
          title={metricsConfig.info.title}
          key={metricsType}
          index={"overview-view-line-ele-id" + metricsType}
          isLoading={isLoading}
          option={data[metricsType]}
        />
      }
      return <Shard
        currentTime={currentTime}
        title={metricsConfig.title}
        key={metricsType}
        dataSource={data[metricsType]}
        shardColumns={metricsConfig.shardColumn}
        isLoading={isLoading}
      />
    }

    return (
      <>
        {
          metricsTypes.map(item => renderLineShard(item))
        }
      </>
    );
  }
);
