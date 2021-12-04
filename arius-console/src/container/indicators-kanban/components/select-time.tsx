import React, { useEffect, useRef, useState } from "react";
import { useSelector, shallowEqual } from "react-redux";
import { Radio, DatePicker } from "antd";
import moment from "moment";
import { PERIOD_RADIO, PERIOD_RADIO_MAP } from "../config";
import { AlertTwoTone } from "@ant-design/icons";
const { RangePicker } = DatePicker;

interface SelectTimePropsType {
  onTimeStampChange?: (startTime, endTime) => void;
  refreshTime?: number;
}

export const SelectTime: React.FC<SelectTimePropsType> = ({
  onTimeStampChange,
  refreshTime = 0,
}) => {
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
  const [time, setTime] = useState("oneHour");
  const [dates, setDates] = useState([undefined, undefined]);
  const didMount = useRef(false);
  const timer = useRef(null);

  const disabledDate = (current) => {
    if (!dates || dates.length === 0) {
      return false;
    }
    // const tooLate = dates[0] && current.diff(dates[0], "days") > 6;
    // const tooEarly = dates[1] && dates[1].diff(current, "days") > 6;
    return current > moment().endOf("day");
  };

  const updateTimeStamp = (key: string) => {
    const dates = PERIOD_RADIO_MAP.get(key).dateRange;
    setDates(dates);
    onTimeStampChange &&
      onTimeStampChange(dates[0].valueOf(), dates[1].valueOf());
  };

  const onRadioChange = (e) => {
    const { value } = e.target;
    setTime && setTime(value);
    updateTimeStamp(value);
  };

  const onRangeChange = (dates) => {
    setTime && setTime("");
    setDates(dates);
    onTimeStampChange &&
      onTimeStampChange(dates[0].valueOf(), dates[1].valueOf());
  };

  const setTimer = () => {
    if (!refreshTime || typeof refreshTime !== "number" || refreshTime <= 0) {
      return;
    }
    timer.current && clearInterval(timer.current);
    timer.current = setInterval(() => {
      updateTimeStamp(time);
    }, refreshTime);
  };

  useEffect(() => {
    setTimer();
    return () => {
      clearInterval(timer.current);
    };
  }, [refreshTime, time]);

  useEffect(() => {
    if (!didMount.current) {
      didMount.current = true;
      return;
    }
    updateTimeStamp(time);
    setTimer();
  }, [isClusterKanbanUpdate, isGatewayKanbanUpdate]);

  useEffect(() => {
    const dates = PERIOD_RADIO_MAP.get(time).dateRange;
    setDates(dates);
  }, []);

  return (
    <div>
      <Radio.Group
        style={{ margin: "0 15px 10px 0" }}
        value={time}
        onChange={onRadioChange}
      >
        {PERIOD_RADIO.map((p) => (
          <Radio.Button key={p.key} value={p.key}>
            {p.label}
          </Radio.Button>
        ))}
      </Radio.Group>
      <RangePicker
        disabledDate={disabledDate}
        onCalendarChange={(val) => setDates(val)}
        format="YYYY-MM-DD HH:mm"
        allowClear={false}
        value={dates as [moment.Moment, moment.Moment]}
        onChange={onRangeChange}
        showTime
      />
    </div>
  );
};
