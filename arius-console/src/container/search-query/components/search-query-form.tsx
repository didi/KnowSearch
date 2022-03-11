import React, { memo, useEffect, useState } from "react";
import { Button, Form, Input } from "antd";
import "./search-query-form.less";
import { SelectTime } from "./select-time";
import { PERIOD_RADIO_MAP, PERIOD_RADIO } from "../config";

interface SearchQueryFormPropsType {
  setSearchQuery: (params) => void;
  reload?: boolean;
  isSlow?: boolean;
  value?: any;
}
export const SearchQueryForm: React.FC<SearchQueryFormPropsType> = memo(
  ({ setSearchQuery, reload, isSlow, value }) => {
    const dates = PERIOD_RADIO_MAP.get("oneDay").dateRange;
    const [startTime, setStartTime] = useState(dates[0].valueOf());
    const [endTime, setEndTime] = useState(dates[1].valueOf());
    const [queryIndex, setQueryIndex] = useState("");
    const [totalCost, setTotalCost] = useState("");
    const [form] = Form.useForm();
    const onTimeStampChange = (startDate, endDate) => {
      setStartTime(startDate);
      setEndTime(endDate);
    };
    const onSearch = () => {
      setSearchQuery &&
        setSearchQuery({
          queryIndex,
          startTime,
          endTime,
          totalCost
        });
    };

    React.useEffect(() => {
      return () => {
        sessionStorage.setItem(value, JSON.stringify({
          totalCost,
          queryIndex,
          startTime,
          endTime,
        }))
      }
    }, [totalCost, queryIndex, startTime, endTime])

    useEffect(() => {
      let params: any = sessionStorage.getItem(value);
      if (params) {
        params = JSON.parse(params);
        setStartTime(params?.startTime);
        setEndTime(params?.endTime);
        setQueryIndex(params?.queryIndex);
        setTotalCost(params?.totalCost);
        form.setFieldsValue({ queryIndex: params.queryIndex, totalCost: params.totalCost })
        setTimeout(() => {
          setSearchQuery && setSearchQuery({
            queryIndex: params?.queryIndex,
            startTime: params?.startTime,
            endTime: params?.endTime,
            totalCost: params?.totalCost
          });
        }, 400);
      }
    }, [form])
    return (
      <Form form={form}>
        <div className="search-query-from-box">
          <div className="search-query-from-box-item">
            {
              isSlow ? <Form.Item label="总耗时" name="totalCost">
                <Input
                  style={{ width: 200, marginRight: 20 }}
                  value={totalCost}
                  onChange={(e) => {
                    const { value } = e.target;
                    setTotalCost(value);
                  }}
                  placeholder="请输入"
                />
              </Form.Item>
                : null
            }
            <Form.Item label="查询索引" name="queryIndex">
              <Input
                placeholder="请输入"
                style={{ width: 200 }}
                value={queryIndex}
                onChange={(e) => {
                  const { value } = e.target;
                  setQueryIndex(value);
                }}
              />
            </Form.Item>
          </div>
          <div className="search-query-from-box-item">
            <SelectTime
              onTimeStampChange={onTimeStampChange}
              periodRadio={PERIOD_RADIO}
              periodRadioMap={PERIOD_RADIO_MAP}
              value={[startTime, endTime]}
              reload={reload}
            />
          </div>
          <div className="search-query-from-box-item">
            <Button type="primary" onClick={onSearch}>
              查询
            </Button>
          </div>
        </div>
      </Form>
    );
  }
);
