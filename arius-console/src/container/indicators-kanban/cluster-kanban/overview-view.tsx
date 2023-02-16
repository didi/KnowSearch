import { SyncOutlined } from "@ant-design/icons";
import { Collapse } from "antd";
import { TOP_MAP } from "constants/status-map";
import React, { memo, useState, useEffect, useRef } from "react";
import { IndexConfig, Line } from "../components";
import { OverviewViewBasic } from "./overview-view-basic";
import { formatterTimeYMDHMS, objFlat } from "./config";
import { defaultIndexConfigList, allCheckedData, getCheckedData, indexConfigClassifyList } from "./overview-view-config";
import { LineShard } from "./overview-line-shard";
import { asyncMicroTasks, resize } from "../../../lib/utils";
import { getCheckedList, setCheckedList, getDictionary } from "../../../api/cluster-kanban";
const { Panel } = Collapse;
import "../style/index";
import { shallowEqual, useDispatch, useSelector } from "react-redux";
import { setIsUpdate } from "actions/cluster-kanban";
import { arrayMoveImmutable } from "array-move";
import { Divider } from "knowdesign";

const OVERVIEW = "overview";

export const classPrefix = "monitor";

export const OverviewView = memo(() => {
  const [checkedData, setCheckedData] = useState(getCheckedData([]));
  const [dictionary, setDictionary] = useState({});

  const dispatch = useDispatch();
  const { isUpdate, endTime } = useSelector(
    (state) => ({
      isUpdate: (state as any).clusterKanban.isUpdate,
      endTime: (state as any).clusterKanban.endTime,
    }),
    shallowEqual
  );

  useEffect(() => {
    getAsyncCheckedList();
    _getDictionary();
  }, []);

  const reloadPage = () => {
    dispatch(setIsUpdate(!isUpdate));
  };

  const sortEnd = (item, { oldIndex, newIndex }) => {
    const listsNew = arrayMoveImmutable(checkedData[item], oldIndex, newIndex);
    checkedData[item] = listsNew;
    const checkedList = objFlat(checkedData);
    setCheckedList(OVERVIEW, checkedList);
    setCheckedData({ ...checkedData });
  };

  const getAsyncCheckedList = async () => {
    try {
      const checkedList = await getCheckedList(OVERVIEW);
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

  const _getDictionary = async () => {
    let params = {
      model: "OverView",
    };
    let res = await getDictionary(params);
    let data = {} as any;
    (res || []).forEach((item) => {
      if (item?.metricType) {
        data[item?.metricType] = item;
      }
      if (item?.metricType === "sendTransSize" || item?.metricType === "recvTransSize") {
        data["networkFlow"] = item;
      }
    });
    setDictionary(data);
  };

  const setIndexConfigCheckedData = (changeCheckedData) => {
    const checkedList = objFlat(changeCheckedData);
    setCheckedList(OVERVIEW, checkedList);
    setCheckedData(changeCheckedData);
    reloadPage();
  };

  const renderConfig = () => {
    return (
      <IndexConfig
        title="总览指标配置"
        optionList={defaultIndexConfigList}
        checkedData={checkedData}
        setCheckedData={setIndexConfigCheckedData}
        needShortcut={true}
      />
    );
  };

  return (
    <>
      <div className={`${classPrefix}-overview-search`}>
        <div className={`${classPrefix}-overview-search-reload`}>
          <SyncOutlined className="dashboard-config-icon" onClick={reloadPage} />
        </div>
        <div className={`${classPrefix}-overview-search-filter`}>{renderConfig()}</div>
      </div>
      <div className={`${classPrefix}-overview-content`}>
        <div className={`${classPrefix}-overview-content-config`}>
          <div className={`${classPrefix}-overview-content-config-box`}>
            <OverviewViewBasic />
          </div>
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
                style={{ marginBottom: 20 }}
                key={item + index}
              >
                <Panel header={item} key={index}>
                  <div className={`${classPrefix}-overview-content-line content-margin-top-20`}>
                    {checkedData[item] && checkedData[item].length ? (
                      <LineShard
                        sortEnd={sortEnd}
                        item={item}
                        metricsTypes={checkedData[item]}
                        key={`${item}_${index}`}
                        dictionary={dictionary}
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
    </>
  );
});
