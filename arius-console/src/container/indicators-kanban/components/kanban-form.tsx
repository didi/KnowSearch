import React, { memo, useEffect, useState } from "react";
import { Form, Select } from "antd";
import "../style";
import { CustomPickTime } from "./custom-time-picker";
const { Option } = Select;

interface propsType {
  clusterName: string;
  clusterNameList: { text: string; value: string }[];
  onTimeStampChange: (startTime: number, endTime: number, radioCheckedKey?: string) => void;
  onClusterNameChange: (val: string) => void;
  refreshTime?: number;
}

export const KanbanForm: React.FC<propsType> = memo(
  ({ onTimeStampChange, onClusterNameChange, clusterName, clusterNameList, refreshTime = 0 }) => {
    const [form] = Form.useForm();

    useEffect(() => {
      form.setFieldsValue({ clusterName: clusterName || (clusterNameList[0] && clusterNameList[0].value) });
    }, [clusterName]);

    return (
      <Form form={form}>
        <div className="kanban-from-box">
          <div className="kanban-from-box-item">
            <Form.Item name="clusterName" label="" colon={false} initialValue={clusterName}>
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
          <CustomPickTime onTimeStampChange={onTimeStampChange} refreshTime={refreshTime} />
        </div>
      </Form>
    );
  }
);
