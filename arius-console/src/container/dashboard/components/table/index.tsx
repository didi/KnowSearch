import React from "react";
import { Spin, Table, Tooltip } from "antd";
import InfoTooltip from "component/infoTooltip";
import "./index.less";

interface ITableProps {
  columns: any[];
  dataSource: any[];
  title: string;
  isLoading: boolean;
  tooltip?: JSX.Element | string;
  unit?: string;
  dictionary?: any;
  metricType?: string;
}

const imgSrc = require("./../../../../assets/empty.png");

const TableCard = (props: ITableProps) => {
  const { columns, dataSource = [], title, tooltip, unit, isLoading, dictionary } = props;

  const renderTitle = () => {
    let { price, currentCalLogic, threshold } = dictionary || {};
    return (
      <div className="dashboard-table-title">
        {title}
        {unit && <span>{`(${dataSource.length}${unit})`}</span>}
        {(price || currentCalLogic || threshold) && (
          <InfoTooltip price={price} currentCalLogic={currentCalLogic} threshold={threshold}></InfoTooltip>
        )}
      </div>
    );
  };

  return (
    <div className="dashboard-table-container">
      <Spin spinning={isLoading || false}>
        {renderTitle()}
        {!isLoading &&
          (dataSource.length ? (
            <Table
              columns={columns}
              dataSource={dataSource}
              rowClassName={(record, index) => {
                return index & 1 ? "even-row" : "";
              }}
              pagination={
                dataSource.length <= 6
                  ? false
                  : {
                      simple: true,
                      total: dataSource.length,
                      pageSize: 6,
                    }
              }
            />
          ) : (
            <div className={"dashboard-table-container-empty"}>
              <div>
                <img src={imgSrc} />
              </div>
              <div>
                <span>数据为空</span>
              </div>
            </div>
          ))}
      </Spin>
    </div>
  );
};

export default TableCard;
