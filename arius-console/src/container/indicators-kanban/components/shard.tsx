import { Table } from "antd";
import React, { memo } from "react";
import moment from "moment";
import "../style/index";
import "./style.less";

interface propsType {
  dataSource: any[];
  shardColumns: any[];
  title: string | React.ReactNode;
  currentTime: string;
  isLoading?: boolean;
  tooltip?: React.ReactNode;
}

export const Shard: React.FC<propsType> = memo(({ title, tooltip, shardColumns, dataSource, currentTime, isLoading = false }) => {
  const date = moment(Date.parse(currentTime)).format("yyyy-MM-DD HH:mm");
  return (
    <div className="shard-container">
      <div className="shard-title">
        <div className="title-content">
          <span className="title-text">{title}</span>
          {tooltip}
        </div>
        <div className="title-time">{`时间： ${date}`}</div>
      </div>
      <div className="shard-table">
        <Table
          loading={isLoading}
          rowKey={"key"}
          dataSource={dataSource}
          columns={shardColumns}
          pagination={{
            // simple: true,
            showSizeChanger: false,
            showQuickJumper: true,
            size: "small",
            position: ["bottomRight"],
            pageSize: 5,
            total: dataSource?.length,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </div>
    </div>
  );
});
