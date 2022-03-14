---
order: 0
title: 基本
---

这是一个基础的动态表单。

```jsx
import React, { useState } from 'react';
import { MinusCircleOutlined } from '@ant-design/icons';
import { PlusCircleOutlined } from '@ant-design/icons';
import { useDynamicList, Form, Button, Input } from 'antd-advanced';

const Demo = props => {
  const { list, remove, getKey, push } = useDynamicList(['David', 'Jack']);
  const [form] = Form.useForm();
  const { validateFields } = form;
  const [result, setResult] = useState('');
  const Row = (index, item) => (
    <Form.Item 
      key={getKey(index)} 
      rules={[{required: true, message: 'required'}]}>
      <Form.Item 
          noStyle
          name={`names[${getKey(index)}]`} 
          rules={[{required: true, message: 'required'}]}
      >
        <Input style={{ width: 300 }} placeholder="Please enter your name" />
      </Form.Item >
      {list.length > 1 && (
        <MinusCircleOutlined 
         style={{ marginLeft: 8 }}
          onClick={() => {
            remove(index);
          }}
        />
      )}
      <PlusCircleOutlined
       style={{ marginLeft: 8 }}
        onClick={() => {
          push('');
        }}
      />
    </Form.Item>
  );

  const listValue = (list) => {
    const obj = {}
    list.map((ele, index) => {
      obj[`names[${index}]`] = ele;
    })
    return obj;
  }
  return (
    <>
      <Form initialValues={listValue(list)} form={form}>
        {list.map((ele, index) => Row(index, ele))}
      </Form>
      <Button
        style={{ marginTop: 8 }}
        type="primary"
        onClick={() =>
          validateFields().then(val => setResult(JSON.stringify(Object.values(val))))
        }
      >
        Submit
      </Button>
      <div>{result}</div>
    </>
  );
}

ReactDOM.render(<Demo />, mountNode);
```
