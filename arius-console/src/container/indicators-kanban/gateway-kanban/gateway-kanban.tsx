import React, { memo, useEffect, useState } from "react";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import { setGatewayForm } from "../../../actions/gateway-kanban";
import { oneDayMillims } from "../../../constants/common";
import { TAB_LIST, MENU_MAP } from "./config";

import "../style";

import { SelectTime, HashMenu, RefreshTime } from "../components";

export const classPrefix = "rf-monitor";

export const GatewayKanban = () => {
  const department: string = localStorage.getItem("current-project");
  const dispatch = useDispatch();
  const [refreshTime, setRefreshTime] = useState(0);

  const onTimeStampChange = (startTime: number, endTime: number) => {
    const timeMinus = endTime - startTime;
    dispatch(
      setGatewayForm({
        startTime: startTime,
        endTime: endTime,
        isMoreDay: timeMinus > oneDayMillims,
      })
    );
  };

  useEffect(() => {
    const ONE_HOUR = 1000 * 60 * 60;
    const currentTime = new Date().getTime();
    dispatch(
      setGatewayForm({
        startTime: currentTime - ONE_HOUR,
        endTime: currentTime,
        isMoreDay: false,
      })
    );
  }, [department]);

  return (
    <>
      <div className="table-header">
        <div className="gateway-kanban-header-box">
          <h3>网关看板</h3>
          <div className="gateway-kanban-header-box-select">
            <div style={{ paddingTop: 10 }}>
              <SelectTime
                onTimeStampChange={onTimeStampChange}
                refreshTime={refreshTime}
              />
            </div>
            {/* <span className="gateway-kanban-header-box-content">
              Kibana查看
            </span> */}
          </div>
        </div>
      </div>
      <div className="hash-menu-container">
        <HashMenu
          TAB_LIST={TAB_LIST}
          MENU_MAP={MENU_MAP}
          defaultHash="overview"
          // 监听页面权限的变化
          key={department}
        />
        <div className="refresh-time">
          <RefreshTime changeRefreshTime={setRefreshTime} />
        </div>
      </div>
    </>
  );
};
