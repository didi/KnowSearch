import React, { memo, useState, useEffect, useCallback } from "react";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import { Collapse } from "antd";
import { ReloadOutlined } from "@ant-design/icons";
import _ from "lodash";
import { IndexConfig, SelectRadio } from "../components";
import {
  defaultIndexConfigList,
  allCheckedData,
  getCheckedData,
  indexConfigData,
  indexConfigClassifyList,
  goldConfig,
} from "./index-view-config";
import { formatterTimeYMDHMS, objFlat } from "../config";
import { TOP_MAP } from "constants/status-map";
import { RenderLine } from "../components/render-line";
import {
  getCheckedList,
  setCheckedList,
  getIndexViewData,
  getIndexNameList,
} from "../../../api/cluster-kanban";
import { asyncMicroTasks, resize } from "../../../lib/utils";
import { setIsUpdate } from "actions/cluster-kanban";
import { arrayMoveImmutable } from 'array-move';
import Url from "lib/url-parser";

export const classPrefix = "rf-monitor";
const { Panel } = Collapse;
const INDEX = "index";
export const IndexView = memo(() => {
  const [topNu, setTopNu] = useState(TOP_MAP[0].value);
  const [indexName, setIndexName] = useState(undefined);
  const [indexNameList, setIndexNameList] = useState([]);
  const [checkedData, setCheckedData] = useState(getCheckedData([]));
  const dispatch = useDispatch();

  const sortEnd = (item, { oldIndex, newIndex }) => {
    const listsNew = arrayMoveImmutable(checkedData[item], oldIndex, newIndex)
    checkedData[item] = listsNew;
    const checkedList = objFlat(checkedData);
    setCheckedList(INDEX, checkedList);
    setCheckedData({ ...checkedData });
  };

  const { clusterName, startTime, endTime, isMoreDay, isUpdate } = useSelector(
    (state) => ({
      clusterName: (state as any).clusterKanban.clusterName,
      startTime: (state as any).clusterKanban.startTime,
      endTime: (state as any).clusterKanban.endTime,
      isMoreDay: (state as any).clusterKanban.isMoreDay,
      isUpdate: (state as any).clusterKanban.isUpdate,
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
      return await getIndexViewData(
        metricsTypes,
        clusterName,
        startTime,
        endTime,
        topNu,
        indexName
      );
    },
    [clusterName, startTime, endTime, topNu, indexName, isUpdate]
  );

  const getAsyncIndexNameList = async () => {
    if (clusterName) {
      try {
        const indexNameList = await getIndexNameList(clusterName);
        setIndexName(Url().search?.cluster === clusterName ? Url().search?.index : undefined);
        setIndexNameList(indexNameList);
      } catch (error) {
        console.log(error);
      }
    }
  };

  useEffect(() => {
    getAsyncCheckedList();
  }, []);

  useEffect(() => {
    setIndexName(Url().search?.index)
  }, [Url().search?.index])

  useEffect(() => {
    getAsyncIndexNameList();
  }, [clusterName]);

  const renderTopWhat = () => {
    return (
      <SelectRadio
        topNu={topNu}
        setTopNu={setTopNu}
        content={indexName}
        setContent={setIndexName}
        contentList={indexNameList}
        placeholder="请选择索引"
        style={{ width: 400 }}
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
        goldConfig={goldConfig}
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
                  <div
                    className={`${classPrefix}-overview-content-line  content-margin-top-20`}
                  >
                    {checkedData[item] && checkedData[item].length ? <RenderLine
                      metricsTypes={checkedData[item]}
                      key={item + index + topNu + clusterName}
                      configData={indexConfigData}
                      isMoreDay={isMoreDay}
                      getAsyncViewData={getAsyncViewData}
                      startTime={startTime}
                      endTime={endTime}
                      sortEnd={sortEnd}
                      item={item}
                    /> : ""
                    }
                  </div>
                </Panel>
              </Collapse>
            );
          }
        })}
      </div>
    </>
  );
});
