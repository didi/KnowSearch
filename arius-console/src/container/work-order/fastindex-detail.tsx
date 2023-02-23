import React, { useState, useEffect } from "react";
import { FASTINDEX_STATUS, FASTINDEX_STATS } from "constants/status-map";
import { getFastIndexDetail, cancelFastIndexTask, retryFastIndexTask, rollbackFastIndexTask } from "api/fastindex-api";
import { Button, Progress, message, Collapse } from "antd";
import { XModal } from "component/x-modal";
import FastindexLimitModal from "./fastindex-limit-modal";
import FastIndexLog from "./fastindex-log";
import Url from "lib/url-parser";
import "./index.less";

const { Panel } = Collapse;

export const FastIndexDetail: React.FC = () => {
  const urlParam: any = Url().search;
  const [data, setData]: any = useState({});
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    reloadData();
    const time = setInterval(() => {
      if (data?.status === "running") {
        reloadData();
      }
    }, 10000);
    return () => clearInterval(time);
  }, []);

  const reloadData = async () => {
    let data = await getFastIndexDetail(Number(urlParam?.taskid));
    setData(data);
  };

  const renderItems = (childrenList) => {
    let list = (childrenList || []).map((item) => {
      return (
        <div className="index-item" key={item?.indexName}>
          <span className="title">{item?.indexName}</span>
          <span className={`status ${FASTINDEX_STATS[item.taskStatus]?.className || "cancel"}`}>
            {FASTINDEX_STATS[item.taskStatus]?.text || "未执行"}
          </span>
          <span className="total-count">
            <span>总文档数：{item?.totalDocumentNum || 0}</span>
            <span>（成功数量：{item?.succDocumentNum || 0} </span>
            <span className="failed-num">失败数量：{item?.failedDocumentNum || 0}）</span>
          </span>
        </div>
      );
    });
    return list;
  };

  const renderIndex = () => {
    return (
      <div className="index-container">
        <div className="text">迁移索引：</div>
        {(data?.fastIndexStats?.childrenList || []).map((item) => (
          <div className="index-item" key={item?.indexName}>
            <span className="title">{item?.indexName}</span>
            <span className={`status ${FASTINDEX_STATS[item.taskStatus]?.className || "cancel"}`}>
              {FASTINDEX_STATS[item.taskStatus]?.text || "未执行"}
            </span>
            <span className="total-count">
              <span>总文档数：{item?.totalDocumentNum || 0}</span>
              <span>（成功数量：{item?.succDocumentNum || 0} </span>
              <span className="failed-num">失败数量：{item?.failedDocumentNum || 0}）</span>{" "}
            </span>
          </div>
        ))}
      </div>
    );
  };

  const renderTemplate = () => {
    return (
      <div className="template-container">
        <div className="text">迁移模板：</div>
        <Collapse className="template-content" defaultActiveKey={[data?.fastIndexStats?.childrenList?.[0]?.templateId]}>
          {(data?.fastIndexStats?.childrenList || []).map((item) => {
            return (
              <Panel
                header={
                  <div className="title">
                    <span className="text">{item?.templateName}</span>
                    <span className={`status ${FASTINDEX_STATS[item?.taskStatus]?.className || "cancel"}`}>
                      {FASTINDEX_STATS[item?.taskStatus]?.text || "未执行"}
                    </span>
                  </div>
                }
                key={item.templateId}
              >
                {renderItems(item?.childrenList)}
              </Panel>
            );
          })}
        </Collapse>
      </div>
    );
  };

  const renderButton = () => {
    let expandData = data?.expandData ? JSON.parse(data?.expandData) : {};
    let transferStatus = expandData?.transferStatus;
    return (
      <div className="fastindex-button-container">
        {data?.status === "success" && urlParam.datatype == 1 && transferStatus === 1 && (
          <Button
            className="rollback-task"
            type="primary"
            onClick={() => {
              let rollbackTitle = `确定回切任务“${data?.title}”？`;
              let content = <div className="content">回切后，已迁移的数据会全部回切到源集群中。</div>;
              let onOk = async () => {
                await rollbackFastIndexTask(data.id);
                message.success("操作成功");
                reloadData();
              };
              XModal({ type: "warning", title: rollbackTitle, content, onOk });
            }}
          >
            回切
          </Button>
        )}
        {(data?.status === "waiting" || data?.status === "running") && (
          <Button
            className="cancel-task"
            type="primary"
            onClick={() => {
              XModal({
                type: "info",
                title: `确认取消任务${data?.title}`,
                onOk: async () => {
                  await cancelFastIndexTask(data?.id);
                  message.success("操作成功");
                  reloadData();
                },
              });
            }}
          >
            取消
          </Button>
        )}
        {(data?.status === "failed" || data?.status === "cancel") && (
          <Button
            className="retry-task"
            type="primary"
            onClick={() => {
              let title = `确定重试任务${data?.title}？`;
              let writeType = JSON.parse(data?.expandData)?.writeType;
              let content = (
                <div>
                  <div>
                    {writeType === 1
                      ? " 指定ID+版本号的写入方式，ID冲突时，以高版本文档为主，进行覆盖或者丢弃"
                      : writeType === 2
                      ? "指定ID的写入方式，ID冲突时，进行覆盖写入"
                      : "指定ID的写入方式，ID冲突时，丢弃当前写入"}
                  </div>
                  <div>重试会清除目标索引的全部数据，包括近期写入的数据，请谨慎操作。</div>
                </div>
              );
              let onOk = async () => {
                await retryFastIndexTask(data.id);
                message.success("操作成功");
                reloadData();
              };
              XModal({ type: "warning", title, content, className: "retry-fastindex-modal", onOk });
            }}
          >
            重试
          </Button>
        )}
        {data?.status === "running" && (
          <Button className="update-limit" type="primary" onClick={() => setVisible(true)}>
            修改限流值
          </Button>
        )}
      </div>
    );
  };

  return (
    <div className="fastindex-detail-container">
      <div className="fastindex-detail-header">
        <span className="title">任务详情</span>
        {renderButton()}
      </div>
      <div className="fastindex-progress">
        <div className="left-content">
          执行进度：
          <Progress
            percent={
              data?.status === "success"
                ? 100
                : data?.status === "running"
                ? Math.floor(
                    (data?.fastIndexStats?.succDocumentNum + data?.fastIndexStats?.failedDocumentNum) /
                      data?.fastIndexStats?.totalDocumentNum
                  )
                : 0
            }
            strokeColor={"#1473FF"}
          />
          <span className={`text ${FASTINDEX_STATUS[data?.status || "waiting"]?.className}`}>
            {FASTINDEX_STATUS[data?.status || "waiting"].text}
          </span>
          {data?.status === "running" && (
            <>
              <span className="divide"></span>
              <span className="second-number">{data?.fastIndexStats?.indexMoveRate || 0}条/S</span>
            </>
          )}
        </div>
        <div className="right-content">
          <span>总文档数：{data?.fastIndexStats?.totalDocumentNum || 0}</span>
          <span>（成功：{data?.fastIndexStats?.succDocumentNum || 0} </span>
          <span className="failed-num">失败：{data?.fastIndexStats?.failedDocumentNum || 0}）</span>
        </div>
      </div>
      <div className="index-template">{urlParam.datatype == 2 ? renderIndex() : renderTemplate()}</div>
      <FastIndexLog />
      <FastindexLimitModal
        visible={visible}
        onClose={() => setVisible(false)}
        taskId={data?.id}
        reloadData={reloadData}
        indexMoveRate={data?.fastIndexStats?.indexMoveRate}
      ></FastindexLimitModal>
    </div>
  );
};
