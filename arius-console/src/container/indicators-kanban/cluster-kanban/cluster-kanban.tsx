import React, { memo, useState, useEffect, useRef } from "react";
import { useSelector, useDispatch, shallowEqual } from "react-redux";
import { setClusterForm } from "../../../actions/cluster-kanban";
import { RenderTitle } from "component/render-title";
import { oneDayMillims } from "../../../constants/common";
import { getLogicClusterNames } from "../../../api/cluster-kanban";
import { TAB_LIST, MENU_MAP, CLUSTER_KANBAN_MENU } from "./config";
import { KanbanForm, RefreshTime } from "../components";
import { HashMenu } from "knowdesign";
import Url from "lib/url-parser";
import { Spin } from "knowdesign";
import "../style";
import { isSuperApp } from "lib/utils";
import { getPhyClusterPerApp } from "api/cluster-index-api";
import { RenderEmpty } from "component/LogClusterEmpty";
const ONE_HOUR = 1000 * 60 * 60;

export const ClusterKanban = (props) => {
  const department: string = localStorage.getItem("current-project");
  const { clusterKanban } = useSelector(
    (state) => ({
      clusterKanban: (state as any).clusterKanban,
    }),
    shallowEqual
  );

  const dispatch = useDispatch();

  const currentTime = new Date().getTime();
  const [startTime, setStartTime] = useState(currentTime - ONE_HOUR);
  const [endTime, setEndTime] = useState(currentTime-120000);
  const [clusterName, setClusterName] = useState(clusterKanban.clusterName);
  const [clusterNameList, setClusterNameList] = useState([]);
  const [refreshTime, setRefreshTime] = useState(0);
  const [pageLoad, setPageLoad] = useState(false);
  const [toTopVisible, setToTopVisible] = useState(false);

  const superApp = isSuperApp();

  const container = document.querySelector(".d1-layout-main");

  useEffect(() => {
    setSelectClusterName();
    container.addEventListener("scroll", handleScroll);
    return () => {
      container.removeEventListener("scroll", handleScroll);
    };
  }, []);

  useEffect(() => {
    setClusterName(Url().search.cluster);
  }, [Url().search.cluster]);

  useEffect(() => {
    updateCluster({
      startTime,
      endTime,
      clusterName,
      timeRadioKey: clusterKanban.timeRadioKey,
    });
    return () => {
      updateCluster({
        startTime,
        endTime,
        clusterName: "",
      });
    };
  }, [clusterName]);

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

  const updateCluster = (props) => {
    const { startTime, endTime, clusterName, timeRadioKey } = props;
    // if (!clusterName) {
    //   return;
    // }
    const timeMinus = endTime - startTime;
    dispatch(
      setClusterForm({
        startTime,
        endTime,
        clusterName,
        isMoreDay: timeMinus > oneDayMillims,
        timeRadioKey,
      })
    );
  };

  const onTimeStampChange = (startTime, endTime, timeRadioKey) => {
    updateCluster({
      startTime,
      endTime,
      clusterName,
      timeRadioKey,
    });
    setStartTime(startTime);
    setEndTime(endTime);
  };

  const setSelectClusterName = async () => {
    const superApp = isSuperApp();
    setPageLoad(true);
    try {
      const clusterNames = await (superApp ? getPhyClusterPerApp() : getLogicClusterNames());
      setPageLoad(false);
      if (clusterNames && clusterNames.length > 0) {
        setClusterNameList(clusterNames.map((item) => ({ text: item, value: item })));
        setClusterName(Url().search.cluster || clusterNames[0]);
      }
    } catch (error) {
      console.log(error, "错误");
      setPageLoad(false);
    }
  };

  const renderTitleContent = () => {
    return {
      title: "集群看板",
      content: null,
    };
  };

  const renderNode = () => {
    return (
      <>
        {/* <div className="indicators-header" id="indicators-header">
          <div className="kanban-header-box">
            <RenderTitle {...renderTitleContent()} />
          </div>
          <KanbanForm
            clusterName={clusterName}
            clusterNameList={clusterNameList}
            onTimeStampChange={onTimeStampChange}
            onClusterNameChange={(val) => {
              setClusterName(val);
            }}
            refreshTime={refreshTime}
          />
        </div> */}
        <div className="hash-menu-container cluster">
          <HashMenu
            TAB_LIST={CLUSTER_KANBAN_MENU()}
            MENU_MAP={MENU_MAP}
            defaultHash={isSuperApp() ? "overview" : "index"}
            // 监听页面权限的变化
            key={department + JSON.stringify(Url().search)}
          />
          <div className="kanban-form">
            <KanbanForm
              clusterName={clusterName}
              clusterNameList={clusterNameList}
              onTimeStampChange={onTimeStampChange}
              onClusterNameChange={(val) => {
                setClusterName(val);
              }}
              refreshTime={refreshTime}
            />
          </div>

          <div className="refresh-time">
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
  };

  return (
    <div className="cluster-kanban-container">
      <Spin className="index-spin-name" spinning={pageLoad}>
        {superApp
          ? renderNode()
          : clusterNameList.length
          ? !pageLoad && renderNode()
          : !pageLoad && (
              <div>
                <RenderEmpty {...props} />
              </div>
            )}
      </Spin>
    </div>
  );
};
