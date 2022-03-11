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
} from "./index-view-config";
import { formatterTimeYMDHMS, getOption, objFlat } from "../config";
import { TOP_MAP } from "constants/status-map";
import { RenderLine } from "../components/render-line";
import {
  getCheckedList,
  setCheckedList,
  getIndexViewData,
  getIndexNameList,
} from "../../../api/gateway-kanban";
import { setIsUpdate } from "actions/gateway-kanban";
import { arrayMoveImmutable } from 'array-move';
import DragGroup from './../../../packages/drag-group/DragGroup';

export const classPrefix = "rf-monitor";

const INDEX = "index";

export const IndexView = memo(() => {
  const [topNu, setTopNu] = useState(TOP_MAP[0].value);
  const [indexName, setIndexName] = useState("");
  const [indexNameList, setIndexNameList] = useState([]);
  const [checkedData, setCheckedData] = useState(getCheckedData([]));
  const [metricsTypes, setMetricsTypes] = useState([]);
  const [viewData, setViewData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
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
  const isFirst = useRef(true);
  const timeDiff = useRef(0);
  const prevTopNu = useRef(topNu);

  const sortEnd = ({ oldIndex, newIndex }) => {
    const listsNew = arrayMoveImmutable(checkedData['索引性能指标'], oldIndex, newIndex)
    checkedData['索引性能指标'] = listsNew;
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
        topNu,
        indexName
      );
    },
    [startTime, endTime, topNu, indexName]
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

  const getAsyncIndexNameList = async () => {
    try {
      const indexNameList = await getIndexNameList();
      setIndexNameList(indexNameList);
    } catch (error) {
      console.log(error);
    }
  };

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
    if (!metricsTypes || metricsTypes.length === 0) {
      return;
    }
    getAllAsyncViewData(metricsTypes);
  }, [metricsTypes, getAsyncViewData]);

  const renderTopWhat = () => {
    return (
      <SelectRadio
        topNu={topNu}
        setTopNu={setTopNu}
        content={indexName}
        setContent={setIndexName}
        contentList={indexNameList}
        placeholder="请选择索引"
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
              key={`${item}`}
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
