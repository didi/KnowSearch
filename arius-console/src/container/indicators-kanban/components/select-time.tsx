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
  const [hackValue, setHackValue] = useState(null);
  const [dates, setDates] = useState([undefined, undefined]);
  const [calendarDates,setCalendarDates] = useState([])
  const didMount = useRef(false);
  const timer = useRef(null);

  const disabledDate = (current) => {
    if (!calendarDates || calendarDates.length === 0) {
      return current > moment().endOf('day');
    }
    const tooLate = (calendarDates[0] && current.diff(calendarDates[0], "days") > 6) || current > moment().endOf('day');
    const tooEarly = calendarDates[1] && calendarDates[1].diff(current, "days") > 6;
    return tooLate || tooEarly
  };

  const updateTimeStamp = (key: string) => {
    const _dates = PERIOD_RADIO_MAP.get(key)?.dateRange || dates;
    if (!_dates) return;
    setDates(_dates);
    onTimeStampChange &&
      onTimeStampChange(_dates[0].valueOf(), _dates[1].valueOf());
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

  const onOpenChange = open => {
    if (open) {
      setHackValue([]);
      setCalendarDates([]);
    } else {
      setHackValue(undefined);
    }
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
        onCalendarChange={(val) => setCalendarDates(val)}
        format="YYYY-MM-DD HH:mm"
        allowClear={false}
        value={ (hackValue || dates) as [moment.Moment, moment.Moment]}
        onChange={onRangeChange}
        onOpenChange={onOpenChange}
        showTime={{format:'HH:mm',defaultValue:[moment('00:00:00','HH:mm'),moment('23:59:59','HH:mm')]}}
      />
    </div>
  );
};
