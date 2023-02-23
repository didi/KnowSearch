import React, { useEffect, useRef } from "react";
import { useSelector, shallowEqual } from "react-redux";
import { CustomTimeRangePicker } from "component/TimeRangePicker";

interface CustomPickTimeProps {
  onTimeStampChange?: (startTime, endTime, radioCheckedKey?: string) => void;
  refreshTime?: number;
}

export const CustomPickTime: React.FC<CustomPickTimeProps> = ({ onTimeStampChange, refreshTime = 0 }) => {
  const { isClusterKanbanUpdate } = useSelector(
    (state) => ({
      isClusterKanbanUpdate: (state as any).clusterKanban.isUpdate,
    }),
    shallowEqual
  );
  const { isGatewayKanbanUpdate } = useSelector(
    (state) => ({
      isGatewayKanbanUpdate: (state as any).gatewayKanban.isUpdate,
    }),
    shallowEqual
  );
  const timer = useRef(null);
  const rangeTimeRef = useRef(null);
  const currentUpdateKey = useRef("");

  const updateTimeStamp = (dates = null, isCustomTime = false) => {
    const _dates = dates || rangeTimeRef.current.rangeTime;
    if (!_dates) return;
    // 记录时间差用于刷新生效
    if (isCustomTime) {
      currentUpdateKey.current = `${new Date().getTime()}-${_dates[1].valueOf() - _dates[0].valueOf()}`;
    }

    onTimeStampChange && onTimeStampChange(_dates[0].valueOf(), _dates[1].valueOf(), isCustomTime ? currentUpdateKey.current : undefined);
  };

  const setTimer = () => {
    if (!refreshTime || typeof refreshTime !== "number" || refreshTime <= 0) {
      return;
    }
    timer.current && clearInterval(timer.current);
    timer.current = setInterval(() => {
      rangeTimeRef.current.refresh();
    }, refreshTime);
  };

  useEffect(() => {
    setTimer();
    return () => {
      clearInterval(timer.current);
    };
  }, [refreshTime]);

  useEffect(() => {
    rangeTimeRef.current.refresh();
  }, [isClusterKanbanUpdate, isGatewayKanbanUpdate]);

  return (
    <>
      <CustomTimeRangePicker ref={rangeTimeRef} onChange={(dates, isCustomTime) => updateTimeStamp(dates, isCustomTime)} />
    </>
  );
};
