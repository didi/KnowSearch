import { ReloadOutlined } from "@ant-design/icons";
import { Collapse } from "antd";
import { TOP_MAP } from "constants/status-map";
import React, { memo, useState, useEffect, useRef } from "react";
import { IndexConfig, Line } from "../components";
import { OverviewViewBasic } from "./overview-view-basic";
import { formatterTimeYMDHMS, objFlat } from "./config";
import {
  defaultIndexConfigList,
  allCheckedData,
  getCheckedData,
  indexConfigClassifyList,
} from "./overview-view-config";
import { LineShard } from "./overview-line-shard";
import { asyncMicroTasks, resize } from "../../../lib/utils";
import { getCheckedList, setCheckedList } from "../../../api/cluster-kanban";
const { Panel } = Collapse;
import "../style/index";
import { shallowEqual, useDispatch, useSelector } from "react-redux";
import { setIsUpdate } from "actions/cluster-kanban";

const OVERVIEW = "overview";

export const classPrefix = "rf-monitor";

export const OverviewView = memo(() => {
  const [checkedData, setCheckedData] = useState(getCheckedData([]));
  const dispatch = useDispatch();
  const { isUpdate, endTime } = useSelector(
    (state) => ({
      isUpdate: (state as any).clusterKanban.isUpdate,
      endTime: (state as any).clusterKanban.endTime,
    }),
    shallowEqual
  );

  const reloadPage = () => {
    dispatch(setIsUpdate(!isUpdate));
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

  const setIndexConfigCheckedData = (changeCheckedData) => {
    const checkedList = objFlat(changeCheckedData);
    setCheckedList(OVERVIEW, checkedList);
    setCheckedData(changeCheckedData);
    reloadPage();
  };

  useEffect(() => {
    getAsyncCheckedList();
  }, []);

  const renderConfig = () => {
    return (
      <IndexConfig
        title="总览指标配置"
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
          {renderConfig()}
        </div>
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
                  <div
                    className={`${classPrefix}-overview-content-line content-margin-top-20`}
                  >
                    {
                      checkedData[item] && checkedData[item].length ? <LineShard metricsTypes={checkedData[item]} key={`${item}_${index}`} /> : ""
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
