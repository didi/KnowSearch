import React from 'react';
import { Drawer, Descriptions } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';

const DescriptionsItem = Descriptions.Item;

interface IProps {
  visible: boolean;
  error: string;
  onCancel: () => void;
}

const SchDulingLog: React.FC<IProps> = (props: IProps) => {

  const getWidth = () => {
    return document.querySelector('#d1-layout-main')?.clientWidth || 600;
  }
  return (
    <Drawer
      title={'执行日志'}
      visible={props.visible}
      width={getWidth()}
      maskClosable={true}
      onClose={props.onCancel}
    >
      {/* <ReloadOutlined onClick={() => console.log(1)} style={reloadstyle} /> */}
      <div>
        <Descriptions column={1}>
          {
            JSON.parse(props?.error || '{}')?.message?.split('\n')?.map((item, index) => <DescriptionsItem key={index}>{item}</DescriptionsItem>)
          }
        </Descriptions>
        [Load Log Finish]
      </div>
    </Drawer>
  );
}
export default SchDulingLog;


