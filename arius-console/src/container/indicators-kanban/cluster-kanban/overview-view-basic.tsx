import React, { memo, useEffect, useState } from "react";
import { useSelector, shallowEqual } from "react-redux";
import { Skeleton } from "antd";
import { StateConfig, ContrastFigure } from "../components";

import { getContrastChartProps } from "./config";
import { getOverviewData } from "../../../api/cluster-kanban";
import { toFixedNum } from "../../../lib/utils";
interface propsType {
  reload?: boolean;
}
interface basicPropsType {
  // memFreePercent?: number; // 空闲内存百分比
  heapFreeUsage?: number; // 堆空闲内存百分比
  // memUsedPercent?: number; // 已用内存百分比
  heapUsage?: number; // 堆已用内存百分比
  storeUsage?: number; // 已用磁盘百分比
  storeFreeUsage?: number; // 空闲磁盘百分比
  activeNodeNu?: number; // 活跃节点数
  clusterName?: string; // 物理集群名称
  freeStoreSize?: number; // 空闲磁盘
  invalidNodeNu?: number; // 死亡节点
  heapMemFree?: number; // 堆空闲内存
  // memFree?: number; // 空闲内存
  heapMemTotal?: number; // 堆内存总量
  // memTotal?: number; // 内存总量
  heapMemUsed?: number; // 堆已用内存
  // memUsed?: number; // 已用内存
  numberClientNodes?: number; // Clientnode节点数
  numberDataNodes?: number; // Datanode节点数
  numberIngestNode?: number; // IngestNode节点数
  numberMasterNodes?: number; // Masternode节点数
  numberNodes?: number; //节点总数
  physicCluster?: number; //是否是物理集群
  shardNu?: number; // Shard数量
  status?: string; // 集群状态 number gree 1 yellow 2 red
  storeSize?: number; // 磁盘已使用容量
  totalDocNu?: number; //文档总数
  totalIndicesNu?: number; // 索引数目
  totalStoreSize?: number; // 磁盘总容量
  totalTemplateNu?: number; // 索引模版数目
  [key: string]: any;
}

export const overviewClassPrefix = "rf-monitor";

export const OverviewViewBasic: React.FC<propsType> = memo(({ reload }) => {
  const { clusterName, startTime, endTime, isUpdate } = useSelector(
    (state) => ({
      clusterName: (state as any).clusterKanban.clusterName,
      startTime: (state as any).clusterKanban.startTime,
      endTime: (state as any).clusterKanban.endTime,
      isMoreDay: (state as any).clusterKanban.isMoreDay,
      isUpdate: (state as any).clusterKanban.isUpdate,
    }),
    shallowEqual
  );
  const [basic, setBasic] = useState<basicPropsType>({});
  const [isLoading, setIsLoading] = useState(true);

  const getOverviewBasicData = async () => {
    if (!clusterName) {
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    try {
      const { basic } = await getOverviewData(["basic"], clusterName, startTime, endTime);
      setBasic(basic);
    } catch (error) {
      setBasic({});
      console.log(error);
    } finally {
      setIsLoading(false);
    }
  };

  const bytesToGB = (num: number) => {
    if (!num) {
      return 0;
    }
    return Number((num / 1024 / 1024 / 1024).toFixed(2));
  };

  useEffect(() => {
    getOverviewBasicData();
  }, [clusterName, reload, startTime, endTime, isUpdate]);

  // 总揽视图列表
  const stateConfigurationList = [
    {
      id: 1,
      name: "集群状态",
      state: basic.status || "red",
    },
    {
      id: 2,
      name: "Shard总数",
      count: basic.shardNu || 0,
      unit: "个",
    },
    {
      id: 3,
      name: "索引模版数",
      count: basic.totalTemplateNu || 0,
      unit: "个",
    },
    {
      id: 4,
      name: "文档总数",
      count: basic.totalDocNu || 0,
      unit: "个",
    },
    {
      id: 5,
      name: "索引数",
      count: basic.totalIndicesNu || 0,
      unit: "个",
    },
  ];

  const nodeDistributionList = [
    {
      id: 1,
      name: "Masternode",
      count: basic.numberMasterNodes || 0,
      unit: "个",
    },
    {
      id: 2,
      name: "Datatnode",
      count: basic.numberDataNodes || 0,
      unit: "个",
    },
    {
      id: 3,
      name: "Clientnode",
      count: basic.numberClientNodes || 0,
      unit: "个",
    },
  ];

  const decimalToPercent = (val) => {
    if (!val) {
      return 0;
    }
    val = val * 100;
    if (parseInt(val) != val) {
      return toFixedNum(Number(val), 2);
    }
    return val;
  };

  // const memUsed = bytesToGB(basic.memUsed) || 0;
  const heapMemUsed = bytesToGB(basic?.heapMemUsed) || 0;
  // const memFree = bytesToGB(basic.memFree) || 0;
  const heapMemFree = bytesToGB(basic?.heapMemFree) || 0;
  // const memTotal = toFixedNum(memUsed + memFree) || 0;
  const heapMemTotal = toFixedNum(heapMemUsed + heapMemFree) || 0;
  // const memUsedPercent = toFixedNum(basic.memUsedPercent) || 0;
  const heapUsage = toFixedNum(basic?.heapUsage) || 0;
  // decimalToPercent(memUsed / memTotal) || 0;
  // const memFreePercent = toFixedNum(basic.memFreePercent) || 0;
  const heapFreeUsage = toFixedNum(basic?.heapFreeUsage) || 0;
  // toFixedNum(100 - memUsedPercent) || 0;

  const storeSize = bytesToGB(basic.storeSize) || 0;
  const freeStoreSize = bytesToGB(basic.freeStoreSize) || 0;
  const diskTotal = toFixedNum(storeSize + freeStoreSize) || 0;
  const diskStoreUsage = toFixedNum(basic.storeUsage) || 0;
  // decimalToPercent(storeSize / diskTotal) || 0;
  const diskFreeUsage = toFixedNum(basic.storeFreeUsage) || 0;
  // toFixedNum(100 - diskStoreUsage) || 0;

  const activeNodeNu = basic.activeNodeNu || 0;
  const invalidNodeNu = basic.invalidNodeNu || 0;
  const nodeNuTotal = toFixedNum(activeNodeNu + invalidNodeNu) || 0;
  const activeNodeNuPercent = decimalToPercent(activeNodeNu / nodeNuTotal) || 0;
  const invalidNodeNuPercent = invalidNodeNu
    ? toFixedNum(100 - activeNodeNuPercent) || 0
    : 0;

  const memLegendVal = {
    堆已用内存: {
      value: heapMemUsed + "GB",
      percent: heapUsage,
    },
    堆空闲内存: {
      value: heapMemFree + "GB",
      percent: heapFreeUsage,
    },
  };

  const memoryContrastEChartsData = [
    {
      value: heapMemUsed,
      name: "堆已用内存",
    },
    {
      value: heapMemFree,
      name: "堆空闲内存",
    },
  ];

  const memoryContrastSubtext = heapMemTotal;

  const diskLegendVal = {
    已用磁盘: {
      value: storeSize + "GB",
      percent: diskStoreUsage,
    },
    空闲磁盘: {
      value: freeStoreSize + "GB",
      percent: diskFreeUsage,
    },
  };

  const diskContrastEChartsData = [
    {
      value: storeSize,
      name: "已用磁盘",
    },
    {
      value: freeStoreSize,
      name: "空闲磁盘",
    },
  ];
  const diskContrastSubtext = diskTotal;

  const nodeLegendVal = {
    活跃节点数: {
      value: activeNodeNu + "个",
      percent: activeNodeNuPercent,
    },
    死亡节点数: {
      value: invalidNodeNu + "个",
      percent: invalidNodeNuPercent,
    },
  };

  const nodeContrastEChartsData = [
    {
      value: activeNodeNu,
      name: "活跃节点数",
    },
    {
      value: invalidNodeNu,
      name: "死亡节点数",
    },
  ];
  const nodeContrastSubtext = nodeNuTotal || 0;

  return (
    <>
      {isLoading ? (
        <>
          <Skeleton active />
          <Skeleton active />
          <Skeleton active paragraph={{ rows: 2 }} />
        </>
      ) : (
        <>
          <div
            className={`${overviewClassPrefix}-overview-content-config-box-view`}
          >
            <div
              className={`${overviewClassPrefix}-overview-content-config-box-view-state-config`}
            >
              <StateConfig list={stateConfigurationList} title="状态概览" />
            </div>
            <div
              className={`${overviewClassPrefix}-overview-content-config-box-view-memory-map`}
            >
              <div
                className={`${overviewClassPrefix}-overview-content-config-box-view-memory-map-view`}
              >
                <ContrastFigure
                  id="memory-view"
                  legendVal={memLegendVal}
                  unit="GB"
                  {...getContrastChartProps(
                    "堆内存",
                    memoryContrastEChartsData,
                    memoryContrastSubtext
                  )}
                />
              </div>
              <div className="vertical-line"></div>
              <div
                className={`${overviewClassPrefix}-overview-content-config-box-view-memory-map-view`}
              >
                <ContrastFigure
                  id="disk-view"
                  legendVal={diskLegendVal}
                  unit="GB"
                  {...getContrastChartProps(
                    "磁盘",
                    diskContrastEChartsData,
                    diskContrastSubtext
                  )}
                />
              </div>
            </div>
          </div>
          <div
            className={`${overviewClassPrefix}-overview-content-config-box-node-distribution`}
          >
            <StateConfig list={nodeDistributionList} title="节点分配" />
            <div className="cross-line"></div>
            <ContrastFigure
              id="node-view"
              legendVal={nodeLegendVal}
              unit="个"
              tooltipDirection="left"
              {...getContrastChartProps(
                "节点",
                nodeContrastEChartsData,
                nodeContrastSubtext,
                ["#1473FF", "#D3DAE7"]
              )}
            />
          </div>
        </>
      )}
    </>
  );
});
