import React, { memo, useEffect, useState } from "react";
import { connect, useDispatch } from "react-redux";
import { setGatewayForm } from "../../../actions/gateway-kanban";
import { oneDayMillims } from "../../../constants/common";
import { TAB_LIST, MENU_MAP } from "./config";
import { HashMenu } from "knowdesign";
import { isSuperApp } from "lib/utils";
import { RefreshTime } from "../components";
import { CustomPickTime } from "../components/custom-time-picker";
import { AppState } from "store/type";
import { RenderEmpty } from "component/LogClusterEmpty";
import "../style";

const mapStateToProps = (state) => ({
  app: state.app,
});

export const GatewayKanban = connect(mapStateToProps)((props: { app: AppState }) => {
  const department: string = localStorage.getItem("current-project");
  const dispatch = useDispatch();
  const [refreshTime, setRefreshTime] = useState(0);
  const [toTopVisible, setToTopVisible] = useState(false);

  const container = document.querySelector(".d1-layout-main");

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
        endTime: currentTime-120000,
        isMoreDay: false,
        timeRadioKey: "oneHour",
      })
    );
    container.addEventListener("scroll", handleScroll);
    return () => {
      container.removeEventListener("scroll", handleScroll);
    };
  }, [department]);

  useEffect(() => {
    handleScroll();
  }, [toTopVisible]);

  const handleScroll = () => {
    if (container.scrollTop > 500) {
      setToTopVisible(true);
    } else {
      setToTopVisible(false);
    }
  };

  if (props.app.gatewayStatus === false && !isSuperApp()) {
    // if (isSuperApp()) {
    //   return (
    //     <RenderEmpty
    //       {...props}
    //       title="未部署Gateway集群"
    //       desc="请前往集群管理 ———— 「Gateway集群」，进行Gateway部署"
    //       href="/cluster/gateway"
    //       btnText="接入Gateway"
    //     />
    //   );
    // }
    return <RenderEmpty {...props} title="未部署Gateway集群，请联系管理员进行Gateway部署" desc="" href="" btnText="" />;
  }

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
    </>
  );
});
