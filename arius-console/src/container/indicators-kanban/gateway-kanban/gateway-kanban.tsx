import React, { memo, useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { setGatewayForm } from "../../../actions/gateway-kanban";
import { oneDayMillims } from "../../../constants/common";
import { TAB_LIST, MENU_MAP } from "./config";
import { HashMenu } from "knowdesign";
import "../style";

import { RefreshTime } from "../components";
import { CustomPickTime } from "../components/custom-time-picker";

export const GatewayKanban = () => {
  const department: string = localStorage.getItem("current-project");
  const dispatch = useDispatch();
  const [refreshTime, setRefreshTime] = useState(0);

  const onTimeStampChange = (startTime: number, endTime: number, timeRadioKey?: string) => {
    const timeMinus = endTime - startTime;
    dispatch(
      setGatewayForm({
        startTime,
        endTime,
        isMoreDay: timeMinus > oneDayMillims,
        timeRadioKey,
      })
    );
  };

  useEffect(() => {
    const ONE_HOUR = 1000 * 60 * 60;
    const currentTime = new Date().getTime();
    dispatch(
      setGatewayForm({
        startTime: currentTime - ONE_HOUR,
        endTime: currentTime - 120000,
        isMoreDay: false,
        timeRadioKey: "oneHour",
      })
    );
  }, [department]);

  return (
    <>
      <div className="hash-menu-container gateway">
        <HashMenu
          prefix="gateway-kanban-content"
          TAB_LIST={TAB_LIST}
          MENU_MAP={MENU_MAP}
          defaultHash="node"
          // 监听页面权限的变化
          key={department}
        />
        <CustomPickTime onTimeStampChange={onTimeStampChange} refreshTime={refreshTime} />
        <div className="refresh-time gateway-refresh">
          <RefreshTime changeRefreshTime={setRefreshTime} />
        </div>
      </div>
    </>
  );
};
