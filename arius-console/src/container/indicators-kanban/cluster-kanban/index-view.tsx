import React, { memo, useState, useEffect, useCallback, useRef, useMemo } from "react";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import { Collapse } from "antd";
import _ from "lodash";
import { IndexConfig, SelectRadio } from "../components";
import { defaultIndexConfigList, allCheckedData, getCheckedData, indexConfigData, indexConfigClassifyList } from "./index-view-config";
import { objFlat } from "../config";
import { TOP_MAP, TOP_TIME_RANGE, TOP_TYPE } from "constants/status-map";
import { RenderLine } from "../components/render-line";
import {
  getCheckedList,
  setCheckedList,
  getIndexViewData,
  getPhyIndexNameList,
  getLogicIndexNameList,
  getDictionary,
} from "../../../api/cluster-kanban";
import { asyncMicroTasks, isSuperApp, resize } from "../../../lib/utils";
import { setIsUpdate } from "actions/cluster-kanban";
import { arrayMoveImmutable } from "array-move";
import Url from "lib/url-parser";
import { OperationPanel } from "../components/operation-panel";

export const classPrefix = "monitor";
const { Panel } = Collapse;
const INDEX = "index";
export const IndexView = memo(() => {
  const [indexNameList, setIndexNameList] = useState([]);
  const [update, setUpdate] = useState(false);
  const [checkedData, setCheckedData] = useState(getCheckedData([]));
  const [dictionary, setDictionary] = useState({});

  // 用于判断是否第一次进入页面
  const [flag, setFlag] = useState(true);

  const dispatch = useDispatch();

  const selectRadioValue = useRef({
    topNum: TOP_MAP[0].value,
    topTimeStep: TOP_TIME_RANGE[0].value,
    topMethod: TOP_TYPE[0].value,
    content: Url().search?.index ? [Url().search?.index] : [],
  });

  const sortEnd = (item, { oldIndex, newIndex }) => {
    const listsNew = arrayMoveImmutable(checkedData[item], oldIndex, newIndex);
    checkedData[item] = listsNew;
    const checkedList = objFlat(checkedData);
    setCheckedList(INDEX, checkedList);
    setCheckedData({ ...checkedData });
  };

  const { clusterName, startTime, endTime, isMoreDay, isUpdate, timeRadioKey } = useSelector(
    (state) => ({
      clusterName: (state as any).clusterKanban.clusterName,
      startTime: (state as any).clusterKanban.startTime,
      endTime: (state as any).clusterKanban.endTime,
      isMoreDay: (state as any).clusterKanban.isMoreDay,
      isUpdate: (state as any).clusterKanban.isUpdate,
      timeRadioKey: (state as any).clusterKanban.timeRadioKey,
    }),
    shallowEqual
  );

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
      console.log("cluster-kanban node-view 获取配置下项失败", error);
    }
  };

  const setIndexConfigCheckedData = (changeCheckedData) => {
    const checkedList = objFlat(changeCheckedData);
    setCheckedData(changeCheckedData);
    setCheckedList(INDEX, checkedList);
    reloadPage();
  };
  const getAsyncViewData = useCallback(
    async (metricsTypes) => {
      // 从dashboard跳转至索引视图，selectRadioValue.current.content未赋值时不调接口
      // if (flag && !selectRadioValue.current.content?.length && Url().search?.index) return;
      // setFlag(false);
      return await getIndexViewData(
        metricsTypes,
        clusterName,
        startTime,
        endTime,
        selectRadioValue.current.topNum,
        selectRadioValue.current.content,
        selectRadioValue.current.topMethod,
        selectRadioValue.current.topTimeStep
      );
    },
    [clusterName, startTime, endTime, timeRadioKey, selectRadioValue.current.content]
  );

  const getAsyncIndexNameList = async () => {
    if (clusterName) {
      try {
        selectRadioValue.current.content = [];
        setUpdate(!update);
        const superApp = isSuperApp();
        const indexNameList = await (superApp ? getPhyIndexNameList(clusterName) : getLogicIndexNameList(clusterName));
        selectRadioValue.current.content = Url().search?.cluster === clusterName && Url().search?.index ? [Url().search?.index] : [];
        setIndexNameList(indexNameList);
      } catch (error) {
        console.log(error);
      }
    }
  };

  const _getDictionary = async () => {
    let params = {
      model: "Index",
    };
    let res = await getDictionary(params);
    let data = {} as any;
    (res || []).forEach((item) => {
      if (item?.metricType) {
        data[item?.metricType] = item;
      }
    });
    setDictionary(data);
  };

  useEffect(() => {
    getAsyncCheckedList();
    _getDictionary();
  }, []);

  useEffect(() => {
    getAsyncIndexNameList();
  }, [clusterName]);

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
        content={selectRadioValue.current.content || []}
        contentList={indexNameList}
        placeholder="请选择索引"
        style={{ width: '100%' }}
        type="node"
        allowClear={true}
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
        needShortcut={true}
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

  const RenderContent = useMemo(() => {
    return (
      <div className={`${classPrefix}-overview-content`}>
        {indexConfigClassifyList.map((item, index) => {
          if (checkedData[item] && checkedData[item].length > 0) {
            return (
              <Collapse
                defaultActiveKey={[index]}
                onChange={() => {
                  asyncMicroTasks(resize);
                }}
                style={{ marginTop: 20 }}
                key={item + index}
              >
                <Panel header={item} key={index}>
                  <div className={`${classPrefix}-overview-content-line  content-margin-top-20`}>
                    {checkedData[item] && checkedData[item].length ? (
                      <RenderLine
                        metricsTypes={checkedData[item]}
                        key={item + index + selectRadioValue.current.topNum + clusterName}
                        configData={indexConfigData}
                        isMoreDay={isMoreDay}
                        getAsyncViewData={getAsyncViewData}
                        startTime={startTime}
                        endTime={endTime}
                        sortEnd={sortEnd}
                        item={item}
                        dictionary={dictionary}
                        content={selectRadioValue.current.content}
                      />
                    ) : (
                      ""
                    )}
                  </div>
                </Panel>
              </Collapse>
            );
          }
        })}
      </div>
    );
  }, [startTime, endTime, checkedData, timeRadioKey, indexConfigClassifyList, update, dictionary]);

  return (
    <>
      <OperationPanel classPrefix={classPrefix} reloadPage={reloadPage} endTime={endTime} renderFilter={renderFilter} />
      {RenderContent}
    </>
  );
});
