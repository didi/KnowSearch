import React, { memo, useEffect, useState } from "react";
import { Form, Select } from "antd";
import { SelectTime } from "./select-time";
import "../style";
const { Option } = Select;

interface propsType {
  clusterName: string;
  clusterNameList: { text: string; value: string }[];
  onTimeStampChange: (startTime: number, endTime: number) => void;
  onClusterNameChange: (val: string) => void;
  refreshTime?: number;
}

export const KanbanForm: React.FC<propsType> = memo(
  ({
    onTimeStampChange,
    onClusterNameChange,
    clusterName,
    clusterNameList,
    refreshTime = 0,
  }) => {
    const [form] = Form.useForm();

    useEffect(() => {
      form.setFieldsValue({ clusterName });
    }, [clusterName]);
    return (
      <Form form={form}>
        <div className="kanban-from-box">
          <div className="kanban-from-box-item">
            <Form.Item
              label="集群类型"
              colon={false}
              style={{ marginRight: 15 }}
            >
              <Select
                placeholder="请选择"
                allowClear
                style={{ width: 200 }}
                defaultValue="物理集群"
                disabled
              >
                <Option value="male">物理集群</Option>
              </Select>
            </Form.Item>
            <Form.Item
              name="clusterName"
              label="集群名称"
              colon={false}
              initialValue={clusterName}
            >
              <Select
                placeholder="请选择"
                style={{ width: 200 }}
                onChange={onClusterNameChange}
                value={clusterNameList[0] && clusterNameList[0].value}
                showSearch
              >
                {clusterNameList.map((item, index) => (
                  <Option value={item.value} key={index + item.value}>
                    {item.text}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </div>
          <SelectTime
            onTimeStampChange={onTimeStampChange}
            refreshTime={refreshTime}
          />
        </div>
      </Form>
    );
  }
);
