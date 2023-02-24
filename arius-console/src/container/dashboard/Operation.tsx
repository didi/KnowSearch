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
  const [toTopVisible, setToTopVisible] = useState(false);

  const container = document.querySelector("#d1-layout-main");

  useEffect(() => {
    dashboardIndex().then((res) => {
      dispatch(
        setDashBoardDymanicMetrics({
          dymanicConfigMetrics: res || [],
        })
      );
    });
    _getDictionary();
    container.addEventListener("scroll", handleScroll);
    return () => {
      container.removeEventListener("scroll", handleScroll);
    };
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

  const handleScroll = () => {
    if (container.scrollTop > 500) {
      setToTopVisible(true);
    } else {
      setToTopVisible(false);
    }
  };

  return (
    <DashboardContext.Provider value={dictionary}>
      <HashMenu TAB_LIST={operationTabs} MENU_MAP={OPERATION_MENU_MAP} defaultHash="cluster" prefix="dashboard" key={department} />
      {toTopVisible && (
        <div
          className="to-top"
          onClick={() => {
            container.scrollTop = 0;
          }}
        >
          <svg className="icon svg-icon svg-style" aria-hidden="true">
            <use xlinkHref="#icontop"></use>
          </svg>
        </div>
      )}
    </DashboardContext.Provider>
  );
};

export default OperationPage;
