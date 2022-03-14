import React from 'react';
import { Alert } from 'antd';
import TextLoop from 'react-text-loop';
import './index.less';

interface IProps {
  messages: string[]
}

export const Announcement = (props: IProps) => {
  return (
    <div className="msg-alert mb-20">
      <Alert
        banner
        closable
        message={
          <TextLoop mask>
            {
              props?.messages.map((item, index) => (
                <div key={index}>{item}</div>
              ))
            }
          </TextLoop>
        }
      />
    </div>

  );
}