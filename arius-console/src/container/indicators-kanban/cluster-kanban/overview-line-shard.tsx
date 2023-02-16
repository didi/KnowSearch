import React, { memo, useEffect, useState, useRef } from "react";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import { Spin } from "antd";
import { Line, Shard } from "../components";
import { getOverviewOption } from "./overview-view-config";
import { LINE, SHARD, metricsDataType, indexConfigData } from "./overview-view-config";
import { getOverviewData } from "../../../api/cluster-kanban";
import _, { forEach } from "lodash";
import DragGroup from "../../../d1-packages/drag-group/DragGroup";
import { setIsUpdate, setClusterKanban } from "actions/cluster-kanban";
import InfoTooltip from "component/infoTooltip";

interface propsType {
  metricsTypes: string[];
  sortEnd?: any;
  item?: string;
  dictionary?: any;
}

export const LineShard: React.FC<propsType> = memo(({ metricsTypes, sortEnd, item, dictionary }) => {
  const { clusterName, startTime, endTime, isMoreDay, isUpdate, timeRadioKey } = useSelector(
    (state) => ({
      clusterName: (state as any).clusterKanban.clusterName,
      startTime: (state as any).clusterKanban.startTime,
      endTime: (state as any).clusterKanban.endTime,
      isMoreDay: (state as any).clusterKanban.isMoreDay,
      isUpdate: (state as any).clusterKanban.isUpdate,
      timeRadioKey: (state as any).clusterKanban.timeRadioKey,
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
  const dispatch = useDispatch();
  const getLineOption = (lineConfig, data) => {
    if (!data) {
      return;
    }
    return getOverviewOption(lineConfig.info.title, data, lineConfig.info.data, lineConfig.info.list, lineConfig.unit, isMoreDay);
  };

  const getShardOption = (shardConfig, data) => {
    if (!data) {
      return;
    }
    return data.map(shardConfig.mapFn);
  };

  const getOption = (LineShardData, metricsType) => {
    const metricsConfig = _.cloneDeep(metricsDataType[metricsType]);
    // 针对合并数据做特殊处理
    let data = [];
    if (indexConfigData[metricsType] && indexConfigData[metricsType].types) {
      indexConfigData[metricsType].types.forEach((type) => {
        data.push(...LineShardData[type]);
      });
    } else {
      data = LineShardData[metricsType];
    }
    // 判断是折线图还是表格
    if (metricsConfig.type === LINE) {
      return data && data.length ? getLineOption(metricsConfig, data) : {};
    } else {
      return data && data.length ? getShardOption(metricsConfig, LineShardData[metricsType]) : [];
    }
  };

  const getOverviewLineShardData = async () => {
    if (!clusterName) {
      setIsLoading(false);
      return;
    }
    setIsLoading(true);
    try {
      // 针对合并数据做特殊处理
      let newMetricsTypes = [];
      metricsTypes.forEach((item) => {
        if (indexConfigData[item] && indexConfigData[item].types) {
          newMetricsTypes.push(...indexConfigData[item].types);
        } else {
          newMetricsTypes.push(item);
        }
      });
      const LineShardData = await getOverviewData(newMetricsTypes, clusterName, startTime, endTime);
      const data = {};
      //保存配置化标题的值bigShardThreshold
      dispatch(
        setClusterKanban({
          bigShardThreshold: LineShardData?.bigShardThreshold,
        })
      );
      metricsTypes.forEach((item) => {
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
      timeDiff.current = endTime - startTime;
      isFirst.current = false;
      oldClusterName.current = clusterName;
    }
    getOverviewLineShardData();
  }, [isMoreDay, metricsTypes, clusterName, startTime, endTime, timeRadioKey, getOverviewData]);

  const renderLineShard = (metricsType) => {
    const metricsConfig = metricsDataType[metricsType];
    if (!metricsConfig) {
      return "";
    }
    let dict = dictionary[metricsType];
    let tooltip =
      dict?.currentCalLogic || dict?.price || dict?.threshold ? (
        <InfoTooltip
          className="indicators-info"
          currentCalLogic={dict?.currentCalLogic}
          price={dict?.price}
          threshold={dict?.threshold}
        ></InfoTooltip>
      ) : null;
    if (metricsConfig.type === LINE) {
      return (
        <Line
          title={metricsConfig?.info?.title || ""}
          tooltip={tooltip}
          key={metricsType}
          index={"overview-view-line-ele-id" + metricsType}
          isLoading={isLoading}
          option={data[metricsType]}
          connectGroupName={"cluster-overview"}
        />
      );
    }
    //table
    return (
      <Shard
        currentTime={currentTime}
        title={metricsConfig.title || ""}
        tooltip={tooltip}
        key={metricsType}
        dataSource={data[metricsType]}
        shardColumns={metricsConfig.shardColumn}
        isLoading={isLoading}
      />
    );
  };

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
        {metricsTypes.map((item) => renderLineShard(item))}
      </DragGroup>
    </>
  );
});
