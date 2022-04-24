import React from "react";
import { Tabs } from "antd";
import { HashMenu } from "./../../component/hash-menu";
import { operationTabs, OPERATION_MENU_MAP } from './config'

const Operation = () => {
  const department: string = localStorage.getItem("current-project");
  return (<div>
    <HashMenu
      TAB_LIST={operationTabs}
      MENU_MAP={OPERATION_MENU_MAP}
      defaultHash="cluster"
      prefix="dashboard-"
      key={department}
    />
  </div>)
}

export default Operation;
