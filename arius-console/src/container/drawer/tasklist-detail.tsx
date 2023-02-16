import React from "react";
import { Drawer, Descriptions } from "antd";
import "./index.less";

const DescriptionsItem = Descriptions.Item;

interface IProps {
  visible: boolean;
  detailData: any;
  onCancel: () => void;
}

const SchDulingLog: React.FC<IProps> = (props: IProps) => {
  const { detailData } = props;
  const typeMap = [
    {
      type: "任务名称",
      value: detailData?.taskDesc || "-",
    },
    {
      type: "路由策略",
      value: detailData?.routing || "-",
    },
    {
      type: "Cron",
      value: detailData?.cron || "-",
    },
    {
      type: "运行模式",
      value: detailData?.runningType || "-",
    },
    {
      type: "JobHandler",
      value: detailData?.className || "-",
    },
    {
      type: "运行参数",
      value: detailData?.params || "-",
    },
    {
      type: "阻塞处理策略",
      value: detailData?.blockPolicy || "-",
    },
    {
      type: "责任人",
      value: detailData?.owner || "-",
    },
  ];
  return (
    <Drawer title={"任务详情"} visible={props.visible} width={600} maskClosable={true} onClose={props.onCancel}>
      <div>
        <Descriptions className="tasklist-detail" column={1} labelStyle={{ justifyContent: "flex-end", minWidth: 100 }}>
          {typeMap.map((item, index) => (
            <DescriptionsItem key={index} label={item.type}>
              {item.value}
            </DescriptionsItem>
          ))}
        </Descriptions>
      </div>
    </Drawer>
  );
};
export default SchDulingLog;
