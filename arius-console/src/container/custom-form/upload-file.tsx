import React from 'react';
import  { Upload } from 'antd';
import { UploadOutlined } from '@ant-design/icons';

const { Dragger } = Upload;

export interface IUploadFileProps {
  name?: string;
  multiple?: boolean;
  action?: string;
  accept?: string;
  msg?: string;
  url?: any;
  onChange?: (result: any) => any;
}
export const UploadFile = (props: IUploadFileProps) => {

  const handleChange = (e: any) => {
    const { onChange } = props;
    onChange && onChange(e);
  }

  const {multiple, action, accept, msg, url} = props;

  const defaultFileList = [] as any;
    if (url) {
      defaultFileList[0] = {
          uid: 1,
          name: url,
          status: 'done',
      }
    }
  return (
    <>
        <Dragger
          beforeUpload={(file: any) => false}
          multiple={multiple}
          accept={accept}
          action={action}
          onChange={(e) => handleChange(e)}
          defaultFileList={defaultFileList}
          style={{width: '50%'}}
        >
          <p>
            <UploadOutlined style={{fontSize: 20, color: '#2F81F9'}}/>
          </p>
          <p>{msg ? msg : <p>单击或拖动文件到此区域以上传, 支持单、多个文件<br/>且为.gz格式文件。</p>}</p>
        </Dragger>
    </>);
}