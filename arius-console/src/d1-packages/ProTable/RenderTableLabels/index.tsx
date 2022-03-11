import "./index.less";
import React from "react";
import { Tooltip } from "antd";
interface Data {
  list: any[];
  limit: number;
}

export const renderTableLabels = (data: Data): JSX.Element => {
  const { list, limit = 2 } = data as { list: any; limit: number };
  let arr = list;
  if (!Array.isArray(list) && list) {
    arr = list.split(",");
  }
  const showItems = arr.slice(0, limit) || [];
  const hideItems = arr.slice(limit, list.length) || [];

  return (
    <div className="table-long-content-col">
      {showItems.length > 0
        ? showItems.map((item, index) => (
            <Tooltip key={index} placement="bottomLeft" title={item}>
              <span className="show-item" key={index}>
                {item}
              </span>
            </Tooltip>
          ))
        : "-"}
      {hideItems.length > 0 && (
        <Tooltip
          placement="bottomLeft"
          color="#fff"
          title={
            <div className="content-hide-box">
              {list.map((item, index) => (
                <span className="hide-item" key={index}>
                  {item}
                </span>
              ))}
            </div>
          }
        >
          <span className="content-total">{`共${list.length}个`}</span>
        </Tooltip>
      )}
    </div>
  );
};
