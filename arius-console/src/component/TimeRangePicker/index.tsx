import { LeftOutlined, RightOutlined } from "@ant-design/icons";
import { Button, DatePicker, Form, Input, InputNumber, Popover, Row, Select, Tooltip } from "antd";
import { getPopupContainer } from "lib/utils";
import moment, { Moment } from "moment";
import React, { useImperativeHandle, useRef, useState } from "react";
import "./index.less";
const { RangePicker } = DatePicker;
const { Option } = Select;
const FormItem = Form.Item;

const TIME_OPTIONS = [
  {
    label: "最近 15 分钟",
    value: 15 * 60 * 1000,
  },
  {
    label: "最近 30 分钟",
    value: 30 * 60 * 1000,
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

interface IProps {
  value?: [Moment, Moment];
  defaultValue?: [Moment, Moment];
  onChange?: any;
  defaultRange?: number;
}

type RangeValue = [Moment | null, Moment | null] | null;

export const CustomTimeRangePicker = React.forwardRef((props: IProps, ref) => {
  const { onChange, value, defaultValue, defaultRange } = props;
  const defaultRangeTime = defaultValue || [moment(new Date().getTime() - (defaultRange ?? 60 * 60 * 1000)), moment(new Date().getTime())];
  const [rangeTime, setRangeTime] = useState(value || defaultRangeTime);
  const [hackValue, setHackValue] = useState<any>(null); //RangePicker打开面板后值设置为空
  const [dates, setDates] = useState<RangeValue>(null);
  const noNeedChange = useRef(null);
  const [form] = Form.useForm();
  const [iconDisabled, setIconDisabled] = useState(true);
  const [commonTime, setCommonTime] = useState(defaultRange || 60 * 60 * 1000);
  const [isLastTime, setIsLastTime] = useState(false);
  const [open, setOpen] = useState(false);

  const hide = () => {
    setOpen(false);
  };

  const handleOpenChange = (newOpen: boolean) => {
    setOpen(newOpen);
  };

  const onRangeTimeChange = (dates, dateStrings) => {
    if (noNeedChange.current) {
      return;
    }
    setCommonTime(null);
    setIsLastTime(false);
    if (dates[1].valueOf() < new Date().getTime()) {
      setIconDisabled(false);
    }
    changeRangeTime(dates, true);
  };

  useImperativeHandle(ref, () => ({
    rangeTime,
    refresh,
  }));

  const onOpenChange = (open) => {
    if (open) {
      setDates([null, null]);
    } else {
      setHackValue(null);
    }
  };

  const refresh = () => {
    if (commonTime) {
      changeRangeTime([moment(new Date().getTime() - commonTime), moment(new Date().getTime()-120000)], false);
    } else if (isLastTime) {
      const values = form.getFieldsValue();
      const { unit, input, type } = values;
      const rangeValue = unit === "hour" ? input * 60 * 60 * 1000 : unit === "min" ? input * 60 * 1000 : input * 24 * 60 * 60 * 1000;
      changeRangeTime([moment(new Date().getTime() - rangeValue), moment(new Date().getTime())], false);
    } else {
      changeRangeTime([rangeTime[0], rangeTime[1]], true);
    }
  };

  const changeRangeTime = (dates: [Moment, Moment], isCustomTime) => {
    setRangeTime(dates);
    onChange && onChange([dates[0].valueOf(), dates[1].valueOf()], isCustomTime);
  };

  const onFormChange = (values, allValues) => {
    // 时间范围不能超过14天
    if (allValues.unit === "day" && +allValues.input > 14) {
      form.setFieldsValue({
        input: 14,
      });
    }
    if (allValues.unit === "hour" && +allValues.input > 14 * 24) {
      form.setFieldsValue({
        input: 14 * 24,
      });
    }
    if (allValues.unit === "min" && +allValues.input > 14 * 24 * 60) {
      form.setFieldsValue({
        input: 14 * 24 * 60,
      });
    }
  };

  // 点击确定后
  const onQuickSelect = async () => {
    const values = await form.validateFields();
    const { unit, input, type } = values;
    const rangeValue = unit === "hour" ? input * 60 * 60 * 1000 : unit === "min" ? input * 60 * 1000 : input * 24 * 60 * 60 * 1000;
    setCommonTime(null);
    setIsLastTime(true);
    setIconDisabled(true);
    hide();
    const currentTime = new Date().getTime();
    changeRangeTime([moment(currentTime - rangeValue), moment(currentTime)], false);
  };

  // 点击左右箭头后
  const onQuickSwap = (direction: string) => {
    if (direction === "right" && iconDisabled) {
      return;
    }

    const gap = rangeTime[1].valueOf() - rangeTime[0].valueOf();
    let newRangeTime = rangeTime;

    if (direction === "left") {
      setIconDisabled(false);
      newRangeTime = [moment(rangeTime[0].valueOf() - gap), moment(rangeTime[1].valueOf() - gap)];
    }
    if (direction === "right") {
      setIconDisabled(new Date().getTime() <= rangeTime[1].valueOf() + gap);
      // 如果截止时间大于当前时间则选择当前时间
      const endTime = new Date().getTime() - (rangeTime[1].valueOf() + gap) > 0 ? rangeTime[1].valueOf() + gap : new Date().getTime();
      const startTime =
        new Date().getTime() - (rangeTime[1].valueOf() + gap) > 0 ? rangeTime[0].valueOf() + gap : new Date().getTime() - gap;
      newRangeTime = [moment(startTime), moment(endTime)];
    }
    setCommonTime(null);
    setIsLastTime(false);
    changeRangeTime(newRangeTime, true);
  };

  // 选择常用时间
  const onChooseTime = (value) => {
    setCommonTime(value);
    setIsLastTime(false);
    setIconDisabled(true);
    hide();
    changeRangeTime([moment(new Date().getTime() - value), moment(new Date().getTime())], false);
  };

  const disabledDate = (current: Moment) => {
    if (!dates) {
      return false;
    }

    const tooLate = dates[0] && current.diff(dates[0], "days") > 13;
    const tooEarly = dates[1] && dates[1].diff(current, "days") > 13;
    return !!tooEarly || !!tooLate || (current && current > moment().endOf("day"));
  };

  const renderCustomTimeRange = () => {
    return (
      <>
        <div className="quick-select">
          <div className="quick-select-title">快速选择</div>
          <div className="quick-select-icon">
            <Tooltip title="上一时间窗口">
              <div className="icon" onClick={() => onQuickSwap("left")}>
                <LeftOutlined />
              </div>
            </Tooltip>
            <Tooltip title="下一时间窗口">
              <div className={iconDisabled ? "icon disabled" : "icon"} onClick={() => onQuickSwap("right")}>
                <RightOutlined />
              </div>
            </Tooltip>
          </div>
        </div>
        <div className="options">
          <Form onValuesChange={onFormChange} form={form}>
            <Row>
              <FormItem name="type" initialValue={"Last"}>
                <Select className="type">
                  <Option value="Last">Last</Option>
                  {/* <Option value="Next">Next</Option> */}
                </Select>
              </FormItem>
              <FormItem name="input" initialValue={15}>
                <InputNumber min={1} className="content" />
              </FormItem>
              <FormItem name="unit" initialValue={"min"}>
                <Select className="content">
                  <Option value="min">分钟</Option>
                  <Option value="hour">小时</Option>
                  <Option value="day">天</Option>
                </Select>
              </FormItem>
              <FormItem>
                <Button type="primary" ghost onClick={onQuickSelect}>
                  确定
                </Button>
              </FormItem>
            </Row>
          </Form>
        </div>
        <div className="time-options">
          <div className="title">常用</div>
          <div className="common-items">
            {TIME_OPTIONS.map((item) => {
              return (
                <div
                  key={item.value}
                  className={`item ${commonTime === item.value ? "checked" : ""}`}
                  onClick={() => onChooseTime(item.value)}
                >
                  {item.label}
                </div>
              );
            })}
          </div>
        </div>
      </>
    );
  };

  return (
    <>
      <div className="custom-ranger-picker">
        <RangePicker
          showTime={{
            format: "HH:mm",
          }}
          getPopupContainer={getPopupContainer}
          format="YYYY-MM-DD HH:mm"
          separator="~"
          onCalendarChange={(val, dateStrings, info) => {
            let _dates = val;

            // 时间选择如果超过14天则清除待选择的日期
            if (val[1]?.valueOf() - val[0]?.valueOf() > 14 * 24 * 60 * 60 * 1000) {
              setHackValue([info.range === "end" ? null : val[0], info.range === "start" ? null : val[1]]);
              _dates = [info.range === "end" ? null : val[0], info.range === "start" ? null : val[1]];
              noNeedChange.current = true;
            } else {
              noNeedChange.current = false;
            }
            setDates(_dates);
          }}
          disabledDate={disabledDate}
          value={hackValue || rangeTime}
          onChange={onRangeTimeChange}
          onOpenChange={onOpenChange}
          suffixIcon={null}
        />
        <Popover
          visible={open}
          onVisibleChange={handleOpenChange}
          overlayClassName="custom-popover"
          content={renderCustomTimeRange()}
          getPopupContainer={getPopupContainer}
          placement="bottom"
          trigger="click"
        >
          <div className="custom-btn">
            <span className="icon iconfont iconriqi1 cal-icon"></span>
          </div>
        </Popover>
      </div>
    </>
  );
});
