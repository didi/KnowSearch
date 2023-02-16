import { TOP_MAP, TOP_TIME_RANGE, TOP_TYPE } from "constants/status-map";
import React, { memo, useState, useEffect, useCallback, useRef } from "react";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import _ from "lodash";
import { defaultIndexConfigList, allCheckedData, getCheckedData } from "./clientnode-config";
import { objFlat, getOption } from "../config";
import { getRenderToolTip } from "./config";
import { indexConfigData } from "./clientnode-config";
import { IndexConfig, SelectRadio, Line } from "../components";
import { getCheckedList, setCheckedList, getNodeIpList, getClientNodeViewData, getClientNodeList } from "../../../api/gateway-kanban";
import "../style/index";
import { setIsUpdate } from "actions/gateway-kanban";
import { arrayMoveImmutable } from "array-move";
import DragGroup from "../../../d1-packages/drag-group/DragGroup";
import { OperationPanel } from "../components/operation-panel";
import { copyString } from "lib/utils";

export const classPrefix = "monitor";

const CLINETNODE = "clientNode";

export const ClientNodeView = memo(() => {
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

  const selectRadioValue = useRef({
    topNum: TOP_MAP[0].value,
    topTimeStep: TOP_TIME_RANGE[0].value,
    topMethod: TOP_TYPE[0].value,
    content: undefined,
    secondValue: "",
  });

  const reloadPage = () => {
    dispatch(setIsUpdate(!isUpdate));
  };

  const [clientNodeIp, setClientNodeIp] = useState("");
  const [nodeIpList, setNodeIpList] = useState([]);
  const [clientNodeList, setclientNodeList] = useState([]);
  const [checkedData, setCheckedData] = useState(getCheckedData([]));
  const [metricsTypes, setMetricsTypes] = useState([]);
  const [viewData, setViewData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [clientNodeLoading, setClientNodeLoading] = useState(false);

  const isFirst = useRef(true);
  const timeDiff = useRef(0);
  const prevTopNu = useRef(TOP_MAP[0].value);

  const sortEnd = ({ oldIndex, newIndex }) => {
    const listsNew = arrayMoveImmutable(checkedData["ClientNode性能指标"], oldIndex, newIndex);
    checkedData["ClientNode性能指标"] = listsNew;
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
    setClientNodeLoading(true);
    try {
      const clientNodeList = await getClientNodeList(selectRadioValue.current.content, startTime, endTime);
      const newClientNodeList = clientNodeList.map((item) => {
        return {
          name: `${item.v1}_${item.v2}`,
          value: item.v2,
        };
      });

      selectRadioValue.current.secondValue = newClientNodeList?.[0]?.value || "";
      selectRadioValue.current.topNum = newClientNodeList.length ? 0 : selectRadioValue.current.topNum;

      setClientNodeLoading(false);
      setclientNodeList(newClientNodeList);
    } catch (error) {
      console.log(error);
      setClientNodeLoading(false);
    }
  };

  const setIndexConfigCheckedData = (changeCheckedData) => {
    const checkedList = objFlat(changeCheckedData);
    setCheckedList(CLINETNODE, checkedList);
    setCheckedData(changeCheckedData);
    reloadPage();
  };

  const getAsyncViewData = async (metricsTypes) => {
    return await getClientNodeViewData(
      metricsTypes,
      startTime,
      endTime,
      selectRadioValue.current.topNum,
      selectRadioValue.current.content,
      selectRadioValue.current.secondValue,
      selectRadioValue.current.topMethod,
      selectRadioValue.current.topTimeStep
    );
  };

  const getAllAsyncViewData = async (metricsTypes) => {
    if (clientNodeLoading) return;
    try {
      setIsLoading(true);
      let res = await getAsyncViewData(metricsTypes);
      // 去空
      res = res.filter((item) => {
        return item.metricsContents && item.metricsContents.length;
      });
      setViewData(
        res.map((item) =>
          getOption({
            metrics: item,
            configData: indexConfigData,
            isMoreDay,
            isShowTooltipModal: true,
            needShowClusterName: true,
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
    getAsyncClientNodeViewList();
  }, [startTime, endTime, timeRadioKey]);

  useEffect(() => {
    if (selectRadioValue.current.topNum !== 0 && selectRadioValue.current.secondValue) {
      setClientNodeIp("");
      selectRadioValue.current.secondValue = "";
    }
  }, [selectRadioValue.current.topNum]);

  useEffect(() => {
    setMetricsTypes(objFlat(checkedData));
  }, [checkedData]);

  useEffect(() => {
    if (isFirst.current || timeDiff.current !== endTime - startTime || prevTopNu.current !== selectRadioValue.current.topNum) {
      // setIsLoading(true);
      timeDiff.current = endTime - startTime;
      isFirst.current = false;
      prevTopNu.current = selectRadioValue.current.topNum;
    }
    if (!metricsTypes || metricsTypes.length === 0) {
      return;
    }
    getAllAsyncViewData(metricsTypes);
  }, [metricsTypes, clientNodeLoading, clientNodeIp]);

  const onSecondSelectChange = (value) => {
    setClientNodeIp(value);
    selectRadioValue.current.secondValue = value;
  };

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
        placeholder="请选择Gateway节点"
        allowClear={true}
        type="secondSelect"
        topNu={selectRadioValue.current.topNum}
        secondSelectList={clientNodeList}
        secondSelectValue={clientNodeIp}
        secondSelectPlaceholder={"请选择ESClient节点"}
        onSecondSelectChange={onSecondSelectChange}
      />
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

  const renderFilter = () => {
    return (
      <>
        {clientNodeLoading ? null : renderTopWhat()}
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
