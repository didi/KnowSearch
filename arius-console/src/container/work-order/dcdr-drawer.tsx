import React, { useState, useEffect, useRef } from "react";
import { Drawer, Descriptions, Timeline, Button, Modal, message, Tooltip } from "antd";
import "./dcdr.less";
import { CheckOutlined, SyncOutlined, CloseOutlined } from "@ant-design/icons";
import { getTemplateDcdrDetail, dcdrForceSwitch, dcdrRefresh } from "api/dcdr-api";
import { transTimeFormat } from "lib/utils";

interface IDcdrDrawer {
  taskId: number;
  templateId: number;
  visible: boolean;
  onCancel: () => void;
  parentReload: () => void;
}

export const DescList = [
  {
    title: "所属集群(主):",
    dataIndex: "masterCluster",
    render: (value) => value || "-",
  },
  {
    title: "所属集群(从):",
    dataIndex: "slaveCluster",
    render: (value) => value || "-",
  },
  {
    title: "创建时间:",
    dataIndex: "createTime",
    render: (value) => transTimeFormat(value),
  },
  {
    title: "完成时间:",
    dataIndex: "updateTime",
    render: (value) => transTimeFormat(value),
  },
  {
    title: "任务类型:",
    dataIndex: "switchType",
    render: (value) => (value == 1 ? "平滑切换" : "强制切换"),
  },
];

export const DcdrDrawer: React.FC<IDcdrDrawer> = (props: IDcdrDrawer) => {
  const [data, setData]: any = useState({});
  // 依赖data 利用ref避过effect依赖监测
  const ref: any = useRef();

  const renderTitle = (text) => {
    return <div className="dcdr-drawer-title">{text}</div>;
  };

  const reloadData = () => {
    if (props.visible) {
      getTemplateDcdrDetail(props.taskId, props.templateId).then((res) => {
        ref.current = res;
        setData(res);
      });
    }
  };

  useEffect(() => {
    reloadData();
    const time = setInterval(() => {
      if (ref.current && ref.current?.taskStatus !== 0 && ref.current?.taskStatus !== 3 && ref.current?.taskStatus !== 1) {
        reloadData();
      }
    }, 10 * 1000);
    return () => {
      clearInterval(time);
    };
  }, [props.visible, props.taskId, props.templateId, ref]);

  const renderDesc = () => {
    return (
      <div>
        {renderTitle("基本信息")}
        <div style={{ padding: 24, marginBottom: 8 }}>
          <Descriptions column={2} bordered>
            {DescList.map((item) => (
              <Descriptions.Item label={item.title} key={item.title}>
                {item && item.render ? item.render(data[item?.dataIndex]) : data[item?.dataIndex]}
              </Descriptions.Item>
            ))}
          </Descriptions>
        </div>
      </div>
    );
  };

  const renderStep = () => {
    let todo = false;
    let fail = false;
    return (
      <div>
        {renderTitle("执行进度")}
        <div style={{ padding: 24 }}>
          <Timeline>
            {data?.taskProgressList?.map((item, index) => {
              const values = item.split("@@@");
              let icon = (
                <div className="dcdr-drawer-step-icon">
                  <div className="dcdr-drawer-step-icon-text">{index + 1}</div>
                </div>
              );
              if (values && values[1] === "DONE") {
                icon = (
                  <div className="dcdr-drawer-step-icon-done">
                    <CheckOutlined style={{ marginTop: 4 }} />
                  </div>
                );
              }
              if (values && values[1] === "TODO" && !todo && !fail) {
                todo = true;
                icon = (
                  <div className="dcdr-drawer-step-icon-todo">
                    <SyncOutlined style={{ marginTop: 4 }} />
                  </div>
                );
              }
              if (values && values[1] === "FAIL" && !fail) {
                fail = true;
                icon = (
                  <div className="dcdr-drawer-step-icon-fail">
                    <CloseOutlined style={{ marginTop: 4 }} />
                  </div>
                );
              }
              return (
                <Timeline.Item dot={icon} key={index}>
                  <div>
                    <div className="dcdr-drawer-step-title">{values[0]}</div>
                    <div className="dcdr-drawer-step-text">{values[2]}</div>
                  </div>
                </Timeline.Item>
              );
            })}
          </Timeline>
        </div>
      </div>
    );
  };

  const handleDcdrForceSwitch = () => {
    Modal.confirm({
      title: `提示`,
      content: `确定进行强制切换吗？`,
      // icon: <DeleteOutlined style={{ color: "red" }} />,
      // width: 500,
      okText: "确定",
      cancelText: "取消",
      onOk() {
        dcdrForceSwitch(props.taskId, props.templateId).then((res) => {
          message.success("操作成功");
          reloadData();
          props.parentReload();
        });
      },
    });
  };

  const handleDcdrRefresh = () => {
    Modal.confirm({
      title: `提示`,
      content: `确定重试该任务吗？`,
      // icon: <DeleteOutlined style={{ color: "red" }} />,
      // width: 500,
      okText: "确定",
      cancelText: "取消",
      onOk() {
        dcdrRefresh(props.taskId, props.templateId).then((res) => {
          message.success("操作成功");
          reloadData();
          props.parentReload();
        });
      },
    });
  };

  const renderFooter = () => {
    return (
      <div>
        <Button
          className="dcdr-drawer-footer-button"
          type="primary"
          disabled={!(data && data.taskStatus === 3)}
          onClick={handleDcdrRefresh}
        >
          重试
        </Button>
        {(data && data.switchType === 2) || data.taskStatus === 1 || data.taskStatus === 0 ? null : (
          <Button className="dcdr-drawer-footer-button" type="primary" onClick={handleDcdrForceSwitch}>
            强制切换
          </Button>
        )}
        <Button onClick={props?.onCancel}>返回</Button>
      </div>
    );
  };

  return (
    <Drawer
      title={
        <div style={{ fontSize: 14, color: "#1D2330" }}>
          <Tooltip title={data?.taskTitle || "任务详情"}>
            {(data?.taskTitle && data?.taskTitle.length > 20 ? `${data?.taskTitle.substring(0, 20)}...` : data?.taskTitle) || "任务详情"}
          </Tooltip>
        </div>
      }
      visible={props?.visible}
      onClose={props?.onCancel}
      width={700}
      maskClosable={true}
      bodyStyle={{ padding: 0 }}
      footer={renderFooter()}
    >
      {renderDesc()}
      {renderStep()}
    </Drawer>
  );
};
