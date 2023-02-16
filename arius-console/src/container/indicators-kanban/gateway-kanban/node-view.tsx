import { TOP_MAP, TOP_TIME_RANGE, TOP_TYPE } from "constants/status-map";
import React, { memo, useState, useEffect, useCallback, useRef } from "react";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import _ from "lodash";
import { defaultIndexConfigList, allCheckedData, getCheckedData } from "./node-view-config";
import { objFlat, getOption } from "../config";
import { getRenderToolTip } from "./config";
import { indexConfigData } from "./node-view-config";
import { IndexConfig, SelectRadio, Line } from "../components";
import { getCheckedList, setCheckedList, getNodeViewData, getNodeIpList } from "../../../api/gateway-kanban";
import "../style/index";
import { setIsUpdate } from "actions/gateway-kanban";
import { arrayMoveImmutable } from "array-move";
import DragGroup from "../../../d1-packages/drag-group/DragGroup";
import { OperationPanel } from "../components/operation-panel";
import { copyString } from "lib/utils";

export const classPrefix = "monitor";

const NODE = "node";

export const NodeView = memo(() => {
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
  const dispatch = useDispatch();

  const reloadPage = () => {
    dispatch(setIsUpdate(!isUpdate));
  };

  const [nodeIpList, setNodeIpList] = useState([]);
  const [checkedData, setCheckedData] = useState(getCheckedData([]));
  const [metricsTypes, setMetricsTypes] = useState([]);
  const [viewData, setViewData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const isFirst = useRef(true);
  const timeDiff = useRef(0);
  const prevTopNu = useRef(TOP_MAP[0].value);
  const selectRadioValue = useRef({
    topNum: TOP_MAP[0].value,
    topTimeStep: TOP_TIME_RANGE[0].value,
    topMethod: TOP_TYPE[0].value,
    content: [],
  });

  const sortEnd = ({ oldIndex, newIndex }) => {
    const listsNew = arrayMoveImmutable(checkedData["节点性能指标"], oldIndex, newIndex);
    checkedData["节点性能指标"] = listsNew;
    const checkedList = objFlat(checkedData);
    setCheckedList(NODE, checkedList);
    setMetricsTypes([...listsNew]);
  };

  const getAsyncCheckedList = async () => {
    try {
      const checkedList = await getCheckedList(NODE);
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

  const getAsyncNodeViewIpList = async () => {
    try {
      const ipList = await getNodeIpList();
      setNodeIpList(ipList);
    } catch (error) {
      console.log(error);
    }
  };

  const setIndexConfigCheckedData = (changeCheckedData) => {
    const checkedList = objFlat(changeCheckedData);
    setCheckedList(NODE, checkedList);
    setCheckedData(changeCheckedData);
    reloadPage();
  };

  const getAsyncViewData = useCallback(
    async (metricsTypes) => {
      return await getNodeViewData(
        metricsTypes,
        startTime,
        endTime,
        selectRadioValue.current.topNum,
        selectRadioValue.current.content,
        selectRadioValue.current.topMethod,
        selectRadioValue.current.topTimeStep
      );
    },
    [startTime, endTime, timeRadioKey]
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
    getAsyncCheckedList();
  }, []);

  useEffect(() => {
    getAsyncNodeViewIpList();
  }, []);

  useEffect(() => {
    setMetricsTypes(objFlat(checkedData));
  }, [checkedData]);

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
        content={selectRadioValue.current.content}
        contentList={nodeIpList}
        placeholder="请选择节点名称"
        type="node"
      />
    );
  };

  const renderConfig = () => {
    return (
      <IndexConfig
        title="节点指标配置"
        optionList={defaultIndexConfigList}
        checkedData={checkedData}
        setCheckedData={setIndexConfigCheckedData}
      />
    );
  };

  const renderFilter = () => {
    return (
      <>
        {renderTopWhat()}
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
