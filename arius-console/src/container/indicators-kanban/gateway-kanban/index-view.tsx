import React, { memo, useState, useEffect, useCallback, useRef } from "react";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import _ from "lodash";
import { IndexConfig, Line, SelectRadio } from "../components";
import { defaultIndexConfigList, allCheckedData, getCheckedData, indexConfigData } from "./index-view-config";
import { getOption, objFlat } from "../config";
import { getRenderToolTip } from "./config";
import { TOP_MAP, TOP_TIME_RANGE, TOP_TYPE } from "constants/status-map";
import { getCheckedList, setCheckedList, getIndexViewData, getIndexNameList } from "../../../api/gateway-kanban";
import { setIsUpdate } from "actions/gateway-kanban";
import { arrayMoveImmutable } from "array-move";
import DragGroup from "../../../d1-packages/DragGroup";
import { OperationPanel } from "../components/operation-panel";
import { copyString } from "lib/utils";

export const classPrefix = "monitor";

const INDEX = "index";

export const IndexView = memo(() => {
  const [indexNameList, setIndexNameList] = useState([]);
  const [checkedData, setCheckedData] = useState(getCheckedData([]));
  const [metricsTypes, setMetricsTypes] = useState([]);
  const [viewData, setViewData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [indexLoading, setIndexLoading] = useState(false);
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

  const dispatch = useDispatch();
  const isFirst = useRef(true);
  const timeDiff = useRef(0);
  const prevTopNu = useRef(TOP_MAP[0].value);

  const sortEnd = ({ oldIndex, newIndex }) => {
    const listsNew = arrayMoveImmutable(checkedData["索引性能指标"], oldIndex, newIndex);
    checkedData["索引性能指标"] = listsNew;
    const checkedList = objFlat(checkedData);
    setCheckedList(INDEX, checkedList);
    setMetricsTypes([...listsNew]);
  };

  const reloadPage = () => {
    dispatch(setIsUpdate(!isUpdate));
  };

  const getAsyncCheckedList = async () => {
    try {
      const checkedList = await getCheckedList(INDEX);
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
    setCheckedList(INDEX, checkedList);
    setCheckedData(changeCheckedData);
    reloadPage();
  };

  const getAsyncViewData = useCallback(
    async (metricsTypes) => {
      return await getIndexViewData(
        metricsTypes,
        startTime,
        endTime,
        selectRadioValue.current.topNum,
        selectRadioValue.current.content,
        selectRadioValue.current.topMethod,
        selectRadioValue.current.topTimeStep
      );
    },
    [startTime, endTime, timeRadioKey, indexLoading]
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

  const getAsyncIndexNameList = async () => {
    setIndexLoading(true);
    try {
      const indexNameList = await getIndexNameList();
      // if (indexNameList.length) {
      //   selectRadioValue.current.content = indexNameList[0];
      //   selectRadioValue.current.topNum = 0;
      // }
      await setIndexLoading(false);
      setIndexNameList(indexNameList);
    } catch (error) {
      console.log(error);
      setIndexLoading(false);
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
    getAsyncCheckedList();
  }, []);

  useEffect(() => {
    setMetricsTypes(objFlat(checkedData));
  }, [checkedData]);

  useEffect(() => {
    getAsyncIndexNameList();
  }, []);

  useEffect(() => {
    if (isFirst.current || timeDiff.current !== endTime - startTime || prevTopNu.current !== selectRadioValue.current.topNum) {
      setIsLoading(true);
      timeDiff.current = endTime - startTime;
      isFirst.current = false;
      prevTopNu.current = selectRadioValue.current.topNum;
    }
    if (!metricsTypes || metricsTypes.length === 0) {
      return;
    }
    if (indexLoading) return;

    getAllAsyncViewData(metricsTypes);
  }, [metricsTypes, getAsyncViewData]);

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
        contentList={indexNameList}
        placeholder="请选择索引"
        style={{ width: '100%' }}
      />
    );
  };

  const renderConfig = () => {
    return (
      <IndexConfig
        title="索引指标配置"
        optionList={defaultIndexConfigList}
        checkedData={checkedData}
        setCheckedData={setIndexConfigCheckedData}
      />
    );
  };

  const renderFilter = () => {
    return (
      <>
        {indexLoading ? null : renderTopWhat()}
        {renderConfig()}
      </>
    );
  };
  const el = document.getElementsByClassName("monitor-overview-content-line-container")?.[0];

  return (
    <>
      <OperationPanel classPrefix={classPrefix} reloadPage={reloadPage} endTime={endTime} renderFilter={renderFilter} />
      <div className={`${classPrefix}-overview-content-line`}>
        <DragGroup
          sortableContainerProps={{
            onSortEnd: sortEnd,
            axis: "xy",
            distance: el ? el.clientWidth - 80 : 150,
          }}
          gridProps={{
            grid: 12,
            gutter: [10, 10],
          }}
        >
          {metricsTypes.map((item, index) => (
            <Line
              key={`${item}`}
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
