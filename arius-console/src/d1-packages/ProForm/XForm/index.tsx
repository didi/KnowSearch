import * as React from 'react';
import {
  Button,
  Checkbox,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Radio,
  Select,
  Switch,
  Upload,
  Cascader,
  Row,
  Col,
  TimePicker,
  AutoComplete,
  TreeSelect
} from 'antd';
import { UploadOutlined } from '@ant-design/icons';
import { useMemo } from 'react';
import Submitter, { SubmitterProps } from '../Submitter';
import { useState } from 'react';

const TextArea = Input.TextArea;
const { RangePicker } = DatePicker;

export enum FormItemType {
  input = 'input',
  inputNumber = 'input_number',
  textArea = 'text_area',
  select = 'select',
  _switch = '_switch',
  custom = 'custom',
  checkBox = 'check_box',
  datePicker = 'date_picker',
  rangePicker = 'range_picker',
  radioGroup = 'radio_group',
  upload = 'upload',
  text = 'text',
  cascader = 'cascader',
  autoComplete = 'autoComplete',
  timePicker = 'timePicker',
  treeSelect = 'treeSelect',
}

export interface IFormItem {
  key: string;
  label: string;
  type?: FormItemType;
  value?: string;
  options?: any[];
  // 内部组件属性注入
  attrs?: any;
  // form属性注入
  formAttrs?: any;
  defaultValue?: string | number | any[];
  rules?: any[];
  invisible?: boolean;
  extraElement?: JSX.Element;
  colSpan?: any;
  customClassName?: any;
  radioType?: boolean;
  isCustomStyle?: boolean;
  customFormItem?: any;
  treeData?: any[];
}

export interface IFormSelect extends IFormItem {
  options: Array<{
    key?: string | number;
    value: string | number;
    label: string;
    radioType?: boolean;
    disabled?: boolean;
  }>;
}

interface IFormCustom extends IFormItem {
  customFormItem: React.Component;
}
export interface IXFormProps {
  formMap: IFormItem[];
  formData: any;
  form?: any;
  wrappedComponentRef?: any;
  onFinish?: any;
  formLayout?: any;
  layout?: 'inline' | 'horizontal' | 'vertical';
  onHandleValuesChange?: (value: any, allValues: object) => any;
  formItemColSpan?: number;
  contentRender?: any;
  submitter?: any;
}

export const renderFormItem = (item: IFormItem) => {
  switch (item.type) {
    default:
    case FormItemType.input:
      return <Input key={item.key} {...item.attrs} />;
    case FormItemType.inputNumber:
      return <InputNumber {...item.attrs} />;
    case FormItemType.textArea:
      return <TextArea rows={2} {...item.attrs} />;
    case FormItemType.autoComplete:
      return (
        <AutoComplete
          key={item.key}
          options={item.options}
          allowClear={item.attrs?.allowClear || true}
          placeholder={item.attrs?.placeholder || '请输入'}
          filterOption={(inputValue, option) =>
            option!.value.indexOf(inputValue) !== -1
          }
          {...item.attrs}
        />
      );
    case FormItemType.select:
      return (
        <Select key={item.key} placeholder={item.attrs?.placeholder || '请选择'} {...item.attrs}>
          {(item as IFormSelect).options &&
            (item as IFormSelect).options.map((v, index) => (
              <Select.Option key={v.value || v.key || index} value={v.value} disabled={v.disabled}>
                {v.label || v.value}
              </Select.Option>
            ))}
        </Select>
      );
    case FormItemType._switch:
      return <Switch {...item.attrs} />;
    case FormItemType.custom:
      return (item as IFormCustom).customFormItem;
    case FormItemType.checkBox:
      return <Checkbox.Group options={(item as IFormSelect).options} {...item.attrs} />;
    case FormItemType.datePicker:
      return <DatePicker key={item.key} {...item.attrs} />;
    case FormItemType.rangePicker:
      return <RangePicker key={item.key} {...item.attrs} />;
    case FormItemType.timePicker:
      return <TimePicker key={item.key} {...item.attrs} />;
    case FormItemType.autoComplete:
      return (
        <AutoComplete
          key={item.key}
          options={item.options}
          allowClear={item.attrs?.allowClear || true}
          placeholder={item.attrs?.placeholder || "请输入"}
          filterOption={
            item.attrs?.filterOption
              ? item.attrs?.filterOption
              : (inputValue: any, option) =>
                  option!.value.indexOf(inputValue) !== -1
          }
          {...item.attrs}
        />
      );
    case FormItemType.treeSelect:
      return (
        <TreeSelect
          key={item.key}
          treeData={item.treeData}
          placeholder={item.attrs?.placeholder || '请选择'}
          {...item.attrs}
        />
      );
    case FormItemType.radioGroup:
      return (
        <Radio.Group key={item.key} {...item.attrs}>
          {(item as IFormSelect).options.map((v, index) => {
            if (v.radioType) {
              return (
                <Radio.Button key={v.value || v.key || index} value={v.value}>
                  {v.label}
                </Radio.Button>
              );
            }
            return (
              <Radio key={v.value || v.key || index} value={v.value}>
                {v.label}
              </Radio>
            );
          })}
        </Radio.Group>
      );
    case FormItemType.upload:
      return (
        <Upload beforeUpload={false} {...item.attrs}>
          <Button>
            <UploadOutlined />
            上传
          </Button>
        </Upload>
      );
    case FormItemType.cascader:
      return <Cascader key={item.key} options={(item as IFormSelect).options} {...item.attrs} />;
  }
};

export const handleFormItem = (formItem: any, formData: any) => {
  let initialValue = formData[formItem.key] || formItem.defaultValue || undefined;
  let valuePropName = 'value';

  if (formItem.type === FormItemType.datePicker) {
    initialValue = initialValue || null;
  }

  if (formItem.type === FormItemType._switch) {
    initialValue = formData[formItem.key] ? true : false;
  }

  if (formItem.type === FormItemType._switch) {
    valuePropName = 'checked';
  }

  if (formItem.type === FormItemType.upload) {
    valuePropName = 'fileList';
  }
  return { initialValue, valuePropName };
};

const onUploadFileChange = (e: any) => {
  if (Array.isArray(e)) {
    return e;
  }
  return e && e.fileList;
};

export const renderFormContent = ({ formMap, formData, layout, formLayout, formItemColSpan = 24}: any) => {
  return formMap.map((formItem) => {
    const { initialValue = undefined, valuePropName } = handleFormItem(formItem, formData);
    if (formItem.type === FormItemType.text)
      return (
        !formItem.invisible && (
          <Col key={formItem.key} span={formItem.colSpan || formItemColSpan}>
            {layout === 'vertical' ? (
              <>
                <span style={{ padding: '0 0 5px', display: 'block', color: '#919AAC', fontSize: '12px' }}>{formItem.label}</span>
                <span style={{ fontSize: '14px', padding: '0 0 16px', display: 'block' }}>{(formItem as IFormCustom).customFormItem}</span>
              </>
            ) : (
              <Row style={{ padding: '6px 0 10px' }}>
                <Col span={formLayout?.labelCol.span || 4} style={{ textAlign: 'right' }}>
                  <span style={{ padding: '0 10px 0 0', display: 'inline-block' }}>{formItem.label}:</span>
                </Col>
                <Col span={formLayout?.wrapperCol.span || 20}>
                  <span>{(formItem as IFormCustom).customFormItem}</span>
                </Col>
              </Row>
            )}
          </Col>
        )
      );
    return (
      !formItem.invisible && (
        <Col key={formItem.key} span={formItem.colSpan || formItemColSpan}>
          <Form.Item
            name={formItem.key}
            key={formItem.key}
            label={formItem.label}
            rules={formItem.rules || [{ required: false, message: '' }]}
            initialValue={initialValue}
            valuePropName={valuePropName}
            className={formItem.isCustomStyle ? 'ant-form-item-custom' : null} // 兼容负责人选择后表单样式变大
            style={formItem.isCustomStyle ? { marginBottom: 24 } : null}
            getValueFromEvent={formItem.type === FormItemType.upload ? onUploadFileChange : null}
            {...formItem.formAttrs}
          >
            {renderFormItem(formItem)}
          </Form.Item>
        </Col>
      )
    );
  });
};

export const XForm: React.FC<IXFormProps> = (props: IXFormProps) => {
  const {
    layout,
    formLayout,
    formData,
    formMap,
    form,
    wrappedComponentRef,
    onHandleValuesChange,
    contentRender,
    submitter,
    ...rest
  } = props;

  const defaultLayout =
    layout === 'vertical'
      ? null
      : formLayout
      ? formLayout
      : {
          labelCol: { span: 4 },
          wrapperCol: { span: 20 },
        };

  const submitterProps: SubmitterProps = useMemo(() => (typeof submitter === 'boolean' || !submitter ? {} : submitter), [submitter]);

  const [loading, setLoading] = useState<boolean>(false);

  const submitterNode = (() => {
    if (submitter === false) return undefined;
    return (
      <Submitter
        key="submitter"
        {...submitterProps}
        form={form}
        submitButtonProps={{
          loading,
          ...submitterProps.submitButtonProps,
        }}
      />
    );
  })();

  const content = useMemo(() => {
    if (contentRender) {
      return contentRender(submitterNode);
    }
    return null;
  }, [contentRender, submitterNode]);

  return (
    <>
      <Form
        ref={wrappedComponentRef}
        form={form}
        {...defaultLayout}
        layout={layout || 'horizontal'}
        onValuesChange={onHandleValuesChange}
        onFinish={async () => {
          if (!rest.onFinish) return;
          if (loading) return;
          setLoading(true);
          try {
            const finalValues = form.getFieldsValue();
            await rest.onFinish(finalValues);
            setLoading(false);
          } catch (error) {
            setLoading(false);
          }
        }}
      >
        <Row gutter={10}>
          {content ? content : renderFormContent({ ...props })}
        </Row>         
      </Form>
    </>
  );
};
