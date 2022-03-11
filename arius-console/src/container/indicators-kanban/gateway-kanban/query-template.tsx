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
} from "./query-template-config";
import { formatterTimeYMDHMS, getOption, objFlat } from "../config";
import { TOP_MAP } from "constants/status-map";
import {
  getCheckedList,
  setCheckedList,
  getQueryTemplateData,
  getDslMd5List,
  getAppIdList,
} from "../../../api/gateway-kanban";
import { setIsUpdate } from "actions/gateway-kanban";
import { arrayMoveImmutable } from 'array-move';
import DragGroup from './../../../packages/drag-group/DragGroup';
import * as actions from "../../../actions";

export const classPrefix = "rf-monitor";

const secondMetricsType = "dsl";

export const QueryTemplate = memo(() => {
  const [topNu, setTopNu] = useState(TOP_MAP[0].value);
  const [dslMd5, setDslMd5] = useState("");
  const [dslMd5List, setDslMd5List] = useState([]);
  const [checkedData, setCheckedData] = useState(getCheckedData([]));
  const [metricsTypes, setMetricsTypes] = useState([]);
  const [viewData, setViewData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  const sortEnd = ({ oldIndex, newIndex }) => {
    const listsNew = arrayMoveImmutable(checkedData['查询模板性能配置'], oldIndex, newIndex)
    checkedData['查询模板性能配置'] = listsNew;
    const checkedList = objFlat(checkedData);
    setCheckedList(secondMetricsType, checkedList);
    setMetricsTypes([...listsNew]);
  };

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

  const showTooltipModal = (md5, metricsType) => {
    dispatch(actions.setModalId("chartModal", {
      md5,
      metricsType
    }));
  }

  useEffect(() => {
    window['showTooltipModal'] = (md5, metricsType) => {
      showTooltipModal(md5, metricsType);
    };
  }, []);

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

  const getAsyncViewData = useCallback(
    async (metricsTypes) => {
      return await getQueryTemplateData(
        metricsTypes,
        startTime,
        endTime,
        topNu,
        dslMd5
      );
    },
    [startTime, endTime, topNu, dslMd5]
  );

  const getAsyncDslMd5List = async () => {
    try {
      const dslMd5List = await getDslMd5List(startTime, endTime);

      setDslMd5List(dslMd5List);
    } catch (error) {
      console.log(error);
    }
  };

  const getAllAsyncViewData = async (metricsTypes) => {
    try {
      const res = await getAsyncViewData(metricsTypes);
      setViewData(
        res.map((item) => getOption(item, indexConfigData, isMoreDay, false, true))
      );
    } catch (error) {
      setViewData([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    getAsyncCheckedList();
    getAsyncDslMd5List();
  }, []);

  useEffect(() => {
    setMetricsTypes(objFlat(checkedData));
  }, [checkedData]);

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
        content={dslMd5}
        setContent={setDslMd5}
        contentList={dslMd5List}
        placeholder="请选择dslMd5"
      />
    );
  };

  const renderConfig = () => {
    return (
      <IndexConfig
        title="查询模版指标配置"
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
