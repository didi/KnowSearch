import { SyncOutlined } from "@ant-design/icons";
import React from "react";
import { formatterTimeYMDHMS } from "../config";

export const OperationPanel = (props) => {
  const { classPrefix, reloadPage, endTime, renderFilter } = props;

  return (
    <div className={`${classPrefix}-overview-search`}>
      <div className={`${classPrefix}-overview-search-reload`}>
        <SyncOutlined className="dashboard-config-icon" onClick={reloadPage} />
      </div>
      <div className={`${classPrefix}-overview-search-filter`}>{renderFilter()}</div>
    </div>
  );
};
