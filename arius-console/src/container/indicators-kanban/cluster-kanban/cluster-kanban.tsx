import React, { memo, useState, useEffect, useRef } from "react";
import { useSelector, useDispatch, shallowEqual } from "react-redux";
import { setClusterForm } from "../../../actions/cluster-kanban";
import { RenderTitle } from "component/render-title";
import { oneDayMillims } from "../../../constants/common";
import { getClusterNameList } from "../../../api/cluster-kanban";
import { TAB_LIST, MENU_MAP } from "./config";
import { KanbanForm, HashMenu, RefreshTime } from "../components";

import "../style";

const ONE_HOUR = 1000 * 60 * 60;

export const ClusterKanban = () => {
  const department: string = localStorage.getItem("current-project");
  const oldDepartment = useRef(department);
  const { clusterKanban } = useSelector(
    (state) => ({
      clusterKanban: (state as any).clusterKanban,
    }),
    shallowEqual
  );

  const dispatch = useDispatch();

  const currentTime = new Date().getTime();
  const [startTime, setStartTime] = useState(currentTime - ONE_HOUR);
  const [endTime, setEndTime] = useState(currentTime);
  const [clusterName, setClusterName] = useState(clusterKanban.clusterName);
  const [clusterNameList, setClusterNameList] = useState([]);
  const [refreshTime, setRefreshTime] = useState(0);

  const updateCluster = (props) => {
    const { startTime, endTime, clusterName } = props;
    if (!clusterName) {
      return;
    }
    const timeMinus = endTime - startTime;
    dispatch(
      setClusterForm({
        startTime,
        endTime,
        clusterName,
        isMoreDay: timeMinus > oneDayMillims,
      })
    );
  };

  const onTimeStampChange = (startTime, endTime) => {
    updateCluster({
      startTime,
      endTime,
      clusterName,
    });
    setStartTime(startTime);
    setEndTime(endTime);
  };

  const setSelectClusterName = async () => {
    const clusterNameList = await getClusterNameList();
    if (clusterNameList && clusterNameList.length > 0) {
      setClusterNameList(
        clusterNameList.map((item) => ({ text: item, value: item }))
      );
      setClusterName(clusterNameList[0]);
    }
    if(oldDepartment.current !== department) {
      const currentTime = new Date().getTime();
      onTimeStampChange(currentTime - ONE_HOUR, currentTime);
    }
  };

  useEffect(() => {
    setSelectClusterName();
  }, [department]);

  useEffect(() => {
    updateCluster({
      startTime,
      endTime,
      clusterName,
    });
    return () => {
      updateCluster({
        startTime,
        endTime,
        clusterName: "",
      });
    };
  }, [clusterName]);

  const renderTitleContent = () => {
    return {
      title: "集群看板",
      content: null,
    };
  };
  return (
    <>
      <div className="table-header">
        <div className="kanban-header-box">
          <RenderTitle {...renderTitleContent()} />
          {/* <span className="kanban-header-box-content">Kibana查看</span> */}
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
