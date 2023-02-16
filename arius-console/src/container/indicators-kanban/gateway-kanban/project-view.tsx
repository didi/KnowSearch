import React, { memo, useState, useEffect, useCallback, useRef } from "react";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import _ from "lodash";
import { IndexConfig, Line, SelectRadio } from "../components";
import { defaultIndexConfigList, allCheckedData, getCheckedData, indexConfigData } from "./project-view-config";
import { getOption, objFlat } from "../config";
import { getRenderToolTip } from "./config";
import { TOP_MAP, TOP_TIME_RANGE, TOP_TYPE } from "constants/status-map";
import { getCheckedList, setCheckedList, getProjectViewData, getProjectIdList } from "../../../api/gateway-kanban";
import { setIsUpdate } from "actions/gateway-kanban";
import { arrayMoveImmutable } from "array-move";
import DragGroup from "../../../d1-packages/drag-group/DragGroup";
import { OperationPanel } from "../components/operation-panel";
import { copyString } from "lib/utils";

export const classPrefix = "monitor";

const secondUserConfigType = "app";

export const ProjectView = memo(() => {
  const [projectIdList, setProjectIdList] = useState([]);
  const [checkedData, setCheckedData] = useState(getCheckedData([]));
  const [metricsTypes, setMetricsTypes] = useState([]);
  const [projectIdMap, setProjectIdMap] = useState({});
  const [viewData, setViewData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isGetProjectIdEnd, setIsGetProjectIdEnd] = useState(false);

  const sortEnd = ({ oldIndex, newIndex }) => {
    const listsNew = arrayMoveImmutable(checkedData["应用性能指标"], oldIndex, newIndex);
    checkedData["应用性能指标"] = listsNew;
    const checkedList = objFlat(checkedData);
    setCheckedList(secondUserConfigType, checkedList);
    setMetricsTypes([...listsNew]);
  };

  const dispatch = useDispatch();
  const { startTime, endTime, isMoreDay, isUpdate, timeRadioKey } = useSelector(
    (state) => ({
      startTime: (state as any).gatewayKanban.startTime,
      endTime: (state as any).gatewayKanban.endTime,
      isMoreDay: (state as any).gatewayKanban.isMoreDay,
      isUpdate: (state as any).gatewayKanban.isUpdate,
      timeRadioKey: (state as any).gatewayKanban.timeRadioKey,
    }),
    shallowEqual
  );

  const selectRadioValue = useRef({
    topNum: TOP_MAP[0].value,
    topTimeStep: TOP_TIME_RANGE[0].value,
    topMethod: TOP_TYPE[0].value,
    content: undefined,
  });

  const isFirst = useRef(true);
  const timeDiff = useRef(0);
  const prevTopNu = useRef(TOP_MAP[0].value);

  const reloadPage = () => {
    dispatch(setIsUpdate(!isUpdate));
  };

  const getAsyncCheckedList = async () => {
    try {
      const checkedList = await getCheckedList(secondUserConfigType);
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
    setCheckedList(secondUserConfigType, checkedList);
    setCheckedData(changeCheckedData);
    reloadPage();
  };

  const getAsyncIndexNameList = async () => {
    try {
      const projectIdList = await getProjectIdList();
      const projectIdMap = {};
      const newProjectIdList = projectIdList.map((item) => {
        projectIdMap[item.id] = item.projectName;
        return { value: item.id, name: item.projectName };
      });
      // if (newProjectIdList.length) {
      //   selectRadioValue.current.content = newProjectIdList[0].value;
      //   selectRadioValue.current.topNum = 0;
      // }
      setProjectIdList(newProjectIdList);
      setProjectIdMap(projectIdMap);
    } catch (error) {
      console.log(error);
    } finally {
      setIsGetProjectIdEnd(true);
    }
  };

  const getAsyncViewData = useCallback(
    async (metricsTypes) => {
      if (isGetProjectIdEnd) {
        const projectViewData = await getProjectViewData(
          metricsTypes,
          startTime,
          endTime,
          selectRadioValue.current.topNum,
          selectRadioValue.current.content,
          selectRadioValue.current.topMethod,
          selectRadioValue.current.topTimeStep
        );
        return projectViewData.map((item) => ({
          type: item.type,
          metricsContents: item.metricsContents.map((cell) => ({
            name: projectIdMap[cell.name] || cell.name,
            metricsContentCells: cell.metricsContentCells,
          })),
        }));
      }
    },
    [isGetProjectIdEnd, startTime, endTime, timeRadioKey]
  );

  const getAllAsyncViewData = async (metricsTypes) => {
    try {
      setIsLoading(true);
      const res = await getAsyncViewData(metricsTypes);
      setViewData(
        res.map((item) =>
          getOption({
            metrics: item,
            configData: indexConfigData,
            isMoreDay,
            isShowTooltipModal: true,
          })
        )
      );
    } catch (error) {
      setViewData([]);
    } finally {
      setIsLoading(false);
    }
  };

  const showTooltipModal = (md5, metricsType) => {
    copyString(md5);
  };

  useEffect(() => {
    window["showTooltipModal"] = (md5, metricsType) => {
      showTooltipModal(md5, metricsType);
    };
  }, []);

  useEffect(() => {
    setMetricsTypes(objFlat(checkedData));
  }, [checkedData]);

  useEffect(() => {
    getAsyncCheckedList();
    getAsyncIndexNameList();
  }, []);

  useEffect(() => {
    if (isFirst.current || timeDiff.current !== endTime - startTime || prevTopNu.current !== selectRadioValue.current.topNum) {
      setIsLoading(true);
      timeDiff.current = endTime - startTime;
      isFirst.current = false;
      prevTopNu.current = selectRadioValue.current.topNum;
    }
    if (!isGetProjectIdEnd || !metricsTypes || metricsTypes.length === 0) {
      return;
    }
    getAllAsyncViewData(metricsTypes);
  }, [metricsTypes, getAsyncViewData, isGetProjectIdEnd]);

  const onSelectRadioChange = (values, needReload) => {
    selectRadioValue.current = values;
    if (needReload) {
      reloadPage();
    }
  };

  const renderTopWhat = () => {
    return (
      <SelectRadio
        onValueChange={onSelectRadioChange}
        topNu={selectRadioValue.current.topNum}
        content={selectRadioValue.current.content}
        contentList={projectIdList}
        placeholder="请选择应用"
      />
    );
  };

  const renderConfig = () => {
    return (
      <IndexConfig
        title="应用指标配置"
        optionList={defaultIndexConfigList}
        checkedData={checkedData}
        setCheckedData={setIndexConfigCheckedData}
      />
    );
  };

  const renderFilter = () => {
    return (
      <>
        {isGetProjectIdEnd ? renderTopWhat() : null}
        {renderConfig()}
      </>
    );
  };

  return (
    <>
      <OperationPanel classPrefix={classPrefix} reloadPage={reloadPage} endTime={endTime} renderFilter={renderFilter} />
      <div className={`${classPrefix}-overview-content-line`}>
        <DragGroup
          dragContainerProps={{
            onSortEnd: sortEnd,
            axis: "xy",
            distance: 100,
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
              tooltip={getRenderToolTip(indexConfigData[item])}
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
