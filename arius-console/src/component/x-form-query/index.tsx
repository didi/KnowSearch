import React from 'react';
import '@ant-design/compatible/assets/index.css';
import { Button, Form}  from 'antd';
import './index.less';
import { IFormItem, renderFormItem } from 'component/x-form';

interface IForm {
  formMap: IFormItem[];
  submit: (result: any) => any;
}

interface IXFormProps {
  formMap: IFormItem[];
  submit: (result: any) => any;
}

export const DFormQuery = (props: IForm) => {

  const { formMap } = props;

  return (
    <>
      <div className="qurey-create-box">
        <QueryForm
          formMap={formMap}
          submit={props.submit}
        />
      </div>
    </>
  );

}

const QueryForm = (props: IXFormProps) => {
  const [form] = Form.useForm();

  const onSubmit = (value: any) => {
    props.submit(value);
  }

  const handleReset = () => {
    form.resetFields();
  };

  const { formMap } = props;
  return (
    <Form layout='inline' form={form} name="control-hooks" onFinish={onSubmit}>
      {formMap.map((formItem) => {
        return (
          !formItem.invisible &&
          <Form.Item
            key={formItem.key}
            name={formItem.key}
            label={formItem.label}
            initialValue={formItem.defaultValue || ''}
          >
            {
              renderFormItem(formItem)
            }
          </Form.Item>
        );
      })}
      <div style={{ flex: 1, display: 'flex', justifyContent: 'flex-end' }}>
        <div></div>
        <div>
          <Form.Item>
            <Button type="primary" htmlType="submit">
              {'查询'}
            </Button>
          </Form.Item>
          <Form.Item style={{ marginRight: 0 }}>
            <Button onClick={handleReset} >
              {'重置'}
            </Button>
          </Form.Item>
        </div>
      </div>
    </Form>
  );
}
