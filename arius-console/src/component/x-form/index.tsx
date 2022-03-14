import * as React from "react";
import { Button, Checkbox, DatePicker, Form, Input, InputNumber, Radio, Select, Switch, Upload, Cascader, Col, Row } from "antd";
import { UploadOutlined } from "@ant-design/icons";

const TextArea = Input.TextArea;
const { RangePicker } = DatePicker;

export enum FormItemType {
  input = "input",
  inputNumber = "input_number",
  textArea = "text_area",
  select = "select",
  _switch = "_switch",
  custom = "custom",
  checkBox = "check_box",
  datePicker = "date_picker",
  rangePicker = "range_picker",
  radioGroup = "radio_group",
  upload = "upload",
  text = "text",
  cascader = "cascader",
}

export interface IFormItem {
  key: string;
  label: string;
  type: FormItemType;
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
  CustomStyle?: any;
  customFormItem?: any;
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
interface IXFormProps {
  formMap: IFormItem[];
  formData: any;
  form?: any;
  wrappedComponentRef?: any;
  onFinish?: any;
  formLayout?: any;
  layout?: "inline" | "horizontal" | "vertical";
  onHandleValuesChange?: (value: any, allValues: object) => any;
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
    case FormItemType.select:
      return (
        <Select key={item.key} placeholder={item.attrs?.placeholder || "请选择"} {...item.attrs}>
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
      return <Checkbox.Group options={(item as IFormSelect).options} />;
    case FormItemType.datePicker:
      return <DatePicker key={item.key} {...item.attrs} />;
    case FormItemType.rangePicker:
      return <RangePicker key={item.key} {...item.attrs} />;
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
  let valuePropName = "value";

  if (formItem.type === FormItemType.datePicker) {
    initialValue = initialValue || null;
  }

  if (formItem.type === FormItemType._switch) {
    initialValue = formData[formItem.key] ? true : false;
  }

  if (formItem.type === FormItemType._switch) {
    valuePropName = "checked";
  }

  if (formItem.type === FormItemType.upload) {
    valuePropName = "fileList";
  }
  return { initialValue, valuePropName };
};

export const XForm: React.FC<IXFormProps> = (props: IXFormProps) => {
  const { layout, formLayout, formData, formMap, form, wrappedComponentRef, onHandleValuesChange } = props;
  const onUploadFileChange = (e: any) => {
    if (Array.isArray(e)) {
      return e;
    }
    return e && e.fileList;
  };

  const defaultLayout =
    layout === "vertical"
      ? null
      : formLayout
      ? formLayout
      : {
          labelCol: { span: 4 },
          wrapperCol: { span: 16 },
        };

  const renderTwoArray = (formMap) => {
    return (
      <Row gutter={[24, 16]}>
        <Col span={12}>{renderFormItemBox(formMap)[0]}</Col>
        <Col span={12}>{renderFormItemBox(formMap)[1]}</Col>
      </Row>
    );
  };

  const renderFormItemBox = (formMap) => {
    return formMap.map((formItem) => {
      const { initialValue = undefined, valuePropName } = handleFormItem(formItem, formData);
      if (Array.isArray(formItem)) {
        return renderTwoArray(formItem);
      }

      if (formItem.type === FormItemType.text)
        return (
          <div key={formItem.key} style={{ display: "flex" }}>
            <div className="ant-form-item-label">
              <label>{typeof formItem.label === "string" ? formItem.label + ":" : formItem.label}</label>
            </div>
            <div style={{ paddingLeft: 10 }}>{(formItem as IFormCustom).customFormItem}</div>
          </div>
        );
      return (
        !formItem.invisible && (
          <Form.Item
            name={formItem.key}
            key={formItem.key}
            label={formItem.label}
            rules={formItem.rules || [{ required: false, message: "" }]}
            initialValue={initialValue}
            valuePropName={valuePropName}
            className={formItem.isCustomStyle ? "ant-form-item-custom" : null} // 兼容负责人选择后表单样式变大
            style={formItem.isCustomStyle ? { marginBottom: 24, ...formItem.CustomStyle } : null}
            getValueFromEvent={formItem.type === FormItemType.upload ? onUploadFileChange : null}
            {...formItem.formAttrs}
          >
            {renderFormItem(formItem)}
          </Form.Item>
        )
      );
    });
  };

  return (
    <>
      <Form ref={wrappedComponentRef} form={form} {...defaultLayout} layout={layout || "horizontal"} onValuesChange={onHandleValuesChange}>
        {renderFormItemBox(formMap)}
      </Form>
    </>
  );
};
