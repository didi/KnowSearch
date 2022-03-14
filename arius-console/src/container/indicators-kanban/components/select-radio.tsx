import React, { memo, useEffect, useState } from "react";
import { Radio, Select } from "antd";
import { TOP_MAP } from "constants/status-map";
const classPrefix = "rf-monitor";

interface propsType {
  topNu: number;
  setTopNu: (val: number) => void;
  content: string;
  setContent?: (val: string) => void;
  contentList: string[] | { name: string; value: string }[] | any;
  placeholder?: string;
  type?: string;
  allowClear?: boolean;
}

export const SelectRadio = memo((props: propsType) => {
  const { topNu, setTopNu, content, setContent, contentList, placeholder, allowClear, type } =
    props;
  return (
    <>
      {
        type === 'Gateway' ? null : <Radio.Group
          className={`${classPrefix}-overview-search-filter-item`}
          value={topNu}
          onChange={(e) => {
            if (type === 'Gateway') {
              setTopNu(e.target.value);
            } else {
              setTopNu(e.target.value);
              setContent("");
            }
          }}
        >
          {TOP_MAP.map((item) => (
            <Radio.Button key={item.value} value={item.value}>
              {item.label}
            </Radio.Button>
          ))}
        </Radio.Group>
      }
      <Select
        placeholder={placeholder}
        // allowClear={false}
        style={{ width: 200 }}
        value={content || undefined}
        onChange={(val) => {
          if (type === 'Gateway') {
            setContent(val || '');
          } else {
            setTopNu(0);
            setContent(val);
          }
        }}
        allowClear={allowClear || false}
        showSearch
        className={`${classPrefix}-overview-search-filter-item`}
      >
        {contentList.map((item, index) => {
          if (typeof item === "string") {
            return (
              <Select.Option value={item} key={item + index}>
                {item}
              </Select.Option>
            );
          } else {
            return (
              <Select.Option value={item.value} key={item + index}>
                {item.name}
              </Select.Option>
            );
          }
        })}
      </Select>
      {
        type === 'Gateway' ? <Radio.Group
          className={`${classPrefix}-overview-search-filter-item`}
          value={topNu}
          onChange={(e) => {
            if (type === 'Gateway') {
              setTopNu(e.target.value);
            } else {
              setTopNu(e.target.value);
              setContent("");
            }
          }}
        >
          {TOP_MAP.map((item) => (
            <Radio.Button key={item.value} value={item.value}>
              {item.label}
            </Radio.Button>
          ))}
        </Radio.Group> : null
      }
    </>
  );
});
