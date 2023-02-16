import React, { memo, useEffect, useRef, useState } from "react";
import { Button, Dropdown, Popover, Radio, Select, Space, Tooltip, Tag, TreeSelect } from "antd";
import { TOP_MAP, TOP_TIME_RANGE, TOP_TYPE } from "../../../constants/status-map";
import { DownOutlined, SettingOutlined } from "@ant-design/icons";
import Url from "lib/url-parser";
import "./style.less";
import { getPopupContainer } from "lib/utils";
const classPrefix = "monitor";
const { SHOW_CHILD } = TreeSelect;

interface propsType {
  topNu?: number;
  setTopNu?: (val: number) => void;
  content: string | string[];
  setContent?: (val: string | any) => void;
  contentList: string[] | { name: string; value: string }[] | any;
  placeholder?: string;
  type?: string;
  allowClear?: boolean;
  style?: object;
  onValueChange?: any;
  secondSelectValue?: any;
  secondSelectList?: any;
  onSecondSelectChange?: any;
  secondSelectPlaceholder?: string;
}

export const SelectRadio = memo((props: propsType) => {
  const {
    content,
    setContent,
    topNu,
    contentList,
    placeholder,
    allowClear,
    type,
    style,
    onValueChange,
    secondSelectPlaceholder,
    secondSelectValue,
    secondSelectList,
    onSecondSelectChange,
  } = props;

  const [topNum, setTopNum] = useState(topNu ?? TOP_MAP[0].value);
  const [visible, setVisible] = useState(false);
  const [topTimeStep, setTopRange] = useState(TOP_TIME_RANGE[0].value);
  const [topMethod, setTopType] = useState(TOP_TYPE[0].value);

  const lastStatus = useRef({
    topTimeStep: TOP_TIME_RANGE[0].value,
    topMethod: TOP_TYPE[0].value,
  });

  useEffect(() => {
    if (Url().search?.node || Url().search?.template || Url().search?.index) {
      setTopNum(null);
    }
  }, []);

  const onTypeValueChange = (type, value) => {
    if (type === "topTimeStep") {
      lastStatus.current.topTimeStep = topTimeStep;
      setTopRange(value);
    }
    if (type === "topMethod") {
      lastStatus.current.topMethod = topMethod;
      setTopType(value);
    }
  };

  const handleValueChange = (params) => {
    const { topNum, topTimeStep, topMethod, content, needReload = true } = params;
    onValueChange && onValueChange({ topNum, topTimeStep, topMethod, content }, needReload);
  };

  const setValues = (params) => {
    const { topNum, topTimeStep, topMethod, content, needReload = true } = params;
    setTopNum(topNum);
    // TopN计算规则默认1分钟、最大值
    const timeStep = topTimeStep || TOP_TIME_RANGE[0].value;
    const method = topMethod || TOP_TYPE[0].value;
    onTypeValueChange("topTimeStep", timeStep);
    onTypeValueChange("topMethod", method);
    handleValueChange({ topNum, topTimeStep: timeStep, topMethod: method, content, needReload });
  };

  const onSubmit = () => {
    handleValueChange({ topNum, topTimeStep, topMethod, content });
    lastStatus.current.topTimeStep = topTimeStep;
    lastStatus.current.topMethod = topMethod;

    setVisible(false);
  };

  const onCancel = () => {
    setTopRange(lastStatus.current.topTimeStep);
    setTopType(lastStatus.current.topMethod);
    setVisible(false);
  };

  const popoverContent = () => {
    return (
      <div className="top-popover">
        <div className="top-radio">
          <div className="range">
            <div className="title">Top计算时间步长</div>
            <Radio.Group
              className="time-radio-group"
              onChange={(e) => onTypeValueChange("topTimeStep", e.target.value)}
              value={topTimeStep}
            >
              <Space direction="vertical" size={16}>
                {TOP_TIME_RANGE.map((item, index) => (
                  <Radio.Button value={item.value} key={index}>
                    {item.label}
                  </Radio.Button>
                ))}
              </Space>
            </Radio.Group>
          </div>
          <div>
            <div className="title">Top计算方式</div>
            <Radio.Group className="time-radio-group" onChange={(e) => onTypeValueChange("topMethod", e.target.value)} value={topMethod}>
              <Space direction="vertical" size={16}>
                {TOP_TYPE.map((item, index) => (
                  <Radio.Button value={item.value} key={index}>
                    {item.label}
                  </Radio.Button>
                ))}
              </Space>
            </Radio.Group>
          </div>
        </div>
        <div className="btns">
          <Button size="small" onClick={onCancel}>
            取消
          </Button>
          <Button size="small" type="primary" onClick={onSubmit}>
            应用
          </Button>
        </div>
      </div>
    );
  };

  return (
    <>
      <Tooltip overlayClassName="top-n-tips" placement="topLeft" title="TopN计算规则配置，只作用于Top5-Top20算法">
        <Popover placement="bottomLeft" visible={visible} onVisibleChange={setVisible} content={popoverContent} trigger="click">
          <Button className="setting-btn" icon={<SettingOutlined />}>
            <DownOutlined />
          </Button>
        </Popover>
      </Tooltip>

      <Radio.Group
        className={`${classPrefix}-overview-search-filter-item`}
        value={topNum}
        onChange={(e) => {
          setValues({
            topNum: e.target.value,
            topTimeStep: topTimeStep || TOP_TIME_RANGE[0].value,
            topMethod: topMethod || TOP_TYPE[0].value,
            content: type !== "secondSelect" ? (type == "node" ? [] : undefined) : content,
          });
        }}
      >
        {TOP_MAP.map((item) => (
          <Radio.Button key={item.value} value={item.value}>
            {item.label}
          </Radio.Button>
        ))}
      </Radio.Group>
      {type == "treeSelect" ? (
        <div className="tree-select-node">
          <TreeSelect
            getPopupContainer={getPopupContainer}
            treeData={contentList}
            value={content || undefined}
            onChange={(val) => {
              setValues({
                topNum: 0,
                topTimeStep: undefined,
                topMethod: undefined,
                content: val,
              });
            }}
            treeCheckable
            showCheckedStrategy={SHOW_CHILD}
            placeholder={placeholder}
            style={{ width: "100%", ...style }}
          />
        </div>
      ) : (
        <div className="overview-select-mode">
          <Select
            mode={type == "node" ? "multiple" : null}
            showArrow
            maxTagCount="responsive"
            placeholder={placeholder}
            style={{ width: "100%", ...style }}
            value={content || undefined}
            onChange={(val) => {
              if (!val || (typeof val === "object" && !val.length)) {
                setValues({
                  topNum: TOP_MAP[0].value,
                  topTimeStep: TOP_TIME_RANGE[0].value,
                  topMethod: TOP_TYPE[0].value,
                  content: val,
                });
                return;
              }
              const isSecondSelect = type === "secondSelect";
              setValues({
                topNum: isSecondSelect ? topNum || TOP_MAP[0].value : 0,
                topTimeStep: isSecondSelect ? topTimeStep || TOP_TIME_RANGE[0].value : undefined,
                topMethod: isSecondSelect ? topMethod || TOP_TYPE[0].value : undefined,
                content: val,
              });
            }}
            allowClear={allowClear || false}
            showSearch
            filterOption={(val, option: any) => {
              if (type === "node" && typeof option.children !== "string") {
                return option.key.toLowerCase().indexOf(val.toLowerCase()) >= 0;
              } else {
                return option.children.includes(val.trim());
              }
            }}
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
                  <Select.Option value={item.value} key={JSON.stringify(item) + index}>
                    {type === "node" ? (
                      <div className="node-select-item">
                        <span>{item.name}</span>
                        <Tag color="blue">{item.tips}</Tag>
                      </div>
                    ) : (
                      item.name
                    )}
                  </Select.Option>
                );
              }
            })}
          </Select>
        </div>
      )}

      {type === "secondSelect" ? (
        <Select
          className="second-select"
          placeholder={secondSelectPlaceholder}
          onChange={(e: string) => {
            setValues({
              topNum: e ? 0 : TOP_MAP[0].value,
              topTimeStep: e ? undefined : TOP_TIME_RANGE[0].value,
              topMethod: e ? undefined : TOP_TYPE[0].value,
              content,
              needReload: false,
            });
            onSecondSelectChange(e || "");
          }}
          value={secondSelectValue || null}
          showSearch
          filterOption={(val, option) => {
            return option.children.includes(val.trim());
          }}
          allowClear
        >
          {secondSelectList.map((item, index) => {
            if (typeof item === "string") {
              return (
                <Select.Option value={item} key={item + index}>
                  {item}
                </Select.Option>
              );
            } else {
              return (
                <Select.Option value={item.value} key={item.value + index}>
                  {item.name}
                </Select.Option>
              );
            }
          })}
        </Select>
      ) : null}
    </>
  );
});
