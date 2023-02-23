import "./index.less";
import React, { useState } from "react";
import { Button } from "antd";
import { uuid } from "lib/utils";
export const RenderEmpty = (props) => {
  const {
    title = "无集群信息",
    desc = "请前往集群管理 ———— 「我的集群」，进行集群申请",
    href = `/cluster/logic?needApplyCluster=${uuid()}`,
    btnText = "申请集群",
  } = props;
  return (
    <div className="log-cluster-empty">
      <img src={require("../../assets/clusterLogEmpty.png")} alt="" />
      <div className="empty-title">{title}</div>
      <div className="empty-desc">{desc}</div>
      <div>
        <Button
          type="primary"
          onClick={() => {
            props.history.push(href);
          }}
          className="apply-button"
        >
          {btnText}
        </Button>
      </div>
    </div>
  );
};
