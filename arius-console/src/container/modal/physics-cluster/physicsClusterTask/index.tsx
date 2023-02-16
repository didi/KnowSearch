import React, { useState } from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import { AppState, UserState } from "store/type";
import { XFormWrapper } from "component/x-form-wrapper";
import { IFormItem } from "component/x-form";
import { Tabs, Spin, Button } from "antd";
import {
  abnormalShardRetry,
  clearFieldDataMemory,
  nodeState,
  indicesDistribute,
  shardDistribute,
  task_mission_analysis,
  pendTaskAnalysis,
  hotThreadAnalysis,
  shardAssignDescription,
} from "api/cluster-api";
import {
  getPhysicsColumns,
  node_state,
  shard_distribution,
  pending_TaskAnalysis,
  task_Analysis,
  shard_DistributionExplain,
} from "./config";
import { HotTask } from "./codeMirror";
import { PhysicsClusterTable } from "./table";
import { ShardTables } from "./shardTable";
import { IndicesTables } from "./indicesTable";
import { ShardDistributeTabs } from "./shardDistributeTable";
import "./index.less";
import { ErrorSvg, OkSvg } from "./svg";
const { TabPane } = Tabs;

const TabButton = (props) => {
  const [isShow, setIsShow] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [Retry, setRetry] = useState("");

  return (
    <div>
      {isShow && (
        <div className="button-container">
          <span className="test-container">
            <span className="icon iconfont iconinfo"></span>
            <span>{props.tabKey === "8" ? "POST/_cluster/reroute?retry_failed=true" : "POST _cache/clear?fielddata=true"}</span>
          </span>
          <Button
            type="primary"
            loading={isLoading}
            className="button-style"
            onClick={() => {
              setIsLoading(true);
              props
                .network(props.props.params.cluster)
                .then((res) => {
                  res && res.message === "操作失败" ? setRetry("执行失败") : setRetry("执行成功");
                  setIsShow(false);
                })
                .catch((rej) => {
                  setRetry("执行失败");
                })
                .finally(() => {
                  setIsLoading(false);
                  setIsShow(false);
                });
            }}
          >
            执行
          </Button>
        </div>
      )}
      {!isShow && (
        <Spin spinning={isLoading} tip="Loading...">
          <div className="error-container">
            <div className="error-gif">{Retry === "执行成功" ? <OkSvg /> : <ErrorSvg />}</div>
            <div className="error-desc">
              <span>{Retry === "执行成功" ? Retry : `${Retry}请重试`}</span>
            </div>
            {Retry === "执行成功" ? (
              ""
            ) : (
              <div className="retry-button">
                <Button
                  type="primary"
                  loading={isLoading}
                  className="button-style2"
                  onClick={() => {
                    setIsLoading(true);
                    props
                      .network(props.props.params.cluster)
                      .then((res) => {
                        res && res.message === "操作失败" ? setRetry("执行失败") : setRetry("执行成功");
                        setIsShow(false);
                      })
                      .catch((rej) => {
                        setRetry("执行失败");
                      })
                      .finally(() => {
                        setIsLoading(false);
                        setIsShow(false);
                      });
                  }}
                >
                  执行
                </Button>
              </div>
            )}
          </div>
        </Spin>
      )}
    </div>
  );
};

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const PhysicsClusterTask = (props: { dispatch: any; cb: Function; app: AppState; user: UserState; params: any }) => {
  const [currentTabKey, setCurrentTabKey] = useState("");
  //tab点击请求接口
  const changeTabs = (activeKey) => {
    setCurrentTabKey(activeKey);
  };
  const customRenderElementFun = () => {
    return (
      <div className="quick-commands">
        <Tabs activeKey={currentTabKey} onChange={changeTabs} destroyInactiveTabPane>
          <TabPane tab="node_state分析" key="1">
            <PhysicsClusterTable
              {...props}
              network={nodeState}
              columns={node_state}
              hasSearch
              placeholder="请输入关键字，支持node_name"
              keyword="nodeName"
            />
          </TabPane>
          <TabPane tab="indices分布" key="2">
            <IndicesTables {...props} network={indicesDistribute} columns={getPhysicsColumns} />
          </TabPane>
          <TabPane tab="shard分布" key="3">
            <ShardDistributeTabs {...props} network={shardDistribute} columns={shard_distribution} />
          </TabPane>
          <TabPane tab="pending task分析" key="4">
            <PhysicsClusterTable {...props} network={pendTaskAnalysis} columns={pending_TaskAnalysis} />
          </TabPane>
          <TabPane tab="task任务分析" key="5">
            <PhysicsClusterTable
              {...props}
              network={task_mission_analysis}
              columns={task_Analysis}
              hasSearch
              placeholder="请输入关键字，支持nodeName"
              keyword="node"
            />
          </TabPane>
          <TabPane tab="热点线程分析" key="6">
            <HotTask {...props} network={hotThreadAnalysis} />
          </TabPane>
          {/* <TabPane tab="shard分配说明" key="7">
            <ShardTables {...props} network={shardAssignDescription} columns={shard_DistributionExplain} />
          </TabPane>
          <TabPane tab="异常shard分配重试" key="8">
            <TabButton props={props} network={abnormalShardRetry} tabKey={currentTabKey} />
          </TabPane>
          <TabPane tab="清除fielddata内存" key="9">
            <TabButton props={props} network={clearFieldDataMemory} tabKey={currentTabKey} />
          </TabPane> */}
        </Tabs>
        {!currentTabKey && (
          <div className="tab-empty">
            <img src={require("../../../../assets/yellow-receipts@2x.png")} alt="" />
            <div className="tab-desc">请点击执行快捷命令</div>
          </div>
        )}
      </div>
    );
  };
  const xFormModalConfig = {
    formMap: [] as IFormItem[],
    visible: true,
    title: "快捷命令",
    bodyStyle: { Padding: 0 },
    formData: { gatewayUrl: props.params.gatewayUrl },
    onCancel: () => {
      props.dispatch(actions.setDrawerId(""));
    },
    onSubmit: async () => {},
    width: 1080,
    customRenderElement: customRenderElementFun(),
    noform: true,
    type: "drawer",
    nofooter: true,
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(PhysicsClusterTask);
