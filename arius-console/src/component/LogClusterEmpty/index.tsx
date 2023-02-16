import "./index.less";
import React, { useState } from "react";
import { Button } from "antd";
import { uuid } from "lib/utils";
export const RenderEmpty = (props) => {
  return (
    <div className="log-cluster-empty">
      <img src={require("../../assets/clusterLogEmpty.png")} alt="" />
      <div className="empty-title">无集群信息</div>
      <div className="empty-desc">请前往集群管理 ———— 「我的集群」，进行集群申请</div>
      <div>
        <Button
          type="primary"
          onClick={() => {
            props.history.push(`/cluster/logic?needApplyCluster=${uuid()}`);
          }}
          className="apply-button"
        >
          申请集群
        </Button>
      </div>
    </div>
  );
};
