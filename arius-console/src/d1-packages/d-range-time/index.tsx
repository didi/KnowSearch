import React, { useState, useEffect } from "react";
import { Radio, DatePicker, Input, Popover, Space } from "antd";
import { IconFont } from "@knowdesign/icons";
const { RangePicker } = DatePicker;
import moment, { Moment } from "moment";
import { getPopupContainer } from "lib/utils";
import "./style/index.less";

type objectItem = {
  value: number;
  label: string;
};
interface propsType extends React.HTMLAttributes<HTMLDivElement> {
  timeChange: Function;
  rangeTimeArr?: number[];
  popoverClassName?: string;
  // resetAllValue?: any;
  customTimeOptions?: objectItem[];
  value?: any;
  onChange?: any;
  customDisabledDate?: any;
  defaultRangeKey?: number; // 默认选中第几个时间项
}

const TimeOptionsDefault = [
  {
    label: "最近 15 分钟",
    value: 15 * 60 * 1000,
  },
  {
    label: "最近 1 小时",
    value: 60 * 60 * 1000,
  },
  {
    label: "最近 6 小时",
    value: 6 * 60 * 60 * 1000,
  },
  {
    label: "最近 12 小时",
    value: 12 * 60 * 60 * 1000,
  },
  {
    label: "最近 1 天",
    value: 24 * 60 * 60 * 1000,
  },
];

//const TimeModule: React.FC<propsType> = ({ timeChange, rangeTimeArr, popoverClassName, resetAllValue, customTimeOptions, value = {}, onChange }) => {
const TimeModule: React.FC<propsType> = ({
  timeChange,
  rangeTimeArr,
  popoverClassName,
  customTimeOptions,
  value = {},
  onChange,
  defaultRangeKey,
  customDisabledDate,
}) => {
  const [time, setTime] = useState<number>(60 * 60 * 1000);
  const [rangeTime, setrangeTime] = useState<[Moment, Moment]>([moment(new Date().getTime() - time), moment(new Date().getTime())]);
  const [isRelative, setIsRelative] = useState(true);
  const [visible, setVisible] = useState(false);
  const [dates, setDates] = useState([]);
  const [TimeOptions, setfirst] = useState(customTimeOptions || TimeOptionsDefault);
  const [inputValue, setInputValue] = useState<string>(null);
  const [hackValue, setHackValue] = useState<any>(null); //RangePicker打开面板后值设置为空

  useEffect(() => {
    if (rangeTimeArr?.length > 0) {
      setrangeTime([moment(rangeTimeArr[0]), moment(rangeTimeArr[1])]);
      const rangeTimeLen = rangeTimeArr[1] - rangeTimeArr[0];
      setTime(Math.floor(rangeTimeLen / 1000) * 1000);
    }
  }, [rangeTimeArr]);

  useEffect(() => {
    if (!!time) {
      const timeOption = TimeOptions.find((item) => item.value === time);
      timeOption ? setIsRelative(true) : setIsRelative(false);
      timeOption && setInputValue(timeOption?.label);
    }
  }, [time]);
  // const resetFun = () => {
  //   resetAllValue && resetAllValue({ setInputValue, setrangeTime, setTime })
  // }
  // useEffect(() => {
  //   resetFun()
  // }, []);
  useEffect(() => {
    if (defaultRangeKey !== undefined) {
      // 赋值初始默认起始时间
      periodtimeChange({ target: { value: TimeOptions[defaultRangeKey]?.value } });
    } else {
      onChange && triggerChange({ time: null, rangeTime: null, inputValue: null }); //初始化清空所有数据
    }
  }, []);

  const triggerChange = (changedValue) => {
    onChange?.({ time, rangeTime, inputValue, ...value, ...changedValue });
  };

  const periodtimeChange = (e) => {
    const periodtime = e.target.value;
    const timeOption = TimeOptions.find((item) => item.value === periodtime);
    onChange &&
      triggerChange({
        time: periodtime,
        rangeTime: [moment(new Date().getTime() - periodtime), moment(new Date().getTime())],
        inputValue: timeOption?.label,
      });
    setTime(periodtime);
    setrangeTime([moment(new Date().getTime() - periodtime), moment(new Date().getTime())]);
    timeChange([new Date().getTime() - periodtime, new Date().getTime()], true);
    setIsRelative(true);
    setVisible(false);
  };

  const rangeTimeChange = (dates, dateStrings) => {
    setrangeTime(dates);
    setTime(null);
    dateStrings[0] && setInputValue(`${dateStrings[0]} ~ ${dateStrings[1]}`);
    onChange && triggerChange({ time: null, rangeTime: dates, inputValue: dates ? `${dateStrings[0]} ~ ${dateStrings[1]}` : undefined });
    timeChange([moment(dateStrings[0]).valueOf(), moment(dateStrings[1]).valueOf()], false); // 毫秒数
    setIsRelative(false);
    setVisible(false);
  };
  const onOpenChange = (open) => {
    if (open) {
      setHackValue([]);
      setDates([]);
    } else {
      setHackValue(undefined);
    }
  };
  const disabledDate = (current) => {
    if (customDisabledDate) {
      return customDisabledDate(current, dates);
    }
    if (current && current > moment().endOf("day")) {
      return true;
    }
    if (!dates || dates.length === 0) {
      return false;
    }
    return false;
  };

  const handleVisibleChange = (visible) => {
    setVisible(visible);
  };

  const clickContent = (
    <div className="dd-time-range-module">
      {/* <span>时间：</span> */}
      <div className="flx_con">
        <div className="flx_l">
          <h6 className="time_title">选择时间范围</h6>
          <Radio.Group
            // optionType="button"
            // buttonStyle="solid"
            // options={TimeOptions}
            className="time-radio-group"
            onChange={periodtimeChange}
            value={onChange ? value?.time : time}
            //value={time}
          >
            <Space direction="vertical" size={16}>
              {TimeOptions.map((item, index) => (
                <Radio.Button value={item.value} key={index}>
                  {item.label}
                </Radio.Button>
              ))}
            </Space>
          </Radio.Group>
        </div>
        <div className="flx_r">
          <h6 className="time_title">自定义时间范围</h6>
          <RangePicker
            showTime={{
              format: "HH:mm",
            }}
            format="YYYY-MM-DD HH:mm"
            separator="~"
            onCalendarChange={(val) => setDates(val)}
            disabledDate={disabledDate}
            suffixIcon={<IconFont type="icon-riqi" style={{ color: "#74788D" }}></IconFont>}
            //value={hackValue || rangeTime}
            value={hackValue || (onChange ? value?.rangeTime : rangeTime)}
            onChange={rangeTimeChange}
            onOpenChange={onOpenChange}
          />
        </div>
      </div>
    </div>
  );
  return (
    <>
      <div id="d-range-time">
        <Popover
          trigger={["click"]}
          visible={visible}
          onVisibleChange={handleVisibleChange}
          content={clickContent}
          placement="bottomRight"
          overlayClassName={`d-range-time-popover ${popoverClassName ?? ""}`}
          getPopupContainer={getPopupContainer}
        >
          <span className="input-span">
            <Input
              className={isRelative ? "relativeTime d-range-time-input" : "absoluteTime d-range-time-input"}
              value={onChange ? value?.inputValue : inputValue}
              //value={inputValue}
              readOnly={true}
              bordered={false}
              placeholder="请选择时间范围"
              suffix={<IconFont type="icon-jiantou1" rotate={90} style={{ color: "#74788D" }}></IconFont>}
            />
          </span>
        </Popover>
      </div>
    </>
  );
};

export default TimeModule;
