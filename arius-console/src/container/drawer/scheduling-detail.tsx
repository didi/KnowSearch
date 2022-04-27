import { Drawer, Card, Descriptions } from 'antd';
import React from 'react';

const DescriptionsItem = Descriptions.Item;

interface IProps {
  visible: boolean;
  record: any;
  onCancel: () => void;
}

const SchDulingDetail: React.FC<IProps> = (props: IProps) => {
  const { record } = props;
  const renderDetail = () => {
    return (
      <Card title="基本信息" key="1" type="inner">
        <Descriptions column={1}>
          <DescriptionsItem label="地址列表">{record?.allWorkerIps?.map(item => `${item},`)}</DescriptionsItem>
        </Descriptions>
      </Card>
    )
  }

  const renderHandleSchduling = () => {
    const rusult = JSON.parse(record?.result || '{}');
    return (
        <Card title="触发调度" key="2" type="inner" style={{ marginTop: 20 }}>
          <Descriptions column={1}>
            <DescriptionsItem label="address">{record?.workerIp}</DescriptionsItem>
            <DescriptionsItem label="code">{rusult?.code}</DescriptionsItem>
            <DescriptionsItem label="message">{rusult?.message}</DescriptionsItem>
          </Descriptions>
        </Card>
    )
  }
  return (
    <Drawer
      title={'调度详情'}
      visible={props.visible}
      width={600}
      maskClosable={true}
      onClose={props.onCancel}
    >
      {renderDetail()}
      {renderHandleSchduling()}
    </Drawer>
  );
}
export default SchDulingDetail;


