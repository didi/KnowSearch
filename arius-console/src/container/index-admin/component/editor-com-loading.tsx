import React, { memo } from "react"
import { Spin } from "antd"
import { EditorCom } from "component/editor"

interface propsType {
  loading: boolean;
  data: any
}

export const EditorComLoading: React.FC<propsType> = memo(({ loading, data }) => {
  return <Spin spinning={loading}>
    <div style={{ height: 900 }} >
      {
        data ? <EditorCom
          options={{
            language: 'json',
            value: data,
            theme: 'vs',
            automaticLayout: true,
            readOnly: true,
          }}
        /> : ""
      }
    </div>
  </Spin>
})