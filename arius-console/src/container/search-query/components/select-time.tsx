import React, { useEffect, useState } from "react";
import { Radio, DatePicker } from "antd";
import moment from "moment";
import { IPeriod } from "../config";
const { RangePicker } = DatePicker;

interface SelectTimePropsType {
  onTimeStampChange?: (startTime, endTime) => void;
  periodRadio: IPeriod[];
  periodRadioMap: Map<string, IPeriod>;
  reload?: boolean;
  value?: any
}

export const SelectTime: React.FC<SelectTimePropsType> = ({
  onTimeStampChange,
  periodRadio,
  periodRadioMap,
  reload,
  value
}) => {
  const [time, setTime] = useState("oneDay");
  const [dates, setDates] = useState([undefined, undefined]);
  const disabledDate = (current) => {
    if (!dates || dates.length === 0) {
      return false;
    }
    const tooLate = dates[0] && current.diff(dates[0], "days") > 6;
    const tooEarly = dates[1] && dates[1].diff(current, "days") > 6;
    return current > moment().endOf("day");
  };

  const onRadioChange = (e) => {
    const { value } = e.target;
    setTime && setTime(value);
    const dates = periodRadioMap.get(value).dateRange;
    onTimeStampChange &&
      onTimeStampChange(dates[0].valueOf(), dates[1].valueOf());
  };

  const onRangeChange = (dates) => {
    setTime && setTime("");
    onTimeStampChange &&
      onTimeStampChange(dates[0].valueOf(), dates[1].valueOf());
  };

  useEffect(() => {
    if (value) {
      setTime && setTime("");
      setDates([moment(value[0]), moment(value[1])])
    }
  }, [value])

  useEffect(() => {
    if (typeof reload !== "boolean" || !time) {
      return;
    }
    const dates = periodRadioMap.get(time).dateRange;
    onTimeStampChange &&
      onTimeStampChange(dates[0].valueOf(), dates[1].valueOf());
  }, [reload]);

  return (
    <div>
      <Radio.Group
        style={{ margin: "0 20px 10px 0" }}
        value={time}
        onChange={onRadioChange}
      >
        {periodRadio.map((p) => (
          <Radio.Button key={p.key} value={p.key}>
            {p.label}
          </Radio.Button>
        ))}
      </Radio.Group>
      <RangePicker
        // disabledDate={disabledDate}
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
