import { Table } from "antd";
import React, { memo } from "react";
import moment from "moment";
import "../style/index";

const classPrefix = "rf-monitor";
interface propsType {
  dataSource: any[];
  shardColumns: any[];
  title: string;
  currentTime: string;
  isLoading?: boolean;
}

export const Shard: React.FC<propsType> = memo(
  ({ title, shardColumns, dataSource, currentTime, isLoading = false }) => {
    const date = moment(Date.parse(currentTime)).format("yyyy-MM-DD HH:mm");
    return (
      <div
        className={`${classPrefix}-overview-content-line-container shard-container`}
      >
        <div
          className={`${classPrefix}-overview-content-line-item rf-monitor-overview-content-shard`}
        >
          <div className={`${classPrefix}-overview-content-line-item-header`}>
            <div
              className={`${classPrefix}-overview-content-line-item-header-title`}
            >
              {title}
            </div>
            <div
              className={`${classPrefix}-overview-content-line-item-header-time`}
            >
              {`时间： ${date}`}
            </div>
          </div>
          <div>
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
      </div>
    );
  }
);
