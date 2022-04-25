import React, { memo, useEffect, useState } from "react";
import { Radio, Select } from "antd";
import { TOP_MAP } from "constants/status-map";
const classPrefix = "rf-monitor";

interface propsType {
  topNu: number;
  setTopNu: (val: number) => void;
  content: string | string[];
  setContent?: (val: string | any) => void;
  contentList: string[] | { name: string; value: string }[] | any;
  placeholder?: string;
  type?: string;
  allowClear?: boolean;
  style?: object
}

export const SelectRadio = memo((props: propsType) => {
  const { topNu, setTopNu, content, setContent, contentList, placeholder, allowClear, type, style } =
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
              setContent(type == 'node' ? [] : "");
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
        mode={type == 'node' ? 'multiple' : null}
        maxTagCount="responsive"
        placeholder={placeholder}
        style={{ width: type == 'node' ? 350 : 200, ...style }}
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
        filterOption={(val, option) => { return option.children.includes(val.trim()) }}
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
              setContent(type == 'node' ? [] : "");
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
