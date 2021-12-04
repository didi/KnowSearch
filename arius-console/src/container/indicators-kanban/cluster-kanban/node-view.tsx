import { ReloadOutlined } from "@ant-design/icons";
import { TOP_MAP } from "constants/status-map";
import React, { memo, useState, useEffect, useCallback, useRef } from "react";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import { Collapse } from "antd";
import _ from "lodash";

import { RenderLine, IndexConfig, SelectRadio } from "../components";

import {
  getCheckedList,
  setCheckedList,
  getNodeViewData,
  getNodeIpList,
} from "../../../api/cluster-kanban";

import { formatterTimeYMDHMS, objFlat } from "../config";
import {
  indexConfigClassifyList,
  defaultIndexConfigList,
  allCheckedData,
  getCheckedData,
  indexConfigData,
} from "./node-view-config";

import "../style/index";
import { asyncMicroTasks, resize } from "../../../lib/utils";
import { setIsUpdate } from "actions/cluster-kanban";

const { Panel } = Collapse;

export const classPrefix = "rf-monitor";

const NODE = "node";

export const NodeView = memo(() => {
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
  const [topNu, setTopNu] = useState(TOP_MAP[0].value);
  const [nodeIp, setNodeIp] = useState("");
  const [nodeIpList, setNodeIpList] = useState([]);
  const [checkedData, setCheckedData] = useState(getCheckedData([]));

  const dispatch = useDispatch();

  const reloadPage = () => {
    dispatch(setIsUpdate(!isUpdate));
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
      console.log("cluster-kanban node-view 获取配置下项失败", error);
    }
  };

  const getAsyncNodeViewIpList = async () => {
    if (clusterName) {
      try {
        const ipList = await getNodeIpList(clusterName);
        setNodeIpList(ipList);
      } catch (error) {
        console.error(error);
      }
    }
  };

  const setIndexConfigCheckedData = (changeCheckedData) => {
    const checkedList = objFlat(changeCheckedData);
    setCheckedList(NODE, checkedList);
    setCheckedData(changeCheckedData);
    reloadPage();
  };

  const getAsyncNodeViewData = useCallback(
    async (metricsTypes) => {
      return await getNodeViewData(
        metricsTypes,
        clusterName,
        startTime,
        endTime,
        topNu,
        nodeIp
      );
    },
    [clusterName, startTime, endTime, topNu, nodeIp]
  );

  useEffect(() => {
    getAsyncCheckedList();
  }, []);

  useEffect(() => {
    getAsyncNodeViewIpList();
  }, [clusterName]);

  const renderTopWhat = () => {
    return (
      <SelectRadio
        topNu={topNu}
        setTopNu={setTopNu}
        content={nodeIp}
        setContent={setNodeIp}
        contentList={nodeIpList}
        placeholder="请选择节点名称"
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
                    className={`${classPrefix}-overview-content-line content-margin-top-20`}
                  >
                    {checkedData[item] && checkedData[item].length ? <RenderLine
                        metricsTypes={checkedData[item]}
                        key={item + index + topNu + clusterName}
                        configData={indexConfigData}
                        isMoreDay={isMoreDay}
                        getAsyncViewData={getAsyncNodeViewData}
                        startTime={startTime}
                        endTime={endTime}
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
