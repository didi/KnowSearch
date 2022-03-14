import * as React from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import { Modal, Spin, Tabs } from "antd";
import { getTaskLog } from "api/task-api";
import { ITaskLog } from "typesPath/task-types";

const { TabPane } = Tabs;

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const TaskLogModal = connect(mapStateToProps)(
  (props: { dispatch: any; params: any }) => {
    const [loading, setLoading] = React.useState(false);
    const [taskLog, setTaskLog] = React.useState({} as ITaskLog);

    React.useEffect(() => {
      reloadData();
    }, []);

    const reloadData = () => {
      setLoading(true);
      getTaskLog(props.params)
        .then((res) => {
          setTaskLog(res);
        })
        .finally(() => {
          setLoading(false);
        });
    };

    return (
      <>
        <Modal
          visible={true}
          title="日志详情"
          width={700}
          onOk={() => props.dispatch(actions.setModalId(""))}
          onCancel={() => props.dispatch(actions.setModalId(""))}
          okText={"确定"}
          cancelText={"取消"}
        >
          <Spin spinning={loading}>
            <div>
              <Tabs defaultActiveKey="1">
                <TabPane tab="详情列表" key="1">
                  <div className="line-break">{taskLog.agent || '任务未执行完成，请稍后再查看日志。'}</div>
                </TabPane>
                <TabPane tab="部署日志" key="2">
                  <div className="line-break">{taskLog.user || '任务未执行完成，请稍后再查看日志。'}</div>
                </TabPane>
              </Tabs>
            </div>
          </Spin>
        </Modal>
      </>
    );
  }
);
