import React, { memo, useState, useEffect, useCallback, useRef } from "react";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import { ReloadOutlined } from "@ant-design/icons";
import _ from "lodash";
import { IndexConfig, Line, SelectRadio } from "../components";
import {
  defaultIndexConfigList,
  allCheckedData,
  getCheckedData,
  indexConfigData,
} from "./project-view-config";
import { formatterTimeYMDHMS, getOption, objFlat } from "../config";
import { TOP_MAP } from "constants/status-map";
import {
  getCheckedList,
  setCheckedList,
  getProjectViewData,
  getAppIdList,
} from "../../../api/gateway-kanban";
import { setIsUpdate } from "actions/gateway-kanban";
import { arrayMoveImmutable } from 'array-move';
import DragGroup from './../../../packages/drag-group/DragGroup';
export const classPrefix = "rf-monitor";

const secondMetricsType = "app";

export const ProjectView = memo(() => {
  const [topNu, setTopNu] = useState(TOP_MAP[0].value);
  const [appId, setAppId] = useState("");
  const [appIdList, setAppIdList] = useState([]);
  const [checkedData, setCheckedData] = useState(getCheckedData([]));
  const [metricsTypes, setMetricsTypes] = useState([]);
  const [appIdMap, setAppIdMap] = useState({});
  const [viewData, setViewData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isGetAppIdEnd, setIsGetAppIdEnd] = useState(false);

  const sortEnd = ({ oldIndex, newIndex }) => {
    const listsNew = arrayMoveImmutable(checkedData['项目性能指标'], oldIndex, newIndex)
    checkedData['项目性能指标'] = listsNew;
    const checkedList = objFlat(checkedData);
    setCheckedList(secondMetricsType, checkedList);
    setMetricsTypes([...listsNew]);
  };

  const dispatch = useDispatch();
  const { startTime, endTime, isMoreDay, isUpdate } = useSelector(
    (state) => ({
      startTime: (state as any).gatewayKanban.startTime,
      endTime: (state as any).gatewayKanban.endTime,
      isMoreDay: (state as any).gatewayKanban.isMoreDay,
      isUpdate: (state as any).gatewayKanban.isUpdate,
    }),
    shallowEqual
  );

  const isFirst = useRef(true);
  const timeDiff = useRef(0);
  const prevTopNu = useRef(topNu);

  const reloadPage = () => {
    dispatch(setIsUpdate(!isUpdate));
  };

  const getAsyncCheckedList = async () => {
    try {
      const checkedList = await getCheckedList(secondMetricsType);
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
    setCheckedList(secondMetricsType, checkedList);
    setCheckedData(changeCheckedData);
    reloadPage();
  };

  const getAsyncIndexNameList = async () => {
    try {
      const appIdList = await getAppIdList();
      const appIdMap = {};
      const newAppIdList = appIdList.map((item) => {
        appIdMap[item.id] = item.name;
        return { value: item.id, name: item.name };
      });
      setAppIdList(newAppIdList);
      setAppIdMap(appIdMap);
    } catch (error) {
      console.log(error);
    } finally {
      setIsGetAppIdEnd(true);
    }
  };

  const getAsyncViewData = useCallback(
    async (metricsTypes) => {
      if (isGetAppIdEnd) {
        const projectViewData = await getProjectViewData(
          metricsTypes,
          startTime,
          endTime,
          topNu,
          appId
        );
        return projectViewData.map((item) => ({
          type: item.type,
          metricsContents: item.metricsContents.map((cell) => ({
            name: appIdMap[cell.name] || cell.name,
            metricsContentCells: cell.metricsContentCells,
          })),
        }));
      }
    },
    [startTime, endTime, topNu, appId, isGetAppIdEnd]
  );

  const getAllAsyncViewData = async (metricsTypes) => {
    try {
      const res = await getAsyncViewData(metricsTypes);
      setViewData(
        res.map((item) => getOption(item, indexConfigData, isMoreDay))
      );
    } catch (error) {
      setViewData([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    setMetricsTypes(objFlat(checkedData));
  }, [checkedData]);

  useEffect(() => {
    getAsyncCheckedList();
    getAsyncIndexNameList();
  }, []);

  useEffect(() => {
    if (
      isFirst.current ||
      timeDiff.current !== endTime - startTime ||
      prevTopNu.current !== topNu
    ) {
      setIsLoading(true);
      timeDiff.current = endTime - startTime;
      isFirst.current = false;
      prevTopNu.current = topNu;
    }
    if (!isGetAppIdEnd || !metricsTypes || metricsTypes.length === 0) {
      return;
    }
    getAllAsyncViewData(metricsTypes);
  }, [metricsTypes, getAsyncViewData, isGetAppIdEnd]);

  const renderTopWhat = () => {
    return (
      <SelectRadio
        topNu={topNu}
        setTopNu={setTopNu}
        content={appId}
        setContent={setAppId}
        contentList={appIdList}
        placeholder="请选择appId"
      />
    );
  };

  const renderConfig = () => {
    return (
      <IndexConfig
        title="项目指标配置"
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
          {renderTopWhat()}
          {renderConfig()}
        </div>
      </div>
      <div className={`${classPrefix}-overview-content-line`}>
        <DragGroup
          dragContainerProps={{
            onSortEnd: sortEnd,
            axis: "xy",
            distance: 100
          }}
          containerProps={{
            grid: 12,
            gutter: [10, 10],
          }}
        >
          {metricsTypes.map((item, index) => (
            <Line
              key={`${item}_${index}`}
              title={indexConfigData[item]?.title()}
              index={`${item}_${index}`}
              option={viewData[index] || {}}
              isLoading={isLoading}
            />
          ))}
        </DragGroup>
      </div>
    </>
  );
});
