import { ReloadOutlined } from "@ant-design/icons";
import { TOP_MAP } from "constants/status-map";
import React, { memo, useState, useEffect, useCallback, useRef } from "react";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import { Collapse, Select } from "antd";
import _ from "lodash";
import {
  indexConfigClassifyList,
  defaultIndexConfigList,
  allCheckedData,
  getCheckedData,
} from "./clientnode-config";
import { objFlat, getOption, formatterTimeYMDHMS } from "../config";
import { indexConfigData } from "./clientnode-config";
import { RenderLine, IndexConfig, SelectRadio, Line } from "../components";
import {
  getCheckedList,
  setCheckedList,
  getNodeIpList,
  getClientNodeViewData,
  getClientNodeList,
} from "../../../api/gateway-kanban";
import "../style/index";
import { setIsUpdate } from "actions/gateway-kanban";
import { arrayMoveImmutable } from 'array-move';
import DragGroup from './../../../packages/drag-group/DragGroup';
const { Panel } = Collapse;

export const classPrefix = "rf-monitor";

const CLINETNODE = "clientNode";

export const ClientNodeView = memo(() => {
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

  const reloadPage = () => {
    dispatch(setIsUpdate(!isUpdate));
  };

  const [topNu, setTopNu] = useState(TOP_MAP[0].value);
  const [nodeIp, setNodeIp] = useState("");
  const [clientNodeIp, setClientNodeIp] = useState("");
  const [nodeIpList, setNodeIpList] = useState([]);
  const [clientNodeList, setclientNodeList] = useState([]);
  const [checkedData, setCheckedData] = useState(getCheckedData([]));
  const [metricsTypes, setMetricsTypes] = useState([]);
  const [viewData, setViewData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const isFirst = useRef(true);
  const timeDiff = useRef(0);
  const prevTopNu = useRef(topNu);

  const sortEnd = ({ oldIndex, newIndex }) => {
    const listsNew = arrayMoveImmutable(checkedData['ClientNode性能指标'], oldIndex, newIndex)
    checkedData['ClientNode性能指标'] = listsNew;
    const checkedList = objFlat(checkedData);
    setCheckedList(CLINETNODE, checkedList);
    setMetricsTypes([...listsNew]);
  };

  const getAsyncCheckedList = async () => {
    try {
      const checkedList = await getCheckedList(CLINETNODE);
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

  const getAsyncClientNodeViewList = async () => {
    try {
      const clientNodeList = await getClientNodeList(nodeIp, startTime, endTime);
      setclientNodeList(clientNodeList);
    } catch (error) {
      console.log(error);
    }
  };

  const setIndexConfigCheckedData = (changeCheckedData) => {
    const checkedList = objFlat(changeCheckedData);
    setCheckedList(CLINETNODE, checkedList);
    setCheckedData(changeCheckedData);
    reloadPage();
  };

  const getAsyncViewData = useCallback(
    async (metricsTypes) => {
      return await getClientNodeViewData(
        metricsTypes,
        startTime,
        endTime,
        topNu,
        nodeIp,
        clientNodeIp,
      );
    },
    [startTime, endTime, topNu, nodeIp, clientNodeIp, isUpdate]
  );

  const getAllAsyncViewData = async (metricsTypes) => {
    try {
      setIsLoading(true);
      let res = await getAsyncViewData(metricsTypes);
      // 去空
      res = res.filter(item => {
        return item.metricsContents && item.metricsContents.length;
      })
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
    getAsyncCheckedList();
  }, []);

  useEffect(() => {
    getAsyncNodeViewIpList();
  }, []);

  useEffect(() => {
    getAsyncClientNodeViewList();
  }, [nodeIp, startTime, endTime]);

  useEffect(() => {
    if (topNu !== 0 && clientNodeIp) {
      setClientNodeIp('');
    }
  }, [topNu]);

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
        content={nodeIp}
        setContent={setNodeIp}
        contentList={nodeIpList}
        placeholder="请选择Gateway节点"
        allowClear={true}
        type="Gateway"
      />
    );
  };

  const renderClinetNode = () => {
    return (
      <Select
        placeholder="请选择ESClient节点"
        onChange={(e: string) => {
          setClientNodeIp(e || '');
          if (e) {
            setTopNu(0);
          } else {
            setTopNu(TOP_MAP[0].value);
          }
        }}
        value={clientNodeIp || null}
        showSearch
        filterOption={(val, option) => { return option.children.includes(val.trim()) }}
        style={{ marginRight: 8, width: 200 }}
        allowClear
      >
        {clientNodeList.map((item) => <Select.Option value={item} key={item}>{item}</Select.Option>)}
      </Select>
    );
  };

  const renderConfig = () => {
    return (
      <IndexConfig
        title="ClientNode指标配置"
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
          {renderClinetNode()}
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
