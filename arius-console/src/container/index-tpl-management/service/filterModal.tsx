import React, { useState, useRef } from "react";
import { Button, Space, Checkbox, Col, Row } from "antd";
import "./index.less";
const filterOption = [
  {
    text: "未创建",
    value: false,
    key: "openSrv",
  },
  {
    text: "已创建",
    value: true,
    key: "hasDCDR",
  },
];
export const FilterModal = ({ setSelectedKeys, selectedKeys, confirm, clearFilters }) => {
  const handleSearch = (selectedKeys, confirm) => {
    confirm();
  };
  const handleReset = (clearFilters) => {
    clearFilters();
  };
  return (
    <div
      style={{
        padding: 8,
      }}
    >
      <div>DCDR链路创建情况</div>
      <Checkbox.Group
        style={{
          marginBottom: 8,
          display: "block",
          width: "100%",
        }}
        value={selectedKeys}
        onChange={(e) => {
          setSelectedKeys(e ? e : []);
        }}
      >
        {filterOption.map((item) => (
          <div key={item.key}>
            <Checkbox value={item.value}>{item.text}</Checkbox>
          </div>
        ))}
      </Checkbox.Group>
      <Space>
        <Button
          type="primary"
          onClick={() => handleSearch(selectedKeys, confirm)}
          size="small"
          style={{
            width: 90,
          }}
        >
          确定
        </Button>
        <Button
          onClick={() => clearFilters && handleReset(clearFilters)}
          size="small"
          style={{
            width: 90,
          }}
        >
          重置
        </Button>
      </Space>
    </div>
  );
};
