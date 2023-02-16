import { dashboardIndex } from "api/dashboard";
import React, { useEffect, useState, createContext } from "react";
import { HashMenu } from "knowdesign";
import { operationTabs, OPERATION_MENU_MAP } from "./config";
import { useDispatch } from "react-redux";
import { setDashBoardDymanicMetrics } from "actions/dashBoard";
import "./index.less";
import { getDictionary } from "api/dashboard";

export const DashboardContext = createContext(null);

const OperationPage = () => {
  const department: string = localStorage.getItem("current-project");
  const dispatch = useDispatch();

  const [dictionary, setDictionary] = useState({});

  useEffect(() => {
    dashboardIndex().then((res) => {
      dispatch(
        setDashBoardDymanicMetrics({
          dymanicConfigMetrics: res || [],
        })
      );
    });
    _getDictionary();
  }, []);

  const _getDictionary = async () => {
    let params = {
      model: "Dashboard",
    };
    let res = await getDictionary(params);
    let clusterData = {};
    let nodeData = {};
    let indexData = {};
    (res || []).forEach((item: any) => {
      if (item?.metricType) {
        switch (item?.type) {
          case "集群":
            clusterData[item?.metricType] = item;
            break;
          case "节点":
            nodeData[item?.metricType] = item;
            break;
          case "索引":
            indexData[item?.metricType] = item;
            break;
          default:
            break;
        }
      }
    });
    setDictionary({ clusterData, nodeData, indexData });
  };

  return (
    <DashboardContext.Provider value={dictionary}>
      <HashMenu TAB_LIST={operationTabs} MENU_MAP={OPERATION_MENU_MAP} defaultHash="cluster" prefix="dashboard" key={department} />
    </DashboardContext.Provider>
  );
};

export default OperationPage;
