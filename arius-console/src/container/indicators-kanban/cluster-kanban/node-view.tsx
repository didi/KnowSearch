import { TOP_MAP, TOP_TIME_RANGE, TOP_TYPE } from "constants/status-map";
import React, { memo, useState, useEffect, useCallback, useRef, useMemo } from "react";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import { Collapse, TreeSelect } from "antd";
import _ from "lodash";
import { RenderLine, IndexConfig, SelectRadio } from "../components";
import { getCheckedList, setCheckedList, getNodeViewData, getNodeInfoList, getDictionary } from "../../../api/cluster-kanban";
import { objFlat } from "../config";
import { indexConfigClassifyList, defaultIndexConfigList, allCheckedData, getCheckedData, indexConfigData } from "./node-view-config";
import { asyncMicroTasks, resize, uuid } from "../../../lib/utils";
import { setIsUpdate } from "actions/cluster-kanban";
import * as actions from "../../../actions";
import { arrayMoveImmutable } from "array-move";
import Url from "lib/url-parser";
import { OperationPanel } from "../components/operation-panel";
import "../style/index";

const { Panel } = Collapse;
const { SHOW_CHILD } = TreeSelect;

export const classPrefix = "monitor";

const NODE = "node";

const NODE_TYPE = {
  1: "data",
  2: "client",
  3: "master",
  4: "tribe",
};

export const NodeView = memo(() => {
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
  const [nodeIpList, setNodeIpList] = useState([]);
  const [update, setUpdate] = useState(false);
  const [checkedData, setCheckedData] = useState(getCheckedData([]));
  // 用于判断是否第一次进入页面
  const [flag, setFlag] = useState(true);
  const [dictionary, setDictionary] = useState({});

  const selectRadioValue = useRef({
    topNum: TOP_MAP[0].value,
    topTimeStep: TOP_TIME_RANGE[0].value,
    topMethod: TOP_TYPE[0].value,
    content: [],
  });

  useEffect(() => {
    getAsyncCheckedList();
    _getDictionary();
  }, []);

  useEffect(() => {
    if (Url().search?.node) {
      selectRadioValue.current.content = [Url().search?.node];
    }
  }, [Url().search?.node]);

  useEffect(() => {
    getAsyncNodeViewIpList();
  }, [clusterName]);

  const sortEnd = (item, { oldIndex, newIndex }) => {
    const listsNew = arrayMoveImmutable(checkedData[item], oldIndex, newIndex);
    checkedData[item] = listsNew;
    const checkedList = objFlat(checkedData);
    setCheckedList(NODE, checkedList);
    setCheckedData({ ...checkedData });
  };

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

  const getAsyncNodeViewIpList = () => {
    if (clusterName) {
      try {
        selectRadioValue.current.content = [];
        setUpdate(!update);
        getNodeInfoList(clusterName).then((list) => {
          setNodeIpList(
            list.map((item, index) => ({
              ...item,
              name: item.nodeName,
              value: item.nodeName,
              tips: NODE_TYPE[item.nodeType],
            }))
          );
        });
        selectRadioValue.current.content = Url().search?.cluster === clusterName && Url().search?.node ? [Url().search?.node] : [];
      } catch (error) {
        console.error(error);
      }
    }
  };

  const _getDictionary = async () => {
    let params = {
      model: "Node",
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

  const setIndexConfigCheckedData = (changeCheckedData) => {
    const checkedList = objFlat(changeCheckedData);
    setCheckedList(NODE, checkedList);
    setCheckedData(changeCheckedData);
    reloadPage();
  };

  const getAsyncNodeViewData = useCallback(
    async (metricsTypes) => {
      const values = (selectRadioValue.current.content || []).map((item) => {
        return item.split("_")?.[1];
      });
      let content = flag && Url().search?.node ? [Url().search?.node] : values;

      setFlag(false);
      return await getNodeViewData(
        metricsTypes,
        clusterName,
        startTime,
        endTime,
        selectRadioValue.current.topNum,
        content,
        selectRadioValue.current.topMethod,
        selectRadioValue.current.topTimeStep
      );
    },
    [clusterName, startTime, endTime, timeRadioKey]
  );

  const onSelectRadioChange = (values, needReload) => {
    selectRadioValue.current = values;
    if (needReload) {
      reloadPage();
    }
  };

  const renderTopWhat = () => {
    const options = [];
    for (let item of nodeIpList) {
      const index = options.findIndex((row) => row.value === item.nodeType);
      if (index < 0) {
        options.push({
          title: <span className="parent">{item.tips}</span>,
          value: item.nodeType,
          key: uuid(),
          children: [
            {
              title: <span className="child">{item.nodeName}</span>,
              value: item.nodeType + "_" + item.nodeName,
            },
          ],
        });
      } else {
        options[index].children.push({
          title: <span className="child">{item.nodeName}</span>,
          value: item.nodeType + "_" + item.nodeName,
          key: uuid(),
        });
      }
    }
    return (
      <SelectRadio
        onValueChange={onSelectRadioChange}
        content={selectRadioValue.current.content || []}
        contentList={options}
        placeholder="请选择节点名称"
        type="treeSelect"
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

  const ShowTaskTooltipModal = (clusterPhyName, node, time) => {
    dispatch(
      actions.setModalId("chartTableModal", {
        clusterPhyName,
        node,
        time,
      })
    );
  };

  useEffect(() => {
    window["showTaskTooltipModal"] = (clusterPhyName, node, time) => {
      ShowTaskTooltipModal(clusterPhyName, node, time);
    };
  }, []);

  const RenderContent = useMemo(
    () => (
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
                  <div className={`${classPrefix}-overview-content-line content-margin-top-20`}>
                    {checkedData[item] && checkedData[item].length ? (
                      <RenderLine
                        dictionary={dictionary}
                        metricsTypes={checkedData[item]}
                        key={item + index + selectRadioValue.current.topNum + clusterName}
                        configData={indexConfigData}
                        isMoreDay={isMoreDay}
                        getAsyncViewData={getAsyncNodeViewData}
                        startTime={startTime}
                        endTime={endTime}
                        clusterPhyName={clusterName}
                        sortEnd={sortEnd}
                        item={item}
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
    ),
    [startTime, endTime, timeRadioKey, checkedData, indexConfigClassifyList, update, dictionary]
  );

  return (
    <>
      <OperationPanel classPrefix={classPrefix} reloadPage={reloadPage} endTime={endTime} renderFilter={renderFilter} />
      {RenderContent}
    </>
  );
});
