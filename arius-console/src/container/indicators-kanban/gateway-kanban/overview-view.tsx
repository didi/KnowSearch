import { ReloadOutlined } from "@ant-design/icons";
import { Skeleton } from "antd";
import React, { memo, useState, useEffect, useCallback, useRef } from "react";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import _ from "lodash";
import { IndexConfig, Line } from "../components";
import { formatterTimeYMDHMS, objFlat } from "./config";
import {
  defaultIndexConfigList,
  allCheckedData,
  getCheckedData,
  indexConfigData,
  getOverviewOption,
} from "./overview-view-config";
import {
  getCheckedList,
  setCheckedList,
  getOverviewData,
} from "../../../api/gateway-kanban";

import "../style/index";
import { setIsUpdate } from "actions/gateway-kanban";

const OVERVIEW = "overview";

export const classPrefix = "rf-monitor";

export const OverviewView = memo(() => {
  const { startTime, endTime, isMoreDay, isUpdate } = useSelector(
    (state) => ({
      startTime: (state as any).gatewayKanban.startTime,
      endTime: (state as any).gatewayKanban.endTime,
      isMoreDay: (state as any).gatewayKanban.isMoreDay,
      isUpdate: (state as any).gatewayKanban.isUpdate,
    }),
    shallowEqual
  );
  const dispatch = useDispatch();

  const reloadPage = () => {
    dispatch(setIsUpdate(!isUpdate));
  };

  const [checkedData, setCheckedData] = useState(getCheckedData([]));
  const [metricsTypes, setMetricsTypes] = useState([]);
  const [viewData, setViewData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const isFirst = useRef(true);
  const timeDiff = useRef(0);

  const getAsyncOverviewData = useCallback(
    async (metricsTypes) => {
      return await getOverviewData(metricsTypes, startTime, endTime);
    },
    [startTime, endTime]
  );

  const getAsyncCheckedList = async () => {
    try {
      const checkedList = await getCheckedList(OVERVIEW);
      if (!checkedList || checkedList.length === 0) {
        setCheckedData(allCheckedData);
      } else {
        setCheckedData(getCheckedData(checkedList));
      }
    } catch (error) {
      setCheckedData(allCheckedData);
      console.log(error);
    }
  };

  const setIndexConfigCheckedData = (changeCheckedData) => {
    const checkedList = objFlat(changeCheckedData);
    setCheckedList(OVERVIEW, checkedList);
    setCheckedData(changeCheckedData);
    reloadPage();
  };

  const getAllAsyncOverviewData = async (metricsTypes) => {
    try {
      const res = await getAsyncOverviewData(metricsTypes);
      setViewData(
        res.map((item) => getOverviewOption(item, indexConfigData, isMoreDay))
      );
    } catch (error) {
      setViewData([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    getAsyncCheckedList();
  }, []);

  useEffect(() => {
    setMetricsTypes(objFlat(checkedData));
  }, [checkedData]);

  useEffect(() => {
    if (isFirst.current || timeDiff.current !== endTime - startTime) {
      setIsLoading(true);
      timeDiff.current = endTime - startTime;
      isFirst.current = false;
    }
    if (!metricsTypes || metricsTypes.length === 0) {
      return;
    }
    getAllAsyncOverviewData(metricsTypes);
  }, [metricsTypes, getAsyncOverviewData]);

  const renderConfig = () => {
    return (
      <IndexConfig
        title="总览指标配置"
        optionList={defaultIndexConfigList}
        checkedData={checkedData}
        setCheckedData={setIndexConfigCheckedData}
      />
    );
  };

  return (
    <>
      <div className={`${classPrefix}-overview-search`}>
        <div className={`${classPrefix}-overview-search-reload`}>
          <ReloadOutlined className="reload" onClick={reloadPage} />
          <span>上次刷新时间：{formatterTimeYMDHMS(endTime)}</span>
        </div>
        <div className={`${classPrefix}-overview-search-filter`}>
          {renderConfig()}
        </div>
      </div>
      <div className={`${classPrefix}-overview-content-line`}>
        {metricsTypes.map((item, index) => (
          <Line
            key={`${item}_${index}`}
            title={indexConfigData[item]?.title()}
            index={`${item}_${index}`}
            option={viewData[index] || {}}
            isLoading={isLoading}
          />
        ))}
      </div>
    </>
  );
});
