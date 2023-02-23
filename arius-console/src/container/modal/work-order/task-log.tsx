import React, { useState, useEffect } from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import { Drawer, Spin, Tabs } from "antd";
import { getStderrLog, getStdoutLog } from "api/task-api";
import Url from "lib/url-parser";
import "./index.less";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const TaskLogModal = connect(mapStateToProps)((props: { dispatch: any; params: any }) => {
  const [loading, setLoading] = useState(false);
  const [taskLog, setTaskLog] = useState("");

  useEffect(() => {
    reloadData();
  }, []);

  const reloadData = async () => {
    setLoading(true);
    let { host, groupName } = props.params;
    let id = Number(Url().search.taskid);
    try {
      if (props.params.status === 2) {
        // status为2时任务为失败状态，调失败接口
        let res = await getStderrLog(id, host, groupName);
        setTaskLog(res);
      } else {
        let res = await getStdoutLog(id, host, groupName);
        setTaskLog(res);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Drawer visible={true} title="日志详情" width={700} onClose={() => props.dispatch(actions.setModalId(""))}>
        <Spin spinning={loading}>
          <div className="task-log-content">{taskLog || "任务未执行完成，请稍后再查看日志。"}</div>
        </Spin>
      </Drawer>
    </>
  );
});
